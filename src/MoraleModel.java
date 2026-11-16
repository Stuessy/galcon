package src;

import java.util.Collection;

/*
 * Morale rules of the planets, ported from the Legacy game (legacy/planet.py, loop).
 * The morale (0-100) moves step by step towards the target morale, which depends on the ships around a planet.
 * Phase 1 (prepare) computes the target values of all planets from the same state, phase 2 (nextMorale) moves the morale.
 * The intervals are in ticks of Game (10 ms each).
 */
public final class MoraleModel
{
	public static final int MAX_MORALE = 100;
	public static final int NEUTRAL_START_MORALE = 50;
	public static final int HOME_START_MORALE = 100;
	static final int FALL_INTERVAL_TICKS = 6; //About 60 ms per point, about 16 points per second
	static final int RISE_INTERVAL_TICKS = 25; //250 ms per point, 4 points per second

	private MoraleModel()
	{
	}

	//Phase 1: computes the target morale and the ship balance of every planet
	//Caller holds the locks of planets and fleets (see Game.updateAllPlanets)
	static void prepare(Collection<Planet> planets, Collection<Fleet> fleets)
	{
		for (Planet p: planets)
		{
			p.incomingFriendly = 0;
			p.incomingEnemy = 0;
		}
		for (Fleet f: fleets)
		{
			Planet t = f.getTarget(); //A fleet counts for its current target, whoever owns it now
			if (t != null)
			{
				if (f.getOwner() == t.getOwner())
				{
					t.incomingFriendly += f.getNumUnits();
				}
				else
				{
					t.incomingEnemy += f.getNumUnits();
				}
			}
		}
		for (Planet p: planets)
		{
			int friend = p.getNumUnits() + p.incomingFriendly;
			int enemy = p.incomingEnemy;
			for (Planet n: p.getNear())
			{
				if (n.getOwner() == p.getOwner())
				{
					friend += n.getNumUnits();
				}
				else if (n.getOwner() != null)
				{
					enemy += n.getNumUnits();
				}
			}
			p.setRealShips(friend - enemy);
			//Legacy rule: no ships around at all means full morale (also avoids a division by 0)
			int target = (friend + enemy == 0) ? MAX_MORALE : (int) Math.round(friend * 100.0 / (friend + enemy));
			p.setRealMorale(target);
		}
	}

	//Phase 2: moves the morale one point towards the target morale, on the ticks given by the intervals
	//Called from Planet.update
	static int nextMorale(Planet p, int tick)
	{
		int m = p.getMorale();
		if (p.getRealMorale() < m && tick % FALL_INTERVAL_TICKS == 0)
		{
			m--;
		}
		else if (p.getRealMorale() > m && tick % RISE_INTERVAL_TICKS == 0)
		{
			m++;
		}
		return Math.max(0, Math.min(MAX_MORALE, m));
	}
}
