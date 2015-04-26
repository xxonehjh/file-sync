package com.hjh.file.sync.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.hjh.file.sync.process.IProcessListener;
import com.hjh.file.sync.process.SimpleProcessListener;
import com.hjh.file.sync.util.LogHelper;
import com.hjh.file.sync.util.MD5;

/**
 * 文件指纹计算
 * 
 * @author 洪 qq:2260806429
 */
public class KeyGeneral {

	public static void main(String argv[]) throws IOException {
		final String path = "G:\\disk";
		// FSConfig.TEST_PATH;
		for (File item : new File(path).listFiles()) {
			if (item.isFile()) {
				genId(item.getAbsolutePath(), new File(path));
			}
		}
	}

	private static void genId(String path, File root) throws IOException {
		final IProcessListener listener = new SimpleProcessListener(null,
				new File(path).isDirectory() ? 0 : new File(path).length());
		listener.print("gen id " + new File(path).getName());
		String id = id(path, listener, root, true);
		LogHelper.info("ID:" + id);
	}

	public static String id(String filePath, IProcessListener listener,
			File root, boolean fromcache) throws IOException {
		File file = new File(filePath);
		StringBuffer idBuf = new StringBuffer();
		try {
			idBuf.append(file.getCanonicalPath());
		} catch (IOException e) {
			throw new RuntimeException("获取路径失败:" + filePath, e);
		}
		idBuf.append(FSConfig.INFO_SEP);
		idBuf.append(file.lastModified());
		boolean tocache = false;
		if (file.isFile()) {
			idBuf.append(FSConfig.INFO_SEP);
			idBuf.append(file.length());
			idBuf.append(FSConfig.INFO_SEP);
			String key = null;
			if (fromcache) {
				key = FSConfig.getCache(
						filePath.substring(root.getAbsolutePath().length()),
						root);
				if (null != key) {
					listener.work(file.length());
				}
			}
			if (null == key) {
				key = key(filePath, listener);
				tocache = null != key;
			}
			if (null == key) {
				return null;
			}
			idBuf.append(key);
		}
		String id = idBuf.toString().substring(root.getAbsolutePath().length());
		if (tocache) {
			FSConfig.cache(id, root);
		}
		return id;
	}

	public static String key(String filePath, IProcessListener listener)
			throws IOException {

		StringBuffer keyBuf = new StringBuffer();
		File file = new File(filePath);
		FileInputStream in = new FileInputStream(file);
		byte[] cache = new byte[(int) FSConfig.BLOCK_SIZE];
		try {
			int len = 0;
			while (true) {
				if (listener.isCancel()) {
					return null;
				}
				len = in.read(cache);
				if (len <= 0) {
					break;
				}
				keyBuf.append(FSConfig.CONTENT_SEP);
				keyBuf.append(MD5.md5(cache, 0, len));
				listener.work(len);
			}
		} finally {
			in.close();
		}

		byte[] allKeys = keyBuf.toString().getBytes();
		String sumkey = MD5.md5(allKeys, 0, allKeys.length);

		return sumkey + keyBuf.toString();
	}

}
