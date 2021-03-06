/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * SimulatorAppState provides basic functionality of a alien simulator without
 * rendering.
 *
 * @author Sunny
 */
public abstract class SimulatorAppState extends AbstractAppState {

    // Resources from app
    protected SimpleApplication app;
    protected Node rootNode;
    protected AssetManager assetManager;
    protected AppStateManager stateManager;
    protected InputManager inputManager;
    protected ViewPort viewPort;
    protected BulletAppState physics;
    // Physics Fields
    protected Alien alien;
    protected AlienNode currentAlienNode;
    protected Node simRoot;
    protected Node scene;
    // Physics Timing Fields
    protected final double simSpeed;
    protected final double accuracy;
    private float originalSpeed;
    protected final float fixedTimeStep;
    // Flags
    protected volatile boolean toKill;
    protected boolean simInProgress;
    protected boolean isFixedTimeStep;
    // World related
    protected Vector3f startLocation = new Vector3f(0, 4, 0);
    protected Geometry floorGeometry;
    public final Vector3f standardG;

    /**
     * Constructs a minimal AppState for simulation which provides basic utility
     * functions.
     *
     * @param _alien the Alien to be simulation, <code>null</code> if the Alien
     * to simulate is specified manually during instantiation
     * @param _simSpeed the simulation speed, 1.0 should be default
     */
    public SimulatorAppState(Alien _alien, double _simSpeed, double _accuracy) {
        this.simSpeed = _simSpeed;
        this.accuracy = _accuracy;
        this.alien = _alien;
        Vector3f temp = new Vector3f();
        Vector3f.UNIT_Y.mult(-9f, temp);
        standardG = temp;
        this.isFixedTimeStep = false;
        this.fixedTimeStep = 0.0f;
    }
    
    public SimulatorAppState(Alien _alien, double _simSpeed, double _accuracy, double _fixedTimeStep) {
        this.simSpeed = _simSpeed;
        this.accuracy = _accuracy;
        this.alien = _alien;
        Vector3f temp = new Vector3f();
        Vector3f.UNIT_Y.mult(-9f, temp);
        standardG = temp;
        this.isFixedTimeStep = true;
        this.fixedTimeStep = (float)_fixedTimeStep;
    }

    /**
     * Set toKill flag, true will cause this AppState to detach itself in next
     * update cycle.
     *
     * @param _toKill the new value for toKill flag
     */
    public void setToKill(boolean _toKill) {
        //might need to watch for concurrency issues
        this.toKill = _toKill;
    }

    /**
     * Return the current value of toKill flag, should only be called from an
     * update/simpleUpdate method to avoid concurrency issues.
     *
     * @return the current value of toKill flag
     */
    public boolean getToKill() {
        return this.toKill;
    }

    /**
     * Calculate fitness for the current simulated alien. Current implementation
     * returns the x-coordinate of the main body of alien.
     *
     * @return the fitness value for current simulation in double
     */
    protected double calcFitness() {

        if (this.currentAlienNode != null) {
            Vector3f pos = AlienHelper.getGeometryLocation(this.currentAlienNode.geometries.get(0));
            double fitness = pos.x;
            if (Double.isNaN(fitness)) {
                System.err.println(pos);
                throw new RuntimeException("bad fitness");
            }
            return fitness;
        } else {
            throw new UnsupportedOperationException("No simulation running! Cannot compute fitness.");
        }
    }

    /**
     * Set up textures(if needed) and reset the scene.
     */
    @Override
    public void initialize(AppStateManager stateManager, Application app) {

        this.app = (SimpleApplication) app; // can cast Application to something more specific
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        setupTextures();
        this.stateManager = this.app.getStateManager();
        this.inputManager = this.app.getInputManager();
        this.viewPort = this.app.getViewPort();
        this.physics = this.stateManager.getState(BulletAppState.class);
        this.originalSpeed = this.physics.getSpeed();
        
        if (this.isFixedTimeStep) {
            this.stateManager.detach(this.physics);
            physics = new FixedTimeStepBulletAppState(this.fixedTimeStep);
            this.stateManager.attach(this.physics);
        }
        
        this.physics.setSpeed((float) simSpeed);
        this.physics.getPhysicsSpace().setAccuracy((float) accuracy);
        this.physics.getPhysicsSpace().setMaxSubSteps(4 * (int)((1.0/60.0)/this.accuracy));
        this.setToKill(false);

        this.reset();
    }

    protected void resetGravity() {
        this.physics.getPhysicsSpace().setGravity(standardG);
    }

    /**
     * Reset the scene, removing all current instances of aliens.
     */
    public void reset() {

        this.currentAlienNode = null;
        this.simInProgress = true;

        if (this.simRoot != null) {
            this.physics.getPhysicsSpace().removeAll(simRoot);
            this.simRoot.removeFromParent();
        }
        this.simRoot = new Node("SimulatorRoot");
        this.initialiseWorld();
        this.rootNode.attachChild(simRoot);
    }

    protected abstract void setupTextures();
    
    public SimpleApplication getApp() {
        return this.app;
    }
    
    /**
     * Initialise the scene by creating a floor box.
     */
    protected void initialiseWorld() {

        Box floorBox = new Box(140f, 1f, 140f);
        floorGeometry = new Geometry("Floor", floorBox);
        floorGeometry.setLocalTranslation(0, -5, 0);
        floorGeometry.addControl(new RigidBodyControl(0));
        simRoot.attachChild(floorGeometry);
        physics.getPhysicsSpace().add(floorGeometry);
    }

    /**
     * Spawn a new alien at a specified location.
     *
     * @param a the Alien to instantiate
     * @param location the location of newly instantiated Alien
     */
    protected AlienNode instantiateAlien(Alien a, Vector3f location) {

        this.currentAlienNode = AlienHelper.assembleAlien(a, location);
        this.alien = a;
        physics.getPhysicsSpace().addAll(this.currentAlienNode);
        simRoot.attachChild(this.currentAlienNode);

        return this.currentAlienNode;
    }

    /**
     * Return true if a simulation is in progress. Used to allow stopping
     * simulation gracefully.
     *
     * @return the flag indicating if a simulation is in progress
     */
    public boolean isRunningSimulation() {
        return simInProgress;
    }

    /**
     * Remove all geometries created by this AppState and perform appropriate
     * clean up.
     */
    @Override
    public void cleanup() {
        super.cleanup();
        this.physics.getPhysicsSpace().removeAll(simRoot);
        this.simRoot.removeFromParent();
        this.currentAlienNode = null;
        this.physics.setSpeed(originalSpeed);
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    // Note that update is only called while the state is both attached and enabled.
    @Override
    public void update(float tpf) {
        if (toKill) {
            this.stateManager.detach(this);
        }
    }
}
