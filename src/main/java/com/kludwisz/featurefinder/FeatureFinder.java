package com.kludwisz.featurefinder;

public interface FeatureFinder extends Runnable {

    void setWorldSeed(long worldseed);

    String getFeatureTPCommand();

    default String getFeedbackMessage() {
        return "Done!";
    }

    default String name() {
        return "Feature Finder";
    }
}
