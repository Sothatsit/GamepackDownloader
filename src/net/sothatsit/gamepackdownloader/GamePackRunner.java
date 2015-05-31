package net.sothatsit.gamepackdownloader;

import net.sothatsit.gamepackdownloader.util.Log;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class GamePackRunner {

    public static Applet loadApplet(File jarFile) throws Exception {
        URLClassLoader classLoader = new URLClassLoader(new URL[] {jarFile.toURI().toURL()}, GamePackRunner.class.getClassLoader());
        Class<?> Client = classLoader.loadClass("Client");
        Object inst = Client.newInstance();

        return (Applet) inst;
    }

    public static void run(File gameJar) {
        JFrame frame = new JFrame("GamePackDownloader");

        frame.setLayout(new BorderLayout(0, 0));
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Applet applet = null;
        try {
            applet = loadApplet(gameJar);
        } catch (Exception e) {
            Log.error("Error loaded game jar");
            e.printStackTrace();
            GamePackDownloader.exit();
            return;
        }

        applet.setPreferredSize(new Dimension(765, 503));
        applet.setVisible(true);

        frame.getContentPane().add(applet);
        frame.getContentPane().setPreferredSize(new Dimension(765, 503));

        frame.pack();
        frame.revalidate();
        frame.repaint();
    }

}
