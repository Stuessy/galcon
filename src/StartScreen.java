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
 * the human clicks one of them to choose his color. Use waitForChoice() to wait for the choice.
 */
public class StartScreen extends JFrame
{
	private static final String TEXT = "Choose your color to start";
	private static final Font TEXT_FONT = new Font("Monospaced", Font.BOLD, 32);
	private static final int PLANET_RADIUS = Planet.MAX_SIZE;

	private final Color[] colors; //Left planet first, right planet second
	private final CountDownLatch chosen = new CountDownLatch(1);
	private volatile Color choice = null;

	public StartScreen(Color left, Color right)
	{
		super("Galcon - Start");
		colors = new Color[] {left, right};
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

	//Center of the planet with the given index (0 = left, 1 = right)
	private static int centerX(int index)
	{
		return Tournament.WIN_WIDTH * (2 * index + 1) / 4;
	}

	private static int centerY()
	{
		return Tournament.WIN_HEIGHT / 2;
	}

	//Selects the color of the planet under the mouse, if any
	private void handleClick(int x, int y)
	{
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
		}
	}
}
