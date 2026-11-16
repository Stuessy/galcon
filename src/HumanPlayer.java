package src;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.SwingUtilities;

/*
 * A player that is controlled by the mouse instead of an AI.
 * Left-click an own planet to select it, then left-click another planet to send half of its ships there.
 * This works for own, neutral and enemy planets. Clicking the selected planet or empty space cancels the selection.
 * Moving the mouse over an own planet shows its production, morale and ships (as in the Legacy game).
 */
public class HumanPlayer extends Player
{
	final String NAME = "Human Player";
	final Color COLOR; //Chosen on the start screen
	public static final double SHIP_SHARE = 0.5; //Share of the ships of the origin planet that are sent
	private static final int SELECTION_MARGIN = 6; //Distance between planet border and selection ring
	private static final int STATS_GAP = 8; //Distance between planet border and the stats text
	private static final int HOVER_MARGIN = 4; //Generous margin around planet border so hover triggers easily
	private static final long HOVER_LINGER_MS = 1000; //Keep stats visible for 1s when mouse leaves, prevents flickering
	private static final Font STATS_FONT = new Font("Monospaced", Font.PLAIN, 12);

	private Planet selectedPlanet = null; //Only accessed from the event dispatch thread
	private Planet hoveredPlanet = null; //Own planet under the mouse, only accessed from the event dispatch thread
	private long lastHoverTime = 0; //Timestamp of the last hover over the planet or its stats text

	public HumanPlayer(Color color)
	{
		COLOR = color;
	}

	public Color getColor()
	{
		return COLOR;
	}

	public String getPlayerName()
	{
		return NAME;
	}

	//Moves are issued by mouse clicks (see attachToWindow), so there is nothing to compute here
	public void makeMove()
	{
		try
		{
			sleep(10); //Stall, so the thread does not spin
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}

	//Listens to left clicks and mouse movement on the window
	public void attachToWindow(final Tournament window)
	{
		MouseAdapter adapter = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					//Convert into window coordinates, the game area starts below the top bar
					Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), window);
					handleClick(p.x, p.y - Tournament.TOP_BAR_HEIGHT);
				}
			}

			public void mouseMoved(MouseEvent e)
			{
				handleHover(e, window);
			}

			public void mouseDragged(MouseEvent e)
			{
				handleHover(e, window);
			}

			public void mouseExited(MouseEvent e)
			{
				hoveredPlanet = null;
			}
		};
		window.getContentPane().addMouseListener(adapter);
		window.getContentPane().addMouseMotionListener(adapter);
	}

	//Draws a ring around the selected planet, and the stats of an own planet under the mouse
	public void drawOverlay(Graphics g)
	{
		if (selectedPlanet != null && isGameRunning())
		{
			int r = selectedPlanet.getRadius() + SELECTION_MARGIN;
			g.setColor(Color.WHITE);
			g.drawOval(selectedPlanet.getX() - r, selectedPlanet.getY() - r, 2 * r, 2 * r);
		}
		if (hoveredPlanet != null && isGameRunning() && hoveredPlanet.getOwner() == this) //Gone after a conquest
		{
			//Keep stats visible while mouse is over planet/text or within linger time
			if (System.currentTimeMillis() - lastHoverTime <= HOVER_LINGER_MS)
			{
				drawStats(g, hoveredPlanet);
			}
			else
			{
				hoveredPlanet = null;
			}
		}
	}

	//Updates hovered planet on mouse move/drag
	private void handleHover(MouseEvent e, Tournament window)
	{
		Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), window);
		int gx = p.x;
		int gy = p.y - Tournament.TOP_BAR_HEIGHT;
		Planet under = findPlanetAt(gx, gy, HOVER_MARGIN);
		if (under != null)
		{
			//Stats are only shown for own planets, enemy and neutral planets keep their numbers secret
			if (under.getOwner() == this)
			{
				hoveredPlanet = under;
				lastHoverTime = System.currentTimeMillis();
			}
			else
			{
				hoveredPlanet = null; //Hovering over enemy or neutral planet clears stats immediately
			}
		}
		else if (hoveredPlanet != null && isOverStatsText(gx, gy, hoveredPlanet))
		{
			//Mouse moved over the stats text itself: keep it visible while reading
			lastHoverTime = System.currentTimeMillis();
		}
	}

	//Checks if (gx, gy) is over the stats text next to the planet
	private boolean isOverStatsText(int gx, int gy, Planet p)
	{
		int textW = 160;
		int textH = 24;
		int textX = (p.getX() < Tournament.WIN_WIDTH / 2)
				? p.getX() + p.getRadius() + STATS_GAP - 4
				: p.getX() - p.getRadius() - STATS_GAP - textW - 4;
		int textY = p.getY() - textH / 2;
		return gx >= textX && gx <= textX + textW + 8 && gy >= textY && gy <= textY + textH;
	}

	//Text next to an own planet: production per second / morale / number of ships
	private void drawStats(Graphics g, Planet p)
	{
		int morale;
		int ships;
		synchronized (currentGame.getAllPlanets()) //The AI threads change the ships, so read them under the lock
		{
			morale = p.getMorale();
			ships = p.getNumUnits();
		}
		//Production in ships per second: morale share of the production, Game runs 100 ticks per second
		String text = String.format("%.1f / %d / %d", morale / (double) p.PRODUCTION_TIME, morale, ships);
		g.setFont(STATS_FONT);
		FontMetrics fm = g.getFontMetrics();
		//Right of the planet on the left half of the window, left of it on the right half (as in the Legacy game)
		int textX = (p.getX() < Tournament.WIN_WIDTH / 2)
				? p.getX() + p.getRadius() + STATS_GAP
				: p.getX() - p.getRadius() - STATS_GAP - fm.stringWidth(text);
		int textY = p.getY() + (fm.getAscent() - fm.getDescent()) / 2;
		g.setColor(Color.WHITE);
		g.drawString(text, textX, textY);
	}

	//Handles a click at the given game coordinates
	private void handleClick(int x, int y)
	{
		if (!isGameRunning())
		{
			selectedPlanet = null;
			return;
		}
		Game game = currentGame;
		synchronized (game.getAllPlanets()) //No AI sends or production while the click is handled
		{
			Planet clicked = findPlanetAt(x, y);
			if (selectedPlanet != null && selectedPlanet.getOwner() != this)
			{
				selectedPlanet = null; //The selected planet was lost in the meantime
			}

			if (clicked == null || clicked == selectedPlanet)
			{
				selectedPlanet = null; //Empty space or the selected planet: cancel the selection
			}
			else if (selectedPlanet != null)
			{
				sendHalfOfShips(selectedPlanet, clicked); //Any other planet (own, neutral or enemy): send the fleet there
				selectedPlanet = null;
			}
			else if (clicked.getOwner() == this)
			{
				selectedPlanet = clicked; //Own planet without selection: select it
			}
		}
	}

	private void sendHalfOfShips(Planet origin, Planet target)
	{
		PlanetInfo originInfo = origin.getPlanetInfo();
		int units = (int) (originInfo.getNumUnits() * SHIP_SHARE);
		if (units > 0)
		{
			sendFleet(originInfo, units, target.getPlanetInfo());
		}
	}

	//True while there is a game and no player has lost all planets yet
	private boolean isGameRunning()
	{
		return currentGame != null && currentGame.getWinner() == null;
	}

	//Returns the planet whose area contains the given game coordinates, or null
	private Planet findPlanetAt(int x, int y)
	{
		return findPlanetAt(x, y, 0);
	}

	//Returns the planet within radius + margin of the given game coordinates, or null
	private Planet findPlanetAt(int x, int y, int margin)
	{
		Game game = currentGame;
		if (game == null)
		{
			return null;
		}
		synchronized (game.getAllPlanets())
		{
			for (Planet p: game.getAllPlanets())
			{
				int dx = p.getX() - x;
				int dy = p.getY() - y;
				int r = p.getRadius() + margin;
				if (dx * dx + dy * dy <= r * r)
				{
					return p;
				}
			}
		}
		return null;
	}
}
