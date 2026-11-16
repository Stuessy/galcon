package src;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/*
 * Starts a single player session: one human player (mouse controlled, see HumanPlayer) against the EnemyAI.
 * After each game, clicking "Restart" on the endscreen returns to the start screen.
 * The session score (human wins vs AI wins) is kept and accumulated across restarts.
 */
public class SinglePlayer
{
	private static final Color[] PLAYER_COLORS = {Color.BLUE, Color.ORANGE}; //Shown on the start screen
	private static final double SHIP_SPEED = 1.2; //Slightly slower so the human has time to react

	public static void main(String args[]) throws InterruptedException
	{
		List<String> scoreHistory = new ArrayList<String>();
		while (true)
		{
			StartScreen startScreen = new StartScreen(PLAYER_COLORS[0], PLAYER_COLORS[1]);
			Color humanColor = startScreen.waitForChoice(); //Blocks until a planet has been clicked
			Color aiColor = humanColor.equals(PLAYER_COLORS[0]) ? PLAYER_COLORS[1] : PLAYER_COLORS[0];
			Tournament tournament = new Tournament(new HumanPlayer(humanColor),
					new EnemyAI("Enemy AI", aiColor, 800, 1200), 1, SHIP_SPEED, scoreHistory); //AI moves every 0.8-1.2 s
			tournament.waitForRestart(); //Blocks until the human clicks "Restart" on the endscreen
			tournament.dispose();
		}
	}
}
