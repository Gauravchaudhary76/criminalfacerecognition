package src;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FeatureSpace operations and k-NN classification.
 *
 * @author Gaurav <gauravkr11311@gmail.com>
 */
public class FeatureSpaceTest {

    private FeatureSpace featureSpace;

    private Face createDummyFace(String classification) {
        BufferedImage img = new BufferedImage(48, 64, BufferedImage.TYPE_INT_RGB);
        Face face = new Face();
        face.setClassification(classification);
        face.setPicture(new Picture(img));
        face.setDescription("Mock face for " + classification);
        return face;
    }

    @BeforeEach
    public void setUp() {
        featureSpace = new FeatureSpace();
    }

    @Test
    @DisplayName("Inserting faces into FeatureSpace increases size and populates classifications")
    public void testInsertIntoDatabase() {
        assertEquals(0, featureSpace.getFeatureSpaceSize());

        Face faceAlice = createDummyFace("Alice");
        featureSpace.insertIntoDatabase(faceAlice, new double[]{1.0, 2.0, 3.0});

        assertEquals(1, featureSpace.getFeatureSpaceSize());
        assertEquals(1, featureSpace.getClassifications().size());
        assertEquals("Alice", featureSpace.getClassifications().get(0));

        Face faceBob = createDummyFace("Bob");
        featureSpace.insertIntoDatabase(faceBob, new double[]{10.0, 20.0, 30.0});

        assertEquals(2, featureSpace.getFeatureSpaceSize());
        assertEquals(2, featureSpace.getClassifications().size());
        assertTrue(featureSpace.getClassifications().contains("Bob"));
    }

    @Test
    @DisplayName("closestFeature correctly finds the nearest feature vector")
    public void testClosestFeature() {
        Face faceAlice = createDummyFace("Alice");
        featureSpace.insertIntoDatabase(faceAlice, new double[]{1.0, 1.0, 1.0});

        Face faceBob = createDummyFace("Bob");
        featureSpace.insertIntoDatabase(faceBob, new double[]{10.0, 10.0, 10.0});

        FeatureVector probeNearAlice = new FeatureVector();
        probeNearAlice.setFeatureVector(new double[]{1.2, 0.9, 1.1});

        String closest = featureSpace.closestFeature(FeatureSpace.EUCLIDEAN_DISTANCE, probeNearAlice);
        assertEquals("Alice", closest);

        FeatureVector probeNearBob = new FeatureVector();
        probeNearBob.setFeatureVector(new double[]{9.5, 10.2, 9.8});

        String closestBob = featureSpace.closestFeature(FeatureSpace.EUCLIDEAN_DISTANCE, probeNearBob);
        assertEquals("Bob", closestBob);
    }

    @Test
    @DisplayName("k-NN classification returns closest match")
    public void testKnnClassification() {
        Face faceAlice1 = createDummyFace("Alice");
        featureSpace.insertIntoDatabase(faceAlice1, new double[]{0.0, 0.0, 0.0});

        Face faceAlice2 = createDummyFace("Alice");
        featureSpace.insertIntoDatabase(faceAlice2, new double[]{0.5, 0.5, 0.5});

        Face faceBob1 = createDummyFace("Bob");
        featureSpace.insertIntoDatabase(faceBob1, new double[]{10.0, 10.0, 10.0});

        FeatureVector probe = new FeatureVector();
        probe.setFeatureVector(new double[]{0.1, 0.1, 0.1});

        String result = featureSpace.knn(FeatureSpace.EUCLIDEAN_DISTANCE, probe, 3);
        assertEquals("Alice", result);
    }

    @Test
    @DisplayName("orderByDistance sorts all faces by proximity")
    public void testOrderByDistance() {
        Face face1 = createDummyFace("Face1");
        featureSpace.insertIntoDatabase(face1, new double[]{1.0, 1.0, 1.0});

        Face face2 = createDummyFace("Face2");
        featureSpace.insertIntoDatabase(face2, new double[]{5.0, 5.0, 5.0});

        Face face3 = createDummyFace("Face3");
        featureSpace.insertIntoDatabase(face3, new double[]{2.0, 2.0, 2.0});

        FeatureVector probe = new FeatureVector();
        probe.setFeatureVector(new double[]{0.0, 0.0, 0.0});

        FeatureSpace.FaceDistancePair[] pairs = featureSpace.orderByDistance(FeatureSpace.EUCLIDEAN_DISTANCE, probe);
        assertEquals(3, pairs.length);
        assertEquals("Face1", pairs[0].getFace().getClassification());
        assertEquals("Face3", pairs[1].getFace().getClassification());
        assertEquals("Face2", pairs[2].getFace().getClassification());
    }

    @Test
    @DisplayName("get3dFeatureSpace generates normalized coordinates")
    public void testGet3dFeatureSpace() {
        Face face1 = createDummyFace("Alice");
        featureSpace.insertIntoDatabase(face1, new double[]{1.0, 2.0, 3.0});

        Face face2 = createDummyFace("Bob");
        featureSpace.insertIntoDatabase(face2, new double[]{10.0, 20.0, 30.0});

        double[][] coords = featureSpace.get3dFeatureSpace();
        assertNotNull(coords);
        assertTrue(coords.length > 0);

        // Check values are normalized
        for (int i = 0; i < coords.length - 18; i++) {
            assertTrue(coords[i][0] >= -1e-6 && coords[i][0] <= 100.0 + 1e-6);
            assertTrue(coords[i][1] >= -1e-6 && coords[i][1] <= 100.0 + 1e-6);
            assertTrue(coords[i][2] >= -1e-6 && coords[i][2] <= 100.0 + 1e-6);
        }
    }
}
