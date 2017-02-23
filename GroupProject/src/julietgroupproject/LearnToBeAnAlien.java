/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.controls.ActionListener;
import java.util.logging.Logger;

/**
 * The main application running at front end.
 * It handles input from user and uses
 * appropriate AppStates for alien editing/simulation.
 * 
 * @author GeorgeLenovo
 */
public class LearnToBeAnAlien extends SimpleApplication {
    public static void main(String[] args) {
        
        //Disable joint warnings
        Logger physicslogger = Logger.getLogger(PhysicsSpace.class.getName());
        physicslogger.setUseParentHandlers(false);
        
        LearnToBeAnAlien app = new LearnToBeAnAlien();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //disable frame rate info etc
        setDisplayStatView(false);
        BulletAppState physics = new BulletAppState();
        this.stateManager.attach(physics);
        UIAppState uiAppState = new UIAppState(null, 1.0);
        this.stateManager.attach(uiAppState);
    }
}
