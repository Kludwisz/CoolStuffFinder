package com.kludwisz.featurefinder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MultiFeatureFinder implements FeatureFinder {
    private final ArrayList<FeatureFinder> finders;
    private final ArrayList<Boolean> enabledFinders;
    private final Consumer<FeatureFinder> updateFunction;

    private long worldseed;

    @Override
    public void setWorldSeed(long worldseed) {
        this.worldseed = worldseed;
    }

    @Override
    public String getFeatureTPCommand() {
        throw new UnsupportedOperationException("MultiFeatureFinder does not directly support getFeatureTPCommand()");
    }

    // ------------------------------------------------------------------------------------------------------------

    public static List<FeatureFinder> getAllFinders() {
        return List.of(
                new ObbyFinder(),
                new RPFinder(),
                new RPFinder().lootingSwords(),
                new MultiLootingFinder()
        );
    }

    public MultiFeatureFinder(List<FeatureFinder> finderList, Consumer<FeatureFinder> updateFunction) {
        this.finders = new ArrayList<>(finderList);
        this.enabledFinders = new ArrayList<>();
        for (int i = 0; i < finders.size(); i++)
            enabledFinders.add(true);

        this.updateFunction = updateFunction;
    }

    public void enableFinder(int index) {
        enabledFinders.set(index, true);
    }

    public void disableFinder(int index) {
        enabledFinders.set(index, false);
    }

    public List<FeatureFinder> getFinders() {
        return this.finders;
    }

    // ------------------------------------------------------------------------------------------------------------

    @Override
    public void run() {
        for (int i = 0; i < finders.size(); i++) {
            if (enabledFinders.get(i)) {
                finders.get(i).setWorldSeed(worldseed);
                finders.get(i).run();
                updateFunction.accept(finders.get(i));
            }
        }
        //updateFunction.accept(this);
    }
}
