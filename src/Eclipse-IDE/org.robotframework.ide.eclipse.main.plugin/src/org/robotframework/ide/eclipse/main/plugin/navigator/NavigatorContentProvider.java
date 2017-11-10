/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.red.viewers.TreeContentProvider;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class NavigatorContentProvider extends TreeContentProvider {

    private TreeViewer viewer;

    public NavigatorContentProvider() {
        final IEclipseContext activeContext = getContext().getActiveLeaf();
        ContextInjectionFactory.inject(this, activeContext);
    }

    @Override
    public void dispose() {
        final IEclipseContext activeContext = getContext().getActiveLeaf();
        ContextInjectionFactory.uninject(this, activeContext);
    }

    private IEclipseContext getContext() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IEclipseContext.class);
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        this.viewer = (TreeViewer) viewer;
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        return null;
    }

    @Override
    public Object[] getChildren(final Object parentElement) {
        final IFile file = RedPlugin.getAdapter(parentElement, IFile.class);

        if (file != null) {
            return RedPlugin.getModelManager().createSuiteFile(file).getSections().toArray();
        } else if (parentElement instanceof RobotSettingsSection) {
            final List<? extends RobotElement> children = ((RobotSettingsSection) parentElement).getChildren();
            return groupedChildren(children).toArray();
        } else if (parentElement instanceof RobotElement) {
            return ((RobotElement) parentElement).getChildren().toArray();
        }
        return new Object[0];
    }

    private List<RobotElement> groupedChildren(final List<? extends RobotElement> children) {
        final List<RobotElement> grouped = new ArrayList<>(children);
        final Multimap<SettingsGroup, RobotSetting> removedElements = LinkedHashMultimap.create();

        for (final RobotElement element : children) {
            if (element instanceof RobotSetting) {
                final RobotSetting setting = (RobotSetting) element;
                final SettingsGroup group = setting.getGroup();
                if (group != SettingsGroup.NO_GROUP) {
                    grouped.remove(element);
                    removedElements.put(group, setting);
                }
            }
        }
        for (final SettingsGroup key : removedElements.keySet()) {
            grouped.add(new ArtificialGroupingRobotElement(key, new ArrayList<>(removedElements.get(key))));
        }
        return grouped;
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
        if (element instanceof RobotCase || element instanceof RobotKeywordDefinition) {
            return false;
        }
        if (element instanceof RobotElement) {
            return !((RobotElement) element).getChildren().isEmpty();
        }
        return true;
    }

    @Inject
    @Optional
    private void whenStructuralChangeWasMade(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_FILE_ALL) final RobotElement affectedElement) {
        if (viewer != null) {
            Object elementToUpdate = affectedElement;
            if (affectedElement instanceof RobotSuiteFile) {
                elementToUpdate = ((RobotSuiteFile) affectedElement).getFile();
            }
            viewer.refresh(elementToUpdate);
        }
    }

    @Inject
    @Optional
    private void whenFileChangesExternally(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
        if (change.getElement() instanceof RobotSuiteFile && change.getKind() == Kind.CHANGED && viewer != null) {
            final RobotSuiteFile suiteFile = (RobotSuiteFile) change.getElement();
            viewer.refresh(suiteFile.getSuiteFile().getFile());
        }
    }

    @Inject
    @Optional
    private void whenReconcilationWasDone(
            @UIEventTopic(RobotModelEvents.REPARSING_DONE) final RobotSuiteFile fileModel) {
        if (viewer != null && !viewer.getTree().isDisposed()) {
            viewer.refresh(fileModel.getFile());
        }
    }

    @Inject
    @Optional
    private void whenModelIsDisposed(
            @UIEventTopic(RobotModelEvents.SUITE_MODEL_DISPOSED) final RobotElementChange change) {
        if (change.getElement() instanceof RobotSuiteFile && change.getKind() == Kind.CHANGED && viewer != null) {
            final RobotSuiteFile suiteFile = (RobotSuiteFile) change.getElement();
            viewer.refresh(suiteFile.getSuiteFile().getFile());
        }
    }

    @Inject
    @Optional
    private void whenCaseNameChanges(@UIEventTopic(RobotModelEvents.ROBOT_CASE_NAME_CHANGE) final RobotCase testCase) {
        if (viewer != null) {
            viewer.refresh(testCase.getParent());
        }
    }

    @Inject
    @Optional
    private void whenKeywordDefinitionNameChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_NAME_CHANGE) final RobotKeywordDefinition keywordDef) {
        if (viewer != null) {
            viewer.update(keywordDef, null);
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallNameChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE) final RobotKeywordCall keywordCall) {
        if (viewer != null) {
            viewer.update(keywordCall, null);
        }
    }

    @Inject
    @Optional
    private void whenVariableTypeChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE) final RobotVariable variable) {
        if (viewer != null) {
            // actually the sorting may have been affected, so we need to
            // refresh parent
            viewer.refresh(variable.getParent());
        }
    }

    @Inject
    @Optional
    private void whenVariableNameChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE) final RobotVariable variable) {
        if (viewer != null && !viewer.getTree().isDisposed()) {
            // actually the sorting may have been affected, so we need to
            // refresh parent
            viewer.refresh(variable.getParent());
        }
    }

    @Inject
    @Optional
    private void whenSettingArgumentChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE) final RobotSetting setting) {
        // in case of Library/Resource/etc. import or metadata, we are also
        // interested in changes of first argument to keyword call, since it
        // used as label
        if (viewer != null && !viewer.getTree().isDisposed()) {
            viewer.update(setting, null);
        }
    }
}
