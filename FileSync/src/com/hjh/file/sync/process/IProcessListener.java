package com.hjh.file.sync.process;

/**
 * 
 * 进度监听
 * 
 * @author 洪 qq:2260806429
 * 
 */
public interface IProcessListener {

	public void updateTotalSize(long totalSize);

	public void work(long size);

	public boolean isFinish();

	public boolean isCancel();

	public double getPercent();

	public void print(String name);

}
