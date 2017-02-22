/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.scene.Node;
import java.util.Queue;
import static julietgroupproject.SimulatorAppState.DEFAULT_UPDATE_CYCLE;

/**
 *
 * @author GeorgeLenovo
 */
public class TrainingAppState extends SimulatorAppState {

    // Simulation related fields
    protected Queue<SimulationData> queue;
    protected SimulationData currentSim;
    protected float simTimeLimit;

    public TrainingAppState(Alien _alien, Queue<SimulationData> q, double _simSpeed) {
        /*
         * Constructor, taking an Alien object as the Alien
         * to be tested in this simulator, the task queue with
         * simulation data (neural network objects) and the
         * simulation speed.
         * 
         * @param _alien The alien to be tested.
         * @param q The SimulationData queue. Must be thread-safe.
         * @param _simSpeed Simulation speed. Default is 1.0.
         */

        super(_alien, _simSpeed);

        this.queue = q;
    }

    public void startSimulation(SimulationData data) {

        // turn physics back on
        this.physics.setEnabled(true);
        this.reset();
        this.currentSim = data;
        this.simTimeLimit = (float) data.getSimTime();
        this.currentAlienNode = instantiateAlien(this.alien, this.startLocation);
        this.currentAlienNode.addControl(new AlienBrain(data.getToEvaluate()));
        this.simInProgress = true;
    }

    protected void stopSimulation() {
        /*
         * Stop simulation.
         */
        this.simInProgress = false;
        if (this.currentSim != null) {
            synchronized (this.currentSim) {
                double fitness = this.calcFitness();
                this.currentSim.setFitness(fitness);
            }
            System.out.println("Stopping simulation! " + this.currentSim.toString());
        }
        // turn physics off to save CPU time
        this.physics.setEnabled(false);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        // turn physics off to save CPU time
        this.physics.setEnabled(false);
    }

    @Override
    public void reset() {
        super.reset();
        this.currentSim = null;
        this.simInProgress = false;
        this.simTimeLimit = 0.0f;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (this.currentSim != null) {
            // push unfinished simulation back to queue
            queue.add(this.currentSim);
        }
        this.currentSim = null;
    }

    @Override
    public void update(float tpf) {
        if (simInProgress) {
            simTimeLimit -= tpf * physics.getSpeed();
            if (simTimeLimit < 0f) {
                // stop simulation and report result
                stopSimulation();
            }
        } else {
            // try to poll task from the queue
            if (toKill) {
                this.stateManager.detach(this);
            } else {
                SimulationData s;
                s = this.queue.peek();
                if (s != null) {
                    System.out.println(Thread.currentThread().getId() + ": starting simulation!");
                    startSimulation(s);
                }
            }
        }
    }
}