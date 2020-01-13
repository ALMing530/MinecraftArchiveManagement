package wxm.util;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 功能描述:
 *
 * @Description zip文件压缩解压缩工具
 * @ClassName ZipUtil
 * @auther: ALMing
 * @date: 2019/11/25 11:29
 */
public class ZipUtil {
    private long fileSize;

    /**
     * 压缩文件，压缩后存放在项目同目录下/zip/文件明.zip
     *
     * @param file
     * @return
     */
    public static File zip(File file) {
        File relative = new File(".");
        String zipDir = relative.getAbsoluteFile().getParent() + File.separator + "zip/";
        String filename = file.getName().split("\\.")[0];
        File zip = new File(zipDir + filename + ".zip");
        if (!zip.getParentFile().exists()) {
            zip.getParentFile().mkdirs();
        }
        try {
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zip));
            compress(file, zipOutputStream, file.getName());
            zipOutputStream.close();
        } catch (IOException e) {
            System.out.println("compress fail");
            return null;
        }
        return zip;
    }

    /**
     * 解压文件
     *
     * @param srcFile 源文件
     * @param destDir 解压目录
     */
    public static void unZip(File srcFile, String destDir) {
        InputStream inputStream = null;
        try {
            ZipFile zipFile = new ZipFile(srcFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.isDirectory()) {
                    File dir = new File(destDir + zipEntry.getName());
                    dir.mkdirs();
                } else {
                    File childFile = new File(destDir + zipEntry.getName());
                    File parentFile = childFile.getParentFile();
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    inputStream = zipFile.getInputStream(zipEntry);
                    if (Files.exists(childFile.toPath())) {
                        Files.delete(childFile.toPath());
                    }
                    Files.copy(inputStream, childFile.toPath());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 压缩文件实现
     *
     * @param file            源文件
     * @param zipOutputStream 压缩后文件流
     * @param pathName        当前目录
     * @throws IOException
     */
    private static void compress(File file, ZipOutputStream zipOutputStream, String pathName) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                zipOutputStream.putNextEntry(new ZipEntry(pathName + "/"));
                zipOutputStream.closeEntry();
            } else {
                for (File temp : files) {
                    compress(temp, zipOutputStream, pathName + "/" + temp.getName());
                }
            }
        } else {
            zipOutputStream.putNextEntry(new ZipEntry(pathName));
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                int len;
                byte[] bufer = new byte[2048];
                while ((len = fileInputStream.read(bufer)) != -1) {
                    zipOutputStream.write(bufer, 0, len);
                }
                zipOutputStream.closeEntry();
            }
        }
    }

    //    private long getFileLength(File file){
//        if(file.isFile()){
//            fileSize+=file.length();
//        }else {
//            for (File temp:file.listFiles()){
//                getFileLength(temp);
//            }
//        }
//        return fileSize;
//    }
    private static String getProjectPath() {
        File file = new File(".");
        return file.getAbsoluteFile().getParent();
    }

}
