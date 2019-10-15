package generalsgame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import generalsgame.Direction;
import generalsgame.gameobjects.*;
import generalsgame.util.CollisionMap;
import generalsgame.util.PathFinder;

public class BattleMap {
    private String name;
    private Zone currentZone;
    private final double[] lastOffsets = new double[2];

    private ArrayList<MapObject> lastRenderedMapObjects = new ArrayList<>();
    private HashMap<Integer, HashSet> creatureSpatial; //New idea for lessening collision detection load
    private HashMap<Integer, HashSet> structureSpatial; //New idea for lessening collision detection load
    private ArrayList<Creature> creatures;
    private ArrayList<Structure> structures;
    private List<MapObject> targets;

    private CollisionMap collisionMap;
    private PathFinder pathFinder;

    private MapObject screenFocus;
    
    public BattleMap() {
        this.name = "TestMap";
    }

    public String getName() {
        return(this.name);
    }

    public double getLastxOffset () {
        return this.lastOffsets[0];
    }
    public double getLastyOffset () {
        return this.lastOffsets[1];
    }
    

    /**
     * General render method for the location, called 60 times per second
     * by default. Render only updates the given GraphicsContext with what's
     * on the current viewport (dictated by xOffset, yOffset and screen width/height)
     * All the location logic should be handled under tick()
     * @param gc GraphicsContext for the location graphics
     * @param sc Shadow layer drawn on top of the graphics
     */
    public void render (GraphicsContext gc) {
        /*
        * Update Offsets first to know which parts of the location are drawn
        */
        double xOffset = getxOffset(gc, screenFocus.getXPos());
        double yOffset = getyOffset(gc, screenFocus.getYPos());
        //Mists.logger.info("Offset: "+xOffset+","+yOffset);
        this.renderMap(gc, xOffset, yOffset);
        this.lastRenderedMapObjects = this.renderMobs(gc, xOffset, yOffset);
        //this.renderLights(sc, lastRenderedMapObjects, xOffset, yOffset);
    }

        /**
     * Render all the MOBs (creature & structure)
     * on the location that is visible. Returns the list of objects that were rendered
     * @param gc Graphics context to render on
     * @param xOffset Offset for rendering (centered on player usually)
     * @param yOffset Offset for rendering (centered on player usually)
     */
    private ArrayList<MapObject> renderMobs(GraphicsContext gc, double xOffset, double yOffset) {
        ArrayList<MapObject> renderedMOBs = new ArrayList<>();
        //renderedMOBs.addAll(renderStructures(gc, xOffset, yOffset));
        renderedMOBs.addAll(renderCreatures(gc, xOffset, yOffset));
        return renderedMOBs;
    }

    private ArrayList<MapObject> renderCreatures(GraphicsContext gc, double xOffset, double yOffset) {
        /*
       * TODO: Consider rendering mobs in order so that those closer to bottom of the screen overlap those higher up.
       */
      ArrayList<MapObject> renderedMOBs = new ArrayList<>();
       //Find the creatures to render
       if (!this.creatures.isEmpty()) {
           for (Creature mob : this.creatures) {
               /*Always render any creatures with lights on
               * It's probably cheaper to just render them out of screen than it is to iterate
               * through the list again to see which creatures have lights
               * (lights often shine further than creature graphics do)
               */
               if (mob.getLightSize() > 0) {
                   mob.render(xOffset, yOffset, gc); //Draw objects on the ground
                   renderedMOBs.add(mob);
                   continue;
               }
               if (mob.getXPos()-xOffset < -mob.getWidth() ||
                   mob.getXPos()-xOffset > gc.getCanvas().getWidth()) {
                   //Mob is not in window
               } else if (mob.getYPos()-yOffset < -mob.getHeight() ||
                   mob.getYPos()-yOffset > gc.getCanvas().getHeight()) {
                   //Mob is not in window
               } else {
                   //Mob is in window
                   renderedMOBs.add(mob);
                   /**
                    * // Draw collision boxes for debugging purposes, if the Global variable is set
                    * if (Generals.DRAW_COLLISIONS) { 
                    *   this.renderCollisions(mob, gc, xOffset, yOffset);
                    * }
                    **/
               }
           }
       }
       renderedMOBs.sort(new CoordinateComparator());
       for (MapObject mob : renderedMOBs) {
           mob.render(xOffset, yOffset, gc); //Draw objects on the ground
       }
       
       
       return renderedMOBs;
    }

    private void renderMap(GraphicsContext gc, double xOffset, double yOffset) {
        this.currentZone.render(-xOffset, -yOffset, gc);
    }

/**
     * xOffset is calculated from the position of the target in
     * regards to the current window width. If the target would be
     * outside viewable area, it's given offset to keep it inside the bounds
     * @param gc GraphicsContext for window bounds
     * @param xPos the xCoordinate of the target we're following
     * @return xOffset for the current screen position
     */
    public double getxOffset(GraphicsContext gc, double xPos){
        double windowWidth = gc.getCanvas().getWidth();
	//Calculate Offset to ensure Player is centered on the screen
        double xOffset = xPos - (windowWidth / 2);
        //Prevent leaving the screen
        if (xOffset < 0) {
            xOffset = 0;
        } else if (xOffset > currentZone.getWidth() -(windowWidth)) {
            xOffset = currentZone.getWidth() - (windowWidth);
        }
        this.lastOffsets[0] = xOffset;
        return xOffset;
	}

     /**
     * yOffset is calculated from the position of the target in
     * regards to the current window width. If the target would be
     * outside viewable area, it's given offset to keep it inside the bounds
     * @param gc GraphicsContext for window bounds
     * @param yPos the yCoordinate of the target we're following
     * @return yOffset for the current screen position
     */
    public double getyOffset(GraphicsContext gc, double yPos){
        double windowHeight = gc.getCanvas().getHeight();
        //Calculate Offset to ensure Player is centered on the screen
        double yOffset = yPos - (windowHeight / 2);
        //Prevent leaving the screen
        if (yOffset < 0) {
            yOffset = 0;
        } else if (yOffset > currentZone.getHeight() -(windowHeight)) {
            yOffset = currentZone.getHeight() - (windowHeight);
        }
        this.lastOffsets[1] = yOffset;
        return yOffset;
    }
    

    /**
     * When getting a MapObject by coordinates with mouseclick
     * or something, it's often needed to substract xOffset and yOffset
     * from coords. Returns the FIRST creature found at the spot.
     * If no creature is found, returns the first Structure at location.
     * 
     * @param xCoor xCoordinate of the search spot
     * @param yCoor yCoordinate of the search spot
     * @return Creature found at the coordinates
     */
    public MapObject getMobAtLocation(double xCoor, double yCoor) {
        MapObject mobAtLocation = null;
        int spatialID = this.getSpatial(xCoor, yCoor);
        if (!this.creatureSpatial.isEmpty()) {
            HashSet<Creature> spatial = this.creatureSpatial.get(spatialID);
            if (spatial != null) {
                for (Creature mob : spatial) {
                    if (xCoor >= mob.getXPos() && xCoor <= mob.getXPos()+mob.getWidth() &&
                            yCoor >= mob.getYPos() && yCoor <= mob.getYPos()+mob.getHeight()) {
                            //Do a pixelcheck on the mob;
                            //if (Sprite.pixelCollision(xCoor, yCoor, Mists.pixel, mob.getXPos(), mob.getYPos(), mob.getSprite().getImage())) {
                            return mob;
                            //}   
                        }
                }
            }
        }
        Structure s = (getStructureAtLocation(xCoor, yCoor));
        if (s!=null) mobAtLocation = s;
        return mobAtLocation;
    }
    
    public Structure getStructureAtLocation(double xCoor, double yCoor) {
        if (!this.structures.isEmpty()) {
            //Check the collisionmap first
            //No structure on collisionmap means no need to iterate through all structures (or spatials)
            if (this.collisionMap.isBlocked(0, (int)xCoor * collisionMap.nodeSize, (int)yCoor * collisionMap.nodeSize)) {
                for (Structure mob : this.structures) {
                    if (xCoor >= mob.getXPos() && xCoor <= mob.getXPos()+mob.getWidth()) {
                        if (yCoor >= mob.getYPos() && yCoor <= mob.getYPos()+mob.getHeight()) {
                            return mob;
                        }
                    }

                }
            }
        }
        return null;
    }

    private void updateCreatureSpatial() {
        if (this.creatureSpatial == null) this.creatureSpatial = new HashMap<>();
        if (this.creatureSpatial != null) this.creatureSpatial.clear();
        
        for (MapObject creep : this.creatures) {
            HashSet<Integer> mobSpatials = getSpatials(creep);
            for (Integer i : mobSpatials) {
                addToSpatial(creep, i, this.creatureSpatial);
            }
        }

        /* [ 0][ 1][ 2][ 3][ 4]
        *  [ 5][ 6][ 7][ 8][ 9]
        *  [10][11][12][13][14]
        *  [15][16][17][18][19]
        * Above a spatial node is the node number -5,
        * below it is node number +5 and sides are +/- 1
        */
        
    }
    
    private void updateStructureSpatial() {
        if (this.structureSpatial == null) this.structureSpatial = new HashMap<>();
        if (this.structureSpatial != null) this.structureSpatial.clear();
        
        for (MapObject struct : this.structures) {
            HashSet<Integer> mobSpatials = getSpatials(struct);
            for (Integer i : mobSpatials) {
                addToSpatial(struct, i, this.structureSpatial);
            }
        }
    }
    
    private static void addToSpatial (MapObject mob, int spatialID, HashMap<Integer, HashSet> spatial) {
        if (spatial.get(spatialID) == null) spatial.put(spatialID, new HashSet<MapObject>());
        spatial.get(spatialID).add(mob);
    }
    
    /**
     * Get all the spatial hash IDs a given mob is located in
     * @param mob MapObject to check for
     * @return set of spatial hash ID's
     */
    private HashSet<Integer> getSpatials(MapObject mob) {
        Double[] spatialUpLeft;
        Double[] spatialUpRight;
        Double[] spatialDownRight;
        Double[] spatialDownLeft;
        HashSet<Integer> spatialList = new HashSet<>();
        spatialUpLeft = mob.getCorner(Direction.UPLEFT);
        spatialList.add(getSpatial(spatialUpLeft[0],spatialUpLeft[1]));

        spatialUpRight = mob.getCorner(Direction.UPRIGHT);
        spatialList.add(getSpatial(spatialUpRight[0],spatialUpRight[1]));
        
        spatialDownRight = mob.getCorner(Direction.DOWNRIGHT);
        spatialList.add(getSpatial(spatialDownRight[0],spatialDownRight[1]));
        
        spatialDownLeft = mob.getCorner(Direction.DOWNLEFT);
        spatialList.add(getSpatial(spatialDownLeft[0],spatialDownLeft[1]));
        
        return spatialList;
    }
    
    /**
     * Get the spatial hash ID for given coordinates
     * @param xCoor
     * @param yCoor
     * @return Spatial hash ID
     */
    private int getSpatial (double xCoor, double yCoor) {
        int spatialsPerRow = 5; //TODO: Calculate these from map size?
        int spatialRows = 5; //Or maybe map fillrate?
        int sC = (int)this.currentZone.getWidth()/spatialsPerRow;
        int sR = (int)this.currentZone.getHeight()/spatialRows;
        
        return ((int)(xCoor / sC) * (int)(yCoor / sR));
    }

    public ArrayList<MapObject> getLastRenderedObjects() {
        return this.lastRenderedMapObjects;
    }

    
    public ArrayList<Structure> getStructures() {
        return this.structures;
    }
    
    public ArrayList<Creature> getCreatures() {
        return this.creatures;
    }

    public void setTarget(MapObject mob) {
        this.targets.clear();
        if (mob!=null)this.targets.add(mob);
    }

    public void clearTarget() {
        this.targets.clear();
    }
    
    public void addTarget(MapObject mob) {
        this.targets.add(mob);
    }
    
    public List<MapObject> getTargets() {
        return this.targets;
    }

    public Zone getZone() {
        return this.currentZone;
    }

    private class CoordinateComparator implements Comparator<MapObject> {

        @Override
        public int compare(MapObject m1, MapObject m2) {
            return (int)(m1.getCenterYPos() - m2.getCenterYPos());
        }
        
    }

}