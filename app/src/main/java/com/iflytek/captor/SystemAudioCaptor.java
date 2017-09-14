package com.iflytek.captor;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.record.PcmRecorder;
import com.iflytek.cloud.record.PcmRecorder.PcmRecordListener;

import android.util.Log;

public class SystemAudioCaptor extends DataCaptor implements PcmRecordListener {
	private static final String TAG = "SystemAudioCaptor";
	
	private int sample_rate = 16000;
	
	private PcmRecorder mPcmRecorder;
	
	public SystemAudioCaptor(DataCaptureListener listener) {
		super(listener);
	}
	
	private void createPcmRecorder() {
		sample_rate = 16000;
		mPcmRecorder = new PcmRecorder(sample_rate, PcmRecorder.READ_INTERVAL40MS);
	}


	@Override
	public int start() {
		if (isStarted()) {
			return AIUIConstant.SUCCESS;
		}
		
		createPcmRecorder();
		
		if (null != mPcmRecorder) {
			try {
				mPcmRecorder.startRecording(SystemAudioCaptor.this);
			} catch (SpeechError e) {
				e.printStackTrace();
				
				int error = e.getErrorCode();
				Log.e(TAG, "SingleAudioCaptor start error, error=" + error);
				
				return error;
			}
		}
		
		return SUCCESS;
	}

	@Override
	public void stop() {
		if (!isStarted()) {
			return;
		}
		
		if (null != mPcmRecorder) {
			mPcmRecorder.stopRecord(true);
		}
	}

	@Override
	public void release() {
		stop();
		
		if (null != mPcmRecorder) {
			mPcmRecorder = null;
			mIsReleased = true;
			
			if (null != mCaptureListener) {
				mCaptureListener.onCaptorReleased();
			}
		}
		
	}

	@Override
	public void onError(SpeechError error) {
		if (null != mCaptureListener) {
			mCaptureListener.onError(error.getErrorCode(), error.getErrorDescription());
		}
		
		Log.e(TAG, "SingleAudioCaptor error, error=" + error.getErrorCode());
	}

	@Override
	public void onRecordBuffer(byte[] buffer, int offset, int length) {
		if (null != mCaptureListener) {
			// 必须将音频数据复制一份，否则会出现问题
			byte[] audio = new byte[length];
			System.arraycopy(buffer, offset, audio, 0, length);
			
			mCaptureListener.onData(audio, length, null);
		}
	}

	@Override
	public void onRecordReleased() {
		mIsStarted = false;
		if (null != mCaptureListener) {
			mCaptureListener.onCaptureStopped();
		}
		
	}

	@Override
	public void onRecordStarted(boolean success) {
		if (success) {
			mIsStarted = true;
			if (null != mCaptureListener) {
				mCaptureListener.onCaptureStarted();
			}
		}
	}

	@Override
	public int getSampleRate() {
		return sample_rate;
	}

}
