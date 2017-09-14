package com.example.result.entity;

import org.json.JSONObject;

import com.example.result.SemanticResultHandler.ResultListener;

import android.content.Context;

/**
 * 信息类的结果，包括天气、火车和航班
 * 
 * @author admin
 *
 */
public class InfoResult extends SemanticResult {

	public InfoResult(String service, JSONObject json) {
		super(service, json);
	}

	@Override
	public void handleResult(Context context, ResultListener listener) {
		super.handleResult(context, listener);
	}

	@Override
	protected void doAfterTTS() {

	}

}
