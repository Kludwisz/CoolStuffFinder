package com.kludwisz.populationfinder;

import java.util.ArrayList;
import java.util.Comparator;

public class IntervalLookupSet {
    // algorithm constants
    private static final long NUM_BUCKETS = 512;
    private static final long BUCKET_SIZE = (1L << 44) / NUM_BUCKETS;
    private static final long MOD = 1L << 44;
    private static final long MASK = MOD - 1;

    // ---------------------------------------------------

    private final long stepSize;
    private final long intervalRadius;

    private final ArrayList<Interval>[] intervalBuckets = new ArrayList[(int) NUM_BUCKETS];

    public IntervalLookupSet(long stepSize, long intervalRadius) {
        this.stepSize = MOD - stepSize; // we're going back
        this.intervalRadius = intervalRadius;
    }

    public boolean contains(long value) {
        long bucket = value / BUCKET_SIZE;

        // binary search over the intervals inside the bucket
        ArrayList<Interval> intervals = intervalBuckets[(int) bucket];
        if (intervals.isEmpty())
            return false;

        int left = 0;
        int right = intervals.size() - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            Interval interval = intervals.get(mid);

            if (interval.start <= value && value <= interval.end) {
//                System.out.println("Found in bucket " + bucket);
//                System.out.println("Interval: [" + interval.start + " - " + interval.end + "], value " + value);
                return true;
            }
            else if (interval.start > value)
                right = mid - 1;
            else
                left = mid + 1;
        }

        return false;
    }

    // ---------------------------------------------------

    public void createCoveringSet(int numSteps) {
        for (int i = 0; i < NUM_BUCKETS; i++) {
            intervalBuckets[i] = new ArrayList<>();
        }

        long currentStart = (-intervalRadius + MOD) & MASK;
        long currentEnd = intervalRadius & MASK;

        for (int i = 0; i < numSteps; i++) {
            this.addInterval(currentStart, currentEnd);
            currentStart = (currentStart + stepSize) & MASK;
            currentEnd = (currentEnd + stepSize) & MASK;
        }

        this.mergeIntervals();
    }

    private void addInterval(long start, long end) {
        int bucketStart = (int)(start / BUCKET_SIZE);
        int bucketEnd = (int)(end / BUCKET_SIZE);

        // each interval will span at most two buckets
        if (bucketStart == bucketEnd) {
            intervalBuckets[bucketStart].add(new Interval(start, end));
        }
        else if (bucketStart < bucketEnd) {
            // no modulo wrapping, buckets are in a natural order
            // System.out.println("Split intervals in " + bucketStart + " - " + bucketEnd);
            intervalBuckets[bucketStart].add(new Interval(start, end));
            intervalBuckets[bucketEnd].add(new Interval(start, end));
        }
        else {
            // mod wrapping, need to adjust the intervals a bit
            // System.out.println("Mod wrapping in " + bucketStart + " - " + bucketEnd);
            intervalBuckets[bucketEnd].add(new Interval(-1, end));
            intervalBuckets[bucketStart].add(new Interval(start, MOD));
        }
    }

    private void mergeIntervals() {
        for (int b = 0; b < NUM_BUCKETS; b++) {
            ArrayList<Interval> intervals = intervalBuckets[b];
            if (intervals.size() <= 1)
                continue; // no need to merge in empty or single-element interval lists

            intervals.sort(Comparator.comparingLong(a -> a.start));

            ArrayList<Interval> mergedIntervals = new ArrayList<>();
            Interval lastInterval = null;

            for (Interval interval : intervals) {
                if (lastInterval == null) {
                    lastInterval = interval;
                    continue;
                }

                if (lastInterval.end >= interval.start) {
                    lastInterval.end = Math.max(lastInterval.end, interval.end);
                }
                else {
                    mergedIntervals.add(lastInterval);
                    lastInterval = interval;
                }
            }

            if (lastInterval != null)
                mergedIntervals.add(lastInterval);

            intervalBuckets[b] = mergedIntervals;

            // debug
            // System.out.println("Bucket " + b + " has " + mergedIntervals.size() + " intervals");
        }
    }
}
