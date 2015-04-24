package com.hjh.file.sync.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

	public static String md5(byte[] datas, int pos, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(datas, pos, len);
			return toString(md);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static String toString(MessageDigest md) {
		byte b[] = md.digest();
		int i;
		StringBuffer buf = new StringBuffer("");
		for (int offset = 0; offset < b.length; offset++) {
			i = b[offset];
			if (i < 0)
				i += 256;
			if (i < 16)
				buf.append("0");
			buf.append(Integer.toHexString(i));
		}
		return buf.toString();
	}

}
