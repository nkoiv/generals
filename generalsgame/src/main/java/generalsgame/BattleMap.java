package generalsgame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.Iterator;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import generalsgame.Direction;
import generalsgame.gameobjects.*;
import generalsgame.ui.Overlay;
import generalsgame.util.CollisionMap;
import generalsgame.util.PathFinder;

public class BattleMap {
    private String name;
    private Zone currentZone;
    private final double[] lastOffsets = new double[2];

    private ArrayList<MapObject> lastRenderedMapObjects = new ArrayList<>();
    private HashMap<Integer, HashSet> creatureSpatial; //New idea for lessening collision detection load
    private HashMap<Integer, HashSet> structureSpatial; //New idea for lessening collision detection load
    private final HashMap<Integer, MapObject> mobs = new HashMap<>();
    private int nextID = 1;
    private ArrayList<Creature> creatures;
    private ArrayList<Structure> structures;
    private List<MapObject> targets;

    private CollisionMap collisionMap;
    private PathFinder pathFinder;

    private MapObject screenFocus;
    
    public BattleMap() {

        this.creatures = new ArrayList<>();
        this.structures = new ArrayList<>();

        this.name = "TestMap";
        this.currentZone = new BGZone(new Image("/images/pocmap.png"));

        Creature testUnit = new Creature("TestUnit", new Image("/images/Unit/medievalUnit_10.png"));
        this.addCreature(testUnit, 200, 200);
        this.screenFocus = testUnit;

        Creature testUnit2 = new Creature("Also test", new Image("/images/Unit/medievalUnit_03.png"));
        this.addCreature(testUnit2, 400, 270);


        this.localizeMap();
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
     * Construct the little things needed to make
     * the map playable (collisionmaps, lights...)
     */
    private void localizeMap() {
        this.collisionMap = new CollisionMap(this, Generals.TILESIZE);
        Generals.logger.info("Collisionmap generated");
        this.collisionMap.setStructuresOnly(true);
        Generals.logger.info("CollisionMap set to structures only");
        this.collisionMap.updateCollisionLevels();
        Generals.logger.info("Collisionlevels updated");
        this.collisionMap.printMapToConsole();
        this.pathFinder = new PathFinder(this.collisionMap, 100, true);
        this.targets = new ArrayList<>();
        this.creatureSpatial = new HashMap<>();
        this.structureSpatial = new HashMap<>();
        //this.mobQuadTree = new QuadTree(0, new Rectangle(0,0,this.map.getWidth(),this.map.getHeight()));
        Generals.logger.log(Level.INFO, "Map ({0}x{1}) localized", new Object[]{currentZone.getWidth(), currentZone.getHeight()});
    }


    /**
     * Update is the main "tick" of the Location.
     * Movement, combat and triggers should all be handled here
     * 
     * @param time Time since the last update
     * @param elapsedSeconds Seconds passed since game started, used to sync movement completion
     * TODO: @param networking Peer network to relay the updates to
     */
    public void update (double time, int elapsedSeconds) {
        //Update all creatures with movement etc
        if (!this.creatures.isEmpty()) {
            for (Creature mob : this.creatures) { //Mobs do whatever mobs do
                mob.update(time, elapsedSeconds);
            }
        }

        this.fullCleanup(true, true, true);
    }

    public void fullCleanup(boolean cleanCreatures, boolean cleanStructures, boolean cleanEffects) {
        if (cleanCreatures) creatureCleanup();
        if (cleanStructures) structureCleanup();
        this.updateCreatureSpatial();
        this.collisionMap.updateCollisionLevels();
    }

    /**
     * structureCleanup cleans all the "removable"
     * flagged structures.
     */
    private Stack<Integer> structureCleanup() {
        //Structure cleanup
        Stack<Integer> removedStructureIDs = new Stack<>();
        if (!this.structures.isEmpty()) {
            ArrayList<Wall> removedWalls = new ArrayList();
            Iterator<Structure> structureIterator = structures.iterator(); //Cleanup of mobs
            while (structureIterator.hasNext()) {
                MapObject mob = structureIterator.next();
                if (mob.isRemovable()) {
                    if (mob instanceof Wall) {
                        removedWalls.add((Wall)mob);
                        //Update the surrounding walls as per needed
                        //this.updateWallsAt(mob.getCenterXPos(), mob.getCenterYPos());   
                    }
                    structureIterator.remove();
                    this.mobs.remove(mob.getID());
                    removedStructureIDs.add(mob.getID());
                    this.pathFinder.setMapOutOfDate(true);
                    if (this.targets.contains(mob)) this.targets.remove(mob);
                }
            }  
            this.restructureWalls(removedWalls);
        }
        
        return removedStructureIDs;
    }
    
    /**
    * creatureCleanup cleans all the "removable"
    * flagged creatures.
    */
    private Stack<Integer> creatureCleanup() {
        //Creature cleanup
        Stack<Integer> removedCreatureIDs = new Stack<>();
        if (!this.creatures.isEmpty()) {
            Iterator<Creature> creatureIterator = creatures.iterator(); //Cleanup of mobs
            while (creatureIterator.hasNext()) {
                MapObject mob = creatureIterator.next();
                if (mob.isRemovable()) {
                    creatureIterator.remove();
                    int mobID = mob.getID();
                    this.mobs.remove(mobID);
                    removedCreatureIDs.add(mobID);
                    //this.pathFinder.setMapOutOfDate(true); //Creatures are not on pathFindermap atm
                    if (this.targets.contains(mob)) this.targets.remove(mob);
                }
            }     
        }
        return removedCreatureIDs;
    }

    private void restructureWalls (ArrayList<Wall> removedWalls) {
        if (removedWalls.isEmpty()) return;
        for (Wall w : removedWalls) {
            updateWallsAt(w.getCenterXPos(), w.getCenterYPos());
        }
    }

    /**
     * If a wall gets added or removed, the walls around it
     * need to be updated accordingly
     * TODO: This is probably pointless stuff
     * @param xCenterPos xCenter of the happening
     * @param yCenterPos yCenter of the happening
     */
    private void updateWallsAt(double xCenterPos, double yCenterPos) {
        //ArrayList<MapObject> surroundingWalls = new ArrayList();
        //boolean[] boolwalls = new boolean[8];
        /*
         [0][1][2]
         [3]   [4]   
         [5][6][7]
        */
        
        //Note: It's okay to add Nulls here (most will be). Instanceof will take care of that
        //Cardinal directions
        MapObject mob;
        mob = (this.getMobAtLocation(xCenterPos-Generals.TILESIZE, yCenterPos)); //Left
        if (mob instanceof Wall) {Wall w = (Wall)mob; w.removeNeighbour(4); w.updateGraphicsBasedOnNeighbours();}
        mob = (this.getMobAtLocation(xCenterPos+Generals.TILESIZE, yCenterPos)); //Right
        if (mob instanceof Wall) {Wall w = (Wall)mob; w.removeNeighbour(3); w.updateGraphicsBasedOnNeighbours();}
        mob = (this.getMobAtLocation(xCenterPos, yCenterPos-Generals.TILESIZE)); //Up
        if (mob instanceof Wall) {Wall w = (Wall)mob; w.removeNeighbour(6); w.updateGraphicsBasedOnNeighbours();}
        mob = (this.getMobAtLocation(xCenterPos, yCenterPos+Generals.TILESIZE)); //Down
        if (mob instanceof Wall) {Wall w = (Wall)mob; w.removeNeighbour(1); w.updateGraphicsBasedOnNeighbours();}
        //Diagonal directions
        mob = (this.getMobAtLocation(xCenterPos-Generals.TILESIZE, yCenterPos-Generals.TILESIZE)); //UpLeft
        if (mob instanceof Wall) {Wall w = (Wall)mob; w.removeNeighbour(7); w.updateGraphicsBasedOnNeighbours();}
        mob = (this.getMobAtLocation(xCenterPos+Generals.TILESIZE, yCenterPos-Generals.TILESIZE)); //UpRight
        if (mob instanceof Wall) {Wall w = (Wall)mob; w.removeNeighbour(5); w.updateGraphicsBasedOnNeighbours();}
        mob = (this.getMobAtLocation(xCenterPos-Generals.TILESIZE, yCenterPos+Generals.TILESIZE)); //DownLeft
        if (mob instanceof Wall) {Wall w = (Wall)mob; w.removeNeighbour(2); w.updateGraphicsBasedOnNeighbours();}
        mob = (this.getMobAtLocation(xCenterPos+Generals.TILESIZE, yCenterPos+Generals.TILESIZE)); //DownRight
        if (mob instanceof Wall) {Wall w = (Wall)mob; w.removeNeighbour(0); w.updateGraphicsBasedOnNeighbours();}

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
        //Generals.logger.info("Offset: "+xOffset+","+yOffset);
        this.renderMap(gc, xOffset, yOffset);
        this.lastRenderedMapObjects = this.renderMobs(gc, xOffset, yOffset);
        this.renderExtras(gc, xOffset, yOffset);
        //this.renderLights(sc, lastRenderedMapObjects, xOffset, yOffset);
    }

    private void renderExtras(GraphicsContext gc, double xOffset, double yOffset) {
        if (!this.targets.isEmpty()) {
            for (MapObject mob : this.targets) {
                Overlay.drawTargettingCircle(gc, mob);
            }
        }
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
        //Generals.logger.info("Trying to find a mob at location " + xCoor + " x " + yCoor);
        MapObject mobAtLocation = null;
        int spatialID = this.getSpatial(xCoor, yCoor);
        if (!this.creatureSpatial.isEmpty()) {
            HashSet<Creature> spatial = this.creatureSpatial.get(spatialID);
            if (spatial != null) {
                for (Creature mob : spatial) {
                    if (xCoor >= mob.getXPos() && xCoor <= mob.getXPos()+mob.getWidth() &&
                            yCoor >= mob.getYPos() && yCoor <= mob.getYPos()+mob.getHeight()) {
                            //Do a pixelcheck on the mob;
                            //if (Sprite.pixelCollision(xCoor, yCoor, Generals.pixel, mob.getXPos(), mob.getYPos(), mob.getSprite().getImage())) {
                            return mob;
                            //}   
                        }
                }
            } else {
                Generals.logger.info("Spatial was null!");
            }
        } {
            Generals.logger.info("Creature spatial was empty");
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
    
    /** CheckCollisions for a given MapObjects
    * Returns a List with all the objects that collide with MapObject o
    * Now with quad tree to check only objects nearby
    * @param o The MapObject to check collisions with
    * @return a List with all the objects that collide with MapObject o
    */
    public ArrayList<MapObject> checkCollisions (MapObject o) {
        
        ArrayList<MapObject> collidingObjects = new ArrayList<>();
        HashSet<Integer> mobSpatials = getSpatials(o);
        //Spatials cover the creature collisions
        for (Integer i : mobSpatials) {
            addMapObjectCollisions(o, this.creatureSpatial.get(i), collidingObjects);
        }
        
        //Check collisions for structures too
        //For creatures, check the collision versus collisionmap first
        //Note that this returns false on structures the creature is allowed to pass through
        if (o instanceof Creature) {
            if (collidesOnCollisionMap((Creature)o)) {
                addMapObjectCollisions(o, this.structures, collidingObjects);
            }
            Iterator<MapObject> mobIter = collidingObjects.iterator();
            while (mobIter.hasNext()) {
                MapObject mob = mobIter.next();
                if (((Creature)o).getCrossableTerrain().contains(mob.getCollisionLevel())) {
                    mobIter.remove();
                }
            }
        } else {
            addMapObjectCollisions(o, this.structures, collidingObjects);
        }
        
        
        return collidingObjects;
        
    }

    /**
     * Check if a MapObject hits something on the collision map.
     * Useful for pruning down the list of objects that needs true
     * collision detection.
     * TODO: using this, a HUGE creature could get stuck on certain structures?
     * @param mob
     * @return True if collision map had something at mobs coordinates
     */
    private boolean collidesOnCollisionMap(Creature mob) {
        Double[] upleft = mob.getCorner(Direction.UPLEFT);
        Double[] upright = mob.getCorner(Direction.UPRIGHT);
        Double[] downleft = mob.getCorner(Direction.DOWNLEFT);
        Double[] downright = mob.getCorner(Direction.DOWNRIGHT);
        if (this.collisionMap.isBlocked(mob.getCrossableTerrain(),(int)(upleft[0]/collisionMap.nodeSize), (int)(upleft[1]/collisionMap.nodeSize))) return true;
        if (this.collisionMap.isBlocked(mob.getCrossableTerrain(),(int)(upright[0]/collisionMap.nodeSize), (int)(upright[1]/collisionMap.nodeSize))) return true;
        if (this.collisionMap.isBlocked(mob.getCrossableTerrain(),(int)(downleft[0]/collisionMap.nodeSize), (int)(downleft[1]/collisionMap.nodeSize))) return true;
        return (this.collisionMap.isBlocked(mob.getCrossableTerrain(),(int)(downright[0]/collisionMap.nodeSize), (int)(downright[1]/collisionMap.nodeSize)));
    }

    /**
     * Check the supplied iterable list of objects for collisions
     * The method is supplied an arraylist instead of returning one,
     * because same arraylist may go through several cycles of addMapObjectCollisions
     * @param o MapObject to check collisions for
     * @param mapObjectsToCheck List of objects
     * @param collidingObjects List to add the colliding objects on
     */
    private void addMapObjectCollisions(MapObject mob, Iterable mapObjectsToCheck, ArrayList collidingObjects) {         
        if (mapObjectsToCheck == null) return;
        Iterator<MapObject> mobIter = mapObjectsToCheck.iterator();
        while ( mobIter.hasNext() )
        {
            MapObject collidingObject = mobIter.next();
            if (collidingObject.equals(mob)) continue;
            //If the objects are further away than their combined width/height, they cant collide
            if ((Math.abs(collidingObject.getCenterXPos() - mob.getCenterXPos())
                 > (collidingObject.getWidth() + mob.getWidth()))
                || (Math.abs(collidingObject.getCenterYPos() - mob.getCenterYPos())
                 > (collidingObject.getHeight() + mob.getHeight()))) {
                //Objects are far enough from oneanother
            } else {
                if (!collidingObject.equals(mob) && mob.intersects(collidingObject)) { 
                    // Colliding with yourself is not really a collision
                    //Mists.logger.info(mob.getName()+" collided with "+collidingObject.getName());
                    //if (collidingObject instanceof Structure) Mists.logger.info("Collision between "+mob.getName()+" and "+collidingObject.getName()+" ID:"+collidingObject.getID());
                    collidingObjects.add(collidingObject);
                }
            }
            
        }
    }

    /**
     * Check Collisions for a line drawn between two points.
     * This is useful for determining for example line of sight
     * @param xStart x Coordinate of the starting point
     * @param yStart y Coordinate of the starting point
     * @param xGoal x Coordinate of the far end
     * @param yGoal y Coordinate of the far end
     * @return list with all the colliding mapObjects(Creatures and Structures)
     */
    public ArrayList<MapObject> checkCollisions(double xStart, double yStart, double xGoal, double yGoal) {
        ArrayList<MapObject> collidingObjects = new ArrayList<>();
        double xDistance = xGoal - xStart;
        double yDistance = yGoal - yStart;
        Line line = new Line(xStart, yStart, xGoal, yGoal);
        Iterator<Creature> creaturesIter = creatures.iterator();
        while ( creaturesIter.hasNext() )
        {
            MapObject collidingObject = creaturesIter.next();
            //If the objects are further away than their combined width/height, they cant collide
            if ((Math.abs(collidingObject.getCenterXPos() - xStart)
                 > (collidingObject.getWidth() + Math.abs(xDistance)))
                || (Math.abs(collidingObject.getCenterYPos() - yStart)
                 > (collidingObject.getHeight() + Math.abs(yDistance)))) {
                //Objects are far enough from oneanother
            } else {
                if (collidingObject.intersects(line) ) 
                 {
                    collidingObjects.add(collidingObject);
                }
            }
            
        }
        Iterator<Structure> structuresIter = structures.iterator();
        while ( structuresIter.hasNext() )
        {
            MapObject collidingObject = structuresIter.next();
            //If the objects are further away than their combined width/height, they cant collide
            if ((Math.abs(collidingObject.getCenterXPos() - xStart)
                 > (collidingObject.getWidth() + Math.abs(xDistance)))
                || (Math.abs(collidingObject.getCenterYPos() - yStart)
                 > (collidingObject.getHeight() + Math.abs(yDistance)))) {
                //Objects are far enough from oneanother
            } else {
                if (collidingObject.intersects(line)) 
                 {
                    collidingObjects.add(collidingObject);
                }
            }
            
        }
        
        
        return collidingObjects;
    }
    
    public EnumSet<Direction> collidedSides (MapObject mob) {
        ArrayList<MapObject> collidingObjects = this.checkCollisions(mob); //Get the colliding object(s)
        return collidedSides(mob, collidingObjects, this.currentZone.getWidth(), this.currentZone.getHeight());
    }
    
    public static EnumSet<Direction> collidedSides (MapObject mob, ArrayList<MapObject> collidingObjects, double mapWidth, double mapHeight) {
        EnumSet<Direction> collidedDirections = EnumSet.of(Direction.STAY);
        for (MapObject collidingObject : collidingObjects) {
            //Mists.logger.log(Level.INFO, "{0} bumped into {1}", new Object[]{this, collidingObject});
            double collidingX = collidingObject.getCenterXPos();//+(collidingObject.getSprite().getWidth()/2);
            double collidingY = collidingObject.getCenterYPos();//+(collidingObject.getSprite().getHeight()/2);
            double thisX = mob.getCenterXPos();//+(this.getSprite().getWidth()/2);
            double thisY = mob.getCenterYPos();//+(this.getSprite().getHeight()/2);
            double xDistance = (thisX - collidingX);
            double yDistance = (thisY - collidingY);
            if (Math.abs(xDistance) >= Math.abs(yDistance)) {
                //Collided primary on the X (Left<->Right)
                if (mob.getCenterXPos() <= collidingObject.getCenterXPos()) {
                    //CollidingObject is RIGHT of the mob
                    collidedDirections.add(Direction.RIGHT);
                } else {
                    //CollidingObject is LEFT of the mob
                    collidedDirections.add(Direction.LEFT);
                }
            } else {
                //Collided primary on the Y (Up or Down)
                if (mob.getCenterYPos() >= collidingObject.getCenterYPos()) {
                    //CollidingObject is UP of the mob
                    collidedDirections.add(Direction.UP);
                } else {
                    //CollidingObject is DOWN of the mob
                    collidedDirections.add(Direction.DOWN);
                }
            }
        }
        if (mob.getXPos() <= 0) collidedDirections.add(Direction.LEFT);
        if (mob.getYPos() <= 0) collidedDirections.add(Direction.UP);
        if (mob.getCenterXPos() >= mapWidth) collidedDirections.add(Direction.RIGHT);
        if (mob.getCenterYPos() >= mapHeight) collidedDirections.add(Direction.DOWN);
        return collidedDirections;
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

    /**
     * The target of the screen focus is what the camera follows.
     * The view of the location is centered on this target (normally the player)
     * @param focus MapObject to focus on
     */
    public void setScreenFocus(MapObject focus) {
        this.screenFocus = focus;
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

       /**
    * Returns the PathFinder for this Location
    * @return The PathFinder for this location
    */
    public PathFinder getPathFinder() {
        if (this.pathFinder == null) this.pathFinder = new PathFinder(this.collisionMap, 50, true);
        return this.pathFinder;
    }
    
    /**
     * Give the MapObject an unique location-specific ID-number
     * @param mob MapObject to set the ID to
     */
    private void giveID(MapObject mob) {
        if (mob == null) return;
        if (nextID == Integer.MAX_VALUE) nextID = Integer.MIN_VALUE;
        mob.setID(this.nextID);
        nextID++;
        if (nextID == 0) {
            Generals.logger.warning("Out of MapObject IDs, cleaning up");
            this.cleanupIDs();
        } 
    }
    
    /**
     * If ID's run out, clean up the ID list.
     */
    private void cleanupIDs() {
        this.nextID = 1;
        if (this.mobs.isEmpty()) return;
        for (Integer mobID : this.mobs.keySet()) {
            this.giveID(this.mobs.get(mobID));
        }
        //TODO: Inform possible clients that ID's have changed.
    }
    
    public int peekNextID() {
        return this.nextID;
    }
    
    public void setNextID(int id) {
        this.nextID = id;
    }

    public void clearAllMapObjects() {
        for (int mobID : this.mobs.keySet()) {
            this.removeMapObject(mobID);
        }
    }
    
    public void removeMapObject(int mobID) {
        MapObject mob = this.mobs.get(mobID);
        if (mob!=null) this.removeMapObject(mob);            
    }
    
    private void removeMapObject(MapObject mob) {
        Generals.logger.info("removeMapObject "+mob.getName());
        if (mob instanceof Structure) {
            this.structures.remove((Structure)mob);
        }
        if (mob instanceof Creature) {
            this.creatures.remove((Creature)mob);
        }
        this.mobs.remove(mob.getID());
    }
    
    /**
     * Insert a mob into the Location with given mobID.
     * If this mobID is already taken by another object,
     * the previous object is removed to make room for the new one.
     * @param mob MapObject to add to the Location
     * @param mobID LocationID to give to the new MapObject
     */
    public void addMapObject(MapObject mob, int mobID) {
    	if (this.getMapObject(mobID) != null) this.removeMapObject(mobID);
        if (mob == null) return;
        mob.setID(mobID);
        if (mob instanceof Structure) {
            this.structures.add((Structure)mob);
        }
        if (mob instanceof Creature) {
            this.creatures.add((Creature)mob);
        }
        this.mobs.put(mob.getID(), mob);
        mob.setBattleMap(this);
    }
        
    /**
     * Generic MapObject insertion.
     * @param mob MapObject to insert in the map
     */
    public void addMapObject(MapObject mob) {
        if (mob == null) {
            Generals.logger.warning("Tried to add NULL mob to "+this.getName());
        }
        this.giveID(mob);
        if (mob instanceof Structure) {
            this.structures.add((Structure)mob);
        }
        if (mob instanceof Creature) {
            this.creatures.add((Creature)mob);
        }
        mob.setBattleMap(this);
    }
    
    /**
     * Pull the creature by ID from the general MOBs table
     * @param ID Location-specific Identifier for the mob
     * @return MapObject if it exists in the location
     */
    public MapObject getMapObject(int ID) {
        return this.mobs.get(ID);
    }
    

    public void addMapObject(MapObject mob, double xPos, double yPos) {
        if (mob == null) return;
        addMapObject(mob);
        mob.setPosition(xPos, yPos);
        if (this.pathFinder != null && mob instanceof Structure) this.pathFinder.setMapOutOfDate(true);
    }
    
    /**
    * Adds a Structure to the location
    * @param s The structure to be added
    * @param xPos Position for the structure on the X-axis
    * @param yPos Position for the structure on the Y-axis
    */
    private void addStructure(Structure s, double xPos, double yPos) {
        if (!this.structures.contains(s)) {
            this.addMapObject(s);
        }
        s.setBattleMap(this);
        s.setPosition(xPos, yPos);
        if (this.pathFinder != null) this.pathFinder.setMapOutOfDate(true);
    }
    
    /** Adds a Creature to the location
    * @param c The creature to be added
    * @param xPos Position for the creature on the X-axis
    * @param yPos Position for the creature on the Y-axis
    */
    private void addCreature(Creature c, double xPos, double yPos) {
        if (!this.creatures.contains(c)) {
            this.addMapObject(c);
        } else {
            //No need to re-add the creature if it's already in. Just give it a new ID.
            this.removeMapObject(c);
            this.giveID(c);
            Generals.logger.log(Level.WARNING, "Tried to add a {3} to {0} but {3} was already in it. Gave the {3} new ID: {1}", new Object[]{this.getName(), c.getID(), c.getName()});
            this.addMapObject(c);
        }
        c.setBattleMap(this);
        c.setPosition(xPos, yPos);
    }

    private class CoordinateComparator implements Comparator<MapObject> {

        @Override
        public int compare(MapObject m1, MapObject m2) {
            return (int)(m1.getCenterYPos() - m2.getCenterYPos());
        }
        
    }

}