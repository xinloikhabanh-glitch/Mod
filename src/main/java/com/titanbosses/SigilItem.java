package com.titanbosses;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.RegistryObject;

/** Chuot phai vao mat dat de trieu hoi boss (tieu hao 1 sigil, tru che do Creative). */
public class SigilItem extends Item {
    private final RegistryObject<? extends EntityType<?>> bossType;

    public SigilItem(RegistryObject<? extends EntityType<?>> bossType, Properties props) {
        super(props);
        this.bossType = bossType;
    }

    @Override
    public ActionResultType useOn(ItemUseContext ctx) {
        World level = ctx.getLevel();
        if (!(level instanceof ServerWorld)) {
            return ActionResultType.SUCCESS;
        }
        ServerWorld world = (ServerWorld) level;
        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        PlayerEntity player = ctx.getPlayer();

        Entity boss = bossType.get().spawn(world, ctx.getItemInHand(), player, pos, SpawnReason.MOB_SUMMONED, true, false);
        if (boss == null) {
            return ActionResultType.FAIL;
        }
        if (player == null || !player.isCreative()) {
            ctx.getItemInHand().shrink(1);
        }
        world.getServer().getPlayerList().broadcastMessage(
                new StringTextComponent("Boss ").append(boss.getDisplayName()).append(new StringTextComponent(" da xuat hien!")),
                ChatType.SYSTEM, Util.NIL_UUID);
        return ActionResultType.CONSUME;
    }
}
