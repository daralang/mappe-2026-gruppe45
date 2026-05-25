package edu.ntnu.idatt2003.millions.view.component.toast;

import edu.ntnu.idatt2003.millions.service.toast.Toast;
import edu.ntnu.idatt2003.millions.service.toast.ToastType;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

class ToastNode extends HBox {

    ToastNode(Toast toast) {
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().addAll("toast", toast.type() == ToastType.SUCCESS ? "success" : "error");

        String iconLiteral = toast.type() == ToastType.SUCCESS ? "fth-check" : "fth-x-circle";
        FontIcon icon = new FontIcon(iconLiteral);
        icon.getStyleClass().add("toast-icon");

        Label message = new Label(toast.message());
        message.getStyleClass().add("toast-message");

        getChildren().addAll(icon, message);
    }
}
