package net.pixelstatic.Mirage.desktop;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import io.anuke.Mirage.Mirage;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setDecorated(false);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		config.setWindowedMode(d.width, d.height);
		config.setTitle("Mirage");
		new Lwjgl3Application(new Mirage(), config);
	}
}
