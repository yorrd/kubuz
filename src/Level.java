/**
 * Class for randomly creating the level
 * data structure is an 2d-array, where the first index is the segment
 * and the second index is the position in the segment
 * the 2d-array contains boolean, true means in that position is one hole
 * 
 * @author Sebastian Kriege
 * 
 */


import java.util.Random;

public class Level {
	
	private boolean[][] curr_level;
	private int level_counter;
	private int renderDepth = 15;
	private int max_length = 50 + 2*renderDepth;
	private int segments;
	private int edges;	
	
	
	// constructor, creates the first level with one hole per segment
	public Level(int segments, int edges){
		curr_level = new boolean[max_length][segments * edges];
		this.segments = segments;
		this.edges = edges;
		oneHolePerSegment();
		level_counter++;
	}
	
	// returns the current level
	public boolean[][] get_level(){
		return curr_level;
	}
	
	// generates the next level
	public void next_level(){
		level_counter++;
		switch (level_counter){
			case 2: oneHolePerSegment();
					break;
			case 3: oneHolePerSegment();
				    oneHolePerSegment();
					break;
			case 4: oneHolePerSegment();
		    		oneHolePerSegment();
					break;	
			default:removeSegment();
					removeSegment();
					break;	
		}
		
	}
	
	// randomly generates one hole per segment
	public void oneHolePerSegment() {
		Random rand = new Random();
		int j;
		for(int i = renderDepth; i < max_length - renderDepth; i++){
			j = rand.nextInt(segments * edges);
			curr_level[i][j] = true;
		}
	}
	
	// randomly sets a whole segment to true, afterwards one "bridge" is created
	public void removeSegment() {
		Random rand = new Random();
		int i;
		i = rand.nextInt(max_length - 2*renderDepth);
		i += renderDepth;
		for(int j = 0; j < segments * edges; j++){
			curr_level[i][j] = true;
		}
		// generates a "bridge"
		int j = rand.nextInt(segments * edges);
		curr_level[i][j] = false;
	}
}
