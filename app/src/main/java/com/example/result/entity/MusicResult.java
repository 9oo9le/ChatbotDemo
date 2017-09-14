package com.example.result.entity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.result.SemanticResultHandler;
import com.example.result.SemanticResultHandler.ResultListener;
import com.iflytek.aiui.assist.player.AIUIPlayer;
import com.iflytek.aiui.assist.player.AIUIPlayer.ContentType;
import com.iflytek.aiui.assist.player.AIUIPlayer.PlayItem;
import com.iflytek.player.PlayController;
import com.iflytek.utils.PlayUrlUtil;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * 音乐播放语义结果
 * 
 * @author admin
 *
 */
public class MusicResult extends SemanticResult {
	private static final String TAG = "MusicResult";
	
	private final static String KEY_OPERATION = "operation";
	private final static String KEY_SEMANTIC = "semantic";
	private final static String KEY_SLOTS = "slots";
	private final static String KEY_INSTYPE = "insType";
	
	private final static String OPERATION_INS = "INSTRUCTION";
	
	public MusicResult(String service, JSONObject json) {
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
			if (json.has(KEY_DIALOG_STAT)) {
				String dialog_stat = json.getString(KEY_DIALOG_STAT);
				if (DIALOG_STAT_INVALID.equals(dialog_stat)) {
					// 有dialog_stat字段，且值为dataInvalid，播放提示语
					super.handleResult(context,listener);
					return;
				}
			}
			
			String songName = "";
			String singerName = "";
			String playUrl = "";
			
			JSONArray results = json.getJSONObject("data").getJSONArray("result");
			JSONObject song = (JSONObject) results.get(0);
			
			if (json.has("state")) {
				// 语义3.1
				
				// 获取歌手名
				JSONArray singerNames = song.optJSONArray("singernames");

				if (singerNames != null) {
					singerName = (String) singerNames.get(0);
				}
				
				// 获取歌曲名
				songName = song.getString("songname");
				
				// 获取歌曲的播放链接
				playUrl = PlayUrlUtil.encode(song.getString("audiopath"));
			} else {
				// 语义3.0
				
				// 获取歌曲名
				songName = song.getString("songname");
				
				// 获取歌手名
				JSONArray singernames = song.optJSONArray("singernames");
                if (singernames != null && singernames.length() > 1) {
                    StringBuffer singerNameBuffer = new StringBuffer();
                    for (int j = 0; j < singernames.length(); j++) {
                        singerNameBuffer.append(singernames.optString(j) + ",");
                    }
                    singerName = singerNameBuffer.toString();
                }else if(singernames != null && singernames.length() == 1){
                    singerName = singernames.optString(0);
                }
				
				playUrl = song.getString("audiopath");
			}
			
			Log.d(TAG, playUrl);
			listener.onResult(songName, singerName, playUrl);
			if (!TextUtils.isEmpty(playUrl)) {
				// 播放歌手名和歌曲名
				String textContent = singerName + "," + songName;
				
				List<PlayItem> itemList = new ArrayList<AIUIPlayer.PlayItem>();
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
				PlayItem item = new PlayItem(ContentType.TEXT, "音乐结果解析出错", null);
				List<PlayItem> itemList = new ArrayList<AIUIPlayer.PlayItem>();
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
