package com.anobig.lmwb.utility;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class ProxyDrawable extends Drawable {

	private Drawable proxy;
	private boolean mutated;

	public ProxyDrawable(Drawable target) {
		proxy = target;
	}

	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		super.setBounds(left, top, right, bottom);
		if(proxy != null) {
			proxy.setBounds(left, top, right, bottom);
		}
	}

	@Override
	public void setBounds(Rect bounds) {
		super.setBounds(bounds);
		if(proxy != null) {
			proxy.setBounds(bounds);
		}
	}

	public void setProxy(Drawable proxy) {
		if (proxy != this) {
			Rect bounds = getBounds();
			this.proxy = proxy;
			if(this.proxy != null) {
				this.proxy.setBounds(bounds);
			}
			invalidateSelf();
		}
	}

	@Override
	public void draw(Canvas canvas) {
		if (proxy != null) {
			proxy.draw(canvas);
		}
	}

	@Override
	public int getIntrinsicWidth() {
		return proxy != null ? proxy.getIntrinsicWidth() : -1;
	}

	@Override
	public int getIntrinsicHeight() {
		return proxy != null ? proxy.getIntrinsicHeight() : -1;
	}

	@Override
	public int getOpacity() {
		return proxy != null ? proxy.getOpacity() : PixelFormat.TRANSPARENT;
	}

	@Override
	public void setFilterBitmap(boolean filter) {
		if (proxy != null) {
			proxy.setFilterBitmap(filter);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setDither(boolean dither) {
		if (proxy != null) {
			proxy.setDither(dither);
		}
	}

	@Override
	public void setColorFilter(ColorFilter colorFilter) {
		if (proxy != null) {
			proxy.setColorFilter(colorFilter);
		}
	}

	@Override
	public void setAlpha(int alpha) {
		if (proxy != null) {
			proxy.setAlpha(alpha);
		}
	}

	@Override
	public Drawable mutate() {
		if (proxy != null && !mutated && super.mutate() == this) {
			proxy.mutate();
			mutated = true;
		}
		return this;
	}

	@Override
	public void invalidateSelf() {
		super.invalidateSelf();
		if(proxy != null)
			proxy.invalidateSelf();
	}
}
