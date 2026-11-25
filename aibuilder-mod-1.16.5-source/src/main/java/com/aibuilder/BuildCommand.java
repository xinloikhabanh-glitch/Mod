package com.aibuilder;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Pattern;

public final class BuildCommand {

    private static final Pattern FILE_NAME = Pattern.compile("[A-Za-z0-9_\\-]+");

    private BuildCommand() {
    }

    public static void register(CommandDispatcher<CommandSource> d) {
        final int level = AIConfig.PERMISSION_LEVEL.get();
        d.register(Commands.literal("aibuild")
                .requires(s -> s.hasPermission(level))
                .then(Commands.literal("undo").executes(c -> undo(c.getSource())))
                .then(Commands.literal("cancel").executes(c -> cancel(c.getSource())))
                .then(Commands.literal("demo").executes(c -> demo(c.getSource())))
                .then(Commands.literal("file")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(c -> file(c.getSource(), StringArgumentType.getString(c, "name")))))
                .then(Commands.argument("prompt", StringArgumentType.greedyString())
                        .executes(BuildCommand::ask)));
    }

    private static void info(CommandSource s, String text) {
        s.sendSuccess(new StringTextComponent(text), false);
    }

    private static int fail(CommandSource s, String text) {
        s.sendFailure(new StringTextComponent(text));
        return 0;
    }

    // -------------------------------------------------------------- subcommands

    private static int undo(CommandSource s) throws CommandSyntaxException {
        ServerPlayerEntity p = s.getPlayerOrException();
        info(s, BuildManager.undo(p.getUUID()));
        return 1;
    }

    private static int cancel(CommandSource s) throws CommandSyntaxException {
        ServerPlayerEntity p = s.getPlayerOrException();
        int n = BuildManager.cancel(p.getUUID());
        if (n == 0) {
            return fail(s, "Khong co cong trinh nao dang xay.");
        }
        info(s, "Da dung xay. Dung /aibuild undo de go phan da xay.");
        return 1;
    }

    private static int demo(CommandSource s) throws CommandSyntaxException {
        ServerPlayerEntity p = s.getPlayerOrException();
        try {
            String json = AIClient.loadResource("aibuilder_demo.json");
            BlueprintCore.Result r = BlueprintCore.parse(json, AIConfig.MAX_BLOCKS.get(), AIConfig.MAX_RADIUS.get());
            BuildManager.start((ServerWorld) p.level, p.getUUID(), p.blockPosition(), p.getDirection(), r);
            return 1;
        } catch (IOException | BlueprintCore.BlueprintException e) {
            return fail(s, "Loi: " + e.getMessage());
        }
    }

    private static int file(CommandSource s, String name) throws CommandSyntaxException {
        ServerPlayerEntity p = s.getPlayerOrException();
        if (!FILE_NAME.matcher(name).matches()) {
            return fail(s, "Ten file khong hop le.");
        }
        Path f = blueprintDir().resolve(name + ".json");
        if (!Files.isRegularFile(f)) {
            return fail(s, "Khong tim thay " + f);
        }
        try {
            String json = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
            BlueprintCore.Result r = BlueprintCore.parse(json, AIConfig.MAX_BLOCKS.get(), AIConfig.MAX_RADIUS.get());
            BuildManager.start((ServerWorld) p.level, p.getUUID(), p.blockPosition(), p.getDirection(), r);
            return 1;
        } catch (IOException | BlueprintCore.BlueprintException e) {
            return fail(s, "Loi: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ AI build

    private static int ask(CommandContext<CommandSource> c) throws CommandSyntaxException {
        final CommandSource s = c.getSource();
        final ServerPlayerEntity p = s.getPlayerOrException();
        final String prompt = StringArgumentType.getString(c, "prompt").trim();

        if (AIConfig.API_KEY.get().trim().isEmpty() && !AIConfig.API_URL.get().contains("localhost")
                && !AIConfig.API_URL.get().contains("127.0.0.1")) {
            return fail(s, "Chua dat apiKey. Mo config/aibuilder-common.toml va dien apiKey, apiUrl, model.");
        }
        final UUID id = p.getUUID();
        if (!BuildManager.beginRequest(id)) {
            return fail(s, "AI van dang thiet ke cong trinh truoc cua ban, vui long doi.");
        }

        // Chot vi tri ngay luc go lenh: cong trinh se xuat hien dung cho ban dang dung.
        final MinecraftServer server = s.getServer();
        final ServerWorld world = (ServerWorld) p.level;
        final BlockPos origin = p.blockPosition();
        final Direction facing = p.getDirection();
        final int maxBlocks = AIConfig.MAX_BLOCKS.get();
        final int maxRadius = AIConfig.MAX_RADIUS.get();

        info(s, "Dang nho AI thiet ke: \"" + prompt + "\" (co the mat 30-180 giay)...");

        AIClient.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    String text = AIClient.ask(prompt);
                    final BlueprintCore.Result r = BlueprintCore.parse(text, maxBlocks, maxRadius);
                    saveBlueprint(prompt, text);
                    server.execute(new Runnable() {
                        @Override
                        public void run() {
                            BuildManager.start(world, id, origin, facing, r);
                        }
                    });
                } catch (final Exception e) {
                    server.execute(new Runnable() {
                        @Override
                        public void run() {
                            BuildManager.message(server, id, "Loi: " + e.getMessage());
                        }
                    });
                } finally {
                    server.execute(new Runnable() {
                        @Override
                        public void run() {
                            BuildManager.endRequest(id);
                        }
                    });
                }
            }
        });
        return 1;
    }

    // --------------------------------------------------------------- blueprints

    private static Path blueprintDir() {
        return FMLPaths.CONFIGDIR.get().resolve("aibuilder").resolve("blueprints");
    }

    /** Luu ban thiet ke de dung lai bang /aibuild file <ten>. Loi luu file khong anh huong viec xay. */
    private static void saveBlueprint(String prompt, String text) {
        try {
            String slug = Normalizer.normalize(prompt.toLowerCase(java.util.Locale.ROOT), Normalizer.Form.NFD)
                    .replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
            if (slug.length() > 30) {
                slug = slug.substring(0, 30);
            }
            if (slug.isEmpty()) {
                slug = "building";
            }
            Path dir = blueprintDir();
            Files.createDirectories(dir);
            String json;
            try {
                json = BlueprintCore.extractJson(text);
            } catch (BlueprintCore.BlueprintException e) {
                return;
            }
            Files.write(dir.resolve(System.currentTimeMillis() / 1000 + "-" + slug + ".json"),
                    json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            // bo qua
        }
    }
}
