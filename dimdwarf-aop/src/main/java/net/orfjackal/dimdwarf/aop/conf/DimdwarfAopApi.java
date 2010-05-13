// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.aop.conf;

import net.orfjackal.dimdwarf.aop.AopApi;

public class DimdwarfAopApi implements AopApi {

    public String getEntityAnnotation() {
        return "net/orfjackal/dimdwarf/api/Entity";
    }

    public String getEntityInterface() {
        return "net/orfjackal/dimdwarf/api/internal/EntityObject";
    }

    public String getEntityHelperClass() {
        return "net/orfjackal/dimdwarf/entities/tref/EntityHelper";
    }
}
