package com.anobig.lmwb.basic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class User {
	@NonNull
	private final String name;
	@Nullable
	private final String profilePictureURL;

	public User(String name) {
		this(name, null);
	}

	public User(@NonNull String name, @Nullable String profilePictureURL) {
		this.name = name;
		this.profilePictureURL = profilePictureURL;
	}

	@NonNull
	public String getName() {
		return name;
	}

	@Nullable
	public String getProfilePictureURL() {
		return profilePictureURL;
	}
}
