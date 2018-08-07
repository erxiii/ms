package com.erxi.ms.config;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.erxi.ms.access.UserContext;
import com.erxi.ms.domain.MsUser;

/**
 * 统一处理修改token的获取方式 通过mvc参数是否有 MsUser对象
 * 
 * @author erxi
 * @date : 2018年6月19日 上午9:52:48
 */
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

	// @Autowired
	// MsUserService msUserService;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz == MsUser.class;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) throws Exception {
//		HttpServletRequest request = webRequest
//				.getNativeRequest(HttpServletRequest.class);
//		HttpServletResponse response = webRequest
//				.getNativeResponse(HttpServletResponse.class);
//
//		String paramToken = request
//				.getParameter(MsUserService.COOKIE_NAME_TOKEN);
//		String cookieToken = getCookieValue(request,
//				MsUserService.COOKIE_NAME_TOKEN);
//		if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
//			return null;
//		}
//		String token = StringUtils.isEmpty(paramToken) ? cookieToken
//				: paramToken;
//		return msUserService.getByToken(response, token);
		return UserContext.getUser();
	}

//	private String getCookieValue(HttpServletRequest request,
//			String cookieNameToken) {
//		Cookie[] cookies = request.getCookies();
//		if (cookies == null || cookies.length <= 0) {
//			return null;
//		}
//		for (Cookie cookie : cookies) {
//			if (cookie.getName().equals(cookieNameToken)) {
//				return cookie.getValue();
//			}
//		}
//		return null;
//	}

}
