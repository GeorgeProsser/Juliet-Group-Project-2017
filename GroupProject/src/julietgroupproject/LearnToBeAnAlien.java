/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.system.AppSettings;
import java.util.logging.Logger;

/**
 * The main application running at front end.
 * It handles input from user and uses
 * appropriate AppStates for alien editing/simulation.
 * 
 * @author GeorgeLenovo
 */
public class LearnToBeAnAlien extends SimpleApplication {
    
    public static final double SIM_SPEED = 1.0f;
    public static final double SIM_ACCURACY = 1f/60f;
    public static final double FIXED_TIME_STEP = 1.0/60.0;
    
    public static void main(String[] args) {
        
        //Disable joint warnings

        Logger physicslogger = Logger.getLogger(PhysicsSpace.class.getName());
        physicslogger.setUseParentHandlers(false);
        // keep the default renderer
        LearnToBeAnAlien app = new LearnToBeAnAlien();
        app.setShowSettings((System.getProperty("os.name").toLowerCase()).contains("mac"));
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Learn To Be An Alien");
        settings.put("Width", 1920*4/5);
        settings.put("Height",1080*4/5);
        settings.setFullscreen(false);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //disable frame rate info etc
        
        setDisplayStatView(false);
        BulletAppState physics = new BulletAppState();
        this.stateManager.attach(physics);
        UIAppState uiAppState = new UIAppState(new SavedAlien(), SIM_SPEED, SIM_ACCURACY, FIXED_TIME_STEP);
        this.stateManager.attach(uiAppState);
    }
}
