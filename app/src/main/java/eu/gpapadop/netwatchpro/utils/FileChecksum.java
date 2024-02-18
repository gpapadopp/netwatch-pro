package eu.gpapadop.netwatchpro.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileChecksum {
    public static String calculateMD5(String filePath) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (FileInputStream fis = new FileInputStream(filePath);
             DigestInputStream dis = new DigestInputStream(fis, md)) {

            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {}
        }

        byte[] mdBytes = md.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte mdByte : mdBytes) {
            hexString.append(Integer.toHexString(0xFF & mdByte));
        }
        return hexString.toString();
    }
}
