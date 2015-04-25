package com.hjh.file.sync.main;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.hjh.file.sync.core.FSConfig;
import com.hjh.file.sync.core.SyncFolderInfo;
import com.hjh.file.sync.core.SyncItem;
import com.hjh.file.sync.process.CancelControl;
import com.hjh.file.sync.process.IProcessListener;
import com.hjh.file.sync.process.SimpleProcessListener;
import com.hjh.file.sync.util.LogHelper;

/**
 * 程序入口
 * @author 洪 qq:2260806429
 */
public class Main {

	public static void main(String argv[]) throws IOException {

		File sourceFile = new File(FSConfig.TEST_PATH);
		File targetFile = new File("E:\\testsync");

		if (argv.length == 2) {
			sourceFile = new File(argv[0]);
			targetFile = new File(argv[1]);
		}

		if (!sourceFile.exists()) {
			throw new RuntimeException("源对象不存在:" + sourceFile.getAbsolutePath());
		}

		long start = System.currentTimeMillis();
		try {
			sync(sourceFile, targetFile);
		} finally {
			long cost = System.currentTimeMillis() - start;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			LogHelper.info("同步完成耗时:" + printCostTime(cost));
		}
	}

	public static String printCostTime(long cost) {
		if (cost < 1000) {
			return cost + "毫秒";
		}
		cost = cost / 1000;
		if (cost < 60) {
			return cost + "秒";
		}
		if (cost / 60 < 10) {
			return cost / 60 + "分" + cost % 60 + "秒";
		}
		return cost / 60 / 60 + "小时" + cost / 60 % 60 + "分" + cost % 60 % 60
				+ "秒";
	}

	public static void sync(File sourceFile, File targetFile)
			throws IOException {
		final CancelControl cancelControl = new CancelControl();
		LogHelper.info("Sync from \"" + sourceFile.getAbsolutePath()
				+ "\" TO \"" + targetFile.getAbsolutePath() + "\"");
		final IProcessListener listener_source = new SimpleProcessListener(
				cancelControl);
		listener_source.print("source scan");
		final SyncFolderInfo source = new SyncFolderInfo(sourceFile);

		final IProcessListener listener_target = new SimpleProcessListener(
				cancelControl);
		listener_target.print("target scan");
		final SyncFolderInfo target = new SyncFolderInfo(targetFile);

		new Thread() {
			public void run() {
				try {
					source.scan(listener_source);
				} catch (IOException e) {
					LogHelper.error(e);
					cancelControl.cancel = true;
				}
			}
		}.start();

		new Thread() {
			public void run() {
				try {
					target.scan(listener_target);
				} catch (IOException e) {
					LogHelper.error(e);
					cancelControl.cancel = true;
				}
			}
		}.start();

		while (!listener_source.isFinish() || !listener_target.isFinish()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LogHelper.error(e);
			}
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			LogHelper.error(e);
		}

		long size = 0;
		List<SyncItem> items = source.sync(target);
		for (SyncItem item : items) {
			size += item.getSyncSize();
		}
		LogHelper.info("total sync size:"
				+ (size * 100 / (1024 * 1024) * 1.0 / 100) + "M");
		if (!targetFile.exists()) {
			targetFile.mkdir();
		}
		if (!targetFile.exists()) {
			throw new RuntimeException("创建目标文件夹失败");
		}

		// TODO 优化，先同步目录 ， 再多线程同步文件
		final IProcessListener listener_sync = new SimpleProcessListener(
				cancelControl, size);
		listener_sync.print("sync");
		for (SyncItem item : items) {
			if (listener_sync.isCancel()) {
				break;
			}
			item.sync(sourceFile, targetFile, listener_sync);
		}

	}

}
