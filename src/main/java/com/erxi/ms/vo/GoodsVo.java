package com.erxi.ms.vo;

import java.util.Date;

import com.erxi.ms.domain.Goods;

public class GoodsVo extends Goods {
	private Integer stockCount;
	private Double msPrice;
	private Date startDate;
	private Date endDate;

	public Integer getStockCount() {
		return stockCount;
	}

	public void setStockCount(Integer stockCount) {
		this.stockCount = stockCount;
	}

	public Double getMsPrice() {
		return msPrice;
	}

	public void setMsPrice(Double msPrice) {
		this.msPrice = msPrice;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

}
