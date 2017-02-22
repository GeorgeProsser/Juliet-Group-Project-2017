/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext;
import java.util.Queue;

/**
 *
 * @author Sunny <ss2324@cam.ac.uk>
 */
public class SlaveSimulator extends SimpleApplication {

    private BulletAppState bulletAppState;
    private SimulatorAppState simulatorAppState;

    public SlaveSimulator(SimulatorAppState state) {
        simulatorAppState = state;
    }
    
    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();

        stateManager.attach(bulletAppState);
        stateManager.attach(simulatorAppState);
    }
    
    public void kill()
    {
        simulatorAppState.setToKill(true);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!this.stateManager.hasState(simulatorAppState))
        {
            this.stop();
        }
    }
}
