/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */


package generalsgame.util;

import java.util.ArrayList;
import java.util.List;

import generalsgame.Generals;
import generalsgame.gameobjects.MapObject;
import generalsgame.gameobjects.Structure;
import generalsgame.BattleMap;

/**
 * CollisionMap is a location turned simple
 * This is used for the PathFinding to navigate without having to constantly
 * call in Locations Colliding-functions
 * @author daedra
 */
public class CollisionMap {
    /* Location that this CollisionMap is based on */
    private BattleMap map;
    /* Nodes that make up the map */
    private Node[][] nodeMap; 
    /* Visited is used for pathfinding to determine which nodes have already been visited */
    //private Boolean[][] visited;
    public int mapTileWidth;
    public int mapTileHeight;
    public int nodeSize;
    private boolean structuresOnly;
            
    public CollisionMap(BattleMap l, int nodeSize) {
        this.map = l;
        this.nodeSize = nodeSize; //size of nodes in map pixels - usually same as tilesize
        Generals.logger.info("Generating collisionmap for "+l.getName());
        double startTime = System.currentTimeMillis();
        //First we'll convert map to tiles, even if it's BGMap
        Generals.logger.info("Map width: "+l.getZone().getWidth()+" Map height: "+l.getZone().getHeight());
        this.mapTileWidth = (int)(l.getZone().getWidth() / nodeSize)+1;
        this.mapTileHeight = (int)(l.getZone().getHeight() / nodeSize)+1;
        Generals.logger.info("NodeMap: "+mapTileWidth+"x"+mapTileHeight);
        nodeMap = new Node[mapTileWidth][mapTileHeight];
        //visited = new Boolean[mapTileWidth][mapTileHeight];
        //Then populate a nodemap with empty (=collisionLevel 0) nodes
        Generals.logger.info("Nodemap initialized. Going through the nodes... ("+this.mapTileHeight+"x"+this.mapTileWidth+")");
        for (int row = 0; row < this.mapTileHeight;row++) {
            for (int column = 0; column < this.mapTileWidth; column++) {
                this.nodeMap[column][row] = new Node(column, row, nodeSize, 0);
            }
        }        
        Generals.logger.info("Collisionmap generated in "+(System.currentTimeMillis()-startTime)+"ms");
    }
    
    /**
     * UpdateCollisionLevels clears the old collisionmap and
     * updated the collision levels on per node basis, based
     * on the mobs at the location.
     * 
     * TODO: Add in the movement cost from cost inducing mobs (swampland, whatever).
     * It goes into Node.movementCost and Pathfinder is ready for it.
     */
    
    public void updateCollisionLevels() {
        //Generals.logger.info("Updating collisionmap for "+this.location.getName());
        //double startTime = System.currentTimeMillis();
        //Clear the map
        clearNodeMap();
        //Go through all the nodes and check the location if it has something at them
        updateMobsOnNodeMap();
        //Generals.logger.log(Level.INFO, "Collision update done in {0}ms", (System.currentTimeMillis()-startTime));
    }
    
    

    
        /**
     *  Generate a clear nodemap with 0-node at each spot
     */
    private void clearNodeMap() {
        this.nodeMap = new Node[mapTileWidth][mapTileHeight];
        //Then populate a nodemap with empty (=passable) nodes
        for (int row = 0; row < this.mapTileHeight;row++) {
            for (int column = 0; column < this.mapTileWidth; column++) {
                this.nodeMap[column][row] = new Node(column, row, nodeSize, 0);
            }
        }
    }
    
    /**
     * Scan this.location for MOBs and
     * populate the nodemap with them.
     * Note: This should be called AFTER clearNodeMap
     * when refreshing the nodemap, otherwise old
     * MOBs linger on the collisionmap.
     * 
     */
    private void updateMobsOnNodeMap() {
        ArrayList<Structure> mobs = this.map.getStructures();
        //Generals.logger.info("Moblist has " +mobs.size()+" objects");
        for (MapObject mob : mobs) {
            //Generals.logger.info("Doing collisionmapstuff for "+mob.getName());
            //Mob blocks nodes from its top left corner...
            int mobXNodeStart = ((int)mob.getXPos() / nodeSize);
            int mobYNodeStart = ((int)mob.getYPos() / nodeSize);
            //... to its bottom right corner
            int mobXNodeEnd = ((int)(mob.getXPos()+mob.getWidth()-1)/ nodeSize); 
            int mobYNodeEnd = ((int)(mob.getYPos()+mob.getHeight()-1)/ nodeSize); 
            int mobCL = mob.getCollisionLevel();          
            //Structures mark all blocked nodes with collisionLevel
            if (mob instanceof Structure) {
                //Generals.logger.info("This is a structure at "+mobYNodeStart+","+mobXNodeStart);
                if (mob.getCollisionLevel() == 0) continue; //CL 0 means anything can pass through
                for (int row = mobYNodeStart; row <= mobYNodeEnd;row++ ) {
                    for (int column = mobXNodeStart; column <= mobXNodeEnd;column++) {
                        //Generals.logger.info("Should be setting some CL at "+column+","+row);
                        if(this.isOnMap(column, row))
                            this.nodeMap[column][row].setCollisionLevel(mobCL);
                            //Generals.logger.info("Added CL "+mobCL+" at "+column+","+row);
                    }
                }
            } else if (!this.structuresOnly) { //Creatures block only their original spot
                //Generals.logger.info("This is NOT structure");
                //TODO: This is redundant old code that can never be reached
                //If creatures are wanted on collisionmap, they need to be
                //called in via location.getCreatures();
                if(this.isOnMap(mobXNodeStart, mobYNodeStart))
                this.nodeMap[mobXNodeStart][mobYNodeStart].setCollisionLevel(mobCL);
            }
            
            
            //Generals.logger.info("["+mobXNode+","+mobYNode+"] has a "+mob.getName()+ ": set to CL "+mobCL);
        }
        //Generals.logger.info("Collisionmap updated in "+(System.currentTimeMillis()-startTime)+"ms");
    }
    
    
    /** isBlocked checks if the given unit can pass through the given node 
     * returns False if tile is blocked, true if not. Every creature should be able to cross CL 0
     * @param crossableTerrain List of terrains the mover can cross
     * @param x xCoordinate of the checked node
     * @param y yCoordinate of the checked node
     * @return True if the unit cannot pass to this terrain with given crossableTerrainList
    */
    public boolean isBlocked(List<Integer> crossableTerrain ,int x, int y) {
        if (x>this.mapTileWidth-1 || y>this.mapTileHeight-1) return true;
        if (x<0 || y<0) return true;
        if (nodeMap[x][y] == null) return true;
        return (!crossableTerrain.contains(nodeMap[x][y].getCollisionLevel()));
    }
    
    public boolean isBlocked(int crossableTerrain, int x, int y) {
        if (x>this.mapTileWidth-1 || y>this.mapTileHeight-1 || x < 0 || y < 0) return true;
        if (nodeMap[x][y] == null) return true;
	return (crossableTerrain != nodeMap[x][y].getCollisionLevel());
    }

    //The Visited -thing is not currently in use by pathfinding.
    //This because it's essentially done by pathfinder-classes themselves,
    //and does not belong in the collisionmap.
    /*
    public void pfVisit(int xCoor, int yCoor) {
        this.visited[xCoor][yCoor] = true;
    }
    
    public boolean isVisited (int xCoor, int yCoor) {
        return this.visited[xCoor][yCoor];
    }
    */
    public void setStructuresOnly (boolean onlyStructures) {
        this.structuresOnly = onlyStructures;
    }
    
    public boolean isStructuresOnly() {
        return this.structuresOnly;
    }
    
    private boolean isOnMap (int xCoor, int yCoor) {
        return ((xCoor >= 0) && (xCoor < this.mapTileWidth) && (yCoor >= 0) && (yCoor < this.mapTileHeight));
    }
    
    public int getMapTileWidth() {
        return this.mapTileWidth;
    }
    
    public int getMapTileHeight() {
        return this.mapTileHeight;
    }
    
    public BattleMap getBattleMap() {
        return this.map;
    }
    
    public int getNodeSize() {
        return this.nodeSize;
    }
    
    public Node getNode( int x, int y) {
        return this.nodeMap[x][y];
    }
    
    //TODO: Make it so that different terrain can slow or speed up movers
    public float getMovementCost(List<Integer> movementModifiers, int startX, int startY, int targetX, int targetY) {
		return 1; // For now all cost 1;
    }
    
    public void printMapToConsole() {
        for (int row = 0; row < this.mapTileHeight; row++) {
            for (int column = 0; column < this.mapTileWidth; column++) {
                if (this.nodeMap[column][row].getCollisionLevel() >0)
                System.out.print("X");
                else System.out.print(".");
            }
            System.out.println();
        }
    }
    
}
