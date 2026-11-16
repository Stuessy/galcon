package src;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/*
 * Starts a single player session: one human player (mouse controlled, see HumanPlayer) against the EnemyAI.
 * The difficulty (Easy, Normal, Hard, Crazy) is selected on the start screen. After each game, clicking "Restart"
 * on the endscreen returns to the start screen, with the last difficulty preselected.
 * The session score (human wins vs AI wins) is kept and accumulated across restarts.
 */
public class SinglePlayer
{
	private static final Color[] PLAYER_COLORS = {Color.BLUE, Color.ORANGE}; //Shown on the start screen
	/**
	 * Flight speed of all ships in a single player game, in pixels per tick (1 tick = 10 ms, see Game.FRAME_TIME).
	 * Higher values are faster: 1.2 is about 120 pixels per second. Each ship varies by about +-15 % (see Fleet.Ship).
	 * Same for all difficulties. Passed to the Tournament, which hands it to every game.
	 */
	private static final double SHIP_SPEED = 0.9; //Slightly slower so the human has time to react

	public static void main(String args[]) throws InterruptedException
	{
		List<String> scoreHistory = new ArrayList<String>();
		Difficulty difficulty = Difficulty.DEFAULT; //Kept across restarts, so the last choice is preselected
		while (true)
		{
			StartScreen startScreen = new StartScreen(PLAYER_COLORS[0], PLAYER_COLORS[1], difficulty);
			Color humanColor = startScreen.waitForChoice(); //Blocks until a planet has been clicked
			difficulty = startScreen.getDifficulty();
			Color aiColor = humanColor.equals(PLAYER_COLORS[0]) ? PLAYER_COLORS[1] : PLAYER_COLORS[0];
			//Reaction time of the AI depends on the difficulty
			EnemyAI enemy = new EnemyAI("Enemy AI", aiColor, difficulty.getMinPause(), difficulty.getMaxPause());
			Tournament tournament = new Tournament(new HumanPlayer(humanColor), enemy, 1, SHIP_SPEED, scoreHistory,
					difficulty);
			tournament.waitForRestart(); //Blocks until the human clicks "Restart" on the endscreen
			tournament.dispose();
		}
	}
}
