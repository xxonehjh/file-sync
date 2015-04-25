package com.hjh.file.sync.process;

/**
 * @author 洪 qq:2260806429
 */
public class SimpleProcessListener implements IProcessListener {

	private CancelControl cancelControl;
	private String name = "";
	private long totalsize;
	private long worksize = 0;

	public SimpleProcessListener() {
		this(null);
	}

	public SimpleProcessListener(CancelControl cancelControl) {
		this(cancelControl, 0);
	}

	public SimpleProcessListener(CancelControl cancelControl, long totalSize) {
		this.cancelControl = cancelControl;
		this.totalsize = totalSize;
	}

	@Override
	public void work(int size) {
		worksize += size;
	}

	@Override
	public boolean isFinish() {
		return (totalsize != 0 && totalsize == worksize) || isCancel();
	}

	@Override
	public double getPercent() {
		return ((int) (worksize * 1.0 / totalsize * 10000)) * 1.0 / 100;
	}

	@Override
	public boolean isCancel() {
		return cancelControl == null ? false : cancelControl.isCancel();
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
