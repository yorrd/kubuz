import java.util.Random;

public class Level {
	
	private boolean[][] curr_level;
	private int level_counter;
	private int renderDepth = 15;
	private int max_length = 50 + 2*renderDepth;
	private int segments;
	private int edges;	
	
	public Level(int segments, int edges){
		curr_level = new boolean[max_length][segments * edges + 1];
		this.segments = segments;
		this.edges = edges;
		level_one();
		level_counter++;
	}
	
	public boolean[][] get_level(){
		return curr_level;
	}
	
	public void next_level(){
		level_counter++;
		switch (level_counter){
			case 2: level_two();
					break;
		}
		
	}
	
	public void level_one() {
		Random rand = new Random();
		int j;
		for(int i = renderDepth; i < max_length - renderDepth; i++){
			j = rand.nextInt(segments * edges + 1);
			curr_level[i][j] = true;
		}
	}
	
	public void level_two() {
		Random rand = new Random();
		int j;
		for(int i = renderDepth; i < max_length - renderDepth; i++){
			j = rand.nextInt(segments * edges + 1);
			curr_level[i][j] = true;
			j = rand.nextInt(segments * edges + 1);
			curr_level[i][j] = true;
		}
	}
}
