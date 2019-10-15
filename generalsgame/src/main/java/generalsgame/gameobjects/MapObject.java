package generalsgame.gameobjects;

import java.util.HashMap;
import java.util.Objects;

import generalsgame.Direction;
import generalsgame.Generals;
import generalsgame.gamestate.BattleState;
import generalsgame.BattleMap;
import generalsgame.graphics.MovingGraphics;
import generalsgame.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

public class MapObject {

    protected int templateID;
    protected String name;
    protected MovingGraphics graphics;
    protected boolean visible;
    
    protected int collisionLevel;
    protected boolean removable;
    protected BattleMap map;
    protected double lightSize;
    protected Color lightColor;
    
    protected int IDinMap;
    
    public MapObject() {
        this.graphics = new Sprite();
    }
    
    public MapObject (String name) {
        this();
        this.name = name;
        this.visible = true;
    }

    
    public MapObject (String name, Image image) {
        this(name, new Sprite(image));
    }
    
    public MapObject (String name, MovingGraphics graphics) {
        this(name);
        this.graphics = graphics;
    }
    
    public boolean addTextPopup(String text) {
        if (Generals.game.currentState instanceof BattleState) {
            ((BattleState)Generals.game.currentState).addTextFloat(text, this);
            return true;
        }
        return false;
    }
    
    public void setBattleMap(BattleMap map) {
        this.map = map;
    }
    
    public void setPosition (double xPos, double yPos) {
        this.graphics.setPosition(xPos, yPos);
    }
    
    /**
     * Sets the (sprite) position so that the center of the sprite
     * (instead of top left corner) is at the given coordinates.
     * @param xPos Center position of the sprite on X
     * @param yPos Center position of the sprite on Y
     */
    public void setCenterPosition (double xPos, double yPos) {
        this.graphics.setCenterPosition(xPos, yPos);
    }
    
    public int getCollisionLevel() {
        return this.collisionLevel;
    }
    
    public void setCollisionLevel(int cl) {
        this.collisionLevel = cl;
    }
       
    public BattleMap getBattleMap() {
        return this.map;
    }
    
    public double getCenterXPos() {
        return this.graphics.getCenterXPos();
    }
    
    public double getCenterYPos(){
        return this.graphics.getCenterYPos();
    }
    
    public double getXPos(){
        return this.graphics.getXPos();
    }
    
    public double getYPos(){
        return this.graphics.getYPos();
    }
    
    public double getWidth() {
        return this.graphics.getWidth();
    }
    
    public double getHeight() {
        return this.graphics.getHeight();
    }
    
    public double getLightSize() {
        return this.lightSize;
    }
    
    public void setLightSize(double lightsize) {
        this.lightSize = lightsize;
    }

    public Color getLightColor() {
        return lightColor;
    }

    public void setLightColor(Color lightColor) {
        this.lightColor = lightColor;
    }
    
    
    
    public Double[] getCorner(Direction d) {
        return this.getGraphics().getCorner(d);
    }
    
    public boolean intersects(MapObject mob) {
        //if (!"trigger radius".equals(mob.getName()) && !"trigger radius".equals(name))Mists.logger.info("Checking intersection between "+this.getName()+" and "+mob.getName()); 
        return this.graphics.intersects(mob.getGraphics());
    }
    
    public boolean intersects(Shape s) {
        return this.graphics.intersectsWithShape(s);
    }
    
    /**
    * Render draws the Sprite of the MapObject on a given GraphicsContext
    * @param gc GraphicsContext where the object is drawn
    * @param xOffset Used to shift the objects xCoordinate so its drawn where the screen is
    * @param yOffset Used to shift the objects yCoordinate so its drawn where the screen is
    */
    public void render(double xOffset, double yOffset, GraphicsContext gc) {
        if (this.isVisible()) {
            this.graphics.render(xOffset, yOffset, gc);
        }
    }
    
    public void renderCollisions(double xOffset, double yOffset, GraphicsContext gc) {
        if (this.isVisible()) {
            this.graphics.renderCollisions(xOffset, yOffset, gc);
        }
    }
   
    /**
    * Set the MapObject a new Sprite (replacing the old old)
    * @param sprite Sprite to be added
    */
    public void setSprite(Sprite sprite) {
        this.graphics = sprite;
    }
    
    /**
    * Update the position of the MapObject
    * @param time Amount of time passed since the last update
    */
    public void update(double time) {
        this.graphics.update(time);
    }
    
    protected MovingGraphics getGraphics() {
        return this.graphics;
    }
    
    public int getTemplateID() {
        return this.templateID;
    }
    
    public void setTemplateID(int baseID) {
        this.templateID = baseID;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Image getSnapshot() {
        return this.graphics.getImage();
    }
    
    public boolean isVisible() {
    	return this.visible;
    }
    
    public void setVisibility(boolean visible) {
    	this.visible = visible;
    }
    
    /**
     * Set the removable variable by hand
     * @param removable Value to set removable to (true=get removed on next tick)
     */
    public void setRemovable(boolean removable) {
        this.removable = removable;
    }
    
    /**
     * Set the removable parameter to true
     */
    public void remove() {
        this.removable = true;
    }
    
    /**
     * Controls whether or not the mob is to be removed on next
     * pass. Mobs are not removed from locations instantly to
     * avoid concurrent modification errors.
     * @return True if the mob is to be removed on next pass
     */
    public boolean isRemovable() {
        return this.removable;
    }
    
    public void setID(int IDinLocation) {
        this.IDinMap = IDinLocation;
    }
    
    public int getID() {
        return this.IDinMap;
    }
    
    public String[] getInfoText() {
        String[] s = new String[]{
            this.name,
            "ID "+this.IDinMap+" @ "+this.map.getName(),
            "X:"+((int)this.getXPos())+" Y:"+((int)this.getYPos())};
        return s;
    }
    
    @Override
    public String toString(){
        String s;
        if (this.map == null) s = this.name+" at Limbo";
        else s = this.name + " (ID:"+this.IDinMap+") @ |"  + this.graphics.getXPos()+","+this.graphics.getYPos()+"|";
        return s;
    }

    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.templateID;
        hash = 83 * hash + Objects.hashCode(this.map);
        hash = 83 * hash + this.IDinMap;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MapObject other = (MapObject) obj;
        if (this.templateID != other.templateID) {
            return false;
        }
        if (this.name != other.name) {
        	return false;
        }
        if (this.IDinMap != other.IDinMap) {
            return false;
        }
        if (!Objects.equals(this.map, other.map)) {
            return false;
        }
        return true;
    }

	
	protected void readGraphicsFromLibrary(int templateID, double xCoor, double yCoor) {
        /*
        //Separate loading for structures TODO
		if (this instanceof Structure && Generals.structureLibrary != null) {
			Structure dummy = Generals.structureLibrary.create(templateID);
			if (dummy != null) this.graphics = dummy.graphics;
        }
        */
		if (this.graphics == null) this.graphics = new Sprite(); //Blank sprite if generation from Library failed
		this.graphics.setPosition(xCoor, yCoor);
	}
    
    
}
