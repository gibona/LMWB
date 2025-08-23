package com.anobig.lmwb.basic;

import androidx.annotation.Nullable;

import static com.anobig.lmwb.basic.SessionPreferences.USER_NAME;
import static com.anobig.lmwb.basic.SessionPreferences.USER_PROFILE_PICTURE;

public class UserSessionManager {
	// The basic way of creating a SINGLETON object
	private static final UserSessionManager INSTANCE = new UserSessionManager();


	@Nullable
	private User currentUser = null;
	private volatile boolean userLoaded = false;

	private UserSessionManager() {
	}

	public static UserSessionManager get() {
		return INSTANCE;
	}

	public void logoutUser() {
		currentUser = null;

		SessionPreferences.get().edit()
				.putString(USER_NAME, null)
				.putString(USER_PROFILE_PICTURE, null)
				.apply();

	}

	public void logUser(User user) {
		currentUser = user;

		SessionPreferences.get().edit()
				.putString(USER_NAME, user.getName())
				.putString(USER_PROFILE_PICTURE, user.getProfilePictureURL())
				.apply();
	}

	@Nullable
	public User getUser() {
		if (!userLoaded) { // load user once
			synchronized (UserSessionManager.class) {
				currentUser = loadUser();
				userLoaded = true;
			}
		}
		return currentUser;
	}

	@Nullable
	private User loadUser() {
		String userName = SessionPreferences.get().getString(USER_NAME, null);
		if (userName == null)
			return null;

		String profilePictureURL = SessionPreferences.get().getString(USER_PROFILE_PICTURE, null);

		return new User(userName, profilePictureURL);
	}


}
