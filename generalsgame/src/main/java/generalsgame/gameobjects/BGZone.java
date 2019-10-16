/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */
package generalsgame.gameobjects;

import java.util.ArrayList;

import generalsgame.Generals;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 *
 * @author nkoiv
 */
public class BGZone implements Zone {
    
    private Image image;
    private double width;
    private double height;
    
    public BGZone (Image i) {
        this.image = i;
        this.width = i.getWidth();
        this.height = i.getHeight();
        Generals.logger.info("Generated a BGZone");
    }
    
    @Override
    public void render(double xOffset, double yOffset, GraphicsContext gc) {
        
        gc.drawImage( image, xOffset, yOffset );
    }

    @Override
    public ArrayList<Structure> getStaticStructures() {
        //BGmaps have no static structures 
        //So empty list is returned (TODO: at least yet)
        ArrayList<Structure> staticStructures = new ArrayList<>();
        return staticStructures;
    }
    
    @Override
    public double getWidth() {
        return this.width;
    }

    @Override
    public double getHeight() {
        return this.height;
    }

    
}
