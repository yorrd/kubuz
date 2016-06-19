public class Kubus implements Renderable {

    private int renderDepth = 8;
    private int diameter = 4;

    private float[] vertexArray;
    private float[] textureArray;
    private int[] indexArray;

    @Override
    public void render() {
        vertexArray = new float[3 * renderDepth * diameter * 6];
        for(int row = 0; row < renderDepth; row++) {
            // for each row: 4 sides, 6 verts per quad * diameter per side * 3 per vertex
            int offset = row * 4 * 6 * diameter * 3;

            // top
            for(int sideIndex = 0; sideIndex < diameter; sideIndex++) {
            }
            // right
            for(int sideIndex = 0; sideIndex < diameter; sideIndex++) {
            }
            // bottom
            for(int sideIndex = 0; sideIndex < diameter; sideIndex++) {
            }
            // left
            for(int sideIndex = 0; sideIndex < diameter; sideIndex++) {
            }
        }
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
