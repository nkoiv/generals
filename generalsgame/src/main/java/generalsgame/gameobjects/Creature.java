/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */

package generalsgame.gameobjects;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.logging.Level;

import generalsgame.Direction;
import generalsgame.Generals;
import generalsgame.commands.Command;
import generalsgame.graphics.MovingGraphics;
import generalsgame.graphics.Sprite;
import generalsgame.graphics.SpriteAnimation;
import generalsgame.util.Toolkit;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Creature is a "living" MapObject
 * As such, they get (at least some) AI-routines
 * @author nkoiv
 */
public class Creature extends MapObject { 

    private Command activeCommand;
    
    protected HashMap<String, Integer> attributes;
    private Direction facing; //For sprites that turn when they move
    private Direction lastFacing;

    //Old position to snap back to when colliding;
    private double oldXPos;
    private double oldYPos;
    private boolean blocked;
    protected ArrayList<Integer> crossableTerrain; //List of terrains we can go through;

    public Creature (String name, Image image) {
        super(name, new Sprite(image));
        this.crossableTerrain = new ArrayList<>();
        this.crossableTerrain.add(0);
        this.initializeAttributes();
    }

    private void initializeAttributes() {
    	this.attributes = new HashMap<>();
        this.setAttribute("Speed", 50);
        this.setAttribute("MaxHealth", 100 );
        this.setAttribute("Health", 100);
        this.blocked = false;
    }

        

    public int getHealth() {
        return this.getAttribute("Health");
    }

    public int getMaxHealth() {
        return this.getAttribute("MaxHealth");
    }

    public void setAttribute (String attribute, int value) {
        if (this.attributes.containsKey(attribute)) {
            this.attributes.replace(attribute, value);
        } else {
            this.attributes.put(attribute, value);
        }
    }

    /* GetAttribute returns 0 when attribute is not found.
    *  If creature has no Armour, "getAttribute("Armour") gives 0.
    *  Due to this no combat mechanics should ever use division by attributes.
    */
    public int getAttribute(String attribute) {
        if (this.attributes.containsKey(attribute)) {
            return this.attributes.get(attribute);
        } else {
            return 0;
        }
    }

    public void setCommand(Command command) {
        this.activeCommand = command;
    }
    
    public Command getCurrentCommand() {
        return this.activeCommand;
    }

    public void progressCommand(double time, int elapsedSeconds) {
        if (this.activeCommand == null) return;
        //Generals.logger.info("Progressing Command....");
        Generals.logger.info(" - completion time: " + this.activeCommand.getCompletionTime() +" ElapsedSeconds:" + elapsedSeconds );
        this.activeCommand.tick(time);
        if (this.activeCommand.getCompletionTime() <= elapsedSeconds) {
            this.activeCommand = null;
            this.stopMovement();
            Generals.logger.info("Stopped movement");
        }
    }

    //-------------movement-----------
   
    /**
     * Apply movement to the creature.
     * The movement is done by creature sprites x and y velocity,
     * multiplied by time spent moving.
     * @param time Time spent moving
     * @return Return true if movement was possible, false if it was blocked
     */ 
    public boolean applyMovement(double time){
        if (this.map == null) return false;
        /*
        * Check collisions before movement
        * TODO: Add in pixel-based collision detection (compare alphamaps?)
        * TODO: Make collisions respect collisionlevel.
        */
        this.oldXPos = this.getGraphics().getXPos();
        this.oldYPos = this.getGraphics().getYPos();
        //Generals.logger.info("Old positions: "+this.oldXPos+","+this.oldYPos);
        
        ArrayList<MapObject> collidedObjects = this.getBattleMap().checkCollisions(this);
        if (collidedObjects.isEmpty()) {
            //Collided with nothing, free to move
            if (this.blocked) {
                this.blocked = false; //movement went through fine 
                //Generals.logger.info("Movement blocked!");
            }
            this.getGraphics().update(time);
            return true;
        } else { 
            //Check which sides we collided on
            EnumSet<Direction> collidedSides = this.getBattleMap().collidedSides(this, collidedObjects, this.map.getZone().getWidth(), this.map.getZone().getHeight());
            //Generals.logger.info("Checked collisions, found " +collidedSides.toString());
            if (collidedSides.contains(Direction.UP)) {
                //Block movement up
                if (this.getGraphics().getYVelocity() < 0 ) { 
                    //this.getGraphics().setYVelocity(0);
                    this.getGraphics().setYVelocity(-this.getGraphics().getYVelocity()/2);
                }
                this.getGraphics().setYPosition(this.oldYPos);
            }
            if (collidedSides.contains(Direction.DOWN)) {
                //Block movement down
                if (this.getGraphics().getYVelocity() > 0 ) {
                    //this.getGraphics().setYVelocity(0);
                    this.getGraphics().setYVelocity(-this.getGraphics().getYVelocity()/2);
                }
                this.getGraphics().setYPosition(this.oldYPos);
            }
            if (collidedSides.contains(Direction.RIGHT)) {
                //Block movement right
                if (this.getGraphics().getXVelocity() > 0 ) {
                    //this.getGraphics().setXVelocity(0);
                    this.getGraphics().setXVelocity(-this.getGraphics().getXVelocity()/2);
                }
                this.getGraphics().setXPosition(this.oldXPos);
            }
            if (collidedSides.contains(Direction.LEFT)) {
                //Block movement left
                if (this.getGraphics().getXVelocity() < 0 ) {
                    //this.getGraphics().setXVelocity(0);
                    this.getGraphics().setXVelocity(-this.getGraphics().getXVelocity()/2);
                }
                this.getGraphics().setXPosition(this.oldXPos);
                
            }
            if (this.getXPos() < 0 || this.getXPos() > this.map.getZone().getWidth()) this.graphics.setXPosition(this.oldXPos);
            if (this.getYPos() < 0 || this.getYPos() > this.map.getZone().getHeight()) this.graphics.setXPosition(this.oldYPos);
            this.getGraphics().update(time);
            this.blocked = true; //remember this was a bad way to go to
            return false;
        }
    }
    
    
    public boolean moveTowards (double xCoor, double yCoor) {
        this.getGraphics().setVelocity(0, 0);
        double[] direction = Toolkit.getDirectionXY(this.getCenterXPos(), this.getCenterYPos(), xCoor, yCoor);
        direction[0] = direction[0] * this.getAttribute("Speed");
        direction[1] = direction[1] * this.getAttribute("Speed");
        this.graphics.addVelocity(direction[0], direction[1]);
        //Update facing
        this.setFacing(Toolkit.getDirection(0, 0, direction[0], direction[1]));
        return true;
    }
    

    public boolean moveTowards (Direction direction) {
        //this.stopMovement(); //clear old movement (velocity)
        switch(direction) {
            case UP: {
                this.facing = Direction.UP;
                this.getGraphics().setVelocity(0, -this.getAttribute("Speed"));
                return true;
            }
            case DOWN: {
                this.facing = Direction.DOWN;
                this.getGraphics().setVelocity(0, this.getAttribute("Speed"));
                return true;
            }
            case LEFT: {
                this.facing = Direction.LEFT;
                this.getGraphics().setVelocity(-this.getAttribute("Speed"), 0);
                return true;
            }
            case RIGHT: {
                this.facing = Direction.RIGHT;
                this.getGraphics().setVelocity(this.getAttribute("Speed"), 0);
                return true;
            }
            case UPRIGHT: { 
                this.getGraphics().setVelocity(this.getAttribute("Speed")/1.41, -this.getAttribute("Speed")/1.41);
                this.facing = Direction.UPRIGHT;
                return true;
            }
            case UPLEFT: {
                this.getGraphics().setVelocity(-this.getAttribute("Speed")/1.41, -this.getAttribute("Speed")/1.41);
                this.facing = Direction.UPLEFT;
                return true;
            }
            case DOWNRIGHT: {
                this.getGraphics().setVelocity(this.getAttribute("Speed")/1.41, this.getAttribute("Speed")/1.41);
                this.facing = Direction.DOWNRIGHT;
                return true;
            }
            case DOWNLEFT: {
                this.getGraphics().setVelocity(-this.getAttribute("Speed")/1.41, this.getAttribute("Speed")/1.41);
                this.facing = Direction.DOWNLEFT;
                return true;
            }
            case STAY: {
                return stopMovement();
            }
        default: break;
        }
        
        return false;
    }

    public boolean stopMovement() {
        this.getGraphics().setVelocity(0, 0);
        //this.getGraphics().setImage(this.getGraphics().getImage());
        return true;
    }

    public Direction getFacing() {
        return this.facing;
    }
    
    public void setFacing(Direction d) {
        this.facing = d;
    }

    public ArrayList<Integer> getCrossableTerrain() {
        return this.crossableTerrain;
    }

    @Override
    public void update (double time, int elapsedSeconds) {
        //Generals.logger.info(this.name + " is acting...");
        this.progressCommand(time, elapsedSeconds);
        this.graphics.update(time);
    }


    @Override
    public String[] getInfoText() {
        String[] s = new String[]{
            this.name + " - BaseID: "+this.templateID ,
            "ID "+this.IDinMap+" @ "+this.map.getName(),
            "X:"+((int)this.getXPos())+" Y:"+((int)this.getYPos()),
            this.getHealth() + "/"+this.getMaxHealth()+" hp",
        };
        return s;
    }
    
    @Override
    public String toString() {
        String n = this.name + " @ "+"|"+(int)this.getCenterXPos()+"x"+(int)this.getCenterYPos()+"|";
        return n;
    }
    
    public String longString() {
        String n = this.name + " @ "+"|"+(int)this.getCenterXPos()+"x"+(int)this.getCenterYPos()+"|\n";
        n = n+this.map+"\n";
        n = n+"Health: "+this.getHealth()+" / "+this.getMaxHealth()+"\n";
        
        return n;
    }
    
    }
