// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.locks.*;

@ThreadSafe
public class GroupLock<T extends Comparable<T>> {

    private final SortedSet<T> lockedKeys = new TreeSet<T>();

    private final ReentrantLock myLock = new ReentrantLock();
    private final Condition someKeyWasUnlocked = myLock.newCondition();

    @CheckReturnValue
    public LockHandle lockAll(T... keys) {
        return lockAll(Arrays.asList(keys));
    }

    @CheckReturnValue
    public LockHandle lockAll(Collection<T> keys) {
        myLock.lock();
        try {
            SortedSet<T> sortedKeys = new TreeSet<T>(keys);
            for (T key : sortedKeys) {
                awaitAndLock(key);
            }
            return new MyLockHandle(sortedKeys);
        } finally {
            myLock.unlock();
        }
    }

    private void awaitAndLock(T key) {
        while (isLocked(key)) {
            someKeyWasUnlocked.awaitUninterruptibly();
        }
        lockedKeys.add(key);
    }

    public boolean isLocked(T key) {
        myLock.lock();
        try {
            return lockedKeys.contains(key);
        } finally {
            myLock.unlock();
        }
    }

    public int getLockCount() {
        myLock.lock();
        try {
            return lockedKeys.size();
        } finally {
            myLock.unlock();
        }
    }


    @ThreadSafe
    private class MyLockHandle implements LockHandle {

        private Collection<T> keys;

        public MyLockHandle(Collection<T> keys) {
            this.keys = keys;
        }

        public void unlock() {
            myLock.lock();
            try {
                if (keys == null) {
                    throw new IllegalStateException("Keys have already been unlocked: " + keys);
                }
                lockedKeys.removeAll(keys);
                keys = null;
                someKeyWasUnlocked.signalAll();
            } finally {
                myLock.unlock();
            }
        }
    }
}
