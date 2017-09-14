package com.example.result;

import com.example.result.entity.SemanticResult;
import com.iflytek.aiui.assist.player.AIUIPlayer;
import com.iflytek.demo.ChatActivity.ParseResultListener;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class SemanticResultHandler {
	private static final String TAG = "SemanticResultHandler";
	
	private static SemanticResultHandler instance;
	
	private static AIUIPlayer aiuiPlayer;
	
	private Context mContext;
	
	private ResultHandler mResultHandler;
	
	private HandlerThread mHandlerThread;
	
	
	public static SemanticResultHandler getInstance(Context context, AIUIPlayer player) {
		aiuiPlayer = player;
		
		if (null == instance) {
			instance = new SemanticResultHandler(context);
		}
		return instance;
	}
	
	public static AIUIPlayer getAIUIPlayer() {
		return aiuiPlayer;
	}
	
	private SemanticResultHandler(Context context) {
		mContext = context;
		mHandlerThread = new HandlerThread("ResultHandleThread");
		mHandlerThread.start();
		mResultHandler = new ResultHandler(mHandlerThread.getLooper());
	}
	
	public interface ResultListener {
        void onResult(String song, String player, String url);
    }
	ResultListener inListener;
	public void handleResult(SemanticResult result,final ParseResultListener outListener) {
		if (null != mResultHandler && null != result) {
		    inListener = new ResultListener() {
                
                @Override
                public void onResult(String song, String player, String url) {
                    outListener.onResult(song, player,url);
                }
            };
			// 清空以前的消息，避免积压
			mResultHandler.removeMessages(MSG_SEMANTIC_RESULT);
			Message msg = new Message();
			mResultHandler.obtainMessage(MSG_SEMANTIC_RESULT, result).sendToTarget();
		}
	}
	
	public void destroy() {
		if (null != mHandlerThread) {
			mHandlerThread.quit();
			mResultHandler = null;
		}
		instance = null;
	}
	
	private static final int MSG_SEMANTIC_RESULT = 1;
	
	class ResultHandler extends Handler {
		
		public ResultHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			SemanticResult result = (SemanticResult) msg.obj;
			result.handleResult(mContext, inListener);
		}
	}
	
}
