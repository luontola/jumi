// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api;

/**
 * The kind of proxy to generate for given a target class.
 *
 * @author Esko Luontola
 * @since 9.9.2008
 */
public enum ProxyType {

    /**
     * The proxy will extend {@link Object} and implement the same interfaces as the target class.
     */
    INTERFACE,

    /**
     * The proxy will extend the target class. The target class should not have any publicly accessible fields,
     * because the proxy can not intercept field access. The constructor of the target class will not be called
     * when the proxy is created.
     */
    CLASS
}
