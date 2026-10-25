package net.sistr.littlemaidmodelloader.maidmodel;
/** Recording replacement: keeps the box parameters instead of building quads. */
public abstract class ModelBoxBase {
    public float posX1, posY1, posZ1, posX2, posY2, posZ2;
    public String boxName;
    public String kind;
    public int texX, texY, w, h, d, plane;
    public float x, y, z, inflate;
    public boolean mirror;
    public ModelBoxBase(ModelRenderer r, Object... a) {
        texX = (Integer) a[0]; texY = (Integer) a[1];
        x = (Float) a[2]; y = (Float) a[3]; z = (Float) a[4];
        inflate = a.length < 9 ? 0f : (Float) a[8];
        mirror = r.mirror;
    }
    public ModelBoxBase setBoxName(String n) { boxName = n; return this; }
}
