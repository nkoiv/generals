/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */
package generalsgame.ui;

import java.util.logging.Level;

import generalsgame.GameController;
import generalsgame.Generals;
import generalsgame.commands.Command;
import generalsgame.gamestate.BattleState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author nikok
 */
public class BattleButtons {
    public static int DISPLAY_PATHS = 1;
    
   
    
    public static class ToggleBattleMenuButton extends IconButton {
        private final GameController game;
        
        public ToggleBattleMenuButton(GameController game) {
            super("Menu", 0, 0, Generals.graphLibrary.getImage("locationmenuIcon"), Generals.graphLibrary.getImage("locationmenuIcon"), Generals.graphLibrary.getImage("buttonSquareBeige"), Generals.graphLibrary.getImage("buttonSquareBeigePressed"));
            this.game = game;
        }
        
        @Override
        public void handleMouseEvent(MouseEvent me) {
            if (me.getEventType() == MouseEvent.MOUSE_PRESSED) this.pressed = true;
            if (me.getEventType() == MouseEvent.MOUSE_RELEASED) {
                this.pressed = false;
                Generals.logger.log(Level.INFO, "{0} was clicked", this.getName());
                //TODO: Actually having buttons do something
                //game.battleControls.toggleSystemMenu();
            }
        }

    }

    public static class CommandMoveButton extends TextButton {
        private final GameController game;

        public CommandMoveButton(double width, double height, GameController game) {
            super("Move", width, height);
            this.game = game;
        }

        @Override
        public void buttonPress() {
            BattleState s = (BattleState)this.game.currentState;
            s.commandWithMouse(Command.MOVE);
            Generals.logger.log(Level.INFO, "{0} was clicked", this.getName());
        }


    }
    
}
