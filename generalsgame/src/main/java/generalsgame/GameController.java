package generalsgame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

import generalsgame.gamestate.GameState;
import generalsgame.gamestate.MainMenuState;
import generalsgame.gameobjects.WorldMap;

/**
 * GameController is the main workhorse behind GeneralsGame
 * The GameController is generally called from Generals (the view of the game)
 */

 public class GameController {
   public boolean running = false;
   
   private WorldMap currentWorldMap;

   private Canvas gameCanvas;
   private Canvas uiCanvas;
   public Canvas debugCanvas;
   public double WIDTH; //Dimensions of the screen (render area)
   public double HEIGHT; //Dimensions of the screen (render area)
   public double xOffset; //Offsets are used control which part of the map is drawn
   public double yOffset; //If/when a map is larger than display-area, it should be centered on player
   public boolean toggleScale = false;

   private HashMap<Integer,GameState> gameStates;
   public GameState currentState;
   public static final int LOADSCREEN = 0;
   public static final int MAINMENU = 1;
   public static final int LOBBY = 2;
   public static final int BATTLE = 3;
   private boolean loading;


   public GameController (Canvas gameCanvas, Canvas uiCanvas) {
      //Initialize the screen size
      this.gameCanvas = gameCanvas;
      this.uiCanvas = uiCanvas;
      
      WIDTH = gameCanvas.getWidth();
      HEIGHT = uiCanvas.getHeight();

      this.gameStates = new HashMap<>();

   }

   public void start() {
      gameStates.put(MAINMENU, new MainMenuState(this));
      //gameStates.put(LOBBY, new LobbyState(this));
      //gameStates.put(BATTLE, new BattleState(this));

      this.moveToState(MAINMENU);
      currentState.enter();
      
   }

   public void moveToState(int gameStateNumber) {
      //TODO: Do some fancy transition?
     
      if (currentState != null) currentState.exit();
      if (gameStateNumber == LOADSCREEN) {
         this.currentState = null;
         return;
      }
      if (gameStates.get(gameStateNumber) == null) {
          buildNewState(gameStateNumber);
      } 
      currentState = gameStates.get(gameStateNumber);
      currentState.enter();
      updateUI();
   }
   
   private void buildNewState(int gameStateNumber) {
      switch (gameStateNumber) {
          case MAINMENU: gameStates.put(MAINMENU, new MainMenuState(this)) ;break;
          //case LOBBY: gameStates.put(LOBBY, new LobbyState(this)); break;
          //case BATTLE: gameStates.put(BATTLE, new BattleState(this)); break;
          case LOADSCREEN: Generals.logger.warning("Tried to enter loadscreen!"); break;
          default: Generals.logger.warning("Unknown gamestate!") ;break;
      }
   }

    /**
    * Tick checks keybuffer, initiates actions and does just about everything.
    * Tick needs to know how much time has passed since the last tick, so it can
    * even out actions and avoid rollercoaster game speed. 
    * @param time Time passed since last time 
    * @param pressedButtons Buttons currently pressed down
    * @param releasedButtons Buttons recently released
    */
    public void tick(double time, ArrayList<KeyCode> pressedButtons, ArrayList<KeyCode> releasedButtons) {
      if (currentState == null) return;
       currentState.tick(time, pressedButtons, releasedButtons);
   }
   
   /**
   * Render handles updating the game window, and should be called every time something needs refreshed.
   * By default render is called 60 times per second (or as close to as possible) by AnimationTimer -thread.

   */
   public void render() {
       if (this.loading) {
           try {
               //this.loadingScreen.render(gameCanvas, uiCanvas);
               //if (this.loadingScreen.isReady()) this.loading=false;
               return;
           } catch (Exception e) {
               Generals.logger.warning("Loading screen got removed mid-render");
               return;
           }
       }
       //TODO: Consider sorting out UI here instead of handing it all to currentState
       currentState.render(gameCanvas, uiCanvas);
       //Generals.logger.info("Rendered current state on canvas");
       if (toggleScale) {
           //toggleScale(centerCanvas.getGraphicsContext2D());
           //toggleScale(uiCanvas.getGraphicsContext2D());
           toggleScale=false;
       }
   }

   public void updateUI() {
      if (this.running) {
         Generals.logger.info("Updating UI");
         currentState.updateUI();
      }
   }

   public Canvas getGameCanvas() {
         return this.gameCanvas;
   }

   public Canvas getUICanvas() {
         return this.uiCanvas;
   }

 }