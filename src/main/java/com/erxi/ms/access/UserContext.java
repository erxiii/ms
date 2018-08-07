package com.erxi.ms.access;

import com.erxi.ms.domain.MsUser;

public class UserContext {
	private static ThreadLocal<MsUser> LocalUser = new ThreadLocal<MsUser>();

	public static void setUser(MsUser user) {
		LocalUser.set(user);
	}

	public static MsUser getUser() {
		return LocalUser.get();
	}

}
