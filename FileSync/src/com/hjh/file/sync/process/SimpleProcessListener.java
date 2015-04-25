package com.hjh.file.sync.process;

/**
 * @author 洪 qq:2260806429
 */
public class SimpleProcessListener implements IProcessListener {

	private String name = "";
	private long totalsize;
	private long worksize = 0;

	public SimpleProcessListener() {
		this(0);
	}

	public SimpleProcessListener(long totalSize) {
		this.totalsize = totalSize;
	}

	@Override
	public void work(int size) {
		worksize += size;
	}

	@Override
	public boolean isFinish() {
		return totalsize == worksize || isCancel();
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
		touchPrint();
	}

	@Override
	public void print(String name) {
		this.name = name;
		touchPrint();
	}

	private void touchPrint() {
		if (null != name && 0 != name.length() && 0 != this.totalsize) {
			new ProcessPrinter(name).start(this);
		}
	}

}
