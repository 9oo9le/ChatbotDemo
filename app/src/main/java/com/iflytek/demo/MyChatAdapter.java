package com.iflytek.demo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iflytek.aiui.assist.player.AIUIPlayer;
import com.iflytek.player.PlayController;
import com.iflytek.player.PlayerStateListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/8/17.
 */

public class MyChatAdapter extends BaseAdapter {

    ViewHolder holder = null;
    Context context = null;
    ArrayList<HashMap<String, Object>> chatList = null;
    AIUIPlayer mAIUIPlayer;
    Handler mPlayImgHandler;
    Handler mTipHandler;
    MyChatAdapter mAdapter;

    String[] keys = {"image", "voicetime", "text", "path"};

    int[] resIds = {R.id.iv_userhead_txt, R.id.tv_chatcontent_txt, R.id.iv_userhead,
                R.id.tv_voice_time, R.id.tv_chatcontent,R.id.iv_userhead_other,
                R.id.tv_chatcontent_other, R.id.iv_voice,R.id.iv_voice_other,
                R.id.avi_sent, R.id.received_message};

    int[] layout = {R.layout.row_sent_txtmsg, R.layout.row_sent_message,R.layout.row_received_message,
                    R.layout.row_received_media, R.layout.row_init_message};


    public MyChatAdapter(Context context,AIUIPlayer player, Handler playImgHandler, Handler tipHandler,
                         ArrayList<HashMap<String, Object>> chatList) {
        super();
        this.context = context;
        this.mAIUIPlayer = player;
        this.chatList = chatList;
        this.mPlayImgHandler = playImgHandler;
        this.mTipHandler = tipHandler;
        this.mAdapter = this;
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        public ImageView imageView = null;
        public TextView textView = null;
        public TextView timeTv = null;
        public ImageView voiceImgView = null;
        public AVLoadingIndicatorView avLoading = null;
        public LinearLayout messageLayout = null;
        public String audioPath = null;

        public TextView songTv = null;
        public TextView playerTv = null;
        public ImageView playIv = null;
        public String url = null;

        public TextView tip1;
        public TextView tip2;
        public TextView tip3;
        public TextView tip4;
        public TextView tip5;
        public TextView tip6;
        public TextView tip7;
        public TextView tip8;
    }

    /**
     * 用户文本View
     * @param position
     * @param convertView
     * @return
     */
    private View handleFromMeWithText(int position, View convertView){
        convertView = LayoutInflater.from(context).inflate(layout[0], null);
        holder.imageView = (ImageView) convertView.findViewById(resIds[0]);
        holder.textView = (TextView) convertView.findViewById(resIds[1]);

        holder.imageView.setBackgroundResource((Integer) chatList.get(position).get(keys[0]));
        holder.textView.setText(chatList.get(position).get(keys[2]).toString());

        return convertView;
    }

    /**
     * 用户语音消息
     * @param position
     * @param convertView
     * @return
     */
    private View handleFromMeWithVoice(int position, View convertView){
        convertView = LayoutInflater.from(context).inflate(layout[1], null);
        holder.imageView = (ImageView) convertView.findViewById(resIds[2]);
        holder.timeTv = (TextView) convertView.findViewById(resIds[3]);
        holder.textView = (TextView) convertView.findViewById(resIds[4]);
        holder.voiceImgView = (ImageView) convertView.findViewById(resIds[7]);
        holder.avLoading = (AVLoadingIndicatorView) convertView.findViewById(resIds[9]);
        holder.messageLayout = (LinearLayout) convertView.findViewById(resIds[10]);

        holder.imageView.setBackgroundResource((Integer) chatList.get(position).get(keys[0]));
        holder.timeTv.setText(chatList.get(position).get(keys[1]).toString());
        if (chatList.get(position).get(keys[2]) != null) {
            holder.avLoading.setVisibility(View.GONE);
            holder.textView.setVisibility(View.VISIBLE);
            holder.textView.setText(chatList.get(position).get(keys[2]).toString());
        }
        holder.audioPath = chatList.get(position).get(keys[3]).toString();
        final ImageView img = holder.voiceImgView;
        final String path = holder.audioPath;
        final Handler voiceImgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    img.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_voice_right1));
                } else if (msg.what == 1) {
                    img.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_voice_right2));
                } else {
                    img.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_voice_right3));
                }
            }
        };

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View paramView) {
                final PlayController playController = PlayController.getInstance(mAIUIPlayer);
                if(playController.isPlayingMusic() || playController.isPlayingOther()){
                    playController.pause().onStop();
                }

                final Timer timer = new Timer();
                final TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            voiceImgHandler.sendEmptyMessage(0);
                            Thread.sleep(500);
                            voiceImgHandler.sendEmptyMessage(1);
                            Thread.sleep(500);
                            voiceImgHandler.sendEmptyMessage(2);
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                PlayerStateListener stateListener = new PlayerStateListener() {
                    @Override
                    public void onStart() {
                        timer.schedule(task, 0, 1500);
                    }

                    @Override
                    public void onStop() {
                        timer.cancel();
                        voiceImgHandler.sendEmptyMessage(2);
                    }
                };
                playController.playVoice(path,stateListener);
            }
        });
        return convertView;
    }

    /**
     * 机器人消息
     * @param position
     * @param convertView
     * @return
     */
    private View handleFromOther(int position, View convertView){
        convertView = LayoutInflater.from(context).inflate(layout[2], null);
        holder.imageView = (ImageView) convertView.findViewById(resIds[5]);
        holder.textView = (TextView) convertView.findViewById(resIds[6]);
        holder.voiceImgView = (ImageView) convertView.findViewById(resIds[8]);
        holder.messageLayout = (LinearLayout) convertView.findViewById(resIds[10]);

        final ImageView img = holder.voiceImgView;
        holder.imageView.setBackgroundResource((Integer) chatList.get(position).get(keys[0]));
        holder.textView.setText(chatList.get(position).get(keys[2]).toString());
        final Handler voiceImgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    img.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_voice_left1));
                } else if (msg.what == 1) {
                    img.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_voice_left2));
                } else {
                    img.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_voice_left3));
                }
            }
        };

        final String text = chatList.get(position).get(keys[2]).toString();
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View paramView) {
                final PlayController playController = PlayController.getInstance(mAIUIPlayer);
                if(playController.isPlayingMusic() || playController.isPlayingOther()){
                    PlayerStateListener listener = playController.pause();
                    if(listener != null){
                        listener.onStop();
                    }else{
                        Message msg = new Message();
                        msg.arg1 = playController.getPlayMusicPosition();
                        msg.arg2 = R.drawable.play_btn;
                        mPlayImgHandler.sendMessage(msg);
                    }
                }

                final Timer timer = new Timer();
                final TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            voiceImgHandler.sendEmptyMessage(0);
                            Thread.sleep(500);
                            voiceImgHandler.sendEmptyMessage(1);
                            Thread.sleep(500);
                            voiceImgHandler.sendEmptyMessage(2);
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                PlayerStateListener stateListener = new PlayerStateListener() {
                    @Override
                    public void onStart() {
                        timer.schedule(task, 0, 1500);
                    }

                    @Override
                    public void onStop() {
                        timer.cancel();
                        voiceImgHandler.sendEmptyMessage(2);
                    }
                };
                playController.playText(text,stateListener);
            }
        });
        return convertView;
    }

    /**
     * 机器人音乐消息
     * @param position
     * @param convertView
     * @return
     */
    private View handleFromOtherWithMedia(final int position, View convertView){
        convertView = LayoutInflater.from(context).inflate(layout[3], null);
        holder.songTv = (TextView) convertView.findViewById(R.id.tv_remote_title);
        holder.playerTv = (TextView) convertView.findViewById(R.id.tv_remote_artist);
        holder.playIv = (ImageView) convertView.findViewById(R.id.iv_remote_play);
        holder.url = (String) chatList.get(position).get("url");
        final String songUrl = holder.url;

        holder.songTv.setText(chatList.get(position).get("song").toString());
        holder.playerTv.setText(chatList.get(position).get("player").toString());
        holder.playIv.setBackgroundResource((Integer) chatList.get(position).get("status"));

        holder.playIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                final PlayController playController = PlayController.getInstance(mAIUIPlayer);

                PlayerStateListener playerListener = new PlayerStateListener() {
                    @Override
                    public void onStart() {
                        playController.setPlayMusicPosition(position);
                        Message msg = new Message();
                        msg.arg1 = position;
                        msg.arg2 = R.drawable.pause_btn;
                        mPlayImgHandler.sendMessage(msg);
                    }

                    @Override
                    public void onStop() {
                        Message msg = new Message();
                        msg.arg1 = playController.getPlayMusicPosition();
                        msg.arg2 = R.drawable.play_btn;
                        mPlayImgHandler.sendMessage(msg);
                    }
                };

                if(playController.isPlayingMusic()){
                    if(position == playController.getPlayMusicPosition()){
                        playController.pause();
                        chatList.get(playController.getPlayMusicPosition()).put("status", R.drawable.play_btn);
                    }else{
                        playController.playMusic(songUrl,playerListener);
                        chatList.get(playController.getPlayMusicPosition()).put("status", R.drawable.play_btn);
                        playController.setPlayMusicPosition(position);
                        chatList.get(playController.getPlayMusicPosition()).put("status", R.drawable.pause_btn);
                    }
                    mAdapter.notifyDataSetChanged();
                }else{
                    if(position != playController.getPlayMusicPosition()){
                        playController.playMusic(songUrl,playerListener);
                    }else{
                        if(playController.isPlayingOther()){
                            playController.pause();
                            playController.playMusic(songUrl,playerListener);
                        }else{
                            playController.resumeMusic(playerListener);
                            playerListener.onStart();
                        }
                    }
                }
            }
        });

        return convertView;
    }

    /**
     * 机器人欢迎消息
     * @param convertView
     * @return
     */
    private View handleFromInitMsg(View convertView){
        convertView = LayoutInflater.from(context).inflate(layout[4], null);
        holder.tip1 = (TextView) convertView.findViewById(R.id.tv_tip_1);
        holder.tip2 = (TextView) convertView.findViewById(R.id.tv_tip_2);
        holder.tip3 = (TextView) convertView.findViewById(R.id.tv_tip_3);
        holder.tip4 = (TextView) convertView.findViewById(R.id.tv_tip_4);
        holder.tip5 = (TextView) convertView.findViewById(R.id.tv_tip_5);
        holder.tip6 = (TextView) convertView.findViewById(R.id.tv_tip_6);
        holder.tip7 = (TextView) convertView.findViewById(R.id.tv_tip_7);
        holder.tip8 = (TextView) convertView.findViewById(R.id.tv_tip_8);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = ((TextView)view).getText().toString();
                Bundle bundle = new Bundle();
                bundle.putString("text",text);
                Message msg = new Message();
                msg.setData(bundle);
                mTipHandler.sendMessage(msg);
            }
        };

        holder.tip1.setOnClickListener(onClickListener);
        holder.tip2.setOnClickListener(onClickListener);
        holder.tip3.setOnClickListener(onClickListener);
        holder.tip4.setOnClickListener(onClickListener);
        holder.tip5.setOnClickListener(onClickListener);
        holder.tip6.setOnClickListener(onClickListener);
        holder.tip7.setOnClickListener(onClickListener);
        holder.tip8.setOnClickListener(onClickListener);

        return convertView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        holder = new ViewHolder();
        int who = (Integer) chatList.get(position).get("person");
        if (who == 0) {
            return handleFromMeWithText(position,convertView);
        } else if (who == 1) {
            return handleFromMeWithVoice(position,convertView);
        } else if (who == 2) {
            return handleFromOther(position, convertView);
        } else if(who == 3){
            return handleFromOtherWithMedia(position, convertView);
        }else{
            return handleFromInitMsg(convertView);
        }
    }
}
