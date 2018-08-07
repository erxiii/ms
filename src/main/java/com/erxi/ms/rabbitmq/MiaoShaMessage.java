package com.erxi.ms.rabbitmq;

import com.erxi.ms.domain.MsUser;

public class MiaoShaMessage {
	private MsUser msUser;
	private long goodsId;

	public MsUser getMsUser() {
		return msUser;
	}

	public void setMsUser(MsUser msUser) {
		this.msUser = msUser;
	}

	public long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(long goodsId) {
		this.goodsId = goodsId;
	}
}
