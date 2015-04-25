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
	
	public static final String TEST_PATH = "E:\\testsync_source";

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
		return false;
	}

}
