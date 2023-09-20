package com.android.shootgame;

import static com.android.shootgame.utils.Utils.checkIsSupportedDeviceOrFinish;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.android.shootgame.databinding.ActivityCustomObjectBinding;
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
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.List;

public class CustomObjectActivity extends AppCompatActivity {

    private ActivityCustomObjectBinding binding;
    private ArSceneView sceneView;
    private Session session;
    private ModelRenderable modelRenderable;
    private Material colorMaterial = null;

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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_custom_object);

        sceneView = binding.sceneView;

        // Load the 3D model
        loadModel();

        // Set click listeners on all buttons
        setButtonClickListeners();

        // Initialize ARCore
        initializeARCore();

        // Set touch listener on ArSceneView
        sceneView.setOnTouchListener(this::onTouched);
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
        binding.redButton.setOnClickListener(view -> makeOpaqueWithColor(ColorType.RED));

        binding.blueButton.setOnClickListener(view -> makeOpaqueWithColor(ColorType.BLUE));

        binding.greenButton.setOnClickListener(view -> makeOpaqueWithColor(ColorType.GREEN));
    }

    private void loadModel() {
        // Load 3D model here and handle the result in the callback
        RenderableSource renderableSource =
            RenderableSource.builder()
                    .setSource(this, Uri.parse("models/human.glb"), RenderableSource.SourceType.GLB)
                    .setRecenterMode(RenderableSource.RecenterMode.CENTER)
                    .setScale(0.1f)
                    .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .build()
                .thenAccept(renderable -> {
                    if (colorMaterial != null) {
                        renderable.setMaterial(colorMaterial);
                    }
                    modelRenderable = renderable;
                });
    }

    /**
     * Initialize the ARCore session and setup session to {@link ArSceneView}.
     */
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

    private void makeOpaqueWithColor(ColorType colorType) {
        @ColorInt int color;
        switch (colorType) {
            case RED:
                color = android.graphics.Color.RED;
                break;
            case BLUE:
                color = android.graphics.Color.BLUE;
                break;
            case GREEN:
                color = android.graphics.Color.GREEN;
                break;
            default:
                color = Integer.MIN_VALUE;
        }

        if (color != Integer.MIN_VALUE) {
            MaterialFactory.makeOpaqueWithColor(this, new Color(color))
                           .thenAccept(material -> {
                               colorMaterial = material;
                               loadModel();
                           });
        }
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
                        anchorNode.setRenderable(modelRenderable);
                        anchorNode.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));

                        // Only handle the first plane tap and break the loop
                        return true;
                    }
                }
            }
        }
        return false;
    }

    enum ColorType {
        RED,
        BLUE,
        GREEN
    }
}
