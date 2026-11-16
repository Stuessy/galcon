package src;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Determines the neighbours of every planet, ported from the Legacy game (legacy/level.py, initialisation).
 * The neighbours of a planet are its 5 nearest planets, plus every planet that has it among its 5 nearest.
 * Called once per game, because the planet positions never change.
 */
public final class PlanetNeighbours
{
	private static final int NUM_NEAREST = 5; //Nearest planets per planet, before the symmetric completion

	private PlanetNeighbours()
	{
	}

	//Sets the neighbours of all given planets
	static void assign(Collection<Planet> planets)
	{
		Map<Planet, Set<Planet>> near = new HashMap<Planet, Set<Planet>>();
		for (Planet a: planets)
		{
			near.put(a, new LinkedHashSet<Planet>());
		}
		for (Planet a: planets)
		{
			for (Planet b: nearestTo(a, planets))
			{
				near.get(a).add(b);
				near.get(b).add(a); //Symmetric completion: a is also a neighbour of b
			}
		}
		for (Planet a: planets)
		{
			a.setNear(Collections.unmodifiableList(new ArrayList<Planet>(near.get(a))));
		}
	}

	//The planets closest to a (by squared distance of the centers), a itself excluded
	private static List<Planet> nearestTo(final Planet a, Collection<Planet> planets)
	{
		List<Planet> others = new ArrayList<Planet>();
		for (Planet b: planets)
		{
			if (b != a)
			{
				others.add(b);
			}
		}
		Collections.sort(others, new Comparator<Planet>()
		{
			public int compare(Planet p, Planet q)
			{
				int dp = squaredDistance(a, p);
				int dq = squaredDistance(a, q);
				return (dp < dq) ? -1 : ((dp == dq) ? 0 : 1);
			}
		});
		return others.subList(0, Math.min(NUM_NEAREST, others.size()));
	}

	private static int squaredDistance(Planet a, Planet b)
	{
		int dx = a.getX() - b.getX();
		int dy = a.getY() - b.getY();
		return dx * dx + dy * dy;
	}
}
