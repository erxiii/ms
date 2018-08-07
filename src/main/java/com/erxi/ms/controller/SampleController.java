package com.erxi.ms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erxi.ms.domain.User;
import com.erxi.ms.rabbitmq.MQSender;
import com.erxi.ms.redis.RedisService;
import com.erxi.ms.redis.UserKey;
import com.erxi.ms.result.CodeMsg;
import com.erxi.ms.result.Result;
import com.erxi.ms.service.UserService;

@Controller
@RequestMapping("/demo")
public class SampleController {
	@Autowired
	UserService userService;

	@Autowired
	RedisService redisService;

	@Autowired
	MQSender sender;

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello World!";
	}

	@RequestMapping("/hello")
	@ResponseBody
	Result<String> hello() {
		return Result.success("hello.erxi");
	}

	@RequestMapping("/error")
	@ResponseBody
	Result<String> error() {
		return Result.error(CodeMsg.SERVER_ERROR);
	}

	@RequestMapping("/thymeleaf")
	public String thymeleaf(Model model) {
		model.addAttribute("name", "erxi");
		return "hello";
	}

	@RequestMapping("/db")
	@ResponseBody
	Result<User> getDb() {
		User user = userService.getById(1);
		return Result.success(user);
	}

	@RequestMapping("/tx")
	@ResponseBody
	Result<Boolean> dbTx() {
		userService.tx();
		return Result.success(true);
	}

	@RequestMapping("/redis")
	@ResponseBody
	Result<User> getRedis() {
		User u1 = redisService.get(UserKey.getById, "erxi", User.class);
		return Result.success(u1);
	}

	@RequestMapping("/redis2")
	@ResponseBody
	Result<User> setRedis() {
		User u1 = new User();
		u1.setId(2);
		u1.setName("erxi");
		// boolean str = redisService.set(UserKey.getById,"erxi1",u1);
		User l1 = redisService.get(UserKey.getById, "erxi1", User.class);
		return Result.success(l1);
	}

//	@RequestMapping("/mq")
//	@ResponseBody
//	Result<String> mq() {
//		sender.send("hi erxi!");
//		return Result.success("hello.erxi");
//	}
//
//	@RequestMapping("/mq/header")
//	@ResponseBody
//	public Result<String> header() {
//		sender.sendHeader("hello,imooc");
//		return Result.success("Hello，world");
//	}
//
//	@RequestMapping("/mq/fanout")
//	@ResponseBody
//	public Result<String> fanout() {
//		sender.sendFanout("hello,imooc");
//		return Result.success("Hello，world");
//	}
//
//	@RequestMapping("/mq/topic")
//	@ResponseBody
//	public Result<String> topic() {
//		sender.sendTopic("hello,imooc");
//		return Result.success("Hello，world");
//	}
}
