package com.otto.monstergirlcompanions.client.model;

import com.otto.monstergirlcompanions.model.BbModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds a HumanoidModel-compatible mesh from a {@link BbModel}.
 *
 * <p>Groups named like HumanoidModel's parts become root-level parts wherever they sit in the outliner;
 * other groups keep their nesting, and anything outside those parts hangs off {@code body} so it is still
 * drawn. Cubes rotated in Blockbench become small child parts, since Minecraft can only rotate parts.
 * Groups named {@code right_item} / {@code left_item} mark where held items go.
 */
public final class BbModelLayer {
    public static final List<String> HUMANOID_PARTS = List.of("head", "hat", "body", "right_arm", "left_arm", "right_leg", "left_leg");
    public static final String RIGHT_ITEM = "right_item";
    public static final String LEFT_ITEM = "left_item";

    /** The mesh plus the part path (from the root) of each item mount point that exists. */
    public record Built(LayerDefinition layer, Map<String, List<String>> mounts) {}

    private final BbModel model;
    private final Map<String, List<String>> mounts = new HashMap<>();
    private final Map<PartDefinition, Set<String>> usedNames = new HashMap<>();
    private final Map<String, PartDefinition> humanoidParts = new HashMap<>();
    private final Map<String, BbModel.Group> humanoidGroups = new HashMap<>();

    private BbModelLayer(BbModel model) {
        this.model = model;
    }

    public static Built build(BbModel model) {
        return new BbModelLayer(model).build();
    }

    private Built build() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        BbModel.Group rootGroup = model.root();
        float[] rootPivot = rootGroup.origin();

        for (String name : HUMANOID_PARTS) {
            BbModel.Group group = BbModel.find(rootGroup, name);
            if (group == null) {
                group = new BbModel.Group(name, rootPivot, new float[3], new ArrayList<>(), new ArrayList<>());
            }
            List<BbModel.Cube> cubes = new ArrayList<>(group.cubes());
            if (name.equals("body")) {
                cubes.addAll(rootGroup.cubes()); // loose cubes would otherwise never render
            }
            humanoidGroups.put(name, group);
            humanoidParts.put(name, addPart(root, rootPivot, name, group.origin(), group.rotation(), cubes, List.of(name)));
        }

        for (BbModel.Group child : rootGroup.children()) {
            addGroup(child, root, rootPivot, List.of());
        }
        // Humanoid groups may sit inside organising groups; their subtrees are added from here.
        for (String name : HUMANOID_PARTS) {
            BbModel.Group group = humanoidGroups.get(name);
            for (BbModel.Group child : group.children()) {
                addGroup(child, humanoidParts.get(name), group.origin(), List.of(name));
            }
        }
        return new Built(LayerDefinition.create(mesh, model.textureWidth(), model.textureHeight()), mounts);
    }

    /** Adds a non-humanoid group (and its subtree) under {@code parent}; humanoid groups are skipped here. */
    private void addGroup(BbModel.Group group, PartDefinition parent, float[] parentPivot, List<String> parentPath) {
        if (HUMANOID_PARTS.contains(group.name())) {
            return; // added at the root, subtree included
        }
        if (parentPath.isEmpty()) {
            // Directly under the root nothing is drawn by HumanoidModel, so attach to the body.
            parent = humanoidParts.get("body");
            parentPivot = humanoidGroups.get("body").origin();
            parentPath = List.of("body");
        }
        String name = uniqueName(parent, group.name());
        List<String> path = append(parentPath, name);
        PartDefinition part = addPart(parent, parentPivot, name, group.origin(), group.rotation(), group.cubes(), path);
        if (group.name().equals(RIGHT_ITEM) || group.name().equals(LEFT_ITEM)) {
            mounts.put(group.name(), path);
        }
        for (BbModel.Group child : group.children()) {
            addGroup(child, part, group.origin(), path);
        }
    }

    private PartDefinition addPart(PartDefinition parent, float[] parentPivot, String name, float[] pivot, float[] rotation,
                                   List<BbModel.Cube> cubes, List<String> path) {
        CubeListBuilder builder = CubeListBuilder.create();
        List<BbModel.Cube> rotated = new ArrayList<>();
        for (BbModel.Cube cube : cubes) {
            if (cube.rotated()) {
                rotated.add(cube);
            } else {
                addCube(builder, cube, pivot);
            }
        }
        PartDefinition part = parent.addOrReplaceChild(name, builder, pose(parentPivot, pivot, rotation));
        usedNames.computeIfAbsent(parent, p -> new HashSet<>()).add(name);
        for (BbModel.Cube cube : rotated) {
            CubeListBuilder single = CubeListBuilder.create();
            addCube(single, cube, cube.origin());
            String childName = uniqueName(part, cube.name());
            part.addOrReplaceChild(childName, single, pose(pivot, cube.origin(), cube.rotation()));
            usedNames.computeIfAbsent(part, p -> new HashSet<>()).add(childName);
        }
        return part;
    }

    private static void addCube(CubeListBuilder builder, BbModel.Cube cube, float[] pivot) {
        float[] min = toJava(cube.to()[0], cube.to()[1], cube.from()[2]);
        float[] p = toJava(pivot[0], pivot[1], pivot[2]);
        builder.mirror(cube.mirror()).texOffs(cube.u(), cube.v()).addBox(
                min[0] - p[0], min[1] - p[1], min[2] - p[2],
                cube.to()[0] - cube.from()[0], cube.to()[1] - cube.from()[1], cube.to()[2] - cube.from()[2],
                new CubeDeformation(cube.inflate()));
    }

    /** Blockbench (y up from the feet, x mirrored) to Minecraft model space (y down, feet at y = 24). */
    private static float[] toJava(float x, float y, float z) {
        return new float[]{-x, 24.0F - y, z};
    }

    private static PartPose pose(float[] parentPivot, float[] pivot, float[] rotation) {
        float[] a = toJava(pivot[0], pivot[1], pivot[2]);
        float[] b = toJava(parentPivot[0], parentPivot[1], parentPivot[2]);
        return PartPose.offsetAndRotation(a[0] - b[0], a[1] - b[1], a[2] - b[2],
                -rotation[0] * Mth.DEG_TO_RAD, -rotation[1] * Mth.DEG_TO_RAD, rotation[2] * Mth.DEG_TO_RAD);
    }

    private String uniqueName(PartDefinition parent, String name) {
        Set<String> used = usedNames.getOrDefault(parent, Set.of());
        String candidate = name;
        for (int i = 2; used.contains(candidate); i++) {
            candidate = name + "_" + i;
        }
        return candidate;
    }

    private static List<String> append(List<String> path, String name) {
        List<String> out = new ArrayList<>(path);
        out.add(name);
        return List.copyOf(out);
    }
}
