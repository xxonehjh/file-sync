package com.hjh.file.sync.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.hjh.file.sync.process.IProcessListener;
import com.hjh.file.sync.process.ProcessPrinter;
import com.hjh.file.sync.process.SimpleProcessListener;
import com.hjh.file.sync.util.MD5;

/**
 * 文件指纹计算
 * 
 * @author 洪 qq:2260806429
 */
public class KeyGeneral {

	public static void main(String argv[]) throws IOException {
		final String path = FSConfig.TEST_PATH;
		final IProcessListener listener = new SimpleProcessListener(new File(
				path).isDirectory() ? 0 : new File(path).length());
		new ProcessPrinter().start(listener);
		System.out.println(id(path, listener));
	}

	public static String id(String filePath, IProcessListener listener)
			throws IOException {
		File file = new File(filePath);
		StringBuffer idBuf = new StringBuffer();
		try {
			idBuf.append(file.getCanonicalPath());
		} catch (IOException e) {
			throw new RuntimeException("获取路径失败:" + filePath, e);
		}
		idBuf.append(":");
		idBuf.append(file.lastModified());
		idBuf.append(":");
		idBuf.append(file.isDirectory() ? 0 : file.length());
		if (file.isFile()) {
			idBuf.append(":");
			String key = key(filePath, listener);
			if (null == key) {
				return null;
			}
			idBuf.append(key);
		}
		return idBuf.toString();
	}

	public static String key(String filePath, IProcessListener listener)
			throws IOException {

		StringBuffer keyBuf = new StringBuffer();
		File file = new File(filePath);
		FileInputStream in = new FileInputStream(file);
		byte[] cache = new byte[FSConfig.BLOCK_SIZE];
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
				keyBuf.append(";");
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
