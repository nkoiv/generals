/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 * (c) nkoiv / Niko Koivumäki
 */
package generalsgame.gameobjects;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.logging.Level;

import generalsgame.Direction;
import generalsgame.graphics.MovingGraphics;
import generalsgame.graphics.Sprite;
import generalsgame.graphics.SpriteAnimation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Creature is a "living" MapObject
 * As such, they get (at least some) AI-routines
 * @author nkoiv
 */
public class Creature extends MapObject { 
        private int currentHealth;
        private int maxHealth;


        public int getHealth() {
            return this.currentHealth;
        }

        public int getMaxHealth() {
            return this.maxHealth;
        }
    
    }
