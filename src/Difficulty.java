package src;

/*
 * Difficulty levels of the single player game. The level sets the start map (number of AI planets)
 * and the reaction time of the EnemyAI (pause before each move).
 */
public enum Difficulty
{
	EASY("Easy", 1, 1500, 2000),
	NORMAL("Normal", 1, 800, 1200),
	HARD("Hard", 3, 600, 900),
	CRAZY("Crazy", 6, 400, 600);

	public static final Difficulty DEFAULT = NORMAL; //Preselected on the start screen

	private final String label; //Shown on the start screen
	private final int aiPlanets; //AI planets including the AI home planet
	private final int minPause; //Pause before each AI move in ms, randomized between min and max
	private final int maxPause;

	Difficulty(String label, int aiPlanets, int minPause, int maxPause)
	{
		this.label = label;
		this.aiPlanets = aiPlanets;
		this.minPause = minPause;
		this.maxPause = maxPause;
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
