package com.ufrgs.view;

import com.ufrgs.Main;
import com.ufrgs.model.Entity;
import com.ufrgs.model.Rectangle;
import com.ufrgs.technique.*;
import com.ufrgs.util.Display;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class Panel extends JPanel implements KeyListener, ActionListener {

    private final String outputDir;
    // Technique
    List<Entity> entityList;
    private Entity root;
    private Rectangle canvas;
    private int revision = 0;
    // Drawing
    private final JFrame frame;
    private double scale = 0;
    private final Font bigFont = new Font("Bitstream Vera Sans", Font.PLAIN, 22);
    private final Font mediumFont = new Font("Bitstream Vera Sans", Font.PLAIN, 18);
    private final Font smallFont = new Font("Bitstream Vera Sans", Font.PLAIN, 14);
    // Animation
    private double lastRevisionWeight = 0;
    private double progress = 0.0;
    private Timer timer;
    private int DELAY = 30;

    public Panel(Entity root, Rectangle canvas, JFrame frame, String outputDir) {

        this.outputDir = outputDir;
        this.root = root;
        this.canvas = canvas;
        this.frame = frame;
        this.setLayout(null);
        entityList = new ArrayList<>();

        flattenTree(root);
        for (Entity entity : entityList) {
            entity.setRectangle(null, 0);
        }
        root.setRectangle(canvas, 0);

        computeTreemap();
        setFrameTitle();

        timer = new Timer(DELAY, this);
        timer.start();
    }

    private void flattenTree(Entity entity) {

        entityList.add(entity);
        for (Entity child : entity.getChildren()) {
            if (child.isLeaf()) {
                entityList.add(child);
            } else {
                flattenTree(child);
            }
        }
    }

    private void computeTreemap() {

        switch (Main.technique) {
            case nmac:
            case nmew:
                new Nmap(root, canvas, revision);
                break;
            case sqr:
                new SquarifiedTreemap(root, canvas, revision);
                break;
            case otpbm:
            case otpbs:
                new OrderedTreemap(root, canvas, revision);
                break;
            case snd:
                new SliceAndDice(root, canvas, revision);
                break;
            case strip:
                new StripTreemap(root, canvas, revision);
                break;
            case spiral:
                new SpiralTreemap(root, canvas, revision);
                break;
        }
        // computeAspectRatioAverage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D graphics = (Graphics2D) g;

        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_ENABLE);
        graphics.setRenderingHints(rh);

        double maxWeight = 0;
        for (int i = 0; i < root.getNumberOfRevisions(); ++i) {
            if (root.getWeight(i) > maxWeight) {
                maxWeight = root.getWeight(i);
            }
        }

        scale = (progress * (root.getWeight(revision)) + (1 - progress) * lastRevisionWeight) / maxWeight;
        // graphics.scale(scale, scale);
        // PUT THIS LINE BACK TO MAKE IT SCALE

        // Draw leafs
        for (Entity entity : entityList) {
            if (entity.getWeight(revision) > 0 && entity.isLeaf()) {
                entity.draw(graphics, progress);
            }
        }

        // Draw intersections
//        for (int i = 0; i < entityList.size(); ++i) {
//            Entity entityA = entityList.get(i);
//            for (int j = i; j < entityList.size(); ++j) {
//                Entity entityB = entityList.get(j);
//                if (entityA != entityB &&
//                        entityA.getWeight(revision) > 0 && entityA.isLeaf() &&
//                        entityB.getWeight(revision) > 0 && entityB.isLeaf() &&
//                        entityA.getRectangle(progress).intersects(entityB.getRectangle(progress))) {
//
//                    entityA.drawIntersection(graphics, entityB, progress);
//                }
//            }
//        }

        // Draw borders
        for (Entity entity : entityList) {
            if (entity.getWeight(revision) > 0 && entity.isLeaf()) {
                entity.drawBorder(graphics, progress);
            }
        }

        for (Entity entity : entityList) {
            if (entity.getWeight(revision) > 0 && !entity.isLeaf()) {
                entity.drawBorder(graphics, progress);
            }
        }

        // Set font size
        if (scale > 0.8) {
            setFont(smallFont);
        } else if (scale > .5) {
            setFont(mediumFont);
        } else {
            setFont(bigFont);
        }

        // Draw labels
//        Entity.charWidth = graphics.getFontMetrics().stringWidth("X");
//        graphics.setPaint(Color.BLACK);
//        for (Entity entity : entityList) {
//            if (entity.getWeight(revision) > 0 && entity.isLeaf()) {
//                entity.drawLabel(graphics, progress);
//            }
//        }

        // Improves graphics on Linux
        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

        if (keyEvent.getKeyCode() == KeyEvent.VK_X) {
            if (revision < root.getNumberOfRevisions() - 1) {
                lastRevisionWeight = root.getWeight(revision);
                revision++;
                progress = 0.0;
                computeTreemap();
                setFrameTitle();
            } else {
                printCsv();
            }
        }
    }



    private void printCsv() {

        System.out.printf("\n\n");
        for (int i = 0; i < root.getNumberOfRevisions(); ++i) {
            double sum = 0;
            for (Entity entity : entityList) {
                // sum += entity.distanceList.get(i);
            }
            System.out.printf("%.8f\n", sum);
        }
    }


    private void computeAspectRatioAverage() {

        double ratioSum = 0;
        int nEntities = 0;
        for (Entity entity : entityList) {
            if (entity.getWeight(revision) > 0) {
                ratioSum += entity.getAspectRatio();
                nEntities++;
            }
        }
        //System.out.printf("%d,%f,%d\n", revision, ratioSum / nEntities, nEntities);
    }

    private void writeReport() {

        new File(outputDir).mkdirs(); // In case path doesn't exist
        for (int i = 0; i < root.getNumberOfRevisions(); ++i) {
            Path file = Paths.get(String.format("%s/t%d.rect", Main.outputDir, i));
            List<String> lines = new ArrayList<>();
            for (Entity entity : entityList) {
                if (entity.getWeight(i) > 0 && entity.isLeaf()) {
                    Rectangle rectangle = entity.rectangleList.get(i);
                    lines.add(String.format(Locale.ROOT, "%s,%.10f,%.10f,%.10f,%.10f", entity.getId(), rectangle.x, rectangle.y, rectangle.width, rectangle.height));
                }
            }
            lines.sort(String.CASE_INSENSITIVE_ORDER);
            try {
                Files.write(file, lines, Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        System.out.print(outputDir + " done.\n");
    }


    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        if (Main.DISPLAY == Display.ANALISYS) {
            if (revision < root.getNumberOfRevisions() - 1) {
                lastRevisionWeight = root.getWeight(revision);
                revision++;
                progress = 0.0;
                computeTreemap();
                setFrameTitle();
            } else {

                writeReport();
                frame.dispose();
                timer.stop();
            }
        } else {
            if (progress < 1) {
                progress += 0.02;
                repaint();
            } else {
                if (Main.DISPLAY == Display.ANIMATION) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    lastRevisionWeight = root.getWeight(revision);
                    revision++;
                    progress = 0.0;
                    computeTreemap();
                    setFrameTitle();
                } else if (Main.DISPLAY == Display.STEP) {
                    progress = 1;
                }
            }
        }
    }

    private void setFrameTitle() {

        switch (Main.technique) {
            case nmac:
                frame.setTitle("Nmap - Alternate Cut - Revision " + revision);
                break;
            case nmew:
                frame.setTitle("Nmap - Equal Weight - Revision " + revision);
                break;
            case sqr:
                frame.setTitle("Squarified - Revision " + revision);
                break;
            case otpbm:
                frame.setTitle("Ordered - Pivot-by-Middle - Revision " + revision);
                break;
            case otpbs:
                frame.setTitle("Ordered - Pivot-by-Size - Revision " + revision);
                break;
            case snd:
                frame.setTitle("Slice and Dice - Revision " + revision);
                break;
        }

    }
}
