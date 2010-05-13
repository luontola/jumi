// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class GroupLockSpec extends Specification<Object> {

    private GroupLock<String> lock;

    public void create() throws Exception {
        lock = new GroupLock<String>();
    }

    private static void unlockInNewThread(final LockHandle handle, final AtomicBoolean wasUnlockedFirst) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                wasUnlockedFirst.set(true);
                handle.unlock();
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }


    public class WhenNothingIsLocked {

        public void noKeysAreLocked() {
            specify(lock.isLocked("A"), should.equal(false));
            specify(lock.getLockCount(), should.equal(0));
        }
    }

    public class WhenOneKeyIsLocked {
        private LockHandle handle;

        public void create() {
            handle = lock.lockAll("A");
        }

        public void oneKeyIsLocked() {
            specify(lock.isLocked("A"));
            specify(lock.getLockCount(), should.equal(1));
        }

        public void theLockedKeyCanNotBeRelockedUntilItIsFirstUnlocked() {
            AtomicBoolean wasUnlocked = new AtomicBoolean(false);
            unlockInNewThread(handle, wasUnlocked);
            lock.lockAll("A");
            specify(wasUnlocked.get());
        }

        public void otherKeysAreNotLocked() {
            specify(lock.isLocked("B"), should.equal(false));
        }

        public void otherKeysMayBeLocked() {
            lock.lockAll("B");
            specify(lock.isLocked("B"));
            specify(lock.getLockCount(), should.equal(2));
        }
    }

    public class WhenManyKeysAreLocked {
        private LockHandle handle;

        public void create() {
            handle = lock.lockAll("A", "B");
        }

        public void thoseKeysAreLocked() {
            specify(lock.isLocked("A"));
            specify(lock.isLocked("B"));
            specify(lock.getLockCount(), should.equal(2));
        }

        public void anOverlappingSetOfKeysCanNotBeRelockedUntilTheyAreFirstUnlocked() {
            AtomicBoolean wasUnlocked = new AtomicBoolean(false);
            unlockInNewThread(handle, wasUnlocked);
            lock.lockAll("B", "C");
            specify(wasUnlocked.get());
            specify(lock.getLockCount(), should.equal(2));
        }

        public void aDistinctSetOfKeysMayBeLocked() {
            lock.lockAll("C", "D");
            specify(lock.getLockCount(), should.equal(4));
        }
    }

    public class WhenKeysAreUnlocked {
        private LockHandle handleA;

        public void create() {
            handleA = lock.lockAll("A");
            lock.lockAll("B");
            handleA.unlock();
        }

        public void theUnlockedKeysAreNotLocked() {
            specify(lock.isLocked("A"), should.equal(false));
        }

        public void otherLockedKeysAreStillLocked() {
            specify(lock.isLocked("B"));
        }

        public void theUnlockedKeysCanNotBeReunlocked() {
            specify(lock.getLockCount(), should.equal(1));
            specify(new Block() {
                public void run() throws Throwable {
                    handleA.unlock();
                }
            }, should.raise(IllegalStateException.class));
            specify(lock.getLockCount(), should.equal(1));
        }

        public void aUsedHandleCanNotUnlockAKeyWhichIsRelocked() {
            specify(lock.getLockCount(), should.equal(1));
            final LockHandle oldHandleA = handleA;
            final LockHandle newHandleA = lock.lockAll("A");
            specify(lock.getLockCount(), should.equal(2));
            specify(new Block() {
                public void run() throws Throwable {
                    oldHandleA.unlock();
                }
            }, should.raise(IllegalStateException.class));
            specify(lock.getLockCount(), should.equal(2));
            newHandleA.unlock();
            specify(lock.getLockCount(), should.equal(1));
        }
    }
}
