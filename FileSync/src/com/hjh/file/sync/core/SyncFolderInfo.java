package com.hjh.file.sync.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hjh.file.sync.process.IProcessListener;
import com.hjh.file.sync.process.SimpleProcessListener;

/**
 * 目录信息
 * 
 * @author 洪 qq:2260806429
 */
public class SyncFolderInfo {

	public static void main(String argv[]) throws IOException {
		final String path = FSConfig.TEST_PATH;
		final IProcessListener listener = new SimpleProcessListener();
		listener.print("test scan");
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

	public List<SyncItem> sync(SyncFolderInfo target) {
		List<SyncItem> ret = new ArrayList<SyncItem>();
		int maxSource = this.fileKeys.size();
		int maxTarget = target.fileKeys.size();
		for (int i = 0, j = 0; i < maxSource || j < maxTarget;) {
			String keysource = i >= maxSource ? null : this.fileKeys.get(i);
			String keytarget = j >= maxTarget ? null : target.fileKeys.get(j);
			if (keysource == null) {
				ret.add(new SyncItem(null, keytarget));
				j++;
			} else if (keytarget == null) {
				ret.add(new SyncItem(keysource, null));
				i++;
			} else {
				String pathsource = keysource.split(FSConfig.INFO_SEP)[FSConfig.NAME_INDEX];
				String pathtarget = keytarget.split(FSConfig.INFO_SEP)[FSConfig.NAME_INDEX];
				int cmp = pathsource.compareTo(pathtarget);
				if (0 == cmp) {
					if (!keysource.equals(keytarget)) {
						ret.add(new SyncItem(keysource, keytarget));
					}
					i++;
					j++;
				} else if (cmp > 0) {
					ret.add(new SyncItem(null, keytarget));
					j++;
				} else {
					ret.add(new SyncItem(keysource, null));
					i++;
				}
			}
		}
		return ret;
	}

	public SyncFolderInfo scan(IProcessListener listener) throws IOException {
		reset();
		if (!this.folder.exists()) {
			listener.updateTotalSize(0);
			return this;
		}
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
		String id = KeyGeneral.id(file.getAbsolutePath(), listener).substring(
				folder.getAbsolutePath().length());
		if (id.length() > 0) {
			fileKeys.add(id);
		}
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
