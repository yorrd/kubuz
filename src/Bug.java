
class Bug extends Renderable{

	private float ground = -1.61f; // the level where the bug walks on
	private float overGround = 0.02f;
	private float bodySize = 0.05f;
	private float posX = 0f;
	private int aniSteps = 18; // multiple 3
	private float legAnimation[][] = new float[6][9 * aniSteps];
	private int[] animationList;
	private float speed = 0.01f;
	private float[] body = { 
			bodySize / 2, 0.0f, bodySize * 2 / 3,
			bodySize / 2, 0.0f, -bodySize* 2 / 3,
			-bodySize / 2, 0.0f, bodySize* 2 / 3,
			-bodySize / 2, 0.0f, -bodySize* 2 / 3,		
	};
	
	// Constructor
	Bug(){
		this.textureFile = "test3.png";
		init();
	}
	
    public void createGeometry() {
    	// vertexArray
    	vertexArray = new float[6 * 12 + 12];
    	float[] tempArray;
    	int index = 0;    	
    	for(int i = 0; i < 12; i++) {
    		vertexArray[index++] = body[i]; 
    	}
    	animationList = new int[] {0, 1 + aniSteps / 2, aniSteps / 2 - 1 , 1, 3, 2 + aniSteps / 2,};
    	float zPos = bodySize;
    	for(int j = 0; j < 3; j++) {
    		vertexArray[index++] = bodySize;
    		vertexArray[index++] = 0;
    		vertexArray[index++] = zPos;
    		tempArray = new float[]{bodySize, 0, zPos};
    		createLegAnimation(2 * j,  2 * bodySize, tempArray);
    		animateLeg(j * 2, animationList[j * 2]);
    		index += 9;
    		vertexArray[index++] = -bodySize;
    		vertexArray[index++] = 0;
    		vertexArray[index++] = zPos;
    		tempArray = new float[]{-bodySize, 0, zPos};
    		createLegAnimation(2 * j + 1, - 2 * bodySize, tempArray);
    		animateLeg(j * 2 + 1, animationList[j * 2 + 1]);
    		index += 9;
	    	zPos -= bodySize;
    	}
    	// texture Array
    	index = 0;
    	textureArray = new float[2 * vertexArray.length];
    	for(int i = 0; i < 4; i++) {
    		textureArray[index++] = 0f; 
    		textureArray[index++] = 0f;
    	}
    	for(int i = 6; i < vertexArray.length; i += 3) {
    		textureArray[index++] = 0f; 
    		textureArray[index++] = 0f; 
    		textureArray[index++] = 1f; 
    		textureArray[index++] = 0f; 
    		textureArray[index++] = 1f; 
    		textureArray[index++] = 1f; 
    	}
    	// indexArray
    	indexArray = new int[6 + 36];
    	index = 0;
		indexArray[index++] = 0; 
		indexArray[index++] = 1;
		indexArray[index++] = 2; 
		indexArray[index++] = 1;
		indexArray[index++] = 2; 
		indexArray[index++] = 3;
    	for(int i = 0; i < 6; i++) {
    		indexArray[index++] = 4 + i * 4; 
    		indexArray[index++] = 5 + i * 4; 
    		indexArray[index++] = 6 + i * 4; 
    		indexArray[index++] = 5 + i * 4; 
    		indexArray[index++] = 6 + i * 4; 
    		indexArray[index++] = 7 + i * 4; 
    	}
    	rePos(0, bodySize / 3 + overGround + ground, 0.1f);
    }
    
    private void animateLeg(int number, int step){
    	for(int i = 0; i < 9; i += 3) {
        	vertexArray[15 + number * 12 + i] =legAnimation[number][step * 9 + i] + posX;
        	vertexArray[16 + number * 12 + i] =legAnimation[number][step * 9 + i + 1];
        	vertexArray[17 + number * 12 + i] =legAnimation[number][step * 9 + i + 2];
    	}
    }
    
    public void animate() {
    	for (int i = 0; i < 6; i++) {
    		animationList[i] = (animationList[i] + 1) % aniSteps;
    		animateLeg(i, animationList[i]);
    	}
    }
    
    // creates animation list for one leg, scaleX defines direction and range
    private void createLegAnimation(int number, float scaleX, float[] root) {
    	float zPos = - aniSteps * speed / 2 + root[2];
    	int index = 0;
    	for(int i = 0; i < 2 * aniSteps / 3; i++) {
    		legAnimation[number][index++] = root[0] + scaleX / 3;
    		legAnimation[number][index++] = (float) Math.sqrt(Math.pow(0.5f, 2) 
    										-Math.pow(scaleX / 3, 2)
    										-Math.pow((zPos - root[2]) / 3, 2)
    										 ) + root[1] + 0.01f;
    		legAnimation[number][index++] = root[2] + (zPos - root[2]) / 3;
    		legAnimation[number][index++] =legAnimation[number][index - 4];
    		legAnimation[number][index++] =legAnimation[number][index - 4] - 0.02f;
    		legAnimation[number][index++] =legAnimation[number][index - 4];
    		legAnimation[number][index++] = root[0] + scaleX;
    		legAnimation[number][index++] = root[1] - bodySize / 3 - overGround;
    		legAnimation[number][index++] = root[2] + zPos;
    		zPos += speed;
    	}
    	for(int i = 0; i < aniSteps / 3; i++) {
    		legAnimation[number][index++] = root[0] + scaleX / 3;
    		legAnimation[number][index++] = (float) Math.sqrt(Math.pow(0.5f, 2) 
    										-Math.pow(scaleX / 3, 2)
    										-Math.pow((zPos - root[2]) / 3, 2)
    										 ) + root[1] + 0.01f + overGround;
    		legAnimation[number][index++] = root[2] + (zPos - root[2]) / 3;
    		legAnimation[number][index++] =legAnimation[number][index - 4];
    		legAnimation[number][index++] =legAnimation[number][index - 4] - 0.02f;
    		legAnimation[number][index++] =legAnimation[number][index - 4];
    		legAnimation[number][index++] = root[0] + scaleX;
    		legAnimation[number][index++] = root[1] - bodySize / 3;
    		legAnimation[number][index++] = root[2] + zPos;
    		zPos -= 2 * speed;
    	}
    	System.out.println(index);
    }
    
    public void rePos(float offsetX, float offsetY, float offsetZ) {
    	for(int i = 0; i < vertexArray.length; i += 3) {
    		vertexArray[i	 ] += offsetX;
    		vertexArray[i + 1] += offsetY;
    		vertexArray[i + 2] += offsetZ;
    	}
    	for(int i = 0; i < 6; i++) {
    		for(int j = 0; j < aniSteps * 9; j += 3) {
	    		legAnimation[i][j    ] += offsetX;
	    		legAnimation[i][j + 1] += offsetY;
	    		legAnimation[i][j + 2] += offsetZ;
    		}
    	}
    }
    
    private void scale(float scale) {
    	for(int i = 0; i < vertexArray.length; i++) {
    		vertexArray[i] = vertexArray[i] * scale;
    	}
    	for(int i = 0; i < 6; i++) {
    		for(int j = 0; j < aniSteps * 9; j++) {
	    		legAnimation[i][j] = legAnimation[i][j] * scale;
    		}
    	}
    }
    
    public void moveX(float scaleX) {
    	posX += scaleX;
    	for(int i = 0; i < vertexArray.length; i += 3) {
    		vertexArray[i] += scaleX;
    	}
    }

}
