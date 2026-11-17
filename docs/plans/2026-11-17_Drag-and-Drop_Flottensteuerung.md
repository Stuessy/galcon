# Umsetzungsplan: Flottensteuerung per Drag&Drop mit Mehrfachmarkierung

**Titel:** Flottensteuerung per Drag&Drop statt Point&Click, Mehrfachmarkierung eigener Planeten  
**Status:** Umgesetzt  
**Erstellt:** 2026-11-17  
**Überarbeitet:** 2026-11-17 (O3 als Alternative übernommen, O4 und O5 bestätigt, Doppelklick als neue Anforderung O7, danach O7, O12 und O13 bestätigt, zuletzt F1 Zielhervorhebung als Teil der Umsetzung aufgenommen: A12, O14, O15, danach O15 bestätigt)  
**Bereich:** `src` (überwiegend `HumanPlayer`), `README.md` (Abschnitt „Controls (Single Player)“)  

---

## 1. Ausgangslage und Problemstellung (IST)

### 1.1 Fachlicher Hintergrund
- Im Einzelspiel steuert der Mensch über `HumanPlayer` mit der Maus die Farbe, die er auf dem Startbildschirm gewählt hat.
- Heute gilt Point&Click: Ein Klick auf einen eigenen Planeten wählt ihn aus, ein Klick auf einen beliebigen anderen Planeten schickt die Hälfte seiner Schiffe dorthin (`HumanPlayer.SHIP_SHARE = 0.5`, Z. 24).
- Gewünscht ist Drag&Drop: Flotten werden durch Ziehen von einem Planeten zum Ziel geschickt. Mehrere eigene Planeten können vorab markiert werden und senden dann gemeinsam.

### 1.2 IST im Code

| Stelle | Datei / Zeilen | Befund |
|---|---|---|
| Auswahlzustand | `src/HumanPlayer.java` Z. 31 | `selectedPlanet` hält genau **einen** Planeten |
| Eingabe | `src/HumanPlayer.java` Z. 64–95 | Aktion nur in `mousePressed` (Z. 68–76), dort sofort `handleClick`. Kein `mouseReleased`-Handler. `mouseDragged` (Z. 83–86) ruft nur `handleHover` auf |
| Klicklogik | `src/HumanPlayer.java` Z. 183–213 | Jeder Klick entscheidet sofort: Abbruch, Versand (Z. 205) oder Auswahl (Z. 210) |
| Versand | `src/HumanPlayer.java` Z. 215–223 | `sendHalfOfShips` nutzt `Player.sendFleet(PlanetInfo, …)` (`src/Player.java` Z. 144–159), das den Planeten per `PlanetInfo.equals` sucht |
| Ring | `src/HumanPlayer.java` Z. 98–105 | Weißer Ring um `selectedPlanet`, Radius + `SELECTION_MARGIN` (6) |
| Zeichnung | `src/Game.java` Z. 81–96 | `draw` ruft `p1.drawOverlay(g)` (Z. 88) im Offscreen-Puffer auf. Dieser Hook reicht für Ring, Zielline und Zielhervorhebung |
| Planeten | `src/Planet.java` Z. 100–119 | Planeten sind gefüllte Kreise mit Radius `RADIUS` (12 bis 50 px). Farben: Spieler-Blau bzw. -Orange, neutral Hellgrau (`src/Game.java` Z. 128–145) |
| Threads | `src/Game.java` Z. 70–80 | `actionPerformed` läuft über den Swing-Timer auf dem Event-Dispatch-Thread (EDT). Der Gesten-Zustand wird nur dort gelesen und geschrieben |
| Planet-Versand | `src/Planet.java` Z. 212–231 | `sendFleet(int, Planet)` verweigert `target == this`, deckelt die Menge und startet die Flotte am Planetenrand |
| Klassenkommentar | `src/HumanPlayer.java` Z. 14–19 | Beschreibt Point&Click und muss angepasst werden |
| Endscreen-Listener | `src/Tournament.java` Z. 159–168 | `mousePressed` wirkt nur bei `completed`. Kollidiert im Spielbetrieb nicht |
| Bedienung (Doku) | `README.md` Z. 37–41 | Beschreibt Point&Click |

### 1.3 Ursachenanalyse
Kein Fehler, sondern eine Änderung der Bedienung. Die Steuerung löst beim Drücken aus und speichert nur einen Planeten. Drag&Drop braucht zusätzlich Zustand zwischen Drücken und Loslassen (Startplanet, Bewegung, Zeigerposition) und eine Menge von Absendern statt eines Planeten.

### 1.4 Bewusste Änderungen gegenüber dem IST
- Point&Click-Versand entfällt vollständig. Ein Klick auf einen fremden Planeten hebt bei bestehender Markierung die Markierung auf und sendet nicht mehr (Anforderung A5).
- Im Code heißt die Auswahl künftig Markierung: `selectedPlanets` (Liste) ersetzt `selectedPlanet`.

---

## 2. Fachliche Anforderungen & Zielzustand (SOLL)

### 2.1 Anforderungen
- **A1 Drag&Drop:** Taste auf einem eigenen Planeten drücken, zu einem anderen Planeten ziehen, dort loslassen. Erst beim Loslassen fliegen die Flotten.
- **A2 Markierung:** Ein Klick auf einen eigenen Planeten markiert ihn (weiße Umrandung). Mehrere Planeten können markiert sein.
- **A3 Gemeinsamer Befehl:** Beginnt der Drag auf einem eigenen Planeten S, senden S und alle markierten eigenen Planeten je die Hälfte ihrer Schiffe zum Ziel. S wird dabei zur Markierung addiert (O3). Danach sind alle Markierungen aufgehoben.
- **A4 Zielline:** Während des Drags zeichnet das Spiel eine Linie vom Zentrum der sendenden Planeten zum Mauszeiger.
- **A5 Klick auf Neutrales:** Ein Klick auf leeren Raum oder auf einen nicht eigenen Planeten hebt alle Markierungen auf.
- **A6 Abwahl:** Ein Klick auf einen markierten Planeten hebt dessen Markierung auf. Beim Drag bleibt der Startplanet unverändert, er zählt nicht als Klick (O4).
- **A7 Leerer Raum beim Loslassen:** Endet ein Drag im leeren Raum, passiert nichts. Es werden keine Flotten geschickt und keine Markierungen entfernt.
- **A8 Ziele:** Jeder andere Planet ist ein gültiges Ziel, eigen, neutral oder gegnerisch. Ein Planet schickt nichts an sich selbst.
- **A9 Menge unverändert:** Jeder sendende Planet schickt die Hälfte seiner Schiffe als Flotte (`SHIP_SHARE`, ganzzahlig abgerundet). Die Regel gilt je Quelle einzeln (O2).
- **A10 Keine Spiellogik-Änderung:** KI, Moral, Produktion, Flottenflug und Spielende bleiben unverändert.
- **A11 Doppelklick:** Ein Doppelklick auf einen eigenen Planeten markiert alle eigenen Planeten. Bereits markierte bleiben markiert (O7).
- **A12 Zielhervorhebung:** Während eines Drags von einem eigenen Planeten S wird der Planet unter dem Zeiger grün hervorgehoben, wenn er ein gültiges Ziel ist (Begriff in 2.2, O14, O15). Über leerem Raum oder über S selbst gibt es keine Hervorhebung.

### 2.2 Gesten: Klick, Doppelklick oder Drag

Begriffe:
- **Druckpunkt D** und **Startplanet S**: Ort des Tastendrucks und der Planet darunter (ohne Toleranz, `findPlanetAt(x, y)`). S ist `null` bei leerem Raum.
- **Startbereich:** Kreis um S mit Radius `S.getRadius() + DRAG_START_MARGIN`. Bei leerem Raum ein Kreis mit Radius `DRAG_START_MARGIN` um D.
- **Drag:** Der Zeiger hat den Startbereich bei gedrückter Taste verlassen, oder er liegt beim Loslassen außerhalb. Der Drag-Zustand bleibt bestehen, auch wenn der Zeiger zurückkehrt.
- **Klick:** Loslassen innerhalb des Startbereichs ohne vorheriges Verlassen.
- **Doppelklick:** Klick, dessen Druck den Klickzähler 2 oder mehr hat (`MouseEvent.getClickCount()`), und der auf demselben Planeten liegt wie der vorherige Klick (`lastClickPlanet`). Ein Drag dazwischen zählt nicht (E21).
- **Gültiges Ziel:** Planet Z unter dem Zeiger mit Z ≠ S während eines Drags, dessen Startplanet S eigen ist. Das entspricht der Bedingung von G4 (O14).
- `DRAG_START_MARGIN = 6` px (O5).

Ergebnisregeln:

| Nr. | Geste | Bedingung | Aktion |
|---|---|---|---|
| G1 | Klick | S gehört dem Spieler | Markierung von S umschalten: unmarkiert → markiert (A2), markiert → aufheben (A6) |
| G2 | Doppelklick | S gehört dem Spieler | Alle eigenen Planeten markieren (A11, O7). Bereits markierte bleiben markiert |
| G3 | Klick oder Doppelklick | S gehört einem anderen Spieler oder S ist `null` | Alle Markierungen aufheben (A5, O12) |
| G4 | Drag | S gehört dem Spieler, Loslassen auf Ziel Z (Z ≠ `null`, Z ≠ S) | Quellen Q bilden (2.3). Jeder Planet in Q schickt die Hälfte nach Z. Danach alle Markierungen aufheben (A1, A3, A8, O9). Ist Q leer, passiert nichts |
| G5 | Drag | S gehört dem Spieler, Loslassen im leeren Raum | Nichts (A7) |
| G6 | Drag | S gehört dem Spieler, Loslassen auf S selbst (nach Verlassen) | Nichts. Kein Klick, keine Abwahl, Markierungen bleiben unverändert (O4) |
| G7 | Drag | S gehört einem anderen Spieler oder S ist `null` | Nichts. Markierungen bleiben. Kein Klick (O10) |
| G8 | Andere Taste | Rechte oder mittlere Taste | Ignorieren |

### 2.3 Quellen Q beim Drop auf das Ziel Z
- Q = alle markierten eigenen Planeten plus S (falls S eigen ist), ohne das Ziel Z (O3, O8).
- Beispiel: A, B, C sind markiert, Drag von dem unmarkierten Planeten D auf E. Dann gilt Q = {A, B, C, D}.
- Nach dem Befehl werden alle Markierungen aufgehoben, auch die nicht beteiligten (O9).
- Planeten, die nicht mehr dem Spieler gehören, fallen aus Q.

### 2.4 Out of Scope (bewusst nicht ändern)
- Point&Click-Versand (entfällt vollständig, O1).
- Mengenwahl: Der Anteil bleibt bei 50 % je Quelle (O2). Legacy-Stufen (25/50/75/100 %) sind Folgepunkt F2.
- Umlenkung von Flotten während des Flugs.
- Hover-Statistik eigener Planeten (bleibt unverändert, ist aber während des Drags sichtbar).
- Startbildschirm (`StartScreen`) und Tournament-Modus (KI gegen KI, kein `HumanPlayer`).
- KI-Klassen, `Player`-API, `Planet`, `Fleet`, `MoraleModel`, `PlanetNeighbours`, Spielschleife in `Game`.
- Tastatursteuerung.

---

## 3. Technische Analyse und beteiligte Komponenten

| Komponente | Datei / Pfad | Verantwortung / Art der Änderung |
|---|---|---|
| HumanPlayer | `src/HumanPlayer.java` | **Ändern.** Mehrfachmarkierung, Doppelklick, Gesten-Zustand, `mouseReleased`, Befehl, Ringe, Zielline, Zielhervorhebung, Klassenkommentar |
| Game | `src/Game.java` | Unverändert. `draw` ruft `drawOverlay` bereits im Offscreen-Puffer auf |
| Player | `src/Player.java` | Unverändert. Die Hooks `attachToWindow` und `drawOverlay` genügen |
| Planet | `src/Planet.java` | Unverändert. Genutzt werden `getX`, `getY`, `getRadius`, `getOwner`, `getNumUnits`, `sendFleet(int, Planet)` |
| Tournament | `src/Tournament.java` | Unverändert. Liefert `TOP_BAR_HEIGHT` für die Koordinaten |
| StartScreen, SinglePlayer, EnemyAI, SamplePlayer, Fleet, FleetInfo, MoraleModel, PlanetNeighbours, Difficulty | `src/…` | Unverändert |
| README | `README.md` | **Ändern.** Abschnitt „Controls (Single Player)“ (Z. 37–41) |
| Bestehende Pläne | `docs/plans/2026-11-16_*.md` | Unverändert (historische Dokumente) |

### 3.1 Datenfluss und Threads
1. Maus-Ereignisse (EDT) → Adapter in `HumanPlayer.attachToWindow` → Gesten-Felder (nur EDT).
2. `mouseDragged`: aktualisiert die Zeigerposition und setzt `dragging`, sobald der Startbereich verlassen ist. Ein eigenes `repaint` ist nicht nötig, der Spieltimer zeichnet alle 10 ms neu.
3. `mouseReleased`: wertet die Geste nach Tabelle 2.2 aus. Der Befehl läuft unter `synchronized(game.getAllPlanets())`. Die Sperrreihenfolge bleibt `planets` vor `fleets`. `Planet.sendFleet` sperrt `fleets` intern wie bisher.
4. `drawOverlay` (EDT, über `Game.draw`): zeichnet Ringe, Zielline und Zielhervorhebung. Lesezugriff auf Planeten unter `synchronized(game.getAllPlanets())`.

**Thread-Sicherheit:** Die Gesten-Felder, `selectedPlanets` und `lastClickPlanet` werden nur auf dem EDT benutzt, wie `selectedPlanet` heute. Planetendaten werden nur unter der Sperre gelesen oder geschrieben. Die KI-Threads bleiben unverändert.

---

## 4. Lösungsarchitektur und Designentscheidungen
- **Eingabe in `HumanPlayer`:** Die Steuerung ist Sache des menschlichen Spielers. `Game` und `Tournament` bleiben unberührt.
- **Auswertung erst beim Loslassen:** Während des Ziehens wird nichts geschickt (A1). Das Ziehen ist reine Anzeige.
- **Direkter Aufruf `Planet.sendFleet(int, Planet)`:** `HumanPlayer` hält die Sperre bereits. Der Umweg über `Player.sendFleet` mit der `PlanetInfo`-Suche entfällt.
- **Markierung als `ArrayList<Planet>`:** Höchstens 16 Planeten. `contains` und `remove` reichen. Eine Set-Struktur ist nicht nötig.
- **Doppelklick über den Klickzähler:** `MouseEvent.getClickCount()` liefert Java beim Drücken. Die Zeitgrenze folgt der Systemeinstellung, eine eigene Zeitmessung entfällt. Zusätzlich muss der Planet dem vorherigen Klick entsprechen (`lastClickPlanet`). Ohne diese Prüfung würden zwei schnelle Klicks auf verschiedene Planeten als Doppelklick gelten, je nach Plattform.
- **Zielline in `drawOverlay`:** Wie der Ring wird die Linie in den Offscreen-Puffer gezeichnet. Der Mittelpunkt ist der Mittelwert der Planetenmittelpunkte von Q, ohne den Planeten unter dem Zeiger. Die Linie passt sich dadurch live an das Ziel an.
- **Zielhervorhebung mit derselben Zielbestimmung:** `drawDragFeedback` ermittelt den Planeten unter dem Zeiger einmal und nutzt ihn für Linie und Hervorhebung. Beide können dadurch nicht auseinanderlaufen. Die Hervorhebung erscheint nur während eines Drags, nicht beim normalen Hover.
- **Abstand und Farbe der Hervorhebung:** Die Ringe liegen 10 und 11 px außerhalb des Planetenrands. Der Markierungsring liegt bei 6 px und bleibt so getrennt erkennbar. Grün hebt sich von Weiß (Markierung, Zielline), Blau und Orange (Spieler) sowie Hellgrau (neutral) ab.
- **Gemeinsame Koordinatenumrechnung:** `toGameCoordinates(MouseEvent, Tournament)` ersetzt die doppelte Umrechnung in `mousePressed` und `handleHover`.
- **Stil:** Anonyme innere Klassen, keine Lambdas, englische Code-Kommentare wie im bestehenden `src`-Code. Dieser Plan ist auf Deutsch.

---

## 5. Detaillierte Implementierungsschritte

Die Reihenfolge ist verbindlich. Alle Klassen liegen im Paket `src`.

### Schritt 1: `src/HumanPlayer.java`, Felder
- `selectedPlanet` (Z. 31) entfernen und ersetzen durch:
  ```java
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
  ```
- Imports ergänzen: `java.util.ArrayList`, `java.util.Iterator`, `java.util.List`, und `java.awt.Color`, falls noch nicht vorhanden.

### Schritt 2: `src/HumanPlayer.java`, `attachToWindow` (Z. 64–95)
- `mousePressed`: linke Taste → `handlePress(p.x, p.y, e.getClickCount())` mit Koordinaten aus `toGameCoordinates`.
- `mouseReleased` (neu): linke Taste und `pressActive` → `handleRelease(p.x, p.y)`.
- `mouseDragged`: erst `handleDrag(p.x, p.y)`, dann das bisherige `handleHover(e, window)`.
- `mouseMoved` und `mouseExited` bleiben unverändert.

### Schritt 3: `src/HumanPlayer.java`, Koordinaten-Helfer
```java
//Converts a mouse event into game coordinates, the game area starts below the top bar
private Point toGameCoordinates(MouseEvent e, Tournament window)
{
	Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), window);
	return new Point(p.x, p.y - Tournament.TOP_BAR_HEIGHT);
}
```
- `handleHover` nutzt denselben Helfer statt der eigenen Umrechnung (Z. 123–125).

### Schritt 4: `src/HumanPlayer.java`, Gesten-Methoden (neu)
```java
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
		selectedPlanets.clear();
		lastClickPlanet = null;
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
```
- Die bisherige Methode `handleClick` (Z. 183–213) entfällt. Ihre Aufgaben übernehmen `handleRelease` und `handleClickOn`.
- `sendHalfOfShips` (Z. 215–223) wird vereinfacht. Der Planet hält bereits die Sperre:
  ```java
  //Sends half of the ships of the origin, called under the planets lock
  private void sendHalfOfShips(Planet origin, Planet target)
  {
  	int units = (int) (origin.getNumUnits() * SHIP_SHARE);
  	if (units > 0)
  	{
  		origin.sendFleet(units, target);
  	}
  }
  ```

### Schritt 5: `src/HumanPlayer.java`, `drawOverlay` (Z. 98–118)
- Ring für jede Markierung statt nur für `selectedPlanet`. Nur für eigene Planeten (Filter wie bei `removeLostPlanets`). Die Zeichnung läuft unter `synchronized(currentGame.getAllPlanets())`.
- Zielline und Zielhervorhebung über eine gemeinsame Methode. Sie wirkt nur während eines Drags von einem eigenen Planeten (A4, A12):
  ```java
  //Line from the center of the sending planets to the pointer, and a highlight around a valid target under the pointer
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
  	double sumX = 0;
  	double sumY = 0;
  	for (Planet p: origins)
  	{
  		sumX += p.getX();
  		sumY += p.getY();
  	}
  	g.setColor(Color.WHITE);
  	g.drawLine((int) Math.round(sumX / origins.size()), (int) Math.round(sumY / origins.size()), dragX, dragY);
  	if (under != null && under != pressedPlanet) //Valid target: every planet except the start planet (A12)
  	{
  		g.setColor(TARGET_COLOR);
  		for (int margin = TARGET_MARGIN; margin <= TARGET_MARGIN + 1; margin++) //Two rings, 2 px wide
  		{
  			int r = under.getRadius() + margin;
  			g.drawOval(under.getX() - r, under.getY() - r, 2 * r, 2 * r);
  		}
  	}
  }
  ```
- Aufruf in `drawOverlay` innerhalb von `isGameRunning()`: `drawDragFeedback(g);`
- Die bisherige Methode `drawDragLine` entfällt.
- Die Hover-Anzeige (Z. 106–117) bleibt unverändert.

### Schritt 6: `src/HumanPlayer.java`, Klassenkommentar (Z. 14–19) und Kommentar in Z. 50
```java
/*
 * A player that is controlled by the mouse instead of an AI.
 * Drag from one of your planets onto another planet to send half of its ships there. The target may be own, neutral or enemy.
 * A click on one of your planets marks it. A double click on one of your planets marks all of your planets.
 * If a drag starts on a marked planet, all marked planets send to the target. The marks are cleared after each order.
 * While dragging, a valid target under the pointer is highlighted in green.
 * A click on a marked planet removes its mark. A click on empty space or on another player's planet removes all marks.
 * Moving the mouse over an own planet shows its production, morale and ships (as in the Legacy game).
 */
```
- Z. 50: `//Moves are issued by mouse gestures (see attachToWindow), so there is nothing to compute here`.

### Schritt 7: `README.md`, Abschnitt „Controls (Single Player)“ (Z. 37–41)
```markdown
## Controls (Single Player)
* **Click one of your planets** to mark it. A white ring marks the selection. Click it again to remove the mark.
* **Double-click one of your planets** to mark all of your planets.
* **Drag from one of your planets onto another planet** (own, neutral or enemy) and release the mouse there. Every marked planet, and the planet you dragged from, sends half of its ships to the target. The marks are cleared afterwards.
* While dragging, a line runs from the center of the sending planets to the mouse pointer.
* While dragging, the planet under the pointer is highlighted in green if it is a valid target (every planet except the one you dragged from).
* **Click empty space or a planet that is not yours** to clear all marks.
* Releasing the mouse on empty space, or on the planet you dragged from, does nothing.
* **Hover over one of your planets** to see its stats: production (ships per second) / morale / ships.
```

### Schritt 8: Validierung
- `get_errors` für `src/HumanPlayer.java`.
- Kompilierung über den Auto-Build der Eclipse-IDE. Im Projekt gibt es kein Maven.
- Manuelle Tests nach Abschnitt 8.

---

## 6. Rand-, Grenz- und Sonderfälle (Edge Cases)

| Nr. | Fall | Erwartetes Verhalten |
|---|---|---|
| E1 | Klick auf eigenen, unmarkierten Planeten | Markiert (G1) |
| E2 | Klick auf markierten eigenen Planeten | Nur dessen Markierung entfällt, die übrigen bleiben (G1, A6) |
| E3 | Klick auf neutralen oder gegnerischen Planeten oder leeren Raum, bei Markierungen | Alle Markierungen entfernt (G3) |
| E4 | Drag von markiertem S auf unmarkierten Z | Alle markierten Planeten senden je die Hälfte nach Z. Danach keine Markierung (G4, O9) |
| E5 | Drag von markiertem S auf markierten Z | Z sendet nichts. Die übrigen Markierten senden nach Z (O8) |
| E6 | Drag von unmarkiertem eigenen S, andere Planeten sind markiert | S wird zur Markierung addiert. Alle markierten Planeten inklusive S senden nach Z. Danach keine Markierung (G4, O3, O9) |
| E7 | Drag, Loslassen im leeren Raum | Nichts. Markierungen bleiben, S wird nicht markiert (G5, A7) |
| E8 | Drag, Rückkehr auf S und Loslassen | Nichts. Kein Klick, keine Abwahl, Markierungen bleiben unverändert (G6, O4) |
| E9 | Drag beginnt auf fremdem Planeten oder im leeren Raum | Nichts, Markierungen bleiben (G7, O10) |
| E10 | Klick mit leichtem Verrutschen (unter 6 px) | Wird als Klick gewertet, kein Versand (O5) |
| E11 | Rechte Maustaste bei Markierungen | Keine Wirkung (G8) |
| E12 | Markierter Planet wird während des Drags erobert | Wird beim Loslassen aus Q entfernt. Ist Q danach leer, passiert nichts |
| E13 | Markierter Planet wird ohne Drag von der KI erobert | Sein Ring verschwindet bei der nächsten Zeichnung. Die Markierung wird beim nächsten Loslassen entfernt |
| E14 | Spielende während Markierung oder Drag | Markierungen, Klickhistorie und Gesten-Zustand werden verworfen. Keine Flotten, keine Zielline, keine Hervorhebung |
| E15 | Zeiger verlässt das Fenster während des Drags | Der Drag bleibt aktiv, Java liefert die Ereignisse weiter. Loslassen außerhalb zählt als leerer Raum (G5) |
| E16 | Doppelklick auf eigenen, unmarkierten Planeten | Der erste Klick markiert S, der zweite markiert alle eigenen Planeten (G2, O7) |
| E17 | Doppelklick auf eigenen, bereits markierten Planeten | Der erste Klick hebt die Markierung von S auf, der zweite markiert alle eigenen Planeten. Ergebnis: alle eigenen markiert (G2, O7) |
| E18 | Doppelklick auf eigenen Planeten bei anderen Markierungen | Alle eigenen Planeten sind markiert, die bisherigen Markierungen bleiben (O7) |
| E19 | Doppelklick auf fremden Planeten oder im leeren Raum | Wirkt wie ein Klick auf diese Stelle: alle Markierungen werden entfernt (G3, O12) |
| E20 | Zwei schnelle Klicks auf verschiedene Planeten | Kein Doppelklick, da der Planet nicht dem vorherigen Klick entspricht (`lastClickPlanet`). Zwei normale Klicks (O7) |
| E21 | Doppelklick mit einem Drag dazwischen | Der Drag zählt nicht als Klick und setzt `lastClickPlanet` zurück. Kein Doppelklick |
| E22 | Quelle mit einem Schiff | Menge 0, es fliegt nichts. Der Befehl gilt trotzdem, die Markierungen werden aufgehoben (O9) |
| E23 | Ziel ist ein eigener Planet | Verstärkung erlaubt (O11) |
| E24 | Zielplanet liegt unter dem Zeiger während des Drags | Die Zielline geht vom Zentrum ohne diesen Planeten aus. Er sendet beim Loslassen nicht. Er wird grün hervorgehoben (O8, O13, O14) |
| E25 | Q ist beim Zeichnen leer (nur S, der Zeiger liegt auf S) | Keine Zielline |
| E26 | Hover-Statistik während des Drags | Bleibt wie bisher. Hover und Drag beeinflussen sich nicht |
| E27 | Zeiger über gegnerischem, neutralem oder eigenem Planeten während eines Drags | Der Planet wird grün hervorgehoben (A12, O14, O15) |
| E28 | Zeiger über dem Startplaneten S während eines Drags | Keine Hervorhebung. Die Zielline folgt E24 und lässt S aus (A12, O14) |
| E29 | Zeiger über leerem Raum während eines Drags | Keine Hervorhebung (A12) |
| E30 | Drag startet auf fremdem Planeten oder im leeren Raum | Keine Hervorhebung und keine Zielline (G7) |
| E31 | Zeiger über einem Planeten ohne gedrückte Taste (normaler Hover) | Keine Hervorhebung. Sie gilt nur während eines Drags (A12) |
| E32 | Zielplanet wird während des Drags erobert | Die Hervorhebung bleibt. Sie hängt nicht vom Besitzer ab, nur der Startplanet S ist ausgeschlossen (O14) |

---

## 7. Betroffene Dateien (Gesamtübersicht)

- [ ] `src/HumanPlayer.java`: **Ändern** (Felder, Gesten, Doppelklick, Befehl, Zeichnung inkl. Zielhervorhebung, Kommentar, Koordinaten-Helfer)
- [ ] `README.md`: **Ändern** (Abschnitt „Controls (Single Player)“)
- [ ] `src/Game.java`: Unverändert (`drawOverlay`-Aufruf bereits vorhanden)
- [ ] `src/Player.java`: Unverändert (Hooks genügen, Kommentar „see HumanPlayer“ bleibt gültig)
- [ ] `src/Planet.java`, `src/Fleet.java`, `src/Tournament.java`, `src/SinglePlayer.java`, `src/StartScreen.java`: Unverändert
- [ ] `src/EnemyAI.java`, `src/SamplePlayer.java`, `src/MoraleModel.java`, `src/PlanetNeighbours.java`, `src/Difficulty.java`, `src/Assets.java`: Unverändert
- [ ] `legacy/`, `docs/plans/` (bestehende Pläne): Unverändert
- Keine Ressourcen-, Bild- oder Konfigurationsänderungen nötig. Keine Datenbank.

**Konsistenzprüfung (Textsuche per PowerShell `Select-String`, Ergebnis):**
- `selectedPlanet`: nur in `src/HumanPlayer.java` (Z. 31, 100–104, 187–210). Alle Stellen werden ersetzt.
- `handleClick`, `sendHalfOfShips`, `SHIP_SHARE`: nur in `src/HumanPlayer.java`. `SHIP_SHARE` wird von keiner anderen Klasse gelesen.
- `drawOverlay`: Hook in `src/Player.java` Z. 25, Aufrufe in `src/Game.java` Z. 88 und Z. 92. Die Signatur bleibt gleich.
- Klick-Beschreibungen: `src/HumanPlayer.java` Z. 15–17, 50, 63 und `README.md` Z. 38–40. Alle werden angepasst.
- `mouseReleased` existiert in `src` bisher nur im Startbildschirm (dort nur `mousePressed`). Keine Kollision.
- Hinweis: Die Textsuche des Editors lieferte bei dieser Prüfung keine Treffer, obwohl Treffer vorhanden sind. Deshalb wurde mit PowerShell `Select-String` geprüft. Die Suche ist vor der Umsetzung zu wiederholen.

---

## 8. Manuelle Teststrategie & Abnahmekriterien

Im Projekt gibt es keine automatisierten Tests. Die Prüfung erfolgt manuell in Eclipse über `SinglePlayer.main`.

| Nr. | Testfall | Vorbedingung / Schritte | Erwartetes Ergebnis |
|---|---|---|---|
| TF-1 | Einzelmarkierung | Klick auf einen eigenen Planeten | Weiße Umrandung |
| TF-2 | Mehrfachmarkierung | Klick auf drei eigene Planeten A, B, C | Drei weiße Ringe |
| TF-3 | Abwahl | Klick auf markierten Planeten B | Ring um B weg, A und C bleiben |
| TF-4 | Klick auf fremden Planeten | A und C markiert, Klick auf einen neutralen Planeten | Alle Ringe weg, keine Flotte |
| TF-5 | Klick auf leeren Raum | Markierungen gesetzt, Klick in den leeren Raum | Alle Ringe weg |
| TF-6 | Drag von markiertem Planeten | A, B, C markiert. Drag von A auf gegnerischen Planeten D, loslassen | Von A, B, C fliegen Flotten mit je der Hälfte der Schiffe zu D. Markierungen weg |
| TF-7 | Drag ohne Markierung | Drag von eigenem, unmarkiertem A auf neutralen Planeten B | Nur A sendet die Hälfte. Keine Markierung danach |
| TF-8 | Zielline bei Markierung | Drag von markiertem A (mit B, C markiert), Zeiger langsam bewegen | Linie vom Mittelpunkt von A, B, C zum Zeiger, folgt der Bewegung |
| TF-9 | Zielline ohne Markierung | Drag von unmarkiertem A | Linie von A zum Zeiger |
| TF-10 | Drop im leeren Raum | Drag von A in den leeren Raum, loslassen | Keine Flotte, Markierungen bleiben |
| TF-11 | Drop zurück auf Start | Drag von markiertem A, zurück auf A, loslassen. Danach Drag von unmarkiertem D, zurück auf D, loslassen | Keine Flotte. A bleibt markiert, D bleibt unmarkiert. Kein Klick, keine Abwahl (O4) |
| TF-12 | Verrutschen beim Klick | Markierten Planeten anklicken, Maus 2 bis 3 px bewegen, loslassen | Markierung entfernt, kein Versand |
| TF-13 | Markiertes Ziel | A und B markiert, Drag von A auf B | Nur A sendet nach B. B sendet nichts |
| TF-14 | Drag von fremdem Planeten | Drag von KI-Planet auf eigenen Planeten | Nichts. Markierungen bleiben |
| TF-15 | Rechte Maustaste | Rechtsklick auf markierten Planeten | Keine Wirkung |
| TF-16 | Eroberung markierter Planet | Markierten Planeten durch KI-Flotte erobern lassen | Ring verschwindet, Planet ist nicht mehr Quelle |
| TF-17 | Spielende mit Markierung | Markierungen setzen, Partie bis zum Ende spielen | Ringe und Linie weg, Endscreen wie bisher, Restart funktioniert |
| TF-18 | Hover-Statistik | Über eigenem Planeten halten, während ein Drag läuft | Statistik wie bisher sichtbar |
| TF-19 | Regression Startbildschirm | Schwierigkeit wählen, Farbe wählen, Restart | Unverändert |
| TF-20 | Kompilierung | `get_errors` auf `src/HumanPlayer.java`, Eclipse-Auto-Build | Keine Fehler |
| TF-21 | Stabilität | Fünf Minuten Spiel mit vielen Drags und Klicks | Keine Exception in der Konsole |
| TF-22 | Drag von unmarkiertem Planeten bei Markierungen | B und C markiert. Drag von unmarkiertem A auf D, loslassen | A, B und C senden je die Hälfte nach D. Danach keine Markierung (O3) |
| TF-23 | Zielline bei unmarkiertem Start mit Markierungen | B und C markiert, Drag von unmarkiertem A, Zeiger bewegen | Linie vom Mittelpunkt von A, B, C zum Zeiger (O3) |
| TF-24 | Doppelklick auf unmarkierten eigenen Planeten | Keine Markierung gesetzt, Doppelklick auf A | Alle eigenen Planeten markiert (O7) |
| TF-25 | Doppelklick auf markierten eigenen Planeten | B und C markiert, Doppelklick auf B | Alle eigenen Planeten markiert, B, C bleiben markiert (O7) |
| TF-26 | Doppelklick auf fremden Planeten oder leeren Raum | A und C markiert, Doppelklick auf einen neutralen Planeten | Alle Markierungen weg, keine Flotte (O12) |
| TF-27 | Zwei schnelle Klicks auf verschiedene Planeten | Klick auf A, danach schnell Klick auf B (beide eigen) | Kein Doppelklick. A und B sind markiert, übrige eigene Planeten nicht (O7) |
| TF-28 | Klick, Drag, schneller Klick | A markiert. Drag von B auf C, danach schnell Klick auf A | A und B senden nach C. Danach markiert der Klick nur A, kein Doppelklick (O3, E21) |
| TF-29 | Zielhervorhebung über gegnerischem Planeten | Drag von eigenem A, Zeiger über gegnerischen Planeten D, dann D verlassen | Grüner Doppelring um D, solange der Zeiger auf D liegt. Danach weg (A12) |
| TF-30 | Zielhervorhebung über eigenem Planeten | Drag von A auf eigenen Planeten E | Grüner Doppelring um E (O11, O14) |
| TF-31 | Zielhervorhebung über markiertem Planeten | A und B markiert, Drag von A über B | Grüner Ring um B. Der weiße Markierungsring um B bleibt sichtbar und ist unterscheidbar (O8, O15) |
| TF-32 | Keine Hervorhebung über S oder leerem Raum | Drag von A, Zeiger zurück auf A, dann in den leeren Raum | Kein grüner Ring (O14) |
| TF-33 | Keine Hervorhebung ohne Drag | Nur den Mauszeiger über gegnerischem Planeten bewegen, keine Taste gedrückt | Kein grüner Ring (A12) |
| TF-34 | Keine Hervorhebung bei fremdem Start | Drag von KI-Planet über eigenen Planeten | Kein grüner Ring, keine Linie (G7) |
| TF-35 | Lesbarkeit der Hervorhebung | Drag über den kleinsten und den größten Planeten auf der Karte | Ringe liegen außerhalb des Planetenrands, Planetenzahl bleibt lesbar (O15) |

### Abnahmekriterien
- TF-1 bis TF-35 bestanden.
- Keine Exceptions in der Konsole.
- Ein Versand per Klick ist nicht mehr möglich.
- Keine Änderung an `Game`, `Planet`, `Fleet`, `Player`, `EnemyAI`, `SamplePlayer`, `Tournament` und `StartScreen`.
- `README.md` beschreibt die neue Bedienung einschließlich Doppelklick und Zielhervorhebung.

---

## 9. Entscheidungen

Stand 2026-11-17: Alle Entscheidungen sind bestätigt.

| Nr. | Frage | Entscheidung | Status |
|---|---|---|---|
| O1 | Bedienung | Drag&Drop ersetzt Point&Click vollständig. Versand per Klick entfällt | Bestätigt |
| O2 | Schiffsanteil je Quelle | Hälfte (`SHIP_SHARE` 0,5) je Quelle, als Flotte | Bestätigt |
| O3 | Drag von unmarkiertem eigenen Planeten bei bestehenden Markierungen | Der Startplanet wird zur Markierung addiert. Alle markierten Planeten inklusive Startplanet senden. Danach werden alle Markierungen aufgehoben (O9) | Bestätigt (Alternative übernommen) |
| O4 | Loslassen auf dem Startplaneten nach dem Verlassen | Nichts. Zählt nicht als Klick, bewirkt keine Abwahl. Markierungen bleiben unverändert | Bestätigt |
| O5 | Unterscheidung Klick und Drag | Startbereich mit 6 px Toleranz (`DRAG_START_MARGIN`) | Bestätigt |
| O6 | Doppelklick, frühere Fassung | Zwei Umschaltungen. Durch O7 ersetzt | Entfällt |
| O7 | Doppelklick auf eigenen Planeten | Alle eigenen Planeten werden markiert, auch wenn sie bereits markiert sind. Als Doppelklick zählt nur ein doppelter Klick auf denselben Planeten (`lastClickPlanet`, E20) | Bestätigt |
| O8 | Markierter Zielplanet | Er sendet nichts an sich selbst. Die übrigen Markierten senden | Bestätigt |
| O9 | Aufheben nach dem Befehl | Nach jedem ausgeführten Befehl werden alle Markierungen aufgehoben, auch nicht beteiligte | Bestätigt |
| O10 | Drag von fremdem Planeten oder aus dem leeren Raum | Nichts. Markierungen bleiben, kein Klick | Bestätigt |
| O11 | Ziel ist eigener Planet | Erlaubt (Verstärkung) | Bestätigt |
| O12 | Doppelklick auf fremden Planeten oder im leeren Raum | Alle Markierungen werden aufgehoben (G3) | Bestätigt |
| O13 | Zielline-Stil | Weiß, 1 px, durchgezogen, vom Zentrum der sendenden Planeten zum Zeiger. Der Planet unter dem Zeiger zählt nicht mit | Bestätigt |
| O14 | Gültiges Ziel bei der Zielhervorhebung | Jeder Planet unter dem Zeiger außer dem Startplaneten, bei einem Drag von einem eigenen Planeten. Eigene, neutrale und gegnerische Planeten sowie markierte Planeten sind gültig | Bestätigt (abgeleitet aus A8, O8, O11) |
| O15 | Darstellung der Zielhervorhebung | Zwei grüne Ringe (`Color.GREEN`) bei Radius + 10 und + 11 px. Der Markierungsring bei +6 px bleibt getrennt | Bestätigt |

---

## 10. Folgepunkte (nicht Teil dieser Umsetzung)
- **F1** Zielhervorhebung: in diese Umsetzung übernommen (A12, O14, O15).
- **F2** Schiffsanteil wählbar wie in Legacy (25 / 50 / 75 / 100 %), z. B. über Tasten. Derzeit fest 50 %.
- **F3** Kurze Bedienhilfe im Startbildschirm oder im Fenster, da die Drag-Bedienung nicht selbsterklärend ist.

---

## 11. Umsetzungsvermerk
**Datum:** 2026-11-17

Geänderte Dateien:
- `src/HumanPlayer.java`: Felder (`selectedPlanets`, Gesten-Zustand), `mousePressed`/`mouseReleased`/`mouseDragged`, Gesten-Logik (Klick, Doppelklick, Drag, Befehl), `drawOverlay` mit Ringen, Zielline und Zielhervorhebung (`drawDragFeedback`), Klassenkommentar, `toGameCoordinates`. Alte Methoden `handleClick` und `drawDragLine` entfernt. Abweichung zu Schritt 5: `drawDragLine` existierte im IST nicht.
- `README.md`: Abschnitt „Controls (Single Player)“ auf Drag&Drop, Mehrfachmarkierung und Doppelklick umgestellt.

Ergänzung zu E14: Ist die Partie beendet, verwirft `drawOverlay` Markierungen und Gesten-Zustand (`clearGestureState`).

Validierung:
- `get_errors` auf `src/HumanPlayer.java`: keine Fehler.
- `javac` über alle `src/*.java` in ein Temp-Verzeichnis: fehlerfrei.
- Textsuche per PowerShell `Select-String` in `*.java` und `*.md`: `selectedPlanet` (Singular), `handleClick` (in `HumanPlayer`) und `drawDragLine` sind in `src` nicht mehr vorhanden. Verbleibende `handleClick`-Treffer gehören zu `StartScreen`.
- Manuelle Tests TF-1 bis TF-35 sind in Eclipse noch durchzuführen.

**Nachtrag (2026-11-17, Startplanet-Ring):** Beginnt ein Drag auf einem eigenen, unmarkierten Planeten, erhält dieser während des Drags
einen weißen Ring (`drawMarkRing` in `drawDragFeedback`). Die Markierung selbst wird nicht gesetzt. Bricht der Drag ohne Befehl ab
(E7, E8), bleibt der Planet daher unmarkiert (O4). Der Befehl selbst ist unverändert (O3). Neuer Testfall TF-36: Drag von unmarkiertem A
auf D, Zeiger über A bewegen: weißer Ring um A während des Drags. Loslassen auf A: kein Ring danach (O4).

**Nachtrag 2 (2026-11-17, Linien pro Quelle):** Die Zielline ist nicht mehr eine Linie vom Mittelpunkt aller Quellen, sondern je sendendem Planeten
eine eigene weiße Linie vom Mittelpunkt dieses Planeten zum Mauszeiger (`drawDragFeedback`). Zu den Quellen zählen die markierten eigenen
Planeten plus der Startplanet, ohne den Planeten unter dem Zeiger (O3, O8, O13). Bei drei Quellen werden drei Linien gezeichnet.
Die Zielhervorhebung und der Befehl bleiben unverändert.
