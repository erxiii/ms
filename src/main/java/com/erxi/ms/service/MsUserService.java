package com.erxi.ms.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erxi.ms.dao.MsUserDao;
import com.erxi.ms.domain.MsUser;
import com.erxi.ms.exception.GlobalException;
import com.erxi.ms.redis.MsUserKey;
import com.erxi.ms.redis.RedisService;
import com.erxi.ms.result.CodeMsg;
import com.erxi.ms.util.Md5Util;
import com.erxi.ms.util.UUIDUtil;
import com.erxi.ms.vo.LoginVo;

@Service
public class MsUserService {
	private static Logger log = LoggerFactory.getLogger(MsUserService.class);

	public static final String COOKIE_NAME_TOKEN = "token";

	@Autowired
	MsUserDao msUserDao;

	@Autowired
	RedisService redisService;

	public MsUser getById(long mobile) {
		return msUserDao.getById(mobile);
	}

	public String login(HttpServletRequest request,
			HttpServletResponse response, LoginVo loginVo) {
		if (loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();

		// 判断手机好是否存在
		MsUser msUser = getById(Long.parseLong(mobile));
		if (msUser == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}

		// 验证密码
		String PassDB = msUser.getPassword();
		// String saltDB = msUser.getSalt();
		// String calPass = Md5Util.formPassToDBPass(formPass, saltDB);
		if (!formPass.equals(PassDB)) {
			log.info("mm error");
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		String token = UUIDUtil.Uuid();
		addCookie(response, token, msUser);
		return token;
	}

	/**
	 * 热点数据 对象缓存
	 * 
	 * // http://blog.csdn.net/tTU1EvLDeLFq5btqiK/article/details/78693323
	 * 
	 * @param token
	 * @param id
	 * @param formPass
	 * @return
	 */
	public boolean updatePassword(String token, long id, String formPass) {
		// 取user
		MsUser user = getById(id);
		if (user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		// 更新数据库
		MsUser toBeUpdate = new MsUser();
		toBeUpdate.setId(id);
		toBeUpdate.setPassword(Md5Util.formPassToDBPass(formPass,
				user.getSalt()));
		msUserDao.update(toBeUpdate);
		// 处理缓存
		redisService.delete(MsUserKey.getById, "" + id);
		user.setPassword(toBeUpdate.getPassword());
		redisService.set(MsUserKey.token, token, user);
		return true;
	}

	public MsUser getByToken(HttpServletResponse response, String token) {
		if (StringUtils.isEmpty(token)) {
			return null;
		}
		MsUser msUser = redisService.get(MsUserKey.token, token, MsUser.class);
		// 延长有效期
		if (msUser != null) {
			addCookie(response, token, msUser);
		}
		return msUser;
	}

	private void addCookie(HttpServletResponse response, String token,
			MsUser msUser) {
		// 生成cookie
		// log.info(token);
		redisService.set(MsUserKey.token, token, msUser);
		Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
		cookie.setMaxAge(MsUserKey.token.expireSeconds());
		cookie.setPath("/");
		response.addCookie(cookie);
	}

}
