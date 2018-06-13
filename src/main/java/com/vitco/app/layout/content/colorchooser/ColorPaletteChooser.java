package com.vitco.app.layout.content.colorchooser;

import com.vitco.app.layout.content.colorchooser.basic.ColorChooserPrototype;
import com.vitco.app.layout.content.colorchooser.basic.Settings;
import com.vitco.app.settings.VitcoSettings;
import com.vitco.app.util.misc.ColorTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * A color chooser that uses a color palette to determine/store the color.
 */
public class ColorPaletteChooser extends ColorChooserPrototype {

    // the currently set color
    private Color color = Color.BLACK;
    // currently selected
    private Point selected = new Point(-10,-10);
    // box size for displaying the colors stored in this palette chooser
    private final static int BOX_SIZE = 18;
    // contains the set colors
    private final HashMap<Point, Color> colors = new HashMap<Point, Color>();

    // true iff this color palette is locked
    private boolean locked = true;

    // buffer image
    private BufferedImage buffer;
    private boolean bufferOutdated = true;

    private void invalidateBuffer() {
        bufferOutdated = true;
    }

    // the panel that actually shows the colors
    private final JPanel panel = new JPanel() {
        // called on repaint
        @Override
        public void paint(Graphics g) {
            super.paintComponents(g);

            // listen to resize events and repaint this component
            panel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    super.componentResized(e);
                    // force a repaint
                    panel.repaint();
                }
            });

            // refresh the buffer if required
            if (bufferOutdated) {
                buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

                Graphics2D g2 = (Graphics2D) buffer.getGraphics();

                // refresh the background
                g2.setColor(Settings.BG_COLOR);
                g2.fillRect(0, 0, this.getWidth(), this.getHeight());

                // draw the outline
                g2.setColor(new Color(200, 200, 200));
                for (Point p : colors.keySet()) {
                    g2.drawRect(p.x*BOX_SIZE - 1, p.y*BOX_SIZE - 1, BOX_SIZE + 1, BOX_SIZE + 1);
                }

                // draw the colors
                for (Map.Entry<Point, Color> col : colors.entrySet()) {
                    g2.setColor(col.getValue());
                    Point p = col.getKey();
                    g2.fillRect(p.x*BOX_SIZE, p.y*BOX_SIZE, BOX_SIZE, BOX_SIZE);
                }

                g2.dispose();

                bufferOutdated = false;
            }

            // draw the buffer
            g.drawImage(buffer, 0, 0, null);

            // highlight all fields that contain the selected color
            for (Map.Entry<Point, Color> col : colors.entrySet()) {
                if (col.getValue().equals(color)) {
                    Point p = col.getKey();
                    // highlight the selected field differently
                    if (selected.equals(p)) { // selected field
                        g.setColor(ColorTools.perceivedBrightness(color) > 127 ? Color.BLACK : Color.WHITE);
                    } else { // not selected field that contains the selected color
                        g.setColor(ColorTools.perceivedBrightness(color) > 127 ? new Color(50, 50, 50) : new Color(200, 200, 200));
                    }
                    g.drawRect(p.x*BOX_SIZE, p.y*BOX_SIZE, BOX_SIZE - 1, BOX_SIZE - 1);
                }
            }
        }
    };

    // =====================================================

    // load color batch (erases existing colors)
    public void loadColors(HashMap<Point, Color> toLoad) {
        colors.clear();
        for (Map.Entry<Point, Color> col : toLoad.entrySet()) {
            colors.put(col.getKey(), col.getValue());
//            System.out.println("colors.put(new Point(" + col.getKey().x + "," + col.getKey().y + "), new Color("
//                    + col.getValue().getRed() + ", "
//                    + col.getValue().getGreen() + ", "
//                    + col.getValue().getBlue() + "));");
        }
        invalidateBuffer();
        forceSelect(color);
        panel.repaint();
    }

    // get the current color batch
    public final HashMap<Point, Color> getColors() {
        return new HashMap<Point, Color>(colors);
    }

    // =============

    // reorder colors (according to similarity)
    public final void reorderColors() {
        // order colors
        HashSet<Integer> unOrdered = new HashSet<Integer>();
        for (Color col : colors.values()) {
            unOrdered.add(col.getRGB());
        }
        List<Color> ordered = ColorTools.orderColors(unOrdered);

        // clear existing colors
        colors.clear();

        // re-add colors
        int i = 0;
        int size = ordered.size();
        for (int y = 0; y < 7 && i < size; y++) {
            for (int x = 0, len = Math.max(15, (int)Math.ceil(ordered.size()/7.0)); x < len && i < size; x++) {
                this.colors.put(new Point(x + 1, y + 1), ordered.get(i++));
            }
        }

        // repaint
        invalidateBuffer();
        panel.repaint();
    }

    // add colors to this color palette (arrange automatically)
    public final void addColors(HashSet<Integer> colors) {
        // sort colors
        List<Color> ordered = ColorTools.orderColors(colors);

        // ============

        // -- add to existing colors colors
        for (Color color : ordered) {
            addColor(color);
        }

        // repaint
        invalidateBuffer();
        panel.repaint();
    }

    // helper - add color to this color palette (arrange automatically)
    private void addColor(final Color color) {
        // sort by color similarity
        ArrayList<Object[]> orderedColors = new ArrayList<Object[]>();
        for (Map.Entry<Point, Color> entry : colors.entrySet()) {
            orderedColors.add(new Object[] {entry.getKey(), entry.getValue()});
        }
        Collections.sort(orderedColors, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return (int) Math.signum(
                        ColorTools.perceivedSimilarity((Color) o2[1], color) -
                                ColorTools.perceivedSimilarity((Color) o1[1], color)
                );
            }
        });
        // check for a color that we can find a suitable match with
        boolean matched = false;
        while (!matched && !orderedColors.isEmpty()) {
            Object[] currentMatch = orderedColors.remove(0);
            // check if there exists a suitable surrounding
            Point p = (Point)currentMatch[0];
            if (addColor(new Point(p.x - 1, p.y), color) ||
                    addColor(new Point(p.x, p.y - 1), color) ||
                    addColor(new Point(p.x + 1, p.y), color) ||
                    addColor(new Point(p.x, p.y + 1), color)
                    ) {
                matched = true;
            }
        }
        if (!matched) {
            for (int y = 1; !matched; y++) {
                for (int x = 1; x < 7 && !matched; x++) {
                    Point p = new Point(x, y);
                    if (!colors.containsKey(p)) {
                        colors.put(p, color);
                        matched = true;
                    }
                }
            }
        }
    }

    // helper to add a color (return true on success)
    private boolean addColor(Point pos, Color col) {
        // check that position is not invalid
        if (pos.x < 1 || pos.y < 1 || pos.y > 7) {
            return false;
        }
        // check that color is not set
        if (colors.containsKey(pos)) {
            return false;
        }
        double maxSim = 0;
        double minSim = 1;
        Color[] neighbours = new Color[] {
                colors.get(new Point(pos.x+1, pos.y)),
                colors.get(new Point(pos.x-1, pos.y)),
                colors.get(new Point(pos.x, pos.y+1)),
                colors.get(new Point(pos.x, pos.y-1))
        };
        for (Color neighbour : neighbours) {
            if (neighbour != null) {
                Double val = ColorTools.perceivedSimilarity(col, neighbour);
                maxSim = Math.max(maxSim, val);
                minSim = Math.min(minSim, val);
            }
        }
        // no neighbouring colors
        if (minSim > maxSim) {
            colors.put(pos, col);
            return true;
        }
        // check that neighbour similarity is consistent
        if (minSim * 1.5 > maxSim) {
            colors.put(pos, col);
            return true;
        }
        return false;
    }

    // ==============

    // constructor
    public ColorPaletteChooser() {
        // add mouse listener (to detect color changes)
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                // find the position that is clicked
                Point p = new Point(e.getX()/BOX_SIZE, e.getY()/BOX_SIZE);

                // deal with the action
                if (e.isControlDown() && !locked) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        // remove color on right click
                        if (colors.remove(p) != null) {
                            invalidateBuffer();
                        }
                    } else {
                        // set color on left click
                        if (!color.equals(colors.put(p, color))) {
                            invalidateBuffer();
                        }
                    }
                    // refresh the current selection
                    forceSelect(color);
                    panel.repaint();
                } else {
                    // select the color
                    select(p);
                }
            }
        });

        // the buffer is outdated when component is re-sized
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                invalidateBuffer();
            }
        });

        // set the content
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        // style this window
        setBorder(BorderFactory.createLineBorder(VitcoSettings.DEFAULT_BORDER_COLOR, 1));
        setBackground(Settings.BG_COLOR);
    }

    // set whether this color palette is locked or not
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    // set the selected color of this color palette
    public final void setColor(float[] hsb) {
        Color newCol = ColorTools.hsbToColor(hsb);
        if (!newCol.equals(color)) {
            color = newCol;
            // ----------
            forceSelect(newCol);
            // ----------
            panel.repaint();
        }
    }

    // =====================================================

    // helper - force a selection (or default to negative)
    private void forceSelect(Color color) {
        // select the nearest point with that color as selected
        // defaults to (-10,-10) if the color doesn't exist
        float dist = Float.MAX_VALUE;
        Point nP = new Point(-10,-10);
        for (Map.Entry<Point, Color> entry : colors.entrySet()) {
            if (entry.getValue().equals(color)) {
                float nDist = (float) selected.distance(entry.getKey());
                if (nDist < dist) {
                    dist = nDist;
                    nP = entry.getKey();
                }
            }
        }
        selected = nP;
    }

    // ---------------------
    // helper - select a color by position
    private boolean select(Point newSelected) {
        Color color = colors.get(newSelected);
        boolean containsEntry = color != null;
        if (containsEntry) {
            notifyListeners(ColorTools.colorToHSB(color));
            if (!selected.equals(newSelected)) {
                selected = newSelected;
                panel.repaint();
            }
        }
        return containsEntry;
    }
    // helper - to select neighbour
    private void selectNeighbour(int x, int y) {
        int i = 1;
        boolean containsEntry;
        Point p = new Point();
        do {
            p.setLocation(selected.x + i*x, selected.y + i*y);
            containsEntry = select(p);
            i++;
        } while (!containsEntry && i < 50);
    }
    // select relative to currently selected
    public void left() {
        selectNeighbour(-1, 0);
    }
    public void right() {
        selectNeighbour(1, 0);
    }
    public void up() {
        selectNeighbour(0, -1);
    }
    public void down() {
        selectNeighbour(0, 1);
    }
    // -------------------------

    // load the default colors
    public final void loadDefaultColors() {
        colors.clear();
        colors.put(new Point(3,6), new Color(64, 51, 193));
        colors.put(new Point(4,1), new Color(44, 159, 93));
        colors.put(new Point(12,7), new Color(158, 44, 165));
        colors.put(new Point(5,1), new Color(14, 123, 34));
        colors.put(new Point(14,7), new Color(77, 7, 105));
        colors.put(new Point(6,1), new Color(6, 90, 4));
        colors.put(new Point(3,4), new Color(125, 155, 227));
        colors.put(new Point(10,9), new Color(239, 143, 162));
        colors.put(new Point(7,5), new Color(64, 83, 180));
        colors.put(new Point(7,1), new Color(16, 48, 3));
        colors.put(new Point(8,9), new Color(206, 66, 139));
        colors.put(new Point(7,2), new Color(9, 62, 84));
        colors.put(new Point(10,7), new Color(246, 88, 190));
        colors.put(new Point(2,1), new Color(131, 231, 214));
        colors.put(new Point(11,1), new Color(96, 12, 15));
        colors.put(new Point(1,1), new Color(208, 252, 248));
        colors.put(new Point(9,1), new Color(39, 3, 18));
        colors.put(new Point(7,6), new Color(95, 95, 210));
        colors.put(new Point(10,3), new Color(114, 14, 5));
        colors.put(new Point(3,1), new Color(82, 198, 152));
        colors.put(new Point(15,1), new Color(237, 182, 57));
        colors.put(new Point(3,5), new Color(79, 87, 213));
        colors.put(new Point(7,4), new Color(40, 74, 144));
        colors.put(new Point(13,1), new Color(168, 65, 17));
        colors.put(new Point(13,7), new Color(114, 21, 132));
        colors.put(new Point(3,9), new Color(84, 39, 99));
        colors.put(new Point(10,4), new Color(150, 12, 22));
        colors.put(new Point(6,3), new Color(27, 81, 147));
        colors.put(new Point(3,7), new Color(76, 43, 156));
        colors.put(new Point(7,3), new Color(22, 69, 114));
        colors.put(new Point(10,6), new Color(219, 55, 129));
        colors.put(new Point(4,3), new Color(106, 188, 204));
        colors.put(new Point(9,9), new Color(224, 106, 145));
        colors.put(new Point(11,7), new Color(198, 85, 183));
        colors.put(new Point(5,3), new Color(57, 129, 177));
        colors.put(new Point(10,1), new Color(66, 9, 23));
        colors.put(new Point(3,3), new Color(160, 234, 228));
        colors.put(new Point(6,9), new Color(154, 40, 126));
        colors.put(new Point(10,2), new Color(81, 16, 1));
        colors.put(new Point(7,7), new Color(138, 115, 243));
        colors.put(new Point(8,1), new Color(14, 24, 1));
        colors.put(new Point(7,9), new Color(179, 48, 130));
        colors.put(new Point(10,5), new Color(183, 25, 73));
        colors.put(new Point(3,8), new Color(82, 39, 123));
        colors.put(new Point(14,1), new Color(198, 119, 31));
        colors.put(new Point(4,9), new Color(102, 38, 108));
        colors.put(new Point(12,1), new Color(135, 41, 15));
        colors.put(new Point(5,9), new Color(133, 39, 126));
    }

}
