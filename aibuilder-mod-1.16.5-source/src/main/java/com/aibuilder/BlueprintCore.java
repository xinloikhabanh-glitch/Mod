package com.aibuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Chuyen blueprint JSON (do AI sinh ra) thanh danh sach khoi (x,y,z,blockId).
 * Lop nay KHONG phu thuoc vao Minecraft nen co the test rieng.
 */
public final class BlueprintCore {

    public static final class BlueprintException extends Exception {
        private static final long serialVersionUID = 1L;

        public BlueprintException(String msg) {
            super(msg);
        }
    }

    public static final class Entry {
        public final int x, y, z;
        public final String block;

        Entry(int x, int y, int z, String block) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.block = block;
        }
    }

    public static final class Result {
        public String name = "building";
        public final List<Entry> entries = new ArrayList<Entry>();
        public boolean clamped = false;
        public int opCount = 0;
    }

    private static final int Y_MIN = -16;
    private static final int Y_MAX = 250;
    private static final int MAX_SHAPE_RADIUS = 128;

    private final Map<Long, String> map = new HashMap<Long, String>();
    private final Map<String, String> palette = new HashMap<String, String>();
    private final int maxBlocks;
    private final int maxRadius;
    private boolean clamped = false;

    private BlueprintCore(int maxBlocks, int maxRadius) {
        this.maxBlocks = maxBlocks;
        this.maxRadius = maxRadius;
    }

    /** Lay phan JSON ra khoi van ban AI tra ve (bo ``` va chu thua). */
    public static String extractJson(String text) throws BlueprintException {
        if (text == null) {
            throw new BlueprintException("AI khong tra ve noi dung.");
        }
        int a = text.indexOf('{');
        int b = text.lastIndexOf('}');
        if (a < 0 || b <= a) {
            throw new BlueprintException("AI khong tra ve JSON hop le.");
        }
        return text.substring(a, b + 1);
    }

    public static Result parse(String text, int maxBlocks, int maxRadius) throws BlueprintException {
        String json = extractJson(text);
        JsonObject root;
        try {
            JsonElement el = new JsonParser().parse(json);
            if (!el.isJsonObject()) {
                throw new BlueprintException("Blueprint phai la mot JSON object.");
            }
            root = el.getAsJsonObject();
        } catch (BlueprintException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new BlueprintException("JSON loi cu phap (co the AI bi cat giua chung, hay tang maxTokens): " + e.getMessage());
        }

        BlueprintCore core = new BlueprintCore(maxBlocks, maxRadius);
        try {
            return core.run(root);
        } catch (BlueprintException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new BlueprintException("Blueprint sai cau truc: " + e);
        }
    }

    private Result run(JsonObject root) throws BlueprintException {
        Result res = new Result();
        if (root.has("name") && root.get("name").isJsonPrimitive()) {
            res.name = root.get("name").getAsString();
        }
        if (root.has("palette") && root.get("palette").isJsonObject()) {
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("palette").entrySet()) {
                palette.put(e.getKey(), e.getValue().getAsString().trim());
            }
        }
        if (!root.has("ops") || !root.get("ops").isJsonArray()) {
            throw new BlueprintException("Blueprint thieu mang \"ops\".");
        }
        JsonArray ops = root.getAsJsonArray("ops");
        for (JsonElement el : ops) {
            if (!el.isJsonObject()) {
                continue;
            }
            applyOp(el.getAsJsonObject());
            res.opCount++;
        }
        if (map.isEmpty()) {
            throw new BlueprintException("Blueprint khong tao ra khoi nao.");
        }
        for (Map.Entry<Long, String> e : map.entrySet()) {
            long k = e.getKey();
            int y = (int) (k & 1023L) - 64;
            int z = (int) ((k >> 10) & 2047L) - 1024;
            int x = (int) (k >> 21) - 1024;
            res.entries.add(new Entry(x, y, z, e.getValue()));
        }
        // Dat tu duoi len, sau do theo z, x de cong trinh moc dan len tu mat dat.
        Collections.sort(res.entries, new Comparator<Entry>() {
            @Override
            public int compare(Entry a, Entry b) {
                if (a.y != b.y) {
                    return a.y < b.y ? -1 : 1;
                }
                if (a.z != b.z) {
                    return a.z < b.z ? -1 : 1;
                }
                return a.x < b.x ? -1 : (a.x == b.x ? 0 : 1);
            }
        });
        res.clamped = clamped;
        return res;
    }

    // ------------------------------------------------------------------ helpers

    private static int[] vec(JsonObject o, String key) throws BlueprintException {
        if (!o.has(key) || !o.get(key).isJsonArray()) {
            throw new BlueprintException("Op thieu truong \"" + key + "\" dang [x,y,z].");
        }
        JsonArray a = o.getAsJsonArray(key);
        if (a.size() < 3) {
            throw new BlueprintException("Truong \"" + key + "\" phai co 3 so.");
        }
        return new int[] {
            (int) Math.round(a.get(0).getAsDouble()),
            (int) Math.round(a.get(1).getAsDouble()),
            (int) Math.round(a.get(2).getAsDouble())
        };
    }

    private static int num(JsonObject o, String key, int def) {
        if (o.has(key) && o.get(key).isJsonPrimitive()) {
            return (int) Math.round(o.get(key).getAsDouble());
        }
        return def;
    }

    private static boolean bool(JsonObject o, String key) {
        if (o.has(key) && o.get(key).isJsonPrimitive()) {
            JsonElement p = o.get(key);
            if (p.getAsJsonPrimitive().isBoolean()) {
                return p.getAsBoolean();
            }
            return "true".equalsIgnoreCase(p.getAsString());
        }
        return false;
    }

    private String block(JsonObject o) throws BlueprintException {
        if (!o.has("block") || !o.get("block").isJsonPrimitive()) {
            throw new BlueprintException("Op thieu truong \"block\".");
        }
        String s = o.get("block").getAsString().trim();
        String p = palette.get(s);
        if (p != null) {
            s = p;
        }
        return s.toLowerCase(Locale.ROOT);
    }

    private void put(int x, int y, int z, String block) throws BlueprintException {
        if (x < -maxRadius || x > maxRadius || z < -maxRadius || z > maxRadius || y < Y_MIN || y > Y_MAX) {
            clamped = true;
            return;
        }
        long key = ((long) (x + 1024) << 21) | ((long) (z + 1024) << 10) | (long) (y + 64);
        map.put(key, block);
        if (map.size() > maxBlocks) {
            throw new BlueprintException("Cong trinh vuot gioi han " + maxBlocks + " khoi (maxBlocks trong config).");
        }
    }

    private int clampX(int v) {
        return Math.max(-maxRadius, Math.min(maxRadius, v));
    }

    private static int clampY(int v) {
        return Math.max(Y_MIN, Math.min(Y_MAX, v));
    }

    // --------------------------------------------------------------------- ops

    private void applyOp(JsonObject o) throws BlueprintException {
        if (!o.has("op") || !o.get("op").isJsonPrimitive()) {
            return;
        }
        String op = o.get("op").getAsString().toLowerCase(Locale.ROOT);
        String b = block(o);
        switch (op) {
            case "set": {
                int[] p = vec(o, "pos");
                put(p[0], p[1], p[2], b);
                break;
            }
            case "fill":
            case "hollow":
            case "walls": {
                int[] f = vec(o, "from");
                int[] t = vec(o, "to");
                int x0 = Math.min(f[0], t[0]), x1 = Math.max(f[0], t[0]);
                int y0 = Math.min(f[1], t[1]), y1 = Math.max(f[1], t[1]);
                int z0 = Math.min(f[2], t[2]), z1 = Math.max(f[2], t[2]);
                if (x1 < -maxRadius || x0 > maxRadius || z1 < -maxRadius || z0 > maxRadius || y1 < Y_MIN || y0 > Y_MAX) {
                    clamped = true;
                    break;
                }
                int cx0 = clampX(x0), cx1 = clampX(x1), cz0 = clampX(z0), cz1 = clampX(z1);
                int cy0 = clampY(y0), cy1 = clampY(y1);
                if (cx0 != x0 || cx1 != x1 || cz0 != z0 || cz1 != z1 || cy0 != y0 || cy1 != y1) {
                    clamped = true;
                }
                boolean isFill = op.equals("fill");
                boolean isHollow = op.equals("hollow");
                for (int y = cy0; y <= cy1; y++) {
                    for (int z = cz0; z <= cz1; z++) {
                        for (int x = cx0; x <= cx1; x++) {
                            if (!isFill) {
                                boolean edgeXZ = x == x0 || x == x1 || z == z0 || z == z1;
                                boolean edgeY = y == y0 || y == y1;
                                boolean keep = isHollow ? (edgeXZ || edgeY) : edgeXZ;
                                if (!keep) {
                                    continue;
                                }
                            }
                            put(x, y, z, b);
                        }
                    }
                }
                break;
            }
            case "line": {
                int[] f = vec(o, "from");
                int[] t = vec(o, "to");
                int dx = t[0] - f[0], dy = t[1] - f[1], dz = t[2] - f[2];
                int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
                if (steps > 1000) {
                    throw new BlueprintException("Duong thang qua dai (>1000).");
                }
                if (steps == 0) {
                    put(f[0], f[1], f[2], b);
                    break;
                }
                for (int i = 0; i <= steps; i++) {
                    int x = f[0] + (int) Math.round((double) dx * i / steps);
                    int y = f[1] + (int) Math.round((double) dy * i / steps);
                    int z = f[2] + (int) Math.round((double) dz * i / steps);
                    put(x, y, z, b);
                }
                break;
            }
            case "sphere": {
                int[] c = vec(o, "center");
                int r = checkRadius(num(o, "r", 1));
                boolean hollow = bool(o, "hollow");
                boolean upper = bool(o, "upper");
                double outer = (r + 0.5) * (r + 0.5);
                double inner = (r - 0.5) * (r - 0.5);
                for (int dy = upper ? 0 : -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        for (int dx = -r; dx <= r; dx++) {
                            double d2 = dx * dx + dy * dy + dz * dz;
                            if (d2 <= outer && (!hollow || d2 > inner)) {
                                put(c[0] + dx, c[1] + dy, c[2] + dz, b);
                            }
                        }
                    }
                }
                break;
            }
            case "cylinder": {
                int[] c = vec(o, "center");
                int r = checkRadius(num(o, "r", 1));
                int h = Math.max(1, Math.min(num(o, "h", 1), 250));
                boolean hollow = bool(o, "hollow");
                double outer = (r + 0.5) * (r + 0.5);
                double inner = (r - 0.5) * (r - 0.5);
                for (int dy = 0; dy < h; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        for (int dx = -r; dx <= r; dx++) {
                            double d2 = dx * dx + dz * dz;
                            if (d2 <= outer && (!hollow || d2 > inner)) {
                                put(c[0] + dx, c[1] + dy, c[2] + dz, b);
                            }
                        }
                    }
                }
                break;
            }
            case "pyramid": {
                int[] c = vec(o, "center");
                int r = checkRadius(num(o, "r", 1));
                boolean hollow = bool(o, "hollow");
                for (int i = 0; i <= r; i++) {
                    int half = r - i;
                    for (int dz = -half; dz <= half; dz++) {
                        for (int dx = -half; dx <= half; dx++) {
                            if (hollow && Math.abs(dx) != half && Math.abs(dz) != half) {
                                continue;
                            }
                            put(c[0] + dx, c[1] + i, c[2] + dz, b);
                        }
                    }
                }
                break;
            }
            case "cone": {
                int[] c = vec(o, "center");
                int r = checkRadius(num(o, "r", 1));
                int h = Math.max(1, Math.min(num(o, "h", 1), 250));
                boolean hollow = bool(o, "hollow");
                double thick = Math.max(1.0, (double) r / h);
                for (int i = 0; i < h; i++) {
                    double rr = (double) r * (h - i) / h;
                    double outer = (rr + 0.5) * (rr + 0.5);
                    double innerR = rr + 0.5 - thick;
                    double inner = innerR > 0 ? innerR * innerR : -1;
                    int ir = (int) Math.ceil(rr + 0.5);
                    for (int dz = -ir; dz <= ir; dz++) {
                        for (int dx = -ir; dx <= ir; dx++) {
                            double d2 = dx * dx + dz * dz;
                            if (d2 <= outer && (!hollow || d2 > inner)) {
                                put(c[0] + dx, c[1] + i, c[2] + dz, b);
                            }
                        }
                    }
                }
                break;
            }
            default:
                // Op la: bo qua.
                break;
        }
    }

    private static int checkRadius(int r) throws BlueprintException {
        if (r < 1) {
            return 1;
        }
        if (r > MAX_SHAPE_RADIUS) {
            throw new BlueprintException("Ban kinh hinh khoi qua lon (>" + MAX_SHAPE_RADIUS + ").");
        }
        return r;
    }
}
