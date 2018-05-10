package com.ufrgs.technique;

import com.ufrgs.Main;
import com.ufrgs.model.Entity;
import com.ufrgs.model.Rectangle;
import com.ufrgs.util.Technique;

import java.util.ArrayList;
import java.util.List;

public class Nmap {

    private int revision;

    public Nmap(Entity root, Rectangle rectangle, int revision) {

        this.revision = revision;

        // Compute Squarified Treemap to set anchors
        if (revision == 0) {
            new SquarifiedTreemap(root, rectangle, revision);
        }

        nmap(root.getChildren(), rectangle);
    }

    private void nmap(List<Entity> entityList, Rectangle rectangle) {

        List<Entity> entityCopy = new ArrayList<>();
        entityCopy.addAll(entityList);

        for (Entity entity : entityCopy) {
            if (entity.getWeight(revision) > 0) {
                entity.setMovingPoint(entity.getAnchorPoint().x, entity.getAnchorPoint().y);
            }
        }

        if (Main.technique == Technique.nmac) {
            alternateCut(entityList, rectangle);
        } else if (Main.technique == Technique.nmew) {
            equalWeight(entityList, rectangle);
        }

        for (Entity entity : entityCopy) {
            if (!entity.isLeaf() && entity.getWeight(revision) > 0) {
                nmap(entity.getChildren(), entity.getRectangle());
            }
        }
    }

    private void alternateCut(List<Entity> entityList, Rectangle rectangle) {

        if (rectangle.width > rectangle.height) {
            alternateCut(entityList, rectangle, true);
        } else {
            alternateCut(entityList, rectangle, false);
        }
    }

    private void alternateCut(List<Entity> entityList, Rectangle rectangle, boolean verticalBissection) {

        List<Entity> entityCopy = new ArrayList<>();
        entityCopy.addAll(entityList);
        entityCopy.removeIf(entity -> entity.getWeight(revision) <= 0.0);

        if (entityCopy.size() == 0) {
            return;
        } else if (entityCopy.size() == 1) {
            // Done dividing
            entityCopy.get(0).setRectangle(rectangle, revision);
            entityCopy.get(0).setAnchorPoint(rectangle.x + rectangle.width/2, rectangle.y + rectangle.height/2);
            // System.out.println("ctx.rect(" + rectangle.x + ", " + rectangle.y + ", " + rectangle.width + ", " + rectangle.height + ");");
        } else {

            if (verticalBissection) {
                entityCopy.sort((a, b) -> ((Double) a.getAnchorPoint().x).compareTo(b.getAnchorPoint().x));
            } else {
                entityCopy.sort((a, b) -> ((Double) a.getAnchorPoint().y).compareTo(b.getAnchorPoint().y));
            }

            int cutIndex = entityCopy.size()/2;
            List<Entity> entityListA = entityCopy.subList(0, cutIndex);
            List<Entity> entityListB = entityCopy.subList(cutIndex, entityCopy.size());
            double sumA = entityListA.stream().mapToDouble(entity -> entity.getWeight(revision)).sum();
            double sumB = entityListB.stream().mapToDouble(entity -> entity.getWeight(revision)).sum();
            double sumTotal = sumA + sumB;
            Rectangle rectangleA, rectangleB;

            if (verticalBissection) {

                double rectangleWidthA = (sumA / sumTotal) * rectangle.width;
                double rectangleWidthB = (sumB / sumTotal) * rectangle.width;
                double boundary = (entityListA.get(entityListA.size() - 1).getAnchorPoint().x + entityListB.get(0).getAnchorPoint().x) / 2;

                rectangleA = new Rectangle(rectangle.x, rectangle.y,
                        boundary - rectangle.x, rectangle.height);

                rectangleB = new Rectangle(rectangle.x + rectangleA.width, rectangle.y,
                        rectangle.width - rectangleA.width, rectangle.height);

                double affineMatrixA[] = {rectangleWidthA / rectangleA.width, 0, 0, 1, rectangle.x * (1 - (rectangleWidthA / rectangleA.width)), 0};

                double affineMatrixB[] = {rectangleWidthB / rectangleB.width, 0, 0, 1, (rectangle.x + rectangle.width) * (1 - (rectangleWidthB / rectangleB.width)), 0};

                affineTransformation(entityListA, affineMatrixA);
                affineTransformation(rectangleA, affineMatrixA);
                affineTransformation(entityListB, affineMatrixB);
                affineTransformation(rectangleB, affineMatrixB);
            } else {

                double rectangleHeightA = (sumA / sumTotal) * rectangle.height;
                double rectangleHeightB = (sumB / sumTotal) * rectangle.height;
                double boundary = (entityListA.get(entityListA.size() - 1).getAnchorPoint().y + entityListB.get(0).getAnchorPoint().y) / 2;

                rectangleA = new Rectangle(rectangle.x, rectangle.y,
                        rectangle.width, boundary - rectangle.y);
                rectangleB = new Rectangle(rectangle.x, rectangle.y + rectangleA.height,
                        rectangle.width, rectangle.height - rectangleA.height);

                double affineMatrixA[] = {1, 0, 0, rectangleHeightA / rectangleA.height, 0, rectangle.y * (1 - (rectangleHeightA / rectangleA.height))};
                double affineMatrixB[] = {1, 0, 0, rectangleHeightB / rectangleB.height, 0, (rectangle.y + rectangle.height) * (1 - (rectangleHeightB / rectangleB.height))};

                affineTransformation(entityListA, affineMatrixA);
                affineTransformation(rectangleA, affineMatrixA);
                affineTransformation(entityListB, affineMatrixB);
                affineTransformation(rectangleB, affineMatrixB);
            }

            alternateCut(entityListA, rectangleA, !verticalBissection);
            alternateCut(entityListB, rectangleB, !verticalBissection);
        }
    }

    private void equalWeight(List<Entity> entityList, Rectangle rectangle) {

        List<Entity> entityCopy = new ArrayList<>();
        entityCopy.addAll(entityList);
        entityCopy.removeIf(entity -> entity.getWeight(revision) <= 0.0);

        if (entityCopy.size() == 0) {
            return;
        } else if (entityCopy.size() == 1) {
            // Done dividing
            entityCopy.get(0).setRectangle(rectangle, revision);
            //entityCopy.get(0).setPoint(rectangle.x + rectangle.width/2, rectangle.y + rectangle.height/2);
            // System.out.println("ctx.rect(" + rectangle.x + ", " + rectangle.y + ", " + rectangle.width + ", " + rectangle.height + ");");
        } else {
            // Define if we should bisect the data vertically or horizontally and sort the data accordingly
            if (rectangle.width > rectangle.height) {
                entityCopy.sort((a, b) -> ((Double) a.getAnchorPoint().x).compareTo(b.getAnchorPoint().x));
            } else {
                entityCopy.sort((a, b) -> ((Double) a.getAnchorPoint().y).compareTo(b.getAnchorPoint().y));
            }

            int cutIndex = findEWCutElement(entityList);
            List<Entity> entityListA = entityCopy.subList(0, cutIndex);
            List<Entity> entityListB = entityCopy.subList(cutIndex, entityCopy.size());

            double sumA = entityListA.stream().mapToDouble(entity -> entity.getWeight(revision)).sum();
            double sumB = entityListB.stream().mapToDouble(entity -> entity.getWeight(revision)).sum();
            double sumTotal = sumA + sumB;
            Rectangle rectangleA, rectangleB;

            if (entityListA.size() == 0) {
                equalWeight(entityListB, rectangle);
            } else if (entityListB.size() == 0) {
                equalWeight(entityListA, rectangle);
            } else {

                if (rectangle.width > rectangle.height) {

                    double rectangleWidthA = (sumA / sumTotal) * rectangle.width;
                    double rectangleWidthB = (sumB / sumTotal) * rectangle.width;
                    double boundary = (entityListA.get(entityListA.size() - 1).getAnchorPoint().x + entityListB.get(0).getAnchorPoint().x) / 2;

                    rectangleA = new Rectangle(rectangle.x, rectangle.y,
                            boundary - rectangle.x, rectangle.height);

                    rectangleB = new Rectangle(rectangle.x + rectangleA.width, rectangle.y,
                            rectangle.width - rectangleA.width, rectangle.height);

                    double affineMatrixA[] = {rectangleWidthA / rectangleA.width, 0, 0, 1, rectangle.x * (1 - (rectangleWidthA / rectangleA.width)), 0};

                    double affineMatrixB[] = {rectangleWidthB / rectangleB.width, 0, 0, 1, (rectangle.x + rectangle.width) * (1 - (rectangleWidthB / rectangleB.width)), 0};

                    affineTransformation(entityListA, affineMatrixA);
                    affineTransformation(rectangleA, affineMatrixA);
                    affineTransformation(entityListB, affineMatrixB);
                    affineTransformation(rectangleB, affineMatrixB);

                } else {

                    double rectangleHeightA = (sumA / sumTotal) * rectangle.height;
                    double rectangleHeightB = (sumB / sumTotal) * rectangle.height;
                    double boundary = (entityListA.get(entityListA.size() - 1).getAnchorPoint().y + entityListB.get(0).getAnchorPoint().y) / 2;

                    rectangleA = new Rectangle(rectangle.x, rectangle.y,
                            rectangle.width, boundary - rectangle.y);
                    rectangleB = new Rectangle(rectangle.x, rectangle.y + rectangleA.height,
                            rectangle.width, rectangle.height - rectangleA.height);

                    double affineMatrixA[] = {1, 0, 0, rectangleHeightA / rectangleA.height, 0, rectangle.y * (1 - (rectangleHeightA / rectangleA.height))};
                    double affineMatrixB[] = {1, 0, 0, rectangleHeightB / rectangleB.height, 0, (rectangle.y + rectangle.height) * (1 - (rectangleHeightB / rectangleB.height))};

                    affineTransformation(entityListA, affineMatrixA);
                    affineTransformation(rectangleA, affineMatrixA);
                    affineTransformation(entityListB, affineMatrixB);
                    affineTransformation(rectangleB, affineMatrixB);

                }

                equalWeight(entityListA, rectangleA);
                equalWeight(entityListB, rectangleB);
            }
        }
    }


    // Transform points
    private void affineTransformation(List<Entity> entityList, double m[]) {
        for (Entity entity : entityList) {
            double x = entity.getMovingPoint().x * m[0] + entity.getMovingPoint().y * m[2] + m[4];
            double y = entity.getMovingPoint().x * m[1] + entity.getMovingPoint().y * m[3] + m[5];
            entity.setMovingPoint(x, y);
        }
    }

    private void affineTransformation(Rectangle rectangle, double m[]) {
        // Transform rectangle
        double x0, y0, x1, y1;
        x0 = rectangle.x * m[0] + rectangle.y * m[2] + m[4];
        y0 = rectangle.x * m[1] + rectangle.y * m[3] + m[5];

        x1 = (rectangle.x + rectangle.width) * m[0] + (rectangle.y + rectangle.height) * m[2] + m[4];
        y1 = (rectangle.x + rectangle.width) * m[1] + (rectangle.y + rectangle.height) * m[3] + m[5];

        if (Double.isNaN(x0) || Double.isNaN(x1) || Double.isNaN(y0) || Double.isNaN(y1)) {
            int a = (int) rectangle.width;
        }

        rectangle.setValues(x0, y0, x1 - x0, y1 - y0);
    }

    private int findEWCutElement(List<Entity> entityList) {

        int cutElement = 1;
        double sumA = 0;
        double sumB = entityList.stream().mapToDouble(entity -> entity.getWeight(revision)).sum();

        double minDiff = Double.MAX_VALUE;
        for (int i = 0; i < entityList.size(); ++i) {
            sumA += entityList.get(i).getWeight(revision);
            sumB -= entityList.get(i).getWeight(revision);
            if (Math.abs(sumA - sumB) < minDiff) {
                minDiff = Math.abs(sumA - sumB);
                cutElement = i + 1;
            } else {
                break;
            }
        }
        return cutElement;
    }
}
