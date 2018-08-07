package com.erxi.ms.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erxi.ms.domain.MsOrder;
import com.erxi.ms.domain.MsUser;
import com.erxi.ms.redis.RedisService;
import com.erxi.ms.service.GoodsService;
import com.erxi.ms.service.MiaoshaService;
import com.erxi.ms.service.MsUserService;
import com.erxi.ms.service.OrderService;
import com.erxi.ms.vo.GoodsVo;

@Service
public class MQReceiver {

	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

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

	@RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
	public void receive(String message) {
		log.info("receive msg:" + message);
		MiaoShaMessage mm = RedisService.stringToBean(message,
				MiaoShaMessage.class);
		MsUser msUser = mm.getMsUser();
		long goodsId = mm.getGoodsId();

		
		// 判断库存
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
		int stock = goodsVo.getGoodsStock();
		if (stock <= 0) {
			return;
		}
		// 判断是否已经秒杀
		MsOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(
				msUser.getId(), goodsId);
		if (order != null) {
			return;
		}
		// 减库存 下订单 写入秒杀订单 --- 事务操作
		miaoshaService.miaosha(msUser, goodsVo);
	}

	// @RabbitListener(queues = MQConfig.QUEUE)
	// public void receive(String message) {
	// log.info("receive msg:" + message);
	// }
	//
	// @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
	// public void receiveTopic1(String message) {
	// log.info(" topic  queue1 message:" + message);
	// }
	//
	// @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
	// public void receiveTopic2(String message) {
	// log.info(" topic  queue2 message:" + message);
	// }
	//
	// @RabbitListener(queues = MQConfig.HEADER_QUEUE)
	// public void receiveHeaderQueue(byte[] message) {
	// log.info(" header  queue message:" + new String(message));
	// }
}
