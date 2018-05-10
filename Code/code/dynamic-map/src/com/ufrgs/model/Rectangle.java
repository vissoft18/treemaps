package com.ufrgs.model;

public class Rectangle {

    public double x, y, width, height;

    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle(double width, double height) {
        this.x = 0;
        this.y = 0;
        this.width = width;
        this.height = height;
    }

    public double getShortEdge() {
        return (width < height) ? width : height;
    }

    // For the strip algrithm
    public Rectangle subtractAreaFromTop(double area) {
        double areaHeight = area / this.width;
        double newheight = this.height - areaHeight;
        return new Rectangle(x, y + areaHeight, width, newheight);
    }

    public Rectangle cutArea(double area) {

        if (width > height) {
            double areaWidth = area / height;
            double newWidth = width - areaWidth;
            return new Rectangle(x + areaWidth, y, newWidth, height);
        } else {
            double areaHeight = area / this.width;
            double newheight = this.height - areaHeight;
            return new Rectangle(x, y + areaHeight, width, newheight);
        }
    }

    // Used only on Spiral
    public Rectangle subtractAreaFrom(int cutDirection, double area) {

        double areaHeight, newheight;
        double areaWidth, newWidth;
        switch (cutDirection) {
            case 0:
                areaHeight = area / this.width;
                newheight = this.height - areaHeight;
                return new Rectangle(x, y + areaHeight, width, newheight);
            case 1:
                areaWidth = area / height;
                newWidth = width - areaWidth;
                return new Rectangle(x, y, newWidth, height);
            case 2:
                areaHeight = area / this.width;
                newheight = this.height - areaHeight;
                return new Rectangle(x, y, width, newheight);
            case 3:
                areaWidth = area / height;
                newWidth = width - areaWidth;
                return new Rectangle(x + areaWidth, y, newWidth, height);
        }
        return null;
    }

    public void setValues(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean intersects(Rectangle b) {
        return (((this.x + this.width > b.x) || (b.x + b.width > this.x)) &&
                ((this.y + this.height > b.y) || (b.y + b.height > this.y)));
    }

    public Rectangle intersection(Rectangle b) {

        double x0 = Math.max(this.x, b.x);
        double y0 = Math.max(this.y, b.y);
        double x1 = Math.min(this.x + this.width, b.x + b.width);
        double y1 = Math.min(this.y + this.height, b.y + b.height);
        return new Rectangle(x0, y0, x1 - x0, y1 - y0);
    }


    @Override
    public String toString() {
        return "Rectangle{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
