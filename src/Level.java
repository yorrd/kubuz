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
	
	private boolean[][] currLevel;
	private int lvlCounter;
	private int renderDepth = 15;
	private int maxLength = 50 + 2*renderDepth;
	private int segments;
	private int edges;	
	
	
	// constructor, creates the first level with one hole per segment
	public Level(int segments, int edges){
		currLevel = new boolean[maxLength][segments * edges];
		this.segments = segments;
		this.edges = edges;
		oneHolePerSegment();
		lvlCounter++;
	}
	
	// returns the current level
	public boolean[][] get_level(){
		return currLevel;
	}
	
	// generates the next level
	public void next_level(){
		lvlCounter++;
		switch (lvlCounter){
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
		for(int i = renderDepth; i < maxLength - renderDepth; i++){
			j = rand.nextInt(segments * edges);
			currLevel[i][j] = true;
		}
	}
	
	// randomly sets a whole segment to true, afterwards one "bridge" is created
	public void removeSegment() {
		Random rand = new Random();
		int i;
		i = rand.nextInt(maxLength - 2*renderDepth);
		i += renderDepth;
		for(int j = 0; j < segments * edges; j++){
			currLevel[i][j] = true;
		}
		// generates a "bridge"
		int j = rand.nextInt(segments * edges);
		currLevel[i][j] = false;
	}
}
