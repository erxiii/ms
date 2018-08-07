package com.erxi.ms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erxi.ms.domain.MsUser;
import com.erxi.ms.domain.OrderInfo;
import com.erxi.ms.result.CodeMsg;
import com.erxi.ms.result.Result;
import com.erxi.ms.service.GoodsService;
import com.erxi.ms.service.OrderService;
import com.erxi.ms.vo.GoodsVo;
import com.erxi.ms.vo.OrderDetailVo;

@Controller
@RequestMapping("/order")
public class OrderController {

	@Autowired
	OrderService orderService;

	@Autowired
	GoodsService goodsService;

	@RequestMapping("/detail")
	@ResponseBody
//	@NeedLogin 拦截器
	public Result<OrderDetailVo> info(Model model,
			MsUser msUser, @RequestParam("orderId") long orderId) {
		if(msUser == null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		OrderInfo orderInfo = orderService.getOrderById(orderId);
		if(orderInfo==null){
			return Result.error(CodeMsg.ORDER_NOT_EXIST);
		}
		long goodsId = orderInfo.getGoodsId();
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
		OrderDetailVo orderDetailVo = new OrderDetailVo();
		orderDetailVo.setGoodsVo(goodsVo);
		orderDetailVo.setOrderInfo(orderInfo);
		return Result.success(orderDetailVo);
	}
}
