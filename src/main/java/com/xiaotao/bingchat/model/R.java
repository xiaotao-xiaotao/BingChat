package com.xiaotao.bingchat.model;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 响应信息主体
 *
 * @param <T>
 * @author xiaotao
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

	private Integer code;

	private String msg;

	private T data;

	public static <T> R<T> ok() {
		return restResult(null, 0, null);
	}

	public static <T> R<T> ok(T data) {
		ApiErrorCode aec = ApiErrorCode.SUCCESS;
		if (data instanceof Boolean && Boolean.FALSE.equals(data)) {
			aec = ApiErrorCode.ERROR;
		}
		return restResult(aec, data);
	}

	/**
	 * @param data obj
	 * @param msg msg
	 * @param <T>
	 * @return
	 */
	public static <T> R<T> ok(T data, String msg) {
		ApiErrorCode aec = ApiErrorCode.SUCCESS;
		if (data instanceof Boolean && Boolean.FALSE.equals(data)) {
			aec = ApiErrorCode.ERROR;
		}
		return restResult(data, aec.getCode(), StrUtil.isBlankIfStr(msg) ? aec.getMsg() : msg);
	}

	public static <T> R<T> failed() {
		return restResult(ApiErrorCode.ERROR, null);
	}

	public static <T> R<T> failed(String msg) {
		return restResult(null, ApiErrorCode.ERROR.getCode(), msg);
	}

	public static <T> R<T> failed(T data) {
		return restResult(data, ApiErrorCode.ERROR.getCode(), ApiErrorCode.ERROR.getMsg());
	}

	public static <T> R<T> failed(ApiErrorCode errorCode) {
		return restResult(errorCode, null);
	}

	public static <T> R<T> restResult(ApiErrorCode errorCode, T data) {
		return restResult(data, errorCode.getCode(), errorCode.getMsg());
	}

	public static <T> R<T> restResult(T data, int code, String msg) {
		R<T> apiResult = new R<>();
		apiResult.setCode(code);
		apiResult.setData(data);
		apiResult.setMsg(msg);
		return apiResult;
	}

}
