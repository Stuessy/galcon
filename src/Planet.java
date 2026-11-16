//3/13/14
package src;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Collections;
import java.util.List;

public class Planet 
{
	public static final int MAX_SIZE = 50;
	public static final int MIN_SIZE = 12;
	public static final int MAX_NEUTRAL_UNITS = 50;
	public static final int MIN_PRODUCE_TIME = 10;
	public static final int MAX_PRODUCE_TIME = 40;

	private static final Image PLANET_IMAGE = Assets.PLANET_GRAY;
	public static final Font TEXT_FONT = new Font("Monospaced", Font.BOLD, (int) (0.7 * MIN_SIZE * 2));
	
	public final int X, Y, RADIUS, PRODUCTION_TIME;
	private int numUnits, updateCnt = 0;
	private Player owner;
	private Game parent;
	private volatile int morale; //0-100, moves step by step towards realMorale (see MoraleModel)
	private volatile int realMorale; //Target morale, from the ship ratio around the planet
	private volatile int realShips; //Ship balance around the planet (friendly minus enemy)
	int incomingFriendly, incomingEnemy; //Ships of fleets heading here, written only by MoraleModel.prepare
	private double productionProgress = 0; //Part of the next unit, scaled by morale
	private List<Planet> near = Collections.emptyList(); //Neighbours, set once per game (see PlanetNeighbours)

	//Construct a planet owned by a particular person
	public Planet(Player player, Game parent)
	{
		this.parent = parent;
		owner = player;
		numUnits = 100;
		morale = realMorale = MoraleModel.HOME_START_MORALE;
		RADIUS = MAX_SIZE;
		PRODUCTION_TIME = MIN_PRODUCE_TIME;
		//    	setupImage();
		
		//Try to find a safe landing zone for our planet
		int x = Tournament.genX(RADIUS);
		int y = Tournament.genY(RADIUS);
		while (parent.isPlanetOverlapping(x, y, RADIUS))
		{
			x = Tournament.genX(RADIUS);
			y = Tournament.genY(RADIUS);

		}
		
		//Assign our final variables
		X = x;
		Y = y;
	}

	//Construct a planet with a random size, owned by the given player (null = neutral) with the given ships
	public Planet(Game parent, Player owner, int startUnits)
	{
		this.parent = parent;
		this.owner = owner;
		numUnits = startUnits;
		morale = realMorale = MoraleModel.NEUTRAL_START_MORALE;
		RADIUS = (int) (Math.random() * (MAX_SIZE - MIN_SIZE) + MIN_SIZE);
		PRODUCTION_TIME = (int) ((1 - ((double) RADIUS - MIN_SIZE) / (MAX_SIZE - MIN_SIZE)) * 
				(MAX_PRODUCE_TIME - MIN_PRODUCE_TIME) + MIN_PRODUCE_TIME); //Calculate the time between productions
		//		setupImage();
		int x = Tournament.genX(RADIUS);
		int y = Tournament.genY(RADIUS);
		
		while (parent.isPlanetOverlapping(x, y, RADIUS))
		{
			x = Tournament.genX(RADIUS);
			y = Tournament.genY(RADIUS);
		}
		X = x;
		Y = y;
	}
	
	//Construct a neutrally owned planet with a random starting size.
	public Planet(Game parent)
	{
		this(parent, null, (int) (Math.random() * MAX_NEUTRAL_UNITS));
	}

	//Get this planet's information
	public PlanetInfo getPlanetInfo()
	{
		return new PlanetInfo(owner, numUnits, X, Y, RADIUS, PRODUCTION_TIME, morale);
	}

	public int getRadius()
	{
		return RADIUS;
	}

	public void draw(Graphics g, Color bodyColor, Color textColor)
	{
		//Experimental optimization
//		//Change transparency depending on number of units
//		g.setColor(new Color(bodyColor.getRed(),
//				bodyColor.getGreen(),
//				bodyColor.getBlue(),
//				Math.min(50 + numUnits, 255)));
		g.fillOval(X - RADIUS, Y - RADIUS, RADIUS * 2, RADIUS * 2);
		if (textColor != null) //Number of units, centered in the planet
		{
			Color bodyPaint = g.getColor(); //The next planets are drawn with the same color, so restore it
			g.setFont(TEXT_FONT);
			FontMetrics fm = g.getFontMetrics();
			String units = String.valueOf(numUnits);
			g.setColor(textColor);
			g.drawString(units, X - fm.stringWidth(units) / 2, Y + (fm.getAscent() - fm.getDescent()) / 2);
			g.setColor(bodyPaint);
		}
	}

	//One tick: moves the morale towards its target, then produces ships on owned planets
	public void update()
	{
		morale = MoraleModel.nextMorale(this, updateCnt);
		if (owner != null)
		{
			//Full morale gives one unit every PRODUCTION_TIME ticks, half morale half as many
			productionProgress += morale / (double) MoraleModel.MAX_MORALE;
			if (productionProgress >= PRODUCTION_TIME)
			{
				numUnits++;
				productionProgress -= PRODUCTION_TIME;
			}
		}
		updateCnt++;
	}
	
	public Player getOwner()
	{
		return owner;
	}

	public int getNumUnits()
	{
		return numUnits;
	}

	public int getMorale()
	{
		return morale;
	}

	public int getRealMorale()
	{
		return realMorale;
	}

	public int getRealShips()
	{
		return realShips;
	}

	public List<Planet> getNear()
	{
		return near;
	}

	void setNear(List<Planet> nearIn)
	{
		near = nearIn;
	}

	void setRealMorale(int value)
	{
		realMorale = value;
	}

	void setRealShips(int value)
	{
		realShips = value;
	}

	public void invade(Player player, int numUnitsIn)
	{
		if(owner == player)
		{
			numUnits = numUnits + numUnitsIn;
		}
		else
		{
			numUnits = numUnits - numUnitsIn;
			if(numUnits < 0)
			{
				owner = player; //Pass ownership of the planet to the invading player
				numUnits = -numUnits; //Make the number of units positive again
				productionProgress = 0; //Production starts afresh, the morale is kept
			}
		}
	}

	public int getX()
	{
		return X;
	}

	public int getY()
	{
		return Y;
	}

	//Send a fleet from this planet to some other planet
	public void sendFleet(int numSent, Planet target)
	{
		if (target != this && target != null)
		{
			if(numSent > numUnits)
			{
				numSent = numUnits;
			}

			if (numSent > 0)
			{
				//The ships leave from the border of this planet, facing the target
				double dist = Math.hypot(target.X - X, target.Y - Y);
				double launchX = X + (target.X - X) / dist * RADIUS;
				double launchY = Y + (target.Y - Y) / dist * RADIUS;
				parent.addFleet(new Fleet(launchX, launchY, numSent, owner, target, parent.getShipSpeed()));
				numUnits -= numSent;
			}
		}
	}
	
	public String toString() 
	{
		return "Planet [(" + X + ", " + Y + "), r=" + RADIUS
				+ ", produce_time=" + PRODUCTION_TIME + ", numUnits="
				+ numUnits + "]";
	}

}

/*
 * Wrapper class for `Planet` which allows a player to grab all of a Planet's info at once
 */
class PlanetInfo
{	
	public final int NUM_UNITS, X, Y, RADIUS, PRODUCTION_TIME, MORALE;
	public final Player OWNER;

	public PlanetInfo (Player ownerIn, int numUnitsIn, int xIn, int yIn, int radiusIn, int production, int moraleIn)
	{
		OWNER = ownerIn;
		NUM_UNITS = numUnitsIn;
		X = xIn;
		Y = yIn;
		RADIUS = radiusIn;
		PRODUCTION_TIME = production;
		MORALE = moraleIn;
	}

	//Getters
	public Player getOwner()
	{
		return OWNER;
	}
	public int getNumUnits()
	{
		return NUM_UNITS;
	}
	public int getMorale()
	{
		return MORALE;
	}
	public int getX()
	{
		return X;
	}
	public int getY()
	{
		return Y;
	}
	public int getRadius()
	{
		return RADIUS;
	}
	public int getProductionTime()
	{
		return PRODUCTION_TIME;
	}
	//Units per tick, scaled by morale like the production in Planet.update (full morale: 1 / PRODUCTION_TIME)
	//Neutral planets do not produce in this version, so their rate is 0
	public double getProductionRate()
	{
		if (OWNER == null)
		{
			return 0.0;
		}
		return MORALE / (double) MoraleModel.MAX_MORALE / PRODUCTION_TIME;
	}

	//Test to see if this PlanetInfo is the same as some other PlanetInfo
	public boolean equals(PlanetInfo other)
	{
		return (OWNER == other.OWNER &&
				NUM_UNITS == other.NUM_UNITS &&
				X == other.X &&
				Y == other.Y &&
				RADIUS == other.RADIUS);
	}
}
