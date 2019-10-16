/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */

package generalsgame.commands;

import generalsgame.gameobjects.Creature;


public class MoveCommand implements Command {

    private int id;
    private String name;

    private Creature target;
    private double xCoor;
    private double yCoor;

    private int startTime;
    private int endTime;

    public MoveCommand(Creature target, double xCoor, double yCoor, int startTime, int endTime) {
        this.id = Command.MOVE;
        this.name = "Move";

        this.target = target;
        this.xCoor = xCoor;
        this.yCoor = yCoor;
        this.startTime = startTime;
        this.endTime = endTime;
    }


    @Override
    public boolean tick(double time) {
        this.target.moveTowards(xCoor, yCoor);
        this.target.applyMovement(time);
        return true;
    }

    @Override
    public int getID() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getStartTime() {
        return this.startTime;
    }

    @Override
    public int getCompletionTime() {
        return this.endTime;
    }

    @Override
    public boolean isComplete(int currentTime) {
        return this.endTime >= currentTime;
    }


    
}