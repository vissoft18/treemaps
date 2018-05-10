package com.ufrgs.technique;

import com.ufrgs.Main;
import com.ufrgs.model.Entity;
import com.ufrgs.model.Rectangle;
import com.ufrgs.util.Technique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;

public class OrderedTreemap {

    private int revision;
    private double normalizer;

    public OrderedTreemap(Entity root, Rectangle rectangle, int revision) {

        this.revision = revision;

        root.setRectangle(rectangle, revision);

        treemapMultidimensional(root.getChildren(), rectangle);
    }

    private void treemapMultidimensional(List<Entity> entityList, Rectangle rectangle) {

        List<Entity> entityCopy = new ArrayList<>();
        entityCopy.addAll(entityList);

        treemapSingledimensional(entityCopy, rectangle);

        // Recursive calls for the children
        for (Entity entity : entityCopy) {
            if (!entity.isLeaf() && entity.getWeight(revision) > 0) {
                List<Entity> newEntityList = new ArrayList<>();
                newEntityList.addAll(entity.getChildren());
                treemapMultidimensional(newEntityList, entity.getRectangle());
            }
        }
    }

    private void treemapSingledimensional(List<Entity> entityList, Rectangle rectangle) {

        entityList.removeIf(entity -> entity.getWeight(revision) == 0.0);
        normalize(entityList, rectangle.width * rectangle.height);

        // Pivot-by-middle
        if (entityList.size() == 0) {
            return;
        } else if (entityList.size() == 1) {
            entityList.get(0).setRectangle(rectangle, revision);
        } else if (entityList.size() == 2) {

            Entity A = entityList.get(0), B = entityList.get(1);
            if (rectangle.width > rectangle.height) {
                double aWidth = (getNormalizedWeight(A) / (getNormalizedWeight(A) + getNormalizedWeight(B))) * rectangle.width;
                A.setRectangle(new Rectangle(rectangle.x, rectangle.y, aWidth, rectangle.height), revision);
                B.setRectangle(new Rectangle(rectangle.x + aWidth, rectangle.y, rectangle.width - aWidth, rectangle.height), revision);
            } else {
                double aHeight = (getNormalizedWeight(A) / (getNormalizedWeight(A) + getNormalizedWeight(B))) * rectangle.height;
                A.setRectangle(new Rectangle(rectangle.x, rectangle.y, rectangle.width, aHeight), revision);
                B.setRectangle(new Rectangle(rectangle.x, rectangle.y + aHeight, rectangle.width, rectangle.height - aHeight), revision);
            }
        } else if (entityList.size() == 3) {

            Entity A = entityList.get(0), B = entityList.get(1), C = entityList.get(2);
            if (rectangle.width > rectangle.height) {
                double aWidth = (getNormalizedWeight(A) / (getNormalizedWeight(A) + getNormalizedWeight(B) + +getNormalizedWeight(C))) * rectangle.width;
                A.setRectangle(new Rectangle(rectangle.x, rectangle.y, aWidth, rectangle.height), revision);
                treemapSingledimensional(new ArrayList<>(Arrays.asList(B, C)), new Rectangle(rectangle.x + aWidth, rectangle.y, rectangle.width - aWidth, rectangle.height));
            } else {
                double aHeight = (getNormalizedWeight(A) / (getNormalizedWeight(A) + getNormalizedWeight(B) + getNormalizedWeight(C))) * rectangle.height;
                A.setRectangle(new Rectangle(rectangle.x, rectangle.y, rectangle.width, aHeight), revision);
                treemapSingledimensional(new ArrayList<>(Arrays.asList(B, C)), new Rectangle(rectangle.x, rectangle.y + aHeight, rectangle.width, rectangle.height - aHeight));
            }
        } else {

            int pivotIndex = 0;
            if (Main.technique == Technique.otpbm) {
                pivotIndex = entityList.size() / 2;
            } else if (Main.technique == Technique.otpbs){
                int biggestValueIndex = 0;
                double biggestValue = 0.0;
                for (int i = 0; i < entityList.size(); ++i) {
                    if (entityList.get(i).getWeight(revision) > biggestValue) {
                        biggestValue = entityList.get(i).getWeight(revision);
                        biggestValueIndex = i;
                    }
                }
                pivotIndex = biggestValueIndex;
            }

            Entity pivot = entityList.get(pivotIndex);
            List<Entity> l1 = new ArrayList<>(entityList.subList(0, pivotIndex));
            List<Entity> l2 = new ArrayList<>();
            List<Entity> l3 = new ArrayList<>(entityList.subList(pivotIndex + 1, entityList.size()));
            Rectangle r = null, r1 = null, rMinusR1 = null;

            double totalWeight = 0;
            for (Entity entity : entityList) {
                totalWeight += entity.getWeight(revision);
            }

            double l1Weight = 0;
            for (Entity entity : l1) {
                l1Weight += entity.getWeight(revision);
            }

            if (rectangle.width > rectangle.height) {
                double r1Width = (l1Weight / totalWeight) * rectangle.width;
                r1 = new Rectangle(rectangle.x, rectangle.y, r1Width, rectangle.height);
                rMinusR1 = new Rectangle(rectangle.x + r1Width, rectangle.y, rectangle.width - r1Width, rectangle.height);
                r = new Rectangle(rectangle.x + r1Width, rectangle.y, getNormalizedWeight(pivot) / rMinusR1.height, rectangle.height);
            } else {
                double r1Height = (l1Weight / totalWeight) * rectangle.height;
                r1 = new Rectangle(rectangle.x, rectangle.y, rectangle.width, r1Height);
                rMinusR1 = new Rectangle(rectangle.x, rectangle.y + r1Height, rectangle.width, rectangle.height - r1Height);
                r = new Rectangle(rectangle.x, rectangle.y + r1Height, rectangle.width, getNormalizedWeight(pivot) / rMinusR1.width);
            }

            double pPreviousAspectRatio = min(r.width / r.height, r.height / r.width);

            double pNewWidth = 0, pNewHeight = 0, pNewAspectRatio;
            while (l3.size() > 2) {

                l2.add(0, l3.get(0));
                l3.remove(0);

                double l2Weight = 0;
                for (Entity entity : l2) {
                    l2Weight += getNormalizedWeight(entity);
                }

                if (rectangle.width > rectangle.height) {

                    pNewWidth = (getNormalizedWeight(pivot) + l2Weight) / rMinusR1.height;
                    pNewHeight = (getNormalizedWeight(pivot) / (getNormalizedWeight(pivot) + l2Weight)) * rMinusR1.height;
                    pNewAspectRatio = min(pNewWidth / pNewHeight, pNewHeight / pNewWidth);

                    if (pNewAspectRatio < pPreviousAspectRatio) {
                        l3.add(0, l2.get(l2.size() - 1));
                        l2.remove(l2.size() - 1);
                        break;
                    } else {
                        r = new Rectangle(rMinusR1.x, rMinusR1.y, pNewWidth, pNewHeight);
                        pPreviousAspectRatio = pNewAspectRatio;
                    }
                } else {

                    pNewHeight = (getNormalizedWeight(pivot) + l2Weight) / rMinusR1.width;
                    pNewWidth = (getNormalizedWeight(pivot) / (getNormalizedWeight(pivot) + l2Weight)) * rMinusR1.width;
                    pNewAspectRatio = min(pNewWidth / pNewHeight, pNewHeight / pNewWidth);

                    if (pNewAspectRatio < pPreviousAspectRatio) {
                        l3.add(0, l2.get(l2.size() - 1));
                        l2.remove(l2.size() - 1);
                        break;
                    } else {
                        r = new Rectangle(rMinusR1.x, rMinusR1.y, pNewWidth, pNewHeight);
                        pPreviousAspectRatio = pNewAspectRatio;
                    }
                }
            }

            pivot.setRectangle(r, revision);
            if (rectangle.width > rectangle.height) {
                treemapSingledimensional(l1, r1);
                if (l2.size() > 0) {
                    Rectangle r2 = new Rectangle(rMinusR1.x, rMinusR1.y + r.height, r.width, rMinusR1.height - r.height);
                    treemapSingledimensional(l2, r2);
                }
                Rectangle r3 = new Rectangle(rMinusR1.x + r.width, rMinusR1.y, rMinusR1.width - r.width, rMinusR1.height);
                treemapSingledimensional(l3, r3);
            } else {
                treemapSingledimensional(l1, r1);
                if (l2.size() > 0) {
                    Rectangle r2 = new Rectangle(rMinusR1.x + r.width, rMinusR1.y, rMinusR1.width - r.width, r.height);
                    treemapSingledimensional(l2, r2);
                }
                Rectangle r3 = new Rectangle(rMinusR1.x, rMinusR1.y + r.height, rMinusR1.width, rMinusR1.height - r.height);
                treemapSingledimensional(l3, r3);
            }
        }
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