/*
 * StatisticCounter.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.statistic;

import com.github.toolarium.common.util.TextUtil;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * This class acts as a helper class which can be used to compute several
 * simple statistics for a set of numbers.
 *
 * <p>The average and the variance are computed with Welford's online algorithm
 * (and Chan's algorithm when merging counters), which is numerically stable also
 * for large values with a small variance. Negative values are supported.</p>
 *
 * <p>The individual methods are protected by a {@link ReadWriteLock}: concurrent reads run in
 * parallel; writes ({@link #add} and {@link #clear}) are exclusive. Atomicity across multiple calls
 * must still be synchronized externally, or each thread uses its own instance and the instances
 * are merged with {@link #add(StatisticCounter)}.</p>
 *
 * @author patrick
 */
public class StatisticCounter implements IStatisticCounter<StatisticCounter>, Cloneable, Serializable {
    private static final long serialVersionUID = 7610753026869713980L;
    private transient ReadWriteLock lock = new ReentrantReadWriteLock();
    private long counter;
    private double min;
    private double max;
    private double sum;
    private double squareSum;
    private double mean;
    private double m2;


    /**
     * Constructor
     */
    public StatisticCounter() {
        clear();
    }


    /**
     * Constructor
     *
     * @param counter the counter
     * @param min the min value
     * @param max the max value
     * @param sum the sum value
     * @param squareSum the square sum value
     */
    public StatisticCounter(long counter, double min, double max, double sum, double squareSum) {
        clear();
        if (counter <= 0) {
            return;
        }
        this.counter = counter;
        this.min = min;
        this.max = max;
        this.sum = sum;
        this.squareSum = squareSum;
        rebuildMeanAndM2();
    }


    /**
     * Clear and reset internal attributes
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            counter = 0;
            min = Double.POSITIVE_INFINITY;
            max = Double.NEGATIVE_INFINITY;
            sum = 0.0;
            squareSum = 0.0;
            mean = 0.0;
            m2 = 0.0;
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Adds a long value
     *
     * @param num the data to add
     */
    public void add(long num) {
        add((double) num);
    }


    /**
     * Adds a double value. A value which is not finite (NaN or infinite) is ignored, it would make the statistic
     * values invalid.
     *
     * @param num the number to add
     */
    public void add(double num) {
        if (!Double.isFinite(num)) {
            return;
        }

        lock.writeLock().lock();
        try {
            counter++;
            sum += num;
            squareSum += num * num;

            double delta = num - mean;
            mean += delta / counter;
            m2 += delta * (num - mean);

            if (num > max) {
                max = num;
            }

            if (num < min) {
                min = num;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * @see com.github.toolarium.common.statistic.IStatisticCounter#add(java.lang.Object)
     */
    @Override
    public void add(StatisticCounter statisticCounter) {
        if (statisticCounter == null) {
            return;
        }

        // take a consistent snapshot of the argument under its read lock
        long otherCounter;
        double otherMin;
        double otherMax;
        double otherSum;
        double otherSquareSum;
        double otherMean;
        double otherM2;
        statisticCounter.lock.readLock().lock();
        try {
            otherCounter = statisticCounter.counter;
            otherMin = statisticCounter.min;
            otherMax = statisticCounter.max;
            otherSum = statisticCounter.sum;
            otherSquareSum = statisticCounter.squareSum;
            otherMean = statisticCounter.mean;
            otherM2 = statisticCounter.m2;
        } finally {
            statisticCounter.lock.readLock().unlock();
        }

        if (otherCounter <= 0) {
            return;
        }

        lock.writeLock().lock();
        try {
            if (counter <= 0) {
                counter = otherCounter;
                min = otherMin;
                max = otherMax;
                sum = otherSum;
                squareSum = otherSquareSum;
                mean = otherMean;
                m2 = otherM2;
                return;
            }

            min = Math.min(min, otherMin);
            max = Math.max(max, otherMax);

            long total = counter + otherCounter;
            double delta = otherMean - mean;
            mean += delta * otherCounter / total;
            m2 += otherM2 + delta * delta * ((double) counter * otherCounter / total);

            counter = total;
            sum += otherSum;
            squareSum += otherSquareSum;
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Gets the counter
     *
     * @return the counter
     */
    public long getCounter() {
        lock.readLock().lock();
        try {
            return counter;
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Gets the min value back
     *
     * @return the min value, 0 if no value was added
     */
    public double getMinValue() {
        lock.readLock().lock();
        try {
            if (counter <= 0) {
                return 0;
            }
            return min;
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Gets the max value back
     *
     * @return the max value, 0 if no value was added
     */
    public double getMaxValue() {
        lock.readLock().lock();
        try {
            if (counter <= 0) {
                return 0;
            }
            return max;
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Gets the range (German: Spannweite)
     *
     * @return the range
     */
    public double getRange() {
        lock.readLock().lock();
        try {
            return getMaxValue() - getMinValue();
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Gets the total sum
     *
     * @return the sum
     */
    public double getSum() {
        lock.readLock().lock();
        try {
            return sum;
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Gets the square sum
     *
     * @return the square sum
     */
    public double getSquareSum() {
        lock.readLock().lock();
        try {
            return squareSum;
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Gets the total average
     *
     * @return the average
     */
    public double getAverage() {
        lock.readLock().lock();
        try {
            if (counter > 0) {
                return mean;
            }
            return 0;
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Gets the variance
     *
     * @return the variance
     */
    public double getVariance() {
        lock.readLock().lock();
        try {
            if (counter <= 0) {
                return Double.NaN;
            }

            if (counter == 1) {
                return 0d;
            }

            return Math.max(0d, m2 / counter);
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Gets the standard deviation
     *
     * @return the standard deviation
     */
    public double getStandardDeviation() {
        lock.readLock().lock();
        try {
            if (counter <= 0) {
                return Double.NaN;
            }

            if (counter == 1) {
                return 0d;
            }

            return Math.sqrt(getVariance());
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(equalitySnapshot());
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        StatisticCounter other = (StatisticCounter) obj;

        // never hold both locks at the same time (a queued writer could otherwise cause a deadlock between
        // a.equals(b) and b.equals(a)): take a snapshot of each side under its own lock, one after the other
        return Arrays.equals(equalitySnapshot(), other.equalitySnapshot());
    }


    /**
     * Takes a consistent snapshot of the attributes relevant for {@link #equals(Object)} and {@link #hashCode()}
     * under the read lock.
     *
     * @return the snapshot: counter, max, min, square sum and sum (doubles as raw long bits)
     */
    private long[] equalitySnapshot() {
        lock.readLock().lock();
        try {
            long[] snapshot = new long[5];
            snapshot[0] = counter;
            snapshot[1] = Double.doubleToLongBits(getMaxValue());
            snapshot[2] = Double.doubleToLongBits(getMinValue());
            snapshot[3] = Double.doubleToLongBits(squareSum);
            snapshot[4] = Double.doubleToLongBits(sum);
            return snapshot;
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return toString(null);
    }


    /**
     * Create the statistic counter as string
     *
     * @param title the title
     * @return the string representation
     */
    public String toString(String title) {
        lock.readLock().lock();
        try {
            StringBuilder builder = new StringBuilder();

            if (title != null) {
                builder.append(title);
            } else {
                builder.append("StatisticCounter:");
            }

            builder.append(TextUtil.NL);
            builder.append("    number of elements: ").append(counter).append(TextUtil.NL);
            builder.append("                   min: ").append(getMinValue()).append(TextUtil.NL);
            builder.append("                   max: ").append(getMaxValue()).append(TextUtil.NL);
            builder.append("                   sum: ").append(sum).append(TextUtil.NL);
            builder.append("               average: ").append(getAverage()).append(TextUtil.NL);
            builder.append("    standard deviation: ").append(getStandardDeviation());
            return builder.toString();
        } finally {
            lock.readLock().unlock();
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
     * Restores the state and the transient lock after deserialization. Data serialized with version 1.1.0 or older
     * contains no {@code mean} and {@code m2} values, they are rebuilt from the sum and the square sum.
     *
     * @param in the object input stream
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the class cannot be found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        counter = fields.get("counter", 0L);
        min = fields.get("min", Double.POSITIVE_INFINITY);
        max = fields.get("max", Double.NEGATIVE_INFINITY);
        sum = fields.get("sum", 0d);
        squareSum = fields.get("squareSum", 0d);
        mean = fields.get("mean", 0d);
        m2 = fields.get("m2", 0d);
        lock = new ReentrantReadWriteLock();

        if (fields.defaulted("mean") || fields.defaulted("m2")) {
            rebuildMeanAndM2();
        }
    }


    /**
     * Rebuilds the mean and the sum of squared deviations ({@code m2}) from the counter, the sum and the square sum.
     * It is used for values which are only known as sums (e.g. data serialized with version 1.1.0 or older); the
     * deviation is less precise than the incrementally calculated one in case of large values with a small deviation.
     */
    void rebuildMeanAndM2() {
        if (counter <= 0) {
            mean = 0.0;
            m2 = 0.0;
            return;
        }

        mean = sum / counter;
        m2 = Math.max(0d, squareSum - sum * sum / counter);
    }


    /**
     * Creates a copy with the same runtime class (also of a subclass) and its own lock. The fields of a subclass are
     * copied shallow, a subclass with mutable state has to override this method to copy it.
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public StatisticCounter clone() {
        lock.readLock().lock();
        try {
            // the copy keeps the runtime class of a subclass and gets its own lock
            StatisticCounter inst = (StatisticCounter) super.clone();
            inst.lock = new ReentrantReadWriteLock();
            return inst;
        } catch (CloneNotSupportedException e) {
            // can not happen, the class implements Cloneable
            throw new IllegalStateException(e);
        } finally {
            lock.readLock().unlock();
        }
    }
}
