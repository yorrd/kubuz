/*
* Tubus, which the player is running through. This is also a renderable object.
*/
public class Tubus extends Renderable {

    private int renderDepth = 15;
    private int numEdges;
    private int segments;
    private float radius = 4f;
    private float segmentSizeZ = -0.2f;
    private float segmentSizeX = 0;
    private int rotation = 0;
    private int position = 0;

	private boolean[][] curLevel;
    private Level stage;

    /**
     * Also initialize the Level.
     *
     * @param segments number of segments side-by-side on the ground and all walls
     * @param numEdges number of sides in the tube
     */
    public Tubus(int segments, int numEdges){
        textureFile = "segment.png";

		this.segments = segments;
		this.numEdges = numEdges;
    	stage = new Level(segments, numEdges);
    	curLevel = stage.get_level();
        init();
	}

	@Override
	public void createGeometry() {
		createVertexArray();
		createIndexArray();
		createTextureArray();
		segmentSizeX = vertexArray[0] - vertexArray[3];
	}

    /**
     * Utility function for createGeometry. Creates one segment, moves it in the right z position and copies it into
     * vertexArray.
     */
    private void createVertexArray() {
        vertexArray = new float[3  * numEdges * segments * renderDepth];
       	float[] segmentArray;
    	segmentArray = createSegment(1f);
       	int index_vertex = 0;
        for(int i = 0; i < renderDepth; i ++) {
        	segmentArray = changeZ(segmentSizeZ, segmentArray); 
            for(int j = 0; j < segmentArray.length; j ++) {
            	vertexArray[index_vertex++] = segmentArray[j];
            }
        }
    }

    /**
     * Utility function for creating one segment with an offset in the z-coordinate along the tubus.
     *
     * @param offsetZ offset in the direction of the tube
     * @return the vertex segment array
     */
    private float[] createSegment(float offsetZ) {
    	float[] step = new float[2];
    	float[] vertex_segment = new float[3  * numEdges * segments];
    	float angle = 2 * (float) Math.PI / numEdges;
    	int index = 0;
    	// creates first point in right position, 270° + half angle
        for(int edge = 0; edge < numEdges; edge ++) {
        	vertex_segment[index++] = (float) (radius * Math.cos(1.5f * Math.PI + angle / 2 - angle * edge));
        	vertex_segment[index++] = (float) (radius * Math.sin(1.5f * Math.PI + angle / 2 - angle * edge));
        	vertex_segment[index++] = offsetZ;
        	// calculates the difference to the next point
        	step[0] = ((float) (radius * Math.cos(1.5f * Math.PI + angle / 2 - angle * (edge + 1)))
        				- vertex_segment[index - 3]) / segments;
        	step[1] = ((float) (radius * Math.sin(1.5f * Math.PI + angle / 2 - angle * (edge + 1)))
    				    - vertex_segment[index - 2]) / segments;
        	for(int i = 1; i < segments; i++) {
            	vertex_segment[index++] = vertex_segment[index - 4] + step[0];
            	vertex_segment[index++] = vertex_segment[index - 4] + step[1];
            	vertex_segment[index++] = offsetZ;
        	}
        }
		return vertex_segment;
    }

    /**
     * Utility function for createGeometry, creates the index array.
     */
    private void createIndexArray() {
        indexArray = new int[6  * (numEdges * segments + 1) * renderDepth];
		int index = 0;
		for (int j = 0; j < renderDepth - 1; j++){
			for (int i = 0; i < (segments * numEdges); i++) {

				if (curLevel[j + position][i]) continue;

				indexArray[index++] =  j * segments * numEdges + (i + rotation) % (segments * numEdges);
				indexArray[index++] = (j + 1) * segments * numEdges + (i + rotation) % (segments * numEdges);
				indexArray[index++] = (j + 1) * segments * numEdges + (i + 1 + rotation) % (segments * numEdges);

				indexArray[index++] = (j + 1) * segments * numEdges + (i + 1 + rotation) % (segments * numEdges);
				indexArray[index++] =  j * segments * numEdges + (i + 1 + rotation) % (segments * numEdges);
				indexArray[index++] =  j * segments * numEdges + (i + rotation) % (segments * numEdges);
			}
		}
    }

    /**
     * Utility function for createGeometry. Creates textureArray, textures are mirrored in x-y direction.
     */
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

    /**
     * Move the whole tubus in z-direction.
     *
     * @param offsetZ how much to move it
     */
    void moveZ(float offsetZ) {
        if (vertexArray[2] > 0.5f) {
            position++;
            createIndexArray();
            vertexArray = changeZ(segmentSizeZ + offsetZ, vertexArray);
        }
        else {
            vertexArray = changeZ(offsetZ, vertexArray);
        }
    }

    /**
     * Utility function. Adds a value to each z-Value of a vector array.
     *
     * @param offsetZ z-offset to add
     * @param vertex_segment array to act upon
     * @return modified array
     */
    private float[] changeZ(float offsetZ, float[] vertex_segment) {
    	for(int i = 2; i < vertex_segment.length; i += 3) {
    		vertex_segment[i] += offsetZ;
    	}
		return vertex_segment;
    }

    /**
     * Turns the tube. Not the whole thing is rotated, but just the indices. There's no animation, it just places
     * the holes one edge further in the desired direction.
     *
     * @param direction true for left, false for right
     */
    void turn(boolean direction){
    	if (direction){ // turn left
    		rotation += segments;
    		rotation = rotation % (segments * numEdges);
    	}
    	else { // turn right
    		rotation -= segments;
    		rotation = rotation % (segments * numEdges);
    	}
    	if (rotation < 0) rotation += segments * numEdges;
		createIndexArray();
    }

    /**
     * Checks the progress of the level. If it's done, get the next level and return true.
     *
     * @return true if the current level is being finished
     */
    boolean progress() {
    	if (position == 50 + renderDepth){ 
    		position = 0;
        	stage.next_level();
        	curLevel = stage.get_level();
            return true;
    	} else return false;
    }

    /**
     * @return the width of the walking ground
     */
    float getWidth() {
		return segments * segmentSizeX;
	}

    /**
     * @param posX x-value (sideways position of the player)
     * @param posZ z-value along the tube
     * @return whether the player is on a hole or not
     */
    boolean isHole(float posX, float posZ) {
        int x = (int) ((segments * segmentSizeX / 2 - posX) / segmentSizeX);
        int z = (int) ((vertexArray[2] - posZ) / segmentSizeZ);
        return z >= 0 && curLevel[z + position][(x + (segments * numEdges - rotation)) % (segments * numEdges)];
    }

    /**
     * @return the height (y-coordinate) of the ground level
     */
    float getGround() {
    	return vertexArray[1];
    }

    /**
     * Reset the tube to the beginning of the game.
     */
    void reset() {
        stage = new Level(segments, numEdges);
        curLevel = stage.get_level();
		resetTranslationMatrix();
	}
}
