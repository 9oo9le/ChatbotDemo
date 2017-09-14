package com.iflytek.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

/**
 * 音乐播放地址工具类。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月11日 下午2:17:51 
 *
 */
public class PlayUrlUtil {
	
	public static String encode(String url) {
		if (TextUtils.isEmpty(url) || !url.contains("http://")) {
			return url;
		}
		
		Pattern p = Pattern.compile("[\u4E00-\u9FA5]+");
		
		String location = url.substring("http://".length());
		String[] locParts = location.split("/");
		
		if (null == locParts || 1 == locParts.length) {
			return url;
		}
		
		StringBuilder decodeUrl = new StringBuilder("http:/");
		for (int i = 0; i < locParts.length; i++) {
			decodeUrl.append("/");
			Matcher m = p.matcher(locParts[i]);
			
			if (m.find()) {
				try {
					decodeUrl.append(URLEncoder.encode(locParts[i], "utf-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} else {
				decodeUrl.append(locParts[i]);
			}
		}
		
		return decodeUrl.toString().replace("+", " ");
	}
	
}
