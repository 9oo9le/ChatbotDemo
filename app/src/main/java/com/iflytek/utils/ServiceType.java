package com.iflytek.utils;

import java.util.HashMap;

public enum ServiceType {
	WEATHER,           
	TRAIN, 
	FLIGHT, 
	MUSICX, 				// 音乐播放&控制
	AIRCONTROL_SMARTH, 
	CURTAIN_SMARTH, 
	LIGHT_SMARTH, 
	FREEZER_SMARTH,
	HUMIDIFIER_SMARTH, 		// 加湿器
	NUMBER_MASTER, 			// 数字纠错
	CMD, 
	CHAT, 
	SMARTHOME, 				// 智能家居
	TELEPHONE,
	COOKBOOK,
	STORY,
	RADIO,
	JOKE,
	NEWS,
	PM25,
	DATETIME,
	CALC,
	DISHORDER,
	TRANSLATION,
	IDIOM,
	SCHEDULEX,
	OTHER;
	
	
	static HashMap<String, ServiceType> serviceMap = new HashMap<String, ServiceType>();

	static {
		serviceMap.put("weather", WEATHER);
		serviceMap.put("train", TRAIN);
		serviceMap.put("flight", FLIGHT);
		serviceMap.put("musicX", MUSICX);
		serviceMap.put("airControl_smartHome", AIRCONTROL_SMARTH);
		serviceMap.put("curtain_smartH", CURTAIN_SMARTH);
		serviceMap.put("light_smartH", LIGHT_SMARTH);
		serviceMap.put("humidifier_smartHome", HUMIDIFIER_SMARTH);
		serviceMap.put("freezer_smartHome", FREEZER_SMARTH);
		serviceMap.put("numberMaster", NUMBER_MASTER);
		serviceMap.put("smartHome", SMARTHOME);
		serviceMap.put("dishOrder", DISHORDER);
		serviceMap.put("telephone", TELEPHONE);
		serviceMap.put("story", STORY);
		serviceMap.put("news", NEWS);
		serviceMap.put("joke", JOKE);
		serviceMap.put("cmd", CMD);
		serviceMap.put("radio", RADIO);
		serviceMap.put("datetime", DATETIME);
		serviceMap.put("calc", CALC);
		serviceMap.put("pm25", PM25);
		serviceMap.put("translation", TRANSLATION);
		serviceMap.put("idiom", IDIOM);
		serviceMap.put("scheduleX", SCHEDULEX);
	}
	
	
	public static ServiceType getServiceType(String service) {
		ServiceType type = serviceMap.get(service);
		
		if (null == type) {
			type = ServiceType.OTHER;
		}
		
		return type;
	}

}