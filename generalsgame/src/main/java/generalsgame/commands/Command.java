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