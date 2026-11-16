package src;

import java.awt.Color;

/*
 * Starts a single game: one human player (mouse controlled, see HumanPlayer) against the SamplePlayer AI.
 */
public class SinglePlayer
{
	private static final Color[] PLAYER_COLORS = {Color.BLUE, Color.ORANGE}; //Shown on the start screen
	private static final double SHIP_SPEED = 1.5; //Slower than the AI tournament (Fleet.SPEED), so the human can follow

	public static void main(String args[]) throws InterruptedException
	{
		StartScreen startScreen = new StartScreen(PLAYER_COLORS[0], PLAYER_COLORS[1]);
		Color humanColor = startScreen.waitForChoice(); //Blocks until a planet has been clicked
		Color aiColor = humanColor.equals(PLAYER_COLORS[0]) ? PLAYER_COLORS[1] : PLAYER_COLORS[0];
		new Tournament(new HumanPlayer(humanColor), new SamplePlayer(aiColor, 800, 1500), 1, SHIP_SPEED); //One game, the AI moves every 0.8-1.5 s
	}
}
