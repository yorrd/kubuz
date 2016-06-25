
class Bug extends Renderable{

	private float ground = -1.5f; // the level where the bug walks on
	private float overGround = 0.02f;
	private float bodySize = 1f;
	private float pos_x = 0f; // bug must be able to move x-way
	private float pos_z = 0.2f; // z-position
	private float z_size = -0.01f;
	
	// Constructor
	Bug(){
		this.textureFile = "gdv.png";
		init();
	}
	
    public void createGeometry() {
    	// vertexArray
    	vertexArray = new float[6 * 9 + 6 * 3];
    	float[] tempArray;
    	int index = 0;
    	for(int j = 0; j < 3; j++) {
    		tempArray = createLeg(bodySize, z_size);
	    	for(int i = 0; i < 9; i++) {
	    		vertexArray[index++] = tempArray[i]; 
	    	}
    		tempArray = createLeg(-bodySize, z_size);
	    	for(int i = 0; i < 9; i++) {
	    		vertexArray[index++] = tempArray[i]; 
	    	}
	    	z_size += 0.01f;
    	}
    	tempArray = createBody(bodySize);
    	System.out.println(index);    	
    	for(int i = 0; i < 18; i++) {
    		vertexArray[index++] = tempArray[i]; 
    	}
    	// texture Array
    	index = 0;
    	textureArray = new float[vertexArray.length];
    	for(int i = 0; i < vertexArray.length / 3; i++) {
    		textureArray[index++] = index; 
    		textureArray[index++] = index; 
    	}
    	// indexArray
    	indexArray = new int[6 * 6 + 8 * 3];
    	int temp = 0;
    	index = 0;
    	for(int i = 0; i < 6 * 2; i++) {
    		indexArray[index++] = temp++; 
    		indexArray[index++] = temp++; 
    		indexArray[index++] = temp++; 
    	}
    	for(int i = 0; i < 4; i++) {
    		indexArray[index++] = 18; 
    		indexArray[index++] = 19 + (1 + i) % 4; 
    		indexArray[index++] = 19 + (0 + i) % 4; 
    	}
    	for(int i = 0; i < 4; i++) {
    		indexArray[index++] = 23; 
    		indexArray[index++] = 19 + (0 + i) % 4;
    		indexArray[index++] = 19 + (1 + i) % 4;  
    	}
    }
    
    private float[] createLeg(float offset_x, float offset_z) {
    	float[] legArray = new float[9];
    	legArray[0] = pos_x - offset_x;
    	legArray[1] = ground;
    	legArray[2] = pos_z + offset_z;
    	legArray[3] = pos_x - offset_x * 2 / 3;
    	legArray[4] = ground + 0.1f;
    	legArray[5] = pos_z + offset_z;
    	legArray[6] = pos_x - offset_x * 1 / 3;
    	legArray[7] = ground + overGround + bodySize;
    	legArray[8] = pos_z + offset_z;
    			
		return legArray;
    }
    
    private float[] createBody(float offset_x) {
    	float[] bodyArray = new float[18];
    	int index = 0;
    	bodyArray[index++] = pos_x;
    	bodyArray[index++] = ground + overGround;
    	bodyArray[index++] = pos_z;
		bodyArray[index++] = pos_x - offset_x * 1 / 3;
		bodyArray[index++] = ground + overGround + bodySize;
		bodyArray[index++] = pos_z - offset_x * 1 / 3;
		bodyArray[index++] = pos_x - offset_x * 1 / 3;
		bodyArray[index++] = ground + overGround + bodySize;
		bodyArray[index++] = pos_z + offset_x * 1 / 3;
		bodyArray[index++] = pos_x + offset_x * 1 / 3;
		bodyArray[index++] = ground + overGround + bodySize;
		bodyArray[index++] = pos_z + offset_x * 1 / 3;
		bodyArray[index++] = pos_x + offset_x * 1 / 3;
		bodyArray[index++] = ground + overGround + bodySize;
		bodyArray[index++] = pos_z - offset_x * 1 / 3;
    	bodyArray[index++] = pos_x;
    	bodyArray[index++] = ground + overGround + 2 * bodySize;
    	bodyArray[index++] = pos_z;
    	
		return bodyArray;
    }
}
