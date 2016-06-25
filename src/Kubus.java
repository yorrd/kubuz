/**
 * 
 * 
 * @author Sebastian Kriege
 * 
 */



public class Kubus extends Renderable {

    private int renderDepth = 15;
    private int numEdges;
    private int segments;
    private float radius = 2f;
    private float z_distance = -0.2f;
    private int rotation = 0;
    private int position = 0;

	private boolean[][] curr_level;
    private Level stage;
	
	// Constructor
	public Kubus(int segments, int numEdges){
        textureFile = "gdv.png";

		this.segments = segments;
		this.numEdges = numEdges;
    	stage = new Level(segments, numEdges);
    	curr_level = stage.get_level();
        init();
	}
	
	
	// rename to create geometry
	@Override
	public void createGeometry() {
		createVertexArray();
		createIndexArray();
		createTextureArray();
	}
	
	// creates one segment, moves it in the right z position and copies it into vertexArray
    private void createVertexArray() {
        vertexArray = new float[3  * numEdges * segments * renderDepth];
       	float[] segmentArray;
    	segmentArray = createSegment(0f);
       	int index_vertex = 0;
        for(int i = 0; i < renderDepth; i ++) {
        	segmentArray = changeZ(z_distance, segmentArray); 
            for(int j = 0; j < segmentArray.length; j ++) {
            	vertexArray[index_vertex++] = segmentArray[j];
            }
        }
    }

    // creating one segment
    private float[] createSegment(float offset_z) {
    	float[] step = new float[2];
    	float[] vertex_segment = new float[3  * numEdges * segments];
    	float angle = 2 * (float) Math.PI / numEdges;
    	int index = 0;
    	// creates first point in right position, 270° + half angle
        for(int edge = 0; edge < numEdges; edge ++) {
        	vertex_segment[index++] = (float) (radius * Math.cos(1.5f * Math.PI + angle / 2 - angle * edge));
        	vertex_segment[index++] = (float) (radius * Math.sin(1.5f * Math.PI + angle / 2 - angle * edge));
        	vertex_segment[index++] = offset_z;
        	// calculates the difference to the next point
        	step[0] = ((float) (radius * Math.cos(1.5f * Math.PI + angle / 2 - angle * (edge + 1)))
        				- vertex_segment[index - 3]) / segments;
        	step[1] = ((float) (radius * Math.sin(1.5f * Math.PI + angle / 2 - angle * (edge + 1)))
    				    - vertex_segment[index - 2]) / segments;
        	for(int i = 1; i < segments; i++) {
            	vertex_segment[index++] = vertex_segment[index - 4] + step[0];
            	vertex_segment[index++] = vertex_segment[index - 4] + step[1];
            	vertex_segment[index++] = offset_z;
        	}
        }
		return vertex_segment;
    }
    
    // creates indexArray
    private void createIndexArray() {
        indexArray = new int[6  * (numEdges * segments + 1) * renderDepth];
		int index = 0;
		for (int j = 0; j < renderDepth - 1; j++){
			for (int i = 0; i < (segments * numEdges + 1); i++){
				if (curr_level[j + position][i]) continue;
				indexArray[index++] =  j * segments * numEdges + (i + rotation) % (segments * numEdges);
				indexArray[index++] = (j + 1) * segments * numEdges + (i + rotation) % (segments * numEdges);
				indexArray[index++] = (j + 1) * segments * numEdges + (i + 1 + rotation) % (segments * numEdges);
				indexArray[index++] = (j + 1) * segments * numEdges + (i + 1 + rotation) % (segments * numEdges);
				indexArray[index++] =  j * segments * numEdges + (i + 1 + rotation) % (segments * numEdges);
				indexArray[index++] =  j * segments * numEdges + (i + rotation) % (segments * numEdges);
			}
		}
    }
    
    // creates textureArray, textures are mirrored in x-y direction
    private void createTextureArray() {
    	int index = 0;
        textureArray = new float[2  * numEdges * segments * renderDepth];
    	for(int i = 0; i < renderDepth; i ++) {
            	for(int j = 0; j < numEdges * segments; j ++) {
            		textureArray[index++] = j % 2;
            		textureArray[index++] = i;
            	}	
    	}

    }

    // adds z offset to an object
    private float[] changeZ(float offset_z, float[] vertex_segment) {
    	for(int i = 2; i < vertex_segment.length; i += 3) {
    		vertex_segment[i] += offset_z;
    	}
		return vertex_segment;
    }
    
    // moves the whole tubus in z-direction
    void moveZ(float offset_z) {
    	if (vertexArray[2] > 0.5f) {
        	position++;
        	createIndexArray();
        	vertexArray = changeZ(z_distance + offset_z, vertexArray);
        }
    	else {
        	vertexArray = changeZ(offset_z, vertexArray);
    	}
    }
    
    // turns the indices, not the vertices
    void turn(boolean direction){
    	if (direction){ // linksdrehung bei true
    		rotation += segments;
    		rotation = rotation % (segments * numEdges);
    	}
    	else { // rechtsdrehung bei false
    		rotation -= segments;
    		rotation = rotation % (segments * numEdges);
    	}
    	if (rotation < 0) rotation += segments * numEdges;
		createIndexArray();
    }
    
    // checks lvl progress
    void progress() {
    	if (position == 50 + renderDepth){ 
    		position = 0;
        	stage.next_level();
        	curr_level = stage.get_level();
    	}
    }
}
