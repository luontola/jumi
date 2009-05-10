// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc.cms;

/**
 * @author Esko Luontola
 * @since 29.11.2008
 */
public enum Color {
    WHITE(0), GRAY(1), BLACK(2);

    private final int index;

    Color(int index) {
        this.index = index;
    }

    public long getIndex() {
        return index;
    }

    public static Color parseIndex(int index) {
        for (Color color : values()) {
            if (color.index == index) {
                return color;
            }
        }
        throw new IllegalArgumentException("Invalid index: " + index);
    }
}
