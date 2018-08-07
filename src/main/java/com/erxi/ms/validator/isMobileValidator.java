package com.erxi.ms.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.erxi.ms.util.ValidatorUtil;

public class isMobileValidator implements ConstraintValidator<isMobile, String> {

	private boolean required = false;

	/**
	 * 初始化
	 */
	@Override
	public void initialize(isMobile constraintAnnotation) {
		required = constraintAnnotation.required();
	}

	/**
	 * 校验逻辑
	 */
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (required) {
			return ValidatorUtil.isMobile(value);
		} else {
			if (StringUtils.isEmpty(value)) {
				return true;
			} else {
				return ValidatorUtil.isMobile(value);
			}
		}
	}
}
