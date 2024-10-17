package azari.amirhossein.dfa_minimization.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class FXUtils {

    public static void loadSceneFromFXML(String fxmlFile, MouseEvent mouseEvent) {
        FXMLLoader fxmlLoader = new FXMLLoader(FXUtils.class.getResource(fxmlFile));
        Parent page = null;

        try {
            page = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load the FXML file: " + fxmlFile);
            return;
        }

        Stage currentStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        currentStage.setScene(new Scene(page));
        currentStage.show();
    }

    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
