package com.example.result.entity;

import org.json.JSONObject;

import com.example.result.SemanticResultHandler.ResultListener;

import android.content.Context;

/**
 * 聊天结果
 * 
 * @author hj
 *
 */
public class ChatResult extends SemanticResult {

	public ChatResult(String service, JSONObject json) {
		super(service, json);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void handleResult(Context context, ResultListener listener) {
		super.handleResult(context,listener);
	}

	@Override
	protected void doAfterTTS() {
		// TODO Auto-generated method stub
		
	}

}
