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
import java.util.List;
import java.util.Observable;
import java.util.Observer;

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
    private static final int DEFAULT_LINE_COLOR = Color.BLACK;
    private static final int DEFAULT_TEXT_COLOR = Color.DKGRAY;
    private static final int DEFAULT_FILL_COLOR = 0x44000000;
    private static final int DEFAULT_LEVEL_COLOR = 0x44888888;
    private final List<Pair<Float, String>> labelsX = new ArrayList<Pair<Float, String>>();
    private final List<Pair<Float, String>> labelsY = new ArrayList<Pair<Float, String>>();
    private int lineColor;
    private int textColor;
    private int fillColor;
    private int levelColor;
    private float textSize;
    private float spacingPXX;
    private float spacingPXY;
    private float labelPlacePX;
    private boolean enableXAxis;
    private boolean enableYAxis;
    private boolean enableLabels;
    private boolean enableFill;
    private Paint linePaint;
    private Paint textPaint;
    private Paint fillPaint;
    private Paint levelPaint;
    private int width;
    private int height;
    private List<PointsProvider> providers = new ArrayList<PointsProvider>();
    private List<Pair<Float, Float>> pointsPX = new ArrayList<Pair<Float, Float>>();
    private List<Path> paths = new ArrayList<Path>();
    private List<Path> fillPaths = new ArrayList<Path>();
    private float pxProY;
    private float pxProX;
    private double maxY;
    private double maxX;
    private double minX;
    private float textHeight = 0f;
    private Drawable pointDrawable;
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
            enableFill = a.getBoolean(R.styleable.GraphView_graphView_enableFill, true);
            lineColor = a.getColor(R.styleable.GraphView_graphView_lineColor, DEFAULT_LINE_COLOR);
            textColor = a.getColor(R.styleable.GraphView_graphView_textColor, DEFAULT_TEXT_COLOR);
            fillColor = a.getColor(R.styleable.GraphView_graphView_fillColor, DEFAULT_FILL_COLOR);
            levelColor = a.getColor(R.styleable.GraphView_graphView_levelColor, DEFAULT_LEVEL_COLOR);
            pointDrawable = a.getDrawable(R.styleable.GraphView_graphView_pointDrawable);

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
        enableFill = true;
        lineColor = DEFAULT_LINE_COLOR;
        textColor = DEFAULT_TEXT_COLOR;
        fillColor = DEFAULT_FILL_COLOR;
        levelColor = DEFAULT_LEVEL_COLOR;
        pointDrawable = getResources().getDrawable(R.drawable.point);

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
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(lineColor);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillColor);

        levelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        levelPaint.setStyle(Paint.Style.STROKE);
        levelPaint.setColor(levelColor);

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
        if (enableXAxis) {
            //Drawing X-Axis
            canvas.drawLine(
                    labelPlacePX, height - labelPlacePX - 1,
                    width - 1, height - labelPlacePX - 1,
                    linePaint
            );

            if (enableLabels) {
                //Drawing Labels
                textPaint.setTextAlign(Paint.Align.LEFT);
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

        if (enableYAxis) {
            //Drawing Y-Axis
            canvas.drawLine(
                    labelPlacePX, height - labelPlacePX - 1,
                    labelPlacePX, 0,
                    linePaint
            );


            if (enableLabels) {
                textPaint.setTextAlign(Paint.Align.RIGHT);
                for (Pair<Float, String> labelY : labelsY) {
                    //Drawing Y-Labels
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

        if (paths == null) {
            return;
        }

        if (enableFill) {
            //Drawing Fillings for Plot
            for (Path fillPath : fillPaths) {
                canvas.drawPath(fillPath, fillPaint);
            }
        }

        //Drawing Plots
        for (Path path : paths) {
            canvas.drawPath(path, linePaint);
        }

        //Drawing Points
        if (pointsPX == null) {
            return;
        }

        //TODO:Add ability to set or reset points for each plot
        if (pointDrawable != null) {

            for (Pair<Float, Float> point : pointsPX) {
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

    private void changePlot() {
        pxProY = (height - marginPX - labelPlacePX) / (float) maxY;
        pxProX = (width - labelPlacePX) / (float) (maxX - minX);

        pointsPX.clear();

        for (PointsProvider provider : providers) {
            changePath(provider.getPoints());
        }

        changeLabels();
    }

    private void changePath(List<Point> points) {
        paths.clear();
        Path path = new Path();
        float x = labelPlacePX;
        float y = (float) (height - labelPlacePX - pxProY * points.get(0).getY());
        path.moveTo(x, y);
        pointsPX.add(new Pair<Float, Float>(x, y));

        for (int i = 1; i < points.size(); i++) {
            x = (float) (labelPlacePX + pxProX * (points.get(i).getX() - minX));
            y = (float) (height - labelPlacePX - pxProY * points.get(i).getY());
            path.lineTo(x, y);
            pointsPX.add(new Pair<Float, Float>(x, y));
        }
        paths.add(path);

        fillPaths.clear();
        //TODO:ability to set or reset fill for every provider
        if (enableFill) {
            //Construct path for fill
            Path pathFill = new Path(path);
            pathFill.lineTo(
                    width - 1,
                    height - labelPlacePX - 1
            );
            pathFill.lineTo(
                    labelPlacePX,
                    height - labelPlacePX - 1
            );
            pathFill.close();
            fillPaths.add(pathFill);
        }
    }

    private void changeLabels() {
        float lastLabelPX = labelPlacePX;
        Rect rect = new Rect();
        labelsX.clear();
        labelsY.clear();

        if (enableXAxis && enableLabels) {
            //Part for X axis
            double minScaleStepX = maxX;
            int indexLeadProviderX = -1;
            for (int i = 0; i < providers.size(); i++) {
                double stepX = providers.get(i).getScaleStepX();
                if (stepX < minScaleStepX) {
                    minScaleStepX = stepX;
                    indexLeadProviderX = i;
                }
            }

            labelsX.clear();
            for (double x = minX; x <= maxX; x += minScaleStepX) {
                String labelX = providers.get(indexLeadProviderX).getLabelX(x);
                textPaint.getTextBounds(labelX, 0, labelX.length(), rect);
                float pxX = labelPlacePX + (float) (x - minX) * pxProX - rect.width() / 2;
                if ((pxX - lastLabelPX >= spacingPXX) && (pxX + rect.width() <= width)) {
                    lastLabelPX = pxX + rect.width();
                    labelsX.add(new Pair<Float, String>(pxX, labelX));
                }
            }
            textHeight = rect.height();
        }

        if (enableYAxis && enableLabels) {
            //Part for Y axis
            double minScaleStepY = maxY;
            int indexLeadProviderY = -1;
            for (int i = 0; i < providers.size(); i++) {
                double stepY = providers.get(i).getScaleStepY();
                if (stepY < minScaleStepY) {
                    minScaleStepY = stepY;
                    indexLeadProviderY = i;
                }
            }
            labelsY.clear();

            textPaint.getTextBounds(ZERO_LABEL, 0, ZERO_LABEL.length(), rect);
            labelsY.add(new Pair<Float, String>(height - labelPlacePX + rect.height() / 2, ZERO_LABEL));

            lastLabelPX = height - labelPlacePX - rect.height() / 2;
            for (double y = 0; y <= maxY; y += minScaleStepY) {
                String labelY = providers.get(indexLeadProviderY).getLabelY(y);
                textPaint.getTextBounds(labelY, 0, labelY.length(), rect);
                float pxY = height - labelPlacePX - pxProY * (float) y + rect.height() / 2;
                if ((lastLabelPX - pxY >= spacingPXY) && (pxY - rect.height() >= 0)) {
                    lastLabelPX = pxY - rect.height();
                    labelsY.add(new Pair<Float, String>(pxY, labelY));
                }
            }
        }
    }

    /**
     * Recalculate data for GraphView to draw
     *
     * @param observable
     * @param data
     */
    @Override
    public void update(Observable observable, Object data) {

        if (providers.isEmpty()) {
            clear();
            return;
        }


        maxY = getMaxY();
        maxX = getMaxX();
        minX = getMinX();
        changePlot();
        invalidate();
    }

    private double getMaxY() {
        Point.ComparatorY comparator = new Point.ComparatorY();
        double maxY = Collections.max(providers.get(0).getPoints(), comparator).getY();
        for (int i = 1; i < providers.size(); i++) {
            double y = Collections.max(providers.get(i).getPoints(), comparator).getY();
            if (maxY < y) {
                maxY = y;
            }
        }
        return maxY;
    }

    private double getMaxX() {
        Point.ComparatorX comparator = new Point.ComparatorX();
        double maxX = Collections.max(providers.get(0).getPoints(), comparator).getX();
        for (int i = 1; i < providers.size(); i++) {
            double x = Collections.max(providers.get(i).getPoints(), comparator).getX();
            if (maxX < x) {
                maxX = x;
            }
        }
        return maxX;
    }

    private double getMinX() {
        Point.ComparatorX comparator = new Point.ComparatorX();
        double minX = Collections.min(providers.get(0).getPoints(), comparator).getX();
        for (int i = 1; i < providers.size(); i++) {
            double x = Collections.min(providers.get(i).getPoints(), comparator).getX();
            if (minX > x) {
                minX = x;
            }
        }
        return minX;
    }

    private void clear() {
        paths.clear();
        fillPaths.clear();
        labelsX.clear();
        labelsY.clear();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        if (providers.isEmpty()) {
            return;
        }
        changePlot();
        invalidate();
    }

    public void addPointsProvider(PointsProvider pointsProvider) {
        providers.add(pointsProvider);
        pointsProvider.addObserver(this);
        invalidate();
    }

    public void setPointDrawable(Drawable pointDrawable) {
        this.pointDrawable = pointDrawable;
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
     * @return the color of area below the plot
     */
    public int getFillColor() {
        return fillColor;
    }

    /**
     * @param fillColor - the color of area below the plot
     */
    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
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

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
        invalidate();
    }
}
