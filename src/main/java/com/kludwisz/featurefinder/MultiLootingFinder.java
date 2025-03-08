package com.kludwisz.featurefinder;

import com.kludwisz.populationfinder.PopulationSeedChunkFinder;
import Xinyuiii.enumType.BastionType;
import Xinyuiii.properties.BastionGenerator;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import com.seedfinding.mcfeature.structure.BastionRemnant;
import com.seedfinding.mcseed.lcg.LCG;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class MultiLootingFinder implements FeatureFinder {
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
        return tpCommand == null ? "not found." : "10+ looting levels total";
    }

    @Override
    public String name() {
        return "Looting Treasure Finder";
    }


    // ------------------------------------------------------------------------------------------------------------

    @Override
    public void run() {
        tpCommand = null;

        InputStream SEEDLIST_INPUT_STREAM = Objects.requireNonNull(ObbyFinder.class.getClassLoader().getResourceAsStream("bastionObby.txt"));
        try (Scanner fin = new Scanner(SEEDLIST_INPUT_STREAM)) {
            ArrayList<Long> popseeds = new ArrayList<>();
            ChunkRand rand = new ChunkRand();

            while (fin.hasNextLong()) {
                long internalSeed = fin.nextLong();
                rand.setSeed(internalSeed, false);
                fin.nextInt();

                long decoratorSeed = rand.getSeed() ^ LCG.JAVA.multiplier;
                long popseed = decoratorSeed - 40012; // 40012 is bastion salt
                popseeds.add(popseed);
            }

            PopulationSeedChunkFinder finder = new PopulationSeedChunkFinder(worldseed, popseeds);
            List<CPos> chunkPositions = finder.findChunks();
            BastionRemnant bastion = new BastionRemnant(MCVersion.v1_16_1);
            BiomeSource nbs = BiomeSource.of(Dimension.NETHER, MCVersion.v1_16_1, worldseed);

            for (CPos chunkPos : chunkPositions) {
                RPos region = chunkPos.toRegionPos(bastion.getSpacing());
                String cmd = processRegion(chunkPos, region, bastion, nbs, rand);
                if (cmd == null) continue;

                tpCommand = cmd;
                return;
            }
        }
    }

    private String processRegion(CPos chunkPos, RPos region, BastionRemnant bastion, BiomeSource nbs, ChunkRand rand) {
        for (int drx = -1; drx <= 1; drx++) {
            for (int drz = -1; drz <= 1; drz++) {
                CPos pos = bastion.getInRegion(worldseed, region.getX(), region.getZ(), rand);
                if (pos != null && pos.distanceTo(chunkPos, DistanceMetric.CHEBYSHEV) <= 4) {
                    // try and generate the bastion here,
                    // check if chest placement is correct
                    BastionGenerator gen = new BastionGenerator(MCVersion.v1_16_1);
                    gen.generate(worldseed, pos);
                    if (!gen.getType().equals(BastionType.TREASURE)) continue;

                    List<Pair<BPos, List<ItemStack>>> loot = gen.generateLoot();

                    BPos target = null;
                    int goodChests = 0;

                    for (Pair<BPos, List<ItemStack>> pair1 : loot) {
                        if (pair1.getFirst().getY() == 36 && pair1.getFirst().toChunkPos().distanceTo(chunkPos, DistanceMetric.CHEBYSHEV) == 0) {
                            target = pair1.getFirst();
                            goodChests++;
                        }
                    }

                    if (goodChests == 2) {
                        if (!bastion.canSpawn(pos, nbs)) continue;
                        return "/execute in minecraft:the_nether run tp @s " + target.getX() + " " + target.getY() + " " + target.getZ() + " ";
                    }
                }
            }
        }

        return null;
    }
}
