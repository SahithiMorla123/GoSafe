package com.example.gosafe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.LinkedList;

public class SensorGraphView extends View {
    private final Paint paint = new Paint();
    private final Path path = new Path();
    private final LinkedList<Float> dataPoints = new LinkedList<>();
    private final int MAX_DATA_POINTS = 100;

    public SensorGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.CYAN); // Set the graph line color
        paint.setStrokeWidth(5f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true); // Smooths out the line
    }

    public void addDataPoint(float value) {
        dataPoints.add(value);
        if (dataPoints.size() > MAX_DATA_POINTS) {
            dataPoints.removeFirst();
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataPoints.isEmpty()) {
            return;
        }

        path.reset();
        float width = getWidth();
        float height = getHeight();
        float xStep = width / (MAX_DATA_POINTS - 1);


        float maxVal = 20f;

        path.moveTo(0, height - (dataPoints.getFirst() / maxVal * height));

        for (int i = 1; i < dataPoints.size(); i++) {
            float x = i * xStep;
            float y = height - (dataPoints.get(i) / maxVal * height);
            path.lineTo(x, y);
        }
        canvas.drawPath(path, paint);
    }
}