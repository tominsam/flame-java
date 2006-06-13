package jflame;

import java.io.IOException;
import jflame.Network;
import jflame.GUI;

public class Main {

	public static void main(String[] args) throws IOException {
    	GUI gui = new GUI();
        gui.setVisible(true);
        new Network(gui);
    }
    
}
