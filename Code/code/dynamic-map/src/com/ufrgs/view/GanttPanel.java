package com.ufrgs.view;

import com.ufrgs.model.Entity;
import com.ufrgs.model.Rectangle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GanttPanel extends JPanel {

    private final Rectangle canvas;
    private List<Entity> entityList;
    private final int OFFSET = 250;
    private double maxWeight = 0;

    public GanttPanel(Entity root, Rectangle canvas) {

        this.canvas = canvas;
        entityList = new ArrayList<>();
        flattenTree(root);

        entityList = entityList.stream().filter(Entity::isLeaf).collect(Collectors.toList());

        // Get max value for normalization
        for (int i = 0; i < entityList.size(); ++i) {
            Entity entity = entityList.get(i);
            if (entity.isLeaf()) {
                for (int r = 0; r < entity.getNumberOfRevisions(); ++r) {
                    if (entity.getWeight(r) > maxWeight) {
                        maxWeight = entity.getWeight(r);
                    }
                }
            }
        }

        // Count "arrivals and departures"
        int arrivals = 0;
        int departures = 0;
        for (Entity entity : entityList) {
            for (int r = 1; r < entity.getNumberOfRevisions(); ++r) {
                if (entity.getWeight(r-1) == 0 && entity.getWeight(r) > 0) {
                    arrivals++;
                }

                if (entity.getWeight(r) == 0 && entity.getWeight(r-1) > 0) {
                    departures++;
                }
            }
        }

        System.out.println("Arrivals: " + arrivals + "  Departures: " + departures);
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);


        Graphics2D graphics = (Graphics2D) g;
        setBackground(Color.black);
        double width = (canvas.width - OFFSET) / entityList.get(0).getNumberOfRevisions();
        double height = canvas.height / entityList.size();

        for (int i = 0; i < entityList.size(); ++i) {
            for (int r = 0; r < entityList.get(0).getNumberOfRevisions(); ++r) {
                double x = OFFSET + r * width;
                double y = i * height;

                graphics.setColor(Colormap.sequentialColormap((float) (entityList.get(i).getWeight(r)/maxWeight)));
                graphics.fill(new Rectangle2D.Double(x, y, width, height));

                Font font;
                int fontSize = 18;
                do {
                    font = new Font("Bitstream Vera Sans", Font.PLAIN, fontSize);
                    graphics.setFont(font);
                    fontSize--;
                } while (height < graphics.getFont().createGlyphVector(graphics.getFontRenderContext(), "l").getPixelBounds(null, 0, 0).height); // Font height hehe


                String split[] = entityList.get(i).getId().split("/");
                String id = split[split.length - 1];

                graphics.drawString(id, OFFSET - graphics.getFontMetrics().stringWidth(id) - 10, (int) (y + height));
            }
        }
    }
}
