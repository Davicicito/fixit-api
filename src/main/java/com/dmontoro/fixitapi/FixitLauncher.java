package com.dmontoro.fixitapi;

/**
 * Clase lanzadora para evitar el error:
 * "JavaFX runtime components are missing"
 */
public class FixitLauncher {
    public static void main(String[] args) {
        // Redirige a la clase que arranca Spring y JavaFX
        FixitApiApplication.main(args);
    }
}