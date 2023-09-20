package com.android.shootgame;

import static com.android.shootgame.utils.Utils.checkIsSupportedDeviceOrFinish;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import com.android.bulletphysics.PhysicsManager;
import com.android.bulletphysics.PhysicsNode;
import com.android.shootgame.databinding.ActivityPhysicsSimulationBinding;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class PhysicsSimulationActivity extends AppCompatActivity {

    private final static float SPHERE_RADIUS = 3.0f;
    private final static int TOTAL_GAME_TIME = 15;
    private final static int SEC_IN_MILLIS = 1000;
    private final static int ZERO = 0;
    private ArSceneView sceneView ;
    private ActivityPhysicsSimulationBinding binding;
    private Material redColorMaterial, blueColorMaterial, yellowColorMaterial, grayColorMaterial;
    private PhysicsManager physicsManager;
    /// Scene Camera
    private Camera mainCamera;

    private Session session;

    public int gameTime;

    private ArrayList<PhysicsNode> bowlingPins = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(getApplicationContext(), "Device not supported", Toast.LENGTH_LONG).show();
            return;
        }
        // Case to check the ARCore is supported on this device or not.
        if (ArCoreApk.getInstance().checkAvailability(this) != ArCoreApk.Availability.SUPPORTED_INSTALLED) {
            Toast.makeText(getApplicationContext(), "ArCore not supported in activity ", Toast.LENGTH_LONG).show();
            return;
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_physics_simulation);
        sceneView = binding.sceneView;
        physicsManager = new PhysicsManager();

        //initialize physics world
        physicsManager.init();

        // Initialize ARCore session
        initializeARCore();

        setupListeners();

        createColorMaterials();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            session.resume();
            sceneView.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        session.pause();
        sceneView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        session.close();
        //remove physics world
        physicsManager.destroy();
    }

    private void setupListeners() {
        // Set click listener on the throw button.
        binding.throwButton.setOnClickListener(view -> createPhysicsSphereFromEye(SPHERE_RADIUS));

        // Set OnUpdateListener on the AR scene. This will update the physics world and objects in it.
        sceneView.getScene().addOnUpdateListener(frameTime -> {
            physicsManager.updatePhysicsObjects();
            physicsManager.stepPhysicsWorld(frameTime.getDeltaSeconds());
        });

        // Set click listener on the create game button.
        binding.createGameButton.setOnClickListener(view -> createGame());

        // Set click listener on start game button.
        binding.startGameButton.setOnClickListener(view -> {
            // Place bowling pins on the table.
            createBowlingPins(4);
            // Reset timer and score TextView's background and text to default.
            resetGame();
            binding.throwButton.setVisibility(View.VISIBLE);
            // Start countdown of the game
            new CountDownTimer(TOTAL_GAME_TIME * SEC_IN_MILLIS, SEC_IN_MILLIS){
                public void onTick(long millisUntilFinished){
                    // Update timer and score TextView's texts as per latest.
                    binding.timerTextview.setText(getString(R.string.remaining_time_text, gameTime));
                    binding.scoreTextview.setText(getString(R.string.score_text, countScore()));
                    gameTime--;
                }
                public  void onFinish(){
                    // Update timer and score TextView's background and text as game is over.
                    binding.timerTextview.setBackgroundColor(getColor(R.color.holo_red_dark));
                    binding.timerTextview.setText(getString(R.string.game_over));

                    binding.scoreTextview.setBackgroundColor(getColor(R.color.holo_red_dark));
                    binding.scoreTextview.setText(getString(R.string.score_text, countScore()));

                    binding.startGameButton.setText(R.string.restart_game);
                    binding.throwButton.setVisibility(View.INVISIBLE);

                    // Remove all bowling pins once game is finished.
                    removePins();
                }
            }.start();
        });
    }

    /**
     * Counts number pins which are gone beyond table boundaries.
     *
     * @return Number of pins went beyond table boundaries
     */
    private int countScore() {
        int count = 0;
        for(PhysicsNode node : bowlingPins) {
            float positionX = node.getLocalPosition().x;
            float positionY = node.getLocalPosition().y;
            float positionZ = node.getLocalPosition().z;

            if((positionX < -30 || positionX > 30) || positionY < -45.0 || (positionZ < -200 || positionZ > -40)){
                count++;
            }
        }
        return count;
    }

    /**
     * Resets game time, timer and score TextView's background and text to default.
     */
    private void resetGame() {
        // Reset game time
        gameTime = TOTAL_GAME_TIME;
        // Update texts and backgrounds of the score and time TextViews
        binding.timerTextview.setBackgroundColor(getColor(R.color.holo_green_dark));
        binding.timerTextview.setText(getString(R.string.remaining_time_text, TOTAL_GAME_TIME));
        binding.scoreTextview.setBackgroundColor(getColor(R.color.holo_green_dark));
        binding.scoreTextview.setText(getString(R.string.score_text, ZERO));
    }

    /**
     * Removes bowling pins placed on the table.
     */
    private void removePins() {
        if (!bowlingPins.isEmpty()) {
            for (PhysicsNode node : bowlingPins) {
                physicsManager.removePhysicsBody(node, sceneView.getScene());
            }
            bowlingPins.clear();
        }
    }

    /**
     * Generates table required for the bowling game.
     */
    private void createGame(){
        Vector3 size = new Vector3(10,80,10);
        Renderable legRenderable = ShapeFactory.makeCube(size,Vector3.zero(), yellowColorMaterial);

        // Create front left leg of the table.
        physicsManager.createGroundPhysicsNode(legRenderable, sceneView.getScene(), size, new Vector3(-25, -80, -45));

        // Create rear left leg of the table.
        physicsManager.createGroundPhysicsNode(legRenderable, sceneView.getScene(), size, new Vector3(-25, -80, -195));

        // Create front right leg of the table.
        physicsManager.createGroundPhysicsNode(legRenderable, sceneView.getScene(), size, new Vector3(25, -80, -45));

        // Create rear right leg of the table.
        physicsManager.createGroundPhysicsNode(legRenderable, sceneView.getScene(), size, new Vector3(25, -80, -195));

        // Create top of the table.
        Vector3 topSize = new Vector3(60,1,160);
        Renderable topRenderable = ShapeFactory.makeCube(topSize,Vector3.zero(), blueColorMaterial);
        physicsManager.createGroundPhysicsNode(topRenderable, sceneView.getScene(), topSize, new Vector3(0, -40, -120));
    }

    /**
     * Renders bowling pins on top of the table.
     *
     * @param numOfRows Number of bowling pins rows
     */
    private void createBowlingPins(int numOfRows){
        for (int row = 1; row <= numOfRows; row++) {
            float start_position_x = -((row - 1) * 6);
            float position_z = -160 - ((row - 1) * 6);
            for (int pin = 0; pin < row; pin++) {
                float position_x = start_position_x + (pin * 12);
                Vector3 position = new Vector3(position_x, -38, position_z);
                Renderable cylinder = ShapeFactory.makeCylinder(2, 8, Vector3.zero(), redColorMaterial);
                PhysicsNode node = physicsManager.createCylinderPhysicsNode(cylinder, sceneView.getScene(), 2, 8, position, 2f);
                bowlingPins.add(node);
            }
        }
    }

    /**
     * Creates a group of freely falling objects
     */
    private void createColorMaterials(){

        CompletableFuture<Material> boxMaterial =  MaterialFactory.makeOpaqueWithColor(this,
                new Color(android.graphics.Color.RED));
        CompletableFuture<Material> groundMaterial =  MaterialFactory.makeOpaqueWithColor(this,
                new Color(android.graphics.Color.BLUE));
        CompletableFuture<Material> cylinderMaterial =  MaterialFactory.makeOpaqueWithColor(this,
                new Color(android.graphics.Color.YELLOW));
        CompletableFuture<Material> sphereMaterial =  MaterialFactory.makeOpaqueWithColor(this,
                new Color(android.graphics.Color.DKGRAY));
        CompletableFuture.allOf(boxMaterial,groundMaterial,cylinderMaterial,sphereMaterial).thenAccept(
                (v) -> {
                    if(boxMaterial.isDone() && groundMaterial.isDone() &&
                            cylinderMaterial.isDone() && sphereMaterial.isDone()){
                        try {
                            blueColorMaterial = groundMaterial.get();
                            redColorMaterial = boxMaterial.get();
                            yellowColorMaterial = cylinderMaterial.get();
                            grayColorMaterial = sphereMaterial.get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * Throws a Sphere from the camera eye
     *
     * @param radius radius of the sphere
     */
    private void createPhysicsSphereFromEye(float radius){
        Vector3 lookat = mainCamera.getForward();
        Vector3 camPos = mainCamera.getWorldPosition();

        Renderable sphere = ShapeFactory.makeSphere(radius, Vector3.zero(), grayColorMaterial);
        PhysicsNode node = physicsManager.createSpherePhysicsNodeFromEye(sphere,sceneView.getScene(),
                radius,camPos,lookat,490.f,6);
    }

    /**
     * Initialize the ARCore session and setup session to {@link ArSceneView}.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initializeARCore() {
        try {
            // Create new Session and configure it.
            session = new Session(this);
            Config config = new Config(session);
            config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
            session.configure(config);

            // Connect the Session to the ARSceneView
            sceneView.setupSession(session);

            mainCamera = sceneView.getScene().getCamera();
            mainCamera.setFarClipPlane(400);
        } catch (UnavailableArcoreNotInstalledException e) {
            // Handle ARCore not installed
        } catch (Exception e) {
            // Handle other exceptions
        }
    }
}
