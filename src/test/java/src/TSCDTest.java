package src;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TSCD (Two-Stage Classification and Detection) eigenface decomposition.
 *
 * @author Gaurav <gauravkr11311@gmail.com>
 */
public class TSCDTest {

    private Face createTestFace(String name, int shade) {
        // Ideal size 48x64 = 3072 pixels
        BufferedImage img = new BufferedImage(48, 64, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 48; x++) {
            for (int y = 0; y < 64; y++) {
                int gray = (shade + (x * 2) + y) % 256;
                img.setRGB(x, y, new Color(gray, gray, gray).getRGB());
            }
        }

        Face face = new Face();
        face.setClassification(name);
        face.setPicture(new Picture(img));
        face.setDescription("Test face: " + name);
        return face;
    }

    @Test
    @DisplayName("TSCD successfully computes eigenfaces and decomposes probe picture")
    public void testEigenFacesComputation() {
        TSCD tscd = new TSCD();
        assertFalse(tscd.isTrained());

        Face[] trainingFaces = new Face[]{
            createTestFace("Person1", 40),
            createTestFace("Person2", 90),
            createTestFace("Person3", 140),
            createTestFace("Person4", 190)
        };

        Main.ProgressTracker tracker = new Main.ProgressTracker();
        tscd.processTrainingSet(trainingFaces, tracker);

        assertTrue(tscd.isTrained());
        assertNotNull(tscd.getAverageFace());
        assertTrue(tscd.getNumEigenVectors() > 0);

        // Project a probe picture onto the computed eigenvectors
        Picture probePic = trainingFaces[0].getPicture();
        double[] eigenWeights = tscd.getEigenFaces(probePic, 3);

        assertNotNull(eigenWeights);
        assertTrue(eigenWeights.length > 0);
        assertTrue(eigenWeights.length <= 3);
    }
}
