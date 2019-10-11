/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki
 */
package generalsgame.gamestate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;

import generalsgame.GameController;
import generalsgame.Generals;
import generalsgame.ui.MainMenuWindow;
import generalsgame.ui.PopUpMenu;
import generalsgame.ui.UIComponent;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * MainMenuState controls and manages the main menu.
 * Because of the simplicity of the main menu, there is no separate class for it.
 * Main menu is really only composed of UI-components, like buttons.
 * @author nikok
 */
public class MainMenuState implements GameState {

    private HashMap<String, UIComponent> uiComponents;
    private final TreeSet<UIComponent> drawOrder;
    private final GameController GC;
    //private UIComponent currentMenu;
    private boolean gameMenuOpen;
    
    
    public MainMenuState (GameController gc) {
        this.GC = gc;
        this.uiComponents = new HashMap<>();
        this.drawOrder = new TreeSet<>();
        MainMenuWindow mainMenuWindow = new MainMenuWindow(this);
        this.addUIComponent(mainMenuWindow);
        //this.uiComponents.put(mainMenuWindow.getName(), mainMenuWindow);
        //this.currentMenu = uiComponents.get(mainMenuWindow.getName());
        
    }
    
    @Override
    public void render(Canvas gameCanvas, Canvas uiCanvas) {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        GraphicsContext uigc = uiCanvas.getGraphicsContext2D();
        uigc.setFont(Generals.fonts.get("alagard"));
        double screenWidth = uiCanvas.getWidth();
        double screenHeight = uiCanvas.getHeight();
        gc.clearRect(0, 0, screenWidth, screenHeight);
        //TODO: Add a background to render
        
        //Render the UI
        uigc.clearRect(0, 0, screenWidth, screenHeight);
        this.drawLogo(uiCanvas);
        this.drawVersion(uiCanvas);
        if (this.drawOrder != null) {
            for (UIComponent uic : this.drawOrder) {
                uic.render(uigc, 0, 0);
            }
        }
        /*
        if (this.uiComponents != null) {
            for (String s: this.uiComponents.keySet()) {
                this.uiComponents.get(s).render(uigc, 0, 0);
            }
        }
        */
    }
    
    private void drawLogo(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double screenWidth = GC.WIDTH;
        //double screenHeight = game.HEIGHT;
        Image logo = new Image("/images/generals_logo.png");
        gc.drawImage(logo, (screenWidth/2)-(logo.getWidth()/2), 50);
        
    }
    
    private void drawVersion(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.setStroke(Color.ORANGERED);
        gc.strokeText(Generals.gameVersion, GC.WIDTH-300, GC.HEIGHT-20);
        gc.restore();
    }

    @Override
    public void tick(double time, ArrayList<KeyCode> pressedButtons, ArrayList<KeyCode> releasedButtons) {
        //No game logic to perform in game menu
        //perhaps consider adding in some minigame? :)
    }
 

    @Override
    public void handleMouseEvent(MouseEvent me) {
        //See if there's an UI component to click
        if (me.getEventType() == MouseEvent.MOUSE_CLICKED && !mouseClickOnUI(me)) {
            //If not, give the click to the underlying gameLocation
            Generals.logger.info("Click didnt land on an UI button");
        }   
    }
    
    public boolean mouseClickOnUI(MouseEvent me) {
        double clickX = me.getX();
        double clickY = me.getY();
        for (Map.Entry<String, UIComponent> entry : uiComponents.entrySet()) {
            double uicHeight = entry.getValue().getHeight();
            double uicWidth = entry.getValue().getWidth();
            double uicX = entry.getValue().getXPosition();
            double uicY = entry.getValue().getYPosition();
            //Check if the click landed on the ui component
            if (clickX >= uicX && clickX <= (uicX + uicWidth)) {
                if (clickY >= uicY && clickY <= uicY + uicHeight) {
                    entry.getValue().handleMouseEvent(me);
                    me.consume();
                    return true;
                }
            }
            
        }
        
        return false;
    }
    
    @Override
    public GameController getGC() {
        return this.GC;
    }
    
    @Override
    public void exit() {
        Generals.soundManager.stopMusic();
    }

    @Override
    public void enter() {
        try {
            Generals.soundManager.playMusic("menu");
        }catch (Exception e) {
            
        }
    }

    @Override
    public void closePopUpWindows() {
        Stack<UIComponent> popups = new Stack<>();
        for (String k : this.uiComponents.keySet()) {
            if (this.uiComponents.get(k) instanceof PopUpMenu) popups.add(this.uiComponents.get(k));
        }
        while (!popups.isEmpty()) {
            this.removeUIComponent(popups.pop());
        }
    }
    
    @Override
    public void addUIComponent(UIComponent uic) {
        this.uiComponents.put(uic.getName(), uic);
        this.drawOrder.add(uic);
    }
    
    @Override
    public boolean removeUIComponent(String uicName) {
        if (this.uiComponents.keySet().contains(uicName)) {
            //Generals.logger.info("Removing UIC "+uicName);
            this.drawOrder.remove(this.uiComponents.get(uicName));
            this.uiComponents.remove(uicName);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean removeUIComponent(UIComponent uic) {
        if (this.uiComponents.containsValue(uic)) {
            this.drawOrder.remove(uic);
            this.uiComponents.remove(uic.getName());
            return true;
        }
        return false;
    }
    
    @Override
    public UIComponent getUIComponent(String uicName) {
        return this.uiComponents.get(uicName);
    }
    @Override
    public void updateUI() {
        this.uiComponents.get("MainMenu").setPosition((GC.WIDTH/2 - 110), 250);
    }
    
    @Override
    public int getStateID() {
    	return GameController.MAINMENU;
    }
    
}
