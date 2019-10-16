/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */
package generalsgame.gameobjects;

import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author nkoiv
 */
public interface Zone {
    
    //Render the map background
    void render(double xOffset, double yOffset, GraphicsContext gc);
    
    //If the map has static stuctures (walls etc), get them for the location
    ArrayList<Structure> getStaticStructures();
    
    //Map size
    double getWidth();
    double getHeight();
    
}
