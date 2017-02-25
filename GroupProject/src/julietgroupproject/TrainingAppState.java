/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.scene.Node;
import java.util.Queue;

/**
 * An AppState providing useful functionalities for alien training process.
 *
 * @author GeorgeLenovo
 */
public class TrainingAppState extends SimulatorAppState {

    // Simulation related fields
    protected Queue<SimulationData> queue;
    protected SimulationData currentSim;
    protected float simTimeLimit;
    protected boolean fixedTimeStep;
    protected final float timeStep;
    // workaround weird bug of first simulation
    protected boolean isFirstSimulation = true;

    /**
     * Constructor, taking an Alien object as the Alien to be tested in this
     * simulator, the task queue with simulation data (neural network objects)
     * and the simulation speed.
     *
     * @param _alien The alien to be tested.
     * @param q The SimulationData queue. Must be thread-safe.
     * @param _simSpeed Simulation speed. Default is 1.0.
     */
    public TrainingAppState(Alien _alien, Queue<SimulationData> q, double _simSpeed, double _accuracy) {

        super(_alien, _simSpeed, _accuracy);
        this.queue = q;
        this.timeStep = 0.0f;
        this.fixedTimeStep = false;
    }
    
    
    public TrainingAppState(Alien _alien, Queue<SimulationData> q, double _simSpeed, double _accuracy, double _fixedTimeStep) {

        super(_alien, _simSpeed, _accuracy);
        this.queue = q;
        this.timeStep = (float)_fixedTimeStep;
        this.fixedTimeStep = true;
    }

    /**
     * Start a new simulation. This method should not be called externally by
     * another thread.
     *
     * @param data the SimulationData object containing the ANN to be tested and
     * other parameters
     */
    protected void startSimulation(SimulationData data) {
        // turn physics back on
        this.physics.setEnabled(true);
        this.reset();
        this.currentSim = data;
        this.simTimeLimit = (float) data.getSimTime();
        this.currentAlienNode = instantiateAlien(this.alien, this.startLocation);
        AlienBrain brain;
        if (fixedTimeStep) {
            brain = new AlienBrain(data.getToEvaluate(), this.physics.getPhysicsSpace().getAccuracy(), this.physics.getSpeed(), this.timeStep);
        } else {
            brain = new AlienBrain(data.getToEvaluate(), this.physics.getPhysicsSpace().getAccuracy(), this.physics.getSpeed());
        }
        this.currentAlienNode.addControl(brain);
        this.simInProgress = true;
    }

    /**
     * Stop simulation and set fitness value.
     */
    protected void stopSimulation() {

        this.simInProgress = false;
        if (this.currentSim != null && !this.isFirstSimulation) {
            double fitness = this.calcFitness();
            this.currentSim.setFitness(fitness);
            System.out.println("Stopping simulation! " + this.currentSim.toString());
        }
        // turn physics off to save CPU time
        this.physics.setEnabled(false);
        this.isFirstSimulation = false;
    }

    /**
     * Perform initialisation and disable physics.
     */
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        if (this.fixedTimeStep) {
            this.stateManager.detach(this.physics);
            physics = new BulletAppState() {
                @Override
                public void update(float tpf) {
                    if (debugEnabled && debugAppState == null && pSpace != null) {
                        debugAppState = new BulletDebugAppState(pSpace);
                        stateManager.attach(debugAppState);
                    } else if (!debugEnabled && debugAppState != null) {
                        stateManager.detach(debugAppState);
                        debugAppState = null;
                    }
                    if (!active) {
                        return;
                    }
                    pSpace.distributeEvents();
                    this.tpf = timeStep;
                }
            };
            this.physics.setSpeed((float)this.simSpeed);
            this.stateManager.attach(this.physics);
            this.physics.getPhysicsSpace().setAccuracy((float)this.accuracy);
            this.physics.getPhysicsSpace().setMaxSubSteps(200);
        }
        
        
        this.reset();
        resetGravity();
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
        System.out.println(Thread.currentThread().getId() + ": Cleanup for TrainingAppState");
    }

    @Override
    public void update(float tpf) {
        if (simInProgress) {
            if (this.fixedTimeStep) {
                tpf = this.timeStep;
            }
            simTimeLimit -= tpf * physics.getSpeed();
            if (simTimeLimit < 0f) {
                // stop simulation and report result
                stopSimulation();
            }
        } else {
            // try to poll task from the queue
            if (toKill) {
                System.out.println(Thread.currentThread().getId() + ": TrainingAppState detaching myself");
                System.out.println("Detached:" + this.stateManager.detach(this));
            } else {
                SimulationData s;
                if (isFirstSimulation) {
                    s = this.queue.peek();
                } else {
                    s = this.queue.poll();
                }
                if (s != null) {
                    System.out.println(Thread.currentThread().getId() + ": starting simulation!");
                    startSimulation(s);
                }
            }
        }
    }
}
