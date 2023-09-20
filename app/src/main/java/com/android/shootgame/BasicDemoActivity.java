package com.android.shootgame;

import static com.android.shootgame.utils.Utils.checkIsSupportedDeviceOrFinish;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.android.shootgame.databinding.ActivityBasicDemoBinding;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.List;

public class BasicDemoActivity extends AppCompatActivity {

    private ActivityBasicDemoBinding binding;
    private ArSceneView sceneView;
    private Session session;
    private ModelRenderable shapeRenderable;
    private Material originalMaterial;

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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_basic_demo);

        sceneView = binding.sceneView;

        // Initialize ARCore
        initializeARCore();

        // Set touch listener on ArSceneView
        sceneView.setOnTouchListener(this::onTouched);

        // Initialize shape renderable
        initRenderable();

        // Set click listeners on all buttons
        setButtonClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume ARCore session
        try {
            session.resume();
            sceneView.resume();
        } catch (CameraNotAvailableException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause ARCore session
        session.pause();
        sceneView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release ARCore session
        session.close();
        session = null;
        sceneView = null;
    }


    private void setButtonClickListeners() {
        binding.cubeButton.setOnClickListener(view -> makeOpaqueWithShape(Shape.CUBE));

        binding.sphereButton.setOnClickListener(view -> makeOpaqueWithShape(Shape.SPHERE));

        binding.cylinderButton.setOnClickListener(view -> makeOpaqueWithShape(Shape.CYLINDER));
    }

    /**
     * Initializes color material and default {@link ModelRenderable}.
     */
    // initializeDefaultRenderable
    private void initRenderable() {
        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
                       .thenAccept(material -> originalMaterial = material);

        makeOpaqueWithShape(Shape.CUBE);
    }

    /**
     * Method to create new ARCore session and setup session and set an {@link android.view.View.OnTouchListener}
     * to {@link ArSceneView}.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initializeARCore() {
        try {
            // Create new Session and configure it.
            session = new Session(this);
            Config config = new Config(session);
            config.setFocusMode(Config.FocusMode.AUTO);
            config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL);
            session.configure(config);

            // Connect the Session to the ARSceneView
            sceneView.setupSession(session);
        } catch (UnavailableArcoreNotInstalledException e) {
            // Handle ARCore not installed
        } catch (Exception e) {
            // Handle other exceptions
        }
    }

    /**
     * Method to create {@link ModelRenderable} as per provided {@link Shape} type.
     *
     * @param shapeType Type of the {@link Shape}.
     */
    private void makeOpaqueWithShape(Shape shapeType) {
        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED)).thenAccept(material -> {
            switch (shapeType) {
                case CUBE:
                    Vector3 cubeSize = new Vector3(0.05f, 0.05f, 0.05f);
                    shapeRenderable = ShapeFactory.makeCube(cubeSize, Vector3.zero(), material);
                    break;
                case SPHERE:
                    shapeRenderable = ShapeFactory.makeSphere(0.05f, Vector3.zero(), material);
                    break;
                case CYLINDER:
                    shapeRenderable = ShapeFactory.makeCylinder(0.05f, 0.05f, Vector3.zero(), material);
                    break;
            }
            originalMaterial = material;

            shapeRenderable.setShadowCaster(false);
            shapeRenderable.setShadowReceiver(false);
        });
    }

    private boolean onTouched(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // Perform a hit test on the ARFrame to get the hit result
            List<HitResult> hitResults = sceneView.getArFrame().hitTest(event);

            // Filter the hit results to find a plane
            if (hitResults != null && !hitResults.isEmpty()) {
                for (HitResult hitResult: hitResults) {
                    Trackable trackable = hitResult.getTrackable();
                    if (trackable instanceof Plane
                            && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {
                        // Create an anchor at the hit pose
                        Anchor anchor = hitResult.createAnchor();

                        // Create an AnchorNode to hold the anchor
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(sceneView.getScene());
                        anchorNode.setRenderable(shapeRenderable);
                        anchorNode.setLocalScale(new Vector3(1f, 1f, 1f));

                        // Only handle the first plane tap and break the loop
                        return true;
                    }
                }
            }
        }
        return false;
    }

    enum Shape {
        CUBE,
        SPHERE,
        CYLINDER
    }
}
