/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */

package generalsgame.util;

import java.util.List;

/**
 * MoveCostCalculator calculates the distance between point A and point B
 * This is done on a gridMap, with the calculation function being selectable between
 * Manhattan, Diagonal and Euclidean.
 * @author nikok
 */
public class MoveCostCalculator {
    
    private int type;
    public static int MANHATTAN_DISTANCE = 0;
    public static int DIAGONAL_DISTANCE = 1;
    public static int EUCLIDEAN_DISTANCE = 2;
    
    
    public MoveCostCalculator () {
        this.type = MANHATTAN_DISTANCE;
    }
    
    public MoveCostCalculator (int type) {
        this.type = type;
    }
    
    public double getCost(CollisionMap map, List<Integer> movementAbility, int currentX, int currentY, int goalX, int goalY) {
        //TODO: start using the collisionMap and movementAbility to calculate varying costs per tile (swamp, tar...)
        switch (this.type) {
            case 0: return this.manhattanDistance(currentX, currentY, goalX, goalY);
            case 1: return this.diagonalDistance(currentX, currentY, goalX, goalY);
            case 2: return this.euclideanDistance(currentX, currentY, goalX, goalY);
            default: return this.manhattanDistance(currentX, currentY, goalX, goalY);    
        }
    }

    private int manhattanDistance(int currentX, int currentY, int goalX, int goalY) {
        //Linear distance of nodes
        int manhattanDistance = Math.abs(currentX - goalX) + 
                        Math.abs(currentY - goalY);
        return manhattanDistance;
    }
    
    private int diagonalDistance(int currentX, int currentY, int goalX, int goalY) {
        //Diagonal distanceassumes going diagonally costs the same as going cardinal
        int diagonalDistance = Math.max(Math.abs(currentX - goalX),
                        Math.abs(currentY - goalY));
        return diagonalDistance;
    }
    
    private double euclideanDistance(int currentX, int currentY, int goalX, int goalY) {
        /*With euclidean the diagonal movement is considered to be
        * slightly more expensive than cardinal movement
        * ((AC = sqrt(AB^2 + BC^2))), 
        * where AB = x2 - x1 and BC = y2 - y1 and AC will be [x3, y3]
        */
        double euclideanDistance = Math.sqrt(Math.pow(currentX - goalX, 2)
                            + Math.pow(currentY - goalY, 2));
        return euclideanDistance;
    }
    
}
