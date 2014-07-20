package com.github.anastasia.zaitsewa.graphview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;

import java.util.*;

/**
 * Class for drawing custom Graph
 * (contains axis, axis labels, non-interactive points(Drawables) and levels)
 */
public class GraphView extends View implements Observer {

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
    private boolean isXAxisEnable = true;
    private boolean isYAxisEnable = true;
    private Paint linePaint;
    private Paint textPaint;
    private Paint fillPaint;
    private Paint levelPaint;
    private int width;
    private int height;
    private PointsProvider pointsProvider;
    private List<Point> points;
    private float[] pointsPX;
    private Path path;
    private Path pathFill;
    private float pxProY;
    private float pxProX;
    private double maxY;
    private double maxX;
    private double minX;
    private float textHeight;
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
            isXAxisEnable = a.getBoolean(R.styleable.GraphView_isXAxisEnable, true);
            isYAxisEnable = a.getBoolean(R.styleable.GraphView_isYAxisEnable, true);
            lineColor = a.getColor(R.styleable.GraphView_lineColor, DEFAULT_LINE_COLOR);
            textColor = a.getColor(R.styleable.GraphView_textColor, DEFAULT_TEXT_COLOR);
            fillColor = a.getColor(R.styleable.GraphView_fillColor, DEFAULT_FILL_COLOR);
            levelColor = a.getColor(R.styleable.GraphView_levelColor, DEFAULT_LEVEL_COLOR);
            pointDrawable = a.getDrawable(R.styleable.GraphView_pointDrawable);

            textSize = a.getDimension(R.styleable.GraphView_textSize,
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            DEFAULT_TEXT_SIZE_SP,
                            getResources().getDisplayMetrics()
                    )
            );
            spacingPXX = a.getDimensionPixelOffset(
                    R.styleable.GraphView_spacingX,
                    (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            DEFAULT_SPACING_BETWEEN_LABELS_DP,
                            getResources().getDisplayMetrics()
                    )
            );
            spacingPXY = a.getDimensionPixelOffset(
                    R.styleable.GraphView_spacingY,
                    (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            DEFAULT_SPACING_BETWEEN_LABELS_DP,
                            getResources().getDisplayMetrics()
                    )
            );
            labelPlacePX = a.getDimensionPixelOffset(
                    R.styleable.GraphView_labelPlace,
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
        if (isXAxisEnable) {
            //Drawing X-Axis and X-Labels
            canvas.drawLine(
                    labelPlacePX, height - labelPlacePX - 1,
                    width - 1, height - labelPlacePX - 1,
                    linePaint
            );

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

        if (isYAxisEnable) {
            //Drawing Y-Axis, Y-Labels and Y-Levels
            canvas.drawLine(
                    labelPlacePX, height - labelPlacePX - 1,
                    labelPlacePX, 0,
                    linePaint
            );

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
                        levelPaint);
            }
        }

        if (path == null) {
            return;
        }

        //Drawing Fill for Plot
        canvas.drawPath(pathFill, fillPaint);

        //Drawing Plot
        canvas.drawPath(path, linePaint);

        //Drawing Points
        if (pointsPX == null) {
            return;
        }

        if (pointDrawable != null) {

            for (int i = 0; i < pointsPX.length; i = i + 2) {
                float x = pointsPX[i];
                float y = pointsPX[i + 1];

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

        changePath(labelPlacePX);
        changeLabels(labelPlacePX);
    }

    private void changePath(float labelPlacePX) {
        path = new Path();
        pointsPX = new float[points.size() * 2];

        float x = labelPlacePX;
        float y = (float) (height - labelPlacePX - pxProY * points.get(0).getY());
        path.moveTo(x, y);
        pointsPX[0] = x;
        pointsPX[1] = y;

        for (int i = 1; i < points.size(); i++) {
            x = (float) (labelPlacePX + pxProX * (points.get(i).getX() - minX));
            y = (float) (height - labelPlacePX - pxProY * points.get(i).getY());
            path.lineTo(x, y);
            pointsPX[i * 2] = x;
            pointsPX[i * 2 + 1] = y;
        }
        pathFill = new Path(path);
        pathFill.lineTo(
                width - 1,
                height - labelPlacePX - 1
        );
        pathFill.lineTo(
                labelPlacePX,
                height - labelPlacePX - 1
        );
        pathFill.close();
    }

    private void changeLabels(float labelPlacePX) {
        float lastLabelPX = labelPlacePX;
        Rect rect = new Rect();

        //Part for X axis
        double scaleStepX = pointsProvider.getScaleStepX();
        labelsX.clear();
        for (double x = minX; x <= maxX; x += scaleStepX) {
            String labelX = pointsProvider.getLabelX(x);
            textPaint.getTextBounds(labelX, 0, labelX.length(), rect);
            float pxX = labelPlacePX + (float) (x - minX) * pxProX - rect.width() / 2;
            if ((pxX - lastLabelPX >= spacingPXX) && (pxX + rect.width() <= width)) {
                lastLabelPX = pxX + rect.width();
                labelsX.add(new Pair<Float, String>(pxX, labelX));
            }
        }

        textHeight = rect.height();

        //Part for Y axis
        double scaleStepY = pointsProvider.getScaleStepY();
        labelsY.clear();

        String zeroLabel = "0";
        textPaint.getTextBounds(zeroLabel, 0, zeroLabel.length(), rect);
        labelsY.add(new Pair<Float, String>(height - labelPlacePX + rect.height() / 2, zeroLabel));

        lastLabelPX = height - labelPlacePX - rect.height() / 2;
        for (double y = 0; y <= maxY; y += scaleStepY) {
            String labelY = pointsProvider.getLabelY(y);
            textPaint.getTextBounds(labelY, 0, labelY.length(), rect);
            float pxY = height - labelPlacePX - pxProY * (float) y + rect.height() / 2;
            if ((lastLabelPX - pxY >= spacingPXY) && (pxY - rect.height() >= 0)) {
                lastLabelPX = pxY - rect.height();
                labelsY.add(new Pair<Float, String>(pxY, labelY));
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
        points = pointsProvider.getPoints();
        if (points.isEmpty()) {
            clear();
            return;
        }

        maxY = Collections.max(points, new Point.ComparatorY()).getY();
        Collections.sort(points, new Point.ComparatorX());
        maxX = points.get(points.size() - 1).getX();
        minX = points.get(0).getX();
        changePlot();
        invalidate();
    }

    private void clear() {
        path = null;
        pathFill = null;
        labelsX.clear();
        labelsY.clear();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        if (points.isEmpty()) {
            return;
        }
        changePlot();
        invalidate();
    }

    public void setPointsProvider(PointsProvider pointsProvider) {
        this.pointsProvider = pointsProvider;
        points = pointsProvider.getPoints();
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
