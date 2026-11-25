package com.aibuilder;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;

public final class AIConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> API_TYPE;
    public static final ForgeConfigSpec.ConfigValue<String> API_URL;
    public static final ForgeConfigSpec.ConfigValue<String> API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> MODEL;
    public static final ForgeConfigSpec.ConfigValue<String> TOKEN_PARAM;
    public static final ForgeConfigSpec.IntValue MAX_TOKENS;
    public static final ForgeConfigSpec.IntValue TIMEOUT_SECONDS;
    public static final ForgeConfigSpec.IntValue BLOCKS_PER_TICK;
    public static final ForgeConfigSpec.IntValue MAX_BLOCKS;
    public static final ForgeConfigSpec.IntValue MAX_RADIUS;
    public static final ForgeConfigSpec.IntValue DISTANCE;
    public static final ForgeConfigSpec.IntValue PERMISSION_LEVEL;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Ket noi toi AI").push("ai");
        API_TYPE = b.comment("\"openai\" = moi API tuong thich OpenAI (OpenAI, OpenRouter, Groq, DeepSeek, Gemini-openai, Ollama...)",
                "\"anthropic\" = API Claude cua Anthropic")
                .defineInList("apiType", "openai", Arrays.asList("openai", "anthropic"));
        API_URL = b.comment("Dia chi endpoint day du.",
                "gemini: https://generativelanguage.googleapis.com/v1beta/openai/chat/completions (dung apiType openai)",
                "openai: https://api.openai.com/v1/chat/completions",
                "anthropic: https://api.anthropic.com/v1/messages")
                .define("apiUrl", "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions");
        API_KEY = b.comment("API key cua ban (KHONG chia se file nay).")
                .define("apiKey", "");
        MODEL = b.comment("Ten model, vi du gemini-2.5-pro (dep nhat), gemini-2.5-flash (nhanh, re), gpt-4o... Model cang manh thi cong trinh cang dep.")
                .define("model", "gemini-2.5-flash");
        TOKEN_PARAM = b.comment("Ten tham so gioi han token cho kieu openai. Mac dinh max_tokens; model moi cua OpenAI can max_completion_tokens.")
                .define("tokenParam", "max_tokens");
        MAX_TOKENS = b.comment("So token toi da AI duoc viet. Cong trinh lon can nhieu token; model Gemini con tinh ca token \"suy nghi\" nen de >= 32000.")
                .defineInRange("maxTokens", 32000, 1000, 128000);
        TIMEOUT_SECONDS = b.comment("Thoi gian cho AI tra loi (giay).")
                .defineInRange("timeoutSeconds", 300, 10, 1800);
        b.pop();

        b.comment("Cai dat xay dung").push("build");
        BLOCKS_PER_TICK = b.comment("So khoi dat moi tick (20 tick = 1 giay). Cao hon = xay nhanh hon nhung de lag.")
                .defineInRange("blocksPerTick", 400, 1, 20000);
        MAX_BLOCKS = b.comment("Gioi han so khoi cua 1 cong trinh.")
                .defineInRange("maxBlocks", 200000, 100, 2000000);
        MAX_RADIUS = b.comment("Cong trinh khong duoc vuot qua khoang cach nay so voi tam (theo x va z).")
                .defineInRange("maxRadius", 200, 8, 500);
        DISTANCE = b.comment("Cong trinh duoc dat cach nguoi choi bao nhieu khoi ve phia truoc mat.")
                .defineInRange("distance", 4, 0, 100);
        PERMISSION_LEVEL = b.comment("Cap quyen can de dung /aibuild. 0 = ai cung dung duoc, 2 = chi OP.",
                "Tren server cong cong nen dat 2 de tranh ton tien API.")
                .defineInRange("permissionLevel", 0, 0, 4);
        b.pop();

        SPEC = b.build();
    }

    private AIConfig() {
    }
}
