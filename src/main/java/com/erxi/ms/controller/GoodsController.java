package com.erxi.ms.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import com.erxi.ms.domain.MsUser;
import com.erxi.ms.redis.GoodsKey;
import com.erxi.ms.redis.RedisService;
import com.erxi.ms.result.Result;
import com.erxi.ms.service.GoodsService;
import com.erxi.ms.service.MsUserService;
import com.erxi.ms.vo.GoodsDetailVo;
import com.erxi.ms.vo.GoodsVo;

@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	MsUserService userService;

	@Autowired
	GoodsService goodsService;

	@Autowired
	RedisService redisService;

	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;

	@Autowired
	ApplicationContext applicationContext;

	/**
	 * 页面缓存
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param msUser
	 * @return
	 */
	@RequestMapping(value = "/to_list", produces = "text/html")
	@ResponseBody
	public String tolist(HttpServletRequest request,
			HttpServletResponse response, Model model, MsUser msUser) {

		model.addAttribute("user", msUser);
		// 取缓存
		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
		if (!StringUtils.isEmpty(html)) {
			return html;
		}

		// 查询商品列表
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
		// return "goods_list";

		SpringWebContext swc = new SpringWebContext(request, response,
				request.getServletContext(), request.getLocale(),
				model.asMap(), applicationContext);

		// 手动渲染
		html = thymeleafViewResolver.getTemplateEngine().process("goods_list",
				swc);
		if (!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsList, "", html);
		}
		return html;
	}

	/**
	 * url 缓存
	 * 
	 * @param model
	 * @param msUser
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value = "/to_detail2/{goodsId}", produces = "text/html")
	@ResponseBody
	public String todetail2(HttpServletRequest request,
			HttpServletResponse response, Model model, MsUser msUser,
			@PathVariable("goodsId") long goodsId) {
		model.addAttribute("user", msUser);

		// 取缓存
		String html = redisService.get(GoodsKey.getGoodsDetail, "" + goodsId,
				String.class);
		if (!StringUtils.isEmpty(html)) {
			return html;
		}

		// snowflake
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goodsVo);

		long startAt = goodsVo.getStartDate().getTime();
		long endAt = goodsVo.getEndDate().getTime();
		long now = System.currentTimeMillis();

		int miaoshaStatus = 0;
		int remainSeconds = 0;
		if (now < startAt) {// 秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int) ((startAt - now) / 1000);
		} else if (now > endAt) {// 秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		} else {// 秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		model.addAttribute("miaoshaStatus", miaoshaStatus);
		model.addAttribute("remainSeconds", remainSeconds);

		// return "goods_detail";

		SpringWebContext swc = new SpringWebContext(request, response,
				request.getServletContext(), request.getLocale(),
				model.asMap(), applicationContext);

		// 手动渲染
		html = thymeleafViewResolver.getTemplateEngine().process(
				"goods_detail", swc);
		if (!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, html);
		}
		return html;
	}

	@RequestMapping(value = "/detail/{goodsId}")
	@ResponseBody
	public Result<GoodsDetailVo> detail(HttpServletRequest request,
			HttpServletResponse response, Model model, MsUser msUser,
			@PathVariable("goodsId") long goodsId) {

		// snowflake
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);

		long startAt = goodsVo.getStartDate().getTime();
		long endAt = goodsVo.getEndDate().getTime();
		long now = System.currentTimeMillis();

		int miaoshaStatus = 0;
		int remainSeconds = 0;
		if (now < startAt) {// 秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int) ((startAt - now) / 1000);
		} else if (now > endAt) {// 秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		} else {// 秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}

		GoodsDetailVo vo = new GoodsDetailVo();
		vo.setGoodsVo(goodsVo);
		vo.setMsUser(msUser);
		vo.setRemainSeconds(remainSeconds);
		vo.setMiaoshaStatus(miaoshaStatus);

		return Result.success(vo);
	}
}
