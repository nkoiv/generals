/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */
package generalsgame.util;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author nikok
 */
public interface PathfinderAlgorithm {
    
    public Path findPath(CollisionMap map, int tilesize, List<Integer> crossableTerrain, int startX, int startY, int goalX, int goalY);
    
    public List<Node> neighbours(CollisionMap map, List<Integer> crossableTerrain, int x, int y);
    public List<Node> neighbours(CollisionMap map, int clearanceNeed, List<Integer> crossableTerrain, int x, int y);
    public List<Node> diagonalNeighbours(CollisionMap map, List<Integer> crossableTerrain, int x, int y);
    public List<Node> diagonalNeighbours(CollisionMap map, int clearanceNeed, List<Integer> crossableTerrain, int x, int y);
    
    public HashMap<Integer, int[][]> getClearanceMaps(); //Used for console debugging, to examine the clearance map in use
}
