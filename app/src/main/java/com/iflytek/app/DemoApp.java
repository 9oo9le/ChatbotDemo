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
		SpeechUtility.createUtility(DemoApp.this, SpeechConstant.APPID +"=598143a9");
	}
	
}
