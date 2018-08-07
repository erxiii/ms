package com.erxi.ms.redis;

public interface KeyPrefix {
	public int expireSeconds();

	public String getPrefix();
}
