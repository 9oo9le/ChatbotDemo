package com.iflytek.player;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.iflytek.aiui.assist.player.AIUIPlayer;

import java.util.List;

/**
 * Created by Administrator on 2017/8/9.
 */

public class PlayController {

    private static final String TAG = "PlayController";

    boolean mIsPlayingMusic = false;
    boolean mIsPlayingOther = false;

    public int getPlayMusicPosition() {
        return mPlayMusicPosition;
    }

    public void setPlayMusicPosition(int mPlayMusicPosition) {
        this.mPlayMusicPosition = mPlayMusicPosition;
    }

    int mPlayMusicPosition = -1;

    AIUIPlayer mPlayer;

    private static PlayController instance;

    PlayerStateListener mPlayerStateListener;

    Handler mNotifyHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    AIUIPlayer.AIUIPlayerListener mAIUIPlayerListener = new AIUIPlayer.AIUIPlayerListener() {
        @Override
        public void onStart(AIUIPlayer.PlayItem playItem) {
            mPlayerStateListener.onStart();

            Log.i(TAG, "player onStart");
        }

        @Override
        public void onProgress(AIUIPlayer.PlayItem playItem, int i) {

            Log.i(TAG, "player onProgress");
        }

        @Override
        public void onPause(AIUIPlayer.PlayItem playItem) {
            mPlayerStateListener.onStop();
            Log.i(TAG, "player onPause");
        }

        @Override
        public void onResume(AIUIPlayer.PlayItem playItem) {
            mPlayerStateListener.onStart();
            Log.i(TAG, "player onResume");
        }

        @Override
        public void onStop(AIUIPlayer.PlayItem playItem) {
            mPlayerStateListener.onStop();
            Log.i(TAG, "player onStop");
        }

        @Override
        public void onError(AIUIPlayer.PlayItem playItem, int i) {
            mPlayerStateListener.onStop();
            Log.i(TAG, "player onError");
        }

        @Override
        public void onCompleted(AIUIPlayer.PlayItem playItem, boolean b) {
            mPlayerStateListener.onStop();
            Log.i(TAG, "player onCompleted");
        }
    };

    public PlayController(AIUIPlayer player){
        this.mPlayer = player;
    }

    public synchronized static PlayController getInstance(AIUIPlayer player) {
        if (null == instance) {
            instance = new PlayController(player);
        }
        return instance;
    }

    public void playVoice(String path, PlayerStateListener listener){
        if(mPlayerStateListener != null){
            if(mIsPlayingOther)
                mPlayerStateListener.onStop();
        }

        mPlayerStateListener = listener;
        mIsPlayingOther = true;
        mPlayer.playMusic(path,mAIUIPlayerListener);
    }

    public void playMusic(List<AIUIPlayer.PlayItem> items){
        mIsPlayingMusic = true;
        mPlayer.playItems(items, null);
    }

    public void playMusic(String path, PlayerStateListener listener){
        if(mPlayerStateListener != null){
            if(mIsPlayingOther)
                mPlayerStateListener.onStop();
        }
        mPlayerStateListener = listener;
        mIsPlayingMusic = true;
        mPlayer.playMusic(path,mAIUIPlayerListener);
    }

    public PlayerStateListener resumeMusic(PlayerStateListener listener){
        if(mPlayerStateListener != null){
            if(mIsPlayingOther)
                mPlayerStateListener.onStop();
        }

        mPlayer.resume();

        mPlayerStateListener = listener;
        setPlayMusicState(true);
        return mPlayerStateListener;
    }

    public void playText(String text, PlayerStateListener listener){
        if(mPlayerStateListener != null){
            if(mIsPlayingOther)
                mPlayerStateListener.onStop();
        }
        mPlayerStateListener = listener;
        mIsPlayingOther = true;
        mPlayer.playText(text,mAIUIPlayerListener);
    }

    public PlayerStateListener pause(){
        if(mIsPlayingMusic){
            mIsPlayingMusic = false;
        }
        if(mIsPlayingOther){
            mIsPlayingOther = false;
        }
        mPlayer.pause();
        return mPlayerStateListener;
    }

    public boolean isPlayingOther(){
        return mIsPlayingOther;
    }

    public void setPlayOtherState(boolean state){
        mIsPlayingOther = state;
    }

    public boolean isPlayingMusic(){
        return mIsPlayingMusic;
    }

    public void setPlayMusicState(boolean state){
        mIsPlayingMusic = state;
    }
}
