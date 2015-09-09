/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.IElementComparer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;


class SettingElementsComparer implements IElementComparer {

    @Override
    public boolean equals(final Object a, final Object b) {
        if (a instanceof RobotElement && b instanceof RobotElement) {
            return getPositionInTable((RobotElement) a).equals(getPositionInTable((RobotElement) b));
        } else if (a instanceof Entry<?, ?> && b instanceof Entry<?, ?>) {
            return ((Entry<?, ?>) a).getKey().equals(((Entry<?, ?>) b).getKey());
        }
        return a == null && b == null || a instanceof ElementAddingToken && b instanceof ElementAddingToken;
    }

    @Override
    public int hashCode(final Object element) {
        if (element instanceof RobotElement) {
            return getPositionInTable((RobotElement) element).hashCode();
        } else if (element instanceof Entry<?, ?>) {
            return ((Entry<?, ?>) element).getKey().hashCode();
        } else if (element instanceof ElementAddingToken) {
            return Integer.MAX_VALUE;
        }
        return 0;
    }

    private Integer index(final RobotElement element) {
        if (element.getParent() != null) {
            for (int i = 0; i < element.getParent().getChildren().size(); i++) {
                if (element.getParent().getChildren().get(i) == element) {
                    return i;
                }
            }
        }
        return -1;
    }

    private Integer getPositionInTable(final RobotElement element) {
        if (element.getParent() instanceof RobotSettingsSection) {
            return index(element);
        } else {
            return Integer.MIN_VALUE;
        }
    }
}
