package com.iflytek.captor;

import android.os.Bundle;

public interface DataCaptureListener {
	/**
	 * 开始数据采集回调。
	 */
	public void onCaptureStarted();
	
	/**
	 * 采集回调，抛出数据。
	 * 
	 * @param data 数据内容
	 * @param dataLen 数据长度
	 * @param des 数据描述，如：采样率，分辨率等信息
	 */
	public void onData(byte[] data, int dataLen, Bundle des);
	
	/**
	 * 停止采集回调。
	 */
	public void onCaptureStopped();
	
	/**
	 * 数据采集器释放回调。
	 */
	public void onCaptorReleased();
	
	/**
	 * 出错回调。
	 * 
	 * @param error 错误编号
	 * @param des 描述信息
	 */
	public void onError(int error, String des);
	
}
