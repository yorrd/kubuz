


public class GUI extends Renderable{


	public GUI(String textureFile) {
		this.textureFile = textureFile;
		createGeometry();
		shaderVFile = "guivertex.glsl";
		shaderFFile = "guifragment.glsl";
		init();
	}

    
    private void createGeometry() {

        vertexArray = new float[] {
                0, 0, 1,  // left bottom
                .5f, 0, 1,  // right bottom
                0, .5f, 1,  // left top
                .5f, .5f, 1,  // right top
        };

        // second attribute, textures
        textureArray = new float[] {
                0f, 0f,
                1f, 0f,
                0f, 1f,
                1f, 1f
        };

        indexArray = new int[]{0, 1, 2, 2, 1, 3};
    }
}