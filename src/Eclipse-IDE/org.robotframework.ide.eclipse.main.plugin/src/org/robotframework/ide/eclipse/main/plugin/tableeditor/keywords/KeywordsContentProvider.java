package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

class KeywordsContentProvider implements ITreeContentProvider {

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
        if (inputElement instanceof RobotKeywordsSection) {
            final RobotKeywordsSection section = (RobotKeywordsSection) inputElement;
            final boolean isEditable = section.getSuiteFile().isEditable();
            return extendWithAddingToken(inputElement, section.getChildren().toArray(), "keyword", 0, isEditable);
        }
        return new Object[0];
    }

    @Override
    public Object[] getChildren(final Object element) {
        if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition def = (RobotKeywordDefinition) element;
            final boolean isEditable = def.getSuiteFile().isEditable();
            return extendWithAddingToken(element, getKeywordCalls(def), "", 1, isEditable);
        }
        return new Object[0];
    }

    private RobotKeywordCall[] getKeywordCalls(final RobotKeywordDefinition definition) {
        final List<RobotKeywordCall> children = definition.getChildren();
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
        return element instanceof RobotKeywordDefinition;
    }

}
