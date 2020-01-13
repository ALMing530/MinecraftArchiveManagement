package wxm.file;


import com.alibaba.fastjson.JSON;
import wxm.entity.Params;
import wxm.net.BIOUploader;
import wxm.net.NIOUploader;
import wxm.util.ZipUtil;
import wxm.viewctrl.Console;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * 功能描述:
 *
 * @Description 文件服务，发送和接收文件
 * @ClassName FileService
 * @auther: ALMing
 * @date: 2019/11/25 11:33
 */
public class FileService {

    private SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private NIOUploader nioUploader;
    private BIOUploader bioUploader;
    private Console console;
    private String minecraftArchiveDir = System.getProperty("user.home") +
            "/AppData/Roaming/.minecraft/saves/";

    public FileService() {
    }

    public FileService(NIOUploader nioUploader, Console console) {
        this.nioUploader = nioUploader;
        this.console = console;
    }

    public FileService(BIOUploader bioUploader, Console console) {
        this.bioUploader = bioUploader;
        this.console = console;
    }

    public void send(File file) {
        console.consoleAppend("压缩存档中....");
        File zipFile = ZipUtil.zip(file);
        String name = file.getName();
        String type = file.isDirectory() ? "Directory" : "file";
        String time = dateFormater.format(new Date());
        assert zipFile != null;
        long length = zipFile.length();
        System.out.println(length);
        Params params = new Params();
        String param = params
                .addParams("name", name)
                .addParams("type", type)
                .addParams("time", time)
                .addParams("length", length)
                .generate();
        console.consoleAppend("生成参数：" + param);
        byte[] binaryParam = param.getBytes();
        int paramLength = binaryParam.length;
        byte[] bytes = intToBytes(paramLength);
        byte[] cmd = {0, 0, 0, 1};
//        ByteBuffer byteButter1 = ByteBuffer.allocate(4);
//        ByteBuffer byteButter2 = ByteBuffer.allocate(4096);
        bioUploader.uploade(cmd);
        bioUploader.uploade(bytes);
        bioUploader.uploade(binaryParam);
        bioUploader.uploade(zipFile);
        InputStream readBack;
        try {
            readBack = bioUploader.getSocket().getInputStream();
            byte[] buf = new byte[3];
            int readLen;
            if ((readLen = readBack.read(buf)) > 0) {
                String s = new String(buf, 0, readLen);
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        byteButter1.put(bytes);
//        byteButter2.put(binaryParam);
//        nioUploader.uplode(byteButter1);
//        nioUploader.uplode(byteButter2);
//        nioUploader.uplode(zipFile);
//        nioUploader.disconnect();

    }

    public void receive() {
        byte[] cmd = {0, 0, 0, 2};
        bioUploader.uploade(cmd);
        byte[] paramLengthBytes = new byte[4];
        bioUploader.read(paramLengthBytes);
        int paramLength = bytesToInt(paramLengthBytes);
        byte[] paramsBytes = new byte[paramLength];
        bioUploader.read(paramsBytes);
        String paramsStr = new String(paramsBytes);
        HashMap params = JSON.parseObject(paramsStr, HashMap.class);
        System.out.println(params);
        Integer fileLength = (Integer) params.get("length");
        File relative = new File(".");
        String zipDir = relative.getAbsoluteFile().getParent() + File.separator + "tempzip/";
        String filename = ((String) params.get("name")).split("\\.")[0];
        console.consoleAppend("存档名：" + filename);
        console.consoleAppend("压缩后存档大小：" + fileLength);
        File zip = new File(zipDir + filename + ".zip");
        if (!zip.getParentFile().exists()) {
            zip.getParentFile().mkdirs();
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(zip);
            byte[] bytes = new byte[1024];
            int byteCount = 0;
            while (byteCount < fileLength) {
                int read = bioUploader.read(bytes);
                if (read == -1) {
                    break;
                } else {
                    byteCount += read;
                    fileOutputStream.write(bytes, 0, read);
                }
            }
            System.out.println(byteCount);
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert fileOutputStream != null;
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ZipUtil.unZip(zip, minecraftArchiveDir);
    }

    private byte[] intToBytes(int len) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((len >> 24) & 0xff);
        bytes[1] = (byte) ((len >> 16) & 0xff);
        bytes[2] = (byte) ((len >> 8) & 0xff);
        bytes[3] = (byte) (len & 0xff);
        return bytes;
    }

    public int bytesToInt(byte[] bytes) {
        int v0 = (bytes[0] & 0xff) << 24;
        int v1 = (bytes[1] & 0xff) << 16;
        int v2 = (bytes[2] & 0xff) << 8;
        int v3 = (bytes[3] & 0xff);
        return v0 | v1 | v2 | v3;
    }
}
