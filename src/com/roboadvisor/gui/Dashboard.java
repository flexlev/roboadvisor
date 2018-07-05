package com.roboadvisor.gui;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import com.roboadvisor.dao.GeneralDaoService;
import com.roboadvisor.datarepository.Holding;

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
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
import javafx.scene.layout.VBox;
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
	@FXML private AnchorPane log, settings; 
	
	@FXML private TableView<Holding> list_holdings;
	private GeneralDaoService generalDaoService;
			
	public Dashboard() {
		generalDaoService = new GeneralDaoService();
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
		return null;
		
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
	
	}
	
	@FXML
	private void handleQuickSave() throws InterruptedException {
	
	}
	
	private int findTime(String substring) {
		return 0;
		
	}
	
	private String findTimeValue(int value) {
		return null;
		
	}
	
	@FXML
	private void handleMinimize( MouseEvent event ) throws InterruptedException {
		settings.setVisible(false);
		log.setVisible(false);
	}

	@FXML
	private void handleList( MouseEvent event ) throws InterruptedException {
		list_holdings.getItems().clear();
		list_holdings.getColumns().clear();
		
		TableColumn<Holding, String> stock_ticker = new TableColumn<>("Symbol");
		stock_ticker.setCellValueFactory(new PropertyValueFactory<>("symbol"));
		
		TableColumn<Holding, Double> weight = new TableColumn<>("Weight (%)");
		weight.setCellValueFactory(new PropertyValueFactory<>("weight"));
		
		//growth balanced conservative
		List<Holding> holdings = generalDaoService.getHoldingsByPortfolio("growth");
		ObservableList<Holding> list = FXCollections.observableArrayList();
		for(int i = 0; i< holdings.size(); i++) {
			list.add(holdings.get(i));
		}
		
		list_holdings.setItems(list);
		list_holdings.getColumns().addAll(stock_ticker, weight);
		list_holdings.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
		
	}

}
