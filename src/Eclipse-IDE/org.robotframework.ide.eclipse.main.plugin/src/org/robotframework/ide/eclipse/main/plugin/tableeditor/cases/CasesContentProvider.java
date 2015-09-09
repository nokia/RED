/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

class CasesContentProvider implements ITreeContentProvider {

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        // nothing to do
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        if (inputElement instanceof RobotCasesSection) {
            final RobotCasesSection section = (RobotCasesSection) inputElement;
            final boolean isEditable = section.getSuiteFile().isEditable();
            return extendWithAddingToken(inputElement, section.getChildren().toArray(), "test case", 0, isEditable);
        }
        return new Object[0];
    }

    @Override
    public Object[] getChildren(final Object element) {
        if (element instanceof RobotCase) {
            final RobotCase testCase = (RobotCase) element;
            final boolean isEditable = testCase.getSuiteFile().isEditable();
            return extendWithAddingToken(element, getKeywordCalls(testCase), "", 1, isEditable);
        }
        return new Object[0];
    }

    private RobotKeywordCall[] getKeywordCalls(final RobotCase testCase) {
        final List<RobotKeywordCall> children = testCase.getChildren();
        final List<RobotKeywordCall> filtered = newArrayList(Iterables.filter(children,
                new Predicate<RobotKeywordCall>() {
                    @Override
                    public boolean apply(final RobotKeywordCall call) {
                        return !(call instanceof RobotDefinitionSetting);
                    }
        }));
        return filtered.toArray(new RobotKeywordCall[0]);
    }

    private Object[] extendWithAddingToken(final Object parent, final Object[] elements, final String name,
            final int rank, final boolean isEditable) {
        final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
        newElements[elements.length] = new ElementAddingToken(parent, name, isEditable, rank);
        return newElements;
    }

    @Override
    public Object getParent(final Object element) {
        if (element instanceof RobotElement) {
            return ((RobotElement) element).getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(final Object element) {
        return element instanceof RobotCase;
    }

}
