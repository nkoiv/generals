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
import java.util.logging.Level;
import java.util.logging.Logger;

import generalsgame.graphics.GraphicsLibrary;
import generalsgame.GameController;
import generalsgame.audio.SoundManager;
import generalsgame.audio.SoundManagerJavaFX;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Generals extends Application {
	public static final String gameVersion = "Version-0.1-Pandarin_Pomelo";
    public static final Logger logger = Logger.getLogger(Generals.class.getName());

    public static GameController game;
    public static int TILESIZE = 32;

    public static GraphicsLibrary graphLibrary;
    public static SoundManager soundManager;
    public static HashMap<String, Font> fonts;

    public boolean running = false;
    public final ArrayList<KeyCode> pressedButtons = new ArrayList<>();
    public final ArrayList<KeyCode> releasedButtons = new ArrayList<>();
    public final double[] mouseClickCoordinates = new double[2];
    public static HashMap<String, ImageCursor> cursors;


    public static Stage primaryStage;
    int width = 800;
    int height = 600;

    @Override
    public void start(Stage primaryStage) {
        Generals.primaryStage = primaryStage;
        primaryStage.setTitle("The Generals Game");
        primaryStage.setMinHeight(height);
        primaryStage.setMinWidth(width);
        primaryStage.setHeight(height);
        primaryStage.setWidth(width);
        Group root = new Group();
        Scene launchScene = new Scene(root);
        final Canvas gameCanvas = new Canvas(width, height);
        gameCanvas.widthProperty().bind(primaryStage.widthProperty());
        gameCanvas.heightProperty().bind(primaryStage.heightProperty());
        final Canvas uiCanvas = new Canvas(width, height);
        uiCanvas.widthProperty().bind(primaryStage.widthProperty());
        uiCanvas.heightProperty().bind(primaryStage.heightProperty());

        root.getChildren().add(gameCanvas);
        root.getChildren().add(uiCanvas);

        logger.info("Scene initialized");
        primaryStage.setScene(launchScene);

        setupSoundManager();
        logger.info("SoundManager initialized");
        loadFonts();
        logger.info("Fonts loaded");


        setupKeyHandlers(primaryStage);
        setupMouseHandles(root);
        setupWindowResizeListeners(launchScene);

        logger.info("Game set up");
        game = new GameController(gameCanvas, uiCanvas);
        game.WIDTH = primaryStage.getWidth();
        game.HEIGHT = primaryStage.getHeight();

        primaryStage.show();
        this.loadLibraries(gameCanvas, uiCanvas);

        game.start();
        running = true;

          primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        logger.info("Generals game started");
        
        /*
        * Game main loop
        */
        new AnimationTimer() //Logic loop
        {
            final double startNanoTime = System.nanoTime();
            double previousNanoTime = 0;
            int runningTime = 0;
            @Override
            public void handle(long currentNanoTime)
            {
                if (previousNanoTime == 0) {
                   previousNanoTime = currentNanoTime;
                return;
                }
                
                double elapsedSeconds = (currentNanoTime - previousNanoTime) / 1000000000.0;
                previousNanoTime = currentNanoTime;
                runningTime = (int)Math.floor((currentNanoTime - startNanoTime) / 1000000000.0);
                //Do things:

                game.tick(elapsedSeconds, runningTime, pressedButtons, releasedButtons); 
                releasedButtons.clear();
                game.render();
                //System.out.println("FPS : " + (int)(1/elapsedSeconds));
                uiCanvas.getGraphicsContext2D().setFill(Color.DARKRED);
                uiCanvas.getGraphicsContext2D().fillText("FPS : " + (int)(1/elapsedSeconds), 0, 20);
            } 
        }.start();
        
    }

    public static void main (String[] args) {
        logger.info("Generals game launching...");
        Application.launch();
        logger.info("Generals game ended");
    }



    private void setupKeyHandlers(Stage primaryStage) {
    /** KeyPresses and releases are stored separately, so that holding down a button continues to execute commands **/
    primaryStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent e)
            {
                KeyCode code = e.getCode();
                if ( !pressedButtons.contains(code) )
                    pressedButtons.add( code );
                    //logger.log(Level.INFO, "{0} pressed", code);
            }
        });

    primaryStage.getScene().setOnKeyReleased(new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent e)
            {
                KeyCode code = e.getCode();
                pressedButtons.remove( code );
                if (!releasedButtons.contains(code)) 
                    releasedButtons.add( code );
                //logger.log(Level.INFO, "{0} released", code);
            }
        });
    }

    private void loadFonts() {
        fonts = new HashMap<>();
        Font alagard = Font.loadFont(getClass().getResourceAsStream("/fonts/alagard.ttf"), 20);
        fonts.put("alagard", alagard);
        Font alagard20 = Font.loadFont(getClass().getResourceAsStream("/fonts/alagard.ttf"), 20);
        fonts.put("alagard20", alagard20);
        Font alagard12 = Font.loadFont(getClass().getResourceAsStream("/fonts/alagard.ttf"), 12);
        fonts.put("alagard12", alagard12);
        Font romulus = Font.loadFont(getClass().getResourceAsStream("/fonts/romulus.ttf"), 20);
        fonts.put("romulus", romulus);   
        Font romulus20 = Font.loadFont(getClass().getResourceAsStream("/fonts/romulus.ttf"), 20);
        fonts.put("romulus20", romulus20); 
        Font romulus12 = Font.loadFont(getClass().getResourceAsStream("/fonts/romulus.ttf"), 12);
        fonts.put("romulus12", romulus12); 
    }

    private static void setupSoundManager() {
        Generals.soundManager = new SoundManagerJavaFX(5);
    }
    

    private void loadLibraries (Canvas gameCanvas, Canvas uiCanvas) {
        setupGraphicsLibrary();
        logger.info("Graphics library initialized");
        loadCursors();
        logger.info("Cursors loaded");

    }

    private static void setupGraphicsLibrary() {
        Generals.graphLibrary = new GraphicsLibrary();
        GraphicsLibrary.initializeGraphicsLibrary(graphLibrary);
        
    }

    private void loadCursors() {
    	cursors = new HashMap<>();
    	cursors.put("handblue",new ImageCursor(graphLibrary.getImage("cursorHandblue")));
    	cursors.put("handbeige", new ImageCursor(graphLibrary.getImage("cursorHandbeige")));
		cursors.put("handgrey", new ImageCursor(graphLibrary.getImage("cursorHandgrey")));
    	
		cursors.put("gauntletblue",new ImageCursor(graphLibrary.getImage("cursorGauntletblue")));
    	cursors.put("gauntletbronze", new ImageCursor(graphLibrary.getImage("cursorGauntletbronze")));
		cursors.put("gauntletgrey", new ImageCursor(graphLibrary.getImage("cursorGauntletgrey")));
    	
		cursors.put("swordGold",new ImageCursor(graphLibrary.getImage("cursorSwordgold")));
    	cursors.put("swordSilver", new ImageCursor(graphLibrary.getImage("cursorSwordsilver")));
		cursors.put("swordBronze", new ImageCursor(graphLibrary.getImage("cursorSwordbronze")));
    	
    }


    public static void setCursor(String cursorName) {
    	String lowercasename = cursorName.toLowerCase();
    	Cursor cur = cursors.get(lowercasename);
    	if (cur!=null) {
    		if (primaryStage.getScene() == null) Generals.logger.warning("NULL scene!");
    		primaryStage.getScene().setCursor(cur);
    	} else {
    		//If specified cursor wasn't found, change to default cursor
    		primaryStage.getScene().setCursor(Cursor.DEFAULT);
    	}
    }
    
    private void setupMouseHandles(Group root) {
        //Pass the event over to the game
        root.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                game.handleMouseEvent(me);
            }
        });
        root.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                game.handleMouseEvent(me);
            }
        });
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                game.handleMouseEvent(me);
            }
        });
        root.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                game.handleMouseEvent(me);
            }
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                game.handleMouseEvent(me);
            }
        });
        
    }


    private void setupWindowResizeListeners(Scene launchScene) {
                
        launchScene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                Generals.logger.log(Level.INFO, "Width: {0}", newSceneWidth);
                //GeneralsGame.WIDTH = (Double)newSceneWidth;
                //GeneralsGame.updateUI();
            }
        });
        launchScene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                Generals.logger.log(Level.INFO, "Height: {0}", newSceneHeight);
                //GeneralsGame.HEIGHT = (Double)newSceneHeight;
                //GeneralsGame.updateUI();
            }
        });
    }
}