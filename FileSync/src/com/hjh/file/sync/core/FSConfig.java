package com.hjh.file.sync.core;

import java.io.File;

/**
 * 
 * 属性配置
 * 
 * @author 洪 qq:2260806429
 * 
 */
public class FSConfig {
	
	public static final String TEST_PATH = "E:\\hjh\\KuaipanDisk\\21.软件\\02.装机必备";

	public static final int BLOCK_SIZE = 1024 * 1024 * 10; // 10M的块大小

	public static boolean isCompareBinary() { // 是否二进制比较，否则时间比较
		return false;
	}

	public static boolean isIgnore(File file) { // 文件是否忽略
		return false;
	}

}
