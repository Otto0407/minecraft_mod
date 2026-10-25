package net.sistr.littlemaidmodelloader.maidmodel;
public class ModelPlate extends ModelBoxBase {
    public static final int planeXY = 0, planeZY = 1, planeXZ = 2, planeXYInv = 4, planeZYInv = 5, planeXZInv = 6;
    public static final int planeXYFront = 16, planeXYBack = 20, planeZYRight = 17, planeZYLeft = 21, planeXZTop = 18, planeXZBottom = 22;
    public ModelPlate(ModelRenderer r, Object... a) {
        super(r, a);
        kind = "plate"; w = (Integer) a[5]; h = (Integer) a[6]; plane = (Integer) a[7];
    }
}
