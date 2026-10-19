/**
 * Main application class for the face recognition system.
 * Provides the GUI interface and coordinates face recognition operations.
 * This class serves as the entry point and controller for the face recognition
 * application, managing the user interface, image processing, and recognition workflow.
 *
 * @author Gaurav <gauravkr11311@gmail.com>
 * @version 1.0
 * @since 1.0
 */

package src;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;

public class Main extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static final Dimension IDEAL_IMAGE_SIZE = new Dimension(48, 64);
    private static final int NUM_EIGEN_VECTORS = 10;
    private static final int CLASSIFICATION_THRESHOLD = 5;
    private static final int FACE_BROWSER_WIDTH = 400;
    private static final int FACE_BROWSER_HEIGHT = 300;
    private static final int PROGRESS_TIMER_DELAY = 100;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 480;
    private static final int BUTTON_FONT_SIZE = 18;
    private static final String BUTTON_FONT = "Verdana";
    private static final int BUTTON_PADDING_Y = 30;
    private static final int BUTTON_PADDING_X = 110;
    private static final int BUTTON_INSETS = 10;

    private TSCD eigenFaces;
    private FeatureSpace featureSpace;
    private JPanel main;
    private ImageBackgroundPanel background;
    private JProgressBar statusBar;
    private JList<File> fileList;
    private JButton loadImageButton;
    private JButton trainButton;
    private JButton probeButton;
    private JButton cropImageButton;
    private ImageIcon averageFaceIcon;
    private JLabel averageFaceLabel;
    private Container container;
    private FaceItem faceCandidate;
    private FaceBrowser faceBrowser;
    private JScrollPane faceBrowserScrollPane;
    private JButton displayFeatureSpaceButton;
    private FeatureVector lastFeatureVector;
    private ArrayList<Face> faces;
    private ArrayList<FeatureVector> trainingSet;
    private String classification;

    /**
     * Creates a new Main instance.
     * Initializes the core components including eigenfaces processor,
     * feature space, face browser, and training set storage.
     */
    public Main() {
        eigenFaces = new TSCD();
        featureSpace = new FeatureSpace();
        faceBrowser = new FaceBrowser();
        trainingSet = new ArrayList<>();
        faces = new ArrayList<>();
        fileList = new JList<>();
    }

    /**
     * Initializes the component layout with system look and feel.
     * Sets up the main container and initializes all UI components.
     */
    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            // Use default look and feel if system look and feel is not available
        }

        container = this;
        generalInit(container);
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    /**
     * Main entry point to launch the face recognition application directly.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            JFrame frame = new JFrame("3D Face Recognition System");
            Main mainApp = new Main();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(mainApp);
            mainApp.init();
            frame.pack();
            frame.setSize(1200, 750);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /**
     * Initializes the general layout and components of the application.
     * Sets up the main panel, background, buttons, face candidate,
     * status bar, face browser, and right panel.
     *
     * @param container the main container to initialize
     */
    private void generalInit(Container container) {
        container.setLayout(new BorderLayout());
        main = new JPanel();
        background = new ImageBackgroundPanel();
        container.add(background, BorderLayout.CENTER);

        initializeButtons();
        initializeFaceCandidate();
        initializeStatusBar();
        initializeFaceBrowser();
        initializeRightPanel();
    }

    /**
     * Initializes all control buttons with appropriate text and state.
     * Creates buttons for loading images, cropping, computing eigenvectors,
     * face identification, and result display.
     */
    private void initializeButtons() {
        loadImageButton = createButton("Load Images", true);
        cropImageButton = createButton("Crop Images", false);
        trainButton = createButton("Compute Eigen Vectors", false);
        probeButton = createButton("Identify Face", false);
        displayFeatureSpaceButton = createButton("Display Result Chart", false);
    }

    /**
     * Creates a styled button with the specified text and enabled state.
     *
     * @param text the button text
     * @param enabled whether the button should be initially enabled
     * @return the created and configured button
     */
    private JButton createButton(String text, boolean enabled) {
        JButton button = new JButton(text);
        button.setFont(new Font(BUTTON_FONT, Font.PLAIN, BUTTON_FONT_SIZE));
        button.setEnabled(enabled);
        button.addActionListener(this);
        return button;
    }

    /**
     * Initializes the face candidate component for displaying
     * the current face being processed.
     */
    private void initializeFaceCandidate() {
        faceCandidate = new FaceItem();
        faceCandidate.setBorder(BorderFactory.createRaisedBevelBorder());
        faceCandidate.setVisible(false);
    }

    /**
     * Initializes the status bar for showing progress information.
     * Creates a horizontal progress bar with percentage display.
     */
    private void initializeStatusBar() {
        statusBar = new JProgressBar(0, 100);
        statusBar.setStringPainted(true);
        statusBar.setString("Ready");
        container.add(statusBar, BorderLayout.PAGE_END);
    }

    /**
     * Initializes the face browser component for displaying
     * the collection of face images. Sets up scrolling and sizing.
     */
    private void initializeFaceBrowser() {
        faceBrowserScrollPane = new JScrollPane(faceBrowser);
        faceBrowserScrollPane.setPreferredSize(new Dimension(FACE_BROWSER_WIDTH, FACE_BROWSER_HEIGHT));
        faceBrowserScrollPane.setVisible(false);
    }

    /**
     * Initializes the average face label for displaying the computed average face.
     *
     * @param panel the panel to add the label to
     */
    private void initializeAverageFaceLabel(JPanel panel) {
        averageFaceLabel = new JLabel();
        averageFaceLabel.setVisible(false);
        panel.add(averageFaceLabel);
    }

    /**
     * Initializes the right panel containing the logo, candidate, and control buttons.
     * Sets up the layout and adds all necessary components.
     */
    private void initializeRightPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();

        try {
            addLogoToPanel(right);
        } catch (IOException ex) {
            System.out.println("Image face.png missing\n" + ex);
        }

        initializeAverageFaceLabel(right);
        right.add(faceCandidate);
        addButtonsToPanel(right, gbc);
        container.add(right, BorderLayout.EAST);
    }

    /**
     * Creates and configures GridBagConstraints for button layout.
     *
     * @return configured GridBagConstraints object
     */
    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.ipady = BUTTON_PADDING_Y;
        gbc.ipadx = BUTTON_PADDING_X;
        gbc.insets = new Insets(BUTTON_INSETS, BUTTON_INSETS * 2, BUTTON_INSETS, BUTTON_INSETS * 2);
        return gbc;
    }

    /**
     * Adds the application logo to the specified panel.
     *
     * @param panel the panel to add the logo to
     * @throws IOException if the logo image file cannot be read
     */
    private void addLogoToPanel(JPanel panel) throws IOException {
        BufferedImage myPicture = null;
        try (InputStream is = getClass().getResourceAsStream("/face.png")) {
            if (is != null) {
                myPicture = ImageIO.read(is);
            }
        }
        if (myPicture == null) {
            File f = new File("src/main/resources/face.png");
            if (f.exists()) {
                myPicture = ImageIO.read(f);
            }
        }
        if (myPicture != null) {
            JLabel picLabel = new JLabel(new ImageIcon(myPicture));
            panel.add(picLabel);
        }
    }

    /**
     * Adds all control buttons to the specified panel using the given constraints.
     *
     * @param panel the panel to add buttons to
     * @param gbc the GridBagConstraints to use for layout
     */
    private void addButtonsToPanel(JPanel panel, GridBagConstraints gbc) {
        panel.add(loadImageButton, gbc);
        gbc.gridy = 4;
        panel.add(trainButton, gbc);
        gbc.gridy = 6;
        panel.add(probeButton, gbc);
        gbc.gridy = 8;
        panel.add(displayFeatureSpaceButton, gbc);
    }

    /**
     * Handles button click events from the UI.
     * Routes each action to its appropriate handler method.
     *
     * @param event the action event containing the source button
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == loadImageButton) {
            loadImage();
        } else if (source == cropImageButton) {
            crop();
        } else if (source == trainButton) {
            train();
        } else if (source == probeButton) {
            probe();
        } else if (source == displayFeatureSpaceButton) {
            displayFeatureSpace();
        }
    }

    /**
     * Displays the feature space visualization in 3D.
     * Creates and shows an interactive 3D chart of the feature space
     * with the current probe vector highlighted.
     */
    private void displayFeatureSpace() {
        double[][] features = featureSpace.get3dFeatureSpace(lastFeatureVector);
        if (features == null || featureSpace.getFeatureSpaceSize() == 0) {
            JOptionPane.showMessageDialog(this, "No feature space data available. Please load images and compute eigen vectors first.");
            return;
        }

        JFrame frame = new JFrame("3D Face Recognition Feature Space Results");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        FeatureSpaceVisualizer visualizer = new FeatureSpaceVisualizer(
            features, 
            featureSpace.getClassifications(), 
            classification
        );
        frame.setContentPane(visualizer);
        frame.setSize(950, 680);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Processes a probe image for face recognition.
     * Loads the selected image, extracts features, and attempts to
     * identify the face using the trained eigenfaces.
     */
    private void probe() {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select Probe Face Image");
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                long startTime = System.currentTimeMillis();
                updateStatus("Loading Probe Image...");
                File file = fc.getSelectedFile();
                try {
                    Face f = new Face(file);
                    f.setDescription("Probe Face: " + file.getName());
                    f.load(true);
                    processFaceRecognition(f);
                    displayResults(startTime);
                } catch (MalformedURLException e) {
                    System.err.println("There was a problem opening the file : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("There was a problem with the file chooser : " + e.getMessage());
        }
    }

    /**
     * Processes face recognition for a given face.
     * Extracts features and performs k-nearest neighbor classification.
     *
     * @param f the face to process and classify
     */
    private void processFaceRecognition(Face f) {
        double[] rslt = eigenFaces.getEigenFaces(f.getPicture(), NUM_EIGEN_VECTORS);
        lastFeatureVector = new FeatureVector();
        lastFeatureVector.setFeatureVector(rslt);
        lastFeatureVector.setFace(f);

        classification = featureSpace.knn(FeatureSpace.EUCLIDEAN_DISTANCE, lastFeatureVector, CLASSIFICATION_THRESHOLD);
        faceCandidate.setFace(f);
        faceCandidate.setVisible(true);

        FeatureSpace.FaceDistancePair[] distances = featureSpace.orderByDistance(FeatureSpace.EUCLIDEAN_DISTANCE, lastFeatureVector);
        faceBrowser.orderAs(distances);
        faceBrowser.highlightClassifiedAs(classification);

        updateStatus("Face probed. Matched identity: " + (classification != null ? classification : "Unknown"));
    }

    /**
     * Displays the recognition results and processing time.
     *
     * @param startTime the time when processing started
     */
    private void displayResults(long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        Component parent = (container != null) ? container : this;
        JOptionPane.showMessageDialog(parent, 
            String.format("Match Execution Time: %.2f seconds.\nIdentified Face: %s", 
                elapsedTime / 1000.0, (classification != null ? classification : "Unknown")),
            "Recognition Result",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Handles the load image button action.
     * Opens a file chooser for selecting a face images directory and loads them
     * into the face browser.
     */
    private void loadImage() {
        try {
            faces = new ArrayList<>();
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select Directory Containing Face Images");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                setupMainPanel();
                File folder = fc.getSelectedFile();
                try {
                    loadImagesFromFolder(folder);
                    setupFaceBrowser();
                    enableTrainingButtons();
                } catch (MalformedURLException e) {
                    System.err.println("There was a problem opening a file : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("There was a problem with the file chooser : " + e.getMessage());
        }
    }

    /**
     * Sets up the main panel for displaying face images.
     * Removes the background and adds the main content panel.
     */
    private void setupMainPanel() {
        container.remove(background);
        main.setLayout(new BorderLayout());
        container.add(main, BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
    }

    /**
     * Checks if a file has an image extension.
     */
    private static boolean isImageFile(File f) {
        if (f == null || !f.isFile()) {
            return false;
        }
        String n = f.getName().toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".bmp");
    }

    /**
     * Extracts a subject identity from a file or folder name.
     */
    private String extractClassification(File file, String folderName) {
        if (folderName != null && !folderName.isEmpty()) {
            return folderName;
        }
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        if (name.contains("_")) {
            return name.substring(0, name.indexOf('_'));
        }
        if (name.contains("-")) {
            return name.substring(0, name.indexOf('-'));
        }
        return name;
    }

    /**
     * Loads all face images from a specified folder (including subdirectories).
     *
     * @param folder the folder containing face images
     * @throws MalformedURLException if an image file URL is malformed
     */
    private void loadImagesFromFolder(File folder) throws MalformedURLException {
        File[] subFolders = folder.listFiles(File::isDirectory);
        if (subFolders == null) {
            subFolders = new File[0];
        }

        trainingSet.clear();
        faces = new ArrayList<>();
        faceBrowser.empty();

        List<File> allImageFiles = new ArrayList<>();

        // 1. Check for images directly inside folder
        File[] directFiles = folder.listFiles(Main::isImageFile);
        if (directFiles != null) {
            for (File file : directFiles) {
                allImageFiles.add(file);
                String cls = extractClassification(file, null);
                Face f = new Face(file);
                f.setClassification(cls);
                f.setDescription("Face: " + file.getName());
                f.load(true);
                faces.add(f);
                faceBrowser.addFace(f);
            }
        }

        // 2. Check for images in person subfolders
        for (File sub : subFolders) {
            File[] subFiles = sub.listFiles(Main::isImageFile);
            if (subFiles != null) {
                for (File file : subFiles) {
                    allImageFiles.add(file);
                    String cls = sub.getName();
                    Face f = new Face(file);
                    f.setClassification(cls);
                    f.setDescription("Face: " + sub.getName() + "/" + file.getName());
                    f.load(true);
                    faces.add(f);
                    faceBrowser.addFace(f);
                }
            }
        }

        fileList.setListData(allImageFiles.toArray(new File[0]));
        faceBrowser.revalidate();
        faceBrowser.repaint();

        String statusMsg = faces.size() + " face images loaded from " + 
            (subFolders.length > 0 ? (subFolders.length + " subdirectories") : folder.getName());
        updateStatus(statusMsg);
    }

    /**
     * Sets up the face browser component in the main panel.
     * Configures the scroll pane and makes it visible.
     */
    private void setupFaceBrowser() {
        faceBrowserScrollPane.setViewportView(faceBrowser);
        faceBrowserScrollPane.setVisible(true);
        main.add(faceBrowserScrollPane, BorderLayout.CENTER);
        main.revalidate();
        main.repaint();
    }

    /**
     * Enables the training-related buttons after images are loaded.
     */
    private void enableTrainingButtons() {
        trainButton.setEnabled(faces != null && !faces.isEmpty());
        cropImageButton.setEnabled(faces != null && !faces.isEmpty());
    }

    /**
     * Initiates the face cropping process for loaded images.
     */
    private void crop() {
        if (faces == null || faces.isEmpty()) {
            return;
        }
        int count = 0;
        for (Face f : faces) {
            updateProgress(count, faces.size());
            try {
                f.load(true);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            count++;
        }
        resetProgress();
        faceBrowser.refresh();
    }

    /**
     * Updates the progress bar with current progress.
     *
     * @param count current progress count
     * @param total total number of steps
     */
    private void updateProgress(int count, int total) {
        int val = total > 0 ? (count * 100) / total : 0;
        statusBar.setValue(val);
        statusBar.setString(val + "%");
        statusBar.paintImmediately(statusBar.getVisibleRect());
    }

    /**
     * Resets the progress bar to zero.
     */
    private void resetProgress() {
        statusBar.setValue(0);
    }

    /**
     * Initiates the training process using loaded face images.
     * Computes eigenfaces and builds the feature space.
     */
    private void train() {
        if (faces == null || faces.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load face images first.");
            return;
        }

        final ProgressTracker progress = new ProgressTracker();
        Runnable calc = () -> {
            eigenFaces.processTrainingSet(faces.toArray(new Face[0]), progress);
            trainingSet.clear();
            featureSpace = new FeatureSpace();
            for (Face f : faces) {
                double[] rslt = eigenFaces.getEigenFaces(f.getPicture(), NUM_EIGEN_VECTORS);
                FeatureVector fv = new FeatureVector();
                fv.setFeatureVector(rslt);
                fv.setFace(f);
                trainingSet.add(fv);
                featureSpace.insertIntoDatabase(f, rslt);
            }

            BufferedImage avg = getAverageFaceImage();
            if (avg != null) {
                averageFaceIcon = new ImageIcon(avg);
                averageFaceLabel.setIcon(averageFaceIcon);
                averageFaceLabel.setVisible(true);
            }
            probeButton.setEnabled(true);
            displayFeatureSpaceButton.setEnabled(true);
            updateStatus("Training complete! " + faces.size() + " faces in database.");
        };

        progress.run(main, calc, "Training Face Recognition Model");
    }

    /**
     * Updates the status message in the progress bar.
     *
     * @param message the status message to display
     */
    private void updateStatus(String message) {
        statusBar.setString(message);
        statusBar.paintImmediately(statusBar.getVisibleRect());
    }

    /**
     * Saves a BufferedImage to a file.
     *
     * @param f the file to save to
     * @param img the image to save
     * @throws IOException if there is an error writing the file
     */
    public void saveImage(File f, BufferedImage img) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (writers.hasNext()) {
            ImageWriter writer = writers.next();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(f)) {
                writer.setOutput(ios);
                writer.write(img);
            }
        }
    }

    /**
     * Gets the average face image from the eigenfaces computation.
     *
     * @return the average face as a BufferedImage
     */
    public BufferedImage getAverageFaceImage() {
        if (eigenFaces == null || eigenFaces.getAverageFace() == null) {
            return null;
        }
        return CreateImageFromMatrix(eigenFaces.getAverageFace().getRowPackedCopy(), IDEAL_IMAGE_SIZE.width);
    }

    /**
     * Creates a BufferedImage from a matrix of pixel values.
     *
     * @param img the array of pixel values
     * @param width the width of the image
     * @return the created BufferedImage
     */
    public static BufferedImage CreateImageFromMatrix(double[] img, int width) {
        if (img == null || width <= 0) {
            return null;
        }
        BufferedImage bi = new BufferedImage(width, img.length / width, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < img.length; i++) {
            int x = i % width;
            int y = i / width;
            int gray = (int) img[i];
            gray = Math.max(0, Math.min(255, gray));
            bi.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
        }
        return bi;
    }

    /**
     * Inner class for tracking progress of long-running operations.
     * Provides methods for updating and displaying progress information.
     */
    public static class ProgressTracker {
        private ProgressMonitor progressMonitor;
        private Timer timer;
        private String sProgress;
        private boolean bFinished;

        /**
         * Updates the progress message.
         *
         * @param message the new progress message
         */
        public void advanceProgress(final String message) {
            sProgress = message;
            if (progressMonitor != null) {
                progressMonitor.setProgress(1);
                progressMonitor.setNote(sProgress);
            }
        }

        /**
         * Timer listener for updating the progress monitor.
         */
        private class TimerListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (progressMonitor != null) {
                    progressMonitor.setProgress(1);
                    progressMonitor.setNote(sProgress);
                    if (progressMonitor.isCanceled() || bFinished) {
                        timer.stop();
                    }
                }
            }
        }

        /**
         * Runs a task with progress monitoring.
         *
         * @param parent the parent component for the progress dialog
         * @param calc the task to run
         * @param title the title for the progress dialog
         */
        public void run(JComponent parent, final Runnable calc, String title) {
            progressMonitor = new ProgressMonitor(parent, title, "", 0, 100);
            timer = new Timer(PROGRESS_TIMER_DELAY, new TimerListener());
            bFinished = false;
            sProgress = "Starting...";

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    calc.run();
                    return null;
                }

                @Override
                protected void done() {
                    bFinished = true;
                    if (progressMonitor != null) {
                        progressMonitor.close();
                    }
                }
            };
            worker.execute();
            timer.start();
        }

        /**
         * Marks the progress tracking as finished.
         */
        public void finished() {
            bFinished = true;
            if (progressMonitor != null) {
                progressMonitor.close();
            }
        }
    }
}
