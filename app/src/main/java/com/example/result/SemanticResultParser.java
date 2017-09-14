package com.example.result;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.result.entity.ChatResult;
import com.example.result.entity.InfoResult;
import com.example.result.entity.JokeResult;
import com.example.result.entity.MusicResult;
import com.example.result.entity.PoetryResult;
import com.example.result.entity.SemanticResult;
import com.example.result.entity.SemanticResult.ServiceType;

import android.text.TextUtils;

public class SemanticResultParser {
	
	private final static String KEY_SERVICE = "service";
	
	// 解析语义结果，无service字段或者service非演示的业务则返回null
	public static SemanticResult parse(JSONObject jsonObject) {
		SemanticResult semanticResult = null;
		
		try {
			// 增加判空操作，因为jsonObject有可能为null
			if (null == jsonObject || !jsonObject.has(KEY_SERVICE)) {
				return null;
			}
			
			String service = jsonObject.getString(KEY_SERVICE);
			
			if (!TextUtils.isEmpty(service)) {
				ServiceType serviceType = SemanticResult.getServiceType(service);
				
				switch (serviceType) {
					case WEATHER:
					case TRAIN:
					case FLIGHT:
					case TVCHANNEL:
					case EPG:
					case NUMBER_MASTER:
						semanticResult = new InfoResult(service, jsonObject);
						break;
					case MUSICX:
						semanticResult = new MusicResult(service, jsonObject);
						break;
					case JOKE:
						semanticResult = new JokeResult(service, jsonObject);
						break;
					case TV_SMARTH:
					case AIRCONTROL_SMARTH:
					case CURTAIN_SMARTH:
					case LIGHT_SMARTH:
					case HUMIDIFIER_SMARTH:
						break;
					case CMD:
						break;
					case PATTERN:
					case CHAT:
						semanticResult = new ChatResult(service, jsonObject);
						break;
					case POETRY:
						semanticResult = new PoetryResult(service, jsonObject);
						break;
	
					default:
						break;
				}
				if(semanticResult == null){
				    semanticResult = new SemanticResult(service, jsonObject) {
                        
                        @Override
                        protected void doAfterTTS() {
                            
                        }
                    };
				}
                    
			} 
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return semanticResult;
	}
	
	public static SemanticResult parse(String json) {
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return parse(jsonObject);
	}
	
}
