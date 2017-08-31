package com.ns.greg.fancy_seekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gregory on 2017/2/22.
 */
public class FancySeekBar extends View {

  // Progressbar background color
  private int[] backgroundColors;
  // Progressbar height
  private float progressBarHeight;
  // Regular marker text size
  private float markerTextSize;
  // Focus marker text size
  private float focusMarkerTextSize;
  // Seek bar start offset
  private int starOffset = 0;
  // Seek bar end offset
  private int endOffset;
  // Progressbar min progress
  private int min;
  // Progressbar current progress
  private int progress;
  // Progressbar max progress
  private int max;
  private final float padding = 12f;
  private final Paint markerPaint;
  private final Paint focusMarkerPaint;
  private final Paint barPaint;
  // For measure text bounds
  private final Rect bounds = new Rect();
  // Seek bar rect
  private final Rect barRect = new Rect();
  // Gesture detector
  private final GestureDetector gestureDetector;
  // Label list
  private final List<String> markerLabels = new ArrayList<>();

  public FancySeekBar(Context context) {
    this(context, null);
  }

  public FancySeekBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public FancySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    //load styled attributes.
    final TypedArray attributes = context.getTheme()
        .obtainStyledAttributes(attrs, R.styleable.MarkerSeekBar, defStyleAttr, 0);

    backgroundColors = new int[1];
    backgroundColors[0] =
        attributes.getColor(R.styleable.MarkerSeekBar_background_color, Color.GRAY);
    progressBarHeight = attributes.getDimension(R.styleable.MarkerSeekBar_seek_bar_height, 30f);
    float markerWidth = attributes.getDimension(R.styleable.MarkerSeekBar_marker_width, 6f);
    markerTextSize = attributes.getDimension(R.styleable.MarkerSeekBar_marker_text_size, 24f);
    int markerColor = attributes.getColor(R.styleable.MarkerSeekBar_marker_color, Color.GRAY);
    float focusMarkerWidth =
        attributes.getDimension(R.styleable.MarkerSeekBar_focus_marker_width, markerWidth * 3);
    focusMarkerTextSize =
        attributes.getDimension(R.styleable.MarkerSeekBar_focus_marker_text_size, 30f);
    int focusMarkerColor =
        attributes.getColor(R.styleable.MarkerSeekBar_focus_marker_color, Color.RED);

    progress = attributes.getInt(R.styleable.MarkerSeekBar_progress_current, 180);
    min = attributes.getInt(R.styleable.MarkerSeekBar_progress_min, 0);
    max = attributes.getInt(R.styleable.MarkerSeekBar_progress_max, 180);
    setProgress(progress);
    setMax(max);

    attributes.recycle();

    markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    markerPaint.setStrokeWidth(markerWidth);
    markerPaint.setColor(markerColor);
    markerPaint.setTextSize(markerTextSize);

    focusMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    focusMarkerPaint.setStrokeWidth(focusMarkerWidth);
    focusMarkerPaint.setColor(focusMarkerColor);
    focusMarkerPaint.setTextSize(focusMarkerTextSize);

    barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    barPaint.setStyle(Paint.Style.FILL);

    gestureDetector = new GestureDetector(getContext(), new SeekBarGestureDetector(this));
    setOnTouchListener(new OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
      }
    });
  }

  public void setMarkerLabels(List<String> labels) {
    if (labels != null) {
      int size = labels.size();
      if (size > 1) {
        markerLabels.clear();
        markerLabels.addAll(labels);

        String startMarker = labels.get(0);
        markerPaint.getTextBounds(startMarker, 0, startMarker.length(), bounds);
        starOffset = bounds.width() / 2;

        String endMarker = labels.get(size - 1);
        markerPaint.getTextBounds(endMarker, 0, endMarker.length(), bounds);
        endOffset = bounds.width() / 2;
      }
    }
  }

  public void setBarColors(@ColorRes int[] colors) {
    if (colors == null || colors.length == 1) {
      return;
    }

    backgroundColors = colors;
  }

  /**
   * The minimum height to show seek bar.
   * [NOTICED], plus one more padding is to make sure the top label
   * has enough height.
   *
   * e.g.
   *
   * ---------------
   * |   labels    |
   * ---------------
   * | padding * 2 |
   * ---------------
   * |   seek bar  |
   * ---------------
   * | padding * 2 |
   * ---------------
   * |    labels   |
   * ---------------
   */
  @Override protected int getSuggestedMinimumHeight() {
    return (int) (progressBarHeight + focusMarkerTextSize + markerTextSize + padding * 5);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
  }

  private int measureWidth(int measureSpec) {
    int result;
    int mode = MeasureSpec.getMode(measureSpec);
    int size = MeasureSpec.getSize(measureSpec);
    int padding = getPaddingLeft() + getPaddingRight();
    if (mode == MeasureSpec.EXACTLY) {
      result = size;
    } else {
      result = getSuggestedMinimumWidth();
      result += padding;
      if (mode == MeasureSpec.AT_MOST) {
        result = Math.max(result, size);
      }
    }

    return result;
  }

  private int measureHeight(int measureSpec) {
    int result;
    int mode = MeasureSpec.getMode(measureSpec);
    int size = MeasureSpec.getSize(measureSpec);
    int padding = getPaddingTop() + getPaddingBottom();
    if (mode == MeasureSpec.EXACTLY) {
      result = size;
    } else {
      result = getSuggestedMinimumHeight();
      result += padding;
      if (mode == MeasureSpec.AT_MOST) {
        result = Math.min(result, size);
      }
    }

    return result;
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (changed) {
      barRect.left = (int) (getPaddingLeft() + starOffset + padding);
      barRect.top = (int) (getHeight() / 2 - (progressBarHeight / 2));
      barRect.right = (int) (getWidth() - endOffset - padding);
      barRect.bottom = (int) (getHeight() / 2 + (progressBarHeight / 2));
    }
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    drawProgressbar(canvas);
    drawMarker(canvas);
    drawFocusMarker(canvas);
  }

  private void drawProgressbar(Canvas canvas) {
    if (backgroundColors == null || backgroundColors.length == 0) {
      barPaint.setColor(Color.TRANSPARENT);
    } else {
      int colorLength = backgroundColors.length;
      if (colorLength == 1) {
        barPaint.setColor(backgroundColors[0]);
      } else {
        float gradient = 1f / colorLength;
        float[] gradients = new float[colorLength];
        for (int i = 0; i < colorLength; i++) {
          gradients[i] = gradient * (i + 1);
        }

        LinearGradient linearGradient =
            new LinearGradient(barRect.left, barRect.bottom, barRect.right, barRect.bottom,
                backgroundColors, gradients, Shader.TileMode.CLAMP);
        barPaint.setShader(linearGradient);
      }
    }

    // [Draw bar]
    canvas.drawRoundRect(new RectF(barRect), 30, 30, barPaint);
  }

  private void drawMarker(Canvas canvas) {
    int labelSize = markerLabels.size();
    if (labelSize == 0) {
      return;
    }

    // [Draw marker]
    float rate = barRect.width() / (labelSize - 1);
    for (int i = 0; i < labelSize; i++) {
      float scaleTop = 1;
      float scaleBottom = 1;

      if (i == 0 || i == labelSize - 1) {
        scaleTop = 0.9f;
        scaleBottom = 1.1f;
      }

      float x = barRect.left + (i * rate);
      // [Line]
      canvas.drawLine(x, barRect.top * scaleTop, x, barRect.bottom * scaleBottom, markerPaint);

      String label = markerLabels.get(i);
      markerPaint.getTextBounds(label, 0, label.length(), bounds);

      // [Labels]
      canvas.drawText(label, x - bounds.centerX(), barRect.bottom + bounds.height() + padding * 2,
          markerPaint);
    }
  }

  private void drawFocusMarker(Canvas canvas) {
    float rate = (float) barRect.width() / getDelta();
    float x = barRect.left + ((progress - (1f / 2f)) * rate);

    Path path = new Path();
    path.moveTo(x, barRect.bottom - 5f);
    path.lineTo(x - 15f, barRect.bottom + 20f);
    path.lineTo(x + 15f, barRect.bottom + 20f);
    path.close();
    canvas.drawPath(path, focusMarkerPaint);

    //drawVerticalLine(canvas, x);
  }

  private void drawVerticalLine(Canvas canvas, float x) {
    // [Line]
    canvas.drawLine(x, barRect.top * 0.85f, x, barRect.bottom * 1.15f, focusMarkerPaint);

    String label = Integer.toString(progress) + " L";
    markerPaint.getTextBounds(label, 0, label.length(), bounds);

    // [Labels]
    canvas.drawText(label, x - bounds.centerX(), barRect.top - bounds.height() - padding,
        focusMarkerPaint);
  }

  public void setMin(int min) {
    this.min = min;
  }

  public int getMin() {
    return min;
  }

  public void setProgress(int progress) {
    if (progress > max) {
      progress = max;
    } else if (progress < min) {
      progress = min;
    }

    this.progress = progress;
  }

  public int getProgress() {
    return progress;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public int getMax() {
    return max;
  }

  public int getDelta() {
    return max - min + 1;
  }

  private static class SeekBarGestureDetector extends GestureDetector.SimpleOnGestureListener {

    private final FancySeekBar instance;

    SeekBarGestureDetector(FancySeekBar reference) {
      WeakReference<FancySeekBar> weakReference = new WeakReference<FancySeekBar>(reference);
      instance = weakReference.get();
    }

    @Override public boolean onDown(MotionEvent e) {
      return true;
    }

    @Override public boolean onSingleTapConfirmed(MotionEvent e) {
      if (instance.barRect != null) {
        int x = (int) (e.getX() - instance.barRect.left);
        int value = Math.round((float) (x * instance.getDelta() / instance.barRect.width()));
        instance.setProgress(value);
        instance.invalidate();
      }

      return super.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      if (instance.barRect != null) {
        int x = (int) (e2.getX() - instance.barRect.left);
        int value = Math.round((float) (x * instance.getDelta() / instance.barRect.width()));
        instance.setProgress(value);
        instance.invalidate();
      }

      return super.onScroll(e1, e2, distanceX, distanceY);
    }
  }
}
