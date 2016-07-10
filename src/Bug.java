import mat.Vec3;

class Bug extends Renderable{

	private float ground = -3.46f; // the level where the bug walks on
	private float overGround = 0.02f;
	private float bodySize = 0.05f;
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
	
	// Constructor
	Bug(){
		this.textureFile = "bug.png";

        defaultModelAngle = new Vec3(0, 0, 0);
        defaultTranslate = new Vec3(0, ground, 1.2);

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
    	// we created the insect around the coordinate origin, so we have to put him into the right place
    	modifyModel(0,0,0,0, bodySize / 3 + overGround + ground, 0.2f);
    }
    
    // short function to transfer the next step of the animation into vertexArray
    private void animateLeg(int number, int step){
    	for(int i = 0; i < 9; i += 3) {
        	vertexArray[15 + number * 12 + i] =legAnimation[number][step * 9 + i];
        	vertexArray[16 + number * 12 + i] =legAnimation[number][step * 9 + i + 1];
        	vertexArray[17 + number * 12 + i] =legAnimation[number][step * 9 + i + 2];
    	}
    }
    
    // public function to handle the animation
    public void animate() {
    	for (int i = 0; i < 6; i++) {
    		animationList[i] = (animationList[i] + 1) % aniSteps;
    		animateLeg(i, animationList[i]);
    	}
    }
    
    // creates animation list of one leg, scaleX defines direction and range
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
    
	boolean isInBounds(float left, float right) {
		// TODO use proper width rather than estimate
		return ((float)translate.x + 8 * bodySize / 3) < right && ((float)translate.x - 8 * bodySize / 3) > left;
	}
    
	// function to check where the insect is right now
    public float getX() {
        return (float)translate.x;
    }

    // makes the insect fall
    public void fall() {
        if(fallingSince == 0)
            fallingSince = System.currentTimeMillis();

        float duration = (System.currentTimeMillis() - fallingSince) / 1000f;  // difference to now in seconds
		modifyModel(0, 0, 0, 0, -duration * 9.81f / 10, 0);  // 1/10 just looks nice
    }

    public void reset() {
        fallingSince = 0;
        super.reset();
    }

    public float getBodyWidth() {
        return 0.15f * 2;
    }
}
