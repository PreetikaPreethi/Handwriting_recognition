package com.example.handwritingrecognition;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.mlkit.vision.digitalink.Ink;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DrawView extends View {
    private Paint paint;
    private Path path;
    private List<Ink.Stroke> strokes;
    private List<Ink.Point> currentStrokePoints;
    private boolean isErasing = false;
    private int currentColor = Color.BLACK;
    private float lastX, lastY; // For eraser position
    private static final float ERASER_RADIUS = 30; // Eraser size

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        path = new Path();
        strokes = new ArrayList<>();

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Redraw all strokes
        for (Ink.Stroke stroke : strokes) {
            Path strokePath = createPathFromStroke(stroke);
            canvas.drawPath(strokePath, paint);
        }

        // Show small eraser indicator if erasing
        if (isErasing) {
            Paint eraserIndicator = new Paint();
            eraserIndicator.setColor(Color.GRAY);
            eraserIndicator.setStyle(Paint.Style.FILL);
            canvas.drawCircle(lastX, lastY, ERASER_RADIUS, eraserIndicator); // Small eraser icon
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        lastX = x;
        lastY = y;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isErasing) {
                    eraseStroke(x, y); // Erase strokes at touch position
                } else {
                    path.moveTo(x, y);
                    currentStrokePoints = new ArrayList<>();
                    currentStrokePoints.add(Ink.Point.create(x, y, System.currentTimeMillis()));
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isErasing) {
                    eraseStroke(x, y); // Continue erasing as the user moves
                } else {
                    path.lineTo(x, y);
                    currentStrokePoints.add(Ink.Point.create(x, y, System.currentTimeMillis()));
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!isErasing) {
                    Ink.Stroke.Builder strokeBuilder = Ink.Stroke.builder();
                    for (Ink.Point point : currentStrokePoints) {
                        strokeBuilder.addPoint(point);
                    }
                    strokes.add(strokeBuilder.build());
                }
                break;
        }
        invalidate();
        return true;
    }

    public Ink getInk() {
        Ink.Builder inkBuilder = Ink.builder();
        for (Ink.Stroke stroke : strokes) {
            inkBuilder.addStroke(stroke);
        }
        return inkBuilder.build();
    }

    public void clearCanvas() {
        strokes.clear();
        path.reset();
        invalidate();
    }

    public void enableEraser(boolean isEraserEnabled) {
        isErasing = isEraserEnabled;
        invalidate();
    }

    private void eraseStroke(float x, float y) {
        Iterator<Ink.Stroke> iterator = strokes.iterator();
        while (iterator.hasNext()) {
            Ink.Stroke stroke = iterator.next();
            for (Ink.Point point : stroke.getPoints()) {
                if (distance(point.getX(), point.getY(), x, y) < ERASER_RADIUS) {
                    iterator.remove(); // Remove the stroke if within eraser range
                    break;
                }
            }
        }
    }

    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private Path createPathFromStroke(Ink.Stroke stroke) {
        Path newPath = new Path();
        List<Ink.Point> points = stroke.getPoints();
        if (!points.isEmpty()) {
            newPath.moveTo(points.get(0).getX(), points.get(0).getY());
            for (int i = 1; i < points.size(); i++) {
                newPath.lineTo(points.get(i).getX(), points.get(i).getY());
            }
        }
        return newPath;
    }
}
