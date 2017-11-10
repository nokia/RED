/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteStreamFile;
import org.robotframework.red.viewers.Selections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class SelectionsPropertyTester extends PropertyTester {

    @VisibleForTesting
    static final String ALL_ELEMENTS_HAVE_SAME_TYPE = "allElementsHaveSameType";

    @VisibleForTesting
    static final String SELECTED_ACTUAL_FILE = "selectedActualFile";

    @VisibleForTesting
    static final String KEYWORD_CALL_BUT_NOT_DOCUMENTATION = "keywordCallButNotDocumentation";

    @VisibleForTesting
    static final String METADATA_SELECTED = "isMetadataSelected";

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof IStructuredSelection,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + IStructuredSelection.class.getName());

        if (expectedValue instanceof Boolean) {
            return testProperty((IStructuredSelection) receiver, property, ((Boolean) expectedValue).booleanValue());
        }
        return false;
    }

    private static boolean testProperty(final IStructuredSelection selection, final String property,
            final boolean expected) {
        if (ALL_ELEMENTS_HAVE_SAME_TYPE.equals(property)) {
            return allElementsHaveSameType(selection) == expected;
        } else if (SELECTED_ACTUAL_FILE.equals(property)) {
            return isSelectedActualProjectMember(selection) == expected;
        } else if (KEYWORD_CALL_BUT_NOT_DOCUMENTATION.equals(property)) {
            return isKeywordCallButNotDocumentation(selection) == expected;
        } else if (METADATA_SELECTED.equals(property)) {
            return isMetadataSelected(selection) == expected;
        }
        return false;
    }

    private static boolean isKeywordCallButNotDocumentation(IStructuredSelection selection) {
        final Object selected = selection.getFirstElement();
        if (selected == null || !(selected instanceof RobotKeywordCall)) {
            return false;
        }
        final AModelElement<?> element = ((RobotKeywordCall) selected).getLinkedElement();
        return !(element instanceof TestDocumentation) && !(element instanceof KeywordDocumentation);
    }

    private static boolean isMetadataSelected(IStructuredSelection selection) {
        final Object selected = selection.getFirstElement();
        if (selected == null || !(selected instanceof RobotSetting)) {
            return false;
        }
        return SettingsGroup.METADATA.equals(((RobotSetting) selected).getGroup());
    }

    private static boolean isSelectedActualProjectMember(final IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return false;
        }
        final Object firstSelectedElement = selection.getFirstElement();
        if (firstSelectedElement instanceof RobotFileInternalElement) {
            final RobotSuiteFile parentFile = ((RobotFileInternalElement) firstSelectedElement).getSuiteFile();
            if (parentFile instanceof RobotSuiteStreamFile) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private static boolean allElementsHaveSameType(final IStructuredSelection selection) {
        final List<Object> elements = Selections.getElements(selection, Object.class);
        if (elements.isEmpty()) {
            return true;
        }
        Class<?> mostGeneralType = elements.get(0).getClass();
        for (final Object element : elements) {
            if (element.getClass().isAssignableFrom(mostGeneralType)) {
                mostGeneralType = element.getClass();
            }
        }
        if (mostGeneralType == Object.class) {
            return false;
        }
        for (final Object element : elements) {
            if (!mostGeneralType.isInstance(element)) {
                return false;
            }
        }
        return true;
    }

    public static boolean allElementsAreFromSameProject(final IStructuredSelection selection) {
        final List<IResource> resources = Selections.getAdaptableElements(selection, IResource.class);
        final List<RobotCasesSection> sections = Selections.getElements(selection, RobotCasesSection.class);
        final List<RobotCase> cases = Selections.getElements(selection, RobotCase.class);
        if (sections.isEmpty() && cases.isEmpty() && resources.isEmpty()) {
            return false;
        }
        final Set<IProject> projects = new HashSet<IProject>();
        for (final IResource resource : resources) {
            if (projects.add(resource.getProject()) && projects.size() > 1) {
                return false;
            }
        }
        for (final RobotCasesSection section : sections) {
            if (projects.add(section.getSuiteFile().getProject().getProject()) && projects.size() > 1) {
                return false;
            }
        }
        for (final RobotCase robotCase : cases) {
            if (projects.add(robotCase.getSuiteFile().getProject().getProject()) && projects.size() > 1) {
                return false;
            }
        }
        return projects.size() == 1;
    }

}
