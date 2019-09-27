import java.util.Arrays;

public class Bot0 implements BotAPI {

    // The public API of Bot must not change
    // This is ONLY class that you can edit in the program
    // Rename Bot to the name of your team. Use camel case.
    // Bot may not alter the state of the game objects
    // It may only inspect the state of the board and the player objects

    private PlayerAPI me, opponent;
    private BoardAPI board;
    private CubeAPI cube;
    private MatchAPI match;
    private InfoPanelAPI info;
    
    private double countDiffWeight = 1, blockBlotDiffWeight = 1, homeBoardBlockDiffWeight = 2;

    Bot0(PlayerAPI me, PlayerAPI opponent, BoardAPI board, CubeAPI cube, MatchAPI match, InfoPanelAPI info) {
        this.me = me;
        this.opponent = opponent;
        this.board = board;
        this.cube = cube;
        this.match = match;
        this.info = info;
    }

    public String getName() {
        return "Bot0"; // must match the class name
    }

    //Gives score to which move is best
    public String getCommand(Plays possiblePlays) {
        // Add your code here
    	int score[] = new int [possiblePlays.number()];
    	Arrays.fill(score, 0);
    	int i=0;
    	// Applies score based on certain features
    	for(Play play: possiblePlays)
    	{
    		score[i] += countDiff(play)*countDiffWeight;
    		score[i] += blockBlotDiff(play)*blockBlotDiffWeight;
    		score[i] += homeBoardAndAnchorBlock(play)*homeBoardBlockDiffWeight;
    		i++;
    	}
    	
    	// Pick the move with the highest score
    	int command = 1;
    	for(i = 0; i < possiblePlays.number()-1; i++) {
    		if(score[i+1] > score[i]) {
				command = i + 2;
			}  
    		System.out.println("Score move " + (i+1) + ": " + score[i]);
    	}
        return Integer.toString(command);
    }
    
    private int homeBoardAndAnchorBlock(Play play)
    {
    	int [][] currentPipLocations = new int [2][26];
    	int count = 0, i = 0, j = 0;
    	for(i=0; i<2; i++)
    	{
    		for(j=0; j<26; j++)
    		{
    			currentPipLocations[i][j] = board.getNumCheckers(i, j);
    		}
    	}
    	int initialHomeBoardCount = getHomeBlockCount(currentPipLocations);
    	for (Move move : play) {
    		// Home board block
    		if(currentPipLocations[me.getId()][move.getToPip()] == 1
    				&& move.getToPip() < 8)
    		{
    			count += move.getToPip();
    			// Encourages checkers in 2nd quadrant to go for home blocks
    			if(move.getFromPip() < 13 && move.getFromPip() > 7)
    				count += 2;
    			// Strongly discourages checker that is already on home block to move to another home block
    			if(isHomeBlock(move.getFromPip()))
    				count -= 5;
    		}
    		// Anchors
    		else if(currentPipLocations[me.getId()][move.getToPip()] == 1
    				&& move.getToPip() > 18)
    			count += Board.NUM_PIPS-(move.getToPip()+1);
    		
    		currentPipLocations[me.getId()][move.getFromPip()]--;
            currentPipLocations[me.getId()][move.getToPip()]++;
            System.out.println(move.getFromPip());
    		}
    	int afterHomeBoardCount = getHomeBlockCount(currentPipLocations);
    	// Encourages bot to increase the number of home blocks it has
    	if(afterHomeBoardCount-initialHomeBoardCount > 0)
    		count += 2;
    	//System.out.println("count is " + count);
    	return count;
    	}
    
    public int getHomeBlockCount(int[][] currentPipLocations)
    {
    	int homeBlockCount=0;
    	for(int i=0; i<8; i++)
    	{
    		if(currentPipLocations[me.getId()][i] > 1)
    			homeBlockCount++;
    	}
    	return homeBlockCount;
    }
    
    public boolean isHomeBlock(int pip)
    {
    	if(board.getNumCheckers(me.getId(), pip) > 1)
    		return true;
    	else
    		return false;
    }

    public String getDoubleDecision() {
        // Add your code here
        return "n";
    }
    
    /*Return pip count difference betweeen opponent and bot.
      The higher the value, the better the move */
    public int countDiff(Play play) {
    	int [][] currentPipLocations = new int [2][26];
    	for(int[] row:currentPipLocations)
    	{
    		Arrays.fill(row, 0);
    	}
    	int p0 = 0, p1 = 0;
    	int score = 0, i = 0, j = 0;
    	for(i=0; i<2; i++)
    	{
    		for(j=0; j<26; j++)
    		{
    			currentPipLocations[i][j] = board.getNumCheckers(i, j);
    		}
    	}
    	
   		p0 = updatePValue(me.getId(), currentPipLocations, play);
		p1 = updatePValue(opponent.getId(), currentPipLocations, play);

		return p1-p0;
    }
    
    // Returns the new pip count for the player after a play
    private int updatePValue(int player, int[][] pipLocations, Play play)
    {	
    	for (Move move : play) {
    		pipLocations[me.getId()][move.getFromPip()]--;
            pipLocations[me.getId()][move.getToPip()]++;
            int opposingPlayerId = opponent.getId();
            if (move.getToPip()<Board.BAR && move.getToPip()>Board.BEAR_OFF &&
                    pipLocations[opposingPlayerId][25-move.getToPip()] == 1) {
                pipLocations[opposingPlayerId][25-move.getToPip()]--;
                pipLocations[opposingPlayerId][Board.BAR]++;
            }
    	}
    	int i=0;
    	int pValue=0;
    	for(i = 0; i < 26; i++) {	 
    		if(pipLocations[player][i] >= 1) {
    			pValue += i * pipLocations[player][i];  
    		}
    	}
    	return pValue;
    }
    
    //Check the difference in blocks and blots of player for play
    public int blockBlotDiff(Play play) {
    	int blocks = 0, blots = 0;
    	int [][] currentPipLocations = new int [2][26];
    	for(int[] row:currentPipLocations) {
    		Arrays.fill(row, 0);
    	}
    	
    	int i =0, j = 0;
    	for(i = 0; i < 2; i++) {
    		for(j = 0; j < 26; j++) {
    			currentPipLocations[i][j] = board.getNumCheckers(i, j);
    		}
    	}
    	
    	blocks = updateBlockValue(me.getId(), currentPipLocations, play);
    	
    	return blocks;
    }
    
    //Returns the new block/blot difference for player
    private int updateBlockValue(int player, int[][] pipLocations, Play play)
    {	
    	int blockValue=0, blotValue=0;
    	for (Move move : play) {
    		if(pipLocations[me.getId()][move.getToPip()] == 1 && pipLocations[me.getId()][move.getFromPip()] == 1)
    		{
    			blotValue--;
    			blockValue++;
    		}
    		else if(pipLocations[me.getId()][move.getToPip()] > 1 && pipLocations[me.getId()][move.getFromPip()] == 1)
    			blotValue--;
    		else if(pipLocations[me.getId()][move.getToPip()] == 0 && pipLocations[me.getId()][move.getFromPip()] == 2)
    			blotValue += 2;
    		else if(pipLocations[me.getId()][move.getToPip()] > 1 && pipLocations[me.getId()][move.getFromPip()] == 2)
    			blotValue++;
    		else if(pipLocations[me.getId()][move.getToPip()] == 0 && pipLocations[me.getId()][move.getFromPip()] > 2)
    			blotValue++;
    		else if(pipLocations[me.getId()][move.getToPip()] == 1 && pipLocations[me.getId()][move.getFromPip()] > 2)
    			blockValue++;
    		pipLocations[me.getId()][move.getFromPip()]--;
            pipLocations[me.getId()][move.getToPip()]++;
            int opposingPlayerId = opponent.getId();
            if (move.getToPip()<Board.BAR && move.getToPip()>Board.BEAR_OFF &&
                    pipLocations[opposingPlayerId][25-move.getToPip()] == 1) {
                pipLocations[opposingPlayerId][25-move.getToPip()]--;
                pipLocations[opposingPlayerId][Board.BAR]++;
            }
    	}

    	return blockValue-blotValue;
    }

}