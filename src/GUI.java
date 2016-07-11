


class GUI extends Renderable {

    private int totalLifes = 3;
    private int currentLifes = 3;
    boolean gameOver = false;

	GUI() {
        textureFile = "life.png";
		shaderVFile = "guivertex.glsl";
		shaderFFile = "guifragment.glsl";
		init();
	}

    @Override
    public void createGeometry() {

        if(Kubuz.paused || gameOver) {

            textureFile = gameOver ? "gameover.png" : "pause.png";
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

    boolean reduceLife() {
        currentLifes = currentLifes > 0 ? --currentLifes : totalLifes;
        createGeometry();
        gameOver = currentLifes <= 0;
        return gameOver;
    }

    void pauseUnPause() {
        init();
    }
}