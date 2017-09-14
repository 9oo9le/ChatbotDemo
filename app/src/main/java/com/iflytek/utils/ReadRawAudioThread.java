package com.iflytek.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.util.Log;

/**
 * 读原始音频线程。
 * 
 * @author hj
 * @date 2016年7月2日 下午9:15:55 
 *
 */
public class ReadRawAudioThread extends Thread {
	private static final String TAG = "ReadRawAudio";
	
	private String mRawPcmPath = "";
	
	private boolean mStopRun = false;
	
	private boolean mRepeatTest = false;
	
	private RawAudioListener mAudioListener;
	
	private int mReadLen = 1280;
	
	public interface RawAudioListener {
		public void onAudio(byte[] audio);
	}
	
	public ReadRawAudioThread(String rawAudioPath) {
		super("ReadRawAudioThread");
		
		mRawPcmPath = rawAudioPath;
	}
	
	public void setReadLen(int readLen) {
		mReadLen = readLen;
	}
	
	public void setRepeatTest(boolean repeat) {
		mRepeatTest = repeat;
	}
	
	public void stopRun() {
		interrupt();
		mStopRun = true;
	}
	
	public void setAudioListener(RawAudioListener listener) {
		mAudioListener = listener;
	}
	
	@Override
	public void run() {
		super.run();
		
		File pcmFile = new File(mRawPcmPath);
		if (!pcmFile.exists()) {
			Log.e(TAG, "raw pcm file doesn't exist.");
			return;
		}
		
		FileInputStream fis = null;
		
		while (!mStopRun) {
			if (null == fis) {
				try {
					fis = new FileInputStream(pcmFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			byte[] audio = new byte[mReadLen];
			try {
				int readLen = fis.read(audio);
				if (mReadLen != readLen) {
					if (mRepeatTest) {
						fis.close();
						fis = null;
					} else {
						if (null != mAudioListener) {
							mAudioListener.onAudio(new byte[0]);
						}
						
						break;
					}
				} else {
					if (null != mAudioListener) {
						mAudioListener.onAudio(audio);
					}
				}
				
				try {
					if (49152 == mReadLen) {
						sleep(96 - 10);
					} if (12288 == mReadLen) {
						sleep(24 - 8);
					} if (1280 == mReadLen) {
						sleep(40);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (null != fis) {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
