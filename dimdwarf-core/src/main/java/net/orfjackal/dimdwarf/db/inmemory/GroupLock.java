/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
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
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
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

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Esko Luontola
 * @since 19.11.2008
 */
@ThreadSafe
public class GroupLock<T> {

    private final Set<T> lockedKeys = new HashSet<T>();
    private final ReentrantLock lock = new ReentrantLock();

    @CheckReturnValue
    public LockHandle tryLock(T... keys) throws IllegalStateException {
        return tryLock(Arrays.asList(keys));
    }

    @CheckReturnValue
    public LockHandle tryLock(Collection<T> keys) throws IllegalStateException {
        lock.lock();
        try {
            checkNoneIsLocked(keys);
            lockedKeys.addAll(keys);
            return new MyLockHandle(keys);
        } finally {
            lock.unlock();
        }
    }

    private void checkNoneIsLocked(Collection<T> keys) {
        for (T key : keys) {
            if (isLocked(key)) {
                throw new IllegalStateException("Key is already locked: " + key);
            }
        }
    }

    public boolean isLocked(T key) {
        lock.lock();
        try {
            return lockedKeys.contains(key);
        } finally {
            lock.unlock();
        }
    }

    public int getLockCount() {
        lock.lock();
        try {
            return lockedKeys.size();
        } finally {
            lock.unlock();
        }
    }


    private class MyLockHandle implements LockHandle {

        private Collection<T> keys;

        public MyLockHandle(Collection<T> keys) {
            this.keys = Collections.unmodifiableCollection(new ArrayList<T>(keys));
        }

        public void unlock() {
            lock.lock();
            try {
                if (keys == null) {
                    throw new IllegalStateException("Keys have already been unlocked: " + keys);
                }
                lockedKeys.removeAll(keys);
                keys = null;
            } finally {
                lock.unlock();
            }
        }
    }
}
