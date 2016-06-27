
class Bug extends Renderable{

	private float ground = -1.5f; // the level where the bug walks on
	private float overGround = 0.02f;
	private float bodySize = 1f;
	private float pos_x = 0f; // bug must be able to move x-way
	private float pos_z = 0.2f; // z-position
	private float z_size = -0.01f;
	private float[] body = { 
			0.0f, 0.3f, 0.0f,	
			0.25f, 0.0f, 0.25f,
			0.25f, 0.0f, -0.25f,
			-0.25f, 0.0f, 0.25f,
			-0.25f, 0.0f, -0.25f,
			0.0f, -0.3f, 0.0f,			
	};
	
	// Constructor
	Bug(){
		this.textureFile = "leg.png";
		init();
	}
	
    public void createGeometry() {
    	// vertexArray
    	vertexArray = new float[6 * 9 + 6 * 3];
    	float[] tempArray;
    	float zPos = 0.25f;
    	int index = 0;    	
    	for(int i = 0; i < 18; i++) {
    		vertexArray[index++] = body[i]; 
    	}
    	for(int j = 0; j < 3; j++) {
    		tempArray = createLeg(0.25f, zPos);
	    	for(int i = 0; i < 9; i++) {
	    		vertexArray[index++] = tempArray[i]; 
	    	}
    		tempArray = createLeg(-0.25f, zPos);
	    	for(int i = 0; i < 9; i++) {
	    		vertexArray[index++] = tempArray[i]; 
	    	}
	    	zPos -= 0.25f;
    	}
    	// texture Array
    	index = 0;
    	textureArray = new float[2 * vertexArray.length];
    	for(int i = 0; i < 6; i++) {
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
    	indexArray = new int[24 + 18];
    	index = 0;
    	for(int i = 0; i < 4; i++) {
    		indexArray[index++] = 0; 
    		indexArray[index++] = 1 + (1 + i) % 4; 
    		indexArray[index++] = 1 + i % 4; 
    	}
    	for(int i = 0; i < 4; i++) {
    		indexArray[index++] = 5; 
    		indexArray[index++] = 1 + i % 4;
    		indexArray[index++] = 1 + (1 + i) % 4; 
    	}
    	for(int i = 0; i < 6; i++) {
    		indexArray[index++] = 6 + i * 3; 
    		indexArray[index++] = 7 + i * 3; 
    		indexArray[index++] = 8 + i * 3; 
    	}
    	vertexArray = changeZ(-0.1f, vertexArray);
    }
    
    private float[] createLeg(float offset_x, float offset_z) {
    	float[] legArray = new float[9];
    	legArray[0] = offset_x;
    	legArray[1] = 0;
    	legArray[2] = offset_z;
    	legArray[3] = 2 * offset_x;
    	legArray[4] = 1;
    	legArray[5] = offset_z;
    	legArray[6] = 3 * offset_x;
    	legArray[7] = -1;
    	legArray[8] = offset_z;
    			
		return legArray;
    }
    
    private float[] changeZ(float offset_z, float[] vertex_segment) {
    	for(int i = 2; i < vertex_segment.length; i += 3) {
    		vertex_segment[i] += offset_z;
    	}
		return vertex_segment;
    }

}
