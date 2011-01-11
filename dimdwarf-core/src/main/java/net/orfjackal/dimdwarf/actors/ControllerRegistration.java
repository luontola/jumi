// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.actors;

import net.orfjackal.dimdwarf.controller.Controller;

import javax.inject.Provider;

public class ControllerRegistration {

    private final String name;
    private final Provider<? extends Controller> controller;

    public ControllerRegistration(String name,
                                  Provider<? extends Controller> controller) {
        this.name = name;
        this.controller = controller;
    }

    public String getName() {
        return name;
    }

    public Provider<? extends Controller> getController() {
        return controller;
    }
}
