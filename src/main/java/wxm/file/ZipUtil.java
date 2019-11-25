package wxm.file;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
/**
 *
 * 功能描述:
 * @Description zip文件压缩解压缩工具
 * @ClassName ZipUtil
 * @auther: ALMing
 * @date: 2019/11/25 11:29

 *
 */
public class ZipUtil {
    private long fileSize;
    public File zip(File file){
        File relative = new File(".");
        String zipDir = relative.getAbsoluteFile().getParent() + File.separator + "zip/";
        String filename = file.getName().split("\\.")[0];
        File zip = new File(zipDir + filename + ".zip");
        if (!zip.getParentFile().exists()) {
            zip.getParentFile().mkdirs();
        }
        try {
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zip));
            compress(file,zipOutputStream,file.getName());
            zipOutputStream.close();
        } catch (IOException e) {
            System.out.println("compress fail");
            return null;
        }
        return zip;
    }
    public File unZip(File file){
        String distPath = getProjectPath()+File.separator;
        InputStream inputStream =null;
        try{
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()){
                ZipEntry zipEntry = entries.nextElement();
                if(zipEntry.isDirectory()){
                    File dir = new File(distPath+zipEntry.getName());
                    dir.mkdirs();
                }else {
                    File childFile = new File(distPath+zipEntry.getName());
                    File parentFile = childFile.getParentFile();
                    if(!parentFile.exists()){
                        parentFile.mkdirs();
                    }
                    inputStream = zipFile.getInputStream(zipEntry);
                    Files.copy(inputStream,childFile.toPath());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void compress(File file,ZipOutputStream zipOutputStream,String pathName) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if(files.length==0){
                zipOutputStream.putNextEntry(new ZipEntry(pathName+"/"));
                zipOutputStream.closeEntry();
            }else {
                for (File temp:files){
                    compress(temp,zipOutputStream,pathName+"/"+temp.getName());
                }
            }
        }else {
            zipOutputStream.putNextEntry(new ZipEntry(pathName));
            try (FileInputStream fileInputStream = new FileInputStream(file)){
                int len;
                byte[] bufer = new byte[2048];
                while ((len=fileInputStream.read(bufer))!=-1){
                    zipOutputStream.write(bufer,0,len);
                }
                zipOutputStream.closeEntry();
            }
        }
    }
    public static void decompression(String filePath, String targetStr) {
        File source = new File(filePath);
        if (source.exists()) {
            ZipInputStream zis = null;
            BufferedOutputStream bos = null;
            try {
                zis = new ZipInputStream(new FileInputStream(source));
                ZipEntry entry = null;
                while ((entry = zis.getNextEntry()) != null && !entry.isDirectory()) {
                    File target = new File(targetStr, entry.getName());
                    if (!target.getParentFile().exists()) {
                        target.getParentFile().mkdirs();
                    }
                    bos = new BufferedOutputStream(new FileOutputStream(target));
                    int read = 0;
                    byte[] buffer = new byte[1024 * 10];
                    while ((read = zis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, read);
                    }
                    bos.flush();
                }
                zis.closeEntry();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
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
    private String getProjectPath(){
        File file = new File(".");
        return file.getAbsoluteFile().getParent();
    }

}
