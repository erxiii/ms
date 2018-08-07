package com.erxi.ms.redis;

public class MsKey extends BasePrefix {

	private MsKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

	public static MsKey isGoodsOver = new MsKey(0, "go");
	public static MsKey getMiaoshaPath = new MsKey(60, "mp");
	public static MsKey getMiaoshaVerifyCode = new MsKey(300, "mv");
}
