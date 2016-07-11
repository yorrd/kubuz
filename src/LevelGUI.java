


class LevelGUI {

    private float unitWidth = .1f;

    private DigitGUI one = new DigitGUI(0);
    private DigitGUI ten = new DigitGUI(1);
    private DigitGUI hundred = new DigitGUI(2);
    private DigitGUI thousand = new DigitGUI(3);

    LevelGUI() {
        one.modifyModel(0, 0, 0, unitWidth * 3, 0, 0);
        ten.modifyModel(0, 0, 0, unitWidth * 2, 0, 0);
        hundred.modifyModel(0, 0, 0, unitWidth * 1, 0, 0);
        thousand.modifyModel(0, 0, 0, unitWidth * 0, 0, 0);
    }

    public void increase() {
        if(one.n < 9) one.increase();
        else if(ten.n < 9) {one.reset(); ten.increase();}
        else if(hundred.n < 9) {ten.reset(); ten.increase();}
        else if(thousand.n < 9) {hundred.reset(); ten.increase();}
        // else just leave it at 999 if anyone ever gets there =)
    }

    void render() {
        one.render();
        ten.render();
        hundred.render();
        thousand.render();
    }

    void reset() {
        one.reset();
        ten.reset();
        hundred.reset();
        thousand.reset();
        render();
    }


    private class DigitGUI extends Renderable {

        int n = 0;
        int positionFromLeft = 0;

        private DigitGUI(int nth) {
            positionFromLeft = 4 - nth;
            shaderVFile = "guivertex.glsl";
            shaderFFile = "guifragment.glsl";
            init();
        }

        @Override
        public void createGeometry() {
            textureFile = n+".png";

            vertexArray = new float[] {
                    -1f + unitWidth * positionFromLeft, 1f - unitWidth, 1,  // left bottom
                    -1f + unitWidth + unitWidth * positionFromLeft, 1f - unitWidth, 1,  // right bottom
                    -1f + unitWidth * positionFromLeft, 1f, 1,  // left top
                    -1f + unitWidth + unitWidth * positionFromLeft, 1f, 1,  // right top
            };

            // second attribute, textures
            textureArray = new float[] {
                    0f, 1f,
                    1f, 1f,
                    0f, 0f,
                    1f, 0f,
            };

            indexArray = new int[] {0, 1, 2, 2, 1, 3};
        }

        void increase() {
            if (n == -1) n = 0;
            n++;
            init();
        }

        void reset() {
            n = 0;
            init();
        }
    }
}