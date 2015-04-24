package com.hjh.file.sync.process;



public class SimpleProcessListener implements IProcessListener{

	private long totalsize;
	private long worksize = 0;
	
	public SimpleProcessListener(){
		this(0);
	}
	
	public SimpleProcessListener(long totalSize){
		this.totalsize = totalSize;
	}

	@Override
	public void work(int size) {
		worksize += size;
	}

	@Override
	public boolean isFinish() {
		return totalsize == worksize;
	}

	@Override
	public double getPercent() {
		return ((int) (worksize * 1.0 / totalsize * 10000)) * 1.0 / 100;
	}

	@Override
	public boolean isCancel() {
		return false;
	}

	@Override
	public void updateTotalSize(long totalSize) {
		this.totalsize = totalSize;
	}

}
