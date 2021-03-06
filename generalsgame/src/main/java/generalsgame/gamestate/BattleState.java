/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */

package generalsgame.gamestate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Level;

import generalsgame.GameController;
import generalsgame.Generals;
import generalsgame.commands.Command;
import generalsgame.commands.MoveCommand;
import generalsgame.gameobjects.Creature;
import generalsgame.gameobjects.MapObject;
import generalsgame.ui.AudioControls;
import generalsgame.ui.BattleButtons;
import generalsgame.ui.CombatPopup;
import generalsgame.ui.IconButton;
import generalsgame.ui.Overlay;
import generalsgame.ui.PopUpMenu;
import generalsgame.ui.ScrollingPopupText;
import generalsgame.ui.TextButton;
import generalsgame.ui.TextPanel;
import generalsgame.ui.TiledWindow;
import generalsgame.ui.UIComponent;
import generalsgame.ui.AudioControls.MuteMusicButton;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class BattleState implements GameState {

    private final GameController game;
    public boolean gameMenuOpen;
    public boolean paused;
    public double lastDragX;
    public double lastDragY;
    public boolean infoBoxOpen;
    private boolean inConsole;
    private final HashMap<String, UIComponent> uiComponents;
    private final TreeSet<UIComponent> drawOrder;
    private TextPanel infobox;
    private CombatPopup sct;
    private int mouseCommand;

    public BattleState(GameController game) {
        Generals.logger.info("Building a new BattleState...");
        this.game = game;
        this.uiComponents = new HashMap<>();
        this.drawOrder = new TreeSet<>();
        this.mouseCommand = -1;        

    }


    public void addTextFloat(String text, MapObject target) {
        this.sct.addSCT(target, text, Color.CYAN);
    }
    
    public void addTextFloat(ScrollingPopupText sct) {
        this.sct.addSCT(sct);
    }


    /**
     * The Tick command parses user input and sends an update(time) command to the
     * location game is currently at. These are both done only if game is not inside a menu.
     * In other words, the Location is paused while in a menu (that goes in the menu-stack).
     * @param time Time since last update
     * @param pressedButtons List of buttons pressed down by the user
     * @param releasedButtons  List of buttons released by the user
     */
    @Override
    public void tick(double time, int elapsedSeconds, ArrayList<KeyCode> pressedButtons, ArrayList<KeyCode> releasedButtons) {
        if (game.getCurrentMap() == null) {
            return;
        }
        game.getCurrentMap().update(time, elapsedSeconds);
        this.sct.tick(time);
    }
    

    /**
    * The LocationState renderer does things in two layers: Game and UI.
    * Both of these layers are handled with via Canvases. First the Game is rendered on
    * the gameCanvas, then the UI is rendered on the uiCanvas on top of it
    * TODO: Consider separating gameplay into several layers (ground, structures, creatures, (structure)frill, overhead?)
    * @param gameCanvas Canvas to draw the actual gameplay on
    * @param uiCanvas Canvas to draw the UI on
    * @param shadowCanvas Canvas for shadows
    */
   @Override
   public void render(Canvas gameCanvas, Canvas uiCanvas) {
       GraphicsContext gc = gameCanvas.getGraphicsContext2D();
       double screenWidth = gameCanvas.getWidth();
       double screenHeight = gameCanvas.getHeight();
       
       
       //Render the current Location unless paused
       if (!this.paused) {
           gc.clearRect(0, 0, screenWidth, screenHeight);
           if (game.getCurrentMap() != null) {
               game.getCurrentMap().render(gc);
               //Render Location overlay
               Overlay.drawAllHPBars(gc, game.getCurrentMap().getLastRenderedObjects());
           }
       }
       
       //Render the UI
       this.renderUI(uiCanvas);
       
   }

   private void renderUI(Canvas uiCanvas) {
        GraphicsContext uigc = uiCanvas.getGraphicsContext2D();
        double screenWidth = uiCanvas.getWidth();
        double screenHeight = uiCanvas.getHeight();
        //Render the UI
        uigc.clearRect(0, 0, screenWidth, screenHeight);
        if (this.game.getCurrentMap() != null) {
            this.updateInfoBox();
            this.sct.render(uigc);
        }

        if (gameMenuOpen) {
            try {
                //Add a Controls image to display what buttons do - easy alternative to actual Help
                Image controls = new Image("/images/controls.png");
                uigc.drawImage(controls, screenWidth - controls.getWidth(), 50);
            } catch (Exception e) {
                Generals.logger.info("controls.png not found");
            }

        }

        if (this.drawOrder != null) {
            for (UIComponent uic : this.drawOrder) {
                uic.render(uigc, 0, 0);
            }
        }

    }



    public void loadDefaultUI() {
        this.uiComponents.clear();
        this.drawOrder.clear();
        this.infobox = new TextPanel(this, "InfoBox", 250, 150, game.WIDTH-300, game.HEIGHT-500, Generals.graphLibrary.getImageSet("panelBeigeLight"));
        this.infobox.addCloseButton();

        this.inConsole = false;

        this.sct = new CombatPopup();
        this.addUIComponent(this.generateActionBar());
        this.addUIComponent(generateGeneralBar());
        
    }
    
    private TiledWindow generateGeneralBar(){
        int buttonCount = 5;
        TiledWindow generalBar = new TiledWindow(this, "GeneralButtons", 70, buttonCount*60, 0, 80);
        generalBar.setBgOpacity(0.3);
        IconButton menuButton = new BattleButtons.ToggleBattleMenuButton(game);
        generalBar.addSubComponent(menuButton);
        MuteMusicButton muteMusicButton = new AudioControls.MuteMusicButton();
        generalBar.addSubComponent(muteMusicButton);        
        generalBar.setRenderZ(-11);
        return generalBar;
    }

    private TiledWindow generateActionBar() {
        TiledWindow actionBar = new TiledWindow(this, "Actionbar", game.WIDTH, 80, 0, (game.HEIGHT - 80));
        actionBar.setBgOpacity(0.3);
        TextButton moveButton = new BattleButtons.CommandMoveButton(80, 60, this.game);
        
        actionBar.addSubComponent(moveButton);
        
        actionBar.setRenderZ(-10);
        return actionBar;
    }

    /**
     * Activate a waiting action towards the mouse cursor if a button is pressed
     * @param me
     */
    private void commandWithMouse(MouseEvent me) {
        if (this.mouseCommand == -1 || game.getCurrentMap().getTargets().isEmpty()) return;
        if (this.game.getCurrentMap().getTargets().get(0) instanceof Creature ) {
            Creature target = (Creature)this.game.getCurrentMap().getTargets().get(0);
            Command command;
            //A creature is targetted, so give it the current command in mouse
            if ((me.getEventType() == MouseEvent.MOUSE_CLICKED || me.getEventType() == MouseEvent.MOUSE_PRESSED || me.getEventType() == MouseEvent.MOUSE_RELEASED) && me.getButton() == MouseButton.PRIMARY) {
                //TODO: Switch cases for various commands
                command = new MoveCommand(
                    target,
                     me.getSceneX()-game.getCurrentMap().getLastxOffset(),
                      me.getSceneY()-game.getCurrentMap().getLastyOffset(),
                      game.getCurrentTime(),
                      game.getCurrentTime() + 3
                      );
                    
                target.setCommand(command);
                Generals.logger.info(target.getName() + " was given command " + command.getName());
                target.addTextPopup(command.getName() + " command given!");
                this.cancelCommandWithMouse();

            } else if (me.getButton() == MouseButton.SECONDARY) {
                cancelCommandWithMouse();
            }
        } else {
            Generals.logger.info("No creature foundon target list");
        } 
    }

    /**
     * Start casting an action with mouse, activating the action with mouseclick
     * @param Command The action to trigger with mouseclick
     */
    public void commandWithMouse(int commandID) {
    	this.mouseCommand = commandID;
    	//TODO: Take the preferred cursor from config
    	Generals.setCursor("handblue");
    }
    
    /**
     * Cancel the casting of an action, changing
     * the cursor back to default.
     */
    public void cancelCommandWithMouse() {
    	this.mouseCommand = -1;
    	//TODO: Take the default cursor from config
    	Generals.setCursor("default");
    }
    


    @Override
    public GameController getGC() {
        return this.game;
    }
    
    @Override
    public void exit() {
        Generals.soundManager.stopMusic();
    }

    @Override
    public void enter() {
        try {
            Generals.soundManager.playMusic("dungeon");
        }catch (Exception e) {
            
        }
        this.loadDefaultUI();
        this.paused = false;
    }

    
    private void updateInfoBox() {
        if (game.getCurrentMap().getTargets().size() < 1) {
            if (this.uiComponents.containsValue(this.infobox)) this.removeUIComponent("InfoBox");
            return;
        }
        this.infobox.setText(Overlay.generateInfoBoxText(game.getCurrentMap().getTargets().get(0)));
    }

    public void setMouseCommand(int commandID) {
        this.mouseCommand = commandID;
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
    public void handleMouseEvent(MouseEvent me) {
        if (game.getCurrentMap() != null ) {
            if (mouseCommand != -1) {
                //Generals.logger.info("Commanding with mouse...");
                commandWithMouse(me);
            } 
        }
        //See if there's an UI component to click
        if (me.getEventType() == MouseEvent.MOUSE_CLICKED || me.getEventType() == MouseEvent.MOUSE_PRESSED || me.getEventType() == MouseEvent.MOUSE_RELEASED) this.handleClicks(me);
        if (me.getEventType() == MouseEvent.MOUSE_DRAGGED) this.handleMouseDrags(me);
        me.consume();
    }

    private void handleClicks(MouseEvent me) {
        this.lastDragX = 0; this.lastDragY = 0;
        if(!mouseClickOnUI(me)){
            //If not, give the click to the underlying gameLocation
            Generals.logger.info("Click didnt land on an UI button");
            this.mouseClickOnMap(me);
        }
    }
    /**
     * Check if there's any UI component at the mouse event location.
     * If so, trigger that UI components "onClick". 
     * @param me MouseEvent got from the game user (via Game)
     * @return True if UI component was clicked. False if there was no UI there
     */
    
     public boolean mouseClickOnUI(MouseEvent me) {
        UIComponent uic = this.getUIComponentAtCoordinates(me.getX(), me.getY());
        if (uic != null) {
            uic.handleMouseEvent(me);
            me.consume();
            return true;
        }
        return false;
        
    }

    private boolean mouseClickOnMap(MouseEvent me) {
        if (game.getCurrentMap() == null)
            return false;
        double clickX = me.getX();
        double clickY = me.getY();
        double xOffset = this.game.getCurrentMap().getLastxOffset();
        double yOffset = this.game.getCurrentMap().getLastyOffset();
        if (me.getButton() == MouseButton.PRIMARY && me.getEventType() == MouseEvent.MOUSE_RELEASED) {
            // Select a target if possible
            MapObject mob = game.getCurrentMap().getMobAtLocation(xOffset + clickX, yOffset + clickY);
            if (mob == null) {
                //Generals.logger.info("No mob found at "+ (xOffset + clickX) + " x " + (yOffset + clickY) );
            } else {
                //Generals.logger.info("Clicked a mob");
                //Generals.logger.info("Mob was: "+mob.getName());
            }
            if (toggleTarget(clickX, clickY)) return true;
        }
        //Click didn't do anything
        //Generals.logger.info("Clicked battle map, but it didn't land on anything");
        return false;
    }

    /**
     * Check the given mouse coordinates for a map object,
     * and make it the current target (updating infobox) if
     * there is one.
     * If the same mob already was targetted, release the targeting
     * @param clickX local xCoordinate of the mouseclick
     * @param clickY local yCoordinate of the mouseclick
     * @return true if a mob was clicked
     */
    private boolean toggleTarget(double clickX, double clickY) {
        MapObject targetMob = game.getCurrentMap().getMobAtLocation(clickX + game.getCurrentMap().getLastxOffset(),
                clickY + game.getCurrentMap().getLastyOffset());
        if (targetMob != null) {
            if (game.getCurrentMap().getTargets().contains(targetMob)) {
                // There already is the same mob targetted, so reselection should clear the
                // targetting
                game.getCurrentMap().clearTarget();
                this.removeUIComponent("InfoBox");
            
            } else {
                Generals.logger.log(Level.INFO, "Targetted {0}", targetMob.toString());
                game.getCurrentMap().setTarget(targetMob);
                this.addUIComponent(this.infobox);
            }
            return true;
        }
        return false;
    }

    private void handleMouseDrags(MouseEvent me) {
        if (lastDragX == 0 || lastDragY == 0) {
            lastDragX = me.getX();
            lastDragY = me.getY();
        }
        UIComponent uic = this.getUIComponentAtCoordinates(me.getX(), me.getY());
        if (uic != null) uic.handleMouseDrag(me, lastDragX, lastDragY);
        lastDragX = me.getX(); lastDragY = me.getY();
    }

    protected UIComponent getUIComponentAtCoordinates(double xCoor, double yCoor) {
        for (UIComponent uic : this.drawOrder.descendingSet()) {
            double uicHeight = uic.getHeight();
            double uicWidth = uic.getWidth();
            double uicX = uic.getXPosition();
            double uicY = uic.getYPosition();
            //Check if the click landed on the ui component
            if (xCoor >= uicX && xCoor <= (uicX + uicWidth) && yCoor >= uicY && yCoor <= uicY + uicHeight) {
                return uic;
            }
        }
        //Click landed on area without UI component
        return null;
    }

    @Override
    public UIComponent getUIComponent(String uicName) {
        return this.uiComponents.get(uicName);
    }
    @Override
    public void updateUI() {
        //Move the actionbar to where it should be
        Generals.logger.info("Updating UI. Game dimensions: "+game.WIDTH+"x"+game.HEIGHT);
        uiComponents.get("Actionbar").setPosition(0, (game.HEIGHT - 120));
        if(gameMenuOpen && uiComponents.containsKey("GameMenu")) uiComponents.get("GameMenu").setPosition((game.WIDTH/2 - 110), 150);
    }
    
    @Override
    public int getStateID() {
    	return GameController.BATTLE;
    }


}