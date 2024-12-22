package com.kludwisz.featurefinder;

import Xinyuiii.properties.BastionGenerator;
import com.kludwisz.populationfinder.PopulationSeedChunkFinder;
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
import com.seedfinding.mcfeature.loot.item.Items;
import com.seedfinding.mcfeature.structure.BastionRemnant;
import com.seedfinding.mcseed.lcg.LCG;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class ObbyFinder {
    private final InputStream SEEDLIST_INPUT_STREAM = Objects.requireNonNull(ObbyFinder.class.getClassLoader().getResourceAsStream("bastionObby.txt"));

    private final long worldseed;
    private int obbyCount = 0;

    public ObbyFinder(long worldseed) {
        this.worldseed = worldseed;
    }

    public String getFeatureTPCommand() {
        // load seedlist into memory
        try (Scanner fin = new Scanner(SEEDLIST_INPUT_STREAM)) {
            ArrayList<Long> popseeds = new ArrayList<>();
            ChunkRand rand = new ChunkRand();

            while (fin.hasNextLong()) {
                long internalSeed = fin.nextLong();
                rand.setSeed(internalSeed, false);
                fin.nextInt();

                // add all the possible offsets: 0, 1, 2.
                for (int offset = 0; offset <= 2; offset++) {
                    long decoratorSeed = rand.getSeed() ^ LCG.JAVA.multiplier;
                    long popseed = decoratorSeed - 40012; // 40012 is bastion salt
                    rand.advance(-2); // each offset is one nextLong call
                    popseeds.add(popseed);
                }
            }

            PopulationSeedChunkFinder finder = new PopulationSeedChunkFinder(worldseed, popseeds);
            List<CPos> chunkPositions = finder.findChunks();
            BastionRemnant bastion = new BastionRemnant(MCVersion.v1_16_1);
            BiomeSource nbs = BiomeSource.of(Dimension.NETHER, MCVersion.v1_16_1, worldseed);

            for (CPos chunkPos : chunkPositions) {
                RPos region = chunkPos.toRegionPos(bastion.getSpacing());
                CPos pos = bastion.getInRegion(worldseed, region.getX(), region.getZ(), rand);
                if (pos != null && pos.distanceTo(chunkPos, DistanceMetric.CHEBYSHEV) <= 4) {
                    // try and generate the bastion here,
                    // check if loot in any pair of chests is correct
                    BastionGenerator gen = new BastionGenerator(MCVersion.v1_16_1);
                    gen.generate(worldseed, pos);

                    List<Pair<BPos, List<ItemStack>>> loot = gen.generateLoot();

                    for (Pair<BPos, List<ItemStack>> pair1 : loot) {
                        for (Pair<BPos, List<ItemStack>> pair2 : loot) {
                            if (pair1.getFirst().distanceTo(pair2.getFirst(), DistanceMetric.MANHATTAN) == 1) {
                                // found a double chest, check total obby
                                int obbyTotal = 0;
                                for (ItemStack is : pair1.getSecond())
                                    if (is.getItem().equalsName(Items.OBSIDIAN))
                                        obbyTotal += is.getCount();
                                for (ItemStack is : pair2.getSecond())
                                    if (is.getItem().equalsName(Items.OBSIDIAN))
                                        obbyTotal += is.getCount();

                                if (obbyTotal >= 46) {
                                    if (!bastion.canSpawn(pos, nbs)) continue;

                                    obbyCount = obbyTotal;
                                    return "/execute in minecraft:the_nether run tp @s " + pair1.getFirst().getX() + " " + pair1.getFirst().getY() + " " + pair1.getFirst().getZ() + " ";
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public String getFeedbackMessage() {
        return "Found double chest with " + obbyCount  + " obsidian.";
    }
}
