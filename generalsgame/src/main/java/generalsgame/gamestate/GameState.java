/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */

package generalsgame.gamestate;

import java.util.ArrayList;

import generalsgame.GameController;
import generalsgame.ui.UIComponent;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

/**
 * GameStates handle various parts of a game.
 * A state handles both the input and the output, coordinating via Game.class.
 * GameState can be considered as the Controller in an MVC model.
 * Planned states include stuff such as:
 * MainMenu
 * Lobby
 * Battle
 * @author nikok
 */
public interface GameState {
    
    //public HashMap<String, UIComponent> getUIComponents();
    public void addUIComponent(UIComponent uic);
    public UIComponent getUIComponent(String uicname);
    public boolean removeUIComponent(String uicname);
    public boolean removeUIComponent(UIComponent uic);
    
    public GameController getGC();
    
    //Draw things
    public void render(Canvas gameCanvas, Canvas uiCanvas);
    
    //Do things
    public void tick(double time, int elapsedSeconds, ArrayList<KeyCode> pressedButtons, ArrayList<KeyCode> releasedButtons);
    
    //Handle mouse events
    public void handleMouseEvent(MouseEvent me);
    
    //Update UI (screen resize etC)
    public void updateUI();
    
    public void closePopUpWindows();
    
    public int getStateID();
    
    //TODO:
    //Cleanup the stage
    public void exit();
    
    //Initialization
    public void enter();
    
    
    
}
