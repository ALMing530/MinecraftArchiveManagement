package wxm.net;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

/**
 *
 * 功能描述:
 * @Description BIO实现的socket客户端工具
 * @ClassName BIOUploader
 * @auther: ALMing
 * @date: 2019/11/25 11:30

 *
 */
public class BIOUploader {
    static Logger logger = Logger.getLogger(BIOUploader.class);
    private String serverIp;
    private int port;
    private Socket socket;
    public OutputStream writer= null;
    public InputStream reader = null;

    public void bindServer(String serverIp, int port){
        this.serverIp = serverIp;
        this.port = port;
    }

    public boolean connect(){
        try {
            socket = new Socket(serverIp,port);
            writer = socket.getOutputStream();
            reader = socket.getInputStream();
            return socket.isConnected();
        } catch (IOException e) {
            logger.debug("Connect Fail");
        }
        return false;
    }
    public void uploade(File file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            int i=0;
            while ((len=fileInputStream.read(buffer))>0) {
                writer.write(buffer,0,len);
            }
            writer.flush();
            InputStream readBack = socket.getInputStream();
            byte[] bytes = new byte[1024];
            int readLen = 0;
            while ((readLen=readBack.read(bytes))>0){
                String s = new String(bytes,0,readLen);
                System.out.println(s);
            }
        } catch (IOException e) {
            logger.debug("Uploade fail");
        }finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void uploade(byte[] bytes){
        try {
            writer.write(bytes);
        } catch (IOException e) {
            logger.debug("Uploade fail");
        }
    }
    public int read(byte[] bytes){
        try {

            return reader.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public Socket getSocket(){
        return socket;
    }
    public void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean isConnected(){
        return socket==null?false:socket.isConnected();
    }
    public boolean isClosed(){
        return socket==null?true:socket.isClosed();
    }
}
