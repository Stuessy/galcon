# Umsetzungsplan: Schiffsanteil per Button im Einzelspiel (25 / 50 / 75 / 100 %)

**Titel:** Button unten rechts zum stufenweisen Einstellen des Anteils der gesendeten Schiffe, Sperrbereich für Planeten unter dem Button, Prozentanzeige beim Hover  
**Status:** Umgesetzt. Entscheidungen S1 bis S9 geklärt (S1 und S6 vorläufig, im Spiel zu prüfen)  
**Erstellt:** 2026-11-19  
**Überarbeitet:** 2026-11-19 (Entscheidungen S1 bis S9 übernommen: Persistenz entfällt, Hover mit Prozentzahl neu)  
**Bereich:** `src` (`HumanPlayer`, `Tournament`, `Planet`), `README.md` (Abschnitt „Controls (Single Player)“)  
**Bezug:** Folgepunkt F2 und Entscheidung O2 aus `2026-11-17_Drag-and-Drop_Flottensteuerung.md`. Dieser Plan ersetzt die feste Hälfte durch einen wählbaren Anteil.

---

## 1. Ausgangslage und Problemstellung (IST)

### 1.1 Fachlicher Hintergrund
- Im Einzelspiel sendet der Mensch per Drag&Drop immer die Hälfte der Schiffe jedes Quellplaneten (`HumanPlayer.SHIP_SHARE = 0.5`).
- Gewünscht ist ein Button unten rechts im Spielfeld. Ein Klick schaltet den Anteil weiter: 25 %, 50 %, 75 %, 100 %, danach wieder 25 %.
- Die gewählte Zahl ist zugleich der Anteil der Schiffe, den jeder eigene Quellplanet sendet. Standardwert ist 50 %.
- Die Darstellung ist ein Quadrat mit abgerundeten Ecken. Darin füllt ein Kreis als Tortendiagramm den Anteil ab der 12-Uhr-Position im Uhrzeigersinn. 25 % entspricht 0 bis 3 Uhr, 50 % 0 bis 6 Uhr, 75 % 0 bis 9 Uhr, 100 % der ganze Kreis.
- Beim Hover über dem Button wird die aktuelle Prozentzahl angezeigt.
- Beim Erzeugen der Karte darf sich unter dem Button kein Planet befinden.

### 1.2 IST im Code

| Stelle | Datei / Zeilen | Befund |
|---|---|---|
| Anteil | `src/HumanPlayer.java` Z. 30 | `public static final double SHIP_SHARE = 0.5;` ist fest |
| Versand | `src/HumanPlayer.java` Z. 367–390 | `sendFromMarked` (Z. 369) ruft `sendHalfOfShips` (Z. 383) auf. Dort steht `(int) (origin.getNumUnits() * SHIP_SHARE)` |
| Klassenkommentar | `src/HumanPlayer.java` Z. 17–25 | Z. 19 beschreibt „send half of its ships there“ |
| Eingabe | `src/HumanPlayer.java` Z. 84–91 (`mousePressed`), Z. 269–279 (`handlePress`) | Jeder linke Druck läuft über `handlePress`. Ohne Abfangen würde ein Klick auf den Button wie ein Klick auf leeren Raum wirken (alle Markierungen weg, G3) |
| Hover | `src/HumanPlayer.java` Z. 207–231 (`handleHover`), Z. 114–117 (`mouseExited`) | `handleHover` wird von `mouseMoved` und `mouseDragged` aufgerufen. `mouseExited` setzt den Planet-Hover zurück |
| Zeichnung | `src/HumanPlayer.java` Z. 131–163 (`drawOverlay`), Aufrufer `src/Game.java` Z. 88 | Overlay-Hook im Offscreen-Puffer. Button und Hover-Anzeige werden hier gezeichnet |
| Schrift | `src/HumanPlayer.java` Z. 35 | `STATS_FONT` (Monospaced, 12 pt) wird bereits für die Planet-Statistik genutzt |
| Partie | `src/SinglePlayer.java` Z. 35 | Pro Partie wird ein neuer `HumanPlayer` erzeugt. Ein Anteil über Restarts ist deshalb nicht vorhanden. Das ist für S3 gewollt |
| Spielfeld | `src/Game.java` Z. 181–199 (`generateNewMap`) | Heimatplaneten Z. 185–186, KI-Planeten Z. 189–192, neutrale Planeten Z. 193–196. Alle laufen über Konstruktoren der Klasse `Planet` |
| Platzierung | `src/Planet.java` Z. 46–53 (Heimat), Z. 71–78 (alle übrigen) | Rejection-Sampling mit `Tournament.genX/genY` und `Game.isPlanetOverlapping`. Der Button ist nicht berücksichtigt |
| Spielfeldgröße | `src/Tournament.java` Z. 35–37 | `WIN_WIDTH = 960`, `WIN_HEIGHT = 540`. Spielkoordinaten liegen unterhalb der Titelleiste (`TOP_BAR_HEIGHT = 22`, Umrechnung in `HumanPlayer.toGameCoordinates` Z. 124–128) |
| Vorlage Button | `src/Tournament.java` Z. 49–52 (Konstanten), Z. 269–284 (`drawRestartButton`) | Restart-Button: Konstanten für Position, `fillRoundRect` mit 12 px Rundung, Blau `(40, 80, 150)` |
| Wertung 0 Schiffe | `src/MoraleModel.java` Z. 64 | Division durch 0 ist bereits abgefangen. Ein Planet mit 0 Schiffen ist also unproblematisch |
| Versand-Logik | `src/Planet.java` Z. 212–231 (`sendFleet`) | Deckelt `numSent` auf `numUnits`. Bei `numSent <= 0` passiert nichts |

Geprüft per PowerShell `Select-String` über alle `*.java` und `*.md` im Projekt. Die Ergebnisse stehen in Abschnitt 7.

### 1.3 Ursachenanalyse
Kein Fehler, sondern eine fehlende Funktion. Der Anteil ist eine Konstante ohne Zustand und ohne Bedienelement. Außerdem berücksichtigt die Kartenerzeugung kein Bedienelement im Spielfeld. Deshalb kann ein Planet unter dem Button entstehen.

### 1.4 Abgrenzung zu bestehenden Plänen
- Der Plan vom 2026-11-17 bleibt historisch unverändert. Dieser Plan ersetzt dort die Entscheidung O2 (feste Hälfte) und den Folgepunkt F2.
- Die Drag-Gesten (A1 bis A12 und G1 bis G8 des Drag-Plans) bleiben unverändert. Geändert wird nur die Menge.

---

## 2. Fachliche Anforderungen und Zielzustand (SOLL)

### 2.1 Anforderungen
- **B1 Anzeige:** Quadrat mit abgerundeten Ecken unten rechts im Spielfeld. Darin ein Kreis als Tortendiagramm. Der gefüllte Sektor beginnt bei 12 Uhr und läuft im Uhrzeigersinn.
- **B2 Zustände:** 25 %, 50 %, 75 %, 100 %. Ein Klick schaltet eine Stufe weiter. Nach 100 % folgt 25 %.
- **B3 Standard:** Jede Partie beginnt mit 50 %, unabhängig von der letzten Einstellung (S3). Das weicht bewusst von der Schwierigkeit ab, die über Restarts erhalten bleibt.
- **B4 Wirkung:** Jeder Quellplanet sendet den gewählten Anteil seiner Schiffe als Flotte. Ganzzahlig, abgerundet. Gilt für alle Quellen eines Befehls einzeln (wie A9 des Drag-Plans).
- **B5 Klick:** Ein linker Klick auf den Button schaltet eine Stufe weiter. Markierungen und Gesten bleiben unberührt.
- **B6 Keine Planeten unter dem Button:** Bei jeder Kartenerzeugung darf kein Planet den Button überdecken. Die Sperrzone gilt für alle Karten, auch im AI-Tournament (S2). Ein Sicherheitsabstand (S6) gilt zusätzlich, damit Ringe und Zielhervorhebungen nicht auf dem Button liegen.
- **B7 Sichtbarkeit:** Der Button ist nur während einer laufenden Partie sichtbar. Im Endscreen verschwindet er.
- **B8 Hover-Anzeige:** Solange der Mauszeiger über dem Button liegt (auch während eines Drags), wird die aktuelle Prozentzahl angezeigt (S9). Ohne Hover ist nur das Tortendiagramm sichtbar (S7). Die Anzeige aktualisiert sich nach jedem Klick sofort.
- **B9 Keine Spiellogik-Änderung:** KI, Moral, Produktion, Flottenflug und Spielende bleiben unverändert.

### 2.2 Eingaben am Button

| Nr. | Geste | Aktion |
|---|---|---|
| G-B1 | Linker Klick auf den Button | Stufe weiter. Markierungen bleiben. Die Doppelklick-Kette wird unterbrochen (`lastClickPlanet = null`, S4) |
| G-B2 | Drag, der auf dem Button beginnt | Der Druck schaltet einmal weiter (S4); danach entstehen kein Planeten-Drag und keine Markierungsänderung |
| G-B3 | Drag von einem eigenen Planeten, Loslassen auf dem Button | Nichts, wie leerer Raum (A7 des Drag-Plans) |
| G-B4 | Rechte oder mittlere Taste auf dem Button | Nichts |
| G-B5 | Mauszeiger über dem Button | Prozentzahl wird oberhalb des Buttons angezeigt (B8). Keine Hover-Statistik eines Planeten |

### 2.3 Out of Scope (bewusst nicht ändern)
- Tastatursteuerung, zum Beispiel ←/→ wie im Legacy-Menü.
- Änderung der Rundung (bleibt abgerundet).
- Anteil für den AI-Tournament-Modus. Dort gibt es keinen `HumanPlayer`. Die KI-Mengen (`EnemyAI` R1, R3, R6) bleiben unverändert.
- Übernahme des Anteils über Restarts (S3 verneint).
- Der Startbildschirm (`StartScreen`) und `SinglePlayer`.
- Cursor-Änderung und Hover-Hintergrund des Buttons. Der Hover zeigt nur die Prozentzahl.
- Animation der Tortenfüllung.
- Umlenkung von Flotten während des Flugs.

---

## 3. Technische Analyse und beteiligte Komponenten

| Komponente | Datei / Pfad | Verantwortung / Art der Änderung |
|---|---|---|
| Tournament | `src/Tournament.java` | **Ändern.** Geometrie-Konstanten `SHARE_BTN_*` neben den Restart-Konstanten. Statische Prüfmethode `isInShareButtonArea` neben `genX`/`genY` |
| Planet | `src/Planet.java` | **Ändern.** Zwei Schleifenbedingungen (Z. 48 und Z. 74) prüfen zusätzlich den Button-Sperrbereich |
| HumanPlayer | `src/HumanPlayer.java` | **Ändern.** Anteilszustand, Hover-Zustand, Button-Klick in `handlePress`, Hover in `handleHover` und `mouseExited`, Zeichnung und Prozentanzeige in `drawOverlay`, Versandmenge, Kommentare |
| Game | `src/Game.java` | Unverändert. `generateNewMap` ruft nur die Konstruktoren von `Planet` auf, die Prüfung sitzt dort |
| SinglePlayer | `src/SinglePlayer.java` | Unverändert. Pro Partie entsteht ein neuer `HumanPlayer` mit 50 % (S3) |
| README | `README.md` | **Ändern.** Abschnitt „Controls (Single Player)“ (Z. 37–46) |
| Player, Fleet, FleetInfo, MoraleModel, PlanetNeighbours, EnemyAI, SamplePlayer, StartScreen, Difficulty, Assets | `src/…` | Unverändert |
| Bestehende Pläne | `docs/plans/2026-11-1*` | Unverändert (historische Dokumente) |

### 3.1 Threads
- Der Anteil (`shareStep`) und der Hover-Zustand (`shareHovered`) werden nur auf dem Event-Dispatch-Thread gelesen und geschrieben, wie `selectedPlanets` und `hoveredPlanet`.
- Der Versand läuft weiterhin unter `synchronized(currentGame.getAllPlanets())`.
- Die Kartenprüfung läuft im Konstruktor von `Game` (Tournament-Thread). `isInShareButtonArea` liest nur Konstanten und ist damit unkritisch.

---

## 4. Lösungsarchitektur und Designentscheidungen
- **Geometrie zentral in `Tournament`:** Wie bei `RESTART_BTN_*`. Karte (`Planet`) und Button (`HumanPlayer`) lesen dieselben Konstanten und können nicht auseinanderlaufen.
- **Exakte Kreis-Rechteck-Prüfung:** Die Planetenmitte wird auf den nächsten Punkt des Buttons geklemmt. Die Distanz wird gegen `radius + SHARE_BTN_CLEARANCE` verglichen. Das ergibt runde Ecken, ohne ein unnötig großes Rechteck zu sperren.
- **Sperrbereich für alle Karten (S2):** Auch Tournament-Karten erhalten den Sperrbereich. Das ist einfacher und bedeutet weniger Änderungen am Code. Dadurch gibt es keine Sonderfälle in `Planet` oder `Game`.
- **Anteil als Index in Prozent-Liste:** `SHARE_STEPS = {25, 50, 75, 100}` als `int`. Die Rechnung `(long) n * pct / 100` rundet ab und vermeidet Überlauf bei der Multiplikation. Bei 50 % entspricht sie dem bisherigen `(int) (n * 0.5)`.
- **Zustand pro Partie (S3):** `shareStep` ist ein Feld des `HumanPlayer` mit Startwert `DEFAULT_SHARE_STEP`. Da `SinglePlayer` pro Partie einen neuen `HumanPlayer` erzeugt, beginnt jede Partie mit 50 %. Es sind keine Änderungen an `SinglePlayer` nötig.
- **Button-Klick vor den Gesten:** `handlePress` prüft zuerst den Button. Der Klick erreicht weder Auswahl noch Versand. Es wird `pressActive = false` gesetzt, damit auch `mouseReleased` und `mouseDragged` nichts tun.
- **Hover über dieselbe Koordinatenumrechnung:** `handleHover` erhält bereits Spielkoordinaten. Der Button-Hover wird dort mit `isInShareButton` berechnet. `mouseDragged` ruft `handleHover` auf, deshalb wirkt der Hover auch während eines Drags.
- **Prozentanzeige oberhalb des Buttons (S9):** Ein kleines Label mit dunklem Hintergrund, rechtsbündig an der Buttonkante, direkt über dem Button. Gleiche Schrift wie die Planet-Statistik. Die Position ist ein Vorschlag und im Spiel zu prüfen (wie S1).
- **Zeichnung wie der Restart-Button:** `fillRoundRect` mit 12 px Rundung. Der Sektor wird mit `fillArc` gezeichnet. Java zählt Winkel gegen den Uhrzeigersinn, 90 Grad ist 12 Uhr. Ein negativer Sweep läuft im Uhrzeigersinn. 25 % ergibt `-90` (12 bis 3 Uhr), 100 % ergibt `-360`.

---

## 5. Detaillierte Implementierungsschritte

Die Reihenfolge ist verbindlich. Code-Kommentare bleiben englisch, wie im bestehenden `src`-Code.

### Schritt 1: `src/Tournament.java`, Konstanten (nach Z. 52)
```java
//Share button in the lower right corner of the game area (game coordinates, below the top bar)
public static final int SHARE_BTN_SIZE = 56;
public static final int SHARE_BTN_MARGIN = 16; //Distance to the right and bottom border
public static final int SHARE_BTN_X = WIN_WIDTH - SHARE_BTN_MARGIN - SHARE_BTN_SIZE; //888
public static final int SHARE_BTN_Y = WIN_HEIGHT - SHARE_BTN_MARGIN - SHARE_BTN_SIZE; //468
public static final int SHARE_BTN_CLEARANCE = 12; //Planet edge plus rings (up to radius + 11) must stay this far from the button
```

### Schritt 2: `src/Tournament.java`, Prüfmethode (neben `genX`/`genY`, Z. 302–311)
```java
//True if a planet with this center and radius would touch the share button, including the clearance
public static boolean isInShareButtonArea(int cx, int cy, int radius)
{
//Nearest point of the button to the planet center
int nx = Math.max(SHARE_BTN_X, Math.min(cx, SHARE_BTN_X + SHARE_BTN_SIZE));
int ny = Math.max(SHARE_BTN_Y, Math.min(cy, SHARE_BTN_Y + SHARE_BTN_SIZE));
int dx = cx - nx;
int dy = cy - ny;
int reach = radius + SHARE_BTN_CLEARANCE;
return dx * dx + dy * dy <= reach * reach;
}
```

### Schritt 3: `src/Planet.java`, beide Platzierungsschleifen (Z. 48 und Z. 74)
Die Bedingung wird in beiden Konstruktoren ersetzt:
```java
while (parent.isPlanetOverlapping(x, y, RADIUS) || Tournament.isInShareButtonArea(x, y, RADIUS))
```
- Alle Planeten einer Karte (Heimat, KI, neutral) laufen über diese beiden Konstruktoren. `Game.generateNewMap` muss deshalb nicht geändert werden (verifiziert in Abschnitt 7).
- Wie bisher ist die zufällige Platzierung nicht garantiert terminiert. Der zusätzliche Sperrbereich ist klein; TF-12 prüft die praktische Stabilität, beweist aber keine Terminierung.

### Schritt 4: `src/HumanPlayer.java`, Konstanten und Zustand (Z. 30)
`SHIP_SHARE` (Z. 30) wird ersetzt:
```java
private static final int[] SHARE_STEPS = {25, 50, 75, 100}; //Percent of the ships sent per origin, cycled by the button
private static final int DEFAULT_SHARE_STEP = 1; //Index of 50 %, every game starts with it (S3)
private static final Color SHARE_BTN_BG = new Color(40, 80, 150); //Same blue as the restart button
private int shareStep = DEFAULT_SHARE_STEP; //Index into SHARE_STEPS, only accessed from the event dispatch thread
private boolean shareHovered = false; //True while the mouse is over the share button, only accessed from the event dispatch thread
```
Der bestehende Konstruktor `HumanPlayer(Color color)` (Z. 51–54) bleibt unverändert.

### Schritt 5: `src/HumanPlayer.java`, Klick auf den Button in `handlePress` (Z. 269–279)
Am Anfang der Methode einfügen:
```java
if (isGameRunning() && isInShareButton(x, y))
{
//The share button takes the click, marks and gestures are not touched
pressActive = false; //No drag and no release action for this press
shareStep = (shareStep + 1) % SHARE_STEPS.length;
lastClickPlanet = null; //A button click breaks a double click chain (S4)
return;
}
```
Hilfsmethode:
```java
//True if the game coordinates lie on the share button
private static boolean isInShareButton(int x, int y)
{
return x >= Tournament.SHARE_BTN_X && x <= Tournament.SHARE_BTN_X + Tournament.SHARE_BTN_SIZE
&& y >= Tournament.SHARE_BTN_Y && y <= Tournament.SHARE_BTN_Y + Tournament.SHARE_BTN_SIZE;
}
```

### Schritt 6: `src/HumanPlayer.java`, Hover-Zustand (Z. 207–231 und Z. 114–117)
- Am Anfang von `handleHover` (nach der Umrechnung in `gx`, `gy`, Z. 209–211):
  ```java
  shareHovered = isInShareButton(gx, gy); //Share label is shown while the pointer is over the button (B8)
  if (shareHovered)
  {
      hoveredPlanet = null; //Do not show lingering planet stats while the pointer is over the button
      return;
  }
  ```
- In `mouseExited` (Z. 114–117) zusätzlich:
  ```java
  shareHovered = false;
  ```
- Der Planet-Hover wird beim Betreten des Buttons ausdrücklich gelöscht, da die Statistik bisher bis zu einer Sekunde nachleuchtet. Bei `mousePressed` die Button-Hoverprüfung ebenfalls aus den Druckkoordinaten aktualisieren, damit das Label nach einem Klick ohne vorheriges `mouseMoved` sichtbar ist.

### Schritt 7: `src/HumanPlayer.java`, Zeichnung in `drawOverlay` (Z. 137–150)
Am Ende von `drawOverlay`, nach der bisherigen Planet-Statistik, bei laufender Partie:
```java
if (isGameRunning())
{
    drawShareButton(g);
}
```
Methoden (neu, z. B. nach `drawMarkRing`, Z. 166–171):
```java
//Square button with rounded corners, the filled part of the pie shows the chosen share
private void drawShareButton(Graphics g)
{
int x = Tournament.SHARE_BTN_X;
int y = Tournament.SHARE_BTN_Y;
int size = Tournament.SHARE_BTN_SIZE;
int pad = 10; //Space between button border and pie
int d = size - 2 * pad;
g.setColor(SHARE_BTN_BG);
g.fillRoundRect(x, y, size, size, 12, 12);
g.setColor(Color.WHITE);
g.drawRoundRect(x, y, size, size, 12, 12);
//Angles start at 12 o'clock (90 degrees), a negative sweep runs clockwise
g.fillArc(x + pad, y + pad, d, d, 90, -360 * SHARE_STEPS[shareStep] / 100);
g.drawOval(x + pad, y + pad, d, d);
if (shareHovered)
{
drawShareLabel(g);
}
}

//Current share in percent, shown above the right edge of the button while the pointer is over it
private void drawShareLabel(Graphics g)
{
String text = SHARE_STEPS[shareStep] + " %";
g.setFont(STATS_FONT);
FontMetrics fm = g.getFontMetrics();
int w = fm.stringWidth(text) + 8;
int h = fm.getHeight() + 4;
int x = Tournament.SHARE_BTN_X + Tournament.SHARE_BTN_SIZE - w;
int y = Tournament.SHARE_BTN_Y - h - 4;
g.setColor(SHARE_BTN_BG);
g.fillRoundRect(x, y, w, h, 8, 8);
g.setColor(Color.WHITE);
g.drawString(text, x + 4, y + 2 + fm.getAscent());
}
```
- Der Button wird nicht gezeichnet, wenn die Partie beendet ist (B7). Dann ist auch `shareHovered` ohne Wirkung, weil die Zeichnung im `isGameRunning()`-Block liegt.
- Die Zeichnung liegt im Offscreen-Puffer wie die übrigen Overlays (kein Flimmern).

### Schritt 8: `src/HumanPlayer.java`, Kommentare
- Klassenkommentar (Z. 17–25), Zeile 19 und die Verweise auf die Hälfte:
  `Drag from one of your planets onto another planet to send the share shown on the button in the lower right corner (25, 50, 75 or 100 %, default 50 %) of its ships there.`
  Zusätzlich eine Zeile: `A click on the share button in the lower right corner cycles the share to the next step. Hovering over it shows the current share.`
- Z. 367: `//Every origin sends the chosen share of its ships to the target, then all marks are cleared`

### Schritt 9: `src/HumanPlayer.java`, Versandmenge (Z. 382–390)
`sendHalfOfShips` wird zu `sendShareOfShips` umbenannt. Der Aufruf in `sendFromMarked` (Z. 376) wird angepasst.
```java
//Sends the chosen share of the ships of the origin, called under the planets lock
private void sendShareOfShips(Planet origin, Planet target)
{
int units = (int) ((long) origin.getNumUnits() * SHARE_STEPS[shareStep] / 100); //Rounded down without intermediate overflow
if (units > 0)
{
origin.sendFleet(units, target);
}
}
```

### Schritt 10: `README.md`, Abschnitt „Controls (Single Player)“ (Z. 37–46)
- Z. 40 ändern: `Every marked planet, and the planet you dragged from, sends the share shown on the button (default 50 %) of its ships to the target.`
- Neuer Punkt nach Z. 46:
  `* **Click the share button** in the lower right corner to change the share of ships that is sent: 25 %, 50 %, 75 % or 100 %. The pie chart shows the setting, hovering over the button shows the percentage. Every game starts with 50 %.`

### Schritt 11: Validierung
- `get_errors` für `src/Tournament.java`, `src/Planet.java`, `src/HumanPlayer.java`.
- Kompilierung über den Auto-Build der Eclipse-IDE. Im Projekt gibt es kein Maven.
- Textsuche per PowerShell `Select-String` nach `SHIP_SHARE`, `sendHalfOfShips` und `half of` in `src` und `README.md`. Erwartet: keine Treffer außer in historischen Plänen.
- Die Suche ist vor dem Abschluss zu wiederholen (Editor-Suche hat in diesem Projekt Treffer übersehen).

---

## 6. Rand-, Grenz- und Sonderfälle (Edge Cases)

| Nr. | Fall | Erwartetes Verhalten |
|---|---|---|
| E1 | Klick auf den Button ohne Markierung | Stufe wechselt, sonst nichts (G-B1) |
| E2 | Klick auf den Button bei bestehenden Markierungen | Stufe wechselt, Markierungen bleiben (B5) |
| E3 | Doppelklick auf den Button | Jeder Druck ist ein Klick, also zwei Stufen weiter (S4). Keine Doppelklick-Logik auf dem Button |
| E4 | Stufe 100 % und ein Klick | Zurück auf 25 % (B2) |
| E5 | 25 % bei einem Planeten mit 3 Schiffen | 0 Schiffe, kein Flug. Ein Befehl gilt trotzdem und hebt die Markierungen auf (wie E22 des Drag-Plans) |
| E6 | 50 % bei einem Planeten mit 1 Schiff | 0 Schiffe, kein Flug |
| E7 | 100 % | Quelle wird auf 0 Schiffe gesetzt und bleibt im Besitz. Die Produktion läuft weiter. Keine Division durch 0 (`MoraleModel` Z. 64) |
| E8 | Drag beginnt auf dem Button | Der Druck schaltet einmal weiter, danach kein Planeten-Drag und keine Markierungsänderung (G-B2) |
| E9 | Drag endet auf dem Button | Nichts, wie leerer Raum (G-B3). Die Prozentzahl wird während des Drags angezeigt |
| E10 | Rechte Maustaste auf dem Button | Nichts (G-B4) |
| E11 | Partie endet | Der Button und die Prozentzahl verschwinden mit dem Endscreen (B7). Der Anteil ist mit der Partie verworfen |
| E12 | Restart nach einer Partie | Die neue Partie beginnt mit 50 % (B3, S3) |
| E13 | Eine Platzierung würde einen Planeten unter den Button legen | Die Position wird neu gewürfelt (Schleife in `Planet`) |
| E14 | Planetenmittelpunkt im Abstand genau `radius + 12` zum Button | Nicht erlaubt, da die Prüfung `<=` verwendet; eine neue Position wird gewürfelt |
| E15 | Planet mit Radius 50 nahe dem Button | Der Sperrbereich wächst mit dem Radius (`Tournament.isInShareButtonArea` nutzt den Radius) |
| E16 | Flotte fliegt über den Button | Die Flotte fliegt weiter. Der Button liegt im Overlay und verdeckt sie optisch. Ohne Wirkung |
| E17 | Statistik-Text eines eigenen Planeten | Der Text liegt auf der vom Spielfeldrand abgewandten Seite des Planeten. Ein Planet außerhalb des Sperrbereichs kann ihn nicht unter den Button bringen |
| E18 | AI-Tournament (KI gegen KI) | Kein Button. Die Karte hat trotzdem den Sperrbereich (S2). Die KI-Logik bleibt unverändert |
| E19 | Zielhervorhebung nahe dem Button | Kein Planet kann dort liegen, also kein Konflikt |
| E20 | Befehl mit mehreren Quellen | Jede Quelle sendet einzeln den gewählten Anteil (B4) |
| E21 | Druck auf den Button, während ein Planet gedrückt gehalten wird | Nicht möglich. Ein Druck startet nur einen Gesten-Zustand. Der Button-Druck ist kein Planet-Druck |
| E22 | Hover-Label oberhalb des Buttons überdeckt einen Planeten | Möglich, weil die Sperrzone nur den Button selbst schützt. Das Label ist transient und nur bei Hover sichtbar. Im Spiel zu prüfen (S9) |
| E23 | Mauszeiger verlässt das Fenster über dem Button | `mouseExited` setzt `shareHovered` zurück. Das Label verschwindet |
| E24 | Hover über dem Button, dann Klick | Das Label zeigt sofort den neuen Wert (B8) |

---

## 7. Betroffene Dateien (Gesamtübersicht)

- [ ] `src/Tournament.java`: **Ändern** (Konstanten `SHARE_BTN_*`, Methode `isInShareButtonArea`)
- [ ] `src/Planet.java`: **Ändern** (Z. 48 und Z. 74, Platzierungsbedingung)
- [ ] `src/HumanPlayer.java`: **Ändern** (Anteil, Hover, Button-Klick, Zeichnung, Prozentanzeige, Versandmenge, Umbenennung `sendHalfOfShips` → `sendShareOfShips`, Kommentare)
- [ ] `README.md`: **Ändern** (Abschnitt „Controls (Single Player)“)
- [ ] `src/SinglePlayer.java`: Unverändert (S3: jede Partie beginnt mit 50 %)
- [ ] `src/Game.java`: Unverändert. `generateNewMap` ruft nur die Konstruktoren von `Planet` auf
- [ ] `src/Player.java`, `src/Fleet.java`, `src/MoraleModel.java`, `src/PlanetNeighbours.java`, `src/EnemyAI.java`, `src/SamplePlayer.java`, `src/StartScreen.java`, `src/Difficulty.java`, `src/Assets.java`: Unverändert
- [ ] `docs/plans/` (bestehende Pläne): Unverändert
- Keine Ressourcen-, Bild- oder Konfigurationsänderungen nötig. Keine Datenbank.

**Konsistenzprüfung (Textsuche per PowerShell `Select-String`, Ergebnis am 2026-11-19):**
- `SHIP_SHARE`: `src/HumanPlayer.java` Z. 30 und Z. 385. Beide werden ersetzt. Im Drag-Plan nur als Historie.
- `sendHalfOfShips`: `src/HumanPlayer.java` Z. 376 und Z. 383. Beide werden umbenannt.
- `new HumanPlayer(`: nur `src/SinglePlayer.java` Z. 35. Der Konstruktor bleibt unverändert, der Anteil wird über das Feldinitial initialisiert.
- `isPlanetOverlapping` und `genX`/`genY`: nur in `src/Planet.java` (Z. 48–51, Z. 74–77) und `src/Tournament.java`. Die Platzierung läuft damit vollständig über die beiden Schleifen.
- `drawOverlay`: Aufrufe in `src/Game.java` Z. 88 und Z. 92. Die Signatur bleibt gleich.
- `handleHover`: Aufrufe in `src/HumanPlayer.java` Z. 104 und Z. 111. Beide liefern Spielkoordinaten über `toGameCoordinates`.
- Textstellen mit „half“: `src/HumanPlayer.java` Z. 19 und Z. 367, `README.md` Z. 40. Alle werden angepasst.

---

## 8. Manuelle Teststrategie und Abnahmekriterien

Im Projekt gibt es keine automatisierten Tests. Die Prüfung erfolgt manuell in Eclipse über `SinglePlayer.main`.

| Nr. | Testfall | Vorbedingung / Schritte | Erwartetes Ergebnis |
|---|---|---|---|
| TF-1 | Anzeige Start | Partie starten, Button unten rechts betrachten, Maus nicht darüber | Quadrat mit runden Ecken, rechte Hälfte des Kreises gefüllt (0 bis 6 Uhr, 50 %). Keine Prozentzahl |
| TF-2 | Zyklus | Ab 50 % viermal klicken | Der Reihe nach 75 %, 100 %, 25 %, 50 % |
| TF-3 | 25 % | Auf 25 % stellen | Nur das obere rechte Viertel (0 bis 3 Uhr) ist gefüllt |
| TF-4 | 75 % | Auf 75 % stellen | Das obere linke Viertel (9 bis 12 Uhr) ist leer, der Rest gefüllt |
| TF-5 | 100 % | Auf 100 % stellen | Kreis vollständig gefüllt |
| TF-6 | Versand 50 % | Planet mit 20 Schiffen per Drag auf einen neutralen Planeten schicken | 10 Schiffe fliegen, wie bisher |
| TF-7 | Versand 25 % | Planet mit 20 Schiffen, 25 % | 5 Schiffe fliegen |
| TF-8 | Versand 75 % | Planet mit 20 Schiffen, 75 % | 15 Schiffe fliegen |
| TF-9 | Versand 100 % | Planet mit 20 Schiffen, 100 % | 20 Schiffe fliegen, der Planet bleibt eigen und hat 0 Schiffe, danach steigt die Zahl durch Produktion |
| TF-10 | Rundung | Planet mit 3 Schiffen, 25 %, Drag auf einen anderen Planeten | Kein Flug. Markierungen sind danach aufgehoben (E5) |
| TF-11 | Mehrere Quellen | Zwei eigene Planeten mit 20 und 9 Schiffen markieren, 75 %, Drag auf neutralen Planeten | 15 und 6 Schiffe fliegen (E20) |
| TF-12 | Stabilität der Karte | 20 Partien hintereinander mit Crazy (Restart) | Kein Hänger beim Erzeugen der Karte, alle Partien starten |
| TF-13 | Kein Planet unter dem Button | 30 Partien in beliebigen Stufen, Blick auf die untere rechte Ecke jeder Karte | Kein Planet, kein Ring, keine Hervorhebung berührt den Button |
| TF-14 | Button ändert keine Markierung | Zwei Planeten markieren, Button klicken | Ringe bleiben, nur die Stufe ändert sich (B5) |
| TF-15 | Drag auf den Button | Drag von einem eigenen Planeten, loslassen auf dem Button | Nichts, Markierungen bleiben (E9) |
| TF-16 | Drag startet auf dem Button | Auf dem Button drücken, Maus über das Spielfeld ziehen, loslassen | Eine Stufe weiter; kein Versand, keine Zielline und keine Markierungsänderung (E8) |
| TF-17 | Doppelklick auf den Button | Doppelklick auf den Button | Zwei Stufen weiter (E3) |
| TF-18 | Neue Partie | 75 % einstellen, Partie beenden, Restart, Partie starten | Die neue Partie beginnt mit 50 % (B3, E12) |
| TF-19 | Endscreen | Partie bis zum Ende spielen | Button und Prozentzahl sind im Endscreen nicht zu sehen (B7, E11) |
| TF-20 | Rechtsklick auf den Button | Rechtsklick auf den Button | Keine Wirkung (E10) |
| TF-21 | Regression Gesten | Stichprobe aus dem Drag-Plan bei 50 %: TF-6, TF-13, TF-22, TF-28 | Verhalten wie bisher |
| TF-22 | Regression Startbildschirm | Schwierigkeit und Farbe wählen, Restart | Unverändert |
| TF-23 | Kompilierung | `get_errors` auf die drei geänderten Dateien, Eclipse-Auto-Build | Keine Fehler |
| TF-24 | Stabilität | Fünf Minuten Spiel mit vielen Klicks auf den Button und Drags | Keine Exception in der Konsole |
| TF-25 | Hover-Anzeige | Maus über den Button bewegen, ohne Klick | Label „50 %“ oberhalb der rechten Buttonkante. Beim Verlassen verschwindet es (B8, G-B5) |
| TF-26 | Hover nach Klick | Mit Maus über dem Button klicken | Label zeigt sofort den neuen Wert, z. B. „75 %“ (E24) |
| TF-27 | Hover während Drag | Drag von eigenem Planeten, Zeiger über den Button führen | Label erscheint, der Drag bleibt unverändert (E9) |
| TF-28 | Mauszeiger verlässt Fenster | Mit Maus über den Button, dann Fenster verlassen | Label verschwindet (E23) |
| TF-29 | Label und Planeten | Planeten nahe dem Button, Hover über dem Button | Label ist lesbar. Überdeckung eines Planeten dokumentieren (E22, S9 im Spiel prüfen) |
| TF-30 | Hover nach Planet-Statistik | Eigenen Planeten ansteuern, danach innerhalb einer Sekunde auf den Button wechseln | Planetenstatistik verschwindet sofort, aktueller Prozentwert ist sichtbar |

### Abnahmekriterien
- TF-1 bis TF-30 bestanden.
- Keine Exceptions in der Konsole.
- Kein Planet berührt den Button, in keiner Partie.
- Der Versand entspricht bei 50 % exakt dem bisherigen Verhalten.
- Keine Änderung an `Game`, `Player`, `EnemyAI`, `MoraleModel`, `Fleet`, `StartScreen`, `SinglePlayer`.
- `README.md` beschreibt den Button und die Prozentanzeige.

---

## 9. Entscheidungen

Stand 2026-11-19. Die Entscheidungen sind vom Auftraggeber übernommen. S1 und S6 gelten vorläufig und werden im Spiel geprüft.

| Nr. | Frage | Entscheidung | Status |
|---|---|---|---|
| S1 | Position und Größe | Unten rechts, 56 px, 16 px Abstand zum Rand. Spielkoordinaten x 888, y 468 | Vorläufig übernommen, im Spiel zu prüfen |
| S2 | Sperrbereich auch bei Tournament-Karten | Ja, alle Karten gleich. Auftraggeber: weniger Änderungen am Code | Bestätigt |
| S3 | Persistenz über Restarts | Nein. Jede Partie beginnt mit 50 %. Abweichend von der Schwierigkeit | Bestätigt |
| S4 | Klick auf den Button und Doppelklick | Jeder Druck ist ein Klick (zwei Stufen bei Doppelklick). Der Button unterbricht die Doppelklick-Kette | Bestätigt |
| S5 | Rundung | Ganzzahlig abgerundet wie bisher | Bestätigt |
| S6 | Sicherheitsabstand | 12 px zusätzlich zum Planetenradius, damit Ringe bei R + 11 nicht auf dem Button liegen | Vorläufig übernommen, im Spiel zu prüfen |
| S7 | Anzeige ohne Hover | Nur das Tortendiagramm, keine Prozentzahl | Bestätigt |
| S8 | Sichtbarkeit | Nur während einer laufenden Partie | Bestätigt |
| S9 | Mauszeiger über dem Button | Hover-Effekt: aktuelle Prozentzahl als Label oberhalb des Buttons. Position ist ein Vorschlag, im Spiel zu prüfen | Bestätigt (Hover mit Prozentzahl), Position zu prüfen |

---

## 10. Folgepunkte (nicht Teil dieser Umsetzung)
- **F-S1** Tastatursteuerung ←/→ wie im Legacy-Menü.
- **F-S2** Hover-Hintergrund des Buttons (heller bei Hover), falls die Prozentzahl allein nicht genügt.

---

## 11. Umsetzungsvermerk
**Datum:** 2026-11-19

- `src/Tournament.java`: Konstanten `SHARE_BTN_*` und Prüfmethode `isInShareButtonArea` ergänzt.
- `src/Planet.java`: Beide Platzierungsschleifen (Heimat, übrige Planeten) prüfen zusätzlich den Button-Sperrbereich.
- `src/HumanPlayer.java`: `SHIP_SHARE` durch `SHARE_STEPS`, `shareStep` und `shareHovered` ersetzt. Klick auf den Button in `handlePress` (zusätzlich `dragging` und `pressedPlanet` zurückgesetzt). Hover in `handleHover` und `mouseExited`. Zeichnung in `drawOverlay` über `drawShareButton` und `drawShareLabel`. `sendHalfOfShips` in `sendShareOfShips` umbenannt. Kommentare angepasst.
- `README.md`: Abschnitt „Controls (Single Player)“ angepasst.
- Unverändert: `Game`, `SinglePlayer`, `Planet`-Konstruktoren ohne Änderung an `Game`.
- Validierung: `get_errors` ohne Befunde für die drei Java-Dateien. Textsuche nach `SHIP_SHARE`, `sendHalfOfShips` ohne Treffer. Manuelle Tests TF-1 bis TF-30 und die Prüfung von S1 und S6 im Spiel stehen noch aus.
