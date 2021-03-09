package com.rearcam.receive.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;


/**
 * @author liubp
 * @function 检测工具类
 */
public class FileUtil {

    public static String getFileName(String url) {
        String filename = null;
        if (url != null || !"".equals(url)) {
            filename = url.substring(url.lastIndexOf("/") + 1);
        }
        return filename;
    }

    public static boolean fileIsExists(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public static void RecursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }
    // 使用自带算法 获取文件的SHA1
    public static String getFileSHA1(File file) {
        if (file.exists()) {
            MessageDigest digest = null;
            byte buffer[] = new byte[1024];
            int len;
            try {
                digest = MessageDigest.getInstance("SHA-1");// ("SHA-1");
                FileInputStream in = new FileInputStream(file);
                while ((len = in.read(buffer, 0, 1024)) != -1) {
                    digest.update(buffer, 0, len);
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

            // 直接用这玩意转换成16进制，碉堡了、
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);

        } else {
            return "";
        }
    }
    public enum FileFormat {
        JPEG,
        PNG
    }

    public static boolean writeBitmapToFile(Bitmap b, String file_full_path, FileFormat ff) {
        if (b != null && file_full_path != null && file_full_path.length() > 0 && ff != null) {
            java.io.FileOutputStream fos = null;
            try {
                Bitmap.CompressFormat file_format = Bitmap.CompressFormat.PNG;
                if (ff == FileFormat.JPEG) {
                    file_format = Bitmap.CompressFormat.JPEG;
                }
                fos = new java.io.FileOutputStream(file_full_path);
                b.compress(file_format, 90, fos);
            } catch (java.io.IOException ex) {
                // MINI_THUMBNAIL not exists, ignore the exception and generate
                // one.
                return false;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (java.io.IOException ex) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static String getAppTempDir() {
        String dir = getAppSDCardDir() + "/pic";
        File file = new File(dir);
        if (!file.exists())
            file.mkdirs();
        return dir;
    }


    public static String getAppSDCardDir() {
        File file = Environment.getExternalStorageDirectory();
        String storageDir = "";
        if (file != null && file.exists())
            storageDir = file.getPath();
        return storageDir + "/irearCam";
    }

    public static List<String> getPictures(final String strPath) {
        List<String> list = new ArrayList<String>();
        File file = new File(strPath);
        File[] allfiles = file.listFiles();
        if (allfiles == null) {
            return null;
        }
        for(int k = 0; k < allfiles.length; k++) {
            final File fi = allfiles[k];
            if(fi.isFile()) {
                int idx = fi.getPath().lastIndexOf(".");
                if (idx <= 0) {
                    continue;
                }
                String suffix = fi.getPath().substring(idx);
                if (suffix.toLowerCase().equals(".jpg") ||
                        suffix.toLowerCase().equals(".jpeg") ||
                        suffix.toLowerCase().equals(".bmp") ||
                        suffix.toLowerCase().equals(".png") ||
                        suffix.toLowerCase().equals(".gif") ) {
                    list.add(fi.getPath());
                }
            }
        }
        return list;
    }



}
