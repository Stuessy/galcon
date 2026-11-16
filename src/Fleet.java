//10/6/11 1:27pm
package src;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Note: A fleet is a swarm of ships. Each ship is drawn as a tiny triangle and invades the target on its own arrival.
public class Fleet
{
	public static final double SPEED = 2; //Good for quick and dirty scaling
	public static final int LAUNCH_INTERVAL = 2; //Ticks between two ships leaving the planet
	private static final double SHIP_SIZE = 6; //Length of a ship triangle in pixels
	private static final double MAX_SPREAD = 12; //Maximum sideways distance of a ship from the swarm's center line
	private double x, y; //Coordinates of the swarm (center of the flying ships, or the launch point)
	private int numUnits; //How many ships are still alive in that fleet
	private Player owner; //The owner of the fleet
	private Planet target; //The planet the fleet is headed to.
	private final double launchX, launchY; //Where the ships leave their planet
	private final List<Ship> ships = new ArrayList<Ship>();
	private int age = 0; //Ticks since the fleet was sent
	private final double speed; //Base flight speed of the ships, set per game

	//Construct a new fleet. The ships leave the planet one after another, starting at (xIn, yIn)
	//Precondition: destination is not null
	public Fleet(double xIn, double yIn, int units, Player ownedBy, Planet destination, double speedIn)
	{
		x = xIn;
		y = yIn;
		launchX = xIn;
		launchY = yIn;
		owner = ownedBy;
		target = destination;
		speed = speedIn;
		double spread = Math.min(3 + Math.sqrt(units) * 1.2, MAX_SPREAD); //The swarm gets wider with more ships
		for (int i = 0; i < units; i++)
		{
			ships.add(new Ship(xIn, yIn, i * LAUNCH_INTERVAL, spread, speed));
		}
		numUnits = units;
	}

	//Redirect the fleet to a new destination
	public void setDestination(Planet newDest)
	{
		target = newDest;
	}

	public Player getOwner()
	{
		return owner;
	}

	//The planet the fleet is headed to (read by MoraleModel)
	Planet getTarget()
	{
		return target;
	}

	//Ships still alive in this fleet, including those that have not left their planet yet (read by MoraleModel)
	int getNumUnits()
	{
		return numUnits;
	}

	//Moves all launched ships towards the target planet, each ship invades one unit on its own arrival
	//Returns true if the fleet has no ships left
	public boolean update()
	{
		age++;
		Iterator<Ship> it = ships.iterator();
		while (it.hasNext())
		{
			Ship s = it.next();
			if (isLaunched(s) && s.fly(target))
			{
				target.invade(owner, 1);
				it.remove();
			}
		}
		updatePosition();
		return ships.isEmpty();
	}

	//Draws every launched ship as a tiny triangle in the fleet's color
	public void draw(Graphics g, Color color)
	{
		for (Ship s: ships)
		{
			if (isLaunched(s))
			{
				drawShip(g, s, color);
			}
		}
	}

	//A ship leaves its planet once enough ticks have passed since the fleet was sent
	private boolean isLaunched(Ship s)
	{
		return age > s.launchTick;
	}

	//Keeps the position and the ship count up to date (read by the AI, see getFleetInfo)
	private void updatePosition()
	{
		double sumX = 0;
		double sumY = 0;
		int flying = 0;
		for (Ship s: ships)
		{
			if (isLaunched(s))
			{
				sumX += s.x;
				sumY += s.y;
				flying++;
			}
		}
		if (flying > 0)
		{
			x = sumX / flying;
			y = sumY / flying;
		}
		else
		{
			x = launchX;
			y = launchY;
		}
		numUnits = ships.size();
	}

	//Draws one ship as a triangle pointing towards the target, shifted sideways onto its lane
	private void drawShip(Graphics g, Ship s, Color color)
	{
		double dx = target.X - s.x;
		double dy = target.Y - s.y;
		double dist = Math.sqrt(dx * dx + dy * dy);
		if (dist == 0)
		{
			return;
		}
		dx /= dist; //Unit vector towards the target
		dy /= dist;
		double cx = s.x - dy * s.lane; //Center of the ship, shifted sideways
		double cy = s.y + dx * s.lane;
		double half = SHIP_SIZE / 2;
		double wide = SHIP_SIZE * 0.4;
		int[] xs = {
			(int) Math.round(cx + dx * half),
			(int) Math.round(cx - dx * half - dy * wide),
			(int) Math.round(cx - dx * half + dy * wide) };
		int[] ys = {
			(int) Math.round(cy + dy * half),
			(int) Math.round(cy - dy * half + dx * wide),
			(int) Math.round(cy - dy * half - dx * wide) };
		g.setColor(color);
		g.fillPolygon(xs, ys, 3);
	}

	//One ship of the swarm, flying on its own lane
	private static class Ship
	{
		double x, y; //Position of the ship on its flight path
		final int launchTick; //Tick (since the fleet was sent) at which the ship leaves the planet
		final double speed; //Slightly different per ship, so the swarm spreads out
		final double lane; //Sideways offset from the flight path, so the swarm has a width

		Ship(double startX, double startY, int launchTick, double spread, double baseSpeed)
		{
			x = startX;
			y = startY;
			this.launchTick = launchTick;
			speed = baseSpeed * (0.85 + 0.3 * Math.random());
			lane = (Math.random() + Math.random() - 1) * spread; //More ships near the center line than at the edges
		}

		//Moves the ship towards the target, returns true when it has reached the planet
		boolean fly(Planet target)
		{
			double dx = target.X - x;
			double dy = target.Y - y;
			double dist = Math.sqrt(dx * dx + dy * dy);
			if (dist > 0)
			{
				x += dx / dist * speed;
				y += dy / dist * speed;
			}
			dx = target.X - x;
			dy = target.Y - y;
			return target.RADIUS * target.RADIUS > dx * dx + dy * dy;
		}
	}

	public FleetInfo getFleetInfo()
	{
		return new FleetInfo(owner, numUnits, x, y, speed, (target != null) ? target.getPlanetInfo() : null);
	}

	//A string representation of this fleet
	public String toString() 
	{
		return "Fleet [(" + x + ", " + y + "), numUnits=" + numUnits
		+ ", owner=" + owner + ", target=" + target + "]";
	}
}

class FleetInfo
{
	public final int NUM_UNITS;
	public final Player OWNER;
	public final double X, Y, SPEED;
	public final PlanetInfo TARGET;

	public FleetInfo(Player ownerIn, int numUnitsIn, double xIn, double yIn, double speedIn, PlanetInfo targetIn)
	{
		OWNER = ownerIn;
		NUM_UNITS = numUnitsIn;
		X = xIn;
		Y = yIn;
		TARGET = targetIn;
		SPEED = speedIn;
	}

	public Player getOwner()
	{
		return OWNER;
	}

	public int getNumUnits()
	{
		return NUM_UNITS;
	}

	public double getX()
	{
		return X;
	}

	public double getY()
	{
		return Y;
	}

	public PlanetInfo getTarget()
	{
		return TARGET;
	}

	public boolean equals(FleetInfo f)
	{
		if ( OWNER == f.OWNER &&
				NUM_UNITS == f.NUM_UNITS && 
				X == f.X && 
				Y == f.Y &&
				SPEED == f.SPEED &&
				TARGET.equals(f.TARGET))

			return true;

		else return false;
	}

}

