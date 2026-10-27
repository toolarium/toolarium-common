/*
 * ObjectLockManagerTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.common.util.RoundUtil;
import com.github.toolarium.common.util.ThreadUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.Arrays;
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
        assertObjectLockManagerStatistic(o, 3.0, 0.0, 0.0, 1L);

        assertEquals(LIST3, o.lock(LIST3));
        assertEquals(LIST3, o.unlock(LIST3));
        assertObjectLockManagerStatistic(o, 3.0, 0.0, 0.0, 1L);

        assertEquals(LIST2, o.lock(LIST1));
        assertEquals(LIST2, o.unlock(LIST2));
        assertObjectLockManagerStatistic(o, 3.0, 0.0, 0.0, 2L);
        o.releaseResource();
        assertObjectLockManagerStatistic(o, 3.0, 0.0, 0.0, 2L);
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
        assertObjectLockManagerStatistic(o, 2.0, 1.0, 0.0, 1L);

        assertEquals(LIST3, o.lock(LIST3));
        assertEquals(LIST3, o.unlock(LIST3));
        assertObjectLockManagerStatistic(o, 2.3, 0.8, 0.0, 1L);

        assertEquals(LIST4, o.lock(LIST1));
        assertEquals(LIST4, o.unlock(LIST4));
        assertObjectLockManagerStatistic(o, 2.4, 1.8, 0.0, 2L);

        assertEquals(LIST5, o.lock(LIST1));
        assertEquals(LIST5, o.unlock(LIST5));
        assertObjectLockManagerStatistic(o, 2.2, 3.0, 0.0, 2L);

        ThreadUtil.getInstance().sleep(100L);
        o.setObjectLockSize(null);
        assertEquals(LIST1, o.lock(LIST1));
        assertEquals(LIST1, o.unlock(LIST1));
        assertObjectLockManagerStatistic(o, 3.3, 2.6, 0.0, 2L);
        o.releaseResource();
        assertObjectLockManagerStatistic(o, 3.3, 2.6, 0.0, 2L);
    }

    
    /**
     * The statistics are available before the first lock / unlock call
     */
    @Test
    void statisticNotNullBeforeFirstUse() {
        ObjectLockManager o = new ObjectLockManager();
        assertNotNull(o.getLockStatistic());
        assertNotNull(o.getUnlockStatistic());
        assertNotNull(o.getIgnoreLockStatistic());
        assertEquals(0L, o.getLockStatistic().getCounter());
        assertNotNull(o.toString());
        o.cleanup();
    }


    /**
     * Cleanup and toString after a release resource must not fail
     */
    @Test
    void cleanupAndToStringAfterRelease() {
        ObjectLockManager o = new ObjectLockManager(null, 1000L);
        o.cleanupAfterUnlock(false);
        assertEquals(LIST2, o.lock(LIST2));
        assertEquals(LIST2, o.unlock(LIST2));
        assertTrue(o.toString().contains("unlockMap=3"));
        o.cleanup();
        o.releaseResource();
        o.cleanup();
        assertTrue(o.toString().contains("lockMap=0, unlockMap=0"));
    }


    /**
     * Serialization round trip
     *
     * @throws IOException in case of an I/O error
     * @throws ClassNotFoundException in case the class could not be found
     */
    @Test
    void serializationRoundTrip() throws IOException, ClassNotFoundException {
        ObjectLockManager o = new ObjectLockManager(3, null);
        assertEquals(LIST2, o.lock(LIST1));
        assertEquals(LIST2, o.unlock(LIST2));
        assertObjectLockManagerStatistic(o, 3.0, 0.0, 0.0, 1L);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }

        ObjectLockManager copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            copy = (ObjectLockManager) ois.readObject();
        }

        assertObjectLockManagerStatistic(copy, 3.0, 0.0, 0.0, 1L);
        assertTrue(copy.toString().contains("isInitialized=false"));
        assertEquals(LIST2, copy.lock(LIST1));
        assertTrue(copy.lock(LIST2).isEmpty());
        assertEquals(LIST2, copy.unlock(LIST2));
        copy.cleanup();
        assertTrue(copy.toString().contains("isInitialized=true"));
    }


    /**
     * The lock size reached counter counts only calls where elements are left unlocked because of the limit
     */
    @Test
    void lockSizeReachedCountsOnlyRefusedElements() {
        IObjectLockManager o = new ObjectLockManager().setObjectLockSize(3);
        assertEquals(LIST2, o.lock(LIST2)); // exactly fills the limit, nothing refused
        assertEquals(0L, o.getNumberOfLockSizeReached());
        assertEquals(LIST3, o.lock(LIST1)); // 0..2 already locked, 3..5 locked, 6..9 are refused
        assertEquals(1L, o.getNumberOfLockSizeReached());
        assertEquals(LIST5, o.lock(LIST5)); // below the limit
        assertEquals(1L, o.getNumberOfLockSizeReached());

        o.setObjectLockSize(0);
        assertTrue(o.lock(LIST4).isEmpty()); // lock size 0: a non-empty list is refused
        assertEquals(2L, o.getNumberOfLockSizeReached());
        assertTrue(o.lock(List.of()).isEmpty());
        assertEquals(2L, o.getNumberOfLockSizeReached());
    }


    /**
     * A huge unlock timeout must not overflow, the unlocked elements stay blocked
     */
    @Test
    void unlockTimeoutMaxValue() {
        IObjectLockManager o = new ObjectLockManager().setUnlockTimeout(Long.MAX_VALUE);
        assertEquals(LIST2, o.lock(LIST2));
        assertEquals(LIST2, o.unlock(LIST2));
        assertTrue(o.lock(LIST2).isEmpty());
        assertEquals(LIST3, o.lock(LIST3));
    }


    /**
     * A lock size of 0 locks nothing
     */
    @Test
    void lockSizeZero() {
        IObjectLockManager o = new ObjectLockManager().setObjectLockSize(0);
        assertTrue(o.lock(LIST1).isEmpty());
        o.setObjectLockSize(null);
        assertEquals(LIST1, o.lock(LIST1));
    }


    /**
     * Null elements are ignored by lock and unlock, the other elements are processed and not left locked
     */
    @Test
    void nullElementsAreIgnored() {
        IObjectLockManager o = new ObjectLockManager();
        List<Integer> withNull = Arrays.asList(0, null, 1, 2);
        assertEquals(LIST2, o.lock(withNull));
        assertTrue(o.lock(LIST2).isEmpty());
        assertEquals(withNull, o.unlock(withNull));
        assertEquals(LIST2, o.lock(LIST2));
        assertEquals(LIST2, o.unlock(LIST2));

        // with an unlock timeout and a lock size
        o = new ObjectLockManager(2, Long.MAX_VALUE);
        assertEquals(List.of(0, 1), o.lock(Arrays.asList(null, 0, null, 1, 2)));
        o.unlock(Arrays.asList(0, null, 1));
        assertEquals(List.of(2), o.lock(LIST2));
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


    /**
     * Many unlocks with a long unlock timeout: the cleanup runs at most once per second (M05). Every lock is logged
     * on debug level here, so only the function is checked; without logging 40,000 lock / unlock calls take less than 0.1 s.
     */
    @Test
    void manyUnlocksWithLongUnlockTimeout() {
        IObjectLockManager o = new ObjectLockManager(null, 3_600_000L);
        assertTimeoutPreemptively(Duration.ofSeconds(30), () -> {
            for (int i = 0; i < 5_000; i++) {
                List<Integer> element = List.of(i);
                assertEquals(element, o.lock(element));
                o.unlock(element);
            }
        });

        // an unlocked element can not be locked again during the unlock timeout
        assertTrue(o.lock(List.of(1)).isEmpty());
    }


    /**
     * Removing the unlock timeout releases the blocked unlocked elements
     */
    @Test
    void removeUnlockTimeoutClearsUnlockMap() {
        ObjectLockManager o = new ObjectLockManager(null, 60_000L);
        o.cleanupAfterUnlock(false);
        assertEquals(LIST2, o.lock(LIST2));
        assertEquals(LIST2, o.unlock(LIST2));
        assertTrue(o.toString().contains("unlockMap=3"));

        o.setUnlockTimeout(null);
        assertTrue(o.toString().contains("unlockMap=0"));
        assertEquals(LIST2, o.lock(LIST2));
    }
}
