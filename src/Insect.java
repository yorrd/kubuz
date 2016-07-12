import mat.Vec3;

/**
 * The insect which is controlled by the player. It has a body and legs, can move, jump and fall. Subclass of Renderable.
 *
 * @author Sebastian Kriege
 */

class Insect extends Renderable{

	private float ground = -3.46f; // the level which the bug walks on
	private float overGround = 0.02f;
	private float bodySize = 0.05f;
	private float zPos = 1.2f;
	private int aniSteps = 18; // multiple 3
	private float legAnimation[][] = new float[6][9 * aniSteps];
	private int[] animationList;
	private float speed = 0.01f;
	private float[] body = { 
			bodySize, 0.0f, bodySize,
			bodySize, 0.0f, -bodySize,
			-bodySize, 0.0f, bodySize,
			-bodySize, 0.0f, -bodySize,		
	};
    private long fallingSince = 0;
	private int jumpingSince = 0;
    private double lastJumpPoint = 0;

	Insect(float groundLevel, float zOffset) {
        ground = groundLevel;
        zPos -= zOffset;
		this.textureFile = "bug.png";

        // change the default position of the insect, it needs to be on the ground of the tubus and somewhat in front of
        // the camera
        defaultModelAngle = new Vec3(0, 0, 0);
        defaultTranslate = new Vec3(0, bodySize / 3 + overGround + ground, zPos);

		init();
	}

    public void createGeometry() {
    	// vertexArray
    	vertexArray = new float[6 * 12 + 12];
    	// temporary array used for creating the legs
    	float[] tempArray;
    	// writing the body in the vertexArray
    	int index = 0;    	
    	for(int i = 0; i < 12; i++) {
    		vertexArray[index++] = body[i]; 
    	}
    	// creating the legs
    	animationList = new int[] {0, 1 + aniSteps / 2, aniSteps / 2 - 1 , 1, 3, 2 + aniSteps / 2,};
    	// zPos defines the position where the leg is connected to the body in z-direction
    	float zPos = bodySize * 2 / 3;
    	// creating to legs each loop on the same zPosition
    	for(int j = 0; j < 3; j++) {
    		// the point where the leg is connected to the body
    		vertexArray[index++] = bodySize;
    		vertexArray[index++] = 0;
    		vertexArray[index++] = zPos;
    		// tempArray to create parameter for createLegAnimation
    		tempArray = new float[]{bodySize, 0, zPos};
    		// creates the animation of the leg
    		createLegAnimation(2 * j,  2 * bodySize, tempArray);
    		// writes the first animation into the vertexArray
    		animateLeg(j * 2, animationList[j * 2]);
    		index += 9;
    		// repeat for the second leg
    		vertexArray[index++] = -bodySize;
    		vertexArray[index++] = 0;
    		vertexArray[index++] = zPos;
    		tempArray = new float[]{-bodySize, 0, zPos};
    		createLegAnimation(2 * j + 1, - 2 * bodySize, tempArray);
    		animateLeg(j * 2 + 1, animationList[j * 2 + 1]);
    		index += 9;
	    	zPos -= bodySize * 2 / 3;
    	}
    	// texture Array
    	index = 0;
    	textureArray = new float[2 * vertexArray.length];
    	// first the body
		textureArray[index++] = 1f; 
		textureArray[index++] = 1f;
		textureArray[index++] = 1f; 
		textureArray[index++] = 0f;
		textureArray[index++] = 0f; 
		textureArray[index++] = 1f;
		textureArray[index++] = 0f; 
		textureArray[index++] = 0f;
		// now the legs
    	for(int i = 8; i < 2 * vertexArray.length; i += 2) {
    		textureArray[index++] = 0.5f; 
    		textureArray[index++] = 0.5f; 
    	}
    	// indexArray
    	indexArray = new int[6 + 36];
    	index = 0;
    	// first the body
		indexArray[index++] = 0; 
		indexArray[index++] = 1;
		indexArray[index++] = 2; 
		indexArray[index++] = 1;
		indexArray[index++] = 2; 
		indexArray[index++] = 3;
		// now the legs
    	for(int i = 0; i < 6; i++) {
    		indexArray[index++] = 4 + i * 4; 
    		indexArray[index++] = 5 + i * 4; 
    		indexArray[index++] = 6 + i * 4; 
    		indexArray[index++] = 5 + i * 4; 
    		indexArray[index++] = 6 + i * 4; 
    		indexArray[index++] = 7 + i * 4; 
    	}
    }

    /**
     * Short function to transfer the next step of the animation into vertexArray.
     *
     * @param number specify which leg we're talking about
     * @param step number of steps
     */
    private void animateLeg(int number, int step){
    	for(int i = 0; i < 9; i += 3) {
        	vertexArray[15 + number * 12 + i] = legAnimation[number][step * 9 + i];
        	vertexArray[16 + number * 12 + i] = legAnimation[number][step * 9 + i + 1];
        	vertexArray[17 + number * 12 + i] = legAnimation[number][step * 9 + i + 2];
    	}
    }

    /**
     * Do one step in the leg animation.
     */
    void animate() {
    	for (int i = 0; i < 6; i++) {
    		animationList[i] = (animationList[i] + 1) % aniSteps;
    		animateLeg(i, animationList[i]);
    	}
    }

    /**
     * Creates animation list of one leg, scaleX defines direction and range.
     *
     * @param number leg number
     * @param scaleX direction and range
     * @param root origin of the leg (at the body)
     */
    private void createLegAnimation(int number, float scaleX, float[] root) {
    	float zPos = - aniSteps * speed / 2;
    	int index = 0;
    	float legPart = 0.3f;
    	// no correct geometric interpolation, this angle makes it look more vivid
    	float alpha = (float) (2 * Math.PI) * 30 / 360;
    	// two third of the animation is "pulling" the leg into the back of the insect
    	for(int i = 0; i < 2 * aniSteps / 3; i++) {
    		// two points for the joint
    		legAnimation[number][index++] = root[0] + scaleX / 3;
    		legAnimation[number][index++] = (float) Math.sin(alpha) * legPart + root[1] + 0.01f;
    		legAnimation[number][index++] = root[2] + zPos / 3;
    		legAnimation[number][index++] =legAnimation[number][index - 4];
    		legAnimation[number][index++] =legAnimation[number][index - 4] - 0.02f;
    		legAnimation[number][index++] =legAnimation[number][index - 4];
    		// one point for the foot
    		legAnimation[number][index++] = root[0] + scaleX;
    		legAnimation[number][index++] = root[1] - bodySize / 3 - overGround;
    		legAnimation[number][index++] = root[2] + zPos;
    		// preparing the zPos and angle alpha for the next animation step
    		zPos += speed;
    		alpha += (float) (2 * Math.PI)* 30 / (aniSteps * 360);
    	}
    	// one third of the animation is pulling the leg to the front of the insect
    	for(int i = 0; i < aniSteps / 3; i++) {
    		// two points for the joint
    		legAnimation[number][index++] = root[0] + scaleX / 3;
    		legAnimation[number][index++] = (float) Math.sin(alpha) * legPart + root[1] + 0.01f + overGround;
    		legAnimation[number][index++] = root[2] + zPos / 3;
    		legAnimation[number][index++] =legAnimation[number][index - 4];
    		legAnimation[number][index++] =legAnimation[number][index - 4] - 0.02f;
    		legAnimation[number][index++] =legAnimation[number][index - 4];
    		// one point for the foot
    		legAnimation[number][index++] = root[0] + scaleX;
    		legAnimation[number][index++] = root[1] - bodySize / 3;
    		legAnimation[number][index++] = root[2] + zPos;
    		// preparing the zPos and angle alpha for the next animation step
    		zPos -= 2 * speed;
    		alpha -= 2 * (float) (2 * Math.PI)* 30 / (aniSteps * 360);
    	}
    }

    /**
     * @param left left border
     * @param right right border
     * @return whether the insect is within those borders
     */
    boolean isInBounds(float left, float right) {
		return ((float)translate.x + 8 * bodySize / 3) < right && ((float)translate.x - 8 * bodySize / 3) > left;
	}

    /**
     * Do one step in the falling animation. This is using the standard physical formula for falling objects.
     */
    void fall() {
        if(fallingSince == 0)
            fallingSince = System.currentTimeMillis();

        float duration = (System.currentTimeMillis() - fallingSince) / 1000f;  // difference to now in seconds
		modifyModel(0, 0, 0, 0, -duration * 9.81f / 10, 0);  // 1/10 just looks nice
    }

    /**
     * Reset the insect to its original position (used for the reset for example)
     */
    void reset() {
        fallingSince = 0;
        jumpingSince = 0;
        resetTranslationMatrix();
    }

    /**
     * @return the width of the body
     */
    float getBodyWidth() {
        return 0.15f * 2;
    }

    /**
     * Initiate the jump animation.
     */
    void jump() {
		jumpingSince = 1;
	}

    /**
     * Do one step in the jumping animation, according to a quadratic formula (-1/1800(x^2-30)+.5). Triggered by the
     * main loop.
     */
    void doJumpStep() {
        float x = jumpingSince++;
        double currentY = -1/1800D*Math.pow(x-30, 2) + .5f;
        double delta = currentY - lastJumpPoint;
        lastJumpPoint = currentY;

        if(jumpingSince > 60 + 1) {
            jumpingSince = 0;
            lastJumpPoint = 0;
        } else modifyModel(0, 0, 0, 0, (float) delta, 0);
	}

    /**
     * @return whether we're currently in the jumping animation or not
     */
    boolean isJumping() {
		return jumpingSince != 0;
	}
}
