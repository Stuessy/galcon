package src;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

/*
 * A player that is controlled by the mouse instead of an AI.
 * Left-click an own planet to select it, then left-click another planet to send half of its ships there.
 * Clicking another own planet switches the selection. Clicking the selected planet or empty space cancels it.
 */
public class HumanPlayer extends Player
{
	final String NAME = "Human Player";
	final Color COLOR; //Chosen on the start screen
	public static final double SHIP_SHARE = 0.5; //Share of the ships of the origin planet that are sent
	private static final int SELECTION_MARGIN = 6; //Distance between planet border and selection ring

	private Planet selectedPlanet = null; //Only accessed from the event dispatch thread

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

	//Listens to left clicks on the window
	public void attachToWindow(final Tournament window)
	{
		window.getContentPane().addMouseListener(new MouseAdapter()
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
		});
	}

	//Draws a ring around the selected planet
	public void drawOverlay(Graphics g)
	{
		if (selectedPlanet != null && isGameRunning())
		{
			int r = selectedPlanet.getRadius() + SELECTION_MARGIN;
			g.setColor(Color.WHITE);
			g.drawOval(selectedPlanet.getX() - r, selectedPlanet.getY() - r, 2 * r, 2 * r);
		}
	}

	//Handles a click at the given game coordinates
	private void handleClick(int x, int y)
	{
		if (!isGameRunning())
		{
			selectedPlanet = null;
			return;
		}
		Planet clicked = findPlanetAt(x, y);
		if (selectedPlanet != null && selectedPlanet.getOwner() != this)
		{
			selectedPlanet = null; //The selected planet was lost in the meantime
		}

		if (clicked == null || clicked == selectedPlanet)
		{
			selectedPlanet = null; //Empty space or the selected planet: cancel the selection
		}
		else if (clicked.getOwner() == this)
		{
			selectedPlanet = clicked; //Own planet: select it
		}
		else if (selectedPlanet != null)
		{
			sendHalfOfShips(selectedPlanet, clicked); //Neutral or enemy planet: attack it
			selectedPlanet = null;
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
		if (currentGame == null)
		{
			return null;
		}
		for (Planet p: currentGame.getAllPlanets())
		{
			int dx = p.getX() - x;
			int dy = p.getY() - y;
			int r = p.getRadius();
			if (dx * dx + dy * dy <= r * r)
			{
				return p;
			}
		}
		return null;
	}
}
