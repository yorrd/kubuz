import com.sun.istack.internal.NotNull;

interface Renderable {
	

    void moveZ(float offset_Z);
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
