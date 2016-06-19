public class DummyObject implements Renderable {

    private float[] vertexArray;
    private float[] textureArray;
    private int[] indexArray;

    @Override
    public void render() {
        vertexArray = new float[]{
                -0.5f, -0.5f, 0f,	// left bottom		ID: 0
                0.0f, -0.5f, 0f,	// center bottom	ID: 1
                0.5f, -0.5f, 0f,	// right bottom		ID: 2
                -0.5f,  0.5f, 0f,	// left top			ID: 3
                0.0f,  0.5f, 0f,	// center top		ID: 4
                0.7f,  0.7f, 0f	// right top		ID: 5
        };
        textureArray = new float[]{
                0.0f, 1.0f,
                0.5f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                0.5f, 0.0f,
                1.0f, 0.0f,
        };
        indexArray = new int[]{2, 5, 1, 4, 0, 3};
    }

    @Override
    public float[] getVertexArray() {
        return vertexArray;
    }

    @Override
    public float[] getTextureArray() {
        return textureArray;
    }

    @Override
    public int[] getIndexArray() {
        return indexArray;
    }

    @Override
    public String getTextureFilename() {
        return "./assets/gdv.png";
    }
}
