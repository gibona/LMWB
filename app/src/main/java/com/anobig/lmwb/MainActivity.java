package com.anobig.lmwb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.http.NetworkException;
import android.os.Bundle;

import com.anobig.lmwb.basic.User;
import com.anobig.lmwb.basic.UserSessionManager;
import com.anobig.lmwb.bluetooth.BluetoothDeviceDetailHostActivity;
import com.anobig.lmwb.utility.App;
import com.anobig.lmwb.utility.ProxyDrawable;
import com.anobig.lmwb.utility.RoundDrawable;
import com.anobig.lmwb.utility.WebImageUtil;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.navigation.ui.AppBarConfiguration;

import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

	private AppBarConfiguration appBarConfiguration;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		setSupportActionBar(findViewById(R.id.toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// FAB button binding
		findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String message = App.get().getString(R.string.already_logged_in);
				if (UserSessionManager.get().getUser() == null) {
					User user = new User("Aleksandar", "https://anobig.com/me_2025.jpg");
					UserSessionManager.get().logUser(user);

					message = App.get().getString(R.string.welcome, user.getName());
					updateProfilePic();
				}

				Snackbar.make(view, message, Snackbar.LENGTH_LONG)
						.setAnchorView(R.id.fab)
						.show();


			}
		});

		// Web card binding
		findViewById(R.id.web).setOnClickListener(view ->
				App.get().startActivity(MainActivity.this, new Intent(App.get(), WebView.class))
		);

		// Bluetooth card binding
		findViewById(R.id.bluetooth).setOnClickListener(view ->
				App.get().startActivity(MainActivity.this, new Intent(App.get(), BluetoothDeviceDetailHostActivity.class))
		);

		// Updates the profile picture if logged in
		updateProfilePic();
	}

	private void updateProfilePic() {
		String profilePic = null;
		User user = UserSessionManager.get().getUser();
		if (user != null)
			profilePic = user.getProfilePictureURL();

		Drawable defaultDrawable = AppCompatResources.getDrawable(this, R.drawable.account_circle_24px);
		final ProxyDrawable proxyDrawable = new ProxyDrawable(defaultDrawable);

		if (profilePic == null) {
			proxyDrawable.setProxy(defaultDrawable);
		}
		else {
			WebImageUtil.loadDrawable(profilePic, new WebImageUtil.ImageContainer() {
				@Override
				public void onLoaded(Bitmap bitmap) {
					if (bitmap == null)
						return;
					proxyDrawable.setProxy(new RoundDrawable(bitmap, defaultDrawable));
				}

				@Override
				public void onError(Exception exception) {
					// Show user friendly error with option to open Android settings and enable internet connection
					if (exception instanceof IOException) {
						Snackbar.make(findViewById(R.id.fab), R.string.no_internet, Snackbar.LENGTH_LONG)
								.setAnchorView(R.id.fab)
								.setAction(R.string.internet_settings, view -> {
									Intent wiFiSettings = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
									wiFiSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									App.get().startActivity(wiFiSettings);
								})
								.show();
					}
				}
			});
		}

		getSupportActionBar().setHomeAsUpIndicator(proxyDrawable);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Updates the profile picture if logged in or network error
		updateProfilePic();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Adds logout menu item
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		//Handle logout
		if (id == R.id.action_logout) {
			String message = App.get().getString(R.string.already_logged_out);
			User oldUser = UserSessionManager.get().getUser();
			if (oldUser != null) {
				UserSessionManager.get().logoutUser();
				message = App.get().getString(R.string.good_bye, oldUser.getName());
			}

			Snackbar.make(findViewById(R.id.fab), message, Snackbar.LENGTH_LONG)
					.setAnchorView(R.id.fab)
					.show();

			updateProfilePic();

			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}