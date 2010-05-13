// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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
