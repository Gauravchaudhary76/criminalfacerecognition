# Face Recognition System

[![Build Status](https://github.com/Gauravchaudhary76/criminalfacerecognition/actions/workflows/build.yml/badge.svg)](https://github.com/Gauravchaudhary76/criminalfacerecognition/actions/workflows/build.yml)
[![Java Version](https://img.shields.io/badge/Java-21%2B%20LTS-orange)](https://www.oracle.com/java/technologies/downloads/)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](License.txt)

A modernized Java 25+ face recognition system using eigenfaces and Turk & Pentland dimensionality reduction for face recognition under varying expressions, occlusions (e.g., glasses), and pose variations.

---

## Features

- **Face Recognition with Eigenfaces**:
  - Two-Stage Classification and Detection (TSCD).
  - Robust against expressions, glasses, facial hair, and illumination changes.
  - K-Nearest Neighbor (k-NN) classification in eigen-feature space with Euclidean distance.
- **Interactive Swing GUI**:
  - User Authentication (Login & Registration) with SHA-256 password security.
  - Automatic fallback between MySQL database and zero-config local storage (`users.txt`).
  - Face Browser panel displaying all loaded face candidates with distance-sorted similarity ranking.
  - Color-highlighted match detection and candidate preview.
  - Interactive **3D Feature Space Visualization** with real-time mouse rotation, zoom, coordinate axes, and probe highlight.
- **Sample Dataset Included**:
  - Includes synthetic test faces in `data/sample-faces/` for immediate testing.
- **Self-Contained & Modern Build**:
  - Built on standard Java 25 LTS with Maven Wrapper (`./mvnw`).
  - No legacy native 32-bit DLLs or manual JAR downloads required.
  - Packaged with executable fat JAR (`jar-with-dependencies`).

---

## Technical Details

- **Core Algorithm**: Turk & Pentland Eigenface decomposition using surrogate covariance matrix ($A^T A$) with full eigenvector back-projection ($U = A \cdot V$) and L2 unit normalization.
- **Linear Algebra**: Jama Matrix decomposition.
- **UI & Graphics**: Java Swing & Java 2D with anti-aliasing and 3D projection rendering.
- **Authentication**: SHA-256 hashing with Base64 encoding.

---

## Prerequisites

- **Java Development Kit (JDK) 25** or higher.
- **Maven 3.6+** (or use the bundled `mvnw` wrapper).
- *(Optional)* MySQL 5.7+ or 8.0+ for centralized user authentication. If MySQL is not running, the application seamlessly uses local secure storage (`users.txt`).

---

## Building the Project

Clone the repository and build using Maven or the Maven Wrapper:

```bash
# Using Maven Wrapper (Windows)
.\mvnw.cmd clean package

# Using Maven Wrapper (Linux / macOS)
./mvnw clean package

# Or using installed Maven
mvn clean package
```

To run all unit tests and the end-to-end pipeline test:

```bash
mvn test
```

---

## Running the Application

### Option 1: Standalone Executable Fat JAR
Run the pre-packaged self-contained JAR:

```bash
java -jar target/face-recognition-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Option 2: Run Login & Authentication GUI via Maven
```bash
mvn exec:java
```

> **Default Demo Account**: If MySQL is not configured, a default demo account is automatically initialized:
> - **Username**: `admin`
> - **Password**: `admin123`
>
> You can also register a new user directly in the UI.

### Option 3: Launch Face Recognition Workbench Directly
```bash
mvn exec:java -Dexec.mainClass="src.Main"
```

---

## Quick Start Tutorial

1. Launch the application (login with `admin` / `admin123` or run `src.Main`).
2. Click **Load Images** and select `data/sample-faces` (or any folder containing face images / subject subfolders).
3. Click **Compute Eigen Vectors** to train the eigenface space. The average face will be computed and displayed.
4. Click **Identify Face** and choose one of the probe images from `data/sample-faces/probes/` (e.g. `probe_subject01.png`).
   - The system extracts features, sorts the face browser by similarity, and highlights the best match!
5. Click **Display Result Chart** to open the interactive 3D feature space.
   - Click and drag with mouse to rotate the 3D space.
   - Use the mouse wheel to zoom in and out.
   - Double-click to reset view.

---

## Project Structure

```
face-recognition/
├── .github/workflows/build.yml     # GitHub Actions CI build & test workflow
├── data/sample-faces/               # Sample face dataset (subjects & probes)
├── src/main/java/src/
│   ├── Main.java                   # Main UI workbench controller
│   ├── FrontEnd.java               # Authentication & login screen
│   ├── Face.java                   # Face representation & boundary detection
│   ├── TSCD.java                   # Eigenfaces computation & matrix decomposition
│   ├── FeatureSpace.java           # Feature space management & k-NN matching
│   ├── FeatureSpaceVisualizer.java # Interactive 3D feature space renderer
│   ├── FeatureVector.java          # Feature vector container
│   ├── FaceBrowser.java            # Scrollable face browser component
│   ├── Picture.java                # Image processing & pixel extraction
│   ├── DistanceMeasure.java        # Metric interface (Euclidean)
│   └── ImageBackgroundPanel.java   # UI background panel with resource loader
├── src/main/resources/             # Application icons & backgrounds
├── src/test/java/src/              # Unit & integration test suite
│   ├── DistanceMeasureTest.java
│   ├── FeatureSpaceTest.java
│   ├── PictureTest.java
│   ├── TSCDTest.java
│   ├── FaceRecognitionEndToEndTest.java
│   └── SampleDatasetGenerator.java
├── pom.xml                         # Maven build configuration
└── mvnw, mvnw.cmd                  # Maven wrappers
```

---

## Author

- **Gaurav** ([@Gauravchaudhary76](https://github.com/Gauravchaudhary76) | [gauravkr11311@gmail.com](mailto:gauravkr11311@gmail.com))

---

## License

This project is licensed under the GNU General Public License v3 - see the [License.txt](License.txt) file for details.
