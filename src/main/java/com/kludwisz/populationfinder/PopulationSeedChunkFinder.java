package com.kludwisz.populationfinder;

import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcmath.util.Mth;
import com.seedfinding.mcseed.rand.JRand;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

public class PopulationSeedChunkFinder {
    private static final int NUM_STEPS = 2048;
    private static final long MAX_COORDINATE = 1874999L;
    private static final long LOWER_BOUND = (1L << 44) - MAX_COORDINATE;
    private static final long UPPER_BOUND = MAX_COORDINATE;
    private static final long MASK_48 = (1L << 48) - 1;
    private static final long MASK_44 = (1L << 44) - 1;

    private final long worldSeed;
    private final List<Long> populationSeeds;

    public PopulationSeedChunkFinder(long worldSeed, List<Long> populationSeeds) {
        this.worldSeed = worldSeed;
        this.populationSeeds = populationSeeds;
    }

    public List<CPos> findChunks() {
        List<CPos> result = new ArrayList<>();

        //    we have the congruence
        // Ax + Bz = S (mod 2^44)
        // Ax = -Bz + S (mod 2^44)
        // x = -inv(A)Bz + inv(A)S (mod 2^44)
        //    let -inv(A)B = P, inv(A)S = Q
        // x = Pz + Q (mod 2^44)

        long invA = Mth.modInverse(this.getPopSeedA(), 44);
        long minusB = (1L << 44) - getPopSeedB();
        final long P = (invA * minusB) & MASK_44;

        IntervalLookupSet lookupSet = new IntervalLookupSet(P, MAX_COORDINATE);
        lookupSet.createCoveringSet(NUM_STEPS); // R(n)

        final long longSkip = (P * NUM_STEPS) & MASK_44;
        final long initialX = (P * -MAX_COORDINATE) & MASK_44;

        for (long populationSeed : this.populationSeeds) {
            long S = (populationSeed ^ this.worldSeed) & MASK_48;
            if ((S & 15) != 0)
                continue; // congruence can't be satisfied
            S >>= 4;

            long z = -MAX_COORDINATE;
            long x = (initialX + invA * S) & MASK_44; // does not have the +Q !!!

            while (z <= MAX_COORDINATE) {
                if (lookupSet.contains(x)) {
                    // there is valid chunk within the next NUM_STEPS steps
                    for (int i = 0; i < NUM_STEPS; i++) {
                        if (x <= UPPER_BOUND || x >= LOWER_BOUND) {
                            result.add(new CPos((int)x, (int)z));
                        }

                        // go to the next (x,z) pair
                        x += P; x &= MASK_44;
                        z++;
                    }
                }
                else {
                    // skip NUM_STEPS steps
                    x += longSkip; x &= MASK_44;
                    z += NUM_STEPS;
                }
            }
        }


        return result;
    }

    // ---------------------------------------------------

    private long getPopSeedA() {
        JRand rand = new JRand(0L);
        rand.setSeed(this.worldSeed);

        long a = (rand.nextLong() | 1L);
        a &= MASK_44; // upper 4 bits get discarded

        return a;
    }

    private long getPopSeedB() {
        JRand rand = new JRand(0L);
        rand.setSeed(this.worldSeed);

        rand.nextLong(); // a
        long b = (rand.nextLong() | 1L);
        b &= MASK_44; // upper 4 bits get discarded

        return b;
    }

    // ---------------------------------------------------

    public static void timeTest() {
        long worldseed = 1230000L;
        List<Long> popseeds = LongStream.range(0, 16 * 100000).boxed().toList();

        long start = System.nanoTime();
        PopulationSeedChunkFinder finder = new PopulationSeedChunkFinder(worldseed, popseeds);
        List<CPos> positions = finder.findChunks();
        long end = System.nanoTime();

        System.out.println("Execution time: " + (end - start) / 1_000_000.0D + " ms");
        System.out.println("Found " + positions.size() + " chunks");
    }

    public static void main(String[] args) {
        timeTest();
    }
}
