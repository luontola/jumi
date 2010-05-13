// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import java.io.Serializable;

public class DummyTask implements Runnable, Serializable {
    private static final long serialVersionUID = 1L;

    private final String dummyId;

    public DummyTask(String dummyId) {
        this.dummyId = dummyId;
    }

    public String getDummyId() {
        return dummyId;
    }

    public void run() {
    }

    public boolean equals(Object obj) {
        if (obj instanceof DummyTask) {
            DummyTask other = (DummyTask) obj;
            return dummyId.equals(other.dummyId);
        }
        return false;
    }
}
