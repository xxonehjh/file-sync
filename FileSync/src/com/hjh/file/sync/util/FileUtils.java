package com.hjh.file.sync.util;

import java.io.File;

public class FileUtils {

	public static void del(File file) {
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			File list[] = file.listFiles();
			if (null != list) {
				for (File item : list) {
					del(item);
				}
			}
		}
		if (!file.delete()) {
			LogHelper.warn("删除文件失败:" + file.getAbsolutePath());
		}
	}

}
