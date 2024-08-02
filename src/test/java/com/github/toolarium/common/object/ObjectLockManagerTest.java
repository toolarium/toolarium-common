/*
 * ObjectLockManagerTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.common.util.RoundUtil;
import com.github.toolarium.common.util.ThreadUtil;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link ObjectLockManager}.
 *  
 * @author patrick
 */
public class ObjectLockManagerTest {
    private static final List<Integer> LIST1 = List.of(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
    private static final List<Integer> LIST2 = List.of(new Integer[] {0, 1, 2 });
    private static final List<Integer> LIST3 = List.of(new Integer[] {3, 4, 5 });
    private static final List<Integer> LIST4 = List.of(new Integer[] {6, 7, 8 });
    private static final List<Integer> LIST5 = List.of(new Integer[] {9 });
    

    /**
     * Simply lock / unlock 
     */
    @Test
    void lockUnlock() {
        IObjectLockManager o = new ObjectLockManager();
        assertEquals(LIST1, o.lock(LIST1));
        assertEquals(LIST1, o.unlock(LIST1));
        assertObjectLockManagerStatistic(o, 10.0, 0.0, 0.0, 0L);

        assertEquals(LIST1, o.lock(LIST1));
        assertTrue(o.lock(LIST1).isEmpty());
        assertEquals(LIST1, o.unlock(LIST1));
        assertObjectLockManagerStatistic(o, 6.7, 0.0, 3.3, 0L);

        assertEquals(LIST2, o.lock(LIST2));
        assertEquals(LIST2, o.unlock(LIST2));
        assertObjectLockManagerStatistic(o, 5.8, 0.0, 2.5, 0L);

        assertEquals(LIST3, o.lock(LIST3));
        assertEquals(LIST3, o.unlock(LIST3));
        assertObjectLockManagerStatistic(o, 5.2, 0.0, 2.0, 0L);

        assertEquals(LIST1, o.lock(LIST1));
        assertEquals(LIST1, o.unlock(LIST1));
        assertObjectLockManagerStatistic(o, 6.0, 0.0, 1.7, 0L);
        o.releaseResource();
        assertObjectLockManagerStatistic(o, 6.0, 0.0, 1.7, 0L);
    }

    
    /**
     * Simply lock / unlock with size
     */
    @Test
    void lockUnlockWithSize() {
        IObjectLockManager o = new ObjectLockManager().setObjectLockSize(3);
        assertEquals(LIST2, o.lock(LIST1));
        assertEquals(LIST2, o.unlock(LIST2));
        assertObjectLockManagerStatistic(o, 3.0, 0.0, 0.0, 1L);

        assertEquals(LIST2, o.lock(LIST2));
        assertEquals(LIST2, o.unlock(LIST2));
        assertObjectLockManagerStatistic(o, 3.0, 0.0, 0.0, 2L);

        assertEquals(LIST3, o.lock(LIST3));
        assertEquals(LIST3, o.unlock(LIST3));
        assertObjectLockManagerStatistic(o, 3.0, 0.0, 0.0, 3L);

        assertEquals(LIST2, o.lock(LIST1));
        assertEquals(LIST2, o.unlock(LIST2));
        assertObjectLockManagerStatistic(o, 3.0, 0.0, 0.0, 4L);
        o.releaseResource();
        assertObjectLockManagerStatistic(o, 3.0, 0.0, 0.0, 4L);
    }

    
    /**
     * Simply lock / unlock with size
     */
    @Test
    void lockUnlockWithSizeAndUnlockTimeout() {
        IObjectLockManager o = new ObjectLockManager().setObjectLockSize(3).setUnlockTimeout(100L);
        assertEquals(LIST2, o.lock(LIST1));
        assertEquals(LIST2, o.unlock(LIST2));
        assertObjectLockManagerStatistic(o, 3.0, 0.0, 0.0, 1L);

        assertTrue(o.lock(LIST2).isEmpty());
        ThreadUtil.getInstance().sleep(100L);
        assertEquals(LIST2, o.lock(LIST2));
        assertEquals(LIST2, o.unlock(LIST2));
        assertObjectLockManagerStatistic(o, 2.0, 1.0, 0.0, 2L);

        assertEquals(LIST3, o.lock(LIST3));
        assertEquals(LIST3, o.unlock(LIST3));
        assertObjectLockManagerStatistic(o, 2.3, 0.8, 0.0, 3L);

        assertEquals(LIST4, o.lock(LIST1));
        assertEquals(LIST4, o.unlock(LIST4));
        assertObjectLockManagerStatistic(o, 2.4, 1.8, 0.0, 4L);

        assertEquals(LIST5, o.lock(LIST1));
        assertEquals(LIST5, o.unlock(LIST5));
        assertObjectLockManagerStatistic(o, 2.2, 3.0, 0.0, 4L);

        ThreadUtil.getInstance().sleep(100L);
        o.setObjectLockSize(null);
        assertEquals(LIST1, o.lock(LIST1));
        assertEquals(LIST1, o.unlock(LIST1));
        assertObjectLockManagerStatistic(o, 3.3, 2.6, 0.0, 4L);
        o.releaseResource();
        assertObjectLockManagerStatistic(o, 3.3, 2.6, 0.0, 4L);
    }

    
    /**
     * Assert statistic
     *
     * @param o the object lock
     * @param ls the lock statistic
     * @param us the unlock statistic
     * @param is the ignore statistic
     * @param lr the max lock reached statistic
     */
    protected void assertObjectLockManagerStatistic(IObjectLockManager o, Double ls, Double us, Double is, long lr) {
        assertEquals(ls, RoundUtil.getInstance().round(o.getLockStatistic().getAverage(), 1));
        assertEquals(us, RoundUtil.getInstance().round(o.getUnlockStatistic().getAverage(), 1));
        assertEquals(is, RoundUtil.getInstance().round(o.getIgnoreLockStatistic().getAverage(), 1));
        assertEquals(lr, o.getNumberOfLockSizeReached());        
    }
    
}
