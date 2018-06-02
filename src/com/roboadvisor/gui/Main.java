package com.roboadvisor.gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

public class Main extends Application{

	private double xOffset = 0; // Window X position
	private double yOffset = 0; // Window Y position
	
	@Override
	public void start(Stage stage) throws Exception { 

		Parent root = FXMLLoader.load(getClass().getResource("login.fxml")); 
	       
		root.setOnMousePressed(new EventHandler<MouseEvent>() { 
	           @Override
	           public void handle(MouseEvent event) {
	               xOffset = event.getSceneX(); 
	               yOffset = event.getSceneY(); 
	           }
	       });
	    root.setOnMouseDragged(new EventHandler<MouseEvent>() { 
	        @Override
	        public void handle(MouseEvent event) { 
	            stage.setX(event.getScreenX() - xOffset);
	            stage.setY(event.getScreenY() - yOffset);
	        }
	    });
       
       Scene scene = new Scene(root, 1000, 600);
    
       stage.setTitle("Epsila"); 
       stage.initStyle(StageStyle.UNDECORATED);
       stage.setScene(scene); 
       stage.show();
	}
	
	public void start(String[] args) { // When this method is called
		launch(args); // Launch the application
	}


}
