/**
 * 
 * 
 * @author Sebastian Kriege
 * Damit die Ümläüte öch rüchtüg gäyn
 */



public class Kubus implements Renderable {

    private int renderDepth = 15;
    private int numEdges = 4;
    private int segments = 2;
    private float radius = 2f;
    private float z_distance = -0.04f;

    private float[] vertexArray;
    private float[] textureArray;
    private int[] indexArray;
    
	@Override
	public void render() {
		create_initial();
		indexArray = create_index();
		create_texture();
		
	}

    public void create_initial() {
       	float[] segmentArray = new float[3  * numEdges * segments];
    	segmentArray = create_segment(0f);
    	vertexArray = new float[segmentArray.length * renderDepth];
       	int index_vertex = 0;
        for(int i = 0; i < renderDepth; i ++) {
        	segmentArray = changeZ(z_distance, segmentArray); 
            for(int j = 0; j < segmentArray.length; j ++) {
            	vertexArray[index_vertex++] = segmentArray[j];
            }
        }
    }

    public float[] create_segment(float offset_z) {
    	float[] step = new float[2];
    	float[] vertex_segment = new float[3  * numEdges * segments];
    	float angle = 2 * (float) Math.PI / numEdges;
    	int index = 0;
    	// erstellt ersten Eckpunkt in richtiger Position 270° + halber winkel
        for(int edge = 0; edge < numEdges; edge ++) {
        	vertex_segment[index++] = (float) (radius * Math.cos(1.5f * Math.PI + angle / 2 - angle * edge));
        	vertex_segment[index++] = (float) (radius * Math.sin(1.5f * Math.PI + angle / 2 - angle * edge));
        	vertex_segment[index++] = offset_z;
        	// für die Schrittweite wird der naechste Eckpunkt berechnet und der Abstand durch Anzahl segmente geteilt
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
    
    public int[] create_index() {
    	int[] indexArray = new int[6  * (numEdges * segments + 1) * renderDepth];
		int index = 0;
		for (int j = 0; j < renderDepth - 1; j++){
			for (int i = 0; i < (segments * numEdges + 1); i++){
					indexArray[index++] =  j * segments * numEdges + i % (segments * numEdges);
					indexArray[index++] = (j + 1) * segments * numEdges + (i + 1) % (segments * numEdges);
					indexArray[index++] = (j + 1) * segments * numEdges + i % (segments * numEdges);
					indexArray[index++] = (j + 1) * segments * numEdges + (i + 1) % (segments * numEdges);
					indexArray[index++] =  j * segments * numEdges + i % (segments * numEdges);
					indexArray[index++] =  j * segments * numEdges + (i + 1) % (segments * numEdges);
				}
			}
		return indexArray;
    }
    
    public void create_texture() {
        textureArray = new float[]{
                0.0f, 1.0f,
                0.5f, 1.0f,
                1.0f, 1.0f,
        };
    }

    // Funktion zum Anpassen der z-Koordinate
    public float[] changeZ(float offset_z, float[] vertex_segment) {
    	for(int i = 2; i < vertex_segment.length; i += 3) {
    		vertex_segment[i] += offset_z;
    	}
		return vertex_segment;
    }
    
    public void moveZ(float offset_z){
    	vertexArray = changeZ(offset_z, vertexArray);
    	if (vertexArray[2] > 0.4f) {
        	vertexArray = changeZ(z_distance, vertexArray);
        	}
    	System.out.println(vertexArray[2]);
    }

    @Override
    public float[] getVertexArray() {
        return vertexArray;
    }

    @Override
    public float[] getTextureArray() {
        return textureArray;
    }

    @Override
    public int[] getIndexArray() {
        return indexArray;
    }

    @Override
    public String getTextureFilename() {
        return "./assets/gdv.png";
    }
}
