
/*
* Super simple class just for displaying the stars backdrop image. This extends Renderable.
*/

class Backdrop extends Renderable {

    Backdrop() {
        shaderVFile = "guivertex.glsl";
        shaderFFile = "guifragment.glsl";
        textureFile = "stars.png";
        init();
    }

    @Override
    public void createGeometry() {
        vertexArray = new float[] {
                -1, 1, -1,
                1, 1, -1,
                1, -1, -1,
                -1, -1, -1,
        };
        textureArray = new float[] {
                -1f, 1f,
                1f, 1f,
                1f, -1f,
                -1f, -1f,
        };
        indexArray = new int[] {0, 3, 1, 1, 3, 2};
    }
}
