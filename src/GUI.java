/**
 * Multi-Purpose GUI, shows pause and game over screens or number of lives while playing
 *
 * @author Kai Brobeil
 */
class GUI extends Renderable {

    private int totalLifes = 3;
    private int currentLifes = 3;
    boolean gameOver = false;

	GUI() {
        textureFile = "life.png";
        // we're using custom shaders without matrices since we're just mapping to screen coordinates from -1 to 1
		shaderVFile = "guivertex.glsl";
		shaderFFile = "guifragment.glsl";
        // calling init because we have to (to set up buffers and shaders etc)
		init();
	}

    @Override
    public void createGeometry() {

        // if we're in pause or gameover mode (start mode = pause mode), use the big frame
        if(Kubuz.paused || gameOver) {

            textureFile = gameOver ? "gameOver.png" : "pause.png";
            vertexArray = new float[] {
                    -1, -1, 1,
                     1, -1, 1,
                    -1,  1, 1,
                     1,  1, 1,
            };
            textureArray = new float[] {
                    0, 1,
                    1, 1,
                    0, 0,
                    1, 0,
            };
            indexArray = new int[] {0, 1, 2, 2, 1, 3};

            // otherwise use the small frame in the upper right for the hearts
        } else {
            textureFile = "life.png";
            float width = .1f;

            vertexArray = new float[] {
                    1 - currentLifes * width, .9f, 1,  // left bottom
                    1f, .9f, 1,  // right bottom
                    1 - currentLifes * width, 1f, 1,  // left top
                    1f, 1f, 1,  // right top
            };

            // second attribute, textures
            textureArray = new float[] {
                    0f, 1f,
                    currentLifes, 1f,
                    0f, 0f,
                    currentLifes, 0f,
            };

            indexArray = new int[] {0, 1, 2, 2, 1, 3};
        }

    }

    /**
     * reduce lives by one (when the player falls through a hole for example)
     *
     * @return if there are lives left
     */
    boolean reduceLife() {
        currentLifes = currentLifes > 0 ? --currentLifes : totalLifes;
        createGeometry();
        gameOver = currentLifes <= 0;
        return gameOver;
    }

    /**
     * reinitialize when switching between pause modi. This is enough because the only changes happens in
     * createGeometry()
     */
    void pauseUnPause() {
        init();
    }
}