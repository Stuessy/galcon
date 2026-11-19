# Galcon

## Installation:
1. Create a Project in Eclipse called `Galcon`
2. Keep the image files in `assets/images/` and the editable source artwork in `assets/source/`.
3. Keep the `.java` files in the `src/` folder of the `Galcon` project in Eclipse.

## How to Start

### Single Player
Run the `SinglePlayer` class in Eclipse (right-click → Run As → Java Application).

1. On the start screen, select a difficulty below the planets: Easy, Normal (preselected), Hard or Crazy.
2. Click the blue or the orange planet to choose your color. The game starts, the AI plays the other color.
3. When the game is over, click **Restart** to return to the start screen. The win counts are kept across restarts.

### AI Tournament
Run the `Tournament` class in Eclipse (right-click → Run As → Java Application).

* Two built-in AIs, *Enemy AI 1* (blue) and *Enemy AI 2* (orange), play a tournament of `NUM_GAMES` games (default: 2).
* When the tournament is over, the winner is shown. Click **Restart** to start a new tournament. The win counts are kept across restarts.
* Results are also printed to the console. Close the window to stop.
* To change the number of games, edit `NUM_GAMES` in `Tournament.java`.

To write your own AI, start with the `SamplePlayer` class.

## What is Galcon and How Do I Play?
Galcon is an RTS (Real Time Strategy) game that is similar to Risk in that the goal is to capture all of your enemy's planets.

### From Wikipedia:
Players start with 1 large "home planet" and send off ships to conquer other planets around them. The numbers on each planet indicate how many ships it will take to conquer them. The numbers on a player's own planet indicate the amount of ships that their planet holds. Each planet a player owns produces ships for that player with more ships at a faster rate produced depending on the planet's size. Players can select what percentage of the ships to send from a planet and players can redirect ships in mid-flight. The aim of the game is to defeat the other player by eliminating all of the enemy planets.

### To play for yourself
* PC/Mac: http://www.galcon.com/flash/
* iPhone/iPad: Search 'Galcon' in the App Store

## Controls (Single Player)
* **Click one of your planets** to mark it. A white ring marks the selection. Click it again to remove the mark.
* **Double-click one of your planets** to mark all of your planets.
* **Drag from one of your planets onto another planet** (own, neutral or enemy) and release the mouse there. Every marked planet, and the planet you dragged from, sends the share shown on the button (default 50 %) of its ships to the target. The marks are cleared afterwards.
* While dragging, the planet you dragged from gets a white ring, even if it was not marked before.
* While dragging, every sending planet (each marked planet and the planet you dragged from) draws a white line from its center to the mouse pointer.
* While dragging, the planet under the pointer is highlighted in green if it is a valid target (every planet except the one you dragged from).
* **Click empty space or a planet that is not yours** to clear all marks.
* Releasing the mouse on empty space, or on the planet you dragged from, does nothing.
* **Hover over one of your planets** to see its stats: production (ships per second) / morale / ships.
* **Click the share button** in the lower right corner to change the share of ships that is sent: 25 %, 50 %, 75 % or 100 %. The pie chart shows the setting, hovering over the button shows the percentage. Every game starts with 50 %.


## Game Concepts
* **Ships:** The number on a planet is its number of ships. In single player, the ship count of enemy planets is hidden, except on Easy.
* **Fleets:** Ships sent from a planet leave it at once and form a fleet that flies to the target. Each arriving ship adds one ship to an own planet or removes one ship from any other planet. A planet without ships is taken by the next arriving ship.
* **Production:** Owned planets produce ships over time. Bigger planets produce faster: at full morale, from about 2.5 ships per second (smallest) to 10 ships per second (biggest). Neutral planets produce nothing.
* **Morale (0-100):** Morale is a multiplier for production: at 100 a planet produces at full speed, at 50 at half speed. It moves towards a target value, which is the share of the planet owner's ships among all ships on the planet, on its neighboring planets, and in fleets heading to it. Morale falls quickly (about 16 points per second) and recovers slowly (about 4 points per second). Starting home planets begin at 100, all other planets at 50.
* **Neighbors:** Each planet has its nearest planets as neighbors (at least 5). Neighbors influence morale. The built-in AI only sends ships to neighbors, while you can send ships to any planet.
* **Winning:** A player loses as soon as it owns no planets. Ships in flight do not count.
* **Difficulty:** Sets the AI's pause between its moves and the number of AI planets at the start. Every map has 16 planets.

| Difficulty | AI planets at start (incl. home planet) | Pause between AI moves |
|---|---|---|
| Easy | 1 | 1.5-2.0 s |
| Normal (default) | 1 | 0.8-1.2 s |
| Hard | 3 | 0.6-0.9 s |
| Crazy | 6 | 0.4-0.6 s |

## Classes
All classes are in the package `src`.

| Class | Purpose |
|---|---|
| `SinglePlayer` | Entry point for single player: start screen, game and restart loop. |
| `Tournament` | Entry point for the AI tournament. Also the game window and ranking screen used by all modes. |
| `StartScreen` | Start screen for choosing the color and the difficulty. |
| `Difficulty` | Difficulty levels Easy, Normal, Hard and Crazy. |
| `Game` | One game: 10 ms game loop (one tick), map generation, drawing and winner check. |
| `Planet` | A planet with owner, ships, morale and production. `PlanetInfo` is a read-only snapshot for AIs. |
| `Fleet` | Ships flying from a planet to a target. `FleetInfo` is a read-only snapshot for AIs. |
| `MoraleModel` | Morale rules: target morale and morale changes per tick. |
| `PlanetNeighbours` | Determines the neighbors of each planet. |
| `Player` | Abstract base class of all players. Provides the methods listed under "Usable Methods". |
| `EnemyAI` | Built-in AI used in single player and in the tournament. |
| `HumanPlayer` | Mouse-controlled player for single player. |
| `SamplePlayer` | Example AI. Start here to write your own AI. |
| `Assets` | Loads the images (planets and star background). |

## Usable Methods
### Player Class
* Set<PlanetInfo> getAllPlanetInfo()
* Set<FleetInfo> getAllFleetInfo()
* Set<PlanetInfo> getEnemyPlanetInfo()
* Set<FleetInfo> getEnemyFleetInfo()
* Set<PlanetInfo> getMyPlanetInfo()
* Set<FleetInfo> getMyFleetInfo()
* Set<PlanetInfo> getNeutralPlanetInfo()
* void sendFleet(PlanetInfo origin, int numUnits, PlanetInfo target)
* void changeFleetTarget(FleetInfo f, PlanetInfo target)

### PlanetInfo Class
* Player getOwner()
* int getNumUnits()
* int getMorale() <- morale of the planet, 0 to 100. Planets with low morale produce less (100 = full production)
* int getX()
* int getY()
* int getRadius()
* int getProductionTime() <- number of ticks you have to wait to produce 1 unit at full morale (100)
* double getProductionRate() <- units per tick, scaled by morale (full morale: 1 / getProductionTime()), 0 for neutral planets

* boolean equals(PlanetInfo p)

### FleetInfo Class
* Player getOwner()
* int getNumUnits()
* double getX()
* double getY()
* PlanetInfo getTarget()
* boolean equals(FleetInfo p)
  
### For More Information:
To see an example of an AI, open the `SamplePlayer` class. It will cover what you need to do to create an AI.