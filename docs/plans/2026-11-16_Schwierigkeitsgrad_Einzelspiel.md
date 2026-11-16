# Umsetzungsplan: Schwierigkeitsgrad für das Einzelspiel (Übernahme aus dem Legacy-Spiel)

**Titel:** Schwierigkeitsgrad der Gegner-KI im Einzelspiel, Auswahl auf dem Startbildschirm  
**Status:** Umgesetzt  
**Erstellt:** 2026-11-16  
**Überarbeitet:** 2026-11-16 (Medium → Normal, KI-Planeten 1/1/3/6, Reaktionszeit je Stufe, nur Mausbedienung, alle offenen Punkte entschieden)  
**Bereich:** `src` (`StartScreen`, `SinglePlayer`, `Tournament`, `Game`, `Planet`, neu: `Difficulty`)  

---

## 1. Ausgangslage und Problemstellung (IST)

### 1.1 Fachlicher Hintergrund
- Im Legacy-Spiel wählt der Spieler im Hauptmenü mit ←/→ eine Stufe (`easy`, `medium`, `hard`, `crazy`) und startet mit „play“ (`legacy/menu.py`: Stufenliste Z. 26–31, Tastensteuerung Z. 111–116, Start Z. 126–127).
- Im `src`-Spiel gibt es keine Stufe. Der Einzelspieler wählt auf dem Startbildschirm nur seine Farbe. Ein Klick auf einen der beiden Farbplaneten startet sofort das Spiel (`src/StartScreen.java`, Z. 59–73).

### 1.2 Befund: Was die Stufe im Legacy-Spiel tatsächlich verändert
Die Stufe ändert **nur die Startkarte**. Die KI-Logik ist für alle Stufen identisch.

| Stufe (Legacy) | Planeten KI (inkl. Heimat) | Planeten Mensch | Neutrale Planeten | Summe |
|---|---|---|---|---|
| easy | 1 | 1 | 24 | 26 |
| medium | 3 | 1 | 22 | 26 |
| hard | 6 | 1 | 19 | 26 |
| crazy | 10 | 1 | 15 | 26 |

Quelle: `legacy/menu.py` Z. 27–30. Die Liste `counts` hat die Reihenfolge `[neutral, mensch, KI]` (`legacy/level.py` Z. 62–84), übergeben wird sie in `legacy/menu.py` Z. 127.

Weitere, im Code geprüfte Befunde:
- **KI-Logik ohne Stufe:** `legacy/level.py` Z. 429–456 (`computer`) hat keinen Stufenparameter. Die Entscheidungsregeln gelten in allen Stufen gleich.
- **Fester KI-Takt:** `legacy/level.py` Z. 431 (`p1.frame % (FPS/2) == 0`), `legacy/cnst.py`: `FPS = 32`. Die KI entscheidet in allen Stufen alle 16 Frames, also etwa alle 0,5 s.
- **Heimat der KI:** Im Spiel hat sie immer 100 Schiffe (`legacy/level.py` Z. 75–76). Der Parameter `easy` (Z. 77–78: Heimat der KI nur 25 Schiffe) wird nur im Tutorial gesetzt (`legacy/menu.py` Z. 240 übergibt `1`). Beim Spiel „play“ ist er `0` (Z. 127). Er wird deshalb nicht übernommen.
- **Zusatz-KI-Planeten:** Alle KI-Planeten außer der Heimat haben 10–29 Schiffe (`legacy/planet.py`, `__init__`: `rr(10,30)`), Moral 50 (`self.morale = 50`) und zufällige Produktion (`legacy/level.py` Z. 66–70).

### 1.3 IST im `src`-Code
- `src/Game.java` Z. 24: `NUM_PLANETS = 16`. Die Konstante wird nur in `generateNewMap` gelesen (Z. 184).
- `src/Game.java` Z. 177–190 (`generateNewMap`): zwei Heimatplaneten (Z. 181–182), danach `NUM_PLANETS - 2` neutrale Planeten (Z. 184–187). Keine Stufe.
- `src/Game.java` Z. 28–33 (Konstruktor): Die Fluggeschwindigkeit kommt über `parent.getShipSpeed()` aus dem `Tournament`. Dieses Muster wird für die Stufe übernommen.
- `src/Tournament.java` Z. 97–137: Der Konstruktor mit `scoreHistory` führt die komplette Spielschleife aus (Z. 121–132, `new Game` in Z. 123). Das Feld für die Stufe muss deshalb vor dieser Schleife gesetzt sein.
- `src/SinglePlayer.java` Z. 17–30: Endlosschleife aus `StartScreen` (Z. 22–23), `Tournament(HumanPlayer, EnemyAI("Enemy AI", aiColor, 800, 1200), 1, SHIP_SPEED, scoreHistory)` (Z. 25–26) und `waitForRestart()`. Die Pause 800–1200 ms ist der einzige Ort mit dieser Reaktionszeit.
- `src/EnemyAI.java` Z. 25–31 (Konstruktor mit `minPause`, `maxPause`) und Z. 48 (`sleep(minPause + ...)` vor jedem Zug). Die Pause wird also über den Konstruktor gesetzt, die KI-Logik muss dafür nicht geändert werden.
- `src/StartScreen.java`: Konstruktor `(Color left, Color right)` Z. 29–38, `waitForChoice()` Z. 41–45, `handleClick` Z. 59–73, `StartPanel` Z. 76–105.
- `src/Planet.java` Z. 35–58: Heimatkonstruktor `Planet(Player, Game)` (100 Schiffe, Radius 50, Produktion 10). Z. 60–81: Neutralkonstruktor `Planet(Game)` (zufälliger Radius, Produktion aus dem Radius, 0–49 Schiffe, Moral 50). **Es gibt keinen Konstruktor für einen Planeten mit Besitzer und zufälligem Radius.**
- `src/Game.java` Z. 110–115 (`textColorFor`): Die Schiffszahl fremder Planeten wird dem Menschen nicht angezeigt. Die Zusatz-KI-Planeten lassen sich deshalb nur über Konsole oder Debugger prüfen.

### 1.4 Ursachenanalyse
Kein Fehler, sondern ein fehlendes Feature. Die Spielschicht kennt keine Stufe. Der Startbildschirm kann nur eine Farbe zurückgeben. `Planet` kann keinen Planeten mit Besitzer und zufälligem Radius erzeugen.

### 1.5 Bewusste Abweichungen vom Legacy-Spiel
- Die Stufe „medium“ heißt in `src` **Normal** und ist der Standard.
- KI-Planeten inkl. Heimat: Legacy 1/3/6/10, `src` **1/1/3/6** (Vorgabe des Auftraggebers).
- Gesamtzahl der Planeten: Legacy 26, `src` 16 (vorerst, siehe O2).
- Reaktionszeit: Legacy hat einen festen Takt. `src` staffelt sie je Stufe (siehe 2.3).
- Auswahl nur per Maus, ohne Tastatur ←/→ (siehe O4).

---

## 2. Fachliche Anforderungen & Zielzustand (SOLL)

### 2.1 Anforderungen
- **A1 Stufen:** Vier Stufen `Easy`, `Normal`, `Hard`, `Crazy`. Englische Bezeichnungen passend zum Startbildschirm („Choose your color to start“), siehe O3.
- **A2 Startkarte je Stufe:** siehe Tabelle 2.2. Gesamtzahl 16 Planeten (O2, vorerst).
- **A3 Standard:** `Normal`. Die Stufe ist beim Programmstart vorausgewählt (O9). Normal entspricht dem heutigen Spielstand: 1 KI-Planet, 14 neutrale, KI-Pause 800–1200 ms.
- **A4 Auswahl auf dem Startbildschirm:** Vier Schaltflächen unter den beiden Farbplaneten, darüber die Beschriftung „Difficulty“. Die gewählte Stufe ist hervorgehoben. Ein Klick auf eine Schaltfläche wählt nur aus und startet das Spiel nicht. Die Bedienung erfolgt ausschließlich mit der Maus (O4).
- **A5 Start:** Ein Klick auf einen der beiden Farbplaneten startet das Spiel mit der aktuell gewählten Stufe. Der übrige Ablauf bleibt gleich.
- **A6 Restart:** Nach „Restart“ bleibt die zuletzt gewählte Stufe vorausgewählt.
- **A7 Geltungsbereich:** Stufen und Reaktionszeit gelten nur im `SinglePlayer`. `Tournament.main` (KI gegen KI) bleibt unverändert: Startkarte wie Normal, KI-Pausen 500 ms.
- **A8 KI-Logik unverändert:** `EnemyAI` (Regeln R1–R6), `MoraleModel` und `PlanetNeighbours` ändern sich nicht. Je Stufe variiert nur die Pause, sie wird über den bestehenden Konstruktor von `EnemyAI` gesetzt.
- **A9 Zusatz-KI-Planeten:** Besitzer ist die Gegner-KI (`player2`). Größe und Produktionszeit sind zufällig wie bei neutralen Planeten (`PRODUCTION_TIME` aus dem Radius). Schiffe: 10–29, wie im Legacy-Spiel (`rr(10,30)`, O5). Moral startet bei 50 (`MoraleModel.NEUTRAL_START_MORALE`).
- **A10 Heimatplaneten unverändert:** Mensch und KI haben 100 Schiffe, Radius 50, Produktionszeit 10 und Moral 100.
- **A11 Stufe am Endscreen:** Zeile „Difficulty: <Stufe>“ über den Ergebnissen (O6).
- **A12 Reaktionszeit je Stufe:** Die Pause vor jedem Zug der Gegner-KI hängt von der Stufe ab (Tabelle 2.3). Die Werte gelten pro Partie. Ein Stufenwechsel auf dem Startbildschirm wirkt ab der nächsten Partie.
- **A13 Statistik:** Eine Sitzungsstatistik für alle Stufen, ohne Trennung nach Stufe (O7). Die Stufe der jeweiligen Partie wird nur am Endscreen angezeigt (A11).

### 2.2 Startkarte je Stufe (`src`)
Formel: neutrale Planeten = `NUM_PLANETS − 1 − KI-Planeten` (1 Planet für den Menschen).

| Stufe | KI-Planeten inkl. Heimat | Davon Zusatz-KI-Planeten | Mensch | Neutral | Summe |
|---|---|---|---|---|---|
| Easy | 1 | 0 | 1 | 14 | 16 |
| Normal | 1 | 0 | 1 | 14 | 16 |
| Hard | 3 | 2 | 1 | 12 | 16 |
| Crazy | 6 | 5 | 1 | 9 | 16 |

Easy und Normal haben dieselbe Startkarte. Sie unterscheiden sich nur in der Reaktionszeit.

### 2.3 Reaktionszeit der Gegner-KI je Stufe (nur Einzelspiel)
Die Pause vor jedem Zug wird zufällig zwischen Minimum und Maximum gewählt (`EnemyAI.makeMove`, Z. 48).

| Stufe | Pause (ms) | Mittelwert | Vergleich zu Normal |
|---|---|---|---|
| Easy | 1500–2000 | 1750 | +75 % (langsamer) |
| Normal | 800–1200 | 1000 | unverändert |
| Hard | 600–900 | 750 | −25 % (etwas schneller) |
| Crazy | 400–600 | 500 | −50 % (deutlich schneller) |

Crazy übernimmt den Legacy-Takt von 0,5 s (16 Frames bei 32 FPS) als Bereich 400–600 ms (O10). Hard ist ein Vorschlag, der unverändert übernommen wurde (O10). Feinabstimmung nach Playtest (F2).

### 2.4 Out of Scope (bewusst nicht ändern)
- Legacy-Tutorial und der Parameter `easy` (Heimat der KI nur 25 Schiffe). Er wirkt nur im Tutorial.
- **Tastatursteuerung** der Stufenauswahl (←/→ wie im Legacy-Spiel). Bewusst nicht umgesetzt, die Bedienung erfolgt nur mit der Maus (O4).
- Tastenstart (Legacy: Enter). Der Start bleibt ein Mausklick auf einen Farbplaneten.
- Änderungen an den KI-Entscheidungsregeln, an der Moral-Logik, an der Schiffsgeschwindigkeit je Stufe und am Schiffsanteil der KI.
- Startpositionen: Die Heimatplaneten bleiben zufällig (Legacy: feste linke und rechte Kante, siehe F3).
- Speicherung der Stufe in einer Datei. Die Auswahl gilt nur für die laufende Sitzung (siehe F1).
- Anzeige der Schiffszahl fremder Planeten. Sie bleibt nur in Normal, Hard und Crazy verborgen. Easy zeigt sie an (Nachtrag, Abschnitt 11).
- Getrennte Statistik je Stufe (O7).
- Änderungen an `SamplePlayer`, `HumanPlayer` und der Logik von `EnemyAI`. Die öffentliche `Player`-API bleibt unverändert. `README.md` nur für den Nachtrag (Abschnitt 11).
- Balancing-Feintuning über die Werte in 2.2 und 2.3 hinaus. Korrekturen erst nach Playtest (F2).

---

## 3. Technische Analyse und beteiligte Komponenten

| Komponente | Datei / Pfad | Verantwortung / Art der Änderung |
|---|---|---|
| Difficulty | `src/Difficulty.java` | **Neu.** Enum mit Anzeigename, Anzahl KI-Planeten, Pausen (min/max), `getNeutralPlanets(total)`, Standardwert `DEFAULT` und Flag für die Anzeige der Schiffszahl fremder Planeten (nur Easy, Nachtrag) |
| Planet | `src/Planet.java` | **Ändern.** Neuer Konstruktor `Planet(Game, Player, int)` für Planeten mit Besitzer und zufälligem Radius. `Planet(Game)` delegiert dorthin |
| Game | `src/Game.java` | **Ändern.** Feld `difficulty` aus `parent`, `generateNewMap` richtet sich nach der Stufe |
| Tournament | `src/Tournament.java` | **Ändern.** Neuer Konstruktor mit `Difficulty`, Getter `getDifficulty()`, alter Konstruktor delegiert mit `Difficulty.DEFAULT`, Zeile im Endscreen (O6) |
| StartScreen | `src/StartScreen.java` | **Ändern.** Stufen-Schaltflächen per Maus, `getDifficulty()`, Konstruktor mit Startwert |
| SinglePlayer | `src/SinglePlayer.java` | **Ändern.** Stufe über Restarts führen, `EnemyAI` mit den Pausen der Stufe erzeugen, Stufe an `StartScreen` und `Tournament` übergeben |
| EnemyAI | `src/EnemyAI.java` | Unverändert. Die Pausen kommen über den bestehenden Konstruktor |
| HumanPlayer | `src/HumanPlayer.java` | Unverändert |
| MoraleModel, PlanetNeighbours | `src/MoraleModel.java`, `src/PlanetNeighbours.java` | Unverändert. Sie arbeiten mit beliebiger Planetenzahl |
| SamplePlayer | `src/SamplePlayer.java` | Unverändert |
| Legacy-Code | `legacy/` | Unverändert (nur Referenz) |

### 3.1 Datenfluss
1. `SinglePlayer.main`: lokale Variable `difficulty`, Start mit `Difficulty.DEFAULT` (Normal).
2. `StartScreen` (Event-Dispatch-Thread): Klick auf eine Stufe setzt das Feld `difficulty` und zeichnet neu. Klick auf einen Farbplaneten gibt den Latch frei (`chosen.countDown()`).
3. `SinglePlayer.main`: liest nach `waitForChoice()` die Farbe, danach `getDifficulty()`. Dann wird die `EnemyAI` mit `difficulty.getMinPause()` und `difficulty.getMaxPause()` erzeugt und `new Tournament(..., difficulty)` aufgerufen.
4. `Tournament`-Konstruktor: setzt `this.difficulty` vor der Spielschleife. In der Schleife: `new Game(p1, p2, this)`. `Game` liest `parent.getDifficulty()` vor `generateNewMap`. Das Tournament nutzt die Stufe nur für die Startkarte, die Pausen kommen aus der `EnemyAI`.
5. `generateNewMap`: Heimat Mensch, Heimat KI, Zusatz-KI-Planeten (Besitzer `p2`), neutrale Planeten, danach `PlanetNeighbours.assign(planets)`. Der Ablauf läuft wie bisher unter `synchronized(planets)`.

**Thread-Sicherheit:** `StartScreen.difficulty` wird nur auf dem Event-Dispatch-Thread geschrieben und erst nach dem `CountDownLatch` gelesen. Der Latch sorgt für die Sichtbarkeit. `Game.difficulty` ist `final`. Die Pausen-Felder der `EnemyAI` sind `final`. Es kommen keine neuen Sperren dazu.

---

## 4. Lösungsarchitektur und Designentscheidungen
- **Stufe im Spielmodell:** `Game` bestimmt die Startkarte aus der Stufe. Die Reaktionszeit wird beim Erzeugen der `EnemyAI` in `SinglePlayer` gesetzt. Die Entscheidungslogik der KI bleibt gleich. KI-Entwickler sind nicht betroffen, weil die öffentliche API unverändert bleibt.
- **Pause über den Konstruktor:** Die Pause ist pro Partie fest. Ein Stufenwechsel wirkt damit erst auf der nächsten Partie. Das passt zum Ablauf, weil der Startbildschirm nur zwischen Partien erscheint.
- **Easy und Normal teilen die Karte:** Die Unterscheidung liegt allein in der Reaktionszeit. Diese Folge der Vorgabe ist bewusst und wird in E15 festgehalten.
- **Gesamtzahl 16 bleibt (vorerst):** Die Kartendichte bleibt gleich. Die Platzierungsschleife (`Planet`-Konstruktor, `Game.isPlanetOverlapping`, Zufallssuche ohne Abbruch) bleibt im bisherigen Rahmen. Playtests entscheiden über eine Anpassung (O2, F2).
- **Besitzkonvention `player2`:** Im Einzelspiel ist `player2` die Gegner-KI. Die Konvention wird im Code kommentiert. `Tournament.main` nutzt den Standard Normal und hat deshalb keine Zusatz-Planeten.
- **Konstruktor-Delegation:** `Planet(Game)` ruft `this(parent, null, zufällig)` auf. Radius, Produktionszeit und Platzierung stehen nur einmal im Code.
- **Abwärtskompatibilität:** Der alte 5-Parameter-Konstruktor von `Tournament` delegiert mit `Difficulty.DEFAULT`. `Tournament.main` bleibt unverändert.
- **Schutz vor `null`:** `Tournament` setzt bei `null` auf `Difficulty.DEFAULT`.
- **Zeichnung statt Swing-Komponenten:** Die Schaltflächen werden wie der Startbildschirm und die Restart-Schaltfläche (`Tournament.java` Z. 259–274) mit `Graphics` gezeichnet. Die Auswertung erfolgt per Trefferprüfung.
- **Stil:** Anonyme innere Klassen und keine Lambdas, wie in den bestehenden Dateien. Kommentare auf Englisch, wie im bestehenden `src`-Code.

---

## 5. Detaillierte Implementierungsschritte

Die Reihenfolge ist verbindlich. Alle Klassen liegen im Paket `src`.

### Schritt 1: `src/Difficulty.java` (Neu)
```java
package src;

public enum Difficulty
{
	EASY("Easy", 1, 1500, 2000),
	NORMAL("Normal", 1, 800, 1200),
	HARD("Hard", 3, 600, 900),
	CRAZY("Crazy", 6, 400, 600);

	public static final Difficulty DEFAULT = NORMAL; //Preselected on the start screen

	private final String label; //Shown on the start screen
	private final int aiPlanets; //AI planets including the AI home planet
	private final int minPause; //Pause before each AI move in ms, randomized between min and max
	private final int maxPause;

	Difficulty(String label, int aiPlanets, int minPause, int maxPause)
	{
		this.label = label;
		this.aiPlanets = aiPlanets;
		this.minPause = minPause;
		this.maxPause = maxPause;
	}

	public int getAiPlanets()
	{
		return aiPlanets;
	}

	public int getMinPause()
	{
		return minPause;
	}

	public int getMaxPause()
	{
		return maxPause;
	}

	//Neutral planets fill up the rest, the human home planet takes one slot
	public int getNeutralPlanets(int totalPlanets)
	{
		return totalPlanets - 1 - aiPlanets;
	}

	public String toString()
	{
		return label;
	}
}
```

### Schritt 2: `src/Planet.java` (Ändern)
- Neuer Konstruktor. Der Körper stammt aus dem bisherigen Neutralkonstruktor (Z. 61–81), Besitzer und Schiffe kommen aus den Parametern:
  ```java
  //Construct a planet with a random size, owned by the given player (null = neutral)
  public Planet(Game parent, Player owner, int startUnits)
  {
  	this.parent = parent;
  	this.owner = owner;
  	numUnits = startUnits;
  	morale = realMorale = MoraleModel.NEUTRAL_START_MORALE;
  	// RADIUS, PRODUCTION_TIME und Platzierung wie bisher im Neutralkonstruktor
  }

  //Construct a neutrally owned planet with a random starting size.
  public Planet(Game parent)
  {
  	this(parent, null, (int) (Math.random() * MAX_NEUTRAL_UNITS));
  }
  ```
- Der Heimatkonstruktor `Planet(Player, Game)` bleibt unverändert.
- `update()`, `invade()`, `PlanetInfo` bleiben unverändert.

### Schritt 3: `src/Game.java` (Ändern)
- Feld `private final Difficulty difficulty; //Map setup, given by the tournament`.
- Konstruktor: `difficulty = parent.getDifficulty();` direkt nach `shipSpeed = parent.getShipSpeed();`, also vor `generateNewMap(p1, p2)`.
- `generateNewMap` (Z. 177–190) ersetzen:
  ```java
  private void generateNewMap(Player p1, Player p2)
  {
  	synchronized(planets)
  	{
  		planets.add(new Planet(p1, this)); //Human home planet
  		planets.add(new Planet(p2, this)); //AI home planet
  		for (int i = 1; i < difficulty.getAiPlanets(); i++) //Extra AI planets, owned by player 2 (the AI in single player)
  		{
  			planets.add(new Planet(this, p2, 10 + (int) (Math.random() * 20))); //10-29 ships, as Legacy rr(10,30)
  		}
  		for (int i = 0; i < difficulty.getNeutralPlanets(NUM_PLANETS); i++)
  		{
  			planets.add(new Planet(this));
  		}
  		PlanetNeighbours.assign(planets); //Once per game, the planet positions do not change
  	}
  }
  ```
- `NUM_PLANETS` bleibt bei 16.

### Schritt 4: `src/Tournament.java` (Ändern)
- Feld `private final Difficulty difficulty; //Map setup of every game in this tournament`.
- Der bisherige 5-Parameter-Konstruktor (Z. 97–137) delegiert:
  ```java
  public Tournament(Player player1, Player player2, int numGames, double shipSpeed, List<String> scoreHistory)
  {
  	this(player1, player2, numGames, shipSpeed, scoreHistory, Difficulty.DEFAULT);
  }
  ```
- Neuer 6-Parameter-Konstruktor mit `Difficulty difficulty` als letztem Parameter. Sein Körper ist der bisherige Konstruktor-Körper. Die Zuweisung `this.difficulty = (difficulty != null) ? difficulty : Difficulty.DEFAULT;` steht **vor** der Spielschleife (vor Z. 120).
- Getter `public Difficulty getDifficulty()`.
- Endscreen (O6): In `drawRankings` (Z. 248–257) eine Zeile `"Difficulty: " + difficulty` bei y = 150 zeichnen. Sie liegt zwischen dem Titel (y = 100) und den Spielerzeilen (y = 200).

### Schritt 5: `src/StartScreen.java` (Ändern)
- Konstruktor: `public StartScreen(Color left, Color right, Difficulty initial)`, setzt `difficulty = initial`. Der bisherige 2-Parameter-Konstruktor entfällt. Einziger Aufrufer ist `SinglePlayer`.
- Feld `private volatile Difficulty difficulty; //Selected map setup, written on the event dispatch thread`.
- Konstanten:
  ```java
  private static final Font BUTTON_FONT = new Font("Monospaced", Font.BOLD, 20);
  private static final int BUTTON_WIDTH = 160;
  private static final int BUTTON_HEIGHT = 44;
  private static final int BUTTON_GAP = 20;
  private static final int BUTTON_Y = 380; //Below the planets, which end at y = 320
  ```
- Position der Schaltflächen: Die Gesamtbreite ist `n * BUTTON_WIDTH + (n - 1) * BUTTON_GAP` mit `n = Difficulty.values().length`. Sie wird horizontal zentriert. Die Reihenfolge entspricht `Difficulty.values()` (Easy, Normal, Hard, Crazy).
- `handleClick(x, y)`: Zuerst die Stufen-Schaltflächen prüfen. Bei Treffer `difficulty` setzen, `repaint()` und `return`. Danach die Planeten wie bisher.
- Getter `public Difficulty getDifficulty()`.
- Zeichnung in `StartPanel.paintComponent`: Beschriftung „Difficulty“ oberhalb der Schaltflächen. Gewählte Stufe: gefüllt mit `new Color(40, 80, 150)`, weiße Schrift. Übrige Stufen: dunkel gefüllt, graue Schrift.
- Keine Tastatursteuerung (O4).

### Schritt 6: `src/SinglePlayer.java` (Ändern)
```java
List<String> scoreHistory = new ArrayList<String>();
Difficulty difficulty = Difficulty.DEFAULT; //Kept across restarts, so the last choice is preselected
while (true)
{
	StartScreen startScreen = new StartScreen(PLAYER_COLORS[0], PLAYER_COLORS[1], difficulty);
	Color humanColor = startScreen.waitForChoice(); //Blocks until a planet has been clicked
	difficulty = startScreen.getDifficulty();
	Color aiColor = humanColor.equals(PLAYER_COLORS[0]) ? PLAYER_COLORS[1] : PLAYER_COLORS[0];
	//Reaction time of the AI depends on the level
	EnemyAI enemy = new EnemyAI("Enemy AI", aiColor, difficulty.getMinPause(), difficulty.getMaxPause());
	Tournament tournament = new Tournament(new HumanPlayer(humanColor), enemy, 1, SHIP_SPEED, scoreHistory, difficulty);
	...
```
- Der Kommentar „AI moves every 0.8-1.2 s“ entfällt, da die Pause nun aus der Stufe stammt.

### Schritt 7: Validierung
- `get_errors` für `src/Difficulty.java`, `src/Planet.java`, `src/Game.java`, `src/Tournament.java`, `src/StartScreen.java` und `src/SinglePlayer.java`.
- Kompilierung über den Auto-Build der IDE. Im Projekt gibt es kein Maven.
- Tests werden manuell ausgeführt (Abschnitt 8).

---

## 6. Rand-, Grenz- und Sonderfälle (Edge Cases)

| Nr. | Fall | Erwartetes Verhalten |
|---|---|---|
| E1 | Stufe `Normal` (Standard) | Identisch zum heutigen Spielstand: 1 KI-Planet, 14 neutrale, KI-Pause 800–1200 ms |
| E2 | Klick auf eine Stufen-Schaltfläche | Nur Auswahl, kein Spielstart |
| E3 | Klick nahe Schaltflächen oder Planeten | Trefferbereiche überlappen nicht (Planeten bis y = 320, Schaltflächen ab y = 380) |
| E4 | Restart nach einer Partie | Zuletzt gewählte Stufe ist vorausgewählt |
| E5 | `null` als Stufe an `Tournament` | Fällt auf `Difficulty.DEFAULT` (Normal) zurück |
| E6 | Zusatz-KI-Planet nahe der menschlichen Heimat | Möglich, da die Platzierung zufällig ist (wie Legacy). Keine Sonderregel |
| E7 | `Crazy`: 1 Planet des Menschen gegen 6 KI-Planeten, KI-Pause 400–600 ms | Gewollt. Schnelle Niederlage ist möglich. Balancing erst nach Playtest (F2) |
| E8 | Platzierung mit 16 Planeten, wenig freie Fläche | Die Zufallssuche in `Planet` endet (geprüft in TF-12) |
| E9 | `Tournament.main` (KI gegen KI) | Startkarte wie Normal, KI-Pausen 500 ms. Keine Änderung |
| E10 | Mehrere Partien einer Sitzung mit unterschiedlichen Stufen | Die eine Sitzungsstatistik zählt alle Partien (O7, TF-19) |
| E11 | Heimat des Menschen | 100 Schiffe, Radius 50, Produktion 10, unabhängig von der Stufe |
| E12 | Produktionszeit eines Zusatz-KI-Planeten | Wie bei neutralen Planeten, aus dem Radius (10–40 Ticks) |
| E13 | Moral eines Zusatz-KI-Planeten | Startet bei 50 und nähert sich der Zielmoral über `MoraleModel` an |
| E14 | Stufe bei Spielende | Ohne Einfluss auf `getWinner()` (Sieg = Spieler mit verbleibenden Planeten) |
| E15 | Easy und Normal | Gleiche Startkarte. Der einzige Unterschied ist die Reaktionszeit (1500–2000 ms gegen 800–1200 ms) |
| E16 | Stufenwechsel auf dem Startbildschirm | Wirkt ab der nächsten Partie. Die `EnemyAI` wird je Partie neu mit den Pausen der gewählten Stufe erzeugt |

---

## 7. Betroffene Dateien (Gesamtübersicht)

- [x] `src/Difficulty.java`: **Neu**
- [x] `src/Planet.java`: **Ändern** (Konstruktor `Planet(Game, Player, int)`, `Planet(Game)` delegiert)
- [x] `src/Game.java`: **Ändern** (Feld `difficulty`, `generateNewMap`)
- [x] `src/Tournament.java`: **Ändern** (Konstruktoren, Getter, Zeile im Endscreen)
- [x] `src/StartScreen.java`: **Ändern** (Schaltflächen per Maus, Rückgabe der Stufe)
- [x] `src/SinglePlayer.java`: **Ändern** (Stufe über Restarts, `EnemyAI` mit Pausen der Stufe)
- [x] `src/EnemyAI.java`: Unverändert (Pausen kommen über den bestehenden Konstruktor)
- [x] `src/HumanPlayer.java`, `src/MoraleModel.java`, `src/PlanetNeighbours.java`, `src/SamplePlayer.java`: Unverändert
- [x] `README.md`: **Geändert** (nur Nachtrag, Abschnitt 11). Öffentliche `Player`-API bleibt unverändert
- [x] `legacy/`: Unverändert
- Keine Ressourcen-, Bild- oder Konfigurationsänderungen nötig.

**Konsistenzprüfung (per Suche im Code):**
- `NUM_PLANETS` steht nur in `Game.java` (Z. 24, Z. 184). Die Änderung in `generateNewMap` ist damit vollständig.
- `new StartScreen(` und `waitForChoice` stehen nur in `SinglePlayer.java`. Der Konstruktorwechsel betrifft nur diese Stelle.
- `new Tournament(` steht in `SinglePlayer.java` und in `Tournament.main` (Z. 77). Der alte Konstruktor bleibt erhalten.
- `new Game(` steht nur in `Tournament.java` (Z. 123).
- `new EnemyAI(` steht in `SinglePlayer.java` (Z. 26), in `Tournament.java` (Z. 43, Z. 75–76). Nur die Stelle in `SinglePlayer.java` bekommt die Pausen der Stufe. Die Tournament-Stellen bleiben bei 500 ms.
- Annahme „ein Spieler hat genau eine Heimat“: Keine Stelle in `Game`, `Player`, `HumanPlayer`, `EnemyAI`, `MoraleModel` oder `Tournament` setzt das voraus. `Game.getWinner()` prüft nur, ob ein Spieler noch Planeten besitzt.

---

## 8. Manuelle Teststrategie & Abnahmekriterien

Im Projekt gibt es keine automatisierten Tests. Die Prüfung erfolgt manuell in Eclipse über `SinglePlayer.main`. Die Zusatz-KI-Planeten sind in der KI-Farbe sichtbar. Die Schiffszahl fremder Planeten ist für den Menschen in Normal, Hard und Crazy verborgen. In Easy ist sie sichtbar. Für TF-8 und TF-17 werden die Werte deshalb über eine temporäre `System.out`-Ausgabe oder einen Debugger geprüft. Die Ausgabe wird danach entfernt.

| Nr. | Testfall | Vorbedingung / Schritte | Erwartetes Ergebnis |
|---|---|---|---|
| TF-1 | Startbildschirm | `SinglePlayer` starten | Vier Schaltflächen unter den Planeten, „Normal“ hervorgehoben |
| TF-2 | Auswahl ohne Start | Auf „Hard“ klicken | Auswahl wechselt, das Fenster bleibt offen |
| TF-3 | Easy | Mit Easy starten, Planeten zählen | 1 KI-Planet (Heimat), 14 neutrale, 1 Planet des Menschen |
| TF-4 | Normal | Mit Normal starten, Planeten zählen | 1 KI-Planet (Heimat), 14 neutrale, 1 Planet des Menschen |
| TF-5 | Hard | Mit Hard starten, Planeten zählen | 3 KI-Planeten, 12 neutrale, 1 des Menschen |
| TF-6 | Crazy | Mit Crazy starten, Planeten zählen | 6 KI-Planeten, 9 neutrale, 1 des Menschen |
| TF-7 | Gesamtzahl | TF-3 bis TF-6 | In jeder Stufe 16 Planeten |
| TF-8 | Zusatz-KI-Planeten | Mit Crazy starten, Schiffe und Moral der KI-Planeten temporär ausgeben | 5 Zusatz-Planeten mit 10–29 Schiffen, Moral 50 am Start, danach Annäherung an die Zielmoral. Mit Hard: 2 Zusatz-Planeten |
| TF-9 | Heimat unverändert | Beliebige Stufe, eigene Heimat per Hover prüfen | Bei Spielstart 100 Schiffe, Radius 50 |
| TF-10 | Restart | Mit Hard spielen, „Restart“ klicken | Auf dem Startbildschirm ist Hard vorausgewählt |
| TF-11 | Regression Normal | Mit Normal spielen | Verhalten wie vor der Änderung: gleiche Startkarte, KI-Pausen 800–1200 ms |
| TF-12 | Platzierung Crazy | 20 Partien hintereinander mit Crazy (Restart) | Kein Hänger beim Erzeugen der Karte, alle Partien starten |
| TF-13 | Crazy spielbar | Eine Crazy-Partie bis zum Ende | KI sendet Flotten, das Spiel endet regulär, keine Exception in der Konsole |
| TF-14 | Tournament | `Tournament.main` starten | Zwei Partien KI gegen KI, Startkarte wie Normal (1 KI-Planet), KI-Pausen 500 ms |
| TF-15 | Endscreen | Partie beenden | Zeile „Difficulty: <Stufe>“ sichtbar |
| TF-16 | Kompilierung | `get_errors` auf alle Dateien aus Abschnitt 7, Auto-Build der IDE | Keine Fehler |
| TF-17 | Reaktionszeit je Stufe | Temporäre Ausgabe der Zeitabstände zwischen KI-Zügen in `EnemyAI.makeMove`, je Stufe mindestens 20 Züge | Easy 1500–2000 ms, Normal 800–1200 ms, Hard 600–900 ms, Crazy 400–600 ms |
| TF-18 | Stufenwechsel vor dem Start | Hard wählen, dann per Klick auf Normal wechseln, dann Farbplaneten klicken | Startkarte und Pausen entsprechen Normal. Die letzte Auswahl gilt |
| TF-19 | Sitzungsstatistik | Zwei Partien hintereinander (Restart) mit unterschiedlichen Stufen spielen | Die Statistik zählt beide Partien. Es gibt keine Trennung nach Stufe. Der Endscreen zeigt die Stufe der jeweiligen Partie |

### Abnahmekriterien
- TF-1 bis TF-19 bestanden.
- Keine Exceptions in der Konsole.
- Die Startkarte entspricht Tabelle 2.2 in allen Stufen. Die Pausen entsprechen Tabelle 2.3.
- Die Bedienung funktioniert vollständig mit der Maus.
- `EnemyAI`-Logik, `MoraleModel`, `PlanetNeighbours`, `SamplePlayer` und die `Player`-Signaturen sind unverändert.

---

## 9. Entscheidungen (abgestimmt)

| Nr. | Frage | Entscheidung | Verworfene Alternative |
|---|---|---|---|
| O1 | Hebel der Schwierigkeit | Startkarte (KI-Planeten, neutrale Planeten) und Reaktionszeit der KI je Stufe. Entscheidungsregeln der KI bleiben gleich | Zusätzlich Schiffsanteil oder Moral-Regeln je Stufe |
| O2 | Gesamtzahl der Planeten | 16 beibehalten. Playtests zeigen, ob das für die Kartengröße passt (F2) | 26 wie Legacy |
| O3 | Bezeichnungen und Sprache | Easy, Normal, Hard, Crazy (englisch) | Deutsch: Leicht, Normal, Schwer, Verrückt |
| O4 | Bedienung | Nur Maus. Tastatursteuerung (←/→) ist aus dem Plan entfernt und als Out-of-Scope hinterlegt (2.4) | Maus und ←/→ |
| O5 | Schiffe der Zusatz-KI-Planeten | 10–29, wie im Legacy-Spiel (`rr(10,30)`) | 0–49 wie neutrale Planeten |
| O6 | Stufe am Endscreen | Ja. Zeile „Difficulty: <Stufe>“ (A11, TF-15) | Keine Anzeige |
| O7 | Sitzungsstatistik | Eine Statistik pro Sitzung, ohne Trennung nach Stufe (A13, TF-19) | Getrennte Statistik je Stufe |
| O8 | Zweiter Hebel (Reaktionszeit) | Ja. Die Reaktionszeit ist je Stufe gestaffelt (Tabelle 2.3) | Nur Startkarte |
| O9 | Standardstufe beim Programmstart | Normal (vorausgewählt, `Difficulty.DEFAULT`) | Easy |
| O10 | Pausen-Werte | Easy 1500–2000 ms (nach Spieltest von 1200–1600 ms angehoben), Normal 800–1200 ms (unverändert), Hard 600–900 ms (Vorschlag übernommen), Crazy 400–600 ms (Legacy-Zeit) | Crazy 500–700 ms |

---

## 10. Folgepunkte (nicht Teil dieser Umsetzung)
- **F1** Speicherung der Stufe über Sitzungen hinweg (Konfigurationsdatei). Derzeit gilt die Auswahl nur für die laufende Sitzung.
- **F2** Balancing nach Playtest: Gesamtzahl der Planeten (O2, vorerst 16), Anzahl der Zusatz-Planeten und die Pausen aus Tabelle 2.3. Besonders relevant ist Crazy (E7).
- **F3** Startpositionen wie im Legacy-Spiel (Heimat jeweils an einer Kante). Derzeit zufällig.

---

## 11. Umsetzungsvermerk
**Datum:** 2026-11-16

- Neu: `src/Difficulty.java` (Enum EASY, NORMAL, HARD, CRAZY mit `DEFAULT`, Pausen, `getNeutralPlanets`).
- Geändert: `src/Planet.java` (Konstruktor `Planet(Game, Player, int)`, `Planet(Game)` delegiert), `src/Game.java` (Feld `difficulty`, `generateNewMap` nach Stufe), `src/Tournament.java` (6-Parameter-Konstruktor, Getter, Zeile „Difficulty: …“ im Endscreen), `src/StartScreen.java` (Stufen-Schaltflächen per Maus, `getDifficulty()`, Konstruktor mit Startwert), `src/SinglePlayer.java` (Stufe über Restarts, Pausen der Stufe an `EnemyAI`, Stufe an `Tournament`).
- Unverändert: `EnemyAI`, `MoraleModel`, `PlanetNeighbours`, `SamplePlayer`, `HumanPlayer`, `Player`.
- Nachtrag (Easy zeigt die Schiffszahl fremder Planeten): `Difficulty.isEnemyUnitsVisible()` mit Flag nur bei `EASY`, geprüft in `Game.textColorFor`. In Normal, Hard und Crazy bleibt die Zahl verborgen. `README.md` angepasst (Abschnitt „Game Concepts“).
- Validierung Nachtrag: `get_errors` ohne Befund auf `src/Difficulty.java` und `src/Game.java`. Manuelle Prüfung in Easy steht aus.
- Hinweis: Die Zeile „Difficulty: …“ erscheint auch im Endscreen von `Tournament.main` (dort immer Normal).
- Validierung: `get_errors` ohne Befund auf allen sechs Dateien. `javac` (Java 25) über alle Dateien in `src` ohne Fehler. Die Startkarte je Stufe ist per Zählung aus Tabelle 2.2 geprüft (jeweils 16 Planeten). Manuelle Tests TF-1 bis TF-19 stehen noch aus.
