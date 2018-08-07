package com.erxi.ms.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erxi.ms.result.Result;
import com.erxi.ms.service.MsUserService;
import com.erxi.ms.vo.LoginVo;

@Controller
@RequestMapping("/login")
public class LoginController {
	private static Logger log = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	MsUserService msUserService;

	@RequestMapping("/to_login")
	public String toLogin() {
		return "login";
	}

	@RequestMapping("/do_login")
	@ResponseBody
	public Result<Boolean> doLogin(HttpServletRequest request,
			HttpServletResponse response, @Valid LoginVo loginVo) {
		log.info(loginVo.toString());
		// 登录
		msUserService.login(request, response, loginVo);
		return Result.success(true);
	}
}
