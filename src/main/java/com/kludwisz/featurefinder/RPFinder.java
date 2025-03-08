package com.kludwisz.featurefinder;

import com.kludwisz.Logger;
import com.kludwisz.populationfinder.PopulationSeedChunkFinder;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.RuinedPortal;
import com.seedfinding.mcfeature.structure.generator.structure.RuinedPortalGenerator;
import com.seedfinding.mcterrain.TerrainGenerator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class RPFinder implements FeatureFinder {
    private String resource;
    private String type;
    private long worldseed;
    private String tpCommand;

    @Override
    public void setWorldSeed(long worldseed) {
        this.worldseed = worldseed;
    }

    @Override
    public String getFeatureTPCommand() {
        return tpCommand;
    }

    @Override
    public String getFeedbackMessage() {
        if (tpCommand == null)
            return "not found.";
        return type.equals(" (notches)") ? "4+ notches" : "4+ looting III swords";
    }

    @Override
    public String name() {
        return "RPFinder" + type;
    }

    // ------------------------------------------------------------------------------------------------------------

    public RPFinder() {
        this.resource = "notchRP.txt";
        this.type = " (notches)";
    }

    public RPFinder lootingSwords() {
        this.resource = "multiLootingRP.txt";
        this.type = " (looting)";
        return this;
    }

    // ------------------------------------------------------------------------------------------------------------

    @Override
    public void run() {
        tpCommand = null;

        InputStream SEEDLIST_INPUT_STREAM = Objects.requireNonNull(ObbyFinder.class.getClassLoader().getResourceAsStream(resource));
        try (Scanner fin = new Scanner(SEEDLIST_INPUT_STREAM)) {
            ArrayList<Long> popseeds = new ArrayList<>();

            while (fin.hasNextLong()) {
                long popseed = fin.nextLong();
                fin.nextInt();
                popseeds.add(popseed);
            }

            PopulationSeedChunkFinder finder = new PopulationSeedChunkFinder(worldseed, popseeds);
            List<CPos> chunkPositions = finder.findChunks();
            RuinedPortal portal = new RuinedPortal(Dimension.NETHER, MCVersion.v1_16_1);
            ChunkRand rand = new ChunkRand();

            for (CPos chunkPos : chunkPositions) {
                // check if ruined portal can spawn here
                RPos owRegion = chunkPos.toRegionPos(portal.getSpacing());
                if (portal.getInRegion(worldseed, owRegion.getX(), owRegion.getZ(), rand).equals(chunkPos)) {
                    BPos chestPos = processGoodPortal(chunkPos);
                    if (chestPos == null) continue;

                    tpCommand = "/execute in minecraft:the_nether run tp @s " + chestPos.getX() + " " + chestPos.getY() + " " + chestPos.getZ() + " ";
                    return;
                }
            }
        }
    }

    private BPos processGoodPortal(CPos chunkPos) {
        BiomeSource nbs = BiomeSource.of(Dimension.NETHER, MCVersion.v1_16_1, worldseed);
        TerrainGenerator ntg = TerrainGenerator.of(nbs);
        RuinedPortalGenerator rpgen = new RuinedPortalGenerator(MCVersion.v1_16_1);
        rpgen.generate(ntg, chunkPos);
        if (rpgen.getChestsPos().isEmpty())
            return null;

        return rpgen.getChestsPos().get(0).getSecond();
    }
}
