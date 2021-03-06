/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki / #014416884
 */
package generalsgame.ui;

import generalsgame.GameController;
import generalsgame.Generals;
import generalsgame.gamestate.GameState;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * TextWindow is a simple UI component with just text in it.
 * @author nikok
 */
public class TextWindow extends UIComponent {

    protected GameState parent;
    protected Color bgColor;
    protected double bgOpacity;
    protected double margin;
    
    protected CloseButton closeButton;
    
    protected Font font;
    protected String text;
    
    public TextWindow(GameState parent, String name, double width, double height, double xPos, double yPos){
        this.parent = parent;
        this.name = name;
        this.width = width;
        this.height = height;
        this.xPosition = xPos;
        this.yPosition = yPos;
        this.margin = 10;
        this.bgOpacity = 0.8;
        this.bgColor = Color.BLACK;
    }
    
    @Override
    public void render(GraphicsContext gc, double xPosition, double yPosition) {
        //Draw the background window
        gc.save();
        gc.setGlobalAlpha(this.bgOpacity);
        gc.setFill(bgColor);
        gc.fillRect(this.xPosition, this.yPosition,this.width, this.height);
        //Draw the text
        gc.restore();
        this.renderText(gc, xPosition, yPosition);
        if (this.closeButton != null) this.closeButton.render(gc, xPosition+closeButton.xPosition, yPosition+closeButton.yPosition);
    }

    protected void renderText(GraphicsContext gc, double xPosition, double yPosition) {
        gc.save();
        //gc.setFont(Font.font("Verdana"));
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(12));
        gc.fillText(this.text, xPosition+this.margin, yPosition+this.margin+15);
        gc.restore();
    }
    
    public void close() {
        this.parent.removeUIComponent(this.name);
    }
    
    public void addCloseButton() {
        CloseButton cb = new CloseButton(this, this.width-20, 5);
        this.closeButton = cb;
    }
    
    public void removeCloseButton() {
        this.closeButton = null;
    }
    
    public void setText(String string) {
        this.text = string;
    }
    
    public String getText() {
        return this.text;
    }

    @Override
    public void handleMouseEvent(MouseEvent me) {
        if ((me.getEventType() == MouseEvent.MOUSE_CLICKED) && me.getButton() == MouseButton.PRIMARY) {
            //Generals.logger.info("Click event at "+this.getName()+", "+me.getX()+"x"+me.getY());
            //Generals.logger.info("X: "+(xPosition+closeButton.xPosition)+" Y:"+(yPosition+closeButton.yPosition));
            if (closeButton == null) return;
            if (me.getX() > xPosition+closeButton.xPosition && me.getX() < xPosition+closeButton.xPosition+closeButton.width
                    && me.getY() > yPosition+closeButton.yPosition && me.getY() < yPosition+closeButton.yPosition+closeButton.height) {
                closeButton.buttonPress();
                me.consume();
            }
            
        }
    }
    
    protected GameState getParent() {
        return this.parent;
    }
    
    protected GameController getGC() {
        return this.parent.getGC();
    }
    
    protected class CloseButton extends IconButton {
        private TextWindow tw;
        
        public CloseButton(TextWindow tw, double xPosition, double yPosition) {
            super("CloseButton", 15, 15, xPosition, yPosition, Generals.graphLibrary.getImage("iconCrossBlue"), Generals.graphLibrary.getImage("iconCrossBlue"));
            this.tw = tw;
            
        }
        
        @Override
        protected void buttonPress() {
            this.tw.close();
            Generals.game.currentState.closePopUpWindows();
        }
        
    }

}
