package com.otto.monstergirlcompanions.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.otto.monstergirlcompanions.MonsterGirlCompanions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Blockbench "Modded Entity" project read straight from the mod's resources
 * ({@code assets/monstergirlcompanions/bbmodel/<name>.bbmodel}), so a re-exported model needs no code changes.
 * Used on both sides: the server only needs the dimensions, the client builds the mesh and texture from it.
 * Coordinates stay in Blockbench space (y up from the feet, units of 1/16 block).
 */
public final class BbModel {
    public record Cube(String name, float[] from, float[] to, float[] origin, float[] rotation,
                       int u, int v, boolean mirror, float inflate) {
        public boolean rotated() {
            return rotation[0] != 0.0F || rotation[1] != 0.0F || rotation[2] != 0.0F;
        }
    }

    public record Group(String name, float[] origin, float[] rotation, List<Cube> cubes, List<Group> children) {}

    private final String name;
    private final Group root;
    private final int textureWidth;
    private final int textureHeight;
    private final byte[] texturePng;

    private BbModel(String name, Group root, int textureWidth, int textureHeight, byte[] texturePng) {
        this.name = name;
        this.root = root;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.texturePng = texturePng;
    }

    public static BbModel load(String name) {
        String path = "/assets/" + MonsterGirlCompanions.MOD_ID + "/bbmodel/" + name + ".bbmodel";
        try (InputStream in = BbModel.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing model " + path);
            }
            return parse(name, JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject());
        } catch (IOException e) {
            throw new IllegalStateException("Could not read model " + path, e);
        }
    }

    private static BbModel parse(String name, JsonObject json) {
        Map<String, JsonObject> elements = new HashMap<>();
        for (JsonElement e : json.getAsJsonArray("elements")) {
            elements.put(e.getAsJsonObject().get("uuid").getAsString(), e.getAsJsonObject());
        }
        // Format 5 keeps group data in "groups" and only uuids in the outliner; format 4 inlines it.
        Map<String, JsonObject> groups = new HashMap<>();
        if (json.has("groups")) {
            for (JsonElement g : json.getAsJsonArray("groups")) {
                groups.put(g.getAsJsonObject().get("uuid").getAsString(), g.getAsJsonObject());
            }
        }

        Group root = new Group("root", new float[]{0, 24, 0}, new float[3], new ArrayList<>(), new ArrayList<>());
        readChildren(json.getAsJsonArray("outliner"), root, elements, groups);

        JsonObject resolution = json.getAsJsonObject("resolution");
        byte[] png = null;
        JsonArray textures = json.getAsJsonArray("textures");
        if (textures != null && !textures.isEmpty()) {
            String source = textures.get(0).getAsJsonObject().get("source").getAsString();
            png = Base64.getDecoder().decode(source.substring(source.indexOf(',') + 1));
        }
        return new BbModel(name, root, resolution.get("width").getAsInt(), resolution.get("height").getAsInt(), png);
    }

    private static void readChildren(JsonArray children, Group parent, Map<String, JsonObject> elements, Map<String, JsonObject> groups) {
        for (JsonElement child : children) {
            if (child.isJsonPrimitive()) {
                JsonObject e = elements.get(child.getAsString());
                if (e != null && isShown(e)) {
                    parent.cubes().add(readCube(e));
                }
                continue;
            }
            JsonObject node = child.getAsJsonObject();
            JsonObject def = node.has("name") ? node : groups.get(node.get("uuid").getAsString());
            if (def == null || !isShown(def)) {
                continue;
            }
            Group group = new Group(def.get("name").getAsString(), floats(def, "origin"), floats(def, "rotation"),
                    new ArrayList<>(), new ArrayList<>());
            readChildren(node.getAsJsonArray("children"), group, elements, groups);
            parent.children().add(group);
        }
    }

    /** Hidden or non-exported parts in Blockbench stay out of the game. */
    private static boolean isShown(JsonObject o) {
        return (!o.has("visibility") || o.get("visibility").getAsBoolean())
                && (!o.has("export") || o.get("export").getAsBoolean());
    }

    private static Cube readCube(JsonObject e) {
        float[] uv = e.has("uv_offset") ? floats(e, "uv_offset") : new float[2];
        if (e.has("box_uv") && !e.get("box_uv").getAsBoolean()) {
            MonsterGirlCompanions.LOGGER.warn("Cube {} uses per-face UV; only box UV is supported, its UV may look wrong", e.get("name"));
        }
        return new Cube(e.get("name").getAsString(), floats(e, "from"), floats(e, "to"), floats(e, "origin"), floats(e, "rotation"),
                (int) uv[0], (int) uv[1], e.has("mirror_uv") && e.get("mirror_uv").getAsBoolean(),
                e.has("inflate") ? e.get("inflate").getAsFloat() : 0.0F);
    }

    private static float[] floats(JsonObject o, String key) {
        if (!o.has(key)) {
            return new float[3];
        }
        JsonArray a = o.getAsJsonArray(key);
        float[] out = new float[Math.max(3, a.size())];
        for (int i = 0; i < a.size(); i++) {
            out[i] = a.get(i).getAsFloat();
        }
        return out;
    }

    public String name() {
        return name;
    }

    public Group root() {
        return root;
    }

    public int textureWidth() {
        return textureWidth;
    }

    public int textureHeight() {
        return textureHeight;
    }

    /** PNG bytes of the first texture embedded in the project, or null. */
    public byte[] texturePng() {
        return texturePng;
    }

    /** Height of the model in blocks: the top of its highest cube (rotations ignored). */
    public float height() {
        return topOf(root) / 16.0F;
    }

    /** Eye height in blocks: the middle of the {@code head} group, or 85% of the height without one. */
    public float eyeHeight() {
        Group head = find(root, "head");
        if (head == null) {
            return height() * 0.85F;
        }
        return (bottomOf(head) + topOf(head)) / 2.0F / 16.0F;
    }

    public static Group find(Group group, String name) {
        if (group.name().equals(name)) {
            return group;
        }
        for (Group child : group.children()) {
            Group found = find(child, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static float topOf(Group group) {
        float top = Float.NEGATIVE_INFINITY;
        for (Cube c : group.cubes()) {
            top = Math.max(top, c.to()[1] + c.inflate());
        }
        for (Group child : group.children()) {
            top = Math.max(top, topOf(child));
        }
        return top;
    }

    private static float bottomOf(Group group) {
        float bottom = Float.POSITIVE_INFINITY;
        for (Cube c : group.cubes()) {
            bottom = Math.min(bottom, c.from()[1] - c.inflate());
        }
        for (Group child : group.children()) {
            bottom = Math.min(bottom, bottomOf(child));
        }
        return bottom;
    }
}
