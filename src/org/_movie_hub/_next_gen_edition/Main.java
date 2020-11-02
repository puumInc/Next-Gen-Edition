package org._movie_hub._next_gen_edition;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Main extends Application {

    public static File RESOURCE_PATH = new File(System.getenv("JAVAFX_DEV_APP_HOME").concat("\\_movie_hub\\_next_gen_edition"));
    private double xOffset, yOffset;
    public static Stage stage;
    public static Process process;

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/_movie_hub/_next_gen_edition/_fxml/splashScreenPlatform.fxml"));
        Scene scene = new Scene(root);
        scene.setOnMousePressed(event2 -> {
            xOffset = event2.getSceneX();
            yOffset = event2.getSceneY();
        });
        scene.setOnMouseDragged(event1 -> {
            primaryStage.setX(event1.getScreenX() - xOffset);
            primaryStage.setY(event1.getScreenY() - yOffset);
        });
        primaryStage.getIcons().addAll(
                new Image(getClass().getResource("/org/_movie_hub/_next_gen_edition/_images/myIco_x1.png").toExternalForm()),
                new Image(getClass().getResource("/org/_movie_hub/_next_gen_edition/_images/myIco_x2.png").toExternalForm()),
                new Image(getClass().getResource("/org/_movie_hub/_next_gen_edition/_images/myIco_x3.png").toExternalForm()),
                new Image(getClass().getResource("/org/_movie_hub/_next_gen_edition/_images/myIco_x4.png").toExternalForm()));
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            if (Main.process != null) {
                Main.process.destroy();
            }
            System.exit(0);
        });
        Main.stage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
