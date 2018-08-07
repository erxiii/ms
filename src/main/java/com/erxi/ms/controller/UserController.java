package com.erxi.ms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erxi.ms.domain.MsUser;
import com.erxi.ms.result.Result;

@Controller
@RequestMapping("/user")
public class UserController {

	@RequestMapping("/info")
	@ResponseBody
	public Result<MsUser> info(Model model, MsUser msUser) {
		return Result.success(msUser);
	}

//	@RequestMapping("/test")
//	public String toLogin() {
//		return "test";
//	}
//	
//	@RequestMapping("/pic")
//	public void testpic(HttpServletResponse response,
//			HttpServletResponse request, @RequestParam("Id") String id,@RequestParam("type")String type)
//			throws IOException {
//		FileInputStream fis = null;
//		String path = "D://";
//		System.out.println("path:"+path+id+".png");
//		File file = new File(path+id+".png");
//		// File file = new File("home/images/test.png"); 服务器目录和本地图片的区别是图片路径
//		fis = new FileInputStream(file);
//		response.setContentType("image/jpg"); // 设置返回的文件类型
//		response.setHeader("Access-Control-Allow-Origin", "*");// 设置该图片允许跨域访问
//		IOUtils.copy(fis, response.getOutputStream());
//	}

}
