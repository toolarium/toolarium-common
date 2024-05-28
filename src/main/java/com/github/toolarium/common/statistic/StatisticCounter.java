/*
 * StatisticCounter.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.statistic;

import com.github.toolarium.common.util.TextUtil;
import java.io.Serializable;
import java.util.Objects;


/**
 * This class acts as a helper class which can be used to compute several simple statistics for a set of numbers.
 *
 * @author patrick
 */
public class StatisticCounter implements IStatisticCounter<StatisticCounter>, Cloneable, Serializable {
    private static final long serialVersionUID = 7610753026869713980L;
    private long counter;
    private double min;
    private double max;
    private double sum;
    private double squareSum;


    /**
     * Constructor
     */
    public StatisticCounter() {
        clear();
    }
    
    
    /**
     * Constructor
     * @param counter the counter
     * @param min the min value
     * @param max the max value
     * @param sum the sum value
     * @param squareSum the square sum value
     */
    public StatisticCounter(long counter, double min, double max, double sum, double squareSum) {
        this();
        this.counter = counter;
        this.min = min;
        this.max = max;
        this.sum = sum;
        this.squareSum = squareSum;
    }

    
    /**
     * Clear and reset internal attributes
     */
    public synchronized void clear() {
        counter = 0;
        min = -1;
        max = 0;
        sum = 0.0;
        squareSum = 0.0;
    }

    
    /**
     * Adds a long value
     *
     * @param num the data to add
     */
    public synchronized void add(long num) {
        Long val = Long.valueOf(num);
        add(val.doubleValue());
    }
    
    
    /**
     * Adds a double value
     *
     * @param num the number to add
     */
    public synchronized void add(double num) {
        counter++;
        sum += num;
        squareSum += num * num;

        if (num > max) {
            max = num;
        }

        if (min < 0 || num < min) {
            min = num;
        }
    }    


    /**
     * @see com.github.toolarium.common.statistic.IStatisticCounter#add(java.lang.Object)
     */
    @Override
    public synchronized void add(StatisticCounter statisticCounter) {
        if (statisticCounter == null) {
            return;
        }

        if (min < 0) {
            min = statisticCounter.getMinValue();
        } else {
            min = Math.min(min, statisticCounter.getMinValue());
        }
        
        max = Math.max(max, statisticCounter.getMaxValue());

        counter += statisticCounter.getCounter();
        sum += statisticCounter.getSum();
        squareSum += statisticCounter.getSquareSum();
    }
    
    
    /**
     * Gets the counter
     *
     * @return the counter
     */
    public synchronized long getCounter() {
        return counter;
    }


    /**
     * Gets the min value back
     *
     * @return the min value
     */
    public synchronized double getMinValue() {
        if (min < 0) {
            return 0;
        }
        
        return min;
    }

    
    /**
     * Gets the max value back
     *
     * @return the max value
     */
    public synchronized double getMaxValue() {
        return max;
    }

    
    /**
     * Gets the range (German: Spannweite)
     *
     * @return the range
     */
    public synchronized double getRange() {
        return max - min;
    }


    /**
     * Gets the total sum 
     *
     * @return the sum
     */
    public synchronized double getSum() {
        return sum;
    }


    /**
     * Gets the square sum 
     *
     * @return the sum
     */
    public synchronized double getSquareSum() {
        return squareSum;
    }

    
    /**
     * Gets the total average
     *
     * @return the average
     */
    public synchronized double getAverage() {
        if (counter > 0) {
            return sum / counter;
        }
        return 0;
    }
    
    
    /**
     * Gets the variance
     *
     * @return the variance
     */
    public synchronized double getVariance() {
        if (counter <= 0) {
            return Double.NaN;
        }
        
        if (counter == 1) {
            return 0d;
        }

        double mean = getAverage();
        return squareSum / counter - mean * mean;
    }

    
    /**
     * Gets the total average
     *
     * @return the average
     */
    public synchronized double getStandardDeviation() {
        if (counter <= 0) {
            return Double.NaN;
        }
        if (counter == 1) {
            return 0d;

        }
        return Math.sqrt(getVariance());
    }

    
    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(counter, max, min, squareSum, sum);
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
        return counter == other.counter && Double.doubleToLongBits(max) == Double.doubleToLongBits(other.max)
                && Double.doubleToLongBits(min) == Double.doubleToLongBits(other.min)
                && Double.doubleToLongBits(squareSum) == Double.doubleToLongBits(other.squareSum)
                && Double.doubleToLongBits(sum) == Double.doubleToLongBits(other.sum);
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
     * @return the string represenation
     */
    public String toString(String title) {
        StringBuilder builder = new StringBuilder();
        
        if (title != null) {
            builder.append(title);
        } else {
            builder.append("StatisticCounter:");
        }

        builder.append(TextUtil.NL);
        builder.append("    number of elements: ").append(counter).append(TextUtil.NL);
        builder.append("                   min: ").append(getMinValue()).append(TextUtil.NL);
        builder.append("                   max: ").append(max).append(TextUtil.NL);
        builder.append("                   sum: ").append(sum).append(TextUtil.NL);
        builder.append("               average: ").append(getAverage()).append(TextUtil.NL);
        builder.append("    standard deviation: ").append(getStandardDeviation());
        return builder.toString();
    }


    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public StatisticCounter clone() {
        StatisticCounter inst;
        try {
            inst = (StatisticCounter) super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            InternalError ex = new InternalError(
                    "Could not clone object " + getClass().getName() + ": " + e.getMessage());
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        }

        inst.counter = counter;
        inst.min = min;
        inst.max = max;
        inst.sum = sum;
        inst.squareSum = squareSum;
        return inst;
    }
}
