/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.IElementComparer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;


class CodeElementsComparer implements IElementComparer {

    @Override
    public boolean equals(final Object a, final Object b) {
        if (a == null && b == null) {
            return true;
        } else if (a instanceof RobotElement && b instanceof RobotElement) {
            return getPositionInTree((RobotElement) a).equals(getPositionInTree((RobotElement) b));
        } else if (a instanceof ElementAddingToken && b instanceof ElementAddingToken) {
            return getPositionInTree((ElementAddingToken) a).equals(getPositionInTree((ElementAddingToken) b));
        }
        return false;
    }

    @Override
    public int hashCode(final Object element) {
        if (element instanceof RobotElement) {
            return getPositionInTree((RobotElement) element).hashCode();
        } else if (element instanceof ElementAddingToken) {
            return getPositionInTree((ElementAddingToken) element).hashCode();
        }
        return 0;
    }

    private List<Integer> getPositionInTree(final RobotElement element) {
        final List<Integer> address = newArrayList();

        RobotElement current = element;
        while (current instanceof RobotKeywordCall || current instanceof RobotCodeHoldingElement) {
            address.add(0, index(current));
            current = current.getParent();
        }
        return address;
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

    private List<Integer> getPositionInTree(final ElementAddingToken token) {
        if (token.getParent() instanceof RobotKeywordDefinition || token.getParent() instanceof RobotCase) {
            final List<Integer> address = getPositionInTree((RobotElement) token.getParent());
            address.add(Integer.MAX_VALUE);
            return address;
        } else if (token.getParent() instanceof RobotSuiteFileSection) {
            return newArrayList(Integer.MAX_VALUE);
        }
        throw new IllegalStateException("Unrecognized parent of adding token");
    }

}
