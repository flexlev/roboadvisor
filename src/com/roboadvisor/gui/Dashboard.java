package com.roboadvisor.gui;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Dashboard implements Initializable{
	
	private double xOffset = 0; // Window X position
	private double yOffset = 0; // Window Y position
	
	public static int currentDelayTime = 0;
	public static boolean currentAddGap = false;
	public static String currentEmail;
	
	//ObservableList<String> scheduleChoice = FXCollections.observableArrayList("a","b","c"); 
	public static int startMonday;
	public static int startTuesday;
	public static int startWednesday;
	public static int startThursday;
	public static int startFriday;
	public static int startSaturday;
	public static int startSunday;
	public static int endMonday;
	public static int endTuesday;
	public static int endWednesday;
	public static int endThursday;
	public static int endFriday;
	public static int endSaturday;
	public static int endSunday;
	
	private static boolean started = false;
	private static int callingQuickSave;
	
	public static Thread thread;
	
	public Stage primaryStage;
	
	Image imgStart = new Image("file:src/main/java/image/play.png");
	Image imgRestart = new Image("file:src/main/java/image/restart.png");
	
	@FXML private ImageView btn_settings, btn_log, btn_start, btn_close;
	
	@FXML private ChoiceBox<String> delayTime;
	@FXML private RadioButton addGap;
	
	@FXML private Button reset, save, list_refresh;
	@FXML private ChoiceBox<String> s_monday, s_tuesday, s_wednesday, s_thursday, s_friday, s_saturday, s_sunday;
	@FXML private ChoiceBox<String> e_monday, e_tuesday, e_wednesday, e_thursday, e_friday, e_saturday, e_sunday;
	@FXML private AnchorPane log, settings; 
			
	public Dashboard() {
	}
	
	public void display(String currentEmail, Stage prevStage) throws IOException {
		this.currentEmail = currentEmail;
    	// Handle the login button
 		Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml")); // Get the main.fxml file ( this is for the design )
 		root.setStyle("-fx-background-color: transparent;"); 
        
 		primaryStage = new Stage();
 		primaryStage.initStyle(StageStyle.TRANSPARENT);
 		Scene scene = new Scene(root); // Set the size of the scene
 		scene.setFill(Color.TRANSPARENT);
 		primaryStage.setScene( scene ); // Changes to the new screen
 		primaryStage.show();
 		prevStage.close(); 	    
 		
 	    root.setOnMousePressed(new EventHandler<MouseEvent>() { // When the mouse is pressed on the window
 	           @Override
 	           public void handle(MouseEvent event) { // Handle it like this:
 	               xOffset = event.getSceneX(); // Change the xOffset of the window to the current X
 	               yOffset = event.getSceneY(); // Change the yOffset of the window to the current Y
 	           }
 	       });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() { // When the mouse is dragged on the window
            @Override
            public void handle(MouseEvent event) { // Handle it like this:
            	primaryStage.setX(event.getScreenX() - xOffset); // Move the window based on the X position
                primaryStage.setY(event.getScreenY() - yOffset); // Move the window based on the Y position
            }
        });
	}
	
	@FXML
	private void handleStart( MouseEvent event ) throws IOException, InterruptedException { 

	}
	
	private String parametersCheck() {
		if(startMonday > endMonday)
			return "Monday's Start Time is after the End Time. Correct it, Save and Retry.";
		if(startTuesday > endTuesday)
			return "Tuesday's Start Time is after the End Time. Correct it, Save and Retry.";
		if(startWednesday > endWednesday)
			return "Wednesday's Start Time is after the End Time. Correct it, Save and Retry.";
		if(startThursday > endThursday)
			return "Thursday's Start Time is after the End Time. Correct it, Save and Retry.";
		if(startFriday > endFriday)
			return "Friday's Start Time is after the End Time. Correct it, Save and Retry.";
		if(startSaturday > endSaturday)
			return "Saturday's Start Time is after the End Time. Correct it, Save and Retry.";
		if(startSunday > endSunday)
			return "Sunday's Start Time is after the End Time. Correct it, Save and Retry.";
		
		if(currentAddGap & currentDelayTime == 0)
			return "Gap Settings Inconsistency. Make Sure a Value is Selected if the Gap Option is Enabled. Correct it, Save and Retry.";
			
		return "";
	}

	@FXML
	private void handleClose( MouseEvent event ) throws InterruptedException {
		System.exit(0); // Exit the application
	}
	
	@FXML
	private void switchToSettings( MouseEvent event ) throws InterruptedException {
		settings.setVisible(true);
		log.setVisible(false);
	}
	
	@FXML
	private void switchToLog( MouseEvent event ) throws InterruptedException {
		log.setVisible(true);
		settings.setVisible(false);
		handleList(event);
	}
	
	@FXML
	private void handleReset( MouseEvent event ) throws InterruptedException {
		s_monday.setValue("");
		startMonday = 0;
		s_tuesday.setValue("");
		startTuesday = 0;
		s_wednesday.setValue("");
		startWednesday = 0;
		s_thursday.setValue("");
		startThursday = 0;
		s_friday.setValue("");
		startFriday = 0;
		s_saturday.setValue("");
		startSaturday = 0;
		s_sunday.setValue("");
		startSunday = 0;
		
		e_monday.setValue("");
		endMonday = 0;
		e_tuesday.setValue("");
		endTuesday = 0;
		e_wednesday.setValue("");
		endWednesday = 0;
		e_thursday.setValue("");
		endThursday = 0;
		e_friday.setValue("");
		endFriday = 0;
		e_saturday.setValue("");
		endSaturday = 0;
		e_sunday.setValue("");
		endSunday = 0;
		
		currentAddGap = false;
		addGap.setSelected(false);
		
		currentDelayTime = 0;
		delayTime.setValue("");		
	}
	
	@FXML
	public void initialize() {
	}
	
	@FXML
	private void handleQuickSave() throws InterruptedException {
	
	}
	
	private int findTime(String substring) {
		if(substring.equals("12 AM"))	
			return 0;
		if(substring.equals("1 AM"))	
			return 1;
		if(substring.equals("2 AM"))	
			return 2;
		if(substring.equals("3 AM"))	
			return 3;
		if(substring.equals("4 AM"))	
			return 4;
		if(substring.equals("5 AM"))	
			return 5;
		if(substring.equals("6 AM"))	
			return 6;
		if(substring.equals("7 AM"))	
			return 7;
		if(substring.equals("8 AM"))	
			return 8;
		if(substring.equals("9 AM"))	
			return 9;
		if(substring.equals("10 AM"))	
			return 10;
		if(substring.equals("11 AM"))	
			return 11;
		if(substring.equals("12 PM"))	
			return 12;
		if(substring.equals("1 PM"))	
			return 13; 
		if(substring.equals("2 PM"))	
			return 14;
		if(substring.equals("3 PM"))	
			return 15;
		if(substring.equals("4 PM"))	
			return 16;
		if(substring.equals("5 PM"))	
			return 17;
		if(substring.equals("6 PM"))	
			return 18;
		if(substring.equals("7 PM"))	
			return 19;
		if(substring.equals("8 PM"))	
			return 20;
		if(substring.equals("9 PM"))	
			return 21;
		if(substring.equals("10 PM"))	
			return 22;
		if(substring.equals("11 PM"))	
			return 23;
		
		return 0;
	}
	
	private String findTimeValue(int value) {
		if(value == 0)	
			return "12 AM";
		if(value == 1)	
			return "1 AM";
		if(value == 2)	
			return "2 AM";
		if(value == 3)	
			return "3 AM";
		if(value == 4)	
			return "4 AM";
		if(value == 5)	
			return "5 AM";
		if(value == 6)	
			return "6 AM";
		if(value == 7)	
			return "7 AM";
		if(value == 8)	
			return "8 AM";
		if(value == 9)	
			return "9 AM";
		if(value == 10)	
			return "10 AM";
		if(value == 11)	
			return "11 AM";
		if(value == 12)	
			return "12 PM";
		if(value == 13)	
			return "1 PM";
		if(value == 14)	
			return "2 PM";
		if(value == 15)	
			return "3 PM";
		if(value == 16)	
			return "4 PM";
		if(value == 17)	
			return "5 PM";
		if(value == 18)	
			return "6 PM";
		if(value == 19)	
			return "7 PM";
		if(value == 20)	
			return "8 PM";
		if(value == 21)	
			return "9 PM";
		if(value == 22)	
			return "10 PM";
		if(value == 23)	
			return "11 PM";
		
		return "";
	}
	
	@FXML
	private void handleMinimize( MouseEvent event ) throws InterruptedException {
		settings.setVisible(false);
		log.setVisible(false);
	}

	@FXML
	private void handleList( MouseEvent event ) throws InterruptedException {
//		list_history.getItems().clear();
//		list_history.getColumns().clear();
//		
//		TableColumn<PostedAd, String> title = new TableColumn<>("Title");
//		title.setCellValueFactory(new PropertyValueFactory<>("title"));
//		
//		TableColumn<PostedAd, Timestamp> time = new TableColumn<>("Time");
//		time.setCellValueFactory(new PropertyValueFactory<>("timeStamp"));
//		
//		TableColumn<PostedAd, Timestamp> accountNumber = new TableColumn<>("Account");
//		accountNumber.setCellValueFactory(new PropertyValueFactory<>("ad_id"));
//		
//		List<PostedAd> postedAds = executor.getPreviousAds(user.getId());
//		ObservableList<PostedAd> list = FXCollections.observableArrayList();
//		for(PostedAd postedAd : postedAds) {
//			list.add(postedAd);
//		}
//		
//		list_history.setItems(list);
//		list_history.getColumns().addAll(title, accountNumber, time);
//		
//		title.prefWidthProperty().bind(list_history.widthProperty().multiply(0.55));
//		accountNumber.prefWidthProperty().bind(list_history.widthProperty().multiply(0.15));
//		time.prefWidthProperty().bind(list_history.widthProperty().multiply(0.30));
//		
//
//		title.setResizable(false);
//		accountNumber.setResizable(false);
//        time.setResizable(false);
	}
	
	private void printSchedule() {
		System.out.println("Monday Start : " + startMonday + " end : " + endMonday );
		System.out.println("Tuesday Start : " + startTuesday + " end : " + endTuesday );
		System.out.println("Wednesday Start : " + startWednesday + " end : " + endWednesday );
		System.out.println("Thursday Start : " + startThursday + " end : " + endThursday );
		System.out.println("Friday Start : " + startFriday + " end : " + endFriday );
		System.out.println("Saturday Start : " + startSaturday + " end : " + endSaturday );
		System.out.println("Sunday Start : " + startSunday + " end : " + endSunday );
		System.out.println("DelayTime is : " + currentDelayTime);
		System.out.println("currentAddGap : " + currentAddGap);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initialize();
		
	}

}
