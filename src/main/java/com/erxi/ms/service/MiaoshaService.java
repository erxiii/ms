package com.erxi.ms.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erxi.ms.domain.MsOrder;
import com.erxi.ms.domain.MsUser;
import com.erxi.ms.domain.OrderInfo;
import com.erxi.ms.redis.MsKey;
import com.erxi.ms.redis.RedisService;
import com.erxi.ms.util.Md5Util;
import com.erxi.ms.util.UUIDUtil;
import com.erxi.ms.vo.GoodsVo;

@Service
public class MiaoshaService {

	@Autowired
	OrderService orderService;

	@Autowired
	GoodsService goodsService;

	@Autowired
	RedisService redisService;

	@Transactional
	public OrderInfo miaosha(MsUser msUser, GoodsVo goodsVo) {
		// 减库存 下订单 写入秒杀操作
		boolean success = goodsService.reduceStock(goodsVo);
		if (success) {
			// order_info miaosha_order插数据
			return orderService.creatOrder(msUser, goodsVo);
		} else {
			setGoodsOver(goodsVo.getId());
			return null;
		}
	}

	public long getMiaoshaResult(Long userId, long goodsId) {
		MsOrder msOrder = orderService.getMiaoshaOrderByUserIdGoodsId(userId,
				goodsId);
		if (msOrder != null) {
			return msOrder.getOrderId();
		} else {
			boolean isOver = getGoodsOver(goodsId);
			if (isOver) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	private void setGoodsOver(Long goodsId) {
		redisService.set(MsKey.isGoodsOver, "" + goodsId, true);
	}

	private boolean getGoodsOver(long goodsId) {
		return redisService.exist(MsKey.isGoodsOver, "" + goodsId);
	}

	public void reset(List<GoodsVo> goodsList) {
		goodsService.resetStock(goodsList);
		orderService.deleteOrders();
	}

	public boolean checkPath(MsUser msUser, long goodsId, String path) {
		if (msUser == null || path == null) {
			return false;
		}
		String pathOld = redisService.get(MsKey.getMiaoshaPath,
				"" + msUser.getId() + "_" + goodsId, String.class);
		return pathOld.equals(path);
	}

	public String createPath(MsUser msUser, long goodsId) {
		if (msUser == null || goodsId < 0) {
			return null;
		}
		String str = Md5Util.md5(UUIDUtil.Uuid() + "123456");
		redisService.set(MsKey.getMiaoshaPath, "" + msUser.getId() + "_"
				+ goodsId, str);
		return str;
	}

	public BufferedImage createMiaoshaVerifyCode(MsUser msUser, long goodsId) {
		if (msUser == null || goodsId < 0) {
			return null;
		}
		int width = 80;
		int height = 32;
		// create the image
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		// set the background color
		g.setColor(new Color(0xDCDCDC));
		g.fillRect(0, 0, width, height);
		// draw the border
		g.setColor(Color.black);
		g.drawRect(0, 0, width - 1, height - 1);
		// create a random instance to generate the codes
		Random rdm = new Random();
		// make some confusion
		for (int i = 0; i < 50; i++) {
			int x = rdm.nextInt(width);
			int y = rdm.nextInt(height);
			g.drawOval(x, y, 0, 0);
		}
		// generate a random code
		String verifyCode = generateVerifyCode(rdm);
		g.setColor(new Color(0, 100, 0));
		g.setFont(new Font("Candara", Font.BOLD, 24));
		g.drawString(verifyCode, 8, 24);
		g.dispose();
		// 把验证码存到redis中
		int rnd = calc(verifyCode);
		redisService.set(MsKey.getMiaoshaVerifyCode, msUser.getId() + ","
				+ goodsId, rnd);
		// 输出图片
		return image;
	}

	/**
	 * 计算表达式的结果
	 * 
	 * @param verifyCode
	 * @return
	 */
	private static int calc(String verifyCode) {
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			System.out.println(Integer.parseInt(engine.eval(verifyCode).toString().split("\\.")[0]));
			return Integer.parseInt(engine.eval(verifyCode).toString().split("\\.")[0]);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 计算符号
	 */
	private static char[] ops = new char[] { '+', '-', '*' };

	/**
	 * 随机生成计算公式
	 * 
	 * @param rdm
	 * @return
	 */
	private static String generateVerifyCode(Random rdm) {
		int num1 = rdm.nextInt(10);
		int num2 = rdm.nextInt(10);
		int num3 = rdm.nextInt(10);
		char op1 = ops[rdm.nextInt(3)];
		char op2 = ops[rdm.nextInt(3)];
		String exp = "" + num1 + op1 + num2 + op2 + num3;
		return exp;
	}

	public boolean checkVerifyCode(MsUser msUser, long goodsId, int verifyCode) {
		if (msUser == null || goodsId < 0) {
			return false;
		}
		Integer code = redisService.get(MsKey.getMiaoshaVerifyCode,
				msUser.getId() + ","				+ goodsId, Integer.class);
		if(code == null || code-verifyCode !=0){
			return false;
		}
		redisService.delete(MsKey.getMiaoshaVerifyCode, msUser.getId() + ","
				+ goodsId);
		return true;
	}
}
