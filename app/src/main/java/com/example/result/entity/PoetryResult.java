package com.example.result.entity;

import org.json.JSONObject;

/**
 * 对诗结果。
 * 
 * @author hj
 * @date 2016年4月1日 上午11:02:17 
 *
 */
public class PoetryResult extends SemanticResult {

	public PoetryResult(String service, JSONObject jsonObject) {
		super(service, jsonObject);
	}

	@Override
	protected void doAfterTTS() {
		// TODO Auto-generated method stub

	}

}
