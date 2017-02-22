/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.FlyByCamera;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;
import java.util.Queue;
import org.encog.ml.MLRegression;

/**
 *
 * @author Peter
 */
public class DrawingAppState extends SimulatorAppState {

    protected Camera cam;
    protected FlyByCamera flyCam;
    protected ViewPort guiViewPort;
    protected AudioRenderer audioRenderer;
    protected RenderManager renderManager;

    
    //Textures
    private Texture alienTexture1;
    private Texture alienTexture2;
    private Texture alienTexture3;
    private Texture grassTexture;
    private Texture skyTexture;
    
    //Materials
    private Material alienMaterial1;
    private Material alienMaterial2;
    private Material alienMaterial3;
    private Material grassMaterial;
    private Material skyMaterial;
    
    public DrawingAppState(Alien _alien, double _simSpeed) {
        super(_alien, _simSpeed);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.cam = this.app.getCamera();
        this.flyCam = this.app.getFlyByCamera();
        this.guiViewPort = this.app.getGuiViewPort();
        this.audioRenderer = this.app.getAudioRenderer();
        this.renderManager = this.app.getRenderManager();
    }

    protected void initialiseWorld() {

        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        simRoot.addLight(light);
        viewPort.setBackgroundColor(new ColorRGBA(98 / 255f, 167 / 255f, 224 / 255f, 1f));
        
        Box floorBox = new Box(140f, 1f, 140f);
        floorGeometry = new Geometry("Floor", floorBox);
        floorGeometry.setLocalTranslation(0, -5, 0);
        floorGeometry.addControl(new RigidBodyControl(0));
        floorGeometry.setMaterial(grassMaterial);
        floorGeometry.getMesh().scaleTextureCoordinates(new Vector2f(40f, 40f));
        
        simRoot.attachChild(floorGeometry);
        physics.getPhysicsSpace().add(floorGeometry);
        
        //setupBackground();
    }

    protected void setupBackground() {

        Picture p = new Picture("background");
        p.setMaterial(skyMaterial);
        p.setWidth(guiViewPort.getCamera().getWidth());
        p.setHeight(guiViewPort.getCamera().getHeight());
        p.setPosition(0, 0);
        p.updateGeometricState();
        ViewPort pv = renderManager.createPreView("background", cam);
        pv.setClearFlags(true, true, true);
        pv.attachScene(p);
        viewPort.setClearFlags(false, true, true);
        guiViewPort = pv;

    }

    @Override
    public void setupTextures() {
        grassTexture = assetManager.loadTexture("Textures/grass4.png");
        grassTexture.setAnisotropicFilter(4);
        grassTexture.setWrap(Texture.WrapMode.Repeat);
        grassMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        grassMaterial.setTexture("ColorMap", grassTexture);
        skyTexture = assetManager.loadTexture("Textures/sky1.jpg");
        skyTexture.setWrap(Texture.WrapMode.Repeat);
        skyMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        skyMaterial.setTexture("ColorMap", skyTexture);

        alienTexture1 = assetManager.loadTexture("Textures/alien1.jpg");
        alienTexture1.setWrap(Texture.WrapMode.Repeat);
        alienMaterial1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial1.setTexture("ColorMap", alienTexture1);

        alienTexture2 = assetManager.loadTexture("Textures/alien2.jpg");
        alienTexture2.setWrap(Texture.WrapMode.Repeat);
        alienMaterial2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial2.setTexture("ColorMap", alienTexture2);
        alienTexture3 = assetManager.loadTexture("Textures/alien3.jpg");
        alienTexture3.setWrap(Texture.WrapMode.Repeat);
        alienMaterial3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        alienMaterial3.setTexture("ColorMap", alienTexture3);
    }

    @Override
    protected AlienNode instantiateAlien(Alien a, Vector3f location) {
        /*
         * Spawn a new alien at a specified location.
         */

        AlienNode node = super.instantiateAlien(a, location);

        // TEMP
        node.setMaterial(alienMaterial2);

        return node;
    }
}