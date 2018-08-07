package com.erxi.ms.access;

import java.io.OutputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.erxi.ms.domain.MsUser;
import com.erxi.ms.redis.AccessKey;
import com.erxi.ms.redis.RedisService;
import com.erxi.ms.result.CodeMsg;
import com.erxi.ms.service.MsUserService;

/**
 * 拦截器 同意处理user 接口限流处理
 * 
 * @author erxi
 * @date : 2018年7月3日 上午11:43:31
 */

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
	private static Logger log = LoggerFactory
			.getLogger(AccessInterceptor.class);

	@Autowired
	MsUserService msUserService;

	@Autowired
	RedisService redisService;

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			MsUser msUser = getUser(request, response);
			UserContext.setUser(msUser);

			HandlerMethod hm = (HandlerMethod) handler;
			AccessLimit al = hm.getMethodAnnotation(AccessLimit.class);
			if (al == null) {
				return true;
			}

			int seconds = al.seconds();
			int maxCount = al.maxCount();
			boolean needLogin = al.needLogin();
			String key = request.getRequestURI();

			if (needLogin) {
				if (msUser == null) {
					render(response, CodeMsg.SESSION_ERROR);
					return false;
				}
				key += "_" + msUser.getId();
			} else {
				// do nothing
			}
			// 查询访问次数
			AccessKey ak = AccessKey.withExpire(seconds);
			Integer count = redisService.get(ak, key, Integer.class);
			if (count == null) {
				redisService.set(ak, key, 1);
			} else if (count < maxCount) {
				redisService.incr(ak, key);
			} else {
				render(response, CodeMsg.ACCESS_LIMIT_REACHED);
				return false;
			}
		}
		return true;
	}

	private void render(HttpServletResponse response, CodeMsg cm)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		OutputStream out = response.getOutputStream();
		String s = JSON.toJSONString(cm);
		out.write(s.getBytes("UTF-8"));
		out.flush();
		out.close();
	}

	/**
	 * 获取user
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	private MsUser getUser(HttpServletRequest request,
			HttpServletResponse response) {
		String paramToken = request
				.getParameter(MsUserService.COOKIE_NAME_TOKEN);
		String cookieToken = getCookieValue(request,
				MsUserService.COOKIE_NAME_TOKEN);
		if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
			return null;
		}
		String token = StringUtils.isEmpty(paramToken) ? cookieToken
				: paramToken;
		return msUserService.getByToken(response, token);

	}

	private String getCookieValue(HttpServletRequest request,
			String cookieNameToken) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null || cookies.length <= 0) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(cookieNameToken)) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
