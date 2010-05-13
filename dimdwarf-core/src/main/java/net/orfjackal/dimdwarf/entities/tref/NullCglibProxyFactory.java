// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import net.sf.cglib.proxy.*;

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
