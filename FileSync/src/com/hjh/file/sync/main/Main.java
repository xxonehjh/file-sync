package com.hjh.file.sync.main;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.hjh.file.sync.core.FSConfig;
import com.hjh.file.sync.core.SyncFolderInfo;
import com.hjh.file.sync.core.SyncItem;
import com.hjh.file.sync.process.IProcessListener;
import com.hjh.file.sync.process.SimpleProcessListener;
import com.hjh.file.sync.util.LogHelper;

/**
 * @author 洪 qq:2260806429
 */
public class Main {

	public static void main(String argv[]) throws IOException {
		final File sourceFile = new File(FSConfig.TEST_PATH);
		final File targetFile = new File("E:\\testsync");
		long start = System.currentTimeMillis();
		try {
			sync(sourceFile, targetFile);
		} finally {
			LogHelper.info("耗时:"
					+ printCostTime(System.currentTimeMillis() - start));
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
		LogHelper.info("Sync from \"" + sourceFile.getAbsolutePath()
				+ "\" TO \"" + targetFile.getAbsolutePath() + "\"");
		final IProcessListener listener_source = new SimpleProcessListener();
		listener_source.print("source scan");
		SyncFolderInfo source = new SyncFolderInfo(sourceFile)
				.scan(listener_source);
		final IProcessListener listener_target = new SimpleProcessListener();
		listener_target.print("target scan");
		SyncFolderInfo target = new SyncFolderInfo(targetFile)
				.scan(listener_target);
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
		final IProcessListener listener_sync = new SimpleProcessListener(size);
		listener_sync.print("sync");
		for (SyncItem item : items) {
			if (listener_sync.isCancel()) {
				break;
			}
			item.sync(sourceFile, targetFile, listener_sync);
		}
		LogHelper.info("同步完成");
	}

}
