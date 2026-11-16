package src;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

/** Zentrale Pfade und Laufzeitbilder der Anwendung. */
public final class Assets
{
	private static final String IMAGE_DIRECTORY = "assets/images/";
	public static final String BRIGHT_PATH = IMAGE_DIRECTORY + "bright.jpg";
	public static final String EXPLOSION_PACK_PATH = IMAGE_DIRECTORY + "explosion_pack.jpg";
	public static final String PLANET_PATH = IMAGE_DIRECTORY + "planet.png";
	public static final String PLANET_GRAY_PATH = IMAGE_DIRECTORY + "planetGray.png";
	public static final String PLANET_GLOW_PATH = IMAGE_DIRECTORY + "planet_glow.jpg";
	public static final String STAR_BACKGROUND_PATH = IMAGE_DIRECTORY + "SpacePic.jpg";
	public static final String LIGHT_SOURCE_PATH = "assets/source/light.psd";

	public static final Image PLANET_GRAY = loadImage(PLANET_GRAY_PATH);
	public static final Image STAR_BACKGROUND = loadImage(STAR_BACKGROUND_PATH);

	private Assets()
	{
	}

	private static Image loadImage(String path)
	{
		URL resource = Assets.class.getResource("/" + path);
		if (resource != null)
		{
			return new ImageIcon(resource).getImage();
		}
		return new ImageIcon(path).getImage();
	}
}
