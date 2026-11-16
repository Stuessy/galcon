package src;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Enemy AI, ported from the Legacy game (legacy/level.py, computer).
 * Each move it looks at every own planet and decides for each neighbour whether to send ships there.
 * It only sends to neighbouring planets. Attacks only start if the ships can take the target (R6).
 * The morale rules themselves are in MoraleModel and Planet.
 */
public class EnemyAI extends Player
{
	private static final int MIN_SHIPS_TO_SEND = 10; //Planets with fewer ships keep them
	private static final int MORALE_DIFF_FOR_REINFORCEMENT = 10; //Morale difference to an own neighbour that triggers help
	private static final int SHIP_DIFF_FOR_REINFORCEMENT = 10; //Ship difference to an own neighbour that triggers help

	private final String name;
	private final Color color;
	private final int minPause; //Pause before each move in ms, randomized between min and max
	private final int maxPause;

	public EnemyAI(String name, Color color, int minPause, int maxPause)
	{
		this.name = name;
		this.color = color;
		this.minPause = minPause;
		this.maxPause = maxPause;
	}

	public Color getColor()
	{
		return color;
	}

	public String getPlayerName()
	{
		return name;
	}

	//One move: waits first, then decides for all own planets in random order
	public void makeMove()
	{
		try
		{
			sleep(minPause + (int) (Math.random() * (maxPause - minPause + 1))); //Before the guard, so the loop never spins
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			return;
		}
		Game game = currentGame;
		if (game == null || game.getWinner() != null)
		{
			return; //No game yet or game over: nothing to send
		}
		synchronized (game.getAllPlanets())
		{
			List<Planet> own = new ArrayList<Planet>();
			for (Planet p: game.getAllPlanets())
			{
				if (p.getOwner() == this)
				{
					own.add(p);
				}
			}
			Collections.shuffle(own);
			for (Planet origin: own)
			{
				decideFor(game, origin);
			}
		}
	}

	//Decides for one own planet, neighbour by neighbour in random order. Caller holds the lock on the planets.
	private void decideFor(Game game, Planet origin)
	{
		if (origin.getOwner() != this)
		{
			return;
		}
		List<Planet> targets = new ArrayList<Planet>(origin.getNear());
		Collections.shuffle(targets);
		for (Planet target: targets)
		{
			//Checked before every target, because each sending reduces the ships of the origin
			if (origin.getNumUnits() < MIN_SHIPS_TO_SEND) //R1: too few ships
			{
				break;
			}
			//No morale gate: a demoralized planet keeps fighting (the former legacy rule R2 was removed)
			int amount;
			if (target.getOwner() == this)
			{
				if (origin.getRealMorale() - target.getRealMorale() > MORALE_DIFF_FOR_REINFORCEMENT)
				{
					amount = origin.getNumUnits() / 4; //R3: reinforce for morale
				}
				else if (origin.getRealShips() - target.getRealShips() > SHIP_DIFF_FOR_REINFORCEMENT)
				{
					amount = (origin.getRealShips() - target.getRealShips()) / 2; //R4: reinforce for ships
				}
				else
				{
					continue; //R5: own neighbour does not need help
				}
			}
			else
			{
				amount = origin.getNumUnits() * 3 / 4; //R6: neutral or enemy neighbour is attacked
				if (!isStrongEnough(game, target, amount)) //R6: only attack if the ships can take the target
				{
					continue;
				}
			}
			if (amount > 0)
			{
				origin.sendFleet(amount, target);
			}
		}
	}

	//R6 strength check: the new ships plus own ships already heading to the target must exceed its garrison.
	//If the ships on their way are already enough, nothing more is sent. Caller holds the lock on the planets.
	private boolean isStrongEnough(Game game, Planet target, int amount)
	{
		int garrison = target.getNumUnits(); //Ships on the target, the invaders have to beat them
		int committed = shipsHeadingTo(game, target);
		return committed <= garrison && committed + amount > garrison;
	}

	//Own ships in fleets that are on their way to the target, also those that have not left their planet yet
	private int shipsHeadingTo(Game game, Planet target)
	{
		int ships = 0;
		synchronized (game.getAllFleets()) //Lock order: planets before fleets
		{
			for (Fleet f: game.getAllFleets())
			{
				if (f.getOwner() == this && f.getTarget() == target)
				{
					ships += f.getNumUnits();
				}
			}
		}
		return ships;
	}
}
