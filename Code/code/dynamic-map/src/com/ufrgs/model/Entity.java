package com.ufrgs.model;

import com.ufrgs.view.Colormap;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Entity {

    private String id;
    private String shortId;
    private String printId = "";
    private List<Double> weightList;
    public List<Rectangle> rectangleList;
    private Point movingPoint, anchorPoint;
    private Rectangle rectangle, pastRectangle;
    private List<Entity> children;
    public static int charWidth = 0;

    public Entity(String id, int numberOfRevisions) {

        this.id = id;
        String split[] = getId().split("/");
        this.shortId = split[split.length - 1];

        // Initialize lists
        children = new ArrayList<>();
        weightList = new ArrayList<>(numberOfRevisions);
        rectangleList = new ArrayList<>(numberOfRevisions);
        for (int i = 0; i < numberOfRevisions; ++i) {
            weightList.add(0.0);
            rectangleList.add(new Rectangle(0,0,0,0));
        }
    }

    public String getId() {
        return id;
    }

    public int getNumberOfRevisions() {
        return weightList.size();
    }

    public double getWeight(int revision) {
        return weightList.get(revision);
    }

    public void setWeight(double weight, int revision) {
        weightList.set(revision, weight);
    }

    public double getMaximumWeight() {
        return weightList.stream().max(Double::compare).get();
    }

    public Point getAnchorPoint() {
        return anchorPoint;
    }

    public void setAnchorPoint(double x, double y) {
        anchorPoint.setValues(x, y);
    }

    public Point getMovingPoint() {
        return movingPoint;
    }

    public void initPoint(Point point) {
        anchorPoint = new Point(point.x, point.y);
        movingPoint = new Point(point.x, point.y);
    }

    public void setMovingPoint(double x, double y) {
        //pastPoint.setValues(point.x, point.y);
        movingPoint.setValues(x, y);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public Rectangle getRectangle(double progress) {

        double x = rectangle.x * progress + pastRectangle.x * (1 - progress);
        double y = rectangle.y * progress + pastRectangle.y * (1 - progress);
        double width = rectangle.width * progress + pastRectangle.width * (1 - progress);
        double height = rectangle.height * progress + pastRectangle.height * (1 - progress);

        return new Rectangle(x, y, width, height);
    }

    public Rectangle getPastRectangle() {
        return pastRectangle;
    }

    public double getAspectRatio() {

        return Double.min(rectangle.width / rectangle.height,
                rectangle.height / rectangle.width);
    }

    public void setRectangle(Rectangle newRectangle, int revision) {

        if (this.rectangle == null) {
            pastRectangle = newRectangle;
        } else {
            pastRectangle = this.rectangle;
        }
        this.rectangle = newRectangle;
        // Compute metric
        if (newRectangle != null) {
            this.rectangleList.set(revision, new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height));
        }
    }

    public void addChild(Entity entity) {
        children.add(entity);
    }

    public List<Double> getWeightList() {
        return weightList;
    }

    public List<Entity> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public void draw(Graphics2D graphics, double progress) {

        double x = rectangle.x * progress + pastRectangle.x * (1 - progress);
        double y = rectangle.y * progress + pastRectangle.y * (1 - progress);
        double width = rectangle.width * progress + pastRectangle.width * (1 - progress);
        double height = rectangle.height * progress + pastRectangle.height * (1 - progress);

        graphics.setColor(Colormap.sequentialColormap((float) (1 - getAspectRatio())));
        graphics.fill(new Rectangle2D.Double(x, y, width, height));
    }

    public void drawIntersection(Graphics2D graphics, Entity entityB, double progress) {

        Rectangle rectangle = this.getRectangle(progress).intersection(entityB.getRectangle(progress));

        graphics.setColor(Colormap.sequentialColormap((float) (((1 - getAspectRatio()) + (1 - entityB.getAspectRatio()))/2)));
        graphics.fill(new Rectangle2D.Double(rectangle.x, rectangle.y, rectangle.width, rectangle.height));
    }

    public void drawBorder(Graphics2D graphics, double progress) {

        double x = rectangle.x * progress + pastRectangle.x * (1 - progress);
        double y = rectangle.y * progress + pastRectangle.y * (1 - progress);
        double width = rectangle.width * progress + pastRectangle.width * (1 - progress);
        double height = rectangle.height * progress + pastRectangle.height * (1 - progress);

        if (isLeaf()) {
            graphics.setColor(new Color(0,0,0,30));
            graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            graphics.draw(new Rectangle2D.Double(x, y, width, height));
        } else {
            graphics.setColor(Color.black);
            graphics.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            graphics.draw(new Rectangle2D.Double(x, y, width, height));
        }
    }

    private void updatePrintId(double progress) {

        int width = (int) (rectangle.width * progress + pastRectangle.width * (1 - progress)) - charWidth/2;
        if (charWidth > 0) {
            if (width > charWidth * shortId.length()) {
                printId = shortId;
            } else {
                printId = shortId.substring(0, width / charWidth);
            }
        }
    }

    public void drawLabel(Graphics2D graphics, double progress) {

        if (rectangle.height > 20 && rectangle.width > 20) {

            if (progress % 0.2 < 0.01) {
                updatePrintId(progress);
            }

            int x = (int) (rectangle.x * progress + pastRectangle.x * (1 - progress)) + 4;
            int y = (int) (rectangle.y * progress + pastRectangle.y * (1 - progress)) + 20;

            graphics.drawString(printId, x, y);
        }
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id='" + id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id != null ? id.equals(entity.id) : entity.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
