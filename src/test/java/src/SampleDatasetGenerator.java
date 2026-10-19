package src;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Utility to generate synthetic face datasets for testing the face recognition system.
 *
 * @author Gaurav <gauravkr11311@gmail.com>
 */
public class SampleDatasetGenerator {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 140;

    public static void main(String[] args) {
        generateAll("data/sample-faces");
    }

    public static void generateAll(String outputDirPath) {
        File baseDir = new File(outputDirPath);
        baseDir.mkdirs();

        File s1 = new File(baseDir, "subject01");
        File s2 = new File(baseDir, "subject02");
        File s3 = new File(baseDir, "subject03");
        File probes = new File(baseDir, "probes");
        s1.mkdirs();
        s2.mkdirs();
        s3.mkdirs();
        probes.mkdirs();

        // Subject 1: Oval face, warm skin, brown eyes
        Color skin1 = new Color(245, 205, 175);
        saveImage(generateFace(skin1, 0, false, false), new File(s1, "subject01_normal.png"));
        saveImage(generateFace(skin1, 1, false, false), new File(s1, "subject01_smile.png"));
        saveImage(generateFace(skin1, 0, true, false), new File(s1, "subject01_glasses.png"));

        // Subject 2: Square face, fair skin, blue eyes
        Color skin2 = new Color(255, 224, 196);
        saveImage(generateFace(skin2, 0, false, false), new File(s2, "subject02_normal.png"));
        saveImage(generateFace(skin2, 2, false, false), new File(s2, "subject02_serious.png"));
        saveImage(generateFace(skin2, 0, false, true), new File(s2, "subject02_beard.png"));

        // Subject 3: Round face, darker skin, dark eyes
        Color skin3 = new Color(210, 160, 120);
        saveImage(generateFace(skin3, 0, false, false), new File(s3, "subject03_normal.png"));
        saveImage(generateFace(skin3, 1, false, false), new File(s3, "subject03_smile.png"));
        saveImage(generateFace(skin3, 0, true, false), new File(s3, "subject03_glasses.png"));

        // Probes for testing recognition
        saveImage(generateFace(skin1, 1, true, false), new File(probes, "probe_subject01.png"));
        saveImage(generateFace(skin2, 0, false, true), new File(probes, "probe_subject02.png"));
        saveImage(generateFace(skin3, 1, false, false), new File(probes, "probe_subject03.png"));

        System.out.println("Sample face dataset successfully generated in: " + baseDir.getAbsolutePath());
    }

    private static BufferedImage generateFace(Color skinColor, int expression, boolean glasses, boolean beard) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Neutral dark gray background
        g.setColor(new Color(60, 65, 75));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Hair / Head Silhouette
        g.setColor(new Color(30, 25, 20));
        g.fillOval(20, 15, 80, 105);

        // Neck
        g.setColor(skinColor.darker());
        g.fillRect(45, 105, 30, 30);

        // Face Oval
        g.setColor(skinColor);
        g.fillOval(25, 28, 70, 85);

        // Hair Front / Forehead
        g.setColor(new Color(30, 25, 20));
        g.fillArc(22, 18, 76, 40, 0, 180);

        // Eyebrows
        g.setColor(new Color(40, 30, 25));
        g.setStroke(new BasicStroke(2.5f));
        g.drawLine(38, 55, 52, 54);
        g.drawLine(68, 54, 82, 55);

        // Eyes
        g.setColor(Color.WHITE);
        g.fillOval(40, 58, 12, 8);
        g.fillOval(68, 58, 12, 8);
        g.setColor(new Color(30, 20, 15));
        g.fillOval(44, 60, 5, 5);
        g.fillOval(72, 60, 5, 5);

        // Nose
        g.setColor(skinColor.darker());
        g.setStroke(new BasicStroke(1.8f));
        g.drawLine(60, 63, 58, 78);
        g.drawLine(58, 78, 64, 78);

        // Mouth
        g.setColor(new Color(180, 80, 80));
        g.setStroke(new BasicStroke(2.0f));
        if (expression == 1) { // Smile
            g.drawArc(48, 83, 24, 14, 0, -180);
        } else if (expression == 2) { // Serious / Frown
            g.drawArc(48, 90, 24, 12, 0, 180);
        } else { // Neutral
            g.drawLine(48, 89, 72, 89);
        }

        // Glasses
        if (glasses) {
            g.setColor(new Color(35, 35, 35));
            g.setStroke(new BasicStroke(2.2f));
            g.drawRect(36, 54, 19, 15);
            g.drawRect(65, 54, 19, 15);
            g.drawLine(55, 60, 65, 60);
            g.drawLine(25, 58, 36, 60);
            g.drawLine(84, 60, 95, 58);
        }

        // Beard
        if (beard) {
            g.setColor(new Color(35, 25, 20));
            g.setStroke(new BasicStroke(2.0f));
            g.drawArc(40, 85, 40, 26, 0, -180);
        }

        g.dispose();
        return img;
    }

    private static void saveImage(BufferedImage img, File file) {
        try {
            ImageIO.write(img, "png", file);
        } catch (Exception e) {
            System.err.println("Failed to write sample image: " + e.getMessage());
        }
    }
}
