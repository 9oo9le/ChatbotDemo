package com.iflytek.app;

import java.io.File;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.utils.CommonUtils;

import android.app.Application;
import android.os.Environment;

public class DemoApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		initMSC();
		
        String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/ChatbotDemo"; 
        File file=new File(path); 
        CommonUtils.RecursionDeleteFile(file);
	}
	
	private void initMSC() {
		将此处的123456789替换为应用对应的appid，并替换MSC.jar与libmsc.so。应用申请地址：http://aiui.xfyun.cn
		SpeechUtility.createUtility(DemoApp.this, SpeechConstant.APPID +"=123456789");
	}
	
}
