package com.github.anastasia.zaitsewa.graphview;


import java.util.List;
import java.util.Observer;

/**
 * Functions to implement, to provide a points for Graph
 */
public interface PointsProvider<T extends Point> {

    /**
     * @return List of {@link com.github.anastasia.zaitsewa.graphview.Point}
     */
    List<T> getPoints();

    /**
     * Reflect the value representation of coordinate to readable state
     * @param x value representation (example: date in milliseconds)
     * @return representation for UI (example: DD/MM format)
     */
    String getLabelX(double x);

    /**
     * Reflect the value representation of coordinate to readable state
     * @param y value representation (example: 12.3426789)
     * @return representation for UI (example: 12.34)
     */
    String getLabelY(double y);

    /**
     * @return value-step with which a Labels on axis should be drawn (example:
     * if you want labels to be shown every 10 units then you must return 10.0)
     */
    double getScaleStepX();

    /**
     * @return value-step with which a Labels on axis should be drawn (example:
     * if you want labels to be shown every 10 units then you must return 10.0)
     */
    double getScaleStepY();

    /**
     * The View uses an Observer to update its state when original data are changed
     * @param observer
     */
    void addObserver(Observer observer);
}
