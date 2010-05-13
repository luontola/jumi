// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.aop;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.*;

import java.util.*;

@SuppressWarnings({"unchecked"})
public class AddMarkerInterfaceForEntities extends ClassNode {

    private final String annotationToFind;
    private final String interfaceToAdd;
    private final ClassVisitor cv;

    public AddMarkerInterfaceForEntities(AopApi api, ClassVisitor cv) {
        this.annotationToFind = "L" + api.getEntityAnnotation() + ";";
        this.interfaceToAdd = api.getEntityInterface();
        this.cv = cv;
    }

    public void visitEnd() {
        if (hasEntityAnnotation() && !interfaces.contains(interfaceToAdd)) {
            interfaces.add(interfaceToAdd);
        }
        accept(cv);
    }

    private boolean hasEntityAnnotation() {
        for (AnnotationNode an : visibleAnnotations()) {
            if (an.desc.equals(annotationToFind)) {
                return true;
            }
        }
        return false;
    }

    private List<AnnotationNode> visibleAnnotations() {
        return visibleAnnotations != null ? visibleAnnotations : Collections.emptyList();
    }
}
