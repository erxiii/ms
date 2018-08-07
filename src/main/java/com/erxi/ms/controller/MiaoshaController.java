package com.erxi.ms.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erxi.ms.access.AccessLimit;
import com.erxi.ms.domain.MsOrder;
import com.erxi.ms.domain.MsUser;
import com.erxi.ms.rabbitmq.MQSender;
import com.erxi.ms.rabbitmq.MiaoShaMessage;
import com.erxi.ms.redis.GoodsKey;
import com.erxi.ms.redis.MsKey;
import com.erxi.ms.redis.OrderKey;
import com.erxi.ms.redis.RedisService;
import com.erxi.ms.result.CodeMsg;
import com.erxi.ms.result.Result;
import com.erxi.ms.service.GoodsService;
import com.erxi.ms.service.MiaoshaService;
import com.erxi.ms.service.MsUserService;
import com.erxi.ms.service.OrderService;
import com.erxi.ms.vo.GoodsVo;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {
	private static Logger log = LoggerFactory.getLogger(LoginController.class);
	@Autowired
	MsUserService userService;

	@Autowired
	GoodsService goodsService;

	@Autowired
	MiaoshaService miaoshaService;

	@Autowired
	OrderService orderService;

	@Autowired
	RedisService redisService;

	@Autowired
	MQSender sender;

	private HashMap<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

	/**
	 * 系统初始化
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodslList = goodsService.listGoodsVo();
		if (goodslList == null) {
			return;
		}
		for (GoodsVo goodsVo : goodslList) {
			redisService.set(GoodsKey.getMsGoodsStock, "" + goodsVo.getId(),
					goodsVo.getStockCount());

			localOverMap.put(goodsVo.getId(), false);
		}
	}

	@RequestMapping(value = "/reset", method = RequestMethod.GET)
	@ResponseBody
	public Result<Boolean> reset(Model model) {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		for (GoodsVo goods : goodsList) {
			goods.setStockCount(5);
			redisService.set(GoodsKey.getMsGoodsStock, "" + goods.getId(), 5);
			localOverMap.put(goods.getId(), false);
		}
		redisService.delete(OrderKey.getMsOrderByUidGid);
		redisService.delete(MsKey.isGoodsOver);
		miaoshaService.reset(goodsList);
		return Result.success(true);
	}

	/**
	 * GET POST
	 * 
	 * 1 传参 多少 2 GET幂等 POST 服务端数据产生影响
	 * 
	 * @param model
	 * @param msUser
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
	@ResponseBody
	public Result<Integer> tolist(Model model, MsUser msUser,
			@RequestParam("goodsId") long goodsId,
			@PathVariable("path") String path) {
		model.addAttribute("user", msUser);
		if (msUser == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		// 验证path
		boolean check = miaoshaService.checkPath(msUser, goodsId, path);
		if (!check) {
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}
		// 内存标记，减少redis访问
		boolean over = localOverMap.get(goodsId);
		if (over) {
			return Result.error(CodeMsg.MIAOSHA_OVER);
		}

		// 预减库存
		long stock = redisService.decr(GoodsKey.getMsGoodsStock, "" + goodsId);
		if (stock < 0) {
			localOverMap.put(goodsId, true);
			return Result.error(CodeMsg.MIAOSHA_OVER);
		}
		// 判断是否已经秒杀
		MsOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(
				msUser.getId(), goodsId);
		if (order != null) {
			return Result.error(CodeMsg.MIAOSHA_REPEAT);
		}
		// 入队
		MiaoShaMessage mm = new MiaoShaMessage();
		mm.setGoodsId(goodsId);
		mm.setMsUser(msUser);
		sender.sendMiaoshaMessage(mm);
		return Result.success(0);// 排队中

		// // 判断库存
		// GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
		// int stock = goodsVo.getGoodsStock();
		// if (stock <= 0) {
		// return Result.error(CodeMsg.MIAOSHA_OVER);
		// }
		// // 判断是否已经秒杀
		// MsOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(
		// msUser.getId(), goodsId);
		// if (order != null) {
		// return Result.error(CodeMsg.MIAOSHA_REPEAT);
		// }
		// // 减库存 下订单 写入秒杀订单 --- 事务操作
		// OrderInfo orderInfo = miaoshaService.miaosha(msUser, goodsVo);
		// log.info(orderInfo.toString());
		// return Result.success(orderInfo);
	}

	/**
	 * orderId:成功 -1:秒杀失败 0:排队中
	 * 
	 * @param model
	 * @param msUser
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value = "/result", method = RequestMethod.GET)
	@ResponseBody
	public Result<Long> result(Model model, MsUser msUser,
			@RequestParam("goodsId") long goodsId) {
		model.addAttribute("user", msUser);
		if (msUser == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}

		long result = miaoshaService.getMiaoshaResult(msUser.getId(), goodsId);
		return Result.success(result);
	}

	/**
	 * 隐藏秒杀地址
	 * 
	 * @AccessLimit 限流 5秒 最大连接5次 需要登录
	 * 
	 * @param model
	 * @param msUser
	 * @param goodsId
	 * @return
	 */
	@AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
	@RequestMapping(value = "/path", method = RequestMethod.GET)
	@ResponseBody
	public Result<String> path(Model model, MsUser msUser,
			@RequestParam("goodsId") long goodsId,
//			@RequestParam("verifyCode") int verifyCode) {
			
			@RequestParam(value="verifyCode",defaultValue="0") int verifyCode) {

		model.addAttribute("user", msUser);
		if (msUser == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}

		// 验证
		boolean check = miaoshaService.checkVerifyCode(msUser, goodsId,
				verifyCode);
		if (!check) {
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}
		String path = miaoshaService.createPath(msUser, goodsId);
		return Result.success(path);
	}

	/**
	 * 图片验证码
	 * 
	 * @param model
	 * @param msUser
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
	@ResponseBody
	public Result<String> verifyCode(HttpServletResponse response,
			MsUser msUser, @RequestParam("goodsId") long goodsId) {
		if (msUser == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		try {
//			log.info("1");
			BufferedImage image = miaoshaService.createMiaoshaVerifyCode(
					msUser, goodsId);
//			log.info("2");
			OutputStream out = response.getOutputStream();
			ImageIO.write(image, "JPEG", out);
			out.flush();
			out.close();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(CodeMsg.MIAOSHA_FAIL);
		}
	}

}
