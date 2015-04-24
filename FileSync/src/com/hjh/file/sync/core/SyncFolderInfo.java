package com.hjh.file.sync.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hjh.file.sync.process.IProcessListener;
import com.hjh.file.sync.process.ProcessPrinter;
import com.hjh.file.sync.process.SimpleProcessListener;

/**
 * 目录信息
 * 
 * @author 洪 qq:2260806429
 */
public class SyncFolderInfo {

	public static void main(String argv[]) throws IOException {
		final String path = FSConfig.TEST_PATH;
		final IProcessListener listener = new SimpleProcessListener(1);
		new ProcessPrinter().start(listener);
		System.out.println(new SyncFolderInfo(new File(path)).scan(listener)
				.printInfo());
	}

	private File folder;
	private long folderCount;
	private long fileCount;
	private long dataSize;
	private List<String> fileKeys;

	public SyncFolderInfo(File folder) {
		this.folder = folder;
	}

	private void reset() {
		folderCount = 0;
		fileCount = 0;
		dataSize = 0;
		fileKeys = new ArrayList<String>();
	}

	public String printInfo() {
		StringBuffer buf = new StringBuffer();
		buf.append(String.format("folder %d file %d dataSize %d \r\n",
				folderCount, fileCount, dataSize));
		for (String item : fileKeys) {
			buf.append(item);
			buf.append("\r\n");
		}
		return buf.toString();
	}

	public SyncFolderInfo scan(IProcessListener listener) throws IOException {
		reset();
		count(folder, listener);
		if (listener.isCancel()) {
			return this;
		}
		listener.updateTotalSize(dataSize);
		key(folder, listener);
		Collections.sort(fileKeys);
		return this;
	}

	private void key(File file, IProcessListener listener) throws IOException {
		if (FSConfig.isIgnore(file)) {
			return;
		}
		fileKeys.add(KeyGeneral.id(file.getAbsolutePath(), listener));
		if (file.isDirectory()) {
			if (listener.isCancel()) {
				return;
			}
			File[] list = file.listFiles();
			if (null != list) {
				for (File item : list) {
					key(item, listener);
					if (listener.isCancel()) {
						return;
					}
				}
			}
		}
	}

	private void count(File file, IProcessListener listener) {
		if (FSConfig.isIgnore(file)) {
			return;
		}
		if (file.isDirectory()) {
			folderCount++;
			if (listener.isCancel()) {
				return;
			}
			File[] list = file.listFiles();
			if (null != list) {
				for (File item : list) {
					count(item, listener);
					if (listener.isCancel()) {
						return;
					}
				}
			}
		} else {
			fileCount++;
			dataSize += file.length();
		}
	}

}
