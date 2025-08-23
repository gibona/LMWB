package com.anobig.lmwb.basic;

import android.content.Context;
import android.content.SharedPreferences;

import com.anobig.lmwb.utility.App;

class SessionPreferences {
	private static final String PREF_NAME = "user";
	static final String USER_NAME = "name";
	static final String USER_PROFILE_PICTURE = "profile_picture";


	private static volatile SharedPreferences preferences = null;

	static SharedPreferences get() {
		if (preferences == null)
			synchronized (UserSessionManager.class) {
				if (preferences == null)
					preferences = App.get().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
			}

		return preferences;
	}
}
