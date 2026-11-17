package src;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

/*
 * A player that is controlled by the mouse instead of an AI.
 * Drag from one of your planets onto another planet to send half of its ships there. The target may be own, neutral or enemy.
 * A click on one of your planets marks it. A double click on one of your planets marks all of your planets.
 * If a drag starts on a marked planet, all marked planets send to the target. The marks are cleared after each order.
 * While dragging, a valid target under the pointer is highlighted in green.
 * A click on a marked planet removes its mark. A click on empty space or on another player's planet removes all marks.
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
	private static final int DRAG_START_MARGIN = 6; //Pointer must leave the planet area by this much to start a drag
	private static final int TARGET_MARGIN = 10; //Target highlight rings lie 10 and 11 px outside the planet edge
	private static final Color TARGET_COLOR = Color.GREEN; //Distinct from white marks, the player colors and neutral gray

	private final List<Planet> selectedPlanets = new ArrayList<Planet>(); //Marked own planets, only accessed from the event dispatch thread
	private Planet pressedPlanet = null; //Planet under the mouse button at press time, null for empty space
	private int pressX, pressY; //Press position in game coordinates
	private int pressClickCount = 1; //Click count of the press, 2 or more is a double click candidate
	private boolean pressActive = false; //True between a left press and its release
	private boolean dragging = false; //True once the pointer has left the start area
	private int dragX, dragY; //Current pointer position in game coordinates
	private Planet lastClickPlanet = null; //Planet of the previous click, null for empty space, reset after a drag
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

	//Moves are issued by mouse gestures (see attachToWindow), so there is nothing to compute here
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

	//Listens to left button presses, drags and releases, and mouse movement on the window
	public void attachToWindow(final Tournament window)
	{
		MouseAdapter adapter = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					Point p = toGameCoordinates(e, window);
					handlePress(p.x, p.y, e.getClickCount());
				}
			}

			public void mouseReleased(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e) && pressActive)
				{
					Point p = toGameCoordinates(e, window);
					handleRelease(p.x, p.y);
				}
			}

			public void mouseMoved(MouseEvent e)
			{
				handleHover(e, window);
			}

			public void mouseDragged(MouseEvent e)
			{
				Point p = toGameCoordinates(e, window);
				handleDrag(p.x, p.y);
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

	//Converts a mouse event into game coordinates, the game area starts below the top bar
	private Point toGameCoordinates(MouseEvent e, Tournament window)
	{
		Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), window);
		return new Point(p.x, p.y - Tournament.TOP_BAR_HEIGHT);
	}

	//Draws the marks, the drag line and the target highlight, and the stats of an own planet under the mouse
	public void drawOverlay(Graphics g)
	{
		if (!isGameRunning())
		{
			clearGestureState(); //Game over: marks and gestures are dropped
		}
		if (isGameRunning())
		{
			synchronized (currentGame.getAllPlanets()) //The planets are read while they are drawn
			{
				for (Planet p: selectedPlanets)
				{
					if (p.getOwner() == this) //Gone after a conquest
					{
						drawMarkRing(g, p);
					}
				}
				drawDragFeedback(g);
			}
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

	//White ring around a marked own planet
	private void drawMarkRing(Graphics g, Planet p)
	{
		int r = p.getRadius() + SELECTION_MARGIN;
		g.setColor(Color.WHITE);
		g.drawOval(p.getX() - r, p.getY() - r, 2 * r, 2 * r);
	}

	//One line from the center of each sending planet to the pointer, and a highlight around a valid target under the pointer
	private void drawDragFeedback(Graphics g)
	{
		if (!dragging || pressedPlanet == null || pressedPlanet.getOwner() != this)
		{
			return;
		}
		Planet under = findPlanetAt(dragX, dragY); //Used for the line and the highlight alike
		List<Planet> origins = getDragOrigins(pressedPlanet, under);
		if (origins.isEmpty())
		{
			return;
		}
		if (!selectedPlanets.contains(pressedPlanet)) //The start planet shows a white ring during the drag, even if unmarked
		{
			drawMarkRing(g, pressedPlanet);
		}
		g.setColor(Color.WHITE);
		for (Planet p: origins) //One line per sending planet, from its center to the pointer
		{
			g.drawLine(p.getX(), p.getY(), dragX, dragY);
		}
		if (under != null && under != pressedPlanet) //Valid target: every planet except the start planet
		{
			g.setColor(TARGET_COLOR);
			for (int margin = TARGET_MARGIN; margin <= TARGET_MARGIN + 1; margin++) //Two rings, 2 px wide
			{
				int r = under.getRadius() + margin;
				g.drawOval(under.getX() - r, under.getY() - r, 2 * r, 2 * r);
			}
		}
	}

	//Updates hovered planet on mouse move/drag
	private void handleHover(MouseEvent e, Tournament window)
	{
		Point p = toGameCoordinates(e, window);
		int gx = p.x;
		int gy = p.y;
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

	//Left button pressed: remember the start planet and the click count, a drag starts only when the start area is left
	private void handlePress(int x, int y, int clickCount)
	{
		pressActive = true;
		dragging = false;
		pressX = x;
		pressY = y;
		dragX = x;
		dragY = y;
		pressClickCount = clickCount;
		pressedPlanet = isGameRunning() ? findPlanetAt(x, y) : null;
	}

	//Pointer moved with the button held: a drag starts once the pointer leaves the start area
	private void handleDrag(int x, int y)
	{
		if (!pressActive)
		{
			return;
		}
		dragX = x;
		dragY = y;
		if (!isInStartArea(x, y))
		{
			dragging = true;
		}
	}

	//Button released: a click marks, clears or selects all, a drag onto another planet sends the fleets
	private void handleRelease(int x, int y)
	{
		boolean isDrag = dragging || !isInStartArea(x, y); //Checked again, the last drag event may have been missing
		Planet start = pressedPlanet;
		pressActive = false;
		dragging = false;
		pressedPlanet = null;
		if (!isGameRunning())
		{
			clearGestureState();
			return;
		}
		synchronized (currentGame.getAllPlanets()) //No AI sends or production while the gesture is evaluated
		{
			removeLostPlanets();
			Planet released = findPlanetAt(x, y);
			if (!isDrag)
			{
				//Double click: second click on the same planet, the click count comes from the press event
				boolean doubleClick = pressClickCount >= 2 && start != null && start == lastClickPlanet;
				handleClickOn(start, doubleClick);
				lastClickPlanet = start;
			}
			else
			{
				lastClickPlanet = null; //A drag is no click, so it cannot start a double click
				if (start != null && start.getOwner() == this && released != null && released != start)
				{
					sendFromMarked(start, released);
				}
				//Otherwise: drop into empty space, or back onto the start planet, nothing happens (O4, O10)
			}
		}
	}

	//A click toggles the mark of an own planet, a double click marks all own planets (O7)
	//A click or double click on another player's planet or on empty space clears all marks (O12)
	private void handleClickOn(Planet start, boolean doubleClick)
	{
		if (start == null || start.getOwner() != this)
		{
			selectedPlanets.clear();
		}
		else if (doubleClick)
		{
			selectAllOwnPlanets();
		}
		else if (selectedPlanets.contains(start))
		{
			selectedPlanets.remove(start);
		}
		else
		{
			selectedPlanets.add(start);
		}
	}

	//Double click on an own planet: every own planet gets marked, planets that are already marked stay marked
	//Called under the planets lock
	private void selectAllOwnPlanets()
	{
		for (Planet p: currentGame.getAllPlanets())
		{
			if (p.getOwner() == this && !selectedPlanets.contains(p))
			{
				selectedPlanets.add(p);
			}
		}
	}

	//Every origin sends half of its ships to the target, then all marks are cleared
	//Called under the planets lock
	private void sendFromMarked(Planet start, Planet target)
	{
		List<Planet> origins = getDragOrigins(start, target);
		if (!origins.isEmpty())
		{
			for (Planet origin: origins)
			{
				sendHalfOfShips(origin, target);
			}
			selectedPlanets.clear();
		}
	}

	//Sends half of the ships of the origin, called under the planets lock
	private void sendHalfOfShips(Planet origin, Planet target)
	{
		int units = (int) (origin.getNumUnits() * SHIP_SHARE);
		if (units > 0)
		{
			origin.sendFleet(units, target);
		}
	}

	//Origins of a drag: all marked own planets plus the start planet, without the target (O3, O8)
	//Planets that are no longer owned by this player are skipped
	private List<Planet> getDragOrigins(Planet start, Planet exclude)
	{
		List<Planet> origins = new ArrayList<Planet>();
		if (start == null || start.getOwner() != this)
		{
			return origins;
		}
		for (Planet p: selectedPlanets)
		{
			if (p.getOwner() == this)
			{
				origins.add(p);
			}
		}
		if (!origins.contains(start))
		{
			origins.add(start); //The start planet joins the marks (O3)
		}
		origins.remove(exclude); //Removes nothing if exclude is null
		return origins;
	}

	//Removes planets that were conquered by another player since they were marked
	private void removeLostPlanets()
	{
		Iterator<Planet> it = selectedPlanets.iterator();
		while (it.hasNext())
		{
			if (it.next().getOwner() != this)
			{
				it.remove();
			}
		}
	}

	//True while the pointer is inside the start area: the start planet plus DRAG_START_MARGIN, or the press point
	private boolean isInStartArea(int x, int y)
	{
		int cx = (pressedPlanet != null) ? pressedPlanet.getX() : pressX;
		int cy = (pressedPlanet != null) ? pressedPlanet.getY() : pressY;
		int r = ((pressedPlanet != null) ? pressedPlanet.getRadius() : 0) + DRAG_START_MARGIN;
		int dx = cx - x;
		int dy = cy - y;
		return dx * dx + dy * dy <= r * r;
	}

	//Drops marks and gesture state, used when the game is over
	private void clearGestureState()
	{
		selectedPlanets.clear();
		lastClickPlanet = null;
		pressedPlanet = null;
		pressActive = false;
		dragging = false;
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
