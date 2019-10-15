/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki
 */
package generalsgame.gameobjects;

import java.util.ArrayList;

import generalsgame.graphics.MovingGraphics;
import generalsgame.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Structures are MapObjects with varying collision boxes
 * A single structure may have some parts of it causing collision, others not
 * TODO: utilize ImageView viewport to show pieces from same file
 * @author nkoiv
 */
public class Structure extends MapObject {
    
    //Extra sprites are used as a non-collision part of the structure
    protected ArrayList<Sprite> extraSprites;
  
    public Structure() {
        super();
        this.extraSprites = new ArrayList<>();
    }
    
    public Structure(String name, Image image, int collisionLevel) {
        super(name, image);
        this.collisionLevel = collisionLevel;
        this.extraSprites = new ArrayList<>();
    }
    
    public Structure(String name, MovingGraphics graphics, int collisionLevel) {
        super(name, graphics);
        this.collisionLevel = collisionLevel;
        this.extraSprites = new ArrayList<>();
    }
    
    public void addExtra (Sprite sprite, double xOffset, double yOffset) {
        sprite.setPosition(this.getXPos()+xOffset, this.getYPos()+yOffset);
        this.extraSprites.add(sprite);
    }
    
    public void addExtra (Image image, double xOffset, double yOffset) {
        this.extraSprites.add(new Sprite(
        image, this.getXPos()+xOffset, this.getYPos()+yOffset ));
    }
    
    public ArrayList<Sprite> getExtras() {
        return this.extraSprites;
    }
    
    public void removeExtras() {
        this.extraSprites.clear();
    }

    public void renderExtras (double xOffset, double yOffset, GraphicsContext gc) {
        if (!this.extraSprites.isEmpty()) {
            for (Sprite extraSprite : this.extraSprites) {
                //Mists.logger.info("Rendering extra for " +this.getName());
                extraSprite.render(xOffset, yOffset, gc);
            }
        }
    }
    
    public Sprite getSprite() {
        return (Sprite)this.graphics;
    }
    
    @Override
    public void render(double xOffset, double yOffset, GraphicsContext gc) {
        if (this.isVisible()) {
            super.render(xOffset, yOffset, gc);
            this.renderExtras(xOffset, yOffset, gc);
        }
        
    }
    
    //setPosition is overwritten to move extras along with the main sprite
    @Override
    public void setPosition (double xPos, double yPos) {
        if (!this.extraSprites.isEmpty()) {
            for (Sprite extraSprite : extraSprites) { //All extras are moved keeping the same relation to the main sprite
                double xOffset = this.getSprite().getXPos() - extraSprite.getXPos();
                double yOffset = this.getSprite().getYPos() - extraSprite.getYPos();
                extraSprite.setPosition(xPos-xOffset, yPos-yOffset);
            }
        }
        this.getSprite().setPosition(xPos, yPos);
    }
    
    @Override
    public void setCenterPosition (double xPos, double yPos) {
        this.setPosition(xPos+(this.getSprite().getWidth()/2), yPos+(this.getSprite().getHeight()/2));
    }
    
    @Override
    public String[] getInfoText() {
        ArrayList<String> a = new ArrayList<>();
        a.add(this.name+" - TID#"+this.templateID);
        a.add("ID "+this.IDinMap+" @ "+this.map.getName());
        a.add("X:"+((int)this.getXPos())+" Y:"+((int)this.getYPos()));
        /*
        a.add("Flags:");
        for (String f : this.flags.keySet()) {
            a.add(f+" : "+this.flags.get(f));
        }
        */
        String[] s = new String[a.size()];
        for (int i = 0; i < a.size(); i++) {
            s[i] = a.get(i);
        }
        return s;
    }
    
    
}
