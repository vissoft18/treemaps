package UserControl.Visualiser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import statistics.StatisticalTracker;
import treemap.dataStructure.OrderEquivalentLineSegment;
import treemap.dataStructure.OrderEquivalentTreeMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author Max Sondag
 */
public class TreeMapVisualisation extends JPanel {

    Timer timer = new Timer();

    private Rectangle treeMapRectangle;
    private TreeMap newTreeMap;
    private TreeMap oldTreeMap;

    private boolean animationEnabled = true;
    private final int animationMinSteps = 5;
    private final int animationMaxSteps = 40;
    private int animationSteps = 20;
    private final int timeBetweenSteps = 20;//time in milliseconds between steps
    private int animationProgress = 0;
    private boolean orderEnabled = false;
    private boolean orderLabelsEnabled = false;

    public boolean drawWeightsEnabled = false;
    public boolean drawLabelsEnabled = true;

    //Whether the new treeMap was the result of a move
    private boolean movePerformed = false;
    /**
     * whether we have finished painting the treemap. Happens after
     * interpolation
     */
    public volatile boolean treeMapRepaint = false;

    public TreeMapVisualisation() {
        newTreeMap = null;
        oldTreeMap = null;

    }

    public void setAnimationSpeed(double percentage) {
        this.animationSteps = (int) (animationMinSteps + (animationMaxSteps - animationMinSteps) * ((100 - percentage) / 100));
    }

    public Rectangle getTreemapRectangle() {
        updateTreeMapRectangle();
        return treeMapRectangle;
    }

    public boolean isShowingTreeMap() {
        if (newTreeMap == null) {
            return false;
        }
        return true;
    }

    public void updateTreeMapRectangle() {
        treeMapRectangle = new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    public void updateTreeMap(TreeMap treeMap) {
        movePerformed = false;
        oldTreeMap = newTreeMap;
        newTreeMap = treeMap;
        treeMapRepaint = false;

        repaint();
    }

    public void movePerformed(TreeMap treeMap) {
        updateTreeMap(treeMap);
        //we can set it afterwards as repaint is asynchronous
        movePerformed = true;

    }

    private TreeMap worstAspectRatio() {
        TreeMap referenceTm;
        //If a move is performed we follow the one that had the worst aspect
        //ratio in the old treemap. Otherwise we find the new one
        if (movePerformed == true) {
            referenceTm = oldTreeMap;
        } else {
            referenceTm = newTreeMap;

        }

        TreeMap worstTm = null;
        double worstRatio = 0;
        for (TreeMap tm : referenceTm.getAllLeafs()) {
            if (tm.getRectangle().getAspectRatio() > worstRatio) {
                worstRatio = tm.getRectangle().getAspectRatio();
                worstTm = tm;
            }
        }
        //Need to make sure we follow the correct one
        if (movePerformed == true) {
            worstTm = newTreeMap.getChildWithLabel(worstTm.getLabel());
        }
        return worstTm;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (oldTreeMap != null && oldTreeMap != newTreeMap && animationEnabled == true) {

            drawInterpolationTreeMap(g, treeMapRectangle, oldTreeMap, newTreeMap, 0);

            animationProgress++;
            if (animationProgress <= animationSteps) {
                delayedRepaint(timeBetweenSteps);
            } else {
                animationProgress = 0;
                //used to draw the outlines

                drawTreeMap(g, newTreeMap, 0);
                drawOutlinesAndLabels(g, treeMapRectangle, newTreeMap, 0);

                //indicate that we have finished drawing
                treeMapRepaint = true;
            }

        } else if (newTreeMap != null) {

            drawTreeMap(g, newTreeMap, 0);
            drawOutlinesAndLabels(g, treeMapRectangle, newTreeMap, 0);

            //indicate that we have finished drawing
            treeMapRepaint = true;
        }

    }

    long lastTime = System.currentTimeMillis();

    private void requestRepaint() {
        long time = System.currentTimeMillis();
        lastTime = time;
        repaint();
    }

    private void delayedRepaint(int time) {
        //time is in second    
        timer.cancel();
        timer = new Timer();
        TimerTask action = new TimerTask() {
            public void run() {
                requestRepaint();
            }

        };

        timer.schedule(action, time); //this starts the task
    }

    protected Rectangle getInterpolatedRectangle(TreeMap oldTm, TreeMap newTm, double progressPercentage) {
        Rectangle oldR = null;
        if (oldTm != null) {
            oldR = oldTm.getRectangle();
        }
        Rectangle newR = null;
        if (newTm != null) {
            newR = newTm.getRectangle();
        }

        double oldX = 0, oldY = 0, oldW = 0, oldH = 0, newX = 0, newY = 0, newW = 0, newH = 0;

        if (oldR == null && newR == null) {
            System.out.println("Both rectangles are null, cannot interpolate");
            return null;
        }

        if (oldR != null) {
            oldX = oldR.getX();
            oldY = oldR.getY();
            oldW = oldR.getWidth();
            oldH = oldR.getHeight();
        }
        if (newR != null) {
            newX = newR.getX();
            newY = newR.getY();
            newW = newR.getWidth();
            newH = newR.getHeight();
        }
        if (oldR == null) {
            //oldR does not exists. put in in the middle of newR as a 1x1 rectangle
            oldX = newX + newW / 2;
            oldY = newY + newH / 2;
            oldW = 1;
            oldH = 1;
        }
        if (newR == null) {
            //newR does not exists. put in in the middle of oldR as a 1x1 rectangle
            newX = oldX + oldW / 2;
            newY = oldY + oldH / 2;
            newW = 1;
            newH = 1;
        }

        double xDifference = newX - oldX;
        double yDifference = newY - oldY;
        double widthDifference = newW - oldW;
        double heightDifference = newH - oldH;

        double interpolatedX = oldX + xDifference * progressPercentage;
        double interpolatedY = oldY + yDifference * progressPercentage;
        double interpolatedWidth = oldW + widthDifference * progressPercentage;
        double interpolatedHeight = oldH + heightDifference * progressPercentage;

        Rectangle interpolated = new Rectangle(interpolatedX, interpolatedY, interpolatedWidth, interpolatedHeight);
        return interpolated;
    }

    private void drawInterpolationTreeMap(Graphics g, Rectangle parentR, TreeMap oldTm, TreeMap newTm, int depth) {
        double progressPercentage = (double) animationProgress / (double) animationSteps;

        Rectangle interpolated = getInterpolatedRectangle(oldTm, newTm, progressPercentage);
        //topleft corner
        double x = interpolated.getX();
        double y = interpolated.getY();
        if (!newTm.hasChildren()) {
            //if new has no children neither does old
            drawRectangle(g, interpolated, newTm.getColor());
            drawWeights(g, newTm.getTargetSize(), x, y);
        }

        int shrinkSize = getShrinkSize(depth);

        //get innerR
        Rectangle innerR = drawOutline(g, parentR, interpolated, depth, shrinkSize);

        for (TreeMap childNew : newTm.getChildren()) {
            //recurse in the same child
            TreeMap childOld = findTreeMapWithSameLabel(oldTm.getChildren(), childNew.getLabel());
            drawInterpolationTreeMap(g, innerR, childOld, childNew, depth + 1);
        }

        //draw the outline again to make sure it is drawn over it
        innerR = drawOutline(g, parentR, interpolated, depth, shrinkSize);

        if (!newTm.hasChildren() && labelFits(g, interpolated, newTm.getLabel())) {
            drawLabel(g, interpolated, newTm.getLabel());
        }

    }

    public TreeMap findTreeMapWithSameLabel(List<TreeMap> treeMaps, String label) {
        for (TreeMap tm : treeMaps) {
            if (tm.getLabel().equals(label)) {
                return tm;
            }
        }
        return null;
    }

    private void drawTreeMap(Graphics g, TreeMap tm, int depth) {

        if (!tm.hasChildren()) {
            drawRectangle(g, tm.getRectangle(), tm.getColor());
            drawWeights(g, tm.getTargetSize(), tm.getRectangle().getX(), tm.getRectangle().getY());
        }

        for (TreeMap child : tm.getChildren()) {
            drawTreeMap(g, child, depth + 1);
        }
    }

    private void drawOutlinesAndLabels(Graphics g, Rectangle parentR, TreeMap tm, int depth) {

        int shrinkSize = getShrinkSize(depth);

        Rectangle innerR = drawOutline(g, parentR, tm.getRectangle(), depth, shrinkSize);
        if (!tm.hasChildren()) {
            drawLabel(g, innerR, tm.getLabel());
        }

        for (TreeMap child : tm.getChildren()) {
            drawOutlinesAndLabels(g, innerR, child, depth + 1);
        }

    }

    private int getShrinkSize(int depth) {
        int shrinkSize = 0;
        if (depth == 0) {
            shrinkSize = 0;
        } else {
            shrinkSize = 1;
        }
        return shrinkSize;
    }

    private boolean labelFits(Graphics g, Rectangle r, String label) {
        Font f = g.getFont().deriveFont(0, 30);

        g.setFont(f);

        int width = g.getFontMetrics(f).stringWidth(label);
        int height = g.getFontMetrics(f).getHeight();
        //Font needs to fit in the rectangle
        //if the font is to small it becomes unreadable
        if (width >= r.getWidth() || height >= r.getHeight() || f.getSize() < 20) {
            return false;
        }
        return true;
    }

    public void drawLabel(Graphics g, Rectangle r, String label) {

        if (!drawLabelsEnabled) {
            return;
        }

        Font f = g.getFont().deriveFont(0, 25);
        g.setFont(f);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = g2.getFontMetrics(f).stringWidth(label);
        int height = g2.getFontMetrics(f).getHeight();

        if (!labelFits(g2, r, label)) {
            return;
        }
        int x = (int) ((r.getWidth() - width) / 2 + r.getX());
        int y = (int) ((r.getHeight() + height / 2) / 2 + r.getY());

//        int y = (int) (r.getY() + r.getHeight() / 2);
        //draw the outline
        g2.setColor(Color.DARK_GRAY);

//        g.drawString(label, x + 1, y);
//        g.drawString(label, x - 1, y);
//        g.drawString(label, x, y + 1);
//        g.drawString(label, x, y - 1);
////
//        g.drawString(label, x + 1, y + 1);
//        g.drawString(label, x - 1, y - 1);
//        g.drawString(label, x - 1, y + 1);
//        g.drawString(label, x + 1, y - 1);
        //draw the contentsd
        g2.setFont(f);
        g2.setColor(Color.BLACK);
        g2.drawString(label, x, y);
    }

    public Font scaleFont(String text, Rectangle r, Graphics g) {
        float fontSizeWidth = 20.0f;
        float fontSizeHeight = 20.0f;
        Font font = g.getFont().deriveFont(fontSizeWidth);
        int width = g.getFontMetrics(font).stringWidth(text);
        fontSizeWidth = (float) ((r.getWidth() / width) * fontSizeWidth);

        font = g.getFont().deriveFont(fontSizeHeight);
        int height = g.getFontMetrics(font).getHeight();
        fontSizeHeight = (float) ((r.getHeight() / height) * fontSizeHeight);

        return g.getFont().deriveFont(Math.min(Math.min(fontSizeWidth, fontSizeHeight) - 4, 20));
    }

    private void drawRectangle(Graphics g, Rectangle r, Color color) {

        int x = (int) Math.round(r.getX());
        int y = (int) Math.round(r.getY());
        int x2 = (int) Math.round(r.getX2());
        int y2 = (int) Math.round(r.getY2());
        Graphics2D g2 = (Graphics2D) g;

        //draw the rectangle with a gradient
        Color color1 = color;
        Color color2 = new Color(Math.max(0, color.getRed() - 40), Math.max(0, color.getGreen() - 40), Math.max(0, color.getBlue() - 40));

        GradientPaint rect = new GradientPaint(x, y, color1, x2, y2, color2, false);
        g2.setPaint(rect);
        Polygon p = new Polygon();
        p.addPoint(x, y);
        p.addPoint(x, y2);
        p.addPoint(x2, y2);
        p.addPoint(x2, y);

        g2.fillPolygon(p);
    }

    /**
     * Draws the outline of the rectangle. Moreover uses height differences to
     * illustrate the hierarchy. ParentR contains the inner surface area of the
     * parent
     *
     * @param g
     * @param parentR
     * @param r
     * @param depth
     * @param isLeaf
     */
    private Rectangle drawOutline(Graphics g, Rectangle parentR, Rectangle r, int depth, int shrinkSize) {

        //draw outer outline
        Rectangle outerOutline = r.clip(parentR);

        int x1 = (int) Math.round(outerOutline.getX());
        int y1 = (int) Math.round(outerOutline.getY());
        int x2 = (int) Math.round(outerOutline.getX2());
        int y2 = (int) Math.round(outerOutline.getY2());
        int w = x2 - x1;
        int h = y2 - y1;

        int innerX = x1 + shrinkSize;
        int innerY = y1 + shrinkSize;
        int innerW = w - shrinkSize * 2;
        int innerH = h - shrinkSize * 2;

        Rectangle innerRectangle = new Rectangle(innerX, innerY, innerW, innerH);
//        if (depth != 0) {
//            return innerRectangle;
//        }

        if (innerH > shrinkSize && innerW > shrinkSize) {
            //there is an interior

            //start drawing the outline
            Graphics2D g2 = (Graphics2D) g;

            Color color = Color.WHITE;

            g2.setColor(color);
            g2.fillRect(x1, y1, w, shrinkSize);
            g2.fillRect(x1, y1, shrinkSize, h);
            g2.fillRect(x2 - shrinkSize, y1, shrinkSize, h);
            g2.fillRect(x1, y2 - shrinkSize, w, shrinkSize);
//            g2.drawRect(Math.round((int) parentR.getX()), (int) parentR.getY(), (int) parentR.getWidth(), (int) parentR.getHeight());
        }
        return innerRectangle;
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }


    public void setDrawWeight(boolean drawWeightsEnabled) {
        this.drawWeightsEnabled = drawWeightsEnabled;
        System.out.println("drawWeightsEnabled = " + drawWeightsEnabled);
    }

    public void toIpe(String fileName) {
        if (newTreeMap == null) {
            return;
        }
        File outputFile = new File(fileName);
        FileWriter fw;
        try {
            fw = new FileWriter(outputFile, false);
            fw.write(IpeExporter.getPreamble());

            for (TreeMap tm : newTreeMap.getAllLeafs()) {
                fw.write(IpeExporter.getRectangle(tm.getRectangle(), tm.getLabel()));
                fw.flush();
            }
            fw.write(IpeExporter.endIpe());
            fw.flush();
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(StatisticalTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void toIpe() {
        toIpe("IpeExport.ipe");
    }

    public void drawWeights(Graphics g, double targetSize, double newX, double newY) {
        if (drawWeightsEnabled) {
            g.setColor(Color.red);
            g.drawString("" + Math.round(targetSize), (int) newX + 5, (int) newY + 25);
        }
    }

}
