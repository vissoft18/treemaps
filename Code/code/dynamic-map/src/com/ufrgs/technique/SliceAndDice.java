package com.ufrgs.technique;

import com.ufrgs.model.Entity;
import com.ufrgs.model.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class SliceAndDice {

    private final int revision;
    private double normalizer = 0;

    public SliceAndDice(Entity root, Rectangle rectangle, int revision) {

        this.revision = revision;
        this.normalizer = (rectangle.width * rectangle.height) / root.getWeight(revision);
        root.setRectangle(rectangle, revision);

        treemapMultidimensional(root.getChildren(), rectangle, rectangle.width > rectangle.height);
    }

    // Use recursion to compute single dimensional treemaps from a hierarchical dataset
    private void treemapMultidimensional(List<Entity> entityList, Rectangle rectangle, boolean verticalCut) {

        // Make a copy of the data
        List<Entity> entityCopy = new ArrayList<>();
        entityCopy.addAll(entityList);

        treemapSingledimensional(entityCopy, rectangle, verticalCut);

        // Recursive calls for the children
        for (Entity entity : entityCopy) {
            if (!entity.isLeaf() && getNormalizedWeight(entity) > 0) {
                List<Entity> newEntityList = new ArrayList<>();
                newEntityList.addAll(entity.getChildren());
                treemapMultidimensional(newEntityList, entity.getRectangle(), !verticalCut);
            }
        }
    }

    private void treemapSingledimensional(List<Entity> entityList, Rectangle rectangle, boolean verticalCut) {

        entityList.removeIf(entity -> entity.getWeight(revision) == 0.0);

        if (verticalCut) {
            double xOffset = rectangle.x;
            for (Entity entity : entityList) {
                double width = getNormalizedWeight(entity) / rectangle.height;
                entity.setRectangle(new Rectangle(xOffset, rectangle.y, width, rectangle.height), revision);
                xOffset += width;
            }
        } else {
            double yOffset = rectangle.y;
            for (Entity entity : entityList) {
                double height = getNormalizedWeight(entity) / rectangle.width;
                entity.setRectangle(new Rectangle(rectangle.x, yOffset, rectangle.width, height), revision);
                yOffset += height;
            }
        }
    }

    private double getNormalizedWeight(Entity entity) {
        return entity.getWeight(revision) * normalizer;
    }
}