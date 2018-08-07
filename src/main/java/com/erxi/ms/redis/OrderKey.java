package com.erxi.ms.redis;

public class OrderKey extends BasePrefix{

	private OrderKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	
	public static OrderKey getMsOrderByUidGid=new OrderKey(0,"moug");
}
