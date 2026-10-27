/*
 * PercentileHistogram.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.statistic;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Streaming percentile estimator based on a log-scale histogram with a guaranteed relative precision.
 *
 * <p>Each power-of-two range [2^e, 2^(e+1)) is split into {@value #SUB_BUCKET_COUNT} equal sub-buckets,
 * values below {@code 2 * SUB_BUCKET_COUNT} are counted exactly. A percentile is reported as the middle
 * of the bucket that contains the sample of the requested rank (nearest-rank method), clamped to the exact
 * minimum and maximum. The lowest rank (e.g. P0) is reported as the exact minimum and the highest rank (P100) as
 * the exact maximum. The reported value therefore differs from the exact percentile by at most
 * half a bucket width, i.e. less than 0.1% of the value; a constant series is reported exactly.
 * The rank of a quantile is calculated from its decimal value, e.g. P7 of 100 values is the 7th value.</p>
 *
 * <p>Memory is controlled by the number of ranges: range 0 costs 16 KB; each additional range costs 8 KB.
 * The default (~304 KB) covers values up to {@link #MAX_VALUE}.
 * Use {@link #PercentileHistogram(long)} to cap memory by passing the maximum value you expect to measure;
 * values that exceed the configured maximum are clamped to the last bucket rather than rejected. Such values are
 * reported with the precision of the last bucket, except the maximum (P100) which is always reported exactly.</p>
 *
 * <p>The individual methods are protected by a {@link ReadWriteLock}: concurrent reads run in
 * parallel; writes ({@link #add} and {@link #clear}) are exclusive. Atomicity across multiple calls
 * must still be synchronized externally.</p>
 *
 * @author patrick
 */
public class PercentileHistogram implements Cloneable, Serializable {
    /** The number of sub-buckets per power-of-two range, defines the relative precision (1/1024). */
    public static final int SUB_BUCKET_COUNT = 1024;

    /** The maximum value covered at full precision by the default configuration (2^46, 64 TB). */
    public static final long MAX_VALUE = 1L << 46;

    /** The number of ranges used by the default constructor. */
    public static final int DEFAULT_RANGE_COUNT = 37;

    private static final long serialVersionUID = -4158902551244137440L;
    private static final int SUB_BUCKET_BITS = 10;
    private static final int EXACT_LIMIT = 2 * SUB_BUCKET_COUNT;
    private static final Object TIE_LOCK = new Object();

    private transient ReadWriteLock lock = new ReentrantReadWriteLock();
    private final int rangeCount;
    private long[][] counts;
    private long totalCount;
    private long min;
    private long max;


    /**
     * Constructor. Uses {@value #DEFAULT_RANGE_COUNT} ranges (~304 KB max), covering values up to
     * {@link #MAX_VALUE} at full precision.
     */
    public PercentileHistogram() {
        this(MAX_VALUE);
    }


    /**
     * Constructor that caps memory to the range needed to cover {@code maxValue} precisely.
     * Values above {@code maxValue} are clamped to the last bucket rather than rejected.
     * Total max memory is {@code 16 + (rangesFor(maxValue) - 1) * 8} KB.
     *
     * <p>Example: {@code new PercentileHistogram(16L * 1024 * 1024 * 1024)} covers up to 16 GB.</p>
     *
     * @param maxValue the maximum value to cover at full precision, must be non-negative
     * @throws IllegalArgumentException if maxValue is negative or requires more ranges than {@value #DEFAULT_RANGE_COUNT}
     */
    public PercentileHistogram(long maxValue) {
        int ranges = rangesFor(maxValue);
        if (ranges > DEFAULT_RANGE_COUNT) {
            throw new IllegalArgumentException("maxValue requires more than " + DEFAULT_RANGE_COUNT + " ranges: " + maxValue);
        }
        this.rangeCount = ranges;
        this.counts = new long[rangeCount][];
        clear();
    }


    /**
     * Clears all recorded values.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            Arrays.fill(counts, null);
            totalCount = 0;
            min = Long.MAX_VALUE;
            max = Long.MIN_VALUE;
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Records a value. Values that exceed the configured range are clamped to the last bucket.
     *
     * @param value the value, must not be negative
     * @throws IllegalArgumentException if the value is negative
     */
    public void add(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must not be negative: " + value);
        }

        lock.writeLock().lock();
        try {
            int range = rangeIndex(value);
            int sub;
            if (range >= rangeCount) {
                // value exceeds the configured range: clamp to the last bucket
                range = rangeCount - 1;
                if (range == 0) {
                    sub = EXACT_LIMIT - 1;
                } else {
                    sub = SUB_BUCKET_COUNT - 1;
                }
            } else {
                sub = subBucketIndex(value, range);
            }

            if (counts[range] == null) {
                int size;
                if (range == 0) {
                    size = EXACT_LIMIT;
                } else {
                    size = SUB_BUCKET_COUNT;
                }
                counts[range] = new long[size];
            }
            counts[range][sub]++;

            totalCount++;
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Gets the number of recorded values.
     *
     * @return the number of recorded values
     */
    public long getCount() {
        lock.readLock().lock();
        try {
            return totalCount;
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Returns the number of ranges required to cover {@code maxValue} at full precision.
     * Use this to compute the memory footprint for a given maximum value up front, e.g. for 16 GB:
     * <pre>int footprintKB = 16 + (PercentileHistogram.rangesFor(16L * 1024 * 1024 * 1024) - 1) * 8;</pre>
     * Note that the constructor {@link #PercentileHistogram(long)} expects the maximum value itself, not the range count:
     * <pre>new PercentileHistogram(16L * 1024 * 1024 * 1024)</pre>
     *
     * @param maxValue the maximum value to cover, must not be negative
     * @return the required number of ranges
     * @throws IllegalArgumentException if maxValue is negative
     */
    public static int rangesFor(long maxValue) {
        if (maxValue < 0) {
            throw new IllegalArgumentException("maxValue must not be negative: " + maxValue);
        }
        if (maxValue < EXACT_LIMIT) {
            return 1;
        }
        int exponent = 63 - Long.numberOfLeadingZeros(maxValue);
        return exponent - SUB_BUCKET_BITS + 1;
    }


    /**
     * Gets the memory footprint of this histogram in KB.
     * Equals {@code 16 + (rangeCount - 1) * 8} KB.
     *
     * @return the memory footprint in KB
     */
    public int getMemoryFootprintKB() {
        return 16 + (rangeCount - 1) * 8;
    }


    /**
     * Gets the estimated value at the given quantile.
     *
     * @param quantile the quantile between 0 and 1, e.g. 0.95 for P95
     * @return the estimated value, 0 if no value was recorded
     * @throws IllegalArgumentException if the quantile is not between 0 and 1
     */
    public long getValueAtQuantile(double quantile) {
        return getValuesAtQuantiles(quantile)[0];
    }


    /**
     * Gets the estimated values at several quantiles in a single pass over the histogram.
     *
     * @param quantiles the quantiles between 0 and 1 in ascending order, e.g. 0.5, 0.95, 0.99
     * @return the estimated values in the order of the quantiles, all 0 if no value was recorded
     * @throws IllegalArgumentException if a quantile is not between 0 and 1 or the quantiles are not ascending
     */
    public long[] getValuesAtQuantiles(double... quantiles) {
        // validate before acquiring the lock: no shared state is read here
        for (int i = 0; i < quantiles.length; i++) {
            double q = quantiles[i];
            if (Double.isNaN(q) || q < 0 || q > 1) {
                throw new IllegalArgumentException("Quantile must be between 0 and 1: " + q);
            }
            if (i > 0 && q < quantiles[i - 1]) {
                throw new IllegalArgumentException("Quantiles must be in ascending order.");
            }
        }

        long[] result = new long[quantiles.length];
        lock.readLock().lock();
        try {
            if (totalCount == 0 || quantiles.length == 0) {
                return result;
            }

            // compute ranks inside the lock so they are consistent with the counts being scanned
            long[] ranks = new long[quantiles.length];
            for (int i = 0; i < quantiles.length; i++) {
                ranks[i] = rank(quantiles[i], totalCount);
            }

            int next = 0;
            long cumulative = 0;
            for (int range = 0; range < rangeCount && next < ranks.length; range++) {
                long[] bucket = counts[range];
                if (bucket == null) {
                    continue;
                }

                for (int sub = 0; sub < bucket.length && next < ranks.length; sub++) {
                    cumulative += bucket[sub];
                    while (next < ranks.length && cumulative >= ranks[next]) {
                        if (ranks[next] >= totalCount) {
                            // the highest rank is the exact maximum, also if it exceeds the configured range
                            result[next] = max;
                        } else if (ranks[next] <= 1) {
                            // the lowest rank is the exact minimum
                            result[next] = min;
                        } else {
                            result[next] = clamp(representativeValue(range, sub));
                        }
                        next++;
                    }
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        lock.readLock().lock();
        try {
            final int prime = 31;
            int result = Arrays.deepHashCode(counts);
            result = prime * result + Long.hashCode(totalCount);
            result = prime * result + Long.hashCode(min);
            result = prime * result + Long.hashCode(max);
            result = prime * result + Integer.hashCode(rangeCount);
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        PercentileHistogram other = (PercentileHistogram) obj;
        if (rangeCount != other.rangeCount) {
            return false;
        }

        // both read locks are held while comparing; they are always acquired in the same global order
        // (identity hash code) to avoid a deadlock between a.equals(b) and b.equals(a) with queued writers
        int thisHash = System.identityHashCode(this);
        int otherHash = System.identityHashCode(other);
        if (thisHash < otherHash) {
            return equalsUnderLock(this, other);
        } else if (thisHash > otherHash) {
            return equalsUnderLock(other, this);
        } else {
            // rare hash collision: serialize such comparisons with a global tie lock
            synchronized (TIE_LOCK) {
                return equalsUnderLock(this, other);
            }
        }
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            long allocatedRanges = Arrays.stream(counts).filter(c -> c != null).count();
            long minValue = 0;
            long maxRecorded = 0;
            if (totalCount > 0) {
                minValue = min;
                maxRecorded = max;
            }
            return "PercentileHistogram [count=" + totalCount + ", min=" + minValue + ", max=" + maxRecorded
                    + ", ranges=" + allocatedRanges + "/" + rangeCount + ", footprint=" + getMemoryFootprintKB() + " KB]";
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Creates a deep-copy snapshot of this histogram with the same runtime class (also of a subclass). The clone
     * has its own independent lock and counts array; subsequent writes to either instance do not affect the other.
     * The fields of a subclass are copied shallow, a subclass with mutable state has to override this method to copy it.
     *
     * @return a deep copy of this histogram
     * @see java.lang.Object#clone()
     */
    @Override
    public PercentileHistogram clone() {
        lock.readLock().lock();
        try {
            PercentileHistogram inst = (PercentileHistogram) super.clone();
            inst.lock = new ReentrantReadWriteLock();
            inst.counts = new long[rangeCount][];
            for (int i = 0; i < rangeCount; i++) {
                if (counts[i] != null) {
                    inst.counts[i] = Arrays.copyOf(counts[i], counts[i].length);
                }
            }
            return inst;
        } catch (CloneNotSupportedException e) {
            // can not happen, the class implements Cloneable
            throw new IllegalStateException(e);
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Compares the state of two histograms with the same range count while holding the read lock of both.
     * The locks are acquired in the given order.
     *
     * @param first the histogram which is locked first
     * @param second the histogram which is locked second
     * @return true if both histograms contain the same values
     */
    private static boolean equalsUnderLock(PercentileHistogram first, PercentileHistogram second) {
        first.lock.readLock().lock();
        try {
            second.lock.readLock().lock();
            try {
                return first.totalCount == second.totalCount
                        && first.min == second.min
                        && first.max == second.max
                        && Arrays.deepEquals(first.counts, second.counts);
            } finally {
                second.lock.readLock().unlock();
            }
        } finally {
            first.lock.readLock().unlock();
        }
    }


    /**
     * Writes the state under the read lock, so that a consistent snapshot is serialized.
     *
     * @param out the object output stream
     * @throws IOException if an I/O error occurs
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        lock.readLock().lock();
        try {
            out.defaultWriteObject();
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Restores the transient lock after deserialization and validates the invariants of the read state.
     *
     * @param in the object input stream
     * @throws IOException if an I/O error occurs
     * @throws InvalidObjectException if the read state is corrupt
     * @throws ClassNotFoundException if the class cannot be found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        validateState();
        lock = new ReentrantReadWriteLock();
    }


    /**
     * Validates the invariants of the state, used after deserialization.
     *
     * @throws InvalidObjectException if the state is corrupt
     */
    private void validateState() throws InvalidObjectException {
        if (rangeCount < 1 || rangeCount > DEFAULT_RANGE_COUNT) {
            throw new InvalidObjectException("Invalid range count: " + rangeCount);
        }
        if (counts == null || counts.length != rangeCount) {
            throw new InvalidObjectException("Invalid number of ranges.");
        }

        long sum = 0;
        for (int range = 0; range < rangeCount; range++) {
            long[] bucket = counts[range];
            if (bucket == null) {
                continue;
            }

            int expectedSize = SUB_BUCKET_COUNT;
            if (range == 0) {
                expectedSize = EXACT_LIMIT;
            }
            if (bucket.length != expectedSize) {
                throw new InvalidObjectException("Invalid bucket size in range " + range + ": " + bucket.length);
            }

            for (long count : bucket) {
                if (count < 0) {
                    throw new InvalidObjectException("Negative bucket count in range " + range + ".");
                }
                try {
                    sum = Math.addExact(sum, count);
                } catch (ArithmeticException e) {
                    throw new InvalidObjectException("Bucket counts overflow.", e);
                }
            }
        }

        if (sum != totalCount) {
            throw new InvalidObjectException("Total count " + totalCount + " does not match the bucket counts " + sum + ".");
        }

        if (totalCount == 0) {
            if (min != Long.MAX_VALUE || max != Long.MIN_VALUE) {
                throw new InvalidObjectException("Invalid min / max of an empty histogram.");
            }
        } else if (min < 0 || min > max) {
            throw new InvalidObjectException("Invalid min / max: " + min + " / " + max);
        }
    }


    /**
     * Calculates the rank of a quantile with the nearest-rank method. The quantile is used with its decimal value,
     * so that e.g. the quantile 0.07 of 100 values is the rank 7 and not 8 because of the binary floating point value.
     *
     * @param quantile the quantile between 0 and 1
     * @param count the number of values, must be positive
     * @return the rank between 1 and count
     */
    static long rank(double quantile, long count) {
        long rank = BigDecimal.valueOf(quantile).multiply(BigDecimal.valueOf(count)).setScale(0, RoundingMode.CEILING).longValue();
        return Math.min(count, Math.max(1L, rank));
    }


    /**
     * Clamps a value to the recorded minimum and maximum.
     *
     * @param value the value
     * @return the clamped value
     */
    private long clamp(long value) {
        return Math.min(max, Math.max(min, value));
    }


    /**
     * Gets the index of the power-of-two range of a value; range 0 holds the exactly counted values.
     *
     * @param value the value
     * @return the range index
     */
    private static int rangeIndex(long value) {
        if (value < EXACT_LIMIT) {
            return 0;
        }
        int exponent = 63 - Long.numberOfLeadingZeros(value);
        return exponent - SUB_BUCKET_BITS;
    }


    /**
     * Gets the sub-bucket index of a value within its range.
     *
     * @param value the value
     * @param range the range index
     * @return the sub-bucket index
     */
    private static int subBucketIndex(long value, int range) {
        if (range == 0) {
            return (int) value;
        }
        return (int) (value >>> range) - SUB_BUCKET_COUNT;
    }


    /**
     * Gets the representative value (middle) of a bucket.
     *
     * @param range the range index
     * @param sub the sub-bucket index
     * @return the representative value
     */
    private static long representativeValue(int range, int sub) {
        if (range == 0) {
            return sub;
        }
        long lower = ((long) sub + SUB_BUCKET_COUNT) << range;
        long width = 1L << range;
        return lower + width / 2;
    }
}
