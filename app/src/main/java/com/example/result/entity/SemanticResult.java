package com.example.result.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.result.SemanticResultHandler;
import com.example.result.SemanticResultHandler.ResultListener;
import com.iflytek.aiui.assist.player.AIUIPlayer;
import com.iflytek.aiui.assist.player.AIUIPlayer.ContentType;
import com.iflytek.aiui.assist.player.AIUIPlayer.PlayItem;
import com.iflytek.cloud.SpeechSynthesizer;

import android.content.Context;
import android.text.TextUtils;

public abstract class SemanticResult {

	// 演示的service类型
	public static enum ServiceType {
		WEATHER, 
		TRAIN, 
		FLIGHT, 
		MUSICX, 					// 音乐播放&控制
		TV_SMARTH, 
		EPG, 
		TVCHANNEL, 
		AIRCONTROL_SMARTH, 
		CURTAIN_SMARTH, 
		LIGHT_SMARTH, 
		HUMIDIFIER_SMARTH, 		// 加湿器
		NUMBER_MASTER, 			// 数字纠错
		CMD, 
		CHAT, 
		PATTERN, 
		PERSONAL_NAME, 
		POETRY,
		JOKE,
		OTHER
	}

	static HashMap<String, ServiceType> serviceMap = new HashMap<String, ServiceType>();

	static {
		serviceMap.put("weather", ServiceType.WEATHER);
		serviceMap.put("train", ServiceType.TRAIN);
		serviceMap.put("flight", ServiceType.FLIGHT);
		serviceMap.put("musicX", ServiceType.MUSICX);
		serviceMap.put("tv_smartH", ServiceType.TV_SMARTH);
		serviceMap.put("epg", ServiceType.EPG);
		serviceMap.put("tvchannel", ServiceType.TVCHANNEL);
		serviceMap.put("airControl_smartH", ServiceType.AIRCONTROL_SMARTH);
		serviceMap.put("curtain_smartH", ServiceType.CURTAIN_SMARTH);
		serviceMap.put("light_smartH", ServiceType.LIGHT_SMARTH);
		serviceMap.put("humidifier_smartH", ServiceType.HUMIDIFIER_SMARTH);
		serviceMap.put("numberMaster", ServiceType.NUMBER_MASTER);
		serviceMap.put("cmd", ServiceType.CMD);
		serviceMap.put("chat", ServiceType.CHAT);
		serviceMap.put("pattern", ServiceType.PATTERN);
		serviceMap.put("personalName", ServiceType.PERSONAL_NAME);
		serviceMap.put("poetry", ServiceType.POETRY);
		serviceMap.put("joke", ServiceType.JOKE);

	}

	public final static String KEY_TEXT = "text";
	public final static String KEY_ANSWER = "answer";
	public final static String KEY_HISTORY = "history";
	public final static String KEY_PROMPT = "prompt";
	public final static String KEY_DATA = "data";
	public final static String KEY_RESULT = "result";
	public final static String KEY_DIALOG_STAT = "dialog_stat";
	public final static String KEY_SEMANTIC = "semantic";
	public final static String KEY_SLOTS = "slots";
	public final static String KEY_CONTENT = "content";
	public final static String KEY_WEBPAGE = "webpage";

	public final static String DIALOG_STAT_INVALID = "dataInvalid";
	public final static String DATE_CURRENT_DAY = "CURRENT_DAY";

	protected String service;
	protected String answerText = "";
	protected String webpage = "";
	protected JSONObject json = null;

	public static ServiceType getServiceType(String service) {
		ServiceType type = serviceMap.get(service);
		if (null == type) {
			type = ServiceType.OTHER;
		}
		return type;
	}

	public SemanticResult(String service, JSONObject json) {
		this.service = service;
		this.json = json;
		
		if (json.has(KEY_DATA)) {
			try {
				JSONObject data = json.getJSONObject(KEY_DATA);
				webpage = data.optString(KEY_WEBPAGE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void handleResult(Context context, ResultListener listener) {
		
		if (null != json) {
			if (json.has(KEY_ANSWER)) {
				// 合成answer中的text字段
				try {
					JSONObject answer = json.getJSONObject(KEY_ANSWER);
					String text = answer.getString(KEY_TEXT);
					answerText = text;

					if (!TextUtils.isEmpty(text)) {
						int readDigit = 1;
						final ServiceType serviceType = getServiceType();

						if (serviceType == ServiceType.NUMBER_MASTER
								|| serviceType == ServiceType.TRAIN
								|| serviceType == ServiceType.FLIGHT) {
							// 数字纠错、火车、航班业务中的数字分开读
							readDigit = 2;
						} else if (serviceType == ServiceType.MUSICX) {
							// 去掉音乐answer中的live和括号中的内容
							if (null != text) {
								text = text.replaceAll("(live)|(LIVE)|(ＬＩＶＥ)|(\\(.*\\))|(（.*）)","");
							}
						} else if (serviceType == ServiceType.WEATHER) {
							if (!DIALOG_STAT_INVALID.equals(json.get(KEY_DIALOG_STAT))) {
//								text = getWeatherReport(text);
							}
						}
						
						SpeechSynthesizer tts = SpeechSynthesizer.getSynthesizer();
						if (null != tts) {
							tts.setParameter("rdn", readDigit + "");
						}
						AIUIPlayer player = SemanticResultHandler.getAIUIPlayer();
						if (null != player) {
							PlayItem item = new PlayItem(ContentType.TEXT, text, null);
							List<PlayItem> itemList = new ArrayList<AIUIPlayer.PlayItem>();
							itemList.add(item);
							
							player.playItems(itemList, null);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String getWeatherReport(String text) throws JSONException {
		// semantic字段
		JSONArray semanticArray = json.getJSONArray("semantic");
		JSONObject semantic = semanticArray.getJSONObject(0);
		JSONObject slots = semantic.getJSONObject("slots");
		JSONObject datetime = slots.getJSONObject("datetime");
		String date = datetime.getString("date");

		// data字段
		JSONObject data = json.getJSONObject("data");
		JSONArray result = data.getJSONArray("result");
		
		if(DATE_CURRENT_DAY.equals(date)){
			JSONObject weatherData = result.getJSONObject(0);
			
			String weather = weatherData.getString("weather");
			String tempRange = weatherData.getString("tempRange");
			String wind = weatherData.getString("wind");
			text = text/* + weather + tempRange + wind*/;
		} else {
			for (int i = 0; i < result.length(); i++) {
				JSONObject weatherData = result.getJSONObject(i);
				if (weatherData.getString("date").equals(date)) {
					String weather = weatherData.getString("weather");
					String tempRange = weatherData.getString("tempRange");
					String wind = weatherData.getString("wind");
					
					text = text/* + weather + tempRange + wind*/;
					break;
				}
			}
		}
		return text;
	}

	protected abstract void doAfterTTS();

	/**
	 * 获取业务名称
	 * 
	 * @return 文字描述
	 */
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getAnswerText() {
		return answerText;
	}

	/**
	 * 获取业务类型
	 * 
	 * @return 便于编程的enum类型，非文字描述，与业务名称一一对应
	 */
	public ServiceType getServiceType() {
		return getServiceType(service);
	}

	public JSONObject getJson() {
		return json;
	}
	
	public String getWebPage() {
		return webpage;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}

}
