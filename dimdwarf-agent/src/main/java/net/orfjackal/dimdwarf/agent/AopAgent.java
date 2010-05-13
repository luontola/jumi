// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.agent;

import net.orfjackal.dimdwarf.aop.conf.*;

import java.lang.instrument.Instrumentation;

/**
 * See http://java.sun.com/javase/6/docs/api/java/lang/instrument/package-summary.html
 * for details on using agents.
 */
public class AopAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        installTransformations(inst);
    }

//    public static void agentmain(String agentArgs, Instrumentation inst) {
//        installTransformations(inst);
//    }

    private static void installTransformations(Instrumentation inst) {
        inst.addTransformer(new AopTransformationChain(new DimdwarfAopApi()));
    }
}
