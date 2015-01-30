package com.github.anastasia.zaitsewa.graphview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;

/**
 * Class for drawing custom Graph
 * (contains axis, axis labels, non-interactive points(Drawables) and levels)
 */
public class GraphView extends View implements Observer {

    private static final String ZERO_LABEL = "0";
    private static final int MARGIN_DP = 5;
    private static final int DEFAULT_LABEL_PLACE_DP = 15;
    private static final int DEFAULT_TEXT_SIZE_SP = 8;
    private static final int DEFAULT_SPACING_BETWEEN_LABELS_DP = 20;
    private static final int DEFAULT_AXIS_LABEL_MARGIN_DP = 2;
    private static final int DEFAULT_TEXT_COLOR = Color.DKGRAY;
    private static final int DEFAULT_LEVEL_COLOR = 0x44888888;
    private static final int DEFAULT_AXIS_COLOR = Color.BLACK;
    private final List<Pair<Float, String>> labelsX = new ArrayList<Pair<Float, String>>();
    private final List<Pair<Float, String>> labelsY = new ArrayList<Pair<Float, String>>();
    private int textColor;
    private int levelColor;
    private int axisColor;
    private float textSize;
    private float spacingPXX;
    private float spacingPXY;
    private float labelPlacePX;
    private boolean enableXAxis;
    private boolean enableYAxis;
    private boolean enableLabels;
    private Paint textPaint;
    private Paint levelPaint;
    private Paint axisPaint;
    private int width;
    private int height;
    private List<Plot> plots = new ArrayList<Plot>();
    private float pxProY;
    private float pxProX;
    private double maxY;
    private double maxX;
    private double minX;
    private float textHeight = 0f;
    private float defaultAxisLabelMarginPX;
    private float marginPX;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.GraphView,
                0, 0);
        try {
            enableXAxis = a.getBoolean(R.styleable.GraphView_graphView_enableXAxis, true);
            enableYAxis = a.getBoolean(R.styleable.GraphView_graphView_enableYAxis, true);
            enableLabels = a.getBoolean(R.styleable.GraphView_graphView_enableLabels, true);
            textColor = a.getColor(R.styleable.GraphView_graphView_textColor, DEFAULT_TEXT_COLOR);
            levelColor = a.getColor(R.styleable.GraphView_graphView_levelColor, DEFAULT_LEVEL_COLOR);
            axisColor = a.getColor(R.styleable.GraphView_graphView_axisColor, DEFAULT_AXIS_COLOR);

            textSize = a.getDimension(R.styleable.GraphView_graphView_textSize,
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            DEFAULT_TEXT_SIZE_SP,
                            getResources().getDisplayMetrics()
                    )
            );
            spacingPXX = a.getDimensionPixelOffset(
                    R.styleable.GraphView_graphView_spacingX,
                    (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            DEFAULT_SPACING_BETWEEN_LABELS_DP,
                            getResources().getDisplayMetrics()
                    )
            );
            spacingPXY = a.getDimensionPixelOffset(
                    R.styleable.GraphView_graphView_spacingY,
                    (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            DEFAULT_SPACING_BETWEEN_LABELS_DP,
                            getResources().getDisplayMetrics()
                    )
            );
            labelPlacePX = a.getDimensionPixelOffset(
                    R.styleable.GraphView_graphView_labelPlace,
                    (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            DEFAULT_LABEL_PLACE_DP,
                            getResources().getDisplayMetrics()
                    )
            );
        } finally {
            a.recycle();
        }
        init();
    }

    public GraphView(Context context) {
        super(context);
        enableXAxis = true;
        enableYAxis = true;
        enableLabels = true;
        textColor = DEFAULT_TEXT_COLOR;
        levelColor = DEFAULT_LEVEL_COLOR;
        axisColor = DEFAULT_AXIS_COLOR;

        textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_TEXT_SIZE_SP,
                getResources().getDisplayMetrics()
        );
        spacingPXX = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_SPACING_BETWEEN_LABELS_DP,
                getResources().getDisplayMetrics()

        );
        spacingPXY = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_SPACING_BETWEEN_LABELS_DP,
                getResources().getDisplayMetrics()

        );
        labelPlacePX = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_LABEL_PLACE_DP,
                getResources().getDisplayMetrics()
        );
        init();
    }

    private void init() {
        levelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        levelPaint.setStyle(Paint.Style.STROKE);
        levelPaint.setColor(levelColor);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setColor(axisColor);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);

        defaultAxisLabelMarginPX = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_AXIS_LABEL_MARGIN_DP,
                getResources().getDisplayMetrics()
        );
        marginPX = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                MARGIN_DP,
                getResources().getDisplayMetrics()
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawXAxis(canvas);
        drawYAxisWithLevels(canvas);
        drawPlots(canvas);
    }

    private void drawPlots(Canvas canvas) {
        if (plots.isEmpty()) {
            return;
        }

        for (Plot plot : plots) {
            if (plot.style.isFillEnabled()) {
                canvas.drawPath(plot.fillPath, plot.style.getFillPaint());
            }

            canvas.drawPath(plot.path, plot.style.getLinePaint());

            Drawable pointDrawable = plot.style.getPointDrawable();
            if (pointDrawable != null) {
                for (Pair<Float, Float> point : plot.pointsPX) {
                    float x = point.first;
                    float y = point.second;

                    pointDrawable.setBounds(
                            (int) (x - pointDrawable.getIntrinsicWidth() / 2),
                            (int) (y - pointDrawable.getIntrinsicHeight() / 2),
                            (int) (x + pointDrawable.getIntrinsicWidth() / 2),
                            (int) (y + pointDrawable.getIntrinsicHeight() / 2)
                    );
                    pointDrawable.draw(canvas);
                }
            }
        }
    }

    private void drawYAxisWithLevels(Canvas canvas) {
        if (enableYAxis) {
            canvas.drawLine(
                    labelPlacePX, height - labelPlacePX - 1,
                    labelPlacePX, 0,
                    axisPaint
            );


            if (enableLabels) {
                textPaint.setTextAlign(Paint.Align.RIGHT);
                for (Pair<Float, String> labelY : labelsY) {
                    canvas.drawText(
                            labelY.second,
                            labelPlacePX - defaultAxisLabelMarginPX,
                            labelY.first,
                            textPaint
                    );
                    //Draw Levels
                    float levelY = labelY.first - textHeight / 2;
                    canvas.drawLine(
                            labelPlacePX,
                            levelY,
                            width - 1,
                            levelY,
                            levelPaint
                    );
                }
            }
        }
    }

    private void drawXAxis(Canvas canvas) {
        if (enableXAxis) {
            canvas.drawLine(
                    labelPlacePX, height - labelPlacePX - 1,
                    width - 1, height - labelPlacePX - 1,
                    axisPaint
            );

            if (enableLabels) {
                textPaint.setTextAlign(Paint.Align.CENTER);
                for (Pair<Float, String> labelX : labelsX) {
                    canvas.drawText(
                            labelX.second,
                            labelX.first,
                            height - labelPlacePX + textHeight + defaultAxisLabelMarginPX,
                            textPaint
                    );
                }
            }
        }
    }

    private void clearPlot(Plot plot) {
        plot.path.reset();
        plot.fillPath.reset();
        plot.pointsPX.clear();
    }

    private void changeGraph() {
        pxProY = (height - marginPX - labelPlacePX) / (float) maxY;
        pxProX = (width - labelPlacePX) / (float) (maxX - minX);

        for (Plot plot : plots) {
            clearPlot(plot);
            changePlot(plot);
        }

        changeLabels();
    }

    private void changePlot(Plot plot) {
        List<Point> points = plot.provider.getPoints();

        List<Pair<Float, Float>> pointsPX = new ArrayList<Pair<Float, Float>>();
        Path path = new Path();

        float x = (float) (labelPlacePX + pxProX * (points.get(0).getX() - minX));
        float y = (float) (height - labelPlacePX - pxProY * points.get(0).getY());
        path.moveTo(x, y);
        pointsPX.add(new Pair<Float, Float>(x, y));

        for (int i = 1; i < points.size(); i++) {
            x = (float) (labelPlacePX + pxProX * (points.get(i).getX() - minX));
            y = (float) (height - labelPlacePX - pxProY * points.get(i).getY());
            path.lineTo(x, y);
            pointsPX.add(new Pair<Float, Float>(x, y));
        }
        plot.path = path;
        plot.pointsPX = pointsPX;

        if (plot.style.isFillEnabled()) {
            Path fillPath = new Path(path);
            float y0 = height - labelPlacePX - 1;
            if (y != y0) {
                fillPath.lineTo(
                        pointsPX.get(pointsPX.size() - 1).first,
                        y0
                );
            }
            fillPath.lineTo(
                    pointsPX.get(0).first,
                    y0
            );
            fillPath.close();
            plot.fillPath = fillPath;
        }
    }

    private void changeLabels() {
        float lastLabelPX = labelPlacePX;
        Rect rect = new Rect();
        labelsX.clear();
        labelsY.clear();

        if (enableXAxis && enableLabels) {

            Plot leadPlotX = getPlotWithMaxScaleStepX();
            double maxScaleStepX = leadPlotX.provider.getScaleStepX();

            for (double x = minX; x <= maxX; x += maxScaleStepX) {
                String labelX = leadPlotX.provider.getLabelX(x);
                textPaint.getTextBounds(labelX, 0, labelX.length(), rect);
                float pxX = labelPlacePX + (float) (x - minX) * pxProX;
                float pxXFit = pxX - rect.width() / 2;
                if ((pxXFit - lastLabelPX >= spacingPXX) && (pxX <= width)) {
                    lastLabelPX = pxX;
                    labelsX.add(new Pair<>(pxX, labelX));
                }
            }
            textHeight = rect.height();
        }

        if (enableYAxis && enableLabels) {

            Plot leadPlotY = getPlotWithMaxScaleStepY();
            double maxScaleStepY = leadPlotY.provider.getScaleStepY();

            textPaint.getTextBounds(ZERO_LABEL, 0, ZERO_LABEL.length(), rect);
            labelsY.add(new Pair<>(
                    height - labelPlacePX + rect.height() / 2 - 1,
                    ZERO_LABEL
            ));

            lastLabelPX = height - labelPlacePX - rect.height() / 2;
            for (double y = 0; y <= maxY; y += maxScaleStepY) {
                String labelY = leadPlotY.provider.getLabelY(y);
                textPaint.getTextBounds(labelY, 0, labelY.length(), rect);
                float pxY = height - labelPlacePX - pxProY * (float) y + rect.height() / 2;
                if ((lastLabelPX - pxY >= spacingPXY) && (pxY - rect.height() >= 0)) {
                    lastLabelPX = pxY - rect.height();
                    labelsY.add(new Pair<>(pxY - 1, labelY));
                }
            }
        }
    }

    private Plot getPlotWithMaxScaleStepX() throws IllegalArgumentException {
        TreeSet<Plot> plotTreeSet = new TreeSet<>(new ComparatorScaleStepX());
        plotTreeSet.addAll(plots);
        Plot last = plotTreeSet.last();

        if (last.provider.getScaleStepX() == 0) {
            throw new IllegalArgumentException(
                    "At least one PointsProvider should return scaleStepX not equals 0"
            );
        }

        return last;
    }

    private Plot getPlotWithMaxScaleStepY() throws IllegalArgumentException {
        TreeSet<Plot> plotTreeSet = new TreeSet<>(new ComparatorScaleStepY());
        plotTreeSet.addAll(plots);
        Plot last = plotTreeSet.last();

        if (last.provider.getScaleStepY() == 0) {
            throw new IllegalArgumentException(
                    "At least one PointsProvider should return scaleStepY not equals 0"
            );
        }
        return last;
    }

    /**
     * Recalculate data for GraphView to draw
     *
     * @param observable
     * @param data
     */
    @Override
    public void update(Observable observable, Object data) {

        for (Plot plot : plots) {
            if (plot.provider.getPoints().isEmpty()) {
                clear();
                return;
            }
        }

        maxY = getMaxY();
        maxX = getMaxX();
        minX = getMinX();
        changeGraph();
        invalidate();
    }

    private double getMaxY() {
        Point.ComparatorY comparator = new Point.ComparatorY();
        double maxY = Collections.max(
                plots
                        .get(0)
                        .provider
                        .getPoints(),
                comparator
        ).getY();
        for (int i = 1; i < plots.size(); i++) {
            double y = Collections.max(plots.get(i).provider.getPoints(), comparator).getY();
            if (maxY < y) {
                maxY = y;
            }
        }
        return maxY;
    }

    private double getMaxX() {
        Point.ComparatorX comparator = new Point.ComparatorX();
        double maxX = Collections.max(plots.get(0).provider.getPoints(), comparator).getX();
        for (int i = 1; i < plots.size(); i++) {
            double x = Collections.max(plots.get(i).provider.getPoints(), comparator).getX();
            if (maxX < x) {
                maxX = x;
            }
        }
        return maxX;
    }

    private double getMinX() {
        Point.ComparatorX comparator = new Point.ComparatorX();
        double minX = Collections.min(plots.get(0).provider.getPoints(), comparator).getX();
        for (int i = 1; i < plots.size(); i++) {
            double x = Collections.min(plots.get(i).provider.getPoints(), comparator).getX();
            if (minX > x) {
                minX = x;
            }
        }
        return minX;
    }

    private void clear() {
        for (Plot plot : plots) {
            clearPlot(plot);
        }
        labelsX.clear();
        labelsY.clear();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        for (Plot plot : plots) {
            if (plot.provider.getPoints().isEmpty()) {
                clear();
                return;
            }
        }

        changeGraph();
        invalidate();
    }

    public void addPlot(PointsProvider pointsProvider, PlotStyle plotStyle) {
        plots.add(new Plot(pointsProvider, plotStyle));
        pointsProvider.addObserver(this);
        invalidate();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        invalidate();
    }

    /**
     * @return the color of lines that extends the Y-levels of labels
     */

    public int getLevelColor() {
        return levelColor;
    }

    /**
     * @param levelColor - the color of lines that extends the Y-levels of labels
     */
    public void setLevelColor(int levelColor) {
        this.levelColor = levelColor;
        invalidate();
    }

    /**
     * @return the color of lines of axis
     */
    public int getAxisColor() {
        return axisColor;
    }

    /**
     * @param axisColor - the color of lines of axis
     */
    public void setAxisColor(int axisColor) {
        this.axisColor = axisColor;
        invalidate();
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        invalidate();
    }

    /**
     * @return the desirable spacing between X-axis labels
     */
    public float getSpacingPXX() {
        return spacingPXX;
    }

    /**
     * @param spacingPXX - the desirable spacing between X-axis labels
     */
    public void setSpacingPXX(float spacingPXX) {
        this.spacingPXX = spacingPXX;
        invalidate();
    }

    /**
     * @return the desirable spacing in between Y-axis labels
     */
    public float getSpacingPXY() {
        return spacingPXY;
    }

    /**
     * @param spacingPXY - the desirable spacing between Y-axis labels
     */
    public void setSpacingPXY(float spacingPXY) {
        this.spacingPXY = spacingPXY;
        invalidate();
    }

    /**
     * @return the space in pixels for locating Labels on Graph Axis
     */
    public float getLabelPlacePX() {
        return labelPlacePX;
    }

    /**
     * @param labelPlacePX - the space in pixels for locating Labels on Graph Axis
     */
    public void setLabelPlacePX(float labelPlacePX) {
        this.labelPlacePX = labelPlacePX;
        invalidate();
    }

    private class Plot {
        PointsProvider provider;
        PlotStyle style;
        Path path = new Path();
        Path fillPath = new Path();
        List<Pair<Float, Float>> pointsPX = Collections.EMPTY_LIST;

        Plot(PointsProvider provider, PlotStyle style) {
            this.provider = provider;
            this.style = style;
        }
    }

    private class ComparatorScaleStepX implements Comparator<Plot> {
        @Override
        public int compare(Plot lhs, Plot rhs) {
            return Double.compare(lhs.provider.getScaleStepX(), rhs.provider.getScaleStepX());
        }
    }

    private class ComparatorScaleStepY implements Comparator<Plot> {
        @Override
        public int compare(Plot lhs, Plot rhs) {
            return Double.compare(lhs.provider.getScaleStepY(), rhs.provider.getScaleStepY());
        }
    }
}
