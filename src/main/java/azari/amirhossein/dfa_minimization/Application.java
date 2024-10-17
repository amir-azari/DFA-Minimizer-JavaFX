package azari.amirhossein.dfa_minimization;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Application extends javafx.application.Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // open particles page
        FXMLLoader particlesLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        Scene particleScene = new Scene(particlesLoader.load(), 800, 600);
        primaryStage.setResizable(false);
        primaryStage.setTitle("DFA Converter");
        primaryStage.setScene(particleScene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}