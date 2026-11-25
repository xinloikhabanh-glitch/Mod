package com.aibuilder;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FourWayBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Quan ly cac cong trinh dang xay: dat khoi dan dan moi tick, huy, hoan tac. Chi goi tu server thread. */
public final class BuildManager {

    private static final int HISTORY_SIZE = 3;
    private static final int BLOCK_FLAGS = 18; // 2 (gui client) | 16 (khong kich hoat cap nhat lang gieng)

    private static final Set<String> BLOCKED = new HashSet<String>(Arrays.asList(
            "command_block", "repeating_command_block", "chain_command_block", "structure_block",
            "jigsaw", "bedrock", "barrier", "end_portal", "end_gateway"));

    private static final List<Job> ACTIVE = new ArrayList<Job>();
    private static final Map<UUID, Deque<Job>> HISTORY = new HashMap<UUID, Deque<Job>>();
    private static final Set<UUID> REQUESTING = new HashSet<UUID>();

    private BuildManager() {
    }

    // ------------------------------------------------------------------ requests

    public static boolean beginRequest(UUID id) {
        return REQUESTING.add(id);
    }

    public static void endRequest(UUID id) {
        REQUESTING.remove(id);
    }

    public static void clear() {
        ACTIVE.clear();
        HISTORY.clear();
        REQUESTING.clear();
    }

    public static void message(MinecraftServer server, UUID id, String text) {
        ServerPlayerEntity p = server.getPlayerList().getPlayer(id);
        if (p != null) {
            p.sendMessage(new StringTextComponent(text), Util.NIL_UUID);
        }
    }

    // ---------------------------------------------------------------------- job

    private static final class Job {
        final ServerWorld world;
        final UUID owner;
        final String name;
        final int[] x, y, z;
        final BlockState[] states;
        final BlockState[] old; // null voi job hoan tac
        final boolean undo;
        int index = 0;
        int fix = 0;
        int placed = 0;
        int skipped = 0;
        int lastPct = 0;

        Job(ServerWorld world, UUID owner, String name, int[] x, int[] y, int[] z,
            BlockState[] states, boolean undo) {
            this.world = world;
            this.owner = owner;
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.states = states;
            this.undo = undo;
            this.old = undo ? null : new BlockState[x.length];
        }

        /** @return true khi da xong. */
        boolean step() {
            int budget = AIConfig.BLOCKS_PER_TICK.get();
            int total = x.length;

            while (budget > 0 && index < total) {
                BlockPos pos = new BlockPos(x[index], y[index], z[index]);
                if (!world.hasChunkAt(pos)) {
                    skipped++;
                } else {
                    BlockState os = world.getBlockState(pos);
                    BlockState ns = states[index];
                    if (!undo) {
                        old[index] = os;
                    }
                    if (os != ns) {
                        world.setBlock(pos, ns, BLOCK_FLAGS);
                    }
                    placed++;
                }
                index++;
                budget--;
            }

            if (index >= total && !undo) {
                // Giai doan 2: tinh lai hang rao, kinh, tuong, cau thang cho noi voi nhau.
                while (budget > 0 && fix < total) {
                    if (old[fix] != null) {
                        BlockPos pos = new BlockPos(x[fix], y[fix], z[fix]);
                        BlockState cur = world.getBlockState(pos);
                        Block b = cur.getBlock();
                        if (b instanceof FourWayBlock || b instanceof WallBlock || b instanceof StairsBlock) {
                            BlockState upd = Block.updateFromNeighbourShapes(cur, world, pos);
                            if (upd != cur) {
                                world.setBlock(pos, upd, BLOCK_FLAGS);
                            }
                        }
                    }
                    fix++;
                    budget--;
                }
            }

            boolean done = index >= total && (undo || fix >= total);
            if (done) {
                String msg = (undo ? "Da hoan tac " : "Xay xong \"" + name + "\"! Da dat ") + placed + " khoi.";
                if (skipped > 0) {
                    msg += " (Bo qua " + skipped + " khoi vi chunk chua duoc tai - hay dung gan hon hoac tang view distance.)";
                }
                message(world.getServer(), owner, msg);
                return true;
            }

            long units = undo ? index : (index + fix) / 2;
            int pct = (int) (units * 100L / Math.max(1, total));
            if (pct >= lastPct + 25) {
                lastPct = pct / 25 * 25;
                message(world.getServer(), owner, (undo ? "Dang hoan tac " : "Dang xay \"" + name + "\" ") + lastPct + "%...");
            }
            return false;
        }
    }

    public static void tick() {
        if (ACTIVE.isEmpty()) {
            return;
        }
        Iterator<Job> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Job j = it.next();
            boolean done;
            try {
                done = j.step();
            } catch (RuntimeException e) {
                message(j.world.getServer(), j.owner, "Loi khi xay: " + e);
                e.printStackTrace();
                done = true;
            }
            if (done) {
                it.remove();
            }
        }
    }

    // ------------------------------------------------------------------- actions

    /** Huy cac cong trinh dang xay cua nguoi choi (khoi da dat van con, dung /aibuild undo de go). */
    public static int cancel(UUID owner) {
        int n = 0;
        Iterator<Job> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Job j = it.next();
            if (j.owner.equals(owner) && !j.undo) {
                it.remove();
                n++;
            }
        }
        return n;
    }

    /** @return thong bao cho nguoi choi. */
    public static String undo(UUID owner) {
        Deque<Job> h = HISTORY.get(owner);
        if (h == null || h.isEmpty()) {
            return "Khong co cong trinh nao de hoan tac.";
        }
        Job j = h.pollFirst();
        ACTIVE.remove(j);

        int n = 0;
        for (int i = 0; i < j.old.length; i++) {
            if (j.old[i] != null) {
                n++;
            }
        }
        if (n == 0) {
            return "Cong trinh \"" + j.name + "\" chua dat khoi nao nen khong can hoan tac.";
        }
        int[] ux = new int[n];
        int[] uy = new int[n];
        int[] uz = new int[n];
        BlockState[] us = new BlockState[n];
        int k = 0;
        for (int i = j.old.length - 1; i >= 0; i--) { // go tu tren xuong
            if (j.old[i] != null) {
                ux[k] = j.x[i];
                uy[k] = j.y[i];
                uz[k] = j.z[i];
                us[k] = j.old[i];
                k++;
            }
        }
        ACTIVE.add(new Job(j.world, owner, j.name, ux, uy, uz, us, true));
        return "Dang hoan tac \"" + j.name + "\" (" + n + " khoi)...";
    }

    public static void start(ServerWorld world, UUID owner, BlockPos origin, Direction facing,
                             BlueprintCore.Result r) {
        MinecraftServer server = world.getServer();
        int n = r.entries.size();

        // Mat truoc cua blueprint huong BAC (-z). Xoay de mat truoc huong ve phia nguoi choi.
        int q;
        Rotation rot;
        switch (facing) {
            case WEST:
                q = 1;
                rot = Rotation.CLOCKWISE_90;
                break;
            case NORTH:
                q = 2;
                rot = Rotation.CLOCKWISE_180;
                break;
            case EAST:
                q = 3;
                rot = Rotation.COUNTERCLOCKWISE_90;
                break;
            default: // SOUTH
                q = 0;
                rot = Rotation.NONE;
                break;
        }

        int[] rx = new int[n];
        int[] rz = new int[n];
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            BlueprintCore.Entry e = r.entries.get(i);
            int px, pz;
            switch (q) {
                case 1:
                    px = -e.z;
                    pz = e.x;
                    break;
                case 2:
                    px = -e.x;
                    pz = -e.z;
                    break;
                case 3:
                    px = e.z;
                    pz = -e.x;
                    break;
                default:
                    px = e.x;
                    pz = e.z;
                    break;
            }
            rx[i] = px;
            rz[i] = pz;
            minX = Math.min(minX, px);
            maxX = Math.max(maxX, px);
            minZ = Math.min(minZ, pz);
            maxZ = Math.max(maxZ, pz);
        }

        int d = AIConfig.DISTANCE.get();
        int offX, offZ;
        if (facing.getStepZ() != 0) {
            offZ = facing.getStepZ() < 0 ? origin.getZ() - d - maxZ : origin.getZ() + d - minZ;
            offX = origin.getX() - Math.floorDiv(minX + maxX, 2);
        } else {
            offX = facing.getStepX() < 0 ? origin.getX() - d - maxX : origin.getX() + d - minX;
            offZ = origin.getZ() - Math.floorDiv(minZ + maxZ, 2);
        }

        Map<String, BlockState> cache = new HashMap<String, BlockState>();
        Set<String> warnings = new LinkedHashSet<String>();
        int[] ax = new int[n];
        int[] ay = new int[n];
        int[] az = new int[n];
        BlockState[] st = new BlockState[n];
        for (int i = 0; i < n; i++) {
            BlueprintCore.Entry e = r.entries.get(i);
            ax[i] = rx[i] + offX;
            ay[i] = e.y + origin.getY();
            az[i] = rz[i] + offZ;
            st[i] = resolve(e.block, cache, rot, warnings);
        }

        Job job = new Job(world, owner, r.name, ax, ay, az, st, false);
        ACTIVE.add(job);
        Deque<Job> h = HISTORY.get(owner);
        if (h == null) {
            h = new ArrayDeque<Job>();
            HISTORY.put(owner, h);
        }
        h.addFirst(job);
        while (h.size() > HISTORY_SIZE) {
            h.removeLast();
        }

        message(server, owner, "Bat dau xay \"" + r.name + "\" (" + n + " khoi, " + r.opCount + " lenh). "
                + "Dung /aibuild cancel de dung, /aibuild undo de go.");
        if (r.clamped) {
            message(server, owner, "Luu y: mot so khoi nam ngoai gioi han (maxRadius/chieu cao) nen da bi bo.");
        }
        int shown = 0;
        for (String w : warnings) {
            if (shown++ >= 5) {
                message(server, owner, "...va " + (warnings.size() - 5) + " canh bao khac.");
                break;
            }
            message(server, owner, "Canh bao: " + w);
        }
    }

    // ------------------------------------------------------------------- blocks

    private static BlockState parse(String id) {
        try {
            BlockStateParser p = new BlockStateParser(new StringReader(id), false).parse(false);
            return p.getState();
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    private static BlockState resolve(String id, Map<String, BlockState> cache, Rotation rot, Set<String> warnings) {
        BlockState st = cache.get(id);
        if (st != null) {
            return st;
        }
        st = parse(id);
        if (st == null) {
            int br = id.indexOf('[');
            String base = br >= 0 ? id.substring(0, br) : id;
            st = br >= 0 ? parse(base) : null;
            if (st != null) {
                warnings.add("thuoc tinh khong hop le, dung mac dinh: " + id);
            } else {
                warnings.add("block khong ton tai (thay bang stone_bricks): " + id);
                st = Blocks.STONE_BRICKS.defaultBlockState();
            }
        }
        if (BLOCKED.contains(st.getBlock().getRegistryName().getPath())) {
            warnings.add("block bi cam (thay bang stone_bricks): " + id);
            st = Blocks.STONE_BRICKS.defaultBlockState();
        }
        st = st.rotate(rot);
        cache.put(id, st);
        return st;
    }
}
