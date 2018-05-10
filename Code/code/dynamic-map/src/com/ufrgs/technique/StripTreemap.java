package com.ufrgs.technique;

import com.ufrgs.model.Entity;
import com.ufrgs.model.Point;
import com.ufrgs.model.Rectangle;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.max;
import static java.lang.Math.pow;


public class StripTreemap {

    private final int revision;
    private double normalizer = 0;

    public StripTreemap(Entity root, Rectangle rectangle, int revision) {
        this.revision = revision;

        root.setRectangle(rectangle, revision);
        root.initPoint(new Point(rectangle.x/2.0, rectangle.y/2.0));

        List<Entity> children = treemapMultidimensional(root.getChildren(), rectangle);
        for (Entity entity : children) {
            root.addChild(entity);
        }
    }

    // Use recursion to compute single dimensional treemaps from a hierarchical dataset
    private List<Entity> treemapMultidimensional(List<Entity> entityList, Rectangle rectangle) {

        // Sort using entities weight -- layout tends to turn out better
        // entityList.sort(Comparator.comparing(o -> ((Entity) o).getWeight(0)).reversed());
        // Make a copy of data, as the original is destroyed during treemapSingledimensional computation
        List<Entity> entityCopy = new ArrayList<>();
        entityCopy.addAll(entityList);

        treemapSingledimensional(entityList, rectangle);

        // Recursive calls for the children
        for (Entity entity : entityCopy) {
            if (!entity.isLeaf() && getNormalizedWeight(entity) > 0) {
                List<Entity> newEntityList = new ArrayList<>();
                newEntityList.addAll(entity.getChildren());
                treemapMultidimensional(newEntityList, entity.getRectangle());
            }
        }

        return entityCopy;
    }

    private void treemapSingledimensional(List<Entity> entityList, Rectangle rectangle) {

        // Bruls' algorithm assumes that the data is normalized
        normalize(entityList, rectangle.width * rectangle.height);
        entityList.removeIf(entity -> entity.getWeight(revision) == 0.0);

        List<Entity> currentRow = new ArrayList<Entity>();
        strip(entityList, currentRow, rectangle);
    }

    private void strip(List<Entity> entityList, List<Entity> currentRow, Rectangle rectangle) {

        // If all elements have been placed, save coordinates into objects
        if (entityList.size() == 0) {
            saveCoordinates(currentRow, rectangle);
            return;
        }

        // Test if new element should be included in current row
        if (improvesRatio(currentRow, getNormalizedWeight(entityList.get(0)), rectangle.width)) {
            currentRow.add(entityList.get(0));
            entityList.remove(0);
            strip(entityList, currentRow, rectangle);
        } else {
            // New row must be created, subtract area of previous row from container
            double sum = 0;
            for (Entity entity : currentRow) {
                sum += getNormalizedWeight(entity);
            }

            Rectangle newRectangle = rectangle.subtractAreaFromTop(sum);
            saveCoordinates(currentRow, rectangle);
            List<Entity> newCurrentRow = new ArrayList<>();
            strip(entityList, newCurrentRow, newRectangle);
        }
    }

    private void saveCoordinates(List<Entity> currentRow, Rectangle rectangle) {

        double normalizedSum = 0;
        for (Entity entity : currentRow) {
            normalizedSum += getNormalizedWeight(entity);
        }

        double subxOffset = rectangle.x, subyOffset = rectangle.y; // Offset within the container
        double areaWidth = normalizedSum / rectangle.height;
        double areaHeight = normalizedSum / rectangle.width;

//        if (rectangle.width > rectangle.height) {
//            for (Entity entity : currentRow) {
//                double x = subxOffset;
//                double y = subyOffset;
//                double width = areaWidth;
//                double height = getNormalizedWeight(entity) / areaWidth;
//                entity.setRectangle(new Rectangle(x, y, width, height), revision);
//                subyOffset += getNormalizedWeight(entity) / areaWidth;
//                // Save center as we'll be using it as input to the nmap algorithm
//                entity.initPoint(new Point(x + width / 2, y + height / 2));
//            }
//        } else {
            for (Entity entity : currentRow) {
                double x = subxOffset;
                double y = subyOffset;
                double width = getNormalizedWeight(entity) / areaHeight;
                double height = areaHeight;
                entity.setRectangle(new Rectangle(x, y, width, height), revision);
                subxOffset += getNormalizedWeight(entity) / areaHeight;
                // Save center as we'll be using it as input to the nmap algorithm
                entity.initPoint(new Point(x + width / 2, y + height / 2));
            }
//        }
    }

    // Test if adding a new entity to row improves ratios (get closer to 1)
    boolean improvesRatio(List<Entity> currentRow, double nextEntity, double length) {

        if (currentRow.size() == 0) {
            return true;
        }

        double minCurrent = Double.MAX_VALUE, maxCurrent = Double.MIN_VALUE;
        for (Entity entity : currentRow) {
            if (getNormalizedWeight(entity) > maxCurrent) {
                maxCurrent = getNormalizedWeight(entity);
            }

            if (getNormalizedWeight(entity) < minCurrent) {
                minCurrent = getNormalizedWeight(entity);
            }
        }

        double minNew = (nextEntity < minCurrent) ? nextEntity : minCurrent;
        double maxNew = (nextEntity > maxCurrent) ? nextEntity : maxCurrent;

        double sumCurrent = 0;
        for (Entity entity : currentRow) {
            sumCurrent += getNormalizedWeight(entity);
        }
        double sumNew = sumCurrent + nextEntity;

        double currentRatio = max(pow(length, 2) * maxCurrent / pow(sumCurrent, 2),
                pow(sumCurrent, 2) / (pow(length, 2) * minCurrent));
        double newRatio = max(pow(length, 2) * maxNew / pow(sumNew, 2),
                pow(sumNew, 2) / (pow(length, 2) * minNew));

        return currentRatio >= newRatio;
    }

    private void normalize(List<Entity> entityList, double area) {

        double sum = 0;
        for (Entity entity : entityList) {
            sum += entity.getWeight(revision);
        }
        normalizer = area / sum;
    }

    private double getNormalizedWeight(Entity entity) {
            return entity.getWeight(revision) * normalizer;

    }
}