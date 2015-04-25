package com.hjh.file.sync.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import com.hjh.file.sync.util.LogHelper;

/**
 * 
 * 属性配置
 * 
 * @author 洪 qq:2260806429
 * 
 */
public class FSConfig {

	public static final String TEST_PATH = "E:\\testsync_source";

	public static final String CACHE_ID_FILE = ".hsync";

	public static final long BLOCK_SIZE = 1024 * 1024 * 10; // 10M的块大小

	public final static String INFO_SEP = ":";
	public final static String CONTENT_SEP = ";";

	public final static int FOLDER_INFO_LEN = 2;
	public final static int NAME_INDEX = 0;
	public final static int TIME_INDEX = 1;
	public final static int SIZE_INDEX = 2;
	public final static int CONTENT_INDEX = 3;

	public static boolean isCompareBinary() { // 是否二进制比较，否则时间比较
		return false;
	}

	public static boolean isIgnore(File file) { // 文件是否忽略
		if (file.getName().equals(CACHE_ID_FILE)) {
			return true;
		}
		return false;
	}

	private static File cacheFile(String path, File target) {
		try {
			return new File(new File(target, CACHE_ID_FILE), UUID
					.nameUUIDFromBytes(path.getBytes("utf-8")).toString());
		} catch (UnsupportedEncodingException e) {
			LogHelper.error(e);
		}
		throw new RuntimeException("生成缓存路径失败:" + path);
	}

	public static String getCache(String path, File target) throws IOException {
		File cacheFile = cacheFile(path, target);
		if (cacheFile.isFile()) {
			FileInputStream in = new FileInputStream(cacheFile);
			byte[] cache = new byte[(int) cacheFile.length()];
			try {
				in.read(cache);
			} finally {
				in.close();
			}
			return new String(cache);
		}
		return null;
	}

	public static void cache(String id, File target) throws IOException {
		String[] arr = id.split(INFO_SEP);
		long size = Long.parseLong(arr[SIZE_INDEX]);
		if (size > BLOCK_SIZE * 10) {
			String path = arr[NAME_INDEX];
			String content = arr[CONTENT_INDEX];
			File save = cacheFile(path, target);
			if (!save.getParentFile().exists()) {
				save.getParentFile().mkdir();
			}
			FileOutputStream out = new FileOutputStream(save);
			try {
				out.write(content.getBytes());
			} finally {
				out.close();
			}
		}
	}

}
