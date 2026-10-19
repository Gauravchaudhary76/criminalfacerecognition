package src;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Interactive 3D visualization panel for face feature vectors.
 * Renders normalized 3D eigenface coordinates with real-time 3D rotation,
 * zooming, classification-based coloring, and probe highlight.
 *
 * @author Gaurav <gauravkr11311@gmail.com>
 * @version 1.0
 */
public class FeatureSpaceVisualizer extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Color[] CLASS_COLORS = {
        new Color(41, 128, 185),   // Blue
        new Color(39, 174, 96),    // Green
        new Color(230, 126, 34),   // Orange
        new Color(142, 68, 173),   // Purple
        new Color(22, 160, 133),   // Teal
        new Color(241, 196, 15),   // Yellow
        new Color(192, 57, 43),    // Red
        new Color(52, 73, 94),     // Dark Slate
        new Color(233, 30, 99),    // Pink
        new Color(0, 188, 212)     // Cyan
    };

    private final double[][] points;
    private final List<String> classifications;
    private final String probeClassification;
    private final boolean hasProbe;

    // View transformation parameters
    private double pitch = 30.0; // rotation around X axis (degrees)
    private double yaw = 45.0;   // rotation around Y axis (degrees)
    private double zoom = 1.0;
    private int lastMouseX;
    private int lastMouseY;

    public FeatureSpaceVisualizer(double[][] points, List<String> classifications, String probeClassification) {
        this.points = points;
        this.classifications = classifications != null ? classifications : new ArrayList<>();
        this.probeClassification = probeClassification;
        this.hasProbe = probeClassification != null && !probeClassification.isEmpty();

        setLayout(new BorderLayout());
        setBackground(new Color(24, 26, 31));
        setPreferredSize(new Dimension(800, 550));

        // Interaction listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                if (e.getClickCount() == 2) {
                    resetView();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;
                yaw += dx * 0.6;
                pitch += dy * 0.6;
                // Clamp pitch
                pitch = Math.max(-85, Math.min(85, pitch));
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                repaint();
            }
        });

        addMouseWheelListener((MouseWheelEvent e) -> {
            if (e.getWheelRotation() < 0) {
                zoom = Math.min(3.0, zoom * 1.1);
            } else {
                zoom = Math.max(0.4, zoom / 1.1);
            }
            repaint();
        });

        // Bottom control toolbar
        JPanel toolbar = new JPanel();
        toolbar.setOpaque(false);
        JButton resetBtn = new JButton("Reset View");
        resetBtn.addActionListener(e -> resetView());
        toolbar.add(resetBtn);
        add(toolbar, BorderLayout.SOUTH);
    }

    public void resetView() {
        pitch = 30.0;
        yaw = 45.0;
        zoom = 1.0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        double scale = Math.min(getWidth(), getHeight()) * 0.35 * zoom;

        // Draw 3D bounding box & axes
        drawBoundingBox(g2, cx, cy, scale);

        // Draw points
        if (points != null && points.length > 0) {
            drawPoints(g2, cx, cy, scale);
        }

        // Draw UI overlay & legend
        drawOverlay(g2);
    }

    /**
     * Projects 3D point (range 0..100) to 2D screen coordinate.
     */
    private int[] project(double x, double y, double z, int cx, int cy, double scale) {
        // Shift center from [0, 100] to [-1, 1]
        double nx = (x - 50.0) / 50.0;
        double ny = (y - 50.0) / 50.0;
        double nz = (z - 50.0) / 50.0;

        double radYaw = Math.toRadians(yaw);
        double radPitch = Math.toRadians(pitch);

        // Rotate around Y axis (yaw)
        double x1 = nx * Math.cos(radYaw) + nz * Math.sin(radYaw);
        double z1 = -nx * Math.sin(radYaw) + nz * Math.cos(radYaw);
        double y1 = ny;

        // Rotate around X axis (pitch)
        double y2 = y1 * Math.cos(radPitch) - z1 * Math.sin(radPitch);
        double z2 = y1 * Math.sin(radPitch) + z1 * Math.cos(radPitch);
        double x2 = x1;

        // Perspective factor
        double dist = 3.5;
        double fov = dist / (dist + z2);

        int screenX = (int) (cx + x2 * scale * fov);
        int screenY = (int) (cy - y2 * scale * fov);
        return new int[]{screenX, screenY};
    }

    private void drawBoundingBox(Graphics2D g2, int cx, int cy, double scale) {
        // 8 corners of the cube [0, 100]
        int[] c000 = project(0, 0, 0, cx, cy, scale);
        int[] c100 = project(100, 0, 0, cx, cy, scale);
        int[] c010 = project(0, 100, 0, cx, cy, scale);
        int[] c110 = project(100, 100, 0, cx, cy, scale);
        int[] c001 = project(0, 0, 100, cx, cy, scale);
        int[] c101 = project(100, 0, 100, cx, cy, scale);
        int[] c011 = project(0, 100, 100, cx, cy, scale);
        int[] c111 = project(100, 100, 100, cx, cy, scale);

        // Draw wireframe box
        g2.setColor(new Color(60, 68, 80));
        g2.setStroke(new BasicStroke(1.0f));

        drawLine(g2, c000, c100);
        drawLine(g2, c100, c110);
        drawLine(g2, c110, c010);
        drawLine(g2, c010, c000);

        drawLine(g2, c001, c101);
        drawLine(g2, c101, c111);
        drawLine(g2, c111, c011);
        drawLine(g2, c011, c001);

        drawLine(g2, c000, c001);
        drawLine(g2, c100, c101);
        drawLine(g2, c010, c011);
        drawLine(g2, c110, c111);

        // Highlight main axes with color
        g2.setStroke(new BasicStroke(2.0f));
        // X Axis: Red
        g2.setColor(new Color(231, 76, 60));
        drawLine(g2, c000, c100);
        g2.drawString("Feature 1 (X)", c100[0] + 5, c100[1] + 5);

        // Y Axis: Green
        g2.setColor(new Color(46, 204, 113));
        drawLine(g2, c000, c010);
        g2.drawString("Feature 2 (Y)", c010[0] + 5, c010[1] - 5);

        // Z Axis: Blue
        g2.setColor(new Color(52, 152, 219));
        drawLine(g2, c000, c001);
        g2.drawString("Feature 3 (Z)", c001[0] + 5, c001[1] + 5);
    }

    private void drawLine(Graphics2D g2, int[] p1, int[] p2) {
        g2.drawLine(p1[0], p1[1], p2[0], p2[1]);
    }

    private void drawPoints(Graphics2D g2, int cx, int cy, double scale) {
        int numClasses = Math.max(1, classifications.size());
        int pointsPerClass = 18;
        int dbPointsCount = numClasses * pointsPerClass;
        int total = Math.min(points.length, dbPointsCount);

        // Draw regular database points
        for (int i = 0; i < total; i++) {
            int classIdx = i / pointsPerClass;
            Color pointColor = CLASS_COLORS[classIdx % CLASS_COLORS.length];

            int[] p = project(points[i][0], points[i][1], points[i][2], cx, cy, scale);

            // Shadow
            int[] shadow = project(points[i][0], 0, points[i][2], cx, cy, scale);
            g2.setColor(new Color(20, 20, 20, 80));
            g2.fillOval(shadow[0] - 3, shadow[1] - 3, 6, 6);

            // Point
            g2.setColor(pointColor);
            g2.fillOval(p[0] - 5, p[1] - 5, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawOval(p[0] - 5, p[1] - 5, 10, 10);
        }

        // Draw Probe point if present (appended at end of features array)
        if (hasProbe && points.length > dbPointsCount) {
            int probeIdx = dbPointsCount;
            int[] p = project(points[probeIdx][0], points[probeIdx][1], points[probeIdx][2], cx, cy, scale);

            // Glowing ring around probe
            g2.setColor(new Color(255, 215, 0, 160));
            g2.setStroke(new BasicStroke(3.0f));
            g2.drawOval(p[0] - 12, p[1] - 12, 24, 24);

            // Center marker
            g2.setColor(new Color(231, 76, 60));
            g2.fillOval(p[0] - 7, p[1] - 7, 14, 14);
            g2.setColor(Color.YELLOW);
            g2.drawOval(p[0] - 7, p[1] - 7, 14, 14);

            // Probe Label
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(Color.YELLOW);
            g2.drawString("★ Probe (" + probeClassification + ")", p[0] + 15, p[1] - 5);
        }
    }

    private void drawOverlay(Graphics2D g2) {
        // Title & instructions at top left
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        g2.drawString("3D Eigenface Feature Space", 20, 30);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(180, 185, 195));
        g2.drawString("Drag to rotate | Scroll to zoom | Double-click to reset", 20, 48);

        // Legend at top right
        int legendX = getWidth() - 170;
        int legendY = 20;
        int itemH = 18;

        if (!classifications.isEmpty()) {
            int boxH = 25 + classifications.size() * itemH + (hasProbe ? itemH : 0);
            g2.setColor(new Color(32, 36, 43, 200));
            g2.fillRoundRect(legendX - 10, legendY - 10, 165, boxH, 8, 8);
            g2.setColor(new Color(70, 78, 90));
            g2.drawRoundRect(legendX - 10, legendY - 10, 165, boxH, 8, 8);

            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(Color.WHITE);
            g2.drawString("Classes", legendX, legendY + 5);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (int i = 0; i < classifications.size(); i++) {
                int y = legendY + 22 + i * itemH;
                Color col = CLASS_COLORS[i % CLASS_COLORS.length];
                g2.setColor(col);
                g2.fillOval(legendX, y - 8, 8, 8);
                g2.setColor(Color.LIGHT_GRAY);
                String name = classifications.get(i);
                if (name.length() > 16) {
                    name = name.substring(0, 13) + "...";
                }
                g2.drawString(name, legendX + 15, y);
            }

            if (hasProbe) {
                int y = legendY + 22 + classifications.size() * itemH;
                g2.setColor(new Color(231, 76, 60));
                g2.fillOval(legendX, y - 8, 8, 8);
                g2.setColor(Color.YELLOW);
                g2.drawString("Probe Face", legendX + 15, y);
            }
        }
    }
}
