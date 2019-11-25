package wxm.net;

import javafx.concurrent.Task;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
/**
 *
 * 功能描述:
 * @Description 连接UI管理器，管理不同连接状态的UI
 * @ClassName ConnectUIManager
 * @auther: ALMing
 * @date: 2019/11/25 11:28
 */
public class ConnectUIManager extends Task {
    private Socket socket;

    public void setServerIp(TextField serverIp) {
        this.serverIp = serverIp;
    }

    public void setServerPot(TextField serverPot) {
        this.serverPot = serverPot;
    }

    private TextField serverIp;
    private TextField serverPot;
    public ConnectUIManager(Socket socket){
       this.socket = socket;
    }
    @Override
    protected Object call() throws Exception {
        try {
            InputStream readBack = socket.getInputStream();
            while (true){
                readBack.read();
            }
        } catch (IOException e) {
            serverIp.setDisable(false);
            serverPot.setDisable(false);
            updateMessage("连接");
            socket.close();
        }
        return null;
    }
    public void updateConnectButtonText(String content){
        updateMessage(content);
    }
}
