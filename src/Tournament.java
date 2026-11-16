package src;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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
	
	private final static Player[] AIs = {new EnemyAI("Enemy AI 1", Color.BLUE, 500, 500), new EnemyAI("Enemy AI 2", Color.ORANGE, 500, 500)};

	private final static int NUM_GAMES = 2; //The number of games in this tournament
	private static final Font RANKING_FONT = new Font("Monospaced", Font.BOLD, 32);
	private static final Font BUTTON_FONT = new Font("Monospaced", Font.BOLD, 22);

	public static final int RESTART_BTN_X = 40;
	public static final int RESTART_BTN_Y = 330;
	public static final int RESTART_BTN_WIDTH = 200;
	public static final int RESTART_BTN_HEIGHT = 48;

	private Player[] winners; //An array holding the list of winners of the current tournament
	private final List<String> scoreHistory; //Cumulative history of winner names across restarts
	private final CountDownLatch restartLatch = new CountDownLatch(1);
	private boolean restartHovered = false;

	private Game currentGame;
	private boolean completed;
	private Player player1; //The players of this tournament, needed for the window hooks
	private Player player2;
	private final double shipSpeed; //Flight speed of all ships in this tournament
	
	public static void main(String args[]) throws InterruptedException
	{
		if (AIs.length != 2)
		{
			System.err.println("Error! Two AIs are required for Galcon.");
			System.exit(1);
		}
		List<String> scoreHistory = new ArrayList<String>();
		while (true)
		{
			Player ai1 = new EnemyAI("Enemy AI 1", Color.BLUE, 500, 500);
			Player ai2 = new EnemyAI("Enemy AI 2", Color.ORANGE, 500, 500);
			Tournament tournament = new Tournament(ai1, ai2, NUM_GAMES, Fleet.SPEED, scoreHistory);
			tournament.waitForRestart();
			tournament.dispose();
		}
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
		this(player1, player2, numGames, shipSpeed, new ArrayList<String>());
	}
	//Plays numGames games and records all winners into the given cumulative score history
	public Tournament(Player player1, Player player2, int numGames, double shipSpeed, List<String> scoreHistory)
	{
		this.player1 = player1;
		this.player2 = player2;
		this.shipSpeed = shipSpeed;
		this.scoreHistory = (scoreHistory != null) ? scoreHistory : new ArrayList<String>();
		
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
		attachEndscreenListener();
		
		winners = new Player[numGames];
		for (int i = 0; i < numGames; i++) //Play the games
		{
			currentGame = new Game(player1, player2, this);
			Player.currentGame = currentGame;
			Player winner = currentGame.play(); //Returns when a player has lost all planets
			winners[i] = winner;
			if (winner != null)
			{
				this.scoreHistory.add(winner.getPlayerName());
			}
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
		g.drawImage(bufferedImage, 0, 0, this); //Overlays are already drawn into bufferedImage by Game.draw
	}
	public void clear(Graphics g)
	{
		g.drawImage(Assets.STAR_BACKGROUND, 0, 0, WIN_WIDTH, WIN_HEIGHT, this);
	}
	//Listens to mouse movement and clicks on the ranking screen (for the restart button)
	private void attachEndscreenListener()
	{
		getContentPane().addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (completed && isOverRestartButton(e.getComponent(), e.getPoint()))
				{
					handleRestart();
				}
			}
		});
		getContentPane().addMouseMotionListener(new MouseMotionAdapter()
		{
			public void mouseMoved(MouseEvent e)
			{
				if (completed)
				{
					boolean over = isOverRestartButton(e.getComponent(), e.getPoint());
					if (over != restartHovered)
					{
						restartHovered = over;
						setCursor(Cursor.getPredefinedCursor(over ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
						drawRankings(bufferedGraphics);
						repaint();
					}
				}
			}
		});
	}

	private boolean isOverRestartButton(Component comp, Point pt)
	{
		Point p = SwingUtilities.convertPoint(comp, pt, this);
		int gx = p.x;
		int gy = p.y - TOP_BAR_HEIGHT;
		return gx >= RESTART_BTN_X && gx <= RESTART_BTN_X + RESTART_BTN_WIDTH
				&& gy >= RESTART_BTN_Y && gy <= RESTART_BTN_Y + RESTART_BTN_HEIGHT;
	}

	private void handleRestart()
	{
		restartLatch.countDown();
	}

	//Blocks until the human clicks "Restart" on the endscreen
	public void waitForRestart() throws InterruptedException
	{
		restartLatch.await();
	}

	public void dispose()
	{
		setCursor(Cursor.getDefaultCursor());
		super.dispose();
	}

	//Number of games won by the given player name across the entire session
	public int getNumWins(String playerName)
	{
		int wins = 0;
		for (String name : scoreHistory)
		{
			if (name.equals(playerName))
			{
				wins++;
			}
		}
		return wins;
	}

	//Number of games the player has won so far, based on the cumulative session history
	private int getNumWins(Player player)
	{
		return (player != null) ? getNumWins(player.getPlayerName()) : 0;
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

	//Show the result of the tournament and the restart button in the window
	private void drawRankings(Graphics g)
	{
		clear(g);
		g.setFont(RANKING_FONT);
		g.setColor(Color.WHITE);
		g.drawString(getResultMessage(), 40, 100);
		g.drawString(player1.getPlayerName() + ": " + getNumWins(player1) + " won", 40, 200);
		g.drawString(player2.getPlayerName() + ": " + getNumWins(player2) + " won", 40, 250);
		drawRestartButton(g);
	}

	private void drawRestartButton(Graphics g)
	{
		Color bg = restartHovered ? new Color(70, 130, 210) : new Color(40, 80, 150);
		g.setColor(bg);
		g.fillRoundRect(RESTART_BTN_X, RESTART_BTN_Y, RESTART_BTN_WIDTH, RESTART_BTN_HEIGHT, 12, 12);
		g.setColor(Color.WHITE);
		g.drawRoundRect(RESTART_BTN_X, RESTART_BTN_Y, RESTART_BTN_WIDTH, RESTART_BTN_HEIGHT, 12, 12);

		g.setFont(BUTTON_FONT);
		FontMetrics fm = g.getFontMetrics();
		String label = "Restart";
		int textX = RESTART_BTN_X + (RESTART_BTN_WIDTH - fm.stringWidth(label)) / 2;
		int textY = RESTART_BTN_Y + (RESTART_BTN_HEIGHT - fm.getHeight()) / 2 + fm.getAscent();
		g.setColor(Color.WHITE);
		g.drawString(label, textX, textY);
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