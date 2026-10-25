import java.lang.reflect.*;
import java.util.*;
import net.sistr.littlemaidmodelloader.maidmodel.*;

/** Instantiates each LittleMaid model and dumps its part tree (rest pose) as JSON. */
public class Dump {
    static Map<ModelRenderer, String> names;

    public static void main(String[] args) throws Exception {
        for (String cls : args) {
            Class<?> c = Class.forName("net.sistr.littlemaidmodelloader.maidmodel." + cls);
            Object m;
            try { m = c.getConstructor(float.class).newInstance(0.0f); }
            catch (NoSuchMethodException e) { m = c.getConstructor().newInstance(); }
            String note = "init";
            String living = "";
            try {
                c.getMethod("setLivingAnimations", IModelCaps.class, float.class, float.class, float.class).invoke(m, null, 0f, 0f, 0f);
            } catch (InvocationTargetException e) { living = " (setLivingAnimations failed: " + e.getCause() + ")"; }
            try {
                c.getMethod("setRotationAngles", float.class, float.class, float.class, float.class, float.class, float.class, IModelCaps.class)
                        .invoke(m, 0f, 0f, 0f, 0f, 0f, 0.0625f, null);
                note = "setRotationAngles";
            } catch (InvocationTargetException e) {
                note = "init (setRotationAngles failed: " + e.getCause() + ")";
                try {
                    c.getMethod("setDefaultPause", float.class, float.class, float.class, float.class, float.class, float.class, IModelCaps.class)
                            .invoke(m, 0f, 0f, 0f, 0f, 0f, 0f, null);
                    note = "setDefaultPause";
                } catch (InvocationTargetException e2) { /* keep the initModel pose */ }
            }
            note += living;
            if (((ModelMultiBase) m).mainFrame == null) { System.out.println("{\"model\":\"" + cls + "\",\"empty\":true}"); continue; }
            names = new IdentityHashMap<>();
            for (Class<?> k = c; k != null; k = k.getSuperclass())
                for (Field f : k.getDeclaredFields()) {
                    f.setAccessible(true);
                    if (f.getType() == ModelRenderer.class) { Object v = f.get(m); if (v != null) names.putIfAbsent((ModelRenderer) v, f.getName()); }
                    else if (f.getType() == ModelRenderer[].class) {
                        ModelRenderer[] arr = (ModelRenderer[]) f.get(m);
                        if (arr != null) for (int i = 0; i < arr.length; i++) if (arr[i] != null) names.putIfAbsent(arr[i], f.getName() + i);
                    }
                }
            ModelBase mb = (ModelBase) m;
            ModelRenderer root = ((ModelMultiBase) m).mainFrame;
            StringBuilder sb = new StringBuilder();
            sb.append("{\"model\":\"").append(cls).append("\",\"pose\":\"").append(note.replace("\"", "'"))
              .append("\",\"texW\":").append(mb.textureWidth).append(",\"texH\":").append(mb.textureHeight)
              .append(",\"height\":").append(((ModelMultiBase) m).getHeight()).append(",\"width\":").append(((ModelMultiBase) m).getWidth())
              .append(",\"root\":");
            part(sb, root, new HashSet<>());
            sb.append("}");
            System.out.println(sb);
        }
    }

    static void part(StringBuilder sb, ModelRenderer r, Set<ModelRenderer> seen) {
        seen.add(r);
        String n = names.getOrDefault(r, r.boxName != null ? r.boxName : "part");
        sb.append("{\"name\":\"").append(n).append("\",\"pivot\":[").append(r.rotationPointX).append(',').append(r.rotationPointY).append(',').append(r.rotationPointZ)
          .append("],\"offset\":[").append(r.offsetX).append(',').append(r.offsetY).append(',').append(r.offsetZ)
          .append("],\"rot\":[").append(r.rotateAngleX).append(',').append(r.rotateAngleY).append(',').append(r.rotateAngleZ)
          .append("],\"scale\":[").append(r.scaleX).append(',').append(r.scaleY).append(',').append(r.scaleZ)
          .append("],\"priority\":").append(r.rotatePriority).append(",\"visible\":").append(r.showModel && !r.isHidden)
          .append(",\"invertX\":").append(r.isInvertX).append(",\"texW\":").append(r.textureWidth).append(",\"texH\":").append(r.textureHeight)
          .append(",\"cubes\":[");
        for (int i = 0; i < r.cubeList.size(); i++) {
            ModelBoxBase b = r.cubeList.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"kind\":\"").append(b.kind).append("\",\"uv\":[").append(b.texX).append(',').append(b.texY)
              .append("],\"from\":[").append(b.x).append(',').append(b.y).append(',').append(b.z)
              .append("],\"size\":[").append(b.w).append(',').append(b.h).append(',').append(b.d)
              .append("],\"plane\":").append(b.plane).append(",\"inflate\":").append(b.inflate).append(",\"mirror\":").append(b.mirror).append('}');
        }
        sb.append("],\"children\":[");
        boolean first = true;
        if (r.childModels != null) for (ModelRenderer c : r.childModels) {
            if (seen.contains(c)) continue;
            if (!first) sb.append(',');
            first = false;
            part(sb, c, seen);
        }
        sb.append("]}");
    }
}
