
/**
 * Another GUI object just for the level counter in the top right. This holds 4 tiny Renderables which it just displays.
 *
 * @author Kai Brobeil
 */

class LevelGUI {

    // width and height of the numbers
    private float unitWidth = .1f;

    // references to all Renderable objects we're using
    private DigitGUI one = new DigitGUI(0);
    private DigitGUI ten = new DigitGUI(1);
    private DigitGUI hundred = new DigitGUI(2);
    private DigitGUI thousand = new DigitGUI(3);

    /**
     * Move contained Renderables side-by-side, not on top of each other.
     */
    LevelGUI() {
        one.modifyModel(0, 0, 0, unitWidth * 3, 0, 0);
        ten.modifyModel(0, 0, 0, unitWidth * 2, 0, 0);
        hundred.modifyModel(0, 0, 0, unitWidth * 1, 0, 0);
        thousand.modifyModel(0, 0, 0, unitWidth * 0, 0, 0);
    }

    /**
     * Increase the number which is displayed. This works <999, afterwards it won't increase anymore.
     */
    void increase() {
        if(one.n < 9) one.increase();
        else if(ten.n < 9) {one.reset(); ten.increase();}
        else if(hundred.n < 9) {ten.reset(); ten.increase();}
        else if(thousand.n < 9) {hundred.reset(); ten.increase();}
        // else just leave it at 999 if anyone ever gets there =)
    }

    /**
     * Render all Renderable objects contained.
     */
    void render() {
        one.render();
        ten.render();
        hundred.render();
        thousand.render();
    }

    /**
     * Reset all Renderable objects and render the result (0000)
     */
    void reset() {
        one.reset();
        ten.reset();
        hundred.reset();
        thousand.reset();
        render();
    }


    /*
    * Renderable digits which we're using for the LevelGUI
    */

    private class DigitGUI extends Renderable {

        int n = 0;  // number to show
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

        /**
         * Increase the number we're showing. Make sure to reinitialize so that we see the changes.
         */
        void increase() {
            if (n == -1) n = 0;
            n++;
            init();
        }

        /**
         * Reset the number to 0 and make sure to reinitialize so that we see the changes.
         */
        void reset() {
            n = 0;
            init();
        }
    }
}