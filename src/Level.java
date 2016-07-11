import java.util.Random;

public class Level {
	
	private boolean[][] curr_level;
	private int level_counter;
	private int renderDepth = 15;
	private int max_length = 50 + 2*renderDepth;
	private int segments;
	private int edges;	
	
	public Level(int segments, int edges){
		curr_level = new boolean[max_length][segments * edges];
		this.segments = segments;
		this.edges = edges;
		levelOne();
		level_counter++;
	}
	
	public boolean[][] get_level(){
		return curr_level;
	}
	
	public void next_level(){
		level_counter++;
		switch (level_counter){
			case 2: levelTwo();
					break;
			case 3: levelThree();
					break;
			case 4: levelFour();
					break;	
			case 5: levelFive();
					break;	
		}
		
	}
	
	public void levelOne() {
		Random rand = new Random();
		int j;
		for(int i = renderDepth; i < max_length - renderDepth; i++){
			j = rand.nextInt(segments * edges);
			curr_level[i][j] = true;
		}
	}
	
	public void levelTwo() {
		Random rand = new Random();
		int j;
		for(int i = renderDepth; i < max_length - renderDepth; i++){
			j = rand.nextInt(segments * edges);
			while (curr_level[i][j]) {
				j = rand.nextInt(segments * edges);				
			}
			curr_level[i][j] = true;
			while (curr_level[i][j]) {
				j = rand.nextInt(segments * edges);				
			}
			curr_level[i][j] = true;
		}
	}
	
	public void levelThree() {
		Random rand = new Random();
		int j;
		for(int i = renderDepth; i < max_length - renderDepth; i++){
			j = rand.nextInt(segments * edges);
			while (curr_level[i][j]) {
				j = rand.nextInt(segments * edges);				
			}
			curr_level[i][j] = true;
			while (curr_level[i][j]) {
				j = rand.nextInt(segments * edges);				
			}
			curr_level[i][j] = true;
		}
	}
	
	public void levelFour() {
		Random rand = new Random();
		int i;
		i = rand.nextInt(max_length - renderDepth);
		for(int j = 0; j < segments * edges; j++){
			curr_level[i][j] = true;
		}
		int j = rand.nextInt(segments * edges);
		curr_level[i][j] = false;
	}
	
	public void levelFive() {
		Random rand = new Random();
		int i;
		i = rand.nextInt(max_length - renderDepth);
		for(int j = 0; j < segments * edges; j++){
			curr_level[i][j] = true;
		}
		int j = rand.nextInt(segments * edges);
		curr_level[i][j] = false;
	}
}
