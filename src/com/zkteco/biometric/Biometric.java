package com.zkteco.biometric;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Biometric extends Application{
	
	Stage stage;
	Scene new_patient, existing_patient, get_patient, home;
	
	@Override
    public void start(Stage stage) throws Exception {
        try {
        	FXMLLoader loader = new FXMLLoader(getClass().getResource("reader.fxml"));
        	loader.setLocation(getClass().getResource("reader.fxml"));
        	Parent root = loader.load();//FXMLLoader.load(getClass().getResource("reader.fxml"));
            stage.setTitle("Crest Medical Finger Scanner");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("appicon.png")));
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent arg0) {
					((BiometricController)loader.getController()).FreeSensor();
					System.exit(0);
					Platform.exit();
				}});
        } catch (Exception e) {
        	System.out.println(e.getMessage());
        }
    }
	
	public static void main(String[] args) {
		DatabaseHandler.connectDatabase();
		launch(args);
	}

}
