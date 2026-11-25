package com.aibuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.minecraftforge.fml.loading.FMLPaths;

/** Goi API AI (chay tren thread rieng, KHONG duoc dung tren server thread). */
public final class AIClient {

    private static final ExecutorService POOL = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "AIBuilder-HTTP");
            t.setDaemon(true);
            return t;
        }
    });

    private AIClient() {
    }

    public static void submit(Runnable r) {
        POOL.execute(r);
    }

    public static String loadResource(String name) throws IOException {
        InputStream in = AIClient.class.getResourceAsStream("/" + name);
        if (in == null) {
            throw new IOException("Thieu resource " + name);
        }
        try {
            return readAll(in);
        } finally {
            in.close();
        }
    }

    /** System prompt: uu tien file config/aibuilder/system_prompt.txt neu co. */
    public static String systemPrompt() throws IOException {
        File custom = FMLPaths.CONFIGDIR.get().resolve("aibuilder").resolve("system_prompt.txt").toFile();
        if (custom.isFile()) {
            return new String(Files.readAllBytes(custom.toPath()), StandardCharsets.UTF_8);
        }
        return loadResource("aibuilder_prompt.txt");
    }

    public static String ask(String userPrompt) throws IOException {
        String type = AIConfig.API_TYPE.get();
        String key = AIConfig.API_KEY.get().trim();
        String url = AIConfig.API_URL.get().trim();
        int timeoutMs = AIConfig.TIMEOUT_SECONDS.get() * 1000;
        boolean anthropic = "anthropic".equals(type);

        JsonObject body = new JsonObject();
        body.addProperty("model", AIConfig.MODEL.get());
        String sys = systemPrompt();
        JsonArray messages = new JsonArray();
        if (anthropic) {
            body.addProperty("max_tokens", AIConfig.MAX_TOKENS.get());
            body.addProperty("system", sys);
        } else {
            body.addProperty(AIConfig.TOKEN_PARAM.get(), AIConfig.MAX_TOKENS.get());
            JsonObject s = new JsonObject();
            s.addProperty("role", "system");
            s.addProperty("content", sys);
            messages.add(s);
        }
        JsonObject u = new JsonObject();
        u.addProperty("role", "user");
        u.addProperty("content", "Build request: " + userPrompt);
        messages.add(u);
        body.add("messages", messages);

        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        try {
            con.setRequestMethod("POST");
            con.setConnectTimeout(30000);
            con.setReadTimeout(timeoutMs);
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("Accept", "application/json");
            if (anthropic) {
                con.setRequestProperty("x-api-key", key);
                con.setRequestProperty("anthropic-version", "2023-06-01");
            } else if (!key.isEmpty()) {
                con.setRequestProperty("Authorization", "Bearer " + key);
            }
            byte[] out = body.toString().getBytes(StandardCharsets.UTF_8);
            con.setFixedLengthStreamingMode(out.length);
            con.getOutputStream().write(out);
            con.getOutputStream().close();

            int code = con.getResponseCode();
            InputStream in = code >= 400 ? con.getErrorStream() : con.getInputStream();
            String resp = in == null ? "" : readAll(in);
            if (code >= 400) {
                String snip = resp.length() > 500 ? resp.substring(0, 500) : resp;
                throw new IOException("API loi HTTP " + code + ": " + snip);
            }
            return extractText(resp, anthropic);
        } finally {
            con.disconnect();
        }
    }

    static String extractText(String resp, boolean anthropic) throws IOException {
        try {
            JsonObject root = new JsonParser().parse(resp).getAsJsonObject();
            if (anthropic) {
                String stop = root.has("stop_reason") && !root.get("stop_reason").isJsonNull()
                        ? root.get("stop_reason").getAsString() : "";
                if ("max_tokens".equals(stop)) {
                    throw new IOException("AI bi cat giua chung vi het token. Hay tang maxTokens trong config.");
                }
                StringBuilder sb = new StringBuilder();
                for (JsonElement el : root.getAsJsonArray("content")) {
                    JsonObject c = el.getAsJsonObject();
                    if (c.has("type") && "text".equals(c.get("type").getAsString()) && c.has("text")) {
                        sb.append(c.get("text").getAsString());
                    }
                }
                return sb.toString();
            }
            JsonObject choice = root.getAsJsonArray("choices").get(0).getAsJsonObject();
            String finish = choice.has("finish_reason") && !choice.get("finish_reason").isJsonNull()
                    ? choice.get("finish_reason").getAsString() : "";
            if ("length".equals(finish)) {
                throw new IOException("AI bi cat giua chung vi het token. Hay tang maxTokens trong config.");
            }
            return choice.getAsJsonObject("message").get("content").getAsString();
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            String snip = resp.length() > 300 ? resp.substring(0, 300) : resp;
            throw new IOException("Khong doc duoc phan hoi cua API: " + snip);
        }
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) {
            bos.write(buf, 0, n);
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }
}
