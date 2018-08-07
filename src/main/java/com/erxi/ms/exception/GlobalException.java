package com.erxi.ms.exception;

import com.erxi.ms.result.CodeMsg;

public class GlobalException extends RuntimeException {

	/**
	 * 异常拦截
	 */
	private static final long serialVersionUID = 1L;
	private CodeMsg codeMsg;

	public GlobalException(CodeMsg codeMsg) {
		super();
		this.codeMsg = codeMsg;
	}

	public CodeMsg getCodeMsg() {
		return codeMsg;
	}

}
