package com.github.anastasia.zaitsewa.graphview;

import java.util.Comparator;

/**
 * Spending as Point for GraphView
 */
public class Point {
    private double x;
    private double y;

    public Point() {
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /**
     * To compare Points using X
     */
    public static class ComparatorX implements Comparator<Point> {

        @Override
        public int compare(Point lhs, Point rhs) {
            return Double.compare(lhs.getX(), rhs.getX());
        }
    }

    /**
     * To compare Points using Y
     */
    public static class ComparatorY implements Comparator<Point> {

        @Override
        public int compare(Point lhs, Point rhs) {
            return Double.compare(lhs.getY(), rhs.getY());
        }
    }


}
