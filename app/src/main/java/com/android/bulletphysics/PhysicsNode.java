package com.android.bulletphysics;

import com.google.ar.sceneform.AnchorNode;

public class PhysicsNode extends AnchorNode {

    public long getPhysicsObjectId() {
        return physicsObjectId;
    }

    public void setPhysicsObjectId(long physicsObjectId) {
        this.physicsObjectId = physicsObjectId;
    }

    public boolean isStatic() {
        return mass == 0.0f;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }
    private float mass = 0f;
    private long physicsObjectId = Long.MIN_VALUE ;

}
