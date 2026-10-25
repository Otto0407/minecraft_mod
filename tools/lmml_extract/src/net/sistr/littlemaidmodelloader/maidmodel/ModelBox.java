package net.sistr.littlemaidmodelloader.maidmodel;
public class ModelBox extends ModelBoxBase {
    public ModelBox(ModelRenderer r, Object... a) {
        super(r, a);
        kind = "box"; w = (Integer) a[5]; h = (Integer) a[6]; d = (Integer) a[7];
    }
}
