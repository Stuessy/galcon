# Umsetzungsplan: Gegner-KI und Moral-System (Legacy-Portierung)

**Titel:** Gegner-KI und Moral-System aus dem Legacy-Code in das Java-Spiel übernehmen  
**Status:** Umgesetzt  
**Erstellt:** 2026-11-16  
**Bereich:** `src` (Modell: `Planet`, `Fleet`, `Game`; KI: `Player`; Spielmodi: `SinglePlayer`, `Tournament`)  

---

## 1. Ausgangslage und Problemstellung (IST)

### 1.1 Fachlicher Hintergrund
- Im Modus `SinglePlayer` steuert der Mensch per Maus (`HumanPlayer`) gegen eine KI.
- Im Modus `Tournament` treten zwei KIs gegeneinander an.
- Die heutige KI ist `SamplePlayer`. Sie schickt aus Planeten mit mehr als 20 Schiffen je 2 Schiffe an einen **zufälligen** gegnerischen oder neutralen Planeten (`SamplePlayer.makeMove`, Z. 55–68). Ziele sind nicht auf Nachbarn beschränkt.
- Die Produktion ist konstant: Ein besessener Planet erhöht `numUnits` alle `PRODUCTION_TIME` Ticks, unabhängig von Lage oder Zustand (`Planet.update`, Z. 105–112). Es gibt kein Moral-Konzept.

### 1.2 Legacy-Referenz (Python/pygame, `legacy/`)
- Moral-Berechnung und Angleichung: `legacy/planet.py`, Methode `loop` (Z. 184–217)
- Gegner-KI: `legacy/level.py`, Methode `computer` (Z. 429–456)
- Nachbarschaft (`near`): `legacy/level.py`, Initialisierung (Z. 104–121)
- Anflug-Zähler (`attacks`): `legacy/level.py`, `_attack` (Z. 425) und Treffer (Z. 274–277)
- Spielregel-Erklärung: `legacy/menu.py`, Tutorial #3 (Z. 215–228)
- Frame-Rate: `legacy/cnst.py`, `FPS = 32`

### 1.3 Technische Ursachenanalyse (IST-Stellen)
Kein Fehler, sondern fehlendes Feature. Relevante Stellen im Java-Code:
- `Planet.java` Z. 105–112: `update()` produziert nur über `updateCnt % PRODUCTION_TIME`. Keine Moral.
- `Planet.java` Z. 119–134: `invade()` wechselt den Eigentümer, ohne weitere Zustände zu behandeln.
- `Fleet.java` Z. 58–73: Jedes Schiff einer Flotte invadiert einzeln bei Ankunft.
- `Game.java` Z. 203–220: `updateAllFleets()` sperrt nur `fleets`. Dort ruft `invade()` `numUnits` auf.
- `Game.java` Z. 221–230: `updateAllPlanets()` sperrt nur `planets`.
- `Player.java` Z. 122–134: `sendFleet` findet den Planeten über `PlanetInfo.equals` (Anzahl, Position, Radius, Besitzer). Ändert sich `numUnits` zwischen Snapshot und Senden, geschieht **still nichts**.
- Nebenläufigkeit: Der Swing-Timer (EDT, `Game.actionPerformed`) schreibt `numUnits`. KI-Threads (`Player.run` Z. 29–35 → `makeMove`) lesen und senden. Die KI-Sendungen sind nicht mit den Sperren des Timers synchronisiert. Gefahr: verlorene Updates.

---

## 2. Fachliche Anforderungen & Zielzustand (SOLL)

### 2.1 Anforderungen
- **A1 Moral pro Planet:** Ganzzahl 0–100. Start 50, Heimatplaneten der Spieler 100.
- **A2 Zielmoral:** Jeder Tick wird aus dem Schiffsverhältnis im Umfeld berechnet (Formel in Abschnitt 5, Schritt 2).
- **A3 Angleichung:** Die Moral bewegt sich in Einzelschritten zur Zielmoral. Sinken: 1 Punkt alle 6 Ticks (≈ 16 Punkte/s). Steigen: 1 Punkt alle 25 Ticks (4 Punkte/s).
- **A4 Produktion:** Nur besessene Planeten produzieren. Die Rate ist proportional zur Moral (Moral 100 = bisherige Rate, Moral 50 = halbe Rate).
- **A5 Nachbarschaft:** Je Planet die 5 nächsten Planeten (Abstand der Mittelpunkte) plus die symmetrische Ergänzung. Einmal pro Spiel bestimmt.
- **A6 Gegner-KI:** Eigene Unterklasse `EnemyAI` von `Player`. Sie entscheidet je Aufruf für jeden eigenen Planeten nach Abschnitt 2.2 und schickt Schiffe nur an Nachbarplaneten.
- **A7 Beide Modi:** Die Moral ist eine Spielregel in `Game` und `Planet`, keine Option der KI. Sie gilt in `SinglePlayer` und `Tournament`, auch für `SamplePlayer`.
- **A8 KI-Einsatz:**
  - `SinglePlayer`: `EnemyAI` als Gegner, Pause 500 ms (Legacy-Takt).
  - `Tournament`: Zwei `EnemyAI`, Pause 500 ms (Legacy-Takt).
- **A9 Modularität:** Drei eigenständige Klassen: `MoraleModel`, `PlanetNeighbours`, `EnemyAI`. `MoraleModel` und `PlanetNeighbours` enthalten keine Swing- oder Thread-Logik.
- **A10 Öffentliche API:** Die Methodensignaturen von `Player` bleiben unverändert. `PlanetInfo` erhält den Lesezugriff `getMorale()`, damit KI-Entwickler die Moral auslesen können. `PlanetInfo.getProductionRate()` berücksichtigt die Moral, damit die angezeigte Rate der tatsächlichen Produktion entspricht (0.0 bei neutralen Planeten, siehe O5). `README.md` dokumentiert `getMorale()` und `getProductionRate()`.
- **A11 Thread-Sicherheit:** Jeder Zugriff auf Planet- und Flottenzustand läuft unter den Sperren aus Abschnitt 3.1.
- **A12 Anzeige:** Im Modus `SinglePlayer` zeigt der Mensch beim Hover über einem **eigenen** Planeten die Zeile „Produktion / Moral / Schiffe“ (wie Legacy). Bei fremden oder neutralen Planeten erfolgt keine Anzeige, da diese Information geheim ist.

### 2.2 Entscheidungsregeln der Gegner-KI
Ausgewertet je eigenem Planeten (`origin`) und je Nachbarziel (`target`) aus `origin.near` in zufälliger Reihenfolge (neu gemischt je Aufruf).

| Nr. | Bedingung (in dieser Reihenfolge) | Aktion |
|---|---|---|
| R1 | `origin.numUnits < 10` | Abbruch für diesen Planeten (`break`) |
| R2 | `origin.realMorale < 25` | Abbruch für diesen Planeten (`break`) |
| R3 | `target` gehört der KI und `origin.realMorale - target.realMorale > 10` | Senden: `origin.numUnits / 4` (ganzzahlig) |
| R4 | `target` gehört der KI und `origin.realShips - target.realShips > 10` | Senden: `(origin.realShips - target.realShips) / 2` |
| R5 | `target` gehört der KI (sonst) | Nichts, weiter mit dem nächsten Ziel |
| R6 | `target` ist neutral oder gegnerisch | Senden: `origin.numUnits * 3 / 4` |

- R1 und R2 werden vor jedem Ziel neu geprüft, weil sich `numUnits` durch Sendungen ändert (wie im Legacy-Code).
- Eine berechnete Menge ≤ 0 wird nicht gesendet.
- Vergleiche sind strikt (`>`), also löst eine Differenz von genau 10 nicht aus.

### 2.3 Out of Scope (bewusst nicht ändern)
- Schwierigkeitsgrade (easy/medium/hard/crazy), Tutorial und reduzierte Startschiffe aus dem Legacy-Menü.
- Anzeige der Moral für fremde und neutrale Planeten (bleibt geheim).
- Flottenumlenkung durch die Gegner-KI (`changeFleetTarget`). Die Legacy-KI nutzt sie nicht.
- Legacy-Tastensteuerung für Prozentangriffe. Der Mensch bleibt bei `HumanPlayer` (halbe Flotte).
- Produktion neutraler Planeten. Legacy produziert dort, Java nicht. Beibehaltung siehe Abschnitt 4, Entscheidung O5 und Folgepunkt F1 (Abschnitt 10).
- Legacy-Zufallsphase je Planet (`frame % 16`). Ersatz: Pausentakt des Player-Threads.
- Änderungen an `SamplePlayer` (bleibt Beispiel für KI-Entwickler).
- Sound, Grafik, Partikel.

---

## 3. Technische Analyse und beteiligte Komponenten

| Komponente | Datei / Pfad | Verantwortung / Art der Änderung |
|---|---|---|
| MoraleModel | `src/MoraleModel.java` | **Neu.** Konstanten, Phase 1 (Schiffsverhältnis, Zielmoral), Phase 2 (Angleichung) |
| PlanetNeighbours | `src/PlanetNeighbours.java` | **Neu.** Ermittelt `near` je Planet einmal pro Spiel |
| EnemyAI | `src/EnemyAI.java` | **Neu.** Gegner-KI als `Player`-Unterklasse, Regeln R1–R6 |
| Planet | `src/Planet.java` | **Ändern.** Neue Felder, `update()`, `invade()`, Getter/Setter |
| Fleet | `src/Fleet.java` | **Ändern.** Getter `getTarget()` und `getNumUnits()` |
| Game | `src/Game.java` | **Ändern.** Nachbarschaft in `generateNewMap`, Sperren in `updateAllFleets` und `updateAllPlanets`, Aufruf `MoraleModel.prepare` |
| Player | `src/Player.java` | **Ändern.** Nur Sperren in `sendFleet` und `changeFleetTarget` |
| HumanPlayer | `src/HumanPlayer.java` | **Ändern.** Sperre in `handleClick`, Hover-Anzeige eigener Planeten (Mausbewegung, `drawOverlay`) |
| Tournament | `src/Tournament.java` | **Ändern.** Feld `AIs` mit zwei `EnemyAI` |
| SinglePlayer | `src/SinglePlayer.java` | **Ändern.** `SamplePlayer` wird durch `EnemyAI` ersetzt |
| SamplePlayer | `src/SamplePlayer.java` | Unverändert |
| PlanetInfo | in `src/Planet.java` | **Ändern.** Feld `MORALE`, Getter `getMorale()`, `getProductionRate()` mit Moral, `equals()` unverändert |
| FleetInfo | in `src/Fleet.java` | Unverändert |
| Legacy-Code | `legacy/` | Unverändert (nur Referenz) |
| README | `README.md` | **Ändern.** `getMorale()` im Abschnitt „PlanetInfo Class“ ergänzen, `getProductionRate()` beschreiben |

### 3.1 Datenfluss und Sperren je Tick
1. **Swing-Timer (EDT, `Game.FRAME_TIME` = 10 ms)**, `updateAllFleets()`: Sperre `planets`, darin Sperre `fleets`. Flotten fliegen weiter, ankommende Schiffe rufen `invade()` auf.
2. **`updateAllPlanets()`**: Sperre `planets`, darin Sperre `fleets`. `MoraleModel.prepare(planets, fleets)` berechnet je Planet Zielmoral und Schiffsverhältnis. Danach `p.update()` je Planet.
3. **KI-Thread** (`Player.run` → `makeMove`): Zuerst Pause. Dann Sperre `planets`, Entscheidungen, `Planet.sendFleet` (intern `addFleet` mit Sperre `fleets`).

**Lock-Reihenfolge überall: `planets` vor `fleets`. Niemals umgekehrt.**

---

## 4. Lösungsarchitektur und Designentscheidungen
- **Moral als Spielregel im Modell:** Die Regeln liegen in `MoraleModel` und `Planet`, nicht in der KI. Sie gelten für Mensch und KI gleich und in beiden Modi.
- **Zweiphasige Aktualisierung:** Phase 1 berechnet alle Zielwerte aus demselben Zustand. Phase 2 bewegt die Moral und produziert. Legacy rechnet in-place, die Abweichung ist minimal.
- **Produktion als Bruchteil-Akkumulator:** `productionProgress += morale / 100.0`. Bei Moral 100 entspricht das exakt der bisherigen Rate. Abweichung: Die erste Einheit kommt nach `PRODUCTION_TIME` Ticks statt sofort bei `updateCnt == 0`.
- **Produktionsrate für KI-Entwickler:** `PlanetInfo.getProductionRate()` = `MORALE / 100.0 / PRODUCTION_TIME` Einheiten pro Tick. Das ist dieselbe Rate wie der Akkumulator in `Planet.update()`. Moral 50 halbiert sie. Bei neutralen Planeten (`OWNER == null`) ist die Rate 0.0, da dort nicht produziert wird (O5).
- **Zeitbasis:** Legacy-Werte gelten pro Frame bei 32 FPS (≈ 31 ms). In 10-ms-Ticks: Sinken alle 6 Ticks (≈ 60 ms), Steigen alle 25 Ticks (250 ms).
- **Anflug-Zählung:** `Fleet.numUnits` umfasst alle Schiffe der Flotte, auch noch nicht gestartete. Das entspricht Legacy: `attacks` zählt ab dem Befehl.
- **Besitz-Vergleich:** Gleicher Eigentümer zählt als eigen. Bei neutralen Planeten (`null`) zählen neutrale Nachbarn als eigen, Planeten mit Eigentümer als gegnerisch. Das entspricht Legacy.
- **Neutrale Planeten:** Moral wird berechnet (wie Legacy), produziert wird nicht. Das weicht vom Legacy-Code ab: `legacy/planet.py`, `loop`, produziert auch auf neutralen Planeten, ohne Besitzerprüfung. Java bleibt vorerst bei der bisherigen Regel (Entscheidung O5). Bei Eroberung bleibt die Moral erhalten, `productionProgress` wird auf 0 gesetzt.
- **`EnemyAI` liest `Planet`-Objekte direkt** (wie `HumanPlayer`) und sendet über `Planet.sendFleet` unter Sperre. Grund: `Player.sendFleet` mit `PlanetInfo`-Gleichheit schlägt still fehl, sobald sich `numUnits` ändert.
- **Zustandslose KI:** `EnemyAI` speichert nichts zwischen den Aufrufen. In `Tournament` laufen die Threads über mehrere Partien, daher sind keine Resets nötig.
- **Pause vor Guard:** `makeMove` pausiert zuerst. Sonst würde die Methode nach Spielende ohne Wartezeit zurückkehren, und `Player.run` liefe in eine Schleife ohne Pause.
- **Nachbarschaft:** 5 nächste Planeten nach quadriertem Abstand plus symmetrische Hülle. Die Positionen sind `final`, daher einmal pro Spiel.
- **Sprache:** Bezeichner und Kommentare auf Englisch wie im bestehenden galcon-Code. Dieser Plan ist auf Deutsch.

---

## 5. Detaillierte Implementierungsschritte

Die Reihenfolge ist verbindlich. Zugriffe auf Felder anderer Klassen laufen über Getter/Setter (Paketsichtbarkeit, alle Klassen liegen in `src`).

### Schritt 1: `src/PlanetNeighbours.java` (Neu)
- `public final class PlanetNeighbours`, privater Konstruktor, nur statische Methoden.
- `static void assign(Collection<Planet> planets)`:
  1. Je Planet `a` die 5 nächsten anderen Planeten nach quadriertem Abstand (`getX()`, `getY()`) bestimmen: `top5(a)`.
  2. Symmetrische Hülle: Für jedes `b` in `top5(a)` gilt zusätzlich: `a` gehört zu `near(b)`.
  3. Ergebnis je Planet über `setNear(List<Planet>)` als unveränderliche Liste setzen.
- Ein Planet ist nie sein eigener Nachbar.

### Schritt 2: `src/MoraleModel.java` (Neu)
Konstanten:
```
MAX_MORALE = 100
NEUTRAL_START_MORALE = 50
HOME_START_MORALE = 100
FALL_INTERVAL_TICKS = 6    // ≈ 60 ms, ≈ 16 Punkte/s
RISE_INTERVAL_TICKS = 25   // 250 ms, 4 Punkte/s
```

`static void prepare(Collection<Planet> planets, Collection<Fleet> fleets)` (Phase 1, Aufruf unter den Sperren aus Abschnitt 3.1):
- Alle Planeten: `incomingFriendly = 0`, `incomingEnemy = 0`.
- Je Flotte `f` mit Ziel `t = f.getTarget()`:
  - `f.getOwner() == t.getOwner()` → `t.incomingFriendly += f.getNumUnits()`
  - sonst → `t.incomingEnemy += f.getNumUnits()`
- Je Planet `p`:
  ```
  friend = p.getNumUnits() + p.incomingFriendly
  enemy  = p.incomingEnemy
  für n in p.getNear():
      wenn n.getOwner() == p.getOwner()  → friend += n.getNumUnits()
      sonst wenn n.getOwner() != null    → enemy  += n.getNumUnits()
  p.realShips  = friend - enemy
  p.realMorale = (friend + enemy == 0) ? 100 : Math.round(friend * 100.0 / (friend + enemy))
  ```

`static int nextMorale(Planet p, int tick)` (Phase 2, Aufruf aus `Planet.update()`):
```
m = p.getMorale()
wenn p.getRealMorale() < m und tick % FALL_INTERVAL_TICKS == 0  → m - 1
wenn p.getRealMorale() > m und tick % RISE_INTERVAL_TICKS == 0  → m + 1
Rückgabe: m auf 0..100 begrenzt
```

Die Klasse hat keine Abhängigkeit zu Swing, Thread oder `Game`.

### Schritt 3: `src/Fleet.java` (Ändern)
- Hinzufügen: `Planet getTarget()` (gibt `target` zurück) und `int getNumUnits()` (gibt `numUnits` zurück). Beide paketsichtbar.
- Keine Verhaltensänderung.

### Schritt 4: `src/Planet.java` (Ändern)
- Neue Felder:
  ```
  private volatile int morale;
  private volatile int realMorale;
  private volatile int realShips;
  int incomingFriendly, incomingEnemy;        // nur von MoraleModel.prepare geschrieben
  private double productionProgress = 0;
  private List<Planet> near = Collections.emptyList();
  ```
- Konstruktor Spielerplanet: `morale = realMorale = MoraleModel.HOME_START_MORALE`.
- Konstruktor neutraler Planet: `morale = realMorale = MoraleModel.NEUTRAL_START_MORALE`.
- `update()` ersetzen durch:
  ```
  morale = MoraleModel.nextMorale(this, updateCnt);
  if (owner != null) {
      productionProgress += morale / 100.0;
      if (productionProgress >= PRODUCTION_TIME) {
          numUnits++;
          productionProgress -= PRODUCTION_TIME;
      }
  }
  updateCnt++;
  ```
- `invade()`: Bei Eigentümerwechsel `productionProgress = 0` setzen. Keine weitere Änderung.
- Neue Getter: `getNumUnits()`, `getMorale()`, `getRealMorale()`, `getRealShips()`, `getNear()`. Neuer Setter: `setNear(List<Planet>)`.
- `getPlanetInfo()` übergibt zusätzlich die Moral an den erweiterten `PlanetInfo`-Konstruktor (siehe Schritt 4a).

### Schritt 4a: `PlanetInfo` in `src/Planet.java` (Ändern)
- Feld `public final int MORALE`, Konstruktor um den Parameter `moraleIn` erweitern.
- Getter `public int getMorale()`.
- `public double getProductionRate()`: Bei `OWNER == null` Rückgabe 0.0 (O5). Sonst `MORALE / (double) MoraleModel.MAX_MORALE / PRODUCTION_TIME` (vorher `1.0 / PRODUCTION_TIME`). Keine Parameter, Signatur bleibt.
- `getProductionTime()` bleibt unverändert und liefert den Nennwert bei voller Moral.
- `equals(PlanetInfo)` bleibt unverändert, da die Zuordnung in `Player.getPlanetFromInfo` darüber läuft.
- Vorher per Suche alle Aufrufe von `new PlanetInfo(` prüfen. Erwartet: nur `Planet.getPlanetInfo()`.

### Schritt 5: `src/Game.java` (Ändern)
- `generateNewMap`: Nach dem Anlegen aller Planeten, innerhalb `synchronized(planets)`: `PlanetNeighbours.assign(planets)`.
- `updateAllFleets`: Gesamter Rumpf in `synchronized(planets) { synchronized(fleets) { ... } }`. Die bisherige Logik bleibt.
- `updateAllPlanets`:
  ```
  synchronized(planets) {
      synchronized(fleets) { MoraleModel.prepare(planets, fleets); }
      for (Planet p : planets) p.update();
  }
  ```
- `draw(Graphics g)`: Zeichnet `p1.drawOverlay(g)` und `p2.drawOverlay(g)` direkt in den Offscreen-Puffer (`bufferedGraphics`), damit Overlays (Auswahlring, Hover-Statistik) flimmerfrei gedoppelt gepuffert werden.
- In `Tournament.paint(Graphics g)` entfallen die Aufrufe von `drawOverlay`, da sie bereits im `bufferedImage` enthalten sind.

### Schritt 6: `src/Player.java` (Ändern, nur Synchronisation)
- `sendFleet`: Rumpf in `synchronized (currentGame.getAllPlanets()) { ... }`.
- `changeFleetTarget`: Rumpf in `synchronized (currentGame.getAllPlanets()) { synchronized (currentGame.getAllFleets()) { ... } }`.
- Signaturen bleiben gleich.

### Schritt 7: `src/HumanPlayer.java` (Ändern, nur Synchronisation)
- `handleClick`: Nach dem bestehenden Guard `isGameRunning()` die Game-Referenz lokal sichern (`Game game = currentGame;`) und den Rumpf in `synchronized (game.getAllPlanets()) { ... }` setzen.
- `sendHalfOfShips` bleibt unverändert, `Player.sendFleet` sperrt intern.
- Hover-Anzeige:
  - Feld `private Planet hoveredPlanet = null;` (nur EDT).
  - In `attachToWindow` zusätzlich `MouseAdapter` für `mouseMoved`, `mouseDragged` und `mouseExited` auf `window.getContentPane()`. `mouseMoved`/`mouseDragged` rechnet die Koordinate um (wie `mousePressed`), prüft mit `HOVER_MARGIN = 4` Pixeln Toleranz und setzt `hoveredPlanet` auf den Planeten unter der Maus, aber nur wenn `getOwner() == this`, sonst `null`.
  - Bleibt die Maus über der Statistik-Textbox stehen, bleibt die Anzeige aktiv. Beim Verlassen verweilt die Anzeige für `HOVER_LINGER_MS = 1000` ms, um Flackern beim leichten Verrutschen zu verhindern.
  - `drawOverlay(Graphics g)` (bestehende Hook-Methode): Wenn `hoveredPlanet != null`, `isGameRunning()`, `hoveredPlanet.getOwner() == this` und die Verweilzeit nicht abgelaufen ist, wird der Text `"<Produktion> / <Moral> / <Schiffe>"` gezeichnet.
    - Produktion = `getMorale() / PRODUCTION_TIME` in Schiffen pro Sekunde, eine Nachkommastelle (nominal 100 Ticks je Sekunde).
    - Schiffe = `getNumUnits()`.
  - Position: rechts neben dem Planeten, wenn `getX() < Tournament.WIN_WIDTH / 2`, sonst links (wie Legacy). Kleine Schrift, Farbe Weiß.
  - Die Besitzprüfung in `drawOverlay` verhindert, dass die Anzeige nach einer Eroberung stehen bleibt.

### Schritt 8: `src/EnemyAI.java` (Neu)
- `public class EnemyAI extends Player`.
- Konstanten:
  ```
  MIN_SHIPS_TO_SEND = 10
  MIN_MORALE_TO_SEND = 25
  MORALE_DIFF_FOR_REINFORCEMENT = 10
  SHIP_DIFF_FOR_REINFORCEMENT = 10
  ```
- Felder: `name`, `color`, `minPause`, `maxPause` (alle `final`).
- Konstruktor: `EnemyAI(String name, Color color, int minPause, int maxPause)`.
- `getColor()` und `getPlayerName()` liefern die Felder.
- `makeMove()`:
  ```
  try { sleep(minPause + (int) (Math.random() * (maxPause - minPause + 1))); }
  catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

  Game game = currentGame;
  if (game == null || game.getWinner() != null) return;

  synchronized (game.getAllPlanets()) {
      List<Planet> own = Planeten mit getOwner() == this (als ArrayList)
      Collections.shuffle(own);
      for (Planet origin : own) decideFor(origin);
  }
  ```
- `private void decideFor(Planet origin)`:
  ```
  if (origin.getOwner() != this) return;
  targets = Kopie von origin.getNear(), gemischt
  für target in targets:
      if origin.getNumUnits() < MIN_SHIPS_TO_SEND → break        // R1
      if origin.getRealMorale() < MIN_MORALE_TO_SEND → break     // R2
      if target.getOwner() == this:
          if origin.getRealMorale() - target.getRealMorale() > MORALE_DIFF_FOR_REINFORCEMENT
              → amount = origin.getNumUnits() / 4                // R3
          elif origin.getRealShips() - target.getRealShips() > SHIP_DIFF_FOR_REINFORCEMENT
              → amount = (origin.getRealShips() - target.getRealShips()) / 2   // R4
          else → continue                                        // R5
      else → amount = origin.getNumUnits() * 3 / 4               // R6
      if amount > 0 → origin.sendFleet(amount, target)
  ```
- Versand über `origin.sendFleet(...)` (Planet), nicht über `Player.sendFleet`.
- Keine Felder mit Spielzustand.

### Schritt 9: `src/SinglePlayer.java` (Ändern)
- `new SamplePlayer(aiColor, 800, 1500)` ersetzen durch `new EnemyAI("Enemy AI", aiColor, 500, 500)`. Pause 500 ms (Legacy-Takt).

### Schritt 10: `src/Tournament.java` (Ändern)
- Feld `AIs` ersetzen durch `{new EnemyAI("Enemy AI 1", Color.BLUE, 500, 500), new EnemyAI("Enemy AI 2", Color.ORANGE, 500, 500)}`.
- Die Prüfung `AIs.length != 2` und `NUM_GAMES` bleiben.

### Schritt 11: Validierung
- `get_errors` für alle neuen und geänderten Dateien (Abschnitt 7).
- Kompilierung über den Auto-Build der IDE. Im Projekt gibt es kein Maven.
- Die Tests führt der Entwickler manuell aus (Abschnitt 8).

---

## 6. Rand-, Grenz- und Sonderfälle (Edge Cases)

| Nr. | Fall | Erwartetes Verhalten |
|---|---|---|
| E1 | Summe Schiffe im Umfeld = 0 | Zielmoral 100 (Legacy-Regel), keine Division durch 0 |
| E2 | Neutraler Planet | Moral wird berechnet, keine Produktion. Bei Eroberung bleibt die Moral, `productionProgress` = 0 |
| E3 | Eroberung bei unterwegs befindlichen Flotten | Ab dem nächsten Tick zählen die Flotten nach Besitz zum neuen Eigentümer (Zählung über `target`) |
| E4 | Flottenumlenkung durch die KI eines KI-Entwicklers (`changeFleetTarget`) | Zählt ab dem nächsten Tick beim neuen Ziel |
| E5 | Flotte ohne verbleibende Schiffe | Wird entfernt (`update()` liefert `true`), zählt nicht mehr |
| E6 | Moral an den Grenzen 0 oder 100 | Begrenzung, kein Unter- oder Überlauf |
| E7 | Zielmoral wechselt mitten im Intervall | Kein Reset nötig, die Angleichung läuft über das Tick-Modulo weiter |
| E8 | Eigener Planet mit < 10 Schiffen, aber hoher Moral | Kein Versand (R1) |
| E9 | Moral < 25 | Kein Versand von diesem Planeten (R2), auch nicht an Gegner |
| E10 | Moraldifferenz genau 10 | Löst nicht aus (strikt `>`), Prüfung geht mit R4 weiter |
| E11 | Eigenes Ziel, keine Bedingung erfüllt | Kein Versand (R5) |
| E12 | Berechnete Menge ≤ 0 (z. B. negative Schiffsdifferenz) | Kein Versand |
| E13 | Spiel bereits beendet (`getWinner() != null`) | Kein Versand. Die Pause läuft trotzdem, keine Busy-Schleife |
| E14 | `Tournament`, zweite Partie | `EnemyAI`-Threads laufen weiter und lesen `currentGame` je Aufruf neu. Keine Altzustände |
| E15 | `currentGame == null` (vor Partiestart) | Kein Versand, keine Exception |
| E16 | Planet mit weniger als 5 Nachbarn | Nur vorhandene Nachbarn, keine Exception |
| E17 | Mensch klickt, während die KI sendet | Durch Sperre serialisiert, keine verlorenen Updates |
| E18 | KI eines KI-Entwicklers sendet mit veraltetem `PlanetInfo` | Bestehendes Verhalten (stiller Nichtversand), unverändert |
| E19 | Verschachtelte Sperren | Immer `planets` vor `fleets`, dadurch kein Deadlock |
| E20 | Hover über fremdem oder neutralem Planeten | Keine Anzeige |
| E21 | Eigener Planet wird während des Hovers erobert | Anzeige verschwindet, da `drawOverlay` den Besitz prüft |
| E22 | Hover über eigenem Planeten, aber Spiel beendet | Keine Anzeige, da `isGameRunning()` false ist |
| E23 | `getProductionRate()` bei Moral 0 | Rückgabe 0.0, keine Produktion |
| E24 | `getProductionRate()` bei neutralem Planeten | Rückgabe 0.0, unabhängig von der Moral. Abweichung zum Legacy-Code, siehe O5 |

---

## 7. Betroffene Dateien (Gesamtübersicht)

- [x] `src/PlanetNeighbours.java`: **Neu**
- [x] `src/MoraleModel.java`: **Neu**
- [x] `src/EnemyAI.java`: **Neu**
- [x] `src/Planet.java`: **Ändern** (Felder, `update`, `invade`, Getter/Setter)
- [x] `src/Fleet.java`: **Ändern** (Getter `getTarget`, `getNumUnits`)
- [x] `src/Game.java`: **Ändern** (Nachbarschaft, Sperren, `MoraleModel.prepare`)
- [x] `src/Player.java`: **Ändern** (nur Sperren)
- [x] `src/HumanPlayer.java`: **Ändern** (Sperre in `handleClick`, Hover-Anzeige)
- [x] `src/Tournament.java`: **Ändern** (`AIs`)
- [x] `src/SinglePlayer.java`: **Ändern** (`EnemyAI`)
- [ ] `src/SamplePlayer.java`: Unverändert
- [x] `PlanetInfo` (in `src/Planet.java`): **Ändern** (Feld `MORALE`, Getter `getMorale()`, `getProductionRate()` mit Moral)
- [ ] `FleetInfo` (in `src/Fleet.java`): Unverändert
- [x] `README.md`: **Ändern** (`getMorale()` ergänzen, `getProductionRate()` beschreiben)
- [ ] `legacy/`: Unverändert
- Keine Ressourcen-, Bild- oder Konfigurationsänderungen nötig.

---

## 8. Manuelle Teststrategie & Abnahmekriterien

Es gibt keine automatisierten Tests im Projekt. Die Prüfung erfolgt manuell in Eclipse über den Start von `SinglePlayer` bzw. `Tournament`. Zahlenwerte (Moral, Zielmoral) können über eine temporäre `System.out`-Ausgabe geprüft werden. Sie wird nach der Abnahme entfernt.

| Nr. | Testfall | Vorbedingung / Schritte | Erwartetes Ergebnis |
|---|---|---|---|
| TF-1 | Heimatplanet zu Beginn | `SinglePlayer` starten, Heimatplanet des Menschen beobachten | Moral 100, Produktionsrate identisch zur Vorversion |
| TF-2 | Moral sinkt schnell | Gegnerische Flotten in die Nähe eigener Planeten schicken | Moral fällt um ca. 16 Punkte/s in Richtung Zielmoral |
| TF-3 | Moral steigt langsam | Eigene Flotte zu einem geschwächten eigenen Planeten schicken | Moral steigt um ca. 4 Punkte/s |
| TF-4 | Produktion skaliert | Planet mit Zielmoral ~50 halten, Schiffe über 10 s zählen | Ca. halb so viele Schiffe wie bei Moral 100 |
| TF-5 | Neutraler Planet produziert nicht | Neutralen Planeten 10 s beobachten | Anzahl bleibt konstant |
| TF-6 | Eroberung ohne Sprung | Planet erobern, Moral vorher und nachher prüfen | Moral bleibt erhalten, Produktion startet ohne Sprung |
| TF-7 | KI-Ziele nur Nachbarn | `SinglePlayer`, KI-Flotten beobachten | Flotten fliegen nur zu Nachbarplaneten des Ursprungs |
| TF-8 | KI-Schwelle 10 Schiffe | KI-Planet mit weniger als 10 Schiffen beobachten | Kein Versand von diesem Planeten |
| TF-9 | KI-Schwelle Moral 25 | Planet der KI unter Druck setzen, bis Moral < 25 | Kein Versand von diesem Planeten |
| TF-10 | KI-Verstärkung | KI mit mehreren eigenen Planeten, Moraldifferenz > 10 | Flotte zwischen eigenen Planeten, ca. 1/4 der Schiffe |
| TF-11 | KI-Angriff | Neutraler Nachbar der KI | Flotte mit ca. 3/4 der Schiffe |
| TF-12 | Tournament | `Tournament` starten | Zwei Partien, Seiten `Enemy AI 1` und `Enemy AI 2`, Ergebnis angezeigt, keine Exception |
| TF-13 | Spielende stabil | Partie beenden, 30 s warten | Keine neuen Flotten nach Spielende, Anwendung reagiert |
| TF-14 | Mensch-Versand unter Sperre | Mit der Maus senden, während die KI aktiv ist | Versand funktioniert, keine Exception |
| TF-15 | Stabilität | 5 Minuten Partie | Keine Exception, keine `ConcurrentModificationException` in der Konsole |
| TF-16 | Modusunabhängigkeit | Gleiches Szenario in `SinglePlayer` und `Tournament` | Moral und Produktion verhalten sich gleich |
| TF-17 | Kompilierung | Eclipse-Build, `get_errors` auf alle Dateien aus Abschnitt 7 | Keine Fehler |
| TF-18 | Hover über eigenem Planeten | `SinglePlayer`, Maus über eigenen Planeten bewegen | Text „Produktion / Moral / Schiffe“ neben dem Planeten, Moral 100 am Start |
| TF-19 | Hover über fremdem Planeten | Maus über KI- oder neutralem Planeten bewegen | Keine Anzeige |
| TF-20 | `getMorale()` für KI-Entwickler | Temporäre Test-KI liest `PlanetInfo.getMorale()` eigener und fremder Planeten | Werte stimmen mit der Hover-Anzeige überein, `sendFleet` funktioniert wie zuvor |
| TF-21 | `getProductionRate()` mit Moral | Temporäre Test-KI liest `getProductionRate()` eines eigenen Planeten bei Moral 100 (Heimatplanet am Start), danach bei Moral ca. 50 (Angriff abwarten) | Bei Moral 100: `1 / getProductionTime()`. Bei Moral 50: halb so groß. Bei Moral 0: 0.0 |
| TF-22 | `getProductionRate()` bei neutralem Planeten | Temporäre Test-KI liest `getProductionRate()` eines neutralen Planeten | 0.0 |

### Abnahmekriterien
- Alle Testfälle TF-1 bis TF-17 sowie TF-21 und TF-22 bestanden.
- Keine Exceptions in der Konsole.
- `SamplePlayer` und die `Player`-Signaturen sind unverändert. `PlanetInfo` erhält `getMorale()`, `getProductionRate()` berücksichtigt die Moral, `README.md` ist entsprechend ergänzt.
- Die Regeln R1–R6 und die Moral-Formeln stimmen mit dem Legacy-Code überein (Code-Review gegen `legacy/level.py` und `legacy/planet.py`).

---

## 9. Entschiedene Entscheidungen

| Nr. | Frage | Entscheidung |
|---|---|---|
| O1 | Anzeige der Moral für den Menschen | Hover-Anzeige „Produktion / Moral / Schiffe“ nur für eigene Planeten (A12, Schritt 7). |
| O2 | Moral für KI-Entwickler lesbar | Ja, über `PlanetInfo.getMorale()` und README (A10, Schritt 4a). `getProductionRate()` berücksichtigt die Moral. `equals()` bleibt unverändert. |
| O3 | Pausen | 500 ms in beiden Modi, wie im Legacy-Code (A8, Schritt 9 und 10). |
| O4 | Sprache der Bezeichner und Kommentare | Englisch, wie im bestehenden galcon-Code. |
| O5 | Produktion neutraler Planeten | Vorerst nicht. `Planet.update()` produziert nur bei Besitz, `PlanetInfo.getProductionRate()` liefert 0.0 bei `OWNER == null`. Das weicht vom Legacy-Code ab (`legacy/planet.py`, `loop`). Umsetzung als Folgepunkt F1. |

---

## 10. Folgepunkte (nicht Teil dieser Umsetzung)

- **F1 Produktion neutraler Planeten wie im Legacy-Code:** Besitzerprüfung in `Planet.update()` entfernen (Legacy: `legacy/planet.py`, `loop`, ohne Besitzerprüfung). `PlanetInfo.getProductionRate()` liefert dann auch für neutrale Planeten die moralabhängige Rate, die 0.0-Regel (O5, E24) entfällt. Vorher die Balance klären: Neutrale Planeten wachsen ohne Obergrenze und werden dadurch schwerer einnehmbar. Betroffen sind R6 (Angriff auf Neutrale), TF-5, TF-11 und TF-22.

---

## 11. Umsetzungsvermerk

**Datum:** 2026-11-16

- Neu: `src/PlanetNeighbours.java`, `src/MoraleModel.java`, `src/EnemyAI.java`.
- Geändert: `src/Planet.java` (Moral, Produktion, `PlanetInfo.getMorale()`, Setter `setRealMorale`/`setRealShips` für `MoraleModel`), `src/Fleet.java` (Getter), `src/Game.java` (Nachbarschaft, Sperren, Moral-Phase), `src/Player.java` (Sperren in `sendFleet`, `changeFleetTarget` und in den Lese-Helfern gemäß A11), `src/HumanPlayer.java` (Sperre, Hover-Anzeige), `src/SinglePlayer.java`, `src/Tournament.java` (`EnemyAI`), `README.md` (`getMorale()`).
- Unverändert: `src/SamplePlayer.java`, `FleetInfo`, `legacy/`.
- Validierung: `get_errors` auf alle 10 neuen und geänderten Dateien ohne Befund. Kompilierung per Auto-Build und die manuellen Testfälle TF-1 bis TF-20 stehen beim Entwickler aus.
- Nachtrag (2026-11-16): `PlanetInfo.getProductionRate()` berücksichtigt jetzt die Moral (`MORALE / MAX_MORALE / PRODUCTION_TIME` Einheiten pro Tick), siehe A10 und Schritt 4a. `README.md` beschreibt `getProductionRate()` und `getProductionTime()` (Nennwert bei voller Moral). `getProductionTime()` ist unverändert.
- Nachtrag (2026-11-16): `getProductionRate()` liefert für neutrale Planeten 0.0 (O5, E24). Das weicht vom Legacy-Code ab. Die Produktion neutraler Planeten ist als Folgepunkt F1 (Abschnitt 10) festgehalten. `get_errors` für `Planet.java` nach der Änderung ohne Befund.
- Nachtrag (2026-11-16): Flimmern und unzuverlässiges Hovern behoben: `drawOverlay` wird nun in `Game.draw` direkt in den Offscreen-Puffer (`bufferedGraphics`) gezeichnet, anstatt in `Tournament.paint` ungepuffert auf den Front-Buffer (beseitigt 83 % Leer-Frames und extremes Flimmern). Hover-Erkennung in `HumanPlayer` um 4 px Toleranzrand (`HOVER_MARGIN`), `mouseDragged`, Textbox-Hover-Schutz und 1000 ms Verweilzeit (`HOVER_LINGER_MS`) erweitert, damit die Anzeige stabil stehen bleibt, solange man über dem Planeten oder der Textbox verweilt. `get_errors` für `Game.java`, `Tournament.java` und `HumanPlayer.java` ohne Befund.
- Nachtrag (2026-11-16): Endscreen um interaktiven „Restart“-Button erweitert (`Tournament.java`). Klick beendet das Match und kehrt in `SinglePlayer.java` zum Startbildschirm zurück. Die kumulative Spielstatistik (`scoreHistory`) bleibt über Restarts hinweg erhalten und zählt fortlaufend weiter. `get_errors` für `Tournament.java` und `SinglePlayer.java` ohne Befund.
