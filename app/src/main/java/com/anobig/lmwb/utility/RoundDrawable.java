package com.anobig.lmwb.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Utility drawable that makes the content round
 */
public class RoundDrawable extends Drawable {
	private final Bitmap bitmap;
	private int intrinsicWidth = App.dpToPx(75), intrinsicHeight = App.dpToPx(75);


	public RoundDrawable(Bitmap bitmap, Drawable intrinistic) {
		this(bitmap,intrinistic == null ? 0 :intrinistic.getIntrinsicWidth(), intrinistic == null ? 0 : intrinistic.getIntrinsicHeight());
	}
	public RoundDrawable(Bitmap bitmap, int intrinsicWidth, int intrinsicHeight) {
		this.bitmap = bitmap;
		this.intrinsicWidth = intrinsicWidth;
		this.intrinsicHeight = intrinsicHeight;
	}

    /*public RoundDrawable(Bitmap bitmap) {
        this.bitmap = bitmap;
    }*/

	@Override
	public void draw(Canvas canvas) {
		float nw, nh;

		int bitmapWidth = bitmap.getWidth();
		int bitmapHeight = bitmap.getHeight();

		if ((float) bitmapWidth / bitmapHeight > (float) getBounds().width() / getBounds().height()) {
			nh = getBounds().height();
			nw = bitmapWidth * nh / bitmapHeight;
		} else {
			nw = getBounds().width();
			nh = bitmapHeight * nw / bitmapWidth;
		}

		RectF dst = new RectF(
				getBounds().centerX() - nw / 2,
				getBounds().centerY() - nh / 2,
				getBounds().centerX() + nw / 2,
				getBounds().centerY() + nh / 2
		);

		RectF src = new RectF(
				0, 0, bitmapWidth, bitmapHeight
		);


		Matrix matrix = new Matrix();
		matrix.setPolyToPoly(
				new float[]{
						src.left, src.top, src.right, src.top, src.right, src.bottom, src.left, src.bottom
				}, 0,
				new float[]{
						dst.left, dst.top, dst.right, dst.top, dst.right, dst.bottom, dst.left, dst.bottom
				}, 0, 4
		);


		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
		paint.getShader().setLocalMatrix(matrix);
		canvas.drawCircle(getBounds().centerX(), getBounds().centerY(), Math.min(getBounds().width(), getBounds().height()) / 2, paint);
	}

	@Override
	public void setAlpha(int i) {

	}

	@Override
	public void setColorFilter(ColorFilter colorFilter) {

	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public int getIntrinsicWidth() {
		return intrinsicWidth;
	}

	@Override
	public int getIntrinsicHeight() {
		return intrinsicHeight;
	}
}
