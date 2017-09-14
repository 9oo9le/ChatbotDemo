package com.iflytek.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.result.SemanticResultHandler;
import com.example.result.SemanticResultParser;
import com.gyf.barlibrary.ImmersionBar;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.assist.player.AIUIPlayer;
import com.iflytek.captor.DataCaptureListener;
import com.iflytek.captor.SystemAudioCaptor;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.util.VolumeUtil;
import com.iflytek.player.PlayController;
import com.iflytek.utils.AudioFileSave;
import com.iflytek.utils.CommonUtils;
import com.iflytek.utils.SemanticResult;
import com.iflytek.widget.PasteEditText;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

import static com.iflytek.utils.CommonUtils.FlymeSetStatusBarLightMode;
import static com.iflytek.utils.CommonUtils.MIUISetStatusBarLightMode;

public class ChatActivity extends AppCompatActivity implements OnClickListener {

    private final static String TAG = "ChatActivity";

    private final static String PARAM_ASSET_PATH = "cfg/aiui_phone_user.cfg";
    private final static String SCENE_MAIN = "main";
    private final static String SCENE_GAME = "game";
    private final static String SCENE_MEDICAL = "medical";


    public static final String RC4_TIP = "不好意思，我暂时还回答不了你的问题";
    public static final String NO_CONTENT = "无内容~";
    public static final String NO_SAY = "您没有说话哟~";

    private final static String KEY_SERVICE = "service";
    private final static String KEY_RC = "rc";
    private final static String KEY_TEXT = "text";


    public final static int ME_TXT = 0;
    public final static int ME = 1;
    public final static int OTHER = 2;
    public final static int MUSIC = 3;
    public final static int BOT_INIT = 4;

    private InputMethodManager mInputManager;
    private ListView mListView;
    private RelativeLayout mEditTextLayout;
    private View mSendTxtBtn;
    private View mPressToSpeakBtn;
    private View mRecordingContainer;
    private View mSetModeKeyboardBtn;
    private View mSetModeVoiceBtn;
    private PasteEditText mEditTextContent;
    private MyChatAdapter mAdapter;
    private View mDialogInflate;
    private TextView mMainSceneTv;
    private TextView mGameSceneTv;
    private TextView mMedicalSceneTv;
    private Dialog mChangeSceneDialog;
    private ImageView mMicImg;
    private ImageButton mReturnBtn;
    private ImageButton mSwitchBtn;
    private Drawable[] mMicRes;
    private TextView mRecordingHint;

    private AIUIAgent mAIUIAgent;
    private boolean mIsWakeup;
    private Toast mToast;
    // 合成发音人资源
    String mTTSRes = "mengmeng";
    private SpeechSynthesizer mTTS;

    AudioFileSave mAudioFileSave;
    SystemAudioCaptor mAudioCaptor;

    // AIUI播放器
    private AIUIPlayer mAIUIPlayer;
    SemanticResultHandler mResultHandler;
    private JSONObject mLastResultJson = null;

    ArrayList<HashMap<String, Object>> mChatList = null;


    boolean mIsPlaying = false;
    boolean mIsSaidSomething = false;

    public interface ParseResultListener {
        void onResult(String song, String player, String url);
    }

    /**
     * 录音音频变化Img处理
     */
    @SuppressLint("HandlerLeak")
    private Handler mMicImageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mMicImg.setImageDrawable(mMicRes[msg.what]);
        }
    };

    /**
     * 播放暂停音乐按钮处理
     */
    private Handler mPlayImgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int position = msg.arg1;
            int sourceId = msg.arg2;

            mChatList.get(position).put("status", sourceId);
            mAdapter.notifyDataSetChanged();
        }
    };

    /**
     * 添加音乐
     */
    @SuppressLint("HandlerLeak")
    private Handler mMusicHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Bundle bundle = (Bundle) msg.obj;
                String song = bundle.getString("song");
                String player = bundle.getString("player");
                String url = bundle.getString("url");
                addMusicToList(song, url, MUSIC, player, R.drawable.pause_btn);
            }
        }
    };

    /**
     * 初始化聊天界面提示
     */
    private Handler mTipHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String text = msg.getData().getString("text");

            if (null != mAIUIAgent) {
                if (!mIsWakeup) {
                    AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
                    mAIUIAgent.sendMessage(wakeupMsg);
                }

                AIUIMessage writeMsg = new AIUIMessage(AIUIConstant.CMD_WRITE,
                        0, 0, "data_type=text", text.getBytes());
                mAIUIAgent.sendMessage(writeMsg);

                addTextToList(text,null,ME_TXT,null);
                playResult();
            }
        }
    };

    /**
     * 超时结果处理
     */
    private Handler mResultTimeOutHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int position = msg.what;
            HashMap<String, Object> map = mChatList.get(position);

            if(Integer.parseInt(map.get("person").toString()) == ME){
                if(!map.containsKey("text") || map.get("text").toString().equals("")){
                    map.put("text",NO_CONTENT);
                    mChatList.set(position, map);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    /**
     * 录音回调
     */
    DataCaptureListener captureListener = new DataCaptureListener() {

        @Override
        public void onError(int error, String des) {

        }

        @Override
        public void onData(byte[] data, int dataLen, Bundle des) {

            if (null != mAIUIAgent) {
                long curTime = System.currentTimeMillis();

                AIUIMessage writeMsg = new AIUIMessage(AIUIConstant.CMD_WRITE, (int) (curTime & 0xffff), 0,
                        "data_type=audio,sample_rate=16000,timestamp=" + curTime, data);
                mAIUIAgent.sendMessage(writeMsg);
            }

            if (mAudioFileSave != null)
                mAudioFileSave.writeData(data);

            final byte[] d = data;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mMicImageHandler.sendEmptyMessage(calcVolum(VolumeUtil.computeVolume(d, d.length)));
                }
            }).start();
        }

        @Override
        public void onCaptureStopped() {

        }

        @Override
        public void onCaptureStarted() {

        }

        @Override
        public void onCaptorReleased() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        setContentView(R.layout.activity_chat);

        initAIUI();
        setTTSParams();
        initView();

        addInitTextToList();
    }

    /**
     * 初始化状态栏
     */
    private void initStatusBar(){
        ImmersionBar.with(this)
                .keyboardEnable(true)
                .transparentStatusBar()
                .init();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        MIUISetStatusBarLightMode(this.getWindow(), true);
        FlymeSetStatusBarLightMode(this.getWindow(), true);

    }

    /**
     * 初始化TTS
     */
    private void setTTSParams() {
        mTTS = SpeechSynthesizer.createSynthesizer(ChatActivity.this, null);
        mTTS.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mTTS.setParameter(SpeechConstant.VOICE_NAME, mTTSRes);
        mTTS.setParameter(SpeechConstant.SPEED, "50");
        mTTS.setParameter(SpeechConstant.PITCH, "50");
        mTTS.setParameter(SpeechConstant.VOLUME, "50");
        mTTS.setParameter(SpeechConstant.STREAM_TYPE, "3");
        mTTS.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        mAIUIPlayer = new AIUIPlayer(ChatActivity.this, mTTS);

        mResultHandler = SemanticResultHandler.getInstance(ChatActivity.this, mAIUIPlayer);
    }

    /**
     * 添加音乐布局
     * @param song 歌曲名
     * @param Url 歌曲URL
     * @param who 布局类型
     * @param player 歌手
     * @param status 播放状态
     */
    protected void addMusicToList(String song, String Url, int who, String player, int status) {
        mIsPlaying = true;
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("person", who);
        map.put("image", R.drawable.robot_icon/*who == ME_TXT ? R.drawable.touxiang : (who == ME ? R.drawable.touxiang : R.drawable.touxiang)*/);
        if (song != null)
            map.put("song", song);
        if (Url != null) {
            map.put("url", Url);
        }
        if (player != null) {
            map.put("player", player);
        }
        map.put("status", status);
        mChatList.add(map);
        PlayController.getInstance(mAIUIPlayer).setPlayMusicPosition(mChatList.size() - 1);
        PlayController.getInstance(mAIUIPlayer).setPlayMusicState(true);
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mChatList.size() - 1);
    }

    /**
     * 添加发送录音布局
     * @param text 发送内容
     * @param voiceTime 语音时间
     * @param who 布局类型
     * @param path 音频文件路径
     */
    protected void addTextToList(String text, String voiceTime, int who, String path) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("person", who);
        map.put("image", who == ME_TXT ? R.drawable.user_icon : (who == ME ? R.drawable.user_icon : R.drawable.robot_icon));
        if (text != null)
            map.put("text", text);
        if (voiceTime != null) {
            map.put("voicetime", voiceTime + "s");
        }
        if (path != null) {
            map.put("path", path);
        }
        mChatList.add(map);
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mChatList.size() - 1);
    }

    /**
     * 添加初始化提示内容
     */
    protected void addInitTextToList(){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("person", BOT_INIT);
        map.put("image", R.drawable.robot_icon);
        mChatList.add(map);
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mChatList.size() - 1);
    }

    /**
     * 初始化AIUI
     */
    private void initAIUI() {
        mAudioCaptor = new SystemAudioCaptor(captureListener);
        mChatList = new ArrayList<HashMap<String, Object>>();
        if (null == mAIUIAgent) {
            // 创建Agent实例，一创建即会开启服务，不用额外发送CMD_START消息
            mAIUIAgent = AIUIAgent.createAgent(ChatActivity.this, getAIUIParams(), mAIUIListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mAIUIAgent) {
            mAIUIAgent.destroy();
            mAIUIAgent = null;
        }
    }

    private void showTip(final String tip) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (!TextUtils.isEmpty(tip)) {
                    mToast.setText(tip);
                    mToast.show();
                }
            }
        });
    }

    /**
     * 音乐解析结果回调
     */
    ParseResultListener listener = new ParseResultListener() {

        @Override
        public void onResult(String song, String player, String url) {
            Bundle bundle = new Bundle();
            bundle.putString("song", song);
            bundle.putString("player", player);
            bundle.putString("url", url);

            Message msg = new Message();
            msg.obj = bundle;
            msg.what = 0;

            mMusicHandler.sendMessage(msg);
        }
    };

    /**
     * 处理返回结果
     * @param resultJson 结果JSON
     */
    public void handleResult(JSONObject resultJson) {

        // 获得语义结果业务类型
        SemanticResult semanticResult = parseSemanticResult(resultJson);

        if (null != semanticResult) {
            addTextToList(semanticResult.getAnswerText(), null, OTHER, null);
            mLastResultJson = resultJson;
        }
    }

    /**
     * 播放解析结果
     */
    private void playResult() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                int count = 0;
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mLastResultJson != null) {
                        com.example.result.entity.SemanticResult semanticResult = SemanticResultParser.parse(mLastResultJson);
                        mLastResultJson = null;
                        if (null != semanticResult) {
                            mResultHandler.handleResult(semanticResult, listener);
                        }
                        break;
                    } else {
                        count++;
                        if (count >= 10) {
                            break;
                        }
                    }
                }
            }
        }).start();

    }

    /**
     * 解析返回数据
     * @param json
     * @return
     */
    public SemanticResult parseSemanticResult(JSONObject json) {

        HashMap<String, Object> map = mChatList.get(mChatList.size()-1);
        String content = map.get("text").toString();
        try{

            int rc = json.getInt(KEY_RC);
            String text = json.optString(KEY_TEXT);
            //拒识效果展示
            if (rc == 4 && !TextUtils.isEmpty(text) && !text.matches("^[。？，！]")) {
                if(!content.equals(RC4_TIP)){
                    addTextToList(RC4_TIP, null, OTHER, null);
                }
            }

            if (!json.has(KEY_SERVICE)) {
                return null;
            }

            String service = json.optString(KEY_SERVICE);

            return new SemanticResult(service, json);
        }catch (JSONException e){
            if(Integer.parseInt(map.get("person").toString()) == ME){
                map.put("text",NO_CONTENT);
                mChatList.set(mChatList.size()-1, map);
                mAdapter.notifyDataSetChanged();
            }
        }

        return null;
    }

    /**
     * 处理云端返回结果
     * @param event
     */
    private void processResult(AIUIEvent event) {

        try {
            JSONObject bizParamJson = new JSONObject(event.info);
            JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
            JSONObject params = data.getJSONObject("params");
            JSONObject content = data.getJSONArray("content").getJSONObject(0);

            if (content.has("cnt_id")) {
                String cnt_id = content.getString("cnt_id");
                JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));
                String sub = params.optString("sub");
                JSONObject result = cntJson.optJSONObject("intent");

                Log.i(TAG, cntJson.toString());
                if ("nlp".equals(sub)) {
                    //在线语义结果
                    String text = result.optString("text");
                    for (int i = mChatList.size() - 1; i >= 0; i--) {
                        HashMap<String, Object> map = mChatList.get(i);
                        if (Integer.parseInt(map.get("person").toString()) == 1) {
                            if(map.containsKey("text")){
                                if(map.get("text").equals(NO_CONTENT) || map.get("text").equals(NO_SAY)){
                                    map.put("text",text);
                                }else{
                                    map.put("text",map.get("text") + text);
                                }
                            }else{
                                map.put("text",text);
                            }
                            mChatList.set(i, map);
                            mAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    handleResult(result);
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * AIUI监听器
     */
    private AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {
            switch (event.eventType) {
                //唤醒事件
                case AIUIConstant.EVENT_WAKEUP: {
                    mIsWakeup = true;
                }
                break;

                //状态改变事件
                case AIUIConstant.EVENT_STATE: {
                    if (AIUIConstant.STATE_READY == event.arg1) {
                        View v = mPressToSpeakBtn;
                        if(v.isPressed())
                            actionUp(v);
                    }
                }
                break;

                //休眠事件
                case AIUIConstant.EVENT_SLEEP: {
                    mIsWakeup = false;
                    Log.i(TAG,"sleep...");
                }
                break;

                //结果返回事件
                case AIUIConstant.EVENT_RESULT: {
                    processResult(event);
                }
                break;

                //错误返回事件
                case AIUIConstant.EVENT_ERROR: {
                    int errorCode = event.arg1;
                    showTip("errorCode=" + errorCode + ", des=" + event.info);
                }
                break;

                //VAD事件
                case AIUIConstant.EVENT_VAD: {
                    Log.i(TAG,event.arg1 + " vad os");
                    mIsSaidSomething = true;
                }
                break;

                default:
                    break;
            }
        }

    };

    /**
     * 读取参数配置
     * @return 配置参数
     */
    private String getAIUIParams() {
        String params = "";

        AssetManager assetManager = getResources().getAssets();
        try {
            InputStream ins = assetManager.open(PARAM_ASSET_PATH);
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return params;
    }

    @Override
    protected void onPause() {
        super.onPause();
        discardWrite();
        mRecordingContainer.setVisibility(View.INVISIBLE);
        PlayController.getInstance(mAIUIPlayer).pause();
    }

    /**
     * listview滑动监听listener
     */
    private class ListScrollListener implements OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:

                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

        }

    }


    /**
     * 初始化切换场景对话框
     */
    private void initDialog() {
        mChangeSceneDialog = new Dialog(this, R.style.SceneDialogStyle);
        //填充对话框的布局
        mDialogInflate = LayoutInflater.from(this).inflate(R.layout.scene_dialog, null);
        //初始化控件
        mMainSceneTv = (TextView) mDialogInflate.findViewById(R.id.main_scene);
        mGameSceneTv = (TextView) mDialogInflate.findViewById(R.id.game_scene);
        mMedicalSceneTv = (TextView) mDialogInflate.findViewById(R.id.medical_scene);

        mMainSceneTv.setOnClickListener(this);
        mGameSceneTv.setOnClickListener(this);
        mMedicalSceneTv.setOnClickListener(this);

        mChangeSceneDialog.setContentView(mDialogInflate);
        mChangeSceneDialog.setCanceledOnTouchOutside(true);
        Window dialogWindow = mChangeSceneDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;
        dialogWindow.setAttributes(lp);
    }

    /**
     * 初始化View
     */
    protected void initView() {
        mToast = Toast.makeText(ChatActivity.this, "", Toast.LENGTH_SHORT);
        mListView = (ListView) findViewById(R.id.list);

        mAdapter = new MyChatAdapter(this, mAIUIPlayer, mPlayImgHandler, mTipHandler,mChatList);

        mSetModeKeyboardBtn = findViewById(R.id.btn_set_mode_keyboard);
        mSetModeVoiceBtn = findViewById(R.id.btn_set_mode_voice);
        mEditTextContent = (PasteEditText) findViewById(R.id.et_sendmessage);
        mMicImg = (ImageView) findViewById(R.id.mic_image);
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mSwitchBtn = (ImageButton) findViewById(R.id.switch_btn);
        mReturnBtn.setOnClickListener(this);
        mSwitchBtn.setOnClickListener(this);
        initDialog();
        // 动画资源文件,用于录制语音时
        mMicRes = new Drawable[]{
                getResources().getDrawable(R.drawable.record_animate_01),
                getResources().getDrawable(R.drawable.record_animate_02),
                getResources().getDrawable(R.drawable.record_animate_03),
                getResources().getDrawable(R.drawable.record_animate_04),
                getResources().getDrawable(R.drawable.record_animate_05),
                getResources().getDrawable(R.drawable.record_animate_06),
                getResources().getDrawable(R.drawable.record_animate_07),
                getResources().getDrawable(R.drawable.record_animate_08),
                getResources().getDrawable(R.drawable.record_animate_09),
                getResources().getDrawable(R.drawable.record_animate_10),
                getResources().getDrawable(R.drawable.record_animate_11),
                getResources().getDrawable(R.drawable.record_animate_12),
                getResources().getDrawable(R.drawable.record_animate_13),
                getResources().getDrawable(R.drawable.record_animate_14),};

        mSendTxtBtn = findViewById(R.id.btn_send);
        mPressToSpeakBtn = findViewById(R.id.btn_press_to_speak);
        mPressToSpeakBtn.setOnTouchListener(new PressToSpeakListen());
        mRecordingHint = (TextView) findViewById(R.id.recording_hint);
        mEditTextLayout = (RelativeLayout) findViewById(R.id.edittext_layout);
        mEditTextLayout.setBackgroundResource(R.drawable.input_bar_bg_normal);

        mRecordingContainer = findViewById(R.id.recording_container);

        mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mEditTextLayout
                            .setBackgroundResource(R.drawable.input_bar_bg_active);
                } else {
                    mEditTextLayout
                            .setBackgroundResource(R.drawable.input_bar_bg_normal);
                }

            }
        });
        mEditTextContent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEditTextLayout
                        .setBackgroundResource(R.drawable.input_bar_bg_active);
            }
        });
        mListView.setAdapter(mAdapter);

        mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mListView.setOnScrollListener(new ListScrollListener());
        int count = mListView.getCount();
        if (count > 0) {
            mListView.setSelection(count - 1);
        }
    }

    /**
     * 发送按钮事件
     */
    private void sendBtnClicked() {
        String s = mEditTextContent.getText().toString();
        if (!s.trim().equals("")) {
            if (null != mAIUIAgent) {
                if (!mIsWakeup) {
                    // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
                    AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
                    mAIUIAgent.sendMessage(wakeupMsg);
                }

                AIUIMessage writeMsg = new AIUIMessage(AIUIConstant.CMD_WRITE,
                        0, 0, "data_type=text", s.trim().getBytes());
                mAIUIAgent.sendMessage(writeMsg);

                addTextToList(s.trim(), null, ME_TXT, null);
                mEditTextContent.setText("");
                playResult();
            }else {
                initAIUI();
                sendBtnClicked();
            }
        } else {
            Toast.makeText(ChatActivity.this, "发送内容不能为空", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 切换成场景按钮事件
     */
    private void changeSenceBtnClick() {
        mChangeSceneDialog.show();
    }

    /**
     * 消息图标点击事件
     *
     * @param view
     */
    @SuppressLint("NewApi")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_send:
                sendBtnClicked();
                break;
            case R.id.main_scene:
                changeScene(SCENE_MAIN);
                break;
            case R.id.game_scene:
                changeScene(SCENE_GAME);
                break;
            case R.id.medical_scene:
                changeScene(SCENE_MEDICAL);
                break;
            case R.id.return_btn:
                finish();
                break;
            case R.id.switch_btn:
                changeSenceBtnClick();
                break;
            default:
                break;
        }
    }

    /**
     * 动态切换业务场景
     * @param scene 场景名
     */
    private void changeScene(String scene) {
        JSONObject paramsJson = new JSONObject();
        JSONObject speechParamsJson = new JSONObject();
        try {
            speechParamsJson.put("scene", scene);
            paramsJson.put("global", speechParamsJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AIUIMessage setParamsMsg = new AIUIMessage(AIUIConstant.CMD_SET_PARAMS,
                0, 0, paramsJson.toString(), null);
        if (mAIUIAgent != null){
            mAIUIAgent.sendMessage(setParamsMsg);
        }else {
            initAIUI();
            changeScene(scene);
            return;
        }

        mChangeSceneDialog.dismiss();
    }

    /**
     * 显示语音图标按钮
     *
     * @param view
     */
    public void setModeVoice(View view) {
        hideKeyboard();
        mEditTextLayout.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        mSetModeKeyboardBtn.setVisibility(View.VISIBLE);
        mPressToSpeakBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 显示键盘图标
     * @param view
     */
    public void setModeKeyboard(View view) {

        mEditTextLayout.setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
        mSetModeVoiceBtn.setVisibility(View.VISIBLE);
        mEditTextContent.requestFocus();
        mPressToSpeakBtn.setVisibility(View.GONE);

    }

    /**
     * 获取录音保存路径
     * @param paramString 文件名
     * @return 路径
     */
    public String getVoiceFileName(String paramString) {
        Time localTime = new Time();
        localTime.setToNow();
        return paramString + "/" + localTime.toString().substring(0, 15) + ".wav";
    }

    /**
     * 按住说话抬起预处理
     */
    private void actionUp(View v) {

        HashMap<String, Object> map = mChatList.get(mChatList.size() - 1);
        if(map.get("text") == null){
            if(!mIsSaidSomething){
                map.put("text",NO_SAY);
                mChatList.set(mChatList.size() - 1, map);
                mAdapter.notifyDataSetChanged();
            }else{
                mResultTimeOutHandler.sendEmptyMessageDelayed(mChatList.size() - 1,2500);
            }
        }
        mIsSaidSomething = false;
        v.setPressed(false);
        mRecordingContainer.setVisibility(View.INVISIBLE);
        actionNormalUp();
    }

    /**
     * 按住说话按钮抬起
     */
    private void actionNormalUp() {
        if (mAudioFileSave != null) {
            mAudioCaptor.stop();
            int time = mAudioFileSave.stopWrite();
            if (time >= 1) {
                if (null != mAIUIAgent) {
                    String params = "data_type=audio,msc.lng=-1,msc.lat=-1,sample_rate=16000";
                    AIUIMessage stopWriteMsg = new AIUIMessage(AIUIConstant.CMD_STOP_WRITE, 0, 0, params, null);

                    mAIUIAgent.sendMessage(stopWriteMsg);
                }else {
                    initAIUI();
                    sendBtnClicked();
                    return;
                }

                for (int i = mChatList.size() - 1; i >= 0; i--) {
                    HashMap<String, Object> map = mChatList.get(i);
                    if (Integer.parseInt(map.get("person").toString()) == 1) {
                        map.put("voicetime", time + "s");
                        map.put("path", mAudioFileSave.getFilePath());
                        mChatList.set(i, map);
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            } else {
                mAudioFileSave.discardWrite();
                Toast.makeText(getApplicationContext(), "录音时间太短",
                        Toast.LENGTH_SHORT).show();
                for (int i = mChatList.size() - 1; i >= 0; i--) {
                    HashMap<String, Object> map = mChatList.get(i);
                    if (Integer.parseInt(map.get("person").toString()) == 1) {
                        for (int j = i; j < mChatList.size(); j++) {
                            Log.i(TAG, "remove at " + j);
                            mChatList.remove(j);
                        }
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
            mAudioFileSave = null;
            playResult();
        }
    }

    /**
     *  按住说话
     */
    private boolean actionDownStart(View v, MotionEvent event) {
        PlayController playController = PlayController.getInstance(mAIUIPlayer);
        if(playController.isPlayingMusic()){
            Message msg = new Message();
            msg.arg1 = playController.getPlayMusicPosition();
            msg.arg2 = R.drawable.play_btn;
            mPlayImgHandler.sendMessage(msg);
        }
        playController.pause();

        if (!CommonUtils.isExitsSdcard()) {
            Toast.makeText(ChatActivity.this, "发送语音需要sdcard支持！",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        addTextToList(null, "0", ME, "");
        try {
            v.setPressed(true);
            mRecordingContainer.setVisibility(View.VISIBLE);
            mRecordingHint.setText(getString(R.string.move_up_to_cancel));
            mRecordingHint.setBackgroundColor(Color.TRANSPARENT);
            if (!mIsWakeup) {
                AIUIMessage startMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP,
                        0, 0, null, null);
                if(null == mAIUIAgent){
                    initAIUI();
                }
                mAIUIAgent.sendMessage(startMsg);
            }
            mAudioFileSave = new AudioFileSave(getVoiceFileName(CommonUtils.getDirectory()));
            mAudioCaptor.start();
        } catch (Exception e) {
            e.printStackTrace();
            v.setPressed(false);
            discardWrite();
            mRecordingContainer.setVisibility(View.INVISIBLE);
            Toast.makeText(ChatActivity.this, R.string.recoding_fail, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * 按住说话listener
     */
    class PressToSpeakListen implements View.OnTouchListener {
        @SuppressLint({"ClickableViewAccessibility", "Wakelock"})
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return actionDownStart(v, event);
                case MotionEvent.ACTION_MOVE: {
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    actionUp(v);
                    return true;
                default:
                    pressDefault();
                    return false;
            }
        }
    }
    /**
     *  停止录音
     */
    private void pressDefault() {
        mRecordingContainer.setVisibility(View.INVISIBLE);
        discardWrite();
    }

    /**
     * 停止保存音频
     */
    private void discardWrite() {
        if (mAudioFileSave != null) {
            mAudioCaptor.stop();
            mAudioFileSave.discardWrite();
        }
    }



    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                mInputManager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 量化音量大小
     */
    private int calcVolum(int power) {
        int volume = 0;
        if (power <= 0) {
            volume = 0;
        } else if (power >= 0 && power < 2) {
            volume = 1;
        } else if (power >= 2 && power < 4) {
            volume = 2;
        } else if (power >= 4 && power < 6) {
            volume = 3;
        } else if (power >= 6 && power < 8) {
            volume = 4;
        } else if (power >= 8 && power < 10) {
            volume = 5;
        } else if (power >= 10 && power < 12) {
            volume = 6;
        } else if (power >= 12 && power < 14) {
            volume = 7;
        } else if (power >= 14 && power < 16) {
            volume = 8;
        } else if (power >= 16 && power < 18) {
            volume = 9;
        } else if (power >= 18 && power < 20) {
            volume = 10;
        } else if (power >= 20 && power < 22) {
            volume = 11;
        } else if (power >= 22 && power < 24) {
            volume = 12;
        } else {
            volume = 13;
        }
        return volume;
    }


}
