package src;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JFrame;

/**
 * Small fixes @sredmond
 * Changes fleet data type to hashset from linkedlist
 * Fixed arithmetic errors (math.sqrt)
 * Sped up int to string conversion
 * Various inefficiencies
 * Fixes game-breaking cheats (send negative numbers to sendFleet)
 * 
 * NOTE: A tournament can only ever have two players, as of now.
 */

public class Tournament extends JFrame
{
	//Graphical Setup
	public static final int TOP_BAR_HEIGHT = 22;
	public static final int WIN_WIDTH = 960;//Toolkit.getDefaultToolkit().getScreenSize().width;
	public static final int WIN_HEIGHT = 540;//Toolkit.getDefaultToolkit().getScreenSize().height - TOP_BAR_HEIGHT;
	
	//Double buffering
	private static Image bufferedImage = null;
	private static Graphics bufferedGraphics = null;
	
	private final static Player[] AIs = {new SamplePlayer(), new SamplePlayer()};
	private final static int NUM_GAMES = 2; //The number of games in this tournament
	private static final Font RANKING_FONT = new Font("Monospaced", Font.BOLD, 32);
	
	private Player[] winners; //An array holding the list of winners
	private Game currentGame;
	private boolean completed;
	private Player player1; //The players of this tournament, needed for the window hooks
	private Player player2;
	private final double shipSpeed; //Flight speed of all ships in this tournament
	
	public static void main(String args[])
	{
		if (AIs.length != 2)
		{
			System.err.println("Error! Two AIs are required for Galcon.");
			System.exit(1);
		}
		new Tournament(AIs[0], AIs[1]);
	}
	public Tournament(Player player1, Player player2)
	{
		this(player1, player2, NUM_GAMES);
	}
	//Plays numGames games between the two players in one window, with the default ship speed
	public Tournament(Player player1, Player player2, int numGames)
	{
		this(player1, player2, numGames, Fleet.SPEED);
	}
	//Plays numGames games between the two players in one window, all ships fly with shipSpeed
	public Tournament(Player player1, Player player2, int numGames, double shipSpeed)
	{
		this.player1 = player1;
		this.player2 = player2;
		this.shipSpeed = shipSpeed;
		
		//Set up window
		setName("Galcon AI Challenge");
		setTitle("Galcon");
		setSize(WIN_WIDTH, WIN_HEIGHT + TOP_BAR_HEIGHT);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBackground(Color.GRAY);
		setResizable(false);
		setVisible(true);
		
		bufferedImage = createImage(WIN_WIDTH, WIN_HEIGHT);
		bufferedGraphics = bufferedImage.getGraphics();
		
		player1.attachToWindow(this);
		player2.attachToWindow(this);
		
		winners = new Player[numGames];
		for (int i = 0; i < numGames; i++) //Play the games
		{
			currentGame = new Game(player1, player2, this);
			Player.currentGame = currentGame;
			Player winner = currentGame.play(); //Returns when a player has lost all planets
			winners[i] = winner;
			System.out.println("Done with a game, winner is " + winner.getPlayerName());	
		}
		completed = true;
		printRanks();
		drawRankings(bufferedGraphics);
		repaint();
	}
	public void paint(Graphics g)
	{
		g.translate(0, TOP_BAR_HEIGHT);
		g.drawImage(bufferedImage, 0, 0, this);
		player1.drawOverlay(g);
		player2.drawOverlay(g);
	}
	public void clear(Graphics g)
	{
		g.drawImage(Assets.STAR_BACKGROUND, 0, 0, WIN_WIDTH, WIN_HEIGHT, this);
	}
	//Number of games the player has won so far, based on the recorded winners
	private int getNumWins(Player player)
	{
		int wins = 0;
		for (Player winner: winners)
		{
			if (winner == player)
			{
				wins++;
			}
		}
		return wins;
	}

	//Text announcing the overall winner, or a draw
	private String getResultMessage()
	{
		int wins1 = getNumWins(player1);
		int wins2 = getNumWins(player2);
		if (wins1 == wins2)
		{
			return "Draw!";
		}
		return ((wins1 > wins2) ? player1 : player2).getPlayerName() + " wins!";
	}

	//Print the ranks in order and the result of the tournament
	public void printRanks()
	{
		for (Player p: new Player[] {player1, player2})
		{
			System.out.println(p.getPlayerName() + ": \t" + getNumWins(p));
		}
		System.out.println(getResultMessage());
	}

	//Show the result of the tournament in the window
	private void drawRankings(Graphics g)
	{
		clear(g);
		g.setFont(RANKING_FONT);
		g.setColor(Color.WHITE);
		g.drawString(getResultMessage(), 40, 100);
		g.drawString(player1.getPlayerName() + ": " + getNumWins(player1) + " won", 40, 200);
		g.drawString(player2.getPlayerName() + ": " + getNumWins(player2) + " won", 40, 250);
	}
	public double getShipSpeed()
	{
		return shipSpeed;
	}
	public boolean isCompleted()
	{
		return completed;
	}
	public Graphics getBufferedGraphics()
	{
		return bufferedGraphics;
	}

	//Generate a value of x between rad and WIN_WIDTH - rad
	public static int genX(int rad)
	{
		return (int) (Math.random() * (WIN_WIDTH - rad * 2) + rad);
	}
	//Generate a value of y between rad and WIN_HEIGHT - rad
	public static int genY(int rad)
	{
		return (int) (Math.random() * (WIN_HEIGHT - rad * 2) + rad);
	}
	//Get a player's color, or return DARK_GRAY if player is null
	public static Color getPlayerColor(Player player)
	{
		return (player != null) ? player.getColor() : Color.DARK_GRAY;
	}
}