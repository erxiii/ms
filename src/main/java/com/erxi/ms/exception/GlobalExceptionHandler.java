package com.erxi.ms.exception;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erxi.ms.result.CodeMsg;
import com.erxi.ms.result.Result;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
	@ExceptionHandler(value = Exception.class)
	public Result<String> exceptionHandler(HttpServletRequest request,
			Exception e) {
		e.printStackTrace();
		if (e instanceof GlobalException) {
			GlobalException be = (GlobalException) e;
			return Result.error(be.getCodeMsg());
		}else if (e instanceof BindException) {
			BindException be = (BindException) e;
			List<ObjectError> errors = be.getAllErrors();
			ObjectError error = errors.get(0);
			String msg = error.getDefaultMessage();
			return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
		} else {
			return Result.error(CodeMsg.SERVER_ERROR);
		}
	}
}
