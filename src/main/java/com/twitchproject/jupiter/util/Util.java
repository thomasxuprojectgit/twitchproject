package com.twitchproject.jupiter.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;

/**
 * Help encrypt the user password before save to the database
 */
public class Util {

    public static String encryptPassword(String userId, String password) throws IOException {
        return DigestUtils.md5Hex(userId + DigestUtils.md5Hex(password)).toLowerCase();
    }
}
