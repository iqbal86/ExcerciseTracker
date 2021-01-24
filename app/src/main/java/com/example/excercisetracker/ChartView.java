package com.example.excercisetracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ChartView extends View {

    protected final List<Point> chartPoints;
    protected final Paint paint = new Paint();
    protected final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint borderPaint = new Paint();
    protected final ShapeDrawable chartDrawable;
    protected final ShapeDrawable yLabelDrawable;
    protected final ShapeDrawable xLabelDrawable;
    protected ChartStyle chartStyle;
    protected Long manualXGridUnit = null;
    protected Long manualYGridUnit = null;
    protected long yLabelWidth = 0;
    protected long xLabelHeight = 0;
    protected long chartTopMargin = 0;
    protected long chartRightMargin = 0;
    protected List<Long> manualXLabels = null;
    protected List<Long> manualYLabels = null;
    protected Long manualMinX = null;
    protected Long manualMaxX = null;
    protected Long manualMinY = null;
    protected Long manualMaxY = null;
    private static final long DEFAULT_MAX_X = 1000;
    private static final long DEFAULT_MAX_Y = 1000;


    public ChartView(Context context) {
        this(context, new ArrayList<Point>());
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        chartPoints = new ArrayList<>();
        chartStyle = new ChartStyle();
        paint.setAntiAlias(true);
        yLabelDrawable = new ShapeDrawable();
        xLabelDrawable = new ShapeDrawable();
        chartDrawable = new ShapeDrawable();
        updateIfEditMode();
        updateDrawables();
    }

    public ChartView(Context context, List<Point> chartPoints) {
        this(context, chartPoints, new ChartStyle());
    }

    public ChartView(Context context, ChartStyle chartStyle) {
        this(context, new ArrayList<Point>(), chartStyle);
    }

    public ChartView(Context context, List<Point> chartPoints, ChartStyle chartStyle) {
        super(context);
        this.chartPoints = chartPoints;
        this.chartStyle = chartStyle;
        paint.setAntiAlias(true);
        yLabelDrawable = new ShapeDrawable();
        xLabelDrawable = new ShapeDrawable();
        chartDrawable = new ShapeDrawable();
        updateDrawables();
    }
    // redraw the graph
    public void updateDrawables() {
        drawXLabels(xLabelDrawable);
        drawYLabels(yLabelDrawable);
        drawLineChart(chartDrawable);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        yLabelDrawable.draw(canvas);
        xLabelDrawable.draw(canvas);
        chartDrawable.draw(canvas);
    }
    // draw the labels on Y axis
    protected void drawYLabels(ShapeDrawable labelDrawable) {
        Shape labelsShape = new Shape() {
            @Override
            public void draw(Canvas canvas, Paint paint) {
                labelPaint.setTextAlign(Paint.Align.RIGHT);
                labelPaint.setTextSize(chartStyle.getLabelTextSize());
                labelPaint.setColor(chartStyle.getLabelTextColor());
                long minY = getMinY();
                long maxY = getMaxY();
                long yrange = maxY - minY; // range of values on Y axis
                float height = getHeight();

                float left = getYLabelWidth();
                List<Long> yLabels = getYLabels();
                for (long y : yLabels) {
                    String label = formatYLabel(y);
                    float yCoordinate = getYCoordinate(height, y, minY, yrange);
                    canvas.drawText(label, left, yCoordinate, labelPaint);
                }
            }
        };
        measureYLabel();
        labelDrawable.setBounds(0, 0, getWidth(), getHeight());
        labelDrawable.setShape(labelsShape);
    }

    protected void measureYLabel() {
        labelPaint.setTextAlign(Paint.Align.RIGHT);
        labelPaint.setTextSize(chartStyle.getLabelTextSize());

        long minY = getMinY();
        long maxY = getMaxY();
        long yGridUnit = getYGridUnit();

        yLabelWidth = 0;
        chartTopMargin = 0;

        long y = minY;
        Rect textBounds = new Rect();

        while (y <= maxY) {
            String label = formatYLabel(y);
            labelPaint.getTextBounds(label, 0, label.length(), textBounds);
            if (textBounds.width() > yLabelWidth) {
                yLabelWidth = textBounds.width();
            }
            chartTopMargin = textBounds.height();
            y += yGridUnit;
        }
    }

    protected String formatYLabel(long y) {
        if (chartStyle.getYLabelFormatter() != null) {
            return chartStyle.getYLabelFormatter().format(y);
        }
        return String.format("%,d", y);
    }
    // draw the labels of X axis
    protected void drawXLabels(ShapeDrawable labelDrawable) {
        Shape labelsShape = new Shape() {
            @Override
            public void draw(Canvas canvas, Paint paint) { // define the shape of labels
                labelPaint.setTextAlign(Paint.Align.CENTER);
                labelPaint.setTextSize(chartStyle.getLabelTextSize());
                labelPaint.setColor(chartStyle.getLabelTextColor());

                long minX = getMinX();
                long maxX = getMaxX();
                long xrange = maxX - minX; // range of values of X axis

                float width = getWidth();
                float height = getHeight();

                float labelHeight = height - chartStyle.getXLabelMargin();
                Rect textBounds = new Rect();
                List<Long> xLabels = getXLabels();
                for (long x : xLabels) {
                    String label = formatXLabel(x);
                    labelPaint.getTextBounds(label, 0, label.length(), textBounds);
                    float xCoordinate = getXCoordinate(width, x, minX, xrange);
                    canvas.drawText(label, xCoordinate, labelHeight, labelPaint);
                }
            }
        };
        measureXLabel();
        labelDrawable.setBounds(0, 0, getWidth(), getHeight());
        labelDrawable.setShape(labelsShape);
    }

    protected void measureXLabel() {
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(chartStyle.getLabelTextSize());

        long minX = getMinX();
        long maxX = getMaxX();
        long xGridUnit = getXGridUnit();

        xLabelHeight = 0;
        chartRightMargin = 0;

        long x = minX;
        Rect textBounds = new Rect();

        while (x <= maxX) {
            String label = formatXLabel(x);
            labelPaint.getTextBounds(label, 0, label.length(), textBounds);
            int height = (int) (textBounds.height() + chartStyle.getXLabelMargin() * 2);
            if (height > xLabelHeight) {
                xLabelHeight = height;
            }
            chartRightMargin = textBounds.width() / 2;
            x += xGridUnit;
        }
    }

    protected String formatXLabel(long x) {
        if (chartStyle.getXLabelFormatter() != null) {
            return chartStyle.getXLabelFormatter().format(x);
        }
        return String.format("%,d", x);
    }

    protected void drawLineChart(ShapeDrawable chartDrawable) {
        Shape chartShape = new Shape() {
            @Override
            public void draw(Canvas canvas, Paint paint) {
                long minX = getMinX();
                long maxX = getMaxX();
                long xrange = maxX - minX;

                long minY = getMinY();
                long maxY = getMaxY();
                long yrange = maxY - minY;

                float width = getWidth();
                float height = getHeight();
                float left = getChartLeftMargin();
                float top = getChartTopMargin();
                float right = width - getChartRightMargin();
                float bottom = height - getChartBottomMargin();

                drawChartFrame(canvas, left, top, right, bottom);

                drawXGrid(canvas, minX, xrange);
                drawYGrid(canvas, minY, yrange);

                List<ChartStyle.Border> borders = chartStyle.getBorders();
                for (ChartStyle.Border border : borders) {
                    drawChartBorder(canvas, border, left, top, right, bottom);
                }

                drawLines(canvas, minX, xrange, minY, yrange);

                if (chartStyle.isDrawPoint()) {
                    drawPoints(canvas, minX, xrange, minY, yrange);
                }
            }
        };
        chartDrawable.setBounds(0, 0, getWidth(), getHeight());
        chartDrawable.setShape(chartShape);
    }

    protected void drawChartFrame(Canvas canvas, float left, float top, float right, float bottom) {
        canvas.save();
        canvas.clipRect(left, top, right, bottom);
        canvas.drawColor(chartStyle.getBackgroundColor());
        canvas.restore();
    }

    protected void drawChartBorder(Canvas canvas, ChartStyle.Border border, float left, float top, float right, float bottom) {
        borderPaint.setColor(border.getColor());
        borderPaint.setStrokeWidth(border.getWidth());

        float fixWidth = border.getWidth() / 2;
        float leftFix = border.left() ? fixWidth : 0;
        float topFix = border.top() ? fixWidth : 0;
        float bottomFix = border.bottom() ? fixWidth : 0;
        float rightFix = border.right() ? fixWidth : 0;
        if (border.left()) {
            canvas.drawLine(left, top - topFix, left, bottom + bottomFix, borderPaint);
        }
        if (border.top()) {
            canvas.drawLine(left - leftFix, top, right + rightFix, top, borderPaint);
        }
        if (border.right()) {
            canvas.drawLine(right, top - topFix, right, bottom + bottomFix, borderPaint);
        }
        if (border.bottom()) {
            canvas.drawLine(left - leftFix, bottom, right + rightFix, bottom, borderPaint);
        }
    }

    public float getChartLeftMargin() {
        return getYLabelWidth() + chartStyle.getYLabelMargin();
    }

    public float getChartTopMargin() {
        return chartTopMargin;
    }

    public float getChartRightMargin() {
        return chartRightMargin;
    }

    public float getChartBottomMargin() {
        return getXLabelHeight() + chartStyle.getXLabelMargin();
    }

    public void clearManualMinX() {
        manualMinX = null;
        updateDrawables();
    }

    public void setManualMinX(long minX) {
        manualMinX = minX;
        updateDrawables();
    }

    public long getMinX() {
        if (manualMinX != null) {
            return manualMinX;
        }
        return getRawMinX();
    }

    public long getRawMinX() {
        if (chartPoints.isEmpty()) {
            return 0;
        }
        return chartPoints.get(0).getxAxis(); // returns the first value in the list
    }

    public void clearManualMaxX(){
        manualMaxX = null;
        updateDrawables();
    }

    public void setManualMaxX(long maxX) {
        manualMaxX = maxX;
        updateDrawables();
    }
    // gets the maximum value of X
    public long getMaxX() {
        if (manualMaxX != null) {
            return manualMaxX;
        }
        long rawMaxX = getRawMaxX();
        long step = getUnit(getAbsMaxX());
        return (long) ((Math.floor(1.0 * rawMaxX / step) + 1) * step);
    }

    public long getRawMaxX() {
        if (chartPoints.isEmpty()) {
            return DEFAULT_MAX_X;
        }
        return chartPoints.get(chartPoints.size() - 1).getxAxis();
    }

    protected long getAbsMaxX() {
        if (chartPoints.isEmpty()) {
            return DEFAULT_MAX_X;
        }
        long absMaxX = Long.MIN_VALUE;
        for (Point point : chartPoints) {
            long x = Math.abs(point.getxAxis());
            if (x > absMaxX) {
                absMaxX = x;
            }
        }
        return absMaxX;
    }

    protected float getXCoordinate(float width, Point point, long minX, long xrange) {
        return getXCoordinate(width, point.getxAxis(), minX, xrange);
    }

    protected float getXCoordinate(float width, long x, long minX, long xrange) {
        return getXCoordinate(width, x, minX, xrange, true);
    }

    protected float getXCoordinate(float width, long x, long minX, long xrange, boolean inChartArea) {
        if (inChartArea) {
            float left = getChartLeftMargin();
            float right = getChartRightMargin();
            float margin = left + right;
            return (width - margin) * (x - minX) * 1.0f / (xrange) + left;
        } else {
            return width * (x - minX) * 1.0f / xrange;
        }
    }

    protected long getAbsMaxY() {
        if (chartPoints.isEmpty()) {
            return DEFAULT_MAX_Y;
        }
        long absMaxY = Long.MIN_VALUE;
        for (Point point : chartPoints) {
            long y = Math.abs(point.getyAxis());
            if (y > absMaxY) {
                absMaxY = y;
            }
        }
        return absMaxY;
    }

    public void clearManualMinY() {
        manualMinY = null;
        updateDrawables();
    }

    public void setManualMinY(long minY) {
        manualMinY = minY;
        updateDrawables();
    }

    public long getMinY() {
        if (manualMinY != null) {
            return manualMinY;
        }
        long rawMinY = getRawMinY();
        long step = getUnit(getAbsMaxY());
        return (long) ((Math.ceil(1.0 * rawMinY / step)) * step);
    }

    public long getRawMinY() {
        if (chartPoints.isEmpty()) {
            return 0;
        }
        long minY = Long.MAX_VALUE;
        for (Point point : chartPoints) {
            long y = point.getyAxis();
            if (y < minY) {
                minY = y;
            }
        }
        return minY;
    }

    public void clearManualMaxY() {
        manualMaxY = null;
        updateDrawables();
    }

    public void setManualMaxY(long maxY) {
        manualMaxY = maxY;
        updateDrawables();
    }

    public long getMaxY() {
        if (manualMaxY != null) {
            return manualMaxY;
        }
        long rawMaxY = getRawMaxY();
        long step = getUnit(getAbsMaxY());
        return (long) ((Math.floor(1.0 * rawMaxY / step) + 1) * step);
    }

    public long getRawMaxY() {
        if (chartPoints.isEmpty()) {
            return DEFAULT_MAX_Y;
        }
        long maxY = Long.MIN_VALUE;
        for (Point point : chartPoints) {
            long y = point.getyAxis();
            if (y > maxY) {
                maxY = y;
            }
        }
        return maxY;
    }

    protected long getUnit(long maxValue) {
        int digits = (int) Math.log10(maxValue);
        long unit = (long) Math.pow(10, digits);
        return unit;
    }

    protected float getYCoordinate(float height, Point point, long minY, long yrange) {
        return getYCoordinate(height, point.getyAxis(), minY, yrange);
    }

    protected float getYCoordinate(float height, long y, long minY, long yrange) {
        return getYCoordinate(height, y, minY, yrange, true);
    }

    protected float getYCoordinate(float height, long y, long minY, long yrange, boolean inChartArea) {
        if (inChartArea) {
            float top = getChartTopMargin();
            float bottom = getChartBottomMargin();
            float margin = top + bottom;
            return (height - margin) * (1.0f - (y - minY) * 1.0f / (yrange)) + top;
        } else {
            return height * (1.0f - (y - minY) * 1.0f / yrange);
        }
    }

    // function to draw the X grid
    protected void drawXGrid(Canvas canvas, long minX, long xrange) {
        long maxX = getMaxX();
        long xGridUnit = getXGridUnit();

        float width = getWidth();
        float height = getHeight();

        float top = getChartTopMargin();
        float bottom = height - getChartBottomMargin();

        paint.setColor(chartStyle.getGridColor());
        paint.setStrokeWidth(chartStyle.getGridWidth());

        long x = calcMinGridValue(minX, xGridUnit);

        while (x <= maxX) {
            float xCoordinate = getXCoordinate(width, x, minX, xrange);
            canvas.drawLine(xCoordinate, bottom, xCoordinate, top, paint);
            x += xGridUnit;
        }
    }

    // function to draw the Y grid
    protected void drawYGrid(Canvas canvas, long minY, long yrange) {
        long yGridUnit = getYGridUnit();
        long maxY = getMaxY();

        float width = getWidth();
        float height = getHeight();

        float left = getChartLeftMargin();
        float right = width - getChartRightMargin();

        paint.setColor(chartStyle.getGridColor());
        paint.setStrokeWidth(chartStyle.getGridWidth());

        long y = calcMinGridValue(minY, yGridUnit);
        while (y <= maxY) {
            float yCoordinate = getYCoordinate(height, y, minY, yrange);
            canvas.drawLine(left, yCoordinate, right, yCoordinate, paint);
            y += yGridUnit;
        }
    }
    // function to draw the graph line.
    protected void drawLines(Canvas canvas, long minX, long xrange, long minY, long yrange) {
        Point prevPoint = null;
        float px = 0.0f, py = 0.0f;

        float width = getWidth();
        float height = getHeight();

        paint.setColor(chartStyle.getLineColor());
        paint.setStrokeWidth(chartStyle.getLineWidth());
        for (Point point : chartPoints) {
            float x = getXCoordinate(width, point, minX, xrange);
            float y = getYCoordinate(height, point, minY, yrange);
            if (prevPoint != null) {
                canvas.drawLine(px, py, x, y, paint);
            }
            prevPoint = point;
            px = x;
            py = y;
        }
    }
// function to draw points on the graph
    protected void drawPoints(Canvas canvas, long minX, long xrange, long minY, long yrange) {
        float width = getWidth();
        float height = getHeight();

        for (Point point : chartPoints) {
            float x = getXCoordinate(width, point, minX, xrange);
            float y = getYCoordinate(height, point, minY, yrange);

            paint.setColor(chartStyle.getLineColor());
            canvas.drawCircle(x, y, chartStyle.getPointSize(), paint);

            if (chartStyle.isDrawPointCenter()) {
                paint.setColor(chartStyle.getBackgroundColor());
                canvas.drawCircle(x, y, chartStyle.getPointCenterSize(), paint);
            }
        }
    }

    public void clearManualXGridUnit() {
        manualXGridUnit = null;
        updateDrawables();
    }

    public void setManualXGridUnit(long xGridUnit) {
        manualXGridUnit = xGridUnit;
        updateDrawables();
    }

    public long getXGridUnit() {
        if (manualXGridUnit != null) {
            return manualXGridUnit;
        }
        return getUnit(getAbsMaxX());
    }

    public void clearManualYGridUnit() {
        manualYGridUnit = null;
        updateDrawables();
    }

    public void setManualYGridUnit(long yGridUnit) {
        manualYGridUnit = yGridUnit;
        updateDrawables();
    }

    public long getYGridUnit() {
        if (manualYGridUnit != null) {
            return manualYGridUnit;
        }
        return getUnit(getAbsMaxY());
    }

    public List<Long> getXLabels() {
        if (manualXLabels != null) {
            return manualXLabels;
        }

        long minX = getMinX();
        long maxX = getMaxX();
        long xGridUnit = getXGridUnit();
        long x = calcMinGridValue(minX, xGridUnit);
        List<Long> xLabels = new ArrayList<>();
        while (x <= maxX) {
            xLabels.add(x);
            x += xGridUnit;
        }
        return xLabels;
    }

    public void clearManualXLabels() {
        manualXLabels = null;
        updateDrawables();
    }

    public void setManualXLabels(List<Long> labels) {
        manualXLabels = labels;
        updateDrawables();
    }

    public List<Long> getYLabels() {
        if (manualYLabels != null) {
            return manualYLabels;
        }

        long minY = getMinY();
        long maxY = getMaxY();
        long yGridUnit = getYGridUnit();
        long y = calcMinGridValue(minY, yGridUnit);
        List<Long> yLabels = new ArrayList<>();
        while (y <= maxY) {
            yLabels.add(y);
            y += yGridUnit;
        }
        return yLabels;
    }

    public void clearManualYLabels() {
        manualYLabels = null;
        updateDrawables();
    }

    public void setManualYLabels(List<Long> labels) {
        manualYLabels = labels;
        updateDrawables();
    }

    // calculate the minimum values of grid axis
    protected long calcMinGridValue(long min, long gridUnit) {
        return (long) (Math.ceil(1.0 * min / gridUnit) * gridUnit);
    }

    // getter function of label width
    public float getYLabelWidth() {
        if (chartStyle.getYLabelWidth() != ChartStyle.AutoWidth) {
            return chartStyle.getYLabelWidth();
        }
        return yLabelWidth;
    }
    // getter function of label heigh
    public float getXLabelHeight() {
        if (chartStyle.getXLabelHeight() != ChartStyle.AutoHeight) {
            return chartStyle.getXLabelHeight();
        }
        return xLabelHeight;
    }

    public ChartStyle getStyle() {
        return chartStyle;
    }
// setter function of char style for axis labels
    public void setStyle(ChartStyle chartStyle) {
        this.chartStyle = chartStyle;
        updateDrawables();
    }

    public List<Point> getChartPoints() {
        return chartPoints;
    }

    public void setChartPoints(List<Point> chartPoints) {
        this.chartPoints.clear();
        this.chartPoints.addAll(chartPoints);
        updateDrawables();
    }

    private void updateIfEditMode() {
        if (!isInEditMode()) {
            return;
        }
        List<Point> points = new ArrayList<>();
        points.add(new Point(-17, -100));
        points.add(new Point(4, 200));
        points.add(new Point(5, 400));
        points.add(new Point(6, 1100));
        points.add(new Point(7, 700));
        setChartPoints(points);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateDrawables();
    }
}
