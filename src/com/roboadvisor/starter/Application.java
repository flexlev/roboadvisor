package com.roboadvisor.starter;

import com.roboadvisor.gui.Main;

public class Application {
	
	public static String[] arguments;
	
	public static void main(String[] args) {
		arguments = args;
		Main main = new Main();
		main.start(args);
	}

}
