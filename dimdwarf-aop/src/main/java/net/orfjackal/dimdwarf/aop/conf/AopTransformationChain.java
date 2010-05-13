// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.aop.conf;

import net.orfjackal.dimdwarf.aop.*;
import org.objectweb.asm.ClassVisitor;

public class AopTransformationChain extends AbstractTransformationChain {

    private final AopApi api;

    public AopTransformationChain(AopApi api) {
        this.api = api;
    }

    protected ClassVisitor getAdapters(ClassVisitor cv) {
        // the adapter declared last is processed first
        cv = new AddEqualsAndHashCodeMethodsForEntities(api, cv);
        cv = new AddMarkerInterfaceForEntities(api, cv);
        return cv;
    }
}
