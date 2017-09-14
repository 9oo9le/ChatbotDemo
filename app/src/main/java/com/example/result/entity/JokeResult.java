package com.example.result.entity;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.result.SemanticResultHandler;
import com.example.result.SemanticResultHandler.ResultListener;
import com.iflytek.aiui.assist.player.AIUIPlayer;
import com.iflytek.aiui.assist.player.AIUIPlayer.ContentType;
import com.iflytek.aiui.assist.player.AIUIPlayer.PlayItem;
import com.iflytek.player.PlayController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 音乐播放语义结果
 * 
 * @author admin
 *
 */
public class JokeResult extends SemanticResult {
	private static final String TAG = "JokeResult";

	private final static String KEY_OPERATION = "operation";
	private final static String KEY_SEMANTIC = "semantic";
	private final static String KEY_SLOTS = "slots";
	private final static String KEY_INSTYPE = "insType";

	private final static String OPERATION_INS = "INSTRUCTION";

	public JokeResult(String service, JSONObject json) {
		super(service, json);
	}

	public String getInsType() {
		String operation = null;
		try {
			operation = json.getString(KEY_OPERATION);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		if (OPERATION_INS.equals(operation)) {
			// 控制语义
			try {
				JSONObject semantic = json.getJSONObject(KEY_SEMANTIC);
				String insType = semantic.getJSONObject(KEY_SLOTS).getString(KEY_INSTYPE);

				return insType;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return "";
	}

	@Override
	public void handleResult(Context context,  ResultListener listener) {

		try {
			String operation = json.has(KEY_OPERATION) ? json.getString(KEY_OPERATION) : "";
			if (OPERATION_INS.equals(operation)) {
				// 不处理控制语义
				return;
			}

			// 音乐播放
//			if (json.has(KEY_DIALOG_STAT)) {
//				String dialog_stat = json.getString(KEY_DIALOG_STAT);
//				if (DIALOG_STAT_INVALID.equals(dialog_stat)) {
//					// 有dialog_stat字段，且值为dataInvalid，播放提示语
//					super.handleResult(context,listener);
//					return;
//				}
//			}

			String songName = "";
			String singerName = "";
			String playUrl = "";

			JSONArray results = json.getJSONObject("data").getJSONArray("result");
			JSONObject song = (JSONObject) results.get(0);

			songName = song.optString("title");

			singerName = song.optString("author");

			playUrl = song.has("mp3Url") ? song.optString("mp3Url") : song.optString("mp4Url");

			Log.d(TAG, playUrl);
			listener.onResult(songName, singerName, playUrl);
			if (!TextUtils.isEmpty(playUrl)) {
				// 播放歌手名和歌曲名
				String textContent = "请听笑话" + "," + songName;

				List<PlayItem> itemList = new ArrayList<PlayItem>();
				itemList.add(new PlayItem(ContentType.TEXT, textContent, new Runnable() {

							@Override
							public void run() {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}));
				itemList.add(new PlayItem(ContentType.MUSIC, playUrl, null));

				PlayController playController = PlayController.getInstance(SemanticResultHandler.getAIUIPlayer());
				playController.playMusic(itemList);
			}
		} catch (JSONException e1) {
			e1.printStackTrace();

			AIUIPlayer player = SemanticResultHandler.getAIUIPlayer();
			if (null != player) {
				PlayItem item = new PlayItem(ContentType.TEXT, "笑话结果解析出错", null);
				List<PlayItem> itemList = new ArrayList<PlayItem>();
				itemList.add(item);

				PlayController playController = PlayController.getInstance(SemanticResultHandler.getAIUIPlayer());
				playController.playMusic(itemList);
			}
		}
		
	}

	@Override
	protected void doAfterTTS() {
		
	}

    @Override
    public String getAnswerText() {
        return null;
    }
	
}
