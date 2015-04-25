package com.hjh.file.sync.process;

/**
 * @author 洪 qq:2260806429
 */
class ProcessPrinter {

	public boolean done;
	public String name;

	public ProcessPrinter(String name) {
		this.name = name;
	}

	public void start(final IProcessListener listener) {
		new Thread() {
			public void run() {
				done = false;
				try {
					do {
						System.out.println(name + " work:"
								+ String.format("%5.2f", listener.getPercent())
								+ "%");
						if (listener.isFinish()) {
							break;
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} while (true);
				} finally {
					done = true;
				}
			}
		}.start();
	}

}
