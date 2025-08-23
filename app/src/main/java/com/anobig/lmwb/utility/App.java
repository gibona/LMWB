package com.anobig.lmwb.utility;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Gravity;
import android.widget.Toast;

import com.anobig.lmwb.R;

public class App extends Application {

	@SuppressLint("StaticFieldLeak")
	private static App inst;

	public App() {
		if (inst == null)
			inst = this;
	}

	public static App get() {
		return inst;
	}

	public static float dpToPxFloat(float dp) {
		final float scale = Resources.getSystem().getDisplayMetrics().density;
		return (dp * scale);
	}

	public static int dpToPx(float dp) {
		// Is for positive values as written, negative should add -0.5f, see TypedValue.complexToDimensionPixelSize().
		// It's not known that negative values are ever used.
		return (int) (dpToPxFloat(dp) + 0.5f);
	}

	@Override
	public void startActivity(Intent intent) {
		try {
			super.startActivity(intent);
		}
		catch (ActivityNotFoundException e) {
			Toast toast = Toast.makeText(App.get(), R.string.noApplications , Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
