package net.sistr.littlemaidmodelloader.maidmodel;
import java.util.ArrayList;
import java.util.List;
public class ModelRenderer {
    public float scale = 0.0625f;
    public float textureWidth = 64.0f, textureHeight = 32.0f;
    private int textureOffsetX, textureOffsetY;
    public float rotationPointX, rotationPointY, rotationPointZ;
    public float rotateAngleX, rotateAngleY, rotateAngleZ;
    protected boolean compiled = false;
    protected int displayList = 0;
    public boolean mirror = false, showModel = true, isHidden = false, isRendering = true;
    public List<ModelBoxBase> cubeList = new ArrayList<>();
    public List<ModelRenderer> childModels;
    public final String boxName;
    protected ModelBase baseModel;
    public ModelRenderer pearent;
    public float offsetX, offsetY, offsetZ;
    public float scaleX = 1f, scaleY = 1f, scaleZ = 1f;
    public static final float radFactor = 57.295776f;
    public static final float degFactor = (float) Math.PI / 180;
    public int rotatePriority;
    public static final int RotXYZ = 0, RotXZY = 1, RotYXZ = 2, RotYZX = 3, RotZXY = 4, RotZYX = 5;
    public boolean adjust = true;
    public boolean isInvertX = false;

    public ModelRenderer(ModelBase m, String name) {
        baseModel = m; m.boxList.add(this); boxName = name;
        setTextureSize(m.textureWidth, m.textureHeight);
    }
    public ModelRenderer(ModelBase m, int px, int py) { this(m, (String) null); setTextureOffset(px, py); }
    public ModelRenderer(ModelBase m) { this(m, (String) null); }
    public ModelRenderer(ModelBase m, int px, int py, float sx, float sy, float sz) { this(m, px, py); scaleX = sx; scaleY = sy; scaleZ = sz; }
    public ModelRenderer(ModelBase m, float sx, float sy, float sz) { this(m); scaleX = sx; scaleY = sy; scaleZ = sz; }

    public void addChild(ModelRenderer c) { if (childModels == null) childModels = new ArrayList<>(); childModels.add(c); c.pearent = this; }
    public ModelRenderer setTextureOffset(int x, int y) { textureOffsetX = x; textureOffsetY = y; return this; }
    public ModelRenderer addBox(String n, float x, float y, float z, int w, int h, int d) { return addParts(ModelBox.class, n, x, y, z, w, h, d, 0.0f); }
    public ModelRenderer addBox(float x, float y, float z, int w, int h, int d) { return addParts(ModelBox.class, x, y, z, w, h, d, 0.0f); }
    public ModelRenderer addBox(float x, float y, float z, int w, int h, int d, float s) { return addParts(ModelBox.class, x, y, z, w, h, d, s); }
    public ModelRenderer setRotationPoint(float x, float y, float z) { rotationPointX = x; rotationPointY = y; rotationPointZ = z; return this; }
    public void render(float f, boolean b) {}
    public void render(float f) {}
    public void renderWithRotation(float f) {}
    public void postRender(float f) {}
    public ModelRenderer setTextureSize(int w, int h) { textureWidth = w; textureHeight = h; return this; }
    public ModelRenderer addCubeList(ModelBoxBase b) { cubeList.add(b); return this; }
    protected ModelBoxBase getModelBoxBase(Class<? extends ModelBoxBase> c, Object... a) {
        try { return c.getConstructor(ModelRenderer.class, Object[].class).newInstance(this, a); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
    protected Object[] getArg(Object... a) {
        Object[] o = new Object[a.length + 2]; o[0] = textureOffsetX; o[1] = textureOffsetY;
        System.arraycopy(a, 0, o, 2, a.length); return o;
    }
    public ModelRenderer addParts(Class<? extends ModelBoxBase> c, String n, Object... a) { addCubeList(getModelBoxBase(c, getArg(a)).setBoxName(boxName + "." + n)); return this; }
    public ModelRenderer addParts(Class<? extends ModelBoxBase> c, Object... a) { addCubeList(getModelBoxBase(c, getArg(a))); return this; }
    public ModelRenderer addPartsTexture(Class<? extends ModelBoxBase> c, String n, Object... a) { addCubeList(getModelBoxBase(c, a).setBoxName(boxName + "." + n)); return this; }
    public ModelRenderer addPartsTexture(Class<? extends ModelBoxBase> c, Object... a) { addCubeList(getModelBoxBase(c, a)); return this; }
    public ModelRenderer addPlate(float x, float y, float z, int w, int h, int p) { return addParts(ModelPlate.class, x, y, z, w, h, p, 0.0f); }
    public ModelRenderer addPlate(float x, float y, float z, int w, int h, int p, float s) { return addParts(ModelPlate.class, x, y, z, w, h, p, s); }
    public ModelRenderer addPlate(String n, float x, float y, float z, int w, int h, int p) { return addParts(ModelPlate.class, n, x, y, z, w, h, p, 0.0f); }
    public void clearCubeList() { cubeList.clear(); }
    public void setRotatePriority(int v) { rotatePriority = v; }
    public ModelRenderer loadMatrix() { return this; }
    public boolean getMirror() { return mirror; }
    public ModelRenderer setMirror(boolean f) { mirror = f; return this; }
    public boolean getVisible() { return showModel; }
    public void setVisible(boolean f) { showModel = f; }
    public float getRotateAngleX() { return rotateAngleX; }
    public float getRotateAngleDegX() { return rotateAngleX * radFactor; }
    public float setRotateAngleX(float v) { return rotateAngleX = v; }
    public float setRotateAngleDegX(float v) { return rotateAngleX = v * degFactor; }
    public float addRotateAngleX(float v) { return rotateAngleX += v; }
    public float addRotateAngleDegX(float v) { return rotateAngleX += v * degFactor; }
    public float getRotateAngleY() { return rotateAngleY; }
    public float getRotateAngleDegY() { return rotateAngleY * radFactor; }
    public float setRotateAngleY(float v) { return rotateAngleY = v; }
    public float setRotateAngleDegY(float v) { return rotateAngleY = v * degFactor; }
    public float addRotateAngleY(float v) { return rotateAngleY += v; }
    public float addRotateAngleDegY(float v) { return rotateAngleY += v * degFactor; }
    public float getRotateAngleZ() { return rotateAngleZ; }
    public float getRotateAngleDegZ() { return rotateAngleZ * radFactor; }
    public float setRotateAngleZ(float v) { return rotateAngleZ = v; }
    public float setRotateAngleDegZ(float v) { return rotateAngleZ = v * degFactor; }
    public float addRotateAngleZ(float v) { return rotateAngleZ += v; }
    public float addRotateAngleDegZ(float v) { return rotateAngleZ += v * degFactor; }
    public ModelRenderer setRotateAngle(float x, float y, float z) { rotateAngleX = x; rotateAngleY = y; rotateAngleZ = z; return this; }
    public ModelRenderer setRotateAngleDeg(float x, float y, float z) { return setRotateAngle(x * degFactor, y * degFactor, z * degFactor); }
    public float getRotationPointX() { return rotationPointX; }
    public float setRotationPointX(float v) { return rotationPointX = v; }
    public float addRotationPointX(float v) { return rotationPointX += v; }
    public float getRotationPointY() { return rotationPointY; }
    public float setRotationPointY(float v) { return rotationPointY = v; }
    public float addRotationPointY(float v) { return rotationPointY += v; }
    public float getRotationPointZ() { return rotationPointZ; }
    public float setRotationPointZ(float v) { return rotationPointZ = v; }
    public float addRotationPointZ(float v) { return rotationPointZ += v; }
    public ModelRenderer setScale(float x, float y, float z) { scaleX = x; scaleY = y; scaleZ = z; return this; }
    public float setScaleX(float v) { return scaleX = v; }
    public float setScaleY(float v) { return scaleY = v; }
    public float setScaleZ(float v) { return scaleZ = v; }
}
