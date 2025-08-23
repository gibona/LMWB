package com.anobig.lmwb.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Loading image from the web with Async task
 */
public class WebImageUtil {

	public interface ImageContainer {
		void onLoaded(Bitmap bitmap);
		void onError(Exception exception);
	}

	public static void loadDrawable(String profilePicURL, WebImageUtil.ImageContainer imageContainer) {
		new AsyncTask<String, Exception, Bitmap>() {

			@Override
			protected Bitmap doInBackground(String... strings) {
				HttpURLConnection connection = null;
				try {
					connection = (HttpURLConnection) new URL(profilePicURL).openConnection();
					connection.connect();
					InputStream input = connection.getInputStream();
					Bitmap bitmap = BitmapFactory.decodeStream(input);
					return bitmap;
				} catch (Exception e) {
					publishProgress(e);
				}
				return null;
			}

			/**
			 * Progress is used for error handling
			 * @param values
			 */
			@Override
			protected void onProgressUpdate(Exception... values) {
				super.onProgressUpdate(values);
				imageContainer.onError(values[0]);
			}

			@Override
			protected void onPostExecute(Bitmap bitmap) {
				super.onPostExecute(bitmap);
				imageContainer.onLoaded(bitmap);
			}
		}.execute();
	}
}
