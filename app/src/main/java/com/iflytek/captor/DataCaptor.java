package com.iflytek.captor;

public abstract class DataCaptor {
	public static final int SUCCESS = 0;
	
	public static final int FAIL = -1;
	
	protected boolean mIsStarted;
	
	protected boolean mIsReleased;
	
	protected DataCaptureListener mCaptureListener;
	
	public DataCaptor(DataCaptureListener listener) {
		mCaptureListener = listener;
	}
	
	/**
	 * 获取采样率。
	 */
	public abstract int getSampleRate();
	
	/**
	 * 打开采集器，开始采集数据。
	 */
	public abstract int start();
	
	/**
	 * 关闭采集器，停止采集数据。
	 */
	public abstract void stop();
	
	/**
	 * 释放数据采集对象。
	 */
	public abstract void release();
	
	/**
	 * 是否已经开始采集。
	 */
	public boolean isStarted() {
		return mIsStarted;
	}
	
	/**
	 * 是否已经释放。
	 */
	public boolean isReleased() {
		return mIsReleased;
	}
	
}
