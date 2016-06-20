

public class Kubus implements Renderable {

    private int renderDepth = 8;
    private int numEdges = 4;
    private int segments = 2;
    private float radius = 1f;

    private float[] vertexArray;
    private float[] textureArray;
    private int[] indexArray;

    public float[] create_initial() {
       	float[] segmentArray = new float[3  * numEdges * segments];
    	segmentArray = create_segment(0f);
    	vertexArray = new float[segmentArray.length * renderDepth];
       	int index_vertex = 0;
       	float z = 0f;
        for(int i = 0; i < renderDepth; i ++) {
        	segmentArray = changeZ(z, segmentArray); 
            for(int j = 0; j < segmentArray.length; j ++) {
            	vertexArray[index_vertex++] = segmentArray[j];
            }
        	z += 0.2f;
        }
    	for(int i = 0; i < vertexArray.length; i++) {
    		System.out.println(vertexArray[i]);
    	}
		return vertexArray;
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
        	// für die Schrittweite wird der nächste Eckpunkt berechnet und der Abstand durch Anzahl segmente geteilt
        	step[0] = ((float) (radius * Math.cos(1.5f * Math.PI + angle / 2 - angle * (edge + 1)))
        				- vertex_segment[index - 3]) / segments;
        	step[1] = ((float) (radius * Math.sin(1.5f * Math.PI + angle / 2 - angle * (edge + 1)))
    				    - vertex_segment[index - 2]) / segments;
        	System.out.println("Step" + step[0] + step[1]);
        	for(int i = 1; i < segments; i++) {
            	vertex_segment[index++] = vertex_segment[index - 4] + step[0];
            	vertex_segment[index++] = vertex_segment[index - 4] + step[1];
            	vertex_segment[index++] = offset_z;
        	}
        }
		return vertex_segment;
    }
    
    // Funktion zum Anpassen der z-Koordinate
    public float[] changeZ(float offset_z, float[] vertex_segment) {
    	for(int i = 2; i < vertex_segment.length; i += 3) {
    		vertex_segment[i] = offset_z;
    	}
		return vertex_segment;
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
