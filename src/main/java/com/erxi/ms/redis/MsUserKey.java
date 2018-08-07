package com.erxi.ms.redis;

public class MsUserKey extends BasePrefix {

	public static final int TOKEN_EXPIRE = 3600 * 24 * 2;

	private MsUserKey(String prefix) {
		super(prefix);
	}

	private MsUserKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

	public static MsUserKey token = new MsUserKey(TOKEN_EXPIRE, "tk");
	public static MsUserKey getById = new MsUserKey(0, "id");
}
