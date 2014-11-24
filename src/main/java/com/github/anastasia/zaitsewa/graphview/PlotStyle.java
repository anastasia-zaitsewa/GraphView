package com.github.anastasia.zaitsewa.graphview;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * Contains colors for lines, fill area; gives you ability to set or reset filling for plot
 * Default style: without points; with fill (transparent black) below plot; black plot;
 */
public class PlotStyle {
    private static final int DEFAULT_LINE_COLOR = Color.BLACK;
    private static final int DEFAULT_FILL_COLOR = 0x44000000;
    private Paint linePaint;
    private boolean enableFill;
    private Paint fillPaint;
    private Drawable pointDrawable;

    public PlotStyle() {
        pointDrawable = null;
        enableFill = true;

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(DEFAULT_LINE_COLOR);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(DEFAULT_FILL_COLOR);
    }

    /**
     * @param lineColor - the color for lines, that connects points of plot
     */
    public void setLineColor(int lineColor) {
        linePaint.setColor(lineColor);
    }

    /**
     * @param fillColor - the color of area below the plot
     */
    public void setFillColor(int fillColor) {
        fillPaint.setColor(fillColor);
    }

    /**
     * @return a Paint to draw plot's lines
     */
    public Paint getLinePaint() {
        return linePaint;
    }

    /**
     * @return a Paint to draw an area below the plot
     */
    public Paint getFillPaint() {
        return fillPaint;
    }

    /**
     * @return true if area below the plot should be filled and false otherwise
     */
    public boolean isFillEnabled() {
        return enableFill;
    }

    /**
     * @param enableFill set it to true if area below the plot supposed to be filled;
     *                   and - to false otherwise
     */
    public void fillEnable(boolean enableFill) {
        this.enableFill = enableFill;
    }

    /**
     * @return Drawable for points; can return {@value null} if points should not be presented
     */
    public Drawable getPointDrawable() {
        return pointDrawable;
    }

    /**
     * @param pointDrawable - {@link android.graphics.drawable.Drawable} for drawing points
     */
    public void setPointDrawable(Drawable pointDrawable) {
        this.pointDrawable = pointDrawable;
    }
}

