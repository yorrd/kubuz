import com.sun.istack.internal.NotNull;

interface Renderable {
	

    void render();
    @NotNull
    float[] getVertexArray();
    @NotNull
    float[] getTextureArray();
    @NotNull
    int[] getIndexArray();
    @NotNull
    String getTextureFilename();
}
