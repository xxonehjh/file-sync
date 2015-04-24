package com.hjh.file.sync.process;

public class ProcessPrinter {

	public void start(final IProcessListener listener) {
		new Thread() {
			public void run() {
				while (!listener.isFinish()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("work:"
							+ String.format("%5.2f", listener.getPercent())
							+ "%");
				}
			}
		}.start();
	}

}
