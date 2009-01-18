/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.db.inmemory;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Esko Luontola
 * @since 19.11.2008
 */
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
