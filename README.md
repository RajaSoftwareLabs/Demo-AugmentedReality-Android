# Demo-AugmentedReality-Android (ARWorld)

ARWorld is an augmented reality based application built to demonstrate AR and physics simulation concepts. We have built this application using ARCore, Sceneform and bullet framework. This application consists of a total of 4 screens:

HomeScreen: From this screen you can navigate to the remaining three screens.
Basic object demo screen: In this screen you can place default objects in the real world.
Custom object demo screen: In this Screen you can place custom objects in the real world.
Physics simulation: This screen consists of a ‘Shooting game’ where we can interact with AR world objects and play shooting game using those objects.

## Technologies used:

An application employed numerous frameworks and tools to enhance user's experience

- [ARCore](https://developers.google.com/ar/develop) - Framework to build augmented reality experiences.
- [Sceneform](https://developers.google.com/sceneform/develop) - Framework to render realistic 3D scenes without having to learn OpenGL.
- [Bullet](https://pybullet.org/wordpress/) - A physics simulation engine.
- [Android Studio](https://developer.android.com/studio) - The official Integrated Development Environment (IDE) for Android app development.

## How to build:

- First you need to import this project into Android studio.
- We have integrated ARWorld and the physics world using the [‘Bullet’](https://pybullet.org/wordpress/) library. So we need to first install the latest version of NDK and CMake from Android studio SDK tools.
- After installing NDK and CMake sync the project and build it.

## Limitations:

- Understanding the physical environments depends upon device capability.
- Sceneform is not supported on physical devices with lower than Android version 7.0.
- Must have enough empty space around 2 meters for your plane detection.

## This code/software is NOT licensed and is not open for use/change/distribution. Please open an issue / pull-request if you require the same.

