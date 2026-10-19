package src;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for face recognition pipeline.
 * Tests loading images, training eigenfaces, probing, and matching accuracy.
 *
 * @author Gaurav <gauravkr11311@gmail.com>
 */
public class FaceRecognitionEndToEndTest {

    @Test
    @DisplayName("End-to-end training and identification on sample face dataset")
    public void testEndToEndRecognition() throws MalformedURLException {
        File dataDir = new File("data/sample-faces");
        if (!dataDir.exists()) {
            SampleDatasetGenerator.generateAll("data/sample-faces");
        }

        // 1. Load training images
        List<Face> trainingFaces = new ArrayList<>();
        String[] subjects = {"subject01", "subject02", "subject03"};

        for (String subject : subjects) {
            File subDir = new File(dataDir, subject);
            File[] files = subDir.listFiles((dir, name) -> name.endsWith(".png"));
            assertNotNull(files, "Subject directory should exist: " + subDir.getPath());
            assertTrue(files.length > 0, "Subject should have images");

            for (File file : files) {
                Face face = new Face(file);
                face.setClassification(subject);
                face.setDescription(file.getName());
                face.load(true);
                trainingFaces.add(face);
            }
        }

        assertEquals(9, trainingFaces.size(), "Should have loaded 9 training faces (3 per subject)");

        // 2. Train TSCD eigenface model
        TSCD tscd = new TSCD();
        Main.ProgressTracker tracker = new Main.ProgressTracker();
        tscd.processTrainingSet(trainingFaces.toArray(new Face[0]), tracker);

        assertTrue(tscd.isTrained());
        assertTrue(tscd.getNumEigenVectors() > 0);

        // 3. Build FeatureSpace
        FeatureSpace featureSpace = new FeatureSpace();
        int numEigenVectors = Math.min(10, tscd.getNumEigenVectors());

        for (Face face : trainingFaces) {
            double[] weights = tscd.getEigenFaces(face.getPicture(), numEigenVectors);
            featureSpace.insertIntoDatabase(face, weights);
        }

        assertEquals(9, featureSpace.getFeatureSpaceSize());
        assertEquals(3, featureSpace.getClassifications().size());

        // 4. Test probing with unseen probe images
        File probesDir = new File(dataDir, "probes");
        for (String expectedSubject : subjects) {
            File probeFile = new File(probesDir, "probe_" + expectedSubject + ".png");
            assertTrue(probeFile.exists(), "Probe file should exist: " + probeFile.getPath());

            Face probeFace = new Face(probeFile);
            probeFace.load(true);

            double[] probeWeights = tscd.getEigenFaces(probeFace.getPicture(), numEigenVectors);
            FeatureVector probeVector = new FeatureVector();
            probeVector.setFeatureVector(probeWeights);
            probeVector.setFace(probeFace);

            String matchedSubject = featureSpace.knn(FeatureSpace.EUCLIDEAN_DISTANCE, probeVector, 3);
            assertEquals(expectedSubject, matchedSubject, 
                "Probe " + probeFile.getName() + " should match " + expectedSubject);

            // Test ordering by distance
            FeatureSpace.FaceDistancePair[] ordered = featureSpace.orderByDistance(
                FeatureSpace.EUCLIDEAN_DISTANCE, probeVector);
            assertNotNull(ordered);
            assertEquals(9, ordered.length);
            // Closest face should belong to the expected subject
            assertEquals(expectedSubject, ordered[0].getFace().getClassification());
        }

        // 5. Test 3D feature space generation with probe
        double[] probeWeights = tscd.getEigenFaces(trainingFaces.get(0).getPicture(), numEigenVectors);
        FeatureVector probeVector = new FeatureVector();
        probeVector.setFeatureVector(probeWeights);

        double[][] coords = featureSpace.get3dFeatureSpace(probeVector);
        assertNotNull(coords);
        assertTrue(coords.length > 0);
    }
}
