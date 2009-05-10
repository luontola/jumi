// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import net.sf.cglib.proxy.*;

/**
 * @author Esko Luontola
 * @since 27.12.2008
 */
public abstract class NullCglibProxyFactory implements Factory {

    public Object newInstance(Callback callback) {
        throw new UnsupportedOperationException();
    }

    public Object newInstance(Callback[] callbacks) {
        throw new UnsupportedOperationException();
    }

    public Object newInstance(Class[] types, Object[] args, Callback[] callbacks) {
        throw new UnsupportedOperationException();
    }

    public Callback getCallback(int index) {
        throw new UnsupportedOperationException();
    }

    public void setCallback(int index, Callback callback) {
        throw new UnsupportedOperationException();
    }

    public void setCallbacks(Callback[] callbacks) {
        throw new UnsupportedOperationException();
    }

    public Callback[] getCallbacks() {
        throw new UnsupportedOperationException();
    }
}
