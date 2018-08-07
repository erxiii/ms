package com.erxi.ms.util;

import java.util.UUID;

public class UUIDUtil {
	public static String Uuid(){
		return UUID.randomUUID().toString().replace("-", "");
	}
}
