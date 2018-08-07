package com.erxi.ms.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erxi.ms.dao.OrderDao;
import com.erxi.ms.domain.MsOrder;
import com.erxi.ms.domain.MsUser;
import com.erxi.ms.domain.OrderInfo;
import com.erxi.ms.redis.OrderKey;
import com.erxi.ms.redis.RedisService;
import com.erxi.ms.vo.GoodsVo;

@Service
public class OrderService {

	@Autowired
	OrderDao orderDao;

	@Autowired
	RedisService redisService;

	public MsOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {
		// 插库改为插缓存
//		return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
		return redisService.get(OrderKey.getMsOrderByUidGid, "" + userId+"_"+goodsId,
				MsOrder.class);
	}

	public OrderInfo getOrderById(long orderId) {
		 return orderDao.getOrderById(orderId);
	}

	@Transactional
	public OrderInfo creatOrder(MsUser msUser, GoodsVo goodsVo) {
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setCreateDate(new Date());
		orderInfo.setDeliveryAddrId(0L);
		orderInfo.setGoodsCount(1);
		orderInfo.setGoodsId(goodsVo.getId());
		orderInfo.setGoodsName(goodsVo.getGoodsName());
		orderInfo.setGoodsPrice(goodsVo.getMsPrice());
		orderInfo.setOrderChannel(1);
		orderInfo.setStatus(0);
		orderInfo.setUserId(msUser.getId());
		orderDao.insert(orderInfo);

		MsOrder msOrder = new MsOrder();
		msOrder.setGoodsId(goodsVo.getId());
		msOrder.setOrderId(orderInfo.getId());
		msOrder.setUserId(msUser.getId());
		orderDao.insertMiaoshaOrder(msOrder);

		redisService.set(OrderKey.getMsOrderByUidGid, "" + msUser.getId() + "_"
				+ goodsVo.getId(), msOrder);

		return orderInfo;
	}

	public void deleteOrders() {
		orderDao.deleteOrders();
		orderDao.deleteMiaoshaOrders();
	}

}
