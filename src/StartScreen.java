package src;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JPanel;

/*
 * Start screen of the single player game. Two planets in the selectable player colors are shown,
 * the human clicks one of them to choose his color. Below the planets, the difficulty can be selected with the mouse.
 * Use waitForChoice() to wait for the choice, getDifficulty() returns the selected difficulty.
 */
public class StartScreen extends JFrame
{
	private static final String TEXT = "Choose your color to start";
	private static final Font TEXT_FONT = new Font("Monospaced", Font.BOLD, 32);
	private static final int PLANET_RADIUS = Planet.MAX_SIZE;
	private static final Font BUTTON_FONT = new Font("Monospaced", Font.BOLD, 20);
	private static final int BUTTON_WIDTH = 160;
	private static final int BUTTON_HEIGHT = 44;
	private static final int BUTTON_GAP = 20;
	private static final int BUTTON_Y = 380; //Below the planets, which end at y = 320
	private static final String DIFFICULTY_TEXT = "Difficulty";
	private static final Difficulty[] LEVELS = Difficulty.values(); //Button order: Easy, Normal, Hard, Crazy
	private static final int ROW_WIDTH = LEVELS.length * BUTTON_WIDTH + (LEVELS.length - 1) * BUTTON_GAP;
	private static final int ROW_X = (Tournament.WIN_WIDTH - ROW_WIDTH) / 2; //Button row centered in the window

	private final Color[] colors; //Left planet first, right planet second
	private final CountDownLatch chosen = new CountDownLatch(1);
	private volatile Color choice = null;
	private volatile Difficulty difficulty; //Selected map setup, written on the event dispatch thread

	//The given difficulty is preselected
	public StartScreen(Color left, Color right, Difficulty initial)
	{
		super("Galcon - Start");
		colors = new Color[] {left, right};
		difficulty = initial;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setContentPane(new StartPanel());
		pack();
		setVisible(true);
	}

	//Blocks until a planet has been clicked and returns the chosen color
	public Color waitForChoice() throws InterruptedException
	{
		chosen.await();
		return choice;
	}

	//The difficulty selected on the start screen, read after waitForChoice() has returned
	public Difficulty getDifficulty()
	{
		return difficulty;
	}

	//Center of the planet with the given index (0 = left, 1 = right)
	private static int centerX(int index)
	{
		return Tournament.WIN_WIDTH * (2 * index + 1) / 4;
	}

	private static int centerY()
	{
		return Tournament.WIN_HEIGHT / 2;
	}

	//Left edge of the difficulty button with the given index
	private static int buttonX(int index)
	{
		return ROW_X + index * (BUTTON_WIDTH + BUTTON_GAP);
	}

	//Returns the difficulty whose button is under the given point, or null
	private static Difficulty difficultyAt(int x, int y)
	{
		for (int i = 0; i < LEVELS.length; i++)
		{
			if (x >= buttonX(i) && x < buttonX(i) + BUTTON_WIDTH && y >= BUTTON_Y && y < BUTTON_Y + BUTTON_HEIGHT)
			{
				return LEVELS[i];
			}
		}
		return null;
	}

	//Selects the difficulty of the button or the color of the planet under the mouse, if any
	private void handleClick(int x, int y)
	{
		Difficulty clicked = difficultyAt(x, y);
		if (clicked != null)
		{
			difficulty = clicked; //Only selects, the game starts with a click on a planet
			repaint();
			return;
		}
		for (int i = 0; i < colors.length; i++)
		{
			int dx = x - centerX(i);
			int dy = y - centerY();
			if (dx * dx + dy * dy <= PLANET_RADIUS * PLANET_RADIUS)
			{
				choice = colors[i];
				dispose(); //Close the start screen, the game window replaces it
				chosen.countDown();
				return;
			}
		}
	}

	//Panel that draws the title and the two planets
	private class StartPanel extends JPanel
	{
		StartPanel()
		{
			setBackground(Color.WHITE);
			setPreferredSize(new Dimension(Tournament.WIN_WIDTH, Tournament.WIN_HEIGHT));
			addMouseListener(new MouseAdapter()
			{
				public void mousePressed(MouseEvent e)
				{
					handleClick(e.getX(), e.getY());
				}
			});
		}

		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g.drawImage(Assets.STAR_BACKGROUND, 0, 0, getWidth(), getHeight(), this);
			g.setFont(TEXT_FONT);
			FontMetrics fm = g.getFontMetrics();
			g.setColor(Color.WHITE);
			g.drawString(TEXT, (getWidth() - fm.stringWidth(TEXT)) / 2, Tournament.WIN_HEIGHT / 4);
			for (int i = 0; i < colors.length; i++)
			{
				g.setColor(colors[i]);
				g.fillOval(centerX(i) - PLANET_RADIUS, centerY() - PLANET_RADIUS, 2 * PLANET_RADIUS, 2 * PLANET_RADIUS);
			}
			//Difficulty buttons below the planets, the selected one is filled blue
			g.setFont(BUTTON_FONT);
			FontMetrics bfm = g.getFontMetrics();
			g.setColor(Color.WHITE);
			g.drawString(DIFFICULTY_TEXT, (getWidth() - bfm.stringWidth(DIFFICULTY_TEXT)) / 2, BUTTON_Y - 12);
			for (int i = 0; i < LEVELS.length; i++)
			{
				boolean selected = (LEVELS[i] == difficulty);
				int bx = buttonX(i);
				g.setColor(selected ? new Color(40, 80, 150) : new Color(20, 20, 30));
				g.fillRoundRect(bx, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, 12, 12);
				g.setColor(Color.WHITE);
				g.drawRoundRect(bx, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, 12, 12);
				String label = LEVELS[i].toString();
				g.setColor(selected ? Color.WHITE : Color.GRAY);
				int textX = bx + (BUTTON_WIDTH - bfm.stringWidth(label)) / 2;
				int textY = BUTTON_Y + (BUTTON_HEIGHT - bfm.getHeight()) / 2 + bfm.getAscent();
				g.drawString(label, textX, textY);
			}
		}
	}
}
