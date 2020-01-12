package wxm.viewctrl;

import javafx.scene.control.TextArea;

public class Console {
    private TextArea console;

    public Console(TextArea console) {
        this.console = console;
    }

    public TextArea getConsole() {
        return console;
    }

    public void setConsole(TextArea console) {
        this.console = console;
    }
    public void consoleAppend(String msg){
        if("".equals(console.getText())){
            console.appendText(msg);
        }else {
            console.appendText("\n"+msg);
        }
    }
    public void consoleClear(){
        console.clear();
    }
}
