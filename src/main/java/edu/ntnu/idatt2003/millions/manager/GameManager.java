package edu.ntnu.idatt2003.millions.manager;

import edu.ntnu.idatt2003.millions.model.Exchange;
import edu.ntnu.idatt2003.millions.model.Player;
import java.io.File;


public class GameManager {

    private Player player;
    private Exchange exchange;

    public void saveGame(File file) {
        // TODO: delegere til JsonGameFileHandler
    }

    public void createNewGame(String name, String capital, File stockFile) {
        // TODO: implementeres senere
    }

    public void loadGame(File file) {
        // TODO: implementeres senere
    }
}