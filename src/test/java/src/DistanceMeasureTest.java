package src;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DistanceMeasure implementations.
 *
 * @author Gaurav <gauravkr11311@gmail.com>
 */
public class DistanceMeasureTest {

    private DistanceMeasure euclidean;

    @BeforeEach
    public void setUp() {
        euclidean = FeatureSpace.EUCLIDEAN_DISTANCE;
    }

    @Test
    @DisplayName("Identical vectors should have distance 0.0")
    public void testIdenticalVectorsZeroDistance() {
        FeatureVector fv1 = new FeatureVector();
        fv1.setFeatureVector(new double[]{1.0, 2.0, 3.0});

        FeatureVector fv2 = new FeatureVector();
        fv2.setFeatureVector(new double[]{1.0, 2.0, 3.0});

        double distance = euclidean.calculateDistance(fv1, fv2);
        assertEquals(0.0, distance, 1e-9);
    }

    @Test
    @DisplayName("Euclidean distance between 3D vectors follows Pythagorean formula")
    public void testKnownDistance() {
        FeatureVector fv1 = new FeatureVector();
        fv1.setFeatureVector(new double[]{0.0, 0.0, 0.0});

        FeatureVector fv2 = new FeatureVector();
        fv2.setFeatureVector(new double[]{3.0, 4.0, 0.0});

        double distance = euclidean.calculateDistance(fv1, fv2);
        assertEquals(5.0, distance, 1e-9);
    }

    @Test
    @DisplayName("Distance is symmetric: dist(A, B) == dist(B, A)")
    public void testDistanceSymmetry() {
        FeatureVector fv1 = new FeatureVector();
        fv1.setFeatureVector(new double[]{1.5, -2.3, 4.1});

        FeatureVector fv2 = new FeatureVector();
        fv2.setFeatureVector(new double[]{-0.5, 3.2, 1.0});

        double distAB = euclidean.calculateDistance(fv1, fv2);
        double distBA = euclidean.calculateDistance(fv2, fv1);
        assertEquals(distAB, distBA, 1e-9);
    }
}
