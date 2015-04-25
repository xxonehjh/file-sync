package com.hjh.file.sync.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.hjh.file.sync.process.IProcessListener;
import com.hjh.file.sync.util.LogHelper;

/**
 * 同步项
 * 
 * @author 洪 qq:2260806429
 */
public class SyncItem {

	private File targetFile;
	public String from;
	public String to;

	public SyncItem(String from, String to) {
		this.from = from;
		this.to = to;
	}

	public long getSyncSize() {
		if (isDel()) {
			return 0;
		}
		String[] fromArr = from.split(FSConfig.INFO_SEP);
		long fromSize = fromArr.length == FSConfig.FOLDER_INFO_LEN ? 0 : Long
				.parseLong(fromArr[FSConfig.SIZE_INDEX]);
		if (isCopy() || 0 == fromSize) {
			return fromSize;
		}
		long size = 0;
		String[] fromContent = fromArr[FSConfig.CONTENT_INDEX]
				.split(FSConfig.CONTENT_SEP);
		String[] toContent = to.split(FSConfig.INFO_SEP)[FSConfig.CONTENT_INDEX]
				.split(FSConfig.CONTENT_SEP);
		for (int i = 1; i < fromContent.length; i++) {
			if (i < toContent.length) {
				if (toContent[i].equals(fromContent[i])) {
					continue;
				}
			}
			if (i == fromContent.length - 1) {
				size += (fromSize - FSConfig.BLOCK_SIZE * (i - 1));
			} else {
				size += FSConfig.BLOCK_SIZE;
			}
		}
		return size;
	}

	public String toString() {
		if (isDel()) {
			return "del:" + to;
		}
		if (isCopy()) {
			return "add:" + from;
		}
		return "update:" + from + ":" + to;
	}

	public boolean isDel() {
		return null == from;
	}

	public boolean isCopy() {
		return null == to;
	}

	public boolean isUpdate() {
		return !isDel() && !isCopy();
	}

	public void sync(File sourceFile, File targetFile,
			IProcessListener listener_sync) throws IOException {
		this.targetFile = targetFile;
		if (listener_sync.isCancel()) {
			return;
		}
		if (isDel()) {
			del(toPath(targetFile), listener_sync);
		} else if (isCopy()) {
			copy(fromPath(sourceFile), toPath(targetFile), listener_sync);
		} else {
			update(fromPath(sourceFile), toPath(targetFile), listener_sync);
		}
	}

	private String fromPath(File sourceFile) {
		return sourceFile.getAbsolutePath()
				+ from.split(FSConfig.INFO_SEP)[FSConfig.NAME_INDEX];
	}

	private String toPath(File targetFile) {
		return targetFile.getAbsolutePath()
				+ (to == null ? from : to).split(FSConfig.INFO_SEP)[FSConfig.NAME_INDEX];
	}

	private void del(String path, IProcessListener listener_sync) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			File list[] = file.listFiles();
			if (null != list) {
				for (File item : list) {
					if (listener_sync.isCancel()) {
						return;
					}
					del(item.getAbsolutePath(), listener_sync);
				}
			}
		}
		if (listener_sync.isCancel()) {
			return;
		}
		if (!file.delete()) {
			LogHelper.warn("删除文件失败:" + file.getAbsolutePath());
		}
	}

	private void copy(String fromPath, String toPath,
			IProcessListener listener_sync) throws IOException {
		if (listener_sync.isCancel()) {
			return;
		}
		File from = new File(fromPath);
		File to = new File(toPath);
		if (!from.exists()) {
			LogHelper.warn("文件不存在:" + from.getAbsolutePath());
			return;
		}
		if (from.isDirectory()) {
			if (to.isFile()) {
				to.delete();
			}
			if (!to.isDirectory()) {
				to.mkdir();
			}
			to.setLastModified(from.lastModified());
		} else {
			if (to.isDirectory()) {
				del(toPath, listener_sync);
			}
			FileInputStream in = new FileInputStream(from);
			FileOutputStream out = new FileOutputStream(to);
			byte[] cache = new byte[(int) FSConfig.BLOCK_SIZE];
			try {
				int len = 0;
				while (true) {
					if (listener_sync.isCancel()) {
						return;
					}
					len = in.read(cache);
					if (len <= 0) {
						break;
					}
					out.write(cache, 0, len);
					listener_sync.work(len);
				}
				out.close();
				out = null;
				to.setLastModified(from.lastModified());
				FSConfig.cache(this.from, targetFile);
			} finally {
				in.close();
				if (null != out) {
					out.close();
				}
			}
		}
	}

	private void update(String fromPath, String toPath,
			IProcessListener listener_sync) throws IOException {
		if (listener_sync.isCancel()) {
			return;
		}
		File from = new File(fromPath);
		File to = new File(toPath);
		if (!from.exists()) {
			LogHelper.warn("文件不存在:" + from.getAbsolutePath());
			return;
		}
		if (from.isDirectory()) {
			if (to.isFile()) {
				to.delete();
			}
			if (!to.isDirectory()) {
				to.mkdir();
			}
			to.setLastModified(from.lastModified());
		} else {
			if (to.isDirectory()) {
				del(toPath, listener_sync);
			}
			RandomAccessFile in = new RandomAccessFile(from, "r");
			RandomAccessFile out = new RandomAccessFile(to, "rw");
			byte[] cache = new byte[(int) FSConfig.BLOCK_SIZE];
			try {
				int len = 0;
				String[] fromArr = this.from.split(FSConfig.INFO_SEP);
				String[] fromContent = fromArr[FSConfig.CONTENT_INDEX]
						.split(FSConfig.CONTENT_SEP);
				String[] toContent = this.to.split(FSConfig.INFO_SEP)[FSConfig.CONTENT_INDEX]
						.split(FSConfig.CONTENT_SEP);
				for (int i = 1; i < fromContent.length; i++) {
					if (i < toContent.length) {
						if (toContent[i].equals(fromContent[i])) {
							continue;
						}
					}
					long pos = FSConfig.BLOCK_SIZE * (i - 1);
					in.seek(pos);
					len = in.read(cache);
					out.seek(pos);
					out.write(cache, 0, len);
					listener_sync.work(len);
				}
				out.setLength(from.length());
				out.close();
				out = null;
				to.setLastModified(from.lastModified());
				FSConfig.cache(this.from, targetFile);
			} finally {
				in.close();
				if (null != out) {
					out.close();
				}
			}
		}
	}

}