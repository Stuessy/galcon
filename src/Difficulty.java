package src;

/*
 * Difficulty levels of the single player game. The level sets the start map (number of AI planets),
 * the reaction time of the EnemyAI (pause before each move) and whether the ship count of enemy planets is shown.
 */
public enum Difficulty
{
	EASY("Easy", 1, 1500, 2000, true),
	NORMAL("Normal", 1, 800, 1200, false),
	HARD("Hard", 3, 600, 900, false),
	CRAZY("Crazy", 6, 400, 600, false);

	public static final Difficulty DEFAULT = NORMAL; //Preselected on the start screen

	private final String label; //Shown on the start screen
	private final int aiPlanets; //AI planets including the AI home planet
	private final int minPause; //Pause before each AI move in ms, randomized between min and max
	private final int maxPause;
	private final boolean enemyUnitsVisible; //Ship count of enemy planets is shown to the human player

	Difficulty(String label, int aiPlanets, int minPause, int maxPause, boolean enemyUnitsVisible)
	{
		this.label = label;
		this.aiPlanets = aiPlanets;
		this.minPause = minPause;
		this.maxPause = maxPause;
		this.enemyUnitsVisible = enemyUnitsVisible;
	}

	public int getAiPlanets()
	{
		return aiPlanets;
	}

	public int getMinPause()
	{
		return minPause;
	}

	public int getMaxPause()
	{
		return maxPause;
	}

	public boolean isEnemyUnitsVisible()
	{
		return enemyUnitsVisible;
	}

	//Neutral planets fill up the rest, the human home planet takes one slot
	public int getNeutralPlanets(int totalPlanets)
	{
		return totalPlanets - 1 - aiPlanets;
	}

	public String toString()
	{
		return label;
	}
}
