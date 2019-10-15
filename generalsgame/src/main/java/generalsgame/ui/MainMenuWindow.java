/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki
 */
package generalsgame.ui;

import java.util.logging.Level;

import generalsgame.GameController;
import generalsgame.Generals;
import generalsgame.gamestate.GameState;

import javafx.concurrent.Task;
import javafx.application.Platform;

/**
 * MainMenuWindow is the actual Main Menu displayed by
 * the MainMenuState.
 * @author nikok
 */
public class MainMenuWindow extends TiledPanel {

    public MainMenuWindow(GameState parent) {
        super(parent, "MainMenu", 220, 300, (parent.getGC().WIDTH/2 - 110), 250, Generals.graphLibrary.getImageSet("panelBeige"));
        initializeMenuButtons();
        super.setInteractive(true);
    }
    
    private void initializeMenuButtons() {
        JoinLobbyButton menubutton1 = new JoinLobbyButton(super.getParent().getGC(), this.parent);
        SinglePlayerButton menubutton2 = new SinglePlayerButton(super.getParent().getGC());
        OptionsButton menubutton3 = new OptionsButton();
        QuitButton menubutton4 = new QuitButton("Quit game", 200, 60);
        super.addSubComponent(menubutton1);
        super.addSubComponent(menubutton2);
        super.addSubComponent(menubutton3);
        super.addSubComponent(menubutton4);
    }
    
    private class JoinLobbyButton extends TextButton {
        private final GameController gc;
        private final GameState state;
        
        public JoinLobbyButton(GameController gc, GameState state) {
            super("Join lobby", 200, 60);
            this.gc = gc;
            this.state = state;
        }
        
        @Override
        public void buttonPress() {
            Generals.logger.log(Level.INFO, "{0} was clicked", this.getName());

        }
        
    }
    
 
    private class SinglePlayerButton extends TextButton {
        private final GameController gc;
        
        public SinglePlayerButton(GameController gc) {
            super("Single player test", 200, 60);
            this.gc = gc;
        }

        private void newGame() {
            //gc.setLoadingScreen(new LoadingScreen("Starting game", 10));
            Task task = new Task<Void>() {
                @Override public Void call() {
                    gc.newGame();
                    return null;
                }
            };
            new Thread(task).start();
            
        }
        
        @Override
        public void buttonPress() {
            Generals.logger.log(Level.INFO, "{0} was clicked", this.getName());
            this.newGame();
        }
        
    }
    
    private class OptionsButton extends TextButton {

        public OptionsButton() {
            super("TODO: Options", 200, 60);
        }
        
        @Override
        public void buttonPress() {
            Generals.logger.log(Level.INFO, "{0} was clicked", this.getName());
        }
        
    }

    private class QuitButton extends TextButton {
    
        public QuitButton (String name, double width, double height) {
            this(name, width, height, 0, 0);
        }
        
        public QuitButton(String name, double width, double height, double xPosition, double yPosition) {
            super(name, width, height, xPosition, yPosition);
        }
        
        @Override
        public void buttonPress() {
            Generals.logger.log(Level.INFO, "{0} was clicked", this.getName());
            Platform.exit();
            //System.exit(0);
        }
        
    }
    
}
