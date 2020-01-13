package wxm.viewctrl;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import wxm.file.FileService;
import wxm.net.BIOUploader;
import wxm.net.NIOUploader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 功能描述:
 *
 * @Description 主界面控制器
 * @ClassName MainCtrl
 * @auther: ALMing
 * @date: 2019/11/25 11:33
 */
public class MainCtrl implements Controller {
    //    static Logger logger = Logger.getLogger(MainCtrl.class);
    @FXML
    private TextArea console;

    @FXML
    private Button syncToServerbtn;

    @FXML
    private Button syncFromServerbtn;

    @FXML
    private Button connectbtn;

    @FXML
    private TextField serverIp;

    @FXML
    private TextField serverPort;

    @FXML
    private TextField archiveDir;

    private String minecraftArchiveDir = System.getProperty("user.home") +
            "//AppData//Roaming//.minecraft//saves";

    private File globalFile;

    private Tooltip dirTooltip;

    private Alert alert;

    private BIOUploader bioUploader;

    private NIOUploader nioUploader;

    private FileService fileService;

    private Alert loginFrame;

    private Console consoleController;


    @FXML
    public void initialize() {
        consoleController = new Console(this.console);
        File file = new File(minecraftArchiveDir);
        alert = new Alert(Alert.AlertType.INFORMATION);
        dirTooltip = new Tooltip();
        bioUploader = new BIOUploader();
        nioUploader = new NIOUploader();
        fileService = new FileService(bioUploader, consoleController);
        serverIp.setText("127.0.0.1");
        serverPort.setText("53055");
        String currentArchivePath = file.getAbsolutePath();
        dirTooltip.setText(currentArchivePath);
        archiveDir.setTooltip(dirTooltip);
        archiveDir.setText(currentArchivePath);
        console.setWrapText(true);
        register();
    }

    @FXML
    void connectToServer(ActionEvent event) {
        if (bioUploader.isClosed()) {
            Pattern addrRegx = Pattern.compile("^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]"
                    + "|[0-4]\\d))|[0-1]?\\d{1,2})){3}|(?=^.{3,255}$)[a-zA-Z0-"
                    + "9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$");
            Matcher addrMatcher = addrRegx.matcher(serverIp.getText());
            if (!addrMatcher.matches()) {
                alert.setTitle("Error");
                alert.setContentText("Server Address Error");
                alert.showAndWait();
                return;
            }
            Pattern portRegx = Pattern.compile("^([0-9]|[1-9]\\d{1,3}|[1-5]\\"
                    + "d{4}|6[0-4]\\d{4}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$");
            Matcher portMatcher = portRegx.matcher(serverPort.getText());
            if (!portMatcher.matches()) {
                alert.setTitle("Error");
                alert.setContentText("Server Port Error");
                alert.showAndWait();
                return;
            }
            bioUploader.bindServer(serverIp.getText(), Integer.parseInt(serverPort.getText()));
            bioUploader.connect();
            if (!bioUploader.isClosed()) {
                if (needAuth(bioUploader)) {
                    consoleController.consoleAppend("连接成功，正在鉴权");
                    if (!auth(bioUploader)) {
                        bioUploader.disconnect();
                        consoleController.consoleAppend("鉴权失败");
                        return;
                    } else {
                        consoleController.consoleAppend("鉴权成功，以与服务器建立连接");
                    }
                } else {
                    consoleController.consoleAppend("连接成功！不需要鉴权");
                }
                serverIp.setDisable(true);
                serverPort.setDisable(true);
                connectbtn.setText("断开");
            } else {
                alert.setTitle("Error");
                alert.setContentText("Connect fail,check your server status");
                alert.showAndWait();
            }
        } else {
            bioUploader.disconnect();
            serverIp.setDisable(false);
            serverPort.setDisable(false);
            connectbtn.setText("连接");
        }

    }

    @FXML
    void syncFromServer(ActionEvent event) {
        consoleController.consoleAppend("开始从服务器拉取存档");
        new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                fileService.receive();
                return null;
            }
        }).start();
    }

    @FXML
    void syncToServer(ActionEvent event) {
        if (globalFile != null) {
            new Thread(new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        if (!bioUploader.isClosed() && globalFile != null) {
                            fileService.send(globalFile);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }).start();
        } else {
            alert.setTitle("Error");
            alert.setContentText("You haven't selected the file");
            alert.showAndWait();
        }
    }

    @FXML
    void chooseArchive(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Archive");
        dc.setInitialDirectory(new File(minecraftArchiveDir));
        File file = dc.showDialog(new Stage());
        if (file != null) {
            globalFile = file;
            String currentArchivePath = file.getAbsolutePath();
            archiveDir.setText(currentArchivePath);
            dirTooltip.setText(currentArchivePath);
            globalFile = file;
        }
    }

    public boolean needAuth(BIOUploader bioUploader) {
        try {
            InputStream inputStream = bioUploader.getSocket().getInputStream();
            byte[] bytes = new byte[128];
            int read = inputStream.read(bytes);
            String auth = new String(bytes, 0, read);
            if ("authority".equals(auth)) {
                return true;
            } else {
                System.out.println(auth);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean auth(BIOUploader bioUploader) {
        boolean success = false;
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/icon/auth.png"));
        dialog.setTitle("登陆");
        dialog.setHeaderText("服务器要求你验证身份");
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        grid.add(new Label("用户名:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("密\u3000码:"), 0, 1);
        grid.add(password, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });
        Optional<Pair<String, String>> result = dialog.showAndWait();

        if (result.isPresent()) {
            Pair<String, String> userAndPwd = result.get();
            String params = userAndPwd.getKey() + "&" + userAndPwd.getValue();
            bioUploader.uploade(params.getBytes());
            byte[] bytes = new byte[128];
            int read = bioUploader.read(bytes);
            String readBack = new String(bytes, 0, read);
            if ("success".equals(readBack)) {
                success = true;
            }
        }
        return success;
    }

    @Override
    public void register() {
        CtrlContainer.container.put(this.getClass(), this);
    }
}
