/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */

package generalsgame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import generalsgame.gamestate.BattleState;
import generalsgame.gamestate.GameState;
import generalsgame.gamestate.MainMenuState;


/**
 * GameController is the main workhorse behind GeneralsGame,
 * wrapping the various gamestates (lobby, battle...) together.
 * The GameController is generally called from Generals (the view of the game).
 */

 public class GameController {
   public boolean running = false;
   private int currentGameTime;
   private BattleMap currentMap;

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

      this.currentMap = new BattleMap();

   }

   public void start() {
      gameStates.put(MAINMENU, new MainMenuState(this));
      //gameStates.put(LOBBY, new LobbyState(this));
      gameStates.put(BATTLE, new BattleState(this));

      this.moveToState(MAINMENU);
      this.running = true;
      this.loading = false;
      this.currentGameTime = 0;
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
          case BATTLE: gameStates.put(BATTLE, new BattleState(this)); break;
          case LOADSCREEN: Generals.logger.warning("Tried to enter loadscreen!"); break;
          default: Generals.logger.warning("Unknown gamestate!") ;break;
      }
   }

   /**
    * Initialize a new game session 
    *   
    */
   public void newGame() {

        moveToState(GameController.BATTLE);
        
   }

    /**
    * Tick checks keybuffer, initiates actions and does just about everything.
    * Tick needs to know how much time has passed since the last tick, so it can
    * even out actions and avoid rollercoaster game speed. 
    * @param time Time passed since last time 
    * @param pressedButtons Buttons currently pressed down
    * @param releasedButtons Buttons recently released
    */
    public void tick(double time,int elapsedSeconds, ArrayList<KeyCode> pressedButtons, ArrayList<KeyCode> releasedButtons) {
    this.currentGameTime = elapsedSeconds;
      if (currentState == null) return;
       currentState.tick(time, elapsedSeconds, pressedButtons, releasedButtons);
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

   public void handleMouseEvent(MouseEvent me) {
      //Pass the mouse event to the current gamestate
      if (!this.running || this.loading || this.currentState == null) return;
      currentState.handleMouseEvent(me);
  }

   public void updateUI() {
      if (this.running) {
         Generals.logger.info("Updating UI");
         currentState.updateUI();
      }
   }

   public int getCurrentTime() {
       return this.currentGameTime;
   }

   public BattleMap getCurrentMap() {
       return this.currentMap;
   }

   public Canvas getGameCanvas() {
         return this.gameCanvas;
   }

   public Canvas getUICanvas() {
         return this.uiCanvas;
   }

 }