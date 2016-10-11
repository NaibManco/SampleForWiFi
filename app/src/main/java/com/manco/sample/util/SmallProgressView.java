package com.manco.sample.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @since 2016-04-11
 * @author Manco
 *
 */
public class SmallProgressView extends View implements Runnable {
	private static final float RATIO = 0.2f;
	Paint paint;

	int startColor;
	int endColor;

	RectF outRectF;
	RectF inRectF;

	boolean isRotate = false;
	boolean isRunning = false;
	ExecutorService executor;

	float width = 0;
	float height = 0;
	float centerX = 0;
	float centerY = 0;
	float outRadius = 0;
	float paintWidth = 0;
	float inRadius = 0;

	boolean isFirst = true;

	float rotateDegree = 0;

	public SmallProgressView(Context context) {
		this(context, null);
	}

	public SmallProgressView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SmallProgressView(Context context, AttributeSet attrs,
							 int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		outRectF = new RectF();
		inRectF = new RectF();

		startColor = Color.argb(0xff, 0x03, 0xa9, 0xf4);
		endColor = Color.argb(0xff, 0xeb, 0xeb, 0xeb);

		executor = Executors.newSingleThreadExecutor();
	}

	public void setStartColor(int color) {
		startColor = color;
	}

	public void setEndColor(int color) {
		endColor = color;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isFirst) {
			width = getWidth();
			height = getHeight();
			centerX = width / 2;
			centerY = height / 2;
			outRadius = width > height ? centerY : centerX;
			paintWidth = outRadius * RATIO;
			inRadius = outRadius - paintWidth;
			isFirst = false;
		}

		if (isRotate) {
			paint.setColor(endColor);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(paintWidth);

			canvas.drawCircle(centerX, centerY, inRadius, paint);

			canvas.rotate(rotateDegree, centerX, centerY);
			paint.setColor(startColor);

			outRectF.set(centerX - inRadius, centerY - inRadius, centerX
					+ inRadius, centerY + inRadius);
			canvas.drawArc(outRectF, 0, 120, false, paint);
		} else {
			paint.setColor(endColor);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(centerX, centerY, outRadius / 2, paint);
		}
	}

	public void startProgress() {
		isRotate = true;
		isRunning = true;
		if (null == executor || executor.isShutdown() ||executor.isTerminated()) {
			executor = Executors.newSingleThreadExecutor();
		}
		executor.execute(this);
	}

	public void stopProgress() {
		executor.shutdown();
		isRotate = false;
		isRunning = false;
		invalidate();
	}

	public boolean isProgress() {
		return isRunning & isRotate;
	}

	@Override
	public void run() {
		try {
			while (isRunning) {
				if (isRotate) {
					rotateDegree += 30;
					postInvalidate();
					Thread.sleep(100);
				} else {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
