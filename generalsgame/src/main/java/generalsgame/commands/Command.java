/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */

package generalsgame.commands;

public interface Command {

    public static final int MOVE = 1;
    public static final int STOP = 2;

    int getID();
    String getName();

    int getStartTime();
    int getCompletionTime();
    boolean isComplete(int currentTime);

    boolean tick(double time);

}