/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */

package generalsgame.graphics;

import java.util.HashMap;
import java.util.logging.Level;

import generalsgame.Generals;

import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * GraphLibrary stores loaded graphics data
 * The main use is to reduce the overhead done
 * by accidentally loading same file over and over
 * @author nikok
 */
public class GraphicsLibrary {
    private final HashMap<String, Image> gallery;
    private final HashMap<String, Image[]> setgallery;
 
    public GraphicsLibrary() {
        this.gallery = new HashMap<>();
        this.setgallery = new HashMap<>();
    }
    
    
    public void addImage(String name, Image i) {
        String lowercasename = name.toLowerCase();
        if (this.containsImage(lowercasename)) {
            Generals.logger.log(Level.WARNING, "Graphics Library already contains {0}", name);
            return;
        }
        this.gallery.put(lowercasename, i);
    }
    
    public void addImage(String name, ImageView iw, int xCoor, int yCoor, int width, int height) {
        WritableImage snapshot = null;
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        iw.setViewport(new Rectangle2D(xCoor, yCoor, width, height));
        WritableImage image = iw.snapshot(parameters, snapshot);
        this.addImage(name, image);
    }
    

    public void addImageSet(String name, Image... images) {
        String lowercasename = name.toLowerCase();
        if (this.containsImage(lowercasename)) {
            Generals.logger.log(Level.WARNING, "Graphics Set Library already contains {0}", name);
            return;
        }
        this.setgallery.put(lowercasename, images);
    }
    
    public void addImageSet(String name, ImageView iw, int columns, int rows, int width, int height) {
        WritableImage snapshot = null;
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        Image[] images = new Image[columns * rows];
        int counter = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                iw.setViewport(new Rectangle2D(x*width, y*height, width, height));
                WritableImage image = iw.snapshot(parameters, snapshot);
                images[counter] = image;
                counter++;
            }
        }
        
        this.addImageSet(name, images);
    }
    
    public Image getImage(String name) {
        String lowercasename = name.toLowerCase();
        Image i = this.gallery.get(lowercasename);
        if (i == null) Generals.logger.warning("GraphLibrary couldn't find: '"+name+"'");
        return i;
    }
    
    public Image[] getImageSet(String name) {
        String lowercasename = name.toLowerCase();
        return this.setgallery.get(lowercasename);
    }
    
    public boolean containsImage(String name) {
        String lowercasename = name.toLowerCase();
        return this.gallery.containsKey(lowercasename);
    }
    public boolean containsImageSet(String name) {
        String lowercasename = name.toLowerCase();
        return this.setgallery.containsKey(lowercasename);
    }
    

    public static void initializeGraphicsLibrary(GraphicsLibrary lib) {

    	//---Base elements---
        lib.addImage("blank", new Image("/images/blank.png"));
        lib.addImage("black", new Image("/images/black.png"));
        
        lib.addImage("lightspot", new Image("/images/light.png"));
        
    
        //---UI elements----
        //Panels
        lib.addImageSet("panelBeige", new ImageView("/images/ui/panel_beige.png"), 4, 4, 25, 25);
        lib.addImageSet("panelBrown", new ImageView("/images/ui/panel_brown.png"), 4, 4, 25, 25);
        lib.addImageSet("panelBeigeLight", new ImageView("/images/ui/panel_beigeLight.png"), 4, 4, 25, 25);
        lib.addImageSet("panelBlue", new ImageView("/images/ui/panel_blue.png"), 4, 4, 25, 25);
        
        lib.addImageSet("panelInsetBeige", new ImageView("/images/ui/panelInset_beige.png"), 4, 4, 25, 25);
        lib.addImageSet("panelInsetBrown", new ImageView("/images/ui/panelInset_brown.png"), 4, 4, 25, 25);
        lib.addImageSet("panelInsetBeigeLight", new ImageView("/images/ui/panelInset_beigeLight.png"), 4, 4, 25, 25);
        lib.addImageSet("panelInsetBlue", new ImageView("/images/ui/panelInset_blue.png"), 4, 4, 25, 25);
        
        //Bars
        lib.addImage("barGreenHorizontalLeft", new Image("/images/ui/barGreen_horizontalLeft.png"));
        lib.addImage("barGreenHorizontalMid", new Image("/images/ui/barGreen_horizontalMid.png"));
        lib.addImage("barGreenHorizontalRight", new Image("/images/ui/barGreen_horizontalRight.png"));
        lib.addImage("barRedHorizontalLeft", new Image("/images/ui/barRed_horizontalLeft.png"));
        lib.addImage("barRedHorizontalMid", new Image("/images/ui/barRed_horizontalMid.png"));
        lib.addImage("barRedHorizontalRight", new Image("/images/ui/barRed_horizontalRight.png"));
        lib.addImage("barBackHorizontalLeft", new Image("/images/ui/barBack_horizontalLeft.png"));
        lib.addImage("barBackHorizontalMid", new Image("/images/ui/barBack_horizontalMid.png"));
        lib.addImage("barBackHorizontalRight", new Image("/images/ui/barBack_horizontalRight.png"));
        
        //Cursors
        lib.addImage("cursorHandblue", new Image("/images/ui/cursorHand_blue.png"));
        lib.addImage("cursorHandbeige", new Image("/images/ui/cursorHand_beige.png"));
        lib.addImage("cursorHandgrey", new Image("/images/ui/cursorHand_grey.png"));
        
        lib.addImage("cursorGauntletblue", new Image("/images/ui/cursorGauntlet_blue.png"));
        lib.addImage("cursorGauntletbronze", new Image("/images/ui/cursorGauntlet_bronze.png"));
        lib.addImage("cursorGauntletgrey", new Image("/images/ui/cursorGauntlet_grey.png"));
        lib.addImage("cursorGauntletSmallbronze", new Image("/images/ui/cursorGauntletSmall_bronze.png"));
        
        lib.addImage("cursorSwordgold", new Image("/images/ui/cursorSword_gold.png"));
        lib.addImage("cursorSwordsilver", new Image("/images/ui/cursorSword_silver.png"));
        lib.addImage("cursorSwordbronze", new Image("/images/ui/cursorSword_bronze.png"));
        
        //Buttons
        lib.addImage("buttonSquareBeige", new Image("/images/ui/buttonSquare_beige.png"));
        lib.addImage("buttonSquareBeigePressed", new Image("/images/ui/buttonSquare_beige_pressed.png"));
        
        lib.addImage("iconCheckBeige", new Image("/images/ui/iconCheck_beige.png"));
        lib.addImage("iconCheckBlue", new Image("/images/ui/iconCheck_blue.png"));
        lib.addImage("iconCheckBronze", new Image("/images/ui/iconCheck_bronze.png"));
        
        lib.addImage("iconCrossBeige", new Image("/images/ui/iconCross_beige.png"));
        lib.addImage("iconCrossBlue", new Image("/images/ui/iconCross_blue.png"));
        lib.addImage("iconCrossBronze", new Image("/images/ui/iconCross_bronze.png"));
        
        lib.addImage("iconCircleBeige", new Image("/images/ui/iconCircle_beige.png"));
        lib.addImage("iconCircleBlue", new Image("/images/ui/iconCircle_blue.png"));
        lib.addImage("iconCircleronze", new Image("/images/ui/iconCircle_bronze.png"));
        
        //Button bars
        lib.addImageSet("buttonLongBeige", new ImageView("/images/ui/buttonLong_beige.png"), 4, 1, 48, 49);
        lib.addImageSet("buttonLongBrown", new ImageView("/images/ui/buttonLong_brown.png"), 4, 1, 48, 49);
        lib.addImageSet("buttonLongGrey", new ImageView("/images/ui/buttonLong_grey.png"), 4, 1, 48, 49);
        lib.addImageSet("buttonLongBlue", new ImageView("/images/ui/buttonLong_blue.png"), 4, 1, 48, 49);
        
        lib.addImageSet("buttonLongBeigePressed", new ImageView("/images/ui/buttonLong_beige_pressed.png"), 4, 1, 48, 45);
        lib.addImageSet("buttonLongBrownPressed", new ImageView("/images/ui/buttonLong_brown_pressed.png"), 4, 1, 48, 45);
        lib.addImageSet("buttonLongGreyPressed", new ImageView("/images/ui/buttonLong_grey_pressed.png"), 4, 1, 48, 45);
        lib.addImageSet("buttonLongBluePressed", new ImageView("/images/ui/buttonLong_blue_pressed.png"), 4, 1, 48, 45);
        
        
        //Icons
        lib.addImage("musicOnIcon", new Image("/images/ui/musicOn.png"));
        lib.addImage("musicOffIcon", new Image("/images/ui/musicOff.png"));
        
        lib.addImage("charsheetIcon", new Image("/images/ui/charsheetIcon.png"));
        lib.addImage("inventoryIcon", new Image("/images/ui/inventoryIcon.png"));
        lib.addImage("locationmenuIcon", new Image("/images/ui/locationmenuIcon.png"));
        lib.addImage("questlogIcon", new Image("/images/ui/questlogIcon.png"));
        
        lib.addImage("buttonSelectbeige", new Image("/images/ui/buttonSquareSelect_beige.png"));
        lib.addImage("buttonSelectSmallbeige", new Image("/images/ui/buttonSquareSelectSmall_beige.png"));
        
        //Icons
        lib.addImage("circle64", new Image("/images/circle_64.png"));
        lib.addImage("circle32", new Image("/images/circle_32.png"));
    }
    

}
