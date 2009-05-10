// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.aop;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
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
