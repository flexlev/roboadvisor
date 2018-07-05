package com.roboadvisor.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class Login implements Initializable{
	private double xOffset = 0; // Window X position
	private double yOffset = 0; // Window Y position
	
	public static String currentEmail;
	public static String currentPassword;
	
	public Dashboard settings;
	
	
	@FXML private TextField email; // This is linked to the email text field, because it has an ID with the same name
	@FXML private PasswordField password; // This is linked to the password text field, because it has an ID with the same name
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Nothing special here
		
	}
	
	@FXML
	private void handleClose( MouseEvent event ) throws InterruptedException { 
		System.exit(0); // Exit the application
	}
	
	@FXML
	private void handleLogin( MouseEvent event ) throws IOException {
		 currentEmail = email.getText();//"flev1266@gmail.com"; //email.getText();
	     currentPassword = password.getText();
	     
	     if(currentEmail.equals("felix.levesque@mail.utoronto.ca")) {
	    	 settings = new Dashboard();
	    	 settings.display(currentEmail, (Stage) ((Node)event.getSource()).getScene().getWindow());
	     } else {
	    	 Alert alert = new Alert(AlertType.INFORMATION, "Wrong Connection Credentials", ButtonType.OK);
	    	 alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
	    	 alert.show();
	     }
      
       System.out.println(currentEmail + ", " + currentPassword);
       
	}
}
