package src;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Picture component and pixel extraction algorithms.
 *
 * @author Gaurav <gauravkr11311@gmail.com>
 */
public class PictureTest {

    @Test
    @DisplayName("Picture extracts correct grayscale pixel values from image")
    public void testGetImagePixels() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        // Pure White: (255, 255, 255) -> gray = 255
        image.setRGB(0, 0, Color.WHITE.getRGB());
        // Pure Black: (0, 0, 0) -> gray = 0
        image.setRGB(1, 0, Color.BLACK.getRGB());
        // Pure Red: (255, 0, 0) -> gray = 255/3 = 85.0
        image.setRGB(0, 1, Color.RED.getRGB());
        // Pure Green: (0, 255, 0) -> gray = 255/3 = 85.0
        image.setRGB(1, 1, Color.GREEN.getRGB());

        Picture picture = new Picture(image);
        double[] pixels = picture.getImagePixels();

        assertEquals(4, pixels.length);
        assertEquals(255.0, pixels[0], 1.0);
        assertEquals(0.0, pixels[1], 1.0);
        assertEquals(85.0, pixels[2], 1.0);
        assertEquals(85.0, pixels[3], 1.0);
    }

    @Test
    @DisplayName("Picture correctly crops and displays subimage")
    public void testCropAndDisplay() {
        int width = 10;
        int height = 10;
        int[] pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = Color.BLUE.getRGB();
        }

        BufferedImage base = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Picture pic = new Picture(base);

        pic.cropAndDisplay(pixels, width, height, 2, 8, 2, 8);
        BufferedImage cropped = pic.getImage();

        assertNotNull(cropped);
        assertEquals(6, cropped.getWidth());
        assertEquals(6, cropped.getHeight());
    }
}
