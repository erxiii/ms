package com.erxi.ms.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class ValidatorUtil {
	private static final Pattern MOBILE_PATTERN = Pattern.compile("1\\d{10}");

	public static boolean isMobile(String str) {
		if (StringUtils.isEmpty(str)) {
			return false;
		}
		Matcher matcher = MOBILE_PATTERN.matcher(str);
		return matcher.matches();
	}

//	public static void main(String[] args) {
//		System.out.println(isMobile("18912341234"));
//		 System.out.println(isMobile("1891234123"));
//	}
}
