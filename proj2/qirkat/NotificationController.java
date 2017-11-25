package qirkat;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;
//import org.controlsfx.control.Notifications;

public class NotificationController implements Initializable {

    @FXML
    private Label label;

//    @FXML
//    static void showNotification(String title, String msg) {
//        org.controlsfx.control.Notifications notificationBuilder = org.controlsfx.control.Notifications.create()
//                .title(title)
//                .text(msg)
//                .graphic(null)
//                .hideAfter(Duration.seconds(3))
//                .position(Pos.CENTER)
//                .onAction(null);
//        notificationBuilder.show();
//    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
}