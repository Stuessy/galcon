package src;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.Collections;
import javax.swing.*;
import javax.swing.Timer;

public class Game extends JFrame implements ActionListener
{
	public static final int FRAME_TIME = 10;

	private Player p1 = null;
	private Player p2 = null;
	private volatile Player winner = null; //Set on the event dispatch thread, read by play()

	private Set<Fleet> fleets = null;
	private Set<Planet> planets = null;

	private Tournament parent = null; //Each game could potentially be part of a different tournament
	private final double shipSpeed; //Flight speed of the ships in this game, given by the tournament

	private static final int NUM_PLANETS = 16;

	private static Timer clock = null;

	public Game(Player player1, Player player2, Tournament parent)
	{	
		p1 = player1;
		p2 = player2;
		this.parent = parent;
		shipSpeed = parent.getShipSpeed();

		winner = null;
		fleets = Collections.synchronizedSet(new HashSet<Fleet>());
		planets = Collections.synchronizedSet(new HashSet<Planet>());
		generateNewMap(p1, p2);
	}

	public Player play()
	{
		clock = new Timer(FRAME_TIME, this);
		clock.start();

		if (!p1.isAlive())
		{
			p1.start();
		}
		if (!p2.isAlive())
		{
			p2.start();
		}
		while (winner == null) //Wait until the game is over
		{
			try
			{
				Thread.sleep(FRAME_TIME);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return winner;
	}

	public void actionPerformed(ActionEvent e) 
	{
		updateAllFleets();
		updateAllPlanets();
		draw(parent.getBufferedGraphics());
		winner = getWinner(); //Is there a winner now? Set after drawing, so play() returns with a complete frame
		if (winner != null)
		{
			clock.stop();
		}
	}
	public void draw(Graphics g)
	{
		parent.clear(g);
		drawAllPlanets(g);
		drawAllFleets(g);
		parent.repaint(); //Clear the screen
		//		drawCurrentPlayerInfo(g);
	}


	//	private void drawCurrentPlayerInfo(Graphics g)
	//	{
	//		final int FONT_HEIGHT = 40;
	//		Font font = new Font("Monospaced", Font.PLAIN, FONT_HEIGHT);
	//		g.setFont(font);    
	//		g.setColor(p1.getColor());
	//		g.drawString(p1.getPlayerName(), 10, Tournament.TOP_BAR_HEIGHT + FONT_HEIGHT);
	//		g.setColor(p2.getColor());
	//		g.drawString(p2.getPlayerName(), Tournament.WIN_WIDTH / 2, Tournament.TOP_BAR_HEIGHT + FONT_HEIGHT);
	//	}

	//Draw all planets
	public void drawAllPlanets(Graphics g)
	{
		g.setFont(Planet.TEXT_FONT);
		synchronized(planets)
		{	
			//Experimental: looping over planets separately to minimize setColor calls - to optimize
			g.setColor(p1.getColor());
			for(Planet p: planets)
			{
				if (p1.equals(p.getOwner()))
					p.draw(g, null, invertColor(p1.getColor()));
			}
			g.setColor(p2.getColor());
			for(Planet p: planets)
			{
				if (p2.equals(p.getOwner()))
					p.draw(g, null, invertColor(p2.getColor()));
			}
			g.setColor(Color.LIGHT_GRAY);
			for(Planet p: planets)
			{
				if (p.getOwner() == null)
					p.draw(g, null, invertColor(Color.LIGHT_GRAY));
			}
		}
	}

	public void drawAllFleets(Graphics g)
	{
		synchronized(fleets)
		{
			for(Fleet f: fleets)
			{
				Color c = Tournament.getPlayerColor(f.getOwner());
				f.draw(g, c, invertColor(c));
			}
		}
	}

	//Checks if a planet-to-be (specified by (x,y) coordinates and radius) is overlapping any existing planet.
	public boolean isPlanetOverlapping(int x, int y, int radius)
	{
		synchronized(planets)
		{
			for(Planet p: planets)
			{
				int dx = p.getX() - x;
				int dy = p.getY() - y;
				int minSep = p.getRadius() + radius + 10; //Must be at least ten units apart

				if(dx * dx + dy * dy <= minSep * minSep) 
				{
					return true;
				}
			}
			return false;
		}
	}

	private void generateNewMap(Player p1, Player p2)
	{	
		synchronized(planets)
		{
			planets.add(new Planet(p1, this)); //Give player 1 a starting planet
			planets.add(new Planet(p2, this)); //Give player 2 a starting planet

			for (int i = 0 ; i < NUM_PLANETS - 2; i++) //Add the rest of the planets as neutral
			{
				planets.add(new Planet(this));
			}
		}
	}
	public void addFleet(Fleet f)
	{
		synchronized(fleets)
		{
			fleets.add(f);			
		}
	}
	public void clearFleets()
	{
		synchronized(fleets)
		{
			fleets.clear();
		}
	}
	public void clearPlanets()
	{
		synchronized(planets)
		{
			planets.clear();
		}
	}
	public void updateAllFleets()
	{
		Set<Fleet> toBeRemoved = new HashSet<Fleet>();
		synchronized(fleets)
		{
			for(Fleet f: fleets)
			{
				if (f.update()) //Did we hit the planet?
				{
					toBeRemoved.add(f); //Mark it for removal
				}
			}
			for (Fleet f: toBeRemoved) //Remove marked fleets
			{
				fleets.remove(f);
			}
		}
	}
	public void updateAllPlanets()
	{
		synchronized(planets)
		{
			for(Planet p: planets)
			{
				p.update();
			}
		}
	}
	public double getShipSpeed()
	{
		return shipSpeed;
	}
	public Set<Fleet> getAllFleets()
	{
		synchronized(fleets)
		{
			return fleets;
		}
	}
	public Set<Planet> getAllPlanets()
	{
		synchronized(planets)
		{
			return planets;
		}
	}
	private static Color invertColor(Color c)
	{
		return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
	}
	//Returns the winning player, or null if the game is not over. You lose if you have no planets left.
	public Player getWinner()
	{
		boolean p1HasPlanets = false;
		boolean p2HasPlanets = false;
		synchronized(planets)
		{
			for (Planet p: planets)
			{
				if (p1.equals(p.getOwner()))
				{
					p1HasPlanets = true;
				}
				if (p2.equals(p.getOwner()))
				{
					p2HasPlanets = true;
				}
			}
		}
		if (p1HasPlanets && !p2HasPlanets)
		{
			return p1;
		}
		if (p2HasPlanets && !p1HasPlanets)
		{
			return p2;
		}
		return null;
	}
}
