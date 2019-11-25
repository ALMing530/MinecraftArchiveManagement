package wxm.net;



import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
/**
 *
 * 功能描述:
 * @Description NIO实现的socket客户端工具
 * @ClassName NIOUploader
 * @auther: ALMing
 * @date: 2019/11/25 11:31

 *
 */
public class NIOUploader {
    static Logger logger = Logger.getLogger(NIOUploader.class);
    private String serverIp;
    private int port;
    private SocketChannel socketChannel;

    public void bindServer(String serverIp, int port){
        this.serverIp = serverIp;
        this.port = port;
    }
    public boolean connect() {
        InetSocketAddress address = new InetSocketAddress(serverIp, port);
        try {
            socketChannel = SocketChannel.open(address);
            socketChannel.configureBlocking(true);
            return socketChannel.isConnected();
        } catch (IOException e) {
            logger.debug("Connect fail");
        }
        return false;
    }

    public void uplode(ByteBuffer stream) {
        if (socketChannel == null) {
            throw new RuntimeException("Socket may not be initialized");
        } else {
            try {
                stream.flip();
                socketChannel.write(stream);
                stream.clear();
            } catch (IOException e) {
                logger.debug("Uploade fail");
            }
        }
    }

    public void uplode(File file) {
        if (socketChannel == null) {
            throw new RuntimeException("Socket may not be initialized");
        } else {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                FileChannel channel = inputStream.getChannel();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int len = 0;
                while ((len = channel.read(byteBuffer)) > 0) {
                    byteBuffer.flip();
                    socketChannel.write(byteBuffer);
                    byteBuffer.clear();
                }
                InputStream readBack = socketChannel.socket().getInputStream();
                byte[] bytes = new byte[1024];
                int readLen = 0;
                while ((readLen=readBack.read(bytes))>0){
                    String s = new String(bytes,0,readLen);
                    System.out.println(s);
                }
            } catch (IOException e) {
                logger.debug("Uploade fail");
            }

        }
    }
    public void disconnect() {
        try {
            socketChannel.shutdownOutput();
            socketChannel.socket().close();
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean isConnected(){
        return socketChannel.isConnected();
    }
}
