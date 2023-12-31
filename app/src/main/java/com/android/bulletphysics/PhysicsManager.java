package com.android.bulletphysics;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.NodeParent;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;

public class PhysicsManager {

    // Used to load the 'native-lib' physics library on  Class Load.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * Initializes the physics environment. Should be the first method to be called
     */
    public native void init();

    /**
     * Creates a static box representing the physics world terrain
     * @param size box size
     * @param position initial position
     * @param attachedNode attached renderable node
     * @return unique id (native pointer) of the created physics body
     */
    public native long createGround(Vector3 size, Vector3 position, Node attachedNode);

    /**
     * Creates a box physics shape
     * @param size box size
     * @param initialPosition initial world position
     * @param mass shape mass.if > 0, created box will be dynamic otherwise will be static
     * @param attachedNode attached renderable node
     * @return unique id (native pointer) of the created physics body
     */
    public native long createPhysicsBox(Vector3 size, Vector3 initialPosition, float mass,
                                        Node attachedNode);

    /**
     *  Creates a cylinder physics shape
     * @param size cylinder size as x:radius,y:height,z:radius
     * @param initialPosition  initial world position
     * @param mass shape mass.if > 0, created shape will be dynamic otherwise will be static
     * @param attachedNode attached renderable node
     * @return unique id (native pointer) of the created physics body
     */
    public native long createPhysicsCylinder( Vector3 size, Vector3 initialPosition, float mass,
                                              Node attachedNode);

    /**
     * Creates a sphere physics shape
     * @param radius sphere radius
     * @param initialPosition initial world position
     * @param mass shape mass.if > 0, created shape will be dynamic otherwise will be static
     * @param attachedNode  attached renderable node
     * @return unique id (native pointer) of the created physics body
     */
    public native long createPhysicsSphere(float radius, Vector3 initialPosition, float mass,
                                           Node attachedNode);

    /**
     * Creates and throws a box physics shape from the camera eye position
     * @param size shape size
     * @param initialPosition initial world position
     * @param lookat the camera lookat (throw) direction ,normalized .
     * @param mass shape mass.if > 0, created shape will be dynamic otherwise will be static
     * @param force a factor to control the throw force
     * @param attachedNode attached renderable node
     * @return unique id (native pointer) of the created physics body
     */
    public native long createPhysicsBoxFromEye(Vector3 size, Vector3 initialPosition,
                                               Vector3 lookat, float mass,float force, Node attachedNode);

    /**
     * Creates and throws a Cylinder physics shape from the camera eye position
     * @param size cylinder size as x:radius,y:height,z:radius
     * @param initialPosition initial world position
     * @param lookat the camera lookat (throw) direction ,normalized .
     * @param mass shape mass.if > 0, created shape will be dynamic otherwise will be static
     * @param force a factor to control the throw force
     * @param attachedNode attached renderable node
     * @return unique id (native pointer) of the created physics body
     */
    public native long createPhysicsCylinderFromEye(Vector3 size, Vector3 initialPosition,
                                                    Vector3 lookat, float mass,float force, Node attachedNode);

    /**
     * Creates and throws a Sphere physics shape from the camera eye position
     * @param radius sphere radius
     * @param initialPosition initial world position
     * @param lookat the camera lookat (throw) direction ,normalized .
     * @param mass shape mass.if > 0, created shape will be dynamic otherwise will be static
     * @param force a factor to control the throw force
     * @param attachedNode attached renderable node
     * @return unique id (native pointer) of the created physics body
     */
    public native long createPhysicsSphereFromEye(float radius, Vector3 initialPosition,
                                                  Vector3 lookat, float mass,float force,
                                                  Node attachedNode);

    /**
     * steps the physics simulation
     * @param timeStep last frame time
     */
    public native void stepPhysicsWorld(float timeStep);

    /**
     * Removes and deletes the provided physics body from simulation
     * @param id body id.
     */
    public native void removePhysicsBody(long id);

    /**
     * Updates the transformation of the physics bodies renderables. Should be called once per frame.
     */
    public native void updatePhysicsObjects();

    /**
     * Destroys physics world and cleans memory
     */
    public native void destroy();

    /**
     * Returns the current number of simulated bodies
     * @return
     */
    public native long getNumberOfPhysicsBodies();

    /**
     * Creates a Renderable PhysicsNode object representing Ground.
     * @param groundBox renderable box shape
     * @param parent {@link NodeParent} of the renderable node
     * @param size {@link Vector3} providing size of the each side of the cube/box
     * @param initialPosition initial position of the renderable and rigid body
     * @return
     */
    public PhysicsNode createGroundPhysicsNode(Renderable groundBox, NodeParent parent, Vector3 size,
                                               Vector3 initialPosition){
        PhysicsNode node = new PhysicsNode();
        node.setRenderable(groundBox);
        node.setMass(0.0f);
        node.setParent(parent);
        long physicsBody = createGround(size,initialPosition,node);
        node.setPhysicsObjectId(physicsBody);
        return node;
    }

    /**
     * Creates a Renderable PhysicsNode object representing a free falling box.
     * @param box Renderable box
     * @param parent {@link NodeParent} of the renderable node
     * @param size {@link Vector3} providing size of the each side of the cube/box
     * @param initialPosition initial position of the renderable and rigid body
     * @param mass mass of the rigid body
     * @return
     */
    public PhysicsNode createBoxPhysicsNode(Renderable box,NodeParent parent,Vector3 size,
                                            Vector3 initialPosition,float mass){
        PhysicsNode node = new PhysicsNode();
        node.setRenderable(box);
        node.setMass(mass);
        node.setParent(parent);
        long physicsBody = createPhysicsBox(size,
                initialPosition,mass,node);
        node.setPhysicsObjectId(physicsBody);
        return node;
    }

    /**
     * Creates a Renderable PhysicsNode object representing an eye-thrown box  .
     * @param box Renderable box
     * @param parent {@link NodeParent} of the renderable node
     * @param size {@link Vector3} providing size of the each side of the cube/box
     * @param camPosition position of the camera
     * @param lookAt the camera lookat (throw) direction
     * @param force a factor to control the throw force
     * @param mass mass of the rigid body
     * @return
     */
    public PhysicsNode createBoxPhysicsNodeFromEye(Renderable box,NodeParent parent,Vector3 size,
                                                   Vector3 camPosition,Vector3 lookAt,
                                                   float force,float mass){
        PhysicsNode node = new PhysicsNode();
        node.setRenderable(box);
        node.setMass(mass);
        node.setParent(parent);
        long physicsBody = createPhysicsBoxFromEye(size,camPosition,lookAt.normalized(),
                mass,force,node);
        node.setPhysicsObjectId(physicsBody);
        return node;
    }

    /**
     * Creates a Renderable PhysicsNode object representing a free falling Cylinder.
     * @param cylinder Renderable cylinder
     * @param parent {@link NodeParent} of the renderable node
     * @param radius radius of the cylinder
     * @param height height of the cylinder
     * @param initialPosition initial position of the renderable and rigid body
     * @param mass mass of the rigid body
     * @return
     */
    public PhysicsNode createCylinderPhysicsNode(Renderable cylinder,NodeParent parent,
                                                 float radius,float height,
                                                 Vector3 initialPosition,float mass){
        PhysicsNode node = new PhysicsNode();
        node.setRenderable(cylinder);
        node.setMass(mass);
        node.setParent(parent);
        long physicsBody = createPhysicsCylinder(new Vector3(radius,height,radius),
                initialPosition,mass,node);
        node.setPhysicsObjectId(physicsBody);
        return node;
    }

    /**
     * Creates a Renderable PhysicsNode object representing an eye-thrown cylinder.
     * @param cylinder Renderable cylinder
     * @param parent {@link NodeParent} of the renderable node
     * @param radius radius of the cylinder
     * @param height height of the cylinder
     * @param camPosition position of the camera
     * @param lookAt the camera lookat (throw) direction
     * @param force a factor to control the throw force
     * @param mass mass of the rigid body
     * @return
     */
    public PhysicsNode createCylinderPhysicsNodeFromEye(Renderable cylinder,NodeParent parent,
                                                        float radius,float height,
                                                        Vector3 camPosition,Vector3 lookAt,
                                                        float force,float mass){
        PhysicsNode node = new PhysicsNode();
        node.setRenderable(cylinder);
        node.setMass(mass);
        node.setParent(parent);
        long physicsBody = createPhysicsCylinderFromEye(new Vector3(radius,height,radius),
                camPosition,
                lookAt.normalized(),
                mass,force,node);
        node.setPhysicsObjectId(physicsBody);
        return node;
    }

    /**
     * Creates a Renderable PhysicsNode object representing a free falling Sphere.
     * @param sphere Renderable sphere
     * @param parent {@link NodeParent} of the renderable node
     * @param radius radius of the sphere
     * @param initialPosition initial position of the renderable and rigid body
     * @param mass mass of the rigid body
     *
     * @return new {@link PhysicsNode} created for renderable
     */
    public PhysicsNode createSpherePhysicsNode(Renderable sphere,NodeParent parent,
                                               float radius,Vector3 initialPosition,float mass){
        PhysicsNode node = new PhysicsNode();
        node.setRenderable(sphere);
        node.setMass(mass);
        node.setParent(parent);
        long physicsBody = createPhysicsSphere(radius,
                initialPosition,mass,node);
        node.setPhysicsObjectId(physicsBody);
        return node;
    }

    /**
     * Creates a Renderable PhysicsNode object representing an eye-thrown Sphere.
     *
     * @param sphere renderable sphere
     * @param parent {@link NodeParent} of the renderable node
     * @param radius radius of the sphere
     * @param camPosition position of the camera
     * @param lookAt the camera lookat (throw) direction
     * @param force a factor to control the throw force
     * @param mass mass of the rigid body
     *
     * @return new {@link PhysicsNode} created for renderable
     */
    public PhysicsNode createSpherePhysicsNodeFromEye(Renderable sphere,NodeParent parent,
                                                      float radius,Vector3 camPosition,
                                                      Vector3 lookAt,float force,float mass){
        PhysicsNode node = new PhysicsNode();
        node.setRenderable(sphere);
        node.setMass(mass);
        node.setParent(parent);
        long physicsBody = createPhysicsSphereFromEye(radius,camPosition,
                lookAt.normalized(),
                mass,force,node);
        node.setPhysicsObjectId(physicsBody);
        return node;
    }

    /**
     * Removes simulated body of the renderable node.
     *
     * @param node a renderable node
     * @param parent {@link NodeParent} of the renderable node
     */
    public void removePhysicsBody(PhysicsNode node, NodeParent parent) {
        parent.removeChild(node);
        removePhysicsBody(node.getPhysicsObjectId());
    }
}
