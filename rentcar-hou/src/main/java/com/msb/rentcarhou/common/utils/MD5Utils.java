package com.msb.rentcarhou.common.utils;

import org.springframework.util.DigestUtils;

public class MD5Utils {
    
    /**
     * 对字符串进行 MD5 加密
     * @param str 待加密字符串
     * @return 32位小写MD5字符串
     */
    public static String encrypt(String str) {
        if (str == null) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(str.getBytes());
    }
}
