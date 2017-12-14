/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.robotframework.ide.eclipse.main.plugin.search.SearchSettings.SearchFor;
import org.robotframework.ide.eclipse.main.plugin.search.SearchSettings.SearchLimitation;
import org.robotframework.ide.eclipse.main.plugin.search.SearchSettings.SearchTarget;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.viewers.Selections;

/**
 * @author Michal Anglart
 */
public class RedSearchPage extends DialogPage implements ISearchPage {

    static final String ID = "org.robotframework.red.search.page";

    private ISearchPageContainer searchContainer;

    private Combo searchText;
    private Button caseSensitiveButton;

    private final Map<SearchFor, Button> searchForButtons = new HashMap<>();
    private final Map<SearchLimitation, Button> limitToButtons = new HashMap<>();
    private final Map<SearchTarget, Button> targetsButtons = new HashMap<>();


    @Override
    public void setContainer(final ISearchPageContainer container) {
        this.searchContainer = container;
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite internalParent = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(5, 4).applyTo(internalParent);

        final Label notYetImplementedLabel = new Label(internalParent, SWT.WRAP);
        notYetImplementedLabel.setForeground(ColorsManager.getColor(255, 0, 0));
        notYetImplementedLabel.setText("The search feature for RED is currently very limited and in its "
                + "initial phase. As for now it is only possible to search inside documentation content "
                + "of libraries and library keywords.");
        GridDataFactory.fillDefaults().span(2, 1).hint(100, SWT.DEFAULT).applyTo(notYetImplementedLabel);

        final Label label = new Label(internalParent, SWT.NONE);
        label.setText("Search string (* = any string, ? = any character)");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(label);
        
        searchText = new Combo(internalParent, SWT.SINGLE | SWT.BORDER);
        searchText.addModifyListener(e -> searchContainer.setPerformActionEnabled(searchCanBePerformed()));
        GridDataFactory.fillDefaults().grab(true, false).applyTo(searchText);

        caseSensitiveButton = new Button(internalParent, SWT.CHECK);
        caseSensitiveButton.setText("Case sensitive");

        final Composite groupsParent = new Composite(internalParent, SWT.NONE);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(groupsParent);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(groupsParent);

        createSearchForGroup(groupsParent);
        createLimitToGroup(groupsParent);
        createSearchInGroup(internalParent);

        setInput();

        setControl(internalParent);
    }

    private void createSearchForGroup(final Composite parent) {
        final Group searchForGroup = new Group(parent, SWT.NONE);
        searchForGroup.setText("Search for");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(searchForGroup);
        GridLayoutFactory.fillDefaults().applyTo(searchForGroup);
        GridLayoutFactory.fillDefaults().margins(5, 3).applyTo(searchForGroup);

        searchForButtons.put(SearchFor.KEYWORD, new Button(searchForGroup, SWT.RADIO));
        searchForButtons.get(SearchFor.KEYWORD).setText(SearchFor.KEYWORD.getLabel());
        searchForButtons.get(SearchFor.KEYWORD).addSelectionListener(widgetSelectedAdapter(e -> {
            enableTargetButtons(target -> target != SearchTarget.VARIABLE_FILE);
            enableLimitButtons(alwaysTrue());
        }));

        searchForButtons.put(SearchFor.TEST_CASE, new Button(searchForGroup, SWT.RADIO));
        searchForButtons.get(SearchFor.TEST_CASE).setText(SearchFor.TEST_CASE.getLabel());
        searchForButtons.get(SearchFor.TEST_CASE).addSelectionListener(widgetSelectedAdapter(e -> {
            enableTargetButtons(target -> target == SearchTarget.SUITE);
            enableLimitButtons(limit -> limit == SearchLimitation.ONLY_DECLARATIONS);

            deselectLimitButtons();
            limitToButtons.get(SearchLimitation.ONLY_DECLARATIONS).setSelection(true);
        }));

        searchForButtons.put(SearchFor.VARIABLE, new Button(searchForGroup, SWT.RADIO));
        searchForButtons.get(SearchFor.VARIABLE).setText(SearchFor.VARIABLE.getLabel());
        searchForButtons.get(SearchFor.VARIABLE).addSelectionListener(widgetSelectedAdapter(e -> {
            enableTargetButtons(
                    target -> target != SearchTarget.STANDARD_LIBRARY && target != SearchTarget.REFERENCED_LIBRARY);
            enableLimitButtons(alwaysTrue());
        }));

        searchForButtons.put(SearchFor.DOC_CONTENT, new Button(searchForGroup, SWT.RADIO));
        searchForButtons.get(SearchFor.DOC_CONTENT).setText(SearchFor.DOC_CONTENT.getLabel());
        searchForButtons.get(SearchFor.DOC_CONTENT).addSelectionListener(widgetSelectedAdapter(e -> {
            enableTargetButtons(target -> target != SearchTarget.VARIABLE_FILE);
            enableLimitButtons(limit -> limit == SearchLimitation.NO_LIMITS);

            deselectLimitButtons();
            limitToButtons.get(SearchLimitation.NO_LIMITS).setSelection(true);
        }));
    }

    private void enableTargetButtons(final Predicate<SearchTarget> targetPredicate) {
        for (final Entry<SearchTarget, Button> targetEntry : targetsButtons.entrySet()) {
            if (targetPredicate.test(targetEntry.getKey())) {
                targetEntry.getValue().setEnabled(true);
            } else {
                targetEntry.getValue().setEnabled(false);
                targetEntry.getValue().setSelection(false);
            }
        }
    }

    private void enableLimitButtons(final Predicate<SearchLimitation> limitPredicate) {
        for (final Entry<SearchLimitation, Button> targetEntry : limitToButtons.entrySet()) {
            targetEntry.getValue().setEnabled(limitPredicate.test(targetEntry.getKey()));
        }
    }

    private void deselectLimitButtons() {
        limitToButtons.values().forEach(button -> button.setSelection(false));
    }

    private void createLimitToGroup(final Composite parent) {
        final Group limitToGroup = new Group(parent, SWT.NONE);
        limitToGroup.setText("Limit to");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(limitToGroup);
        GridLayoutFactory.fillDefaults().margins(5, 3).applyTo(limitToGroup);

        for (final SearchLimitation limitation : newArrayList(SearchLimitation.ONLY_DECLARATIONS,
                SearchLimitation.ONLY_REFERENCES, SearchLimitation.NO_LIMITS)) {
            limitToButtons.put(limitation, new Button(limitToGroup, SWT.RADIO));
            limitToButtons.get(limitation).setText(limitation.getLabel());
        }
    }

    private void createSearchInGroup(final Composite parent) {
        final Group searchInGroup = new Group(parent, SWT.NONE);
        searchInGroup.setText("Search in");
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(searchInGroup);
        RowLayoutFactory.fillDefaults().type(SWT.HORIZONTAL).fill(true).margins(5, 3).applyTo(searchInGroup);

        for (final SearchTarget target : newArrayList(SearchTarget.SUITE, SearchTarget.RESOURCE,
                SearchTarget.STANDARD_LIBRARY, SearchTarget.REFERENCED_LIBRARY, SearchTarget.VARIABLE_FILE)) {
            targetsButtons.put(target, new Button(searchInGroup, SWT.CHECK));
            targetsButtons.get(target).setText(target.getLabel());
            targetsButtons.get(target).addSelectionListener(
                    widgetSelectedAdapter(e -> searchContainer.setPerformActionEnabled(searchCanBePerformed())));
        }
    }

    private void setInput() {
        final SearchSettingsPersister persister = new SearchSettingsPersister();
        final SearchSettings settings = persister.readSettings();

        searchText.setText("");
        searchText.setItems(persister.getRecentPatterns().toArray(new String[0]));

        searchForButtons.get(settings.getSearchFor()).setSelection(true);

        limitToButtons.get(settings.getSearchLimitation()).setSelection(true);
        for (final SearchTarget target : settings.getTargets()) {
            targetsButtons.get(target).setSelection(true);
        }

        // notify so that the listeners are able to properly enable/disable dependent buttons
        searchForButtons.get(settings.getSearchFor()).notifyListeners(SWT.Selection, new Event());
    }

    private SearchSettings getInput() {
        final SearchSettings settings = new SearchSettings();
        settings.getSearchPattern().setPattern(searchText.getText());
        settings.setCaseSensitive(caseSensitiveButton.getSelection());
        settings.setResourcesRoots(getResourcesRoots());

        for (final Entry<SearchFor, Button> searchForEntry : searchForButtons.entrySet()) {
            if (searchForEntry.getValue().getSelection()) {
                settings.setSearchFor(searchForEntry.getKey());
                break;
            }
        }
        for (final Entry<SearchLimitation, Button> limitEntry : limitToButtons.entrySet()) {
            if (limitEntry.getValue().getSelection()) {
                settings.setSearchLimitation(limitEntry.getKey());
                break;
            }
        }
        for (final Entry<SearchTarget, Button> targetEntry : targetsButtons.entrySet()) {
            if (targetEntry.getValue().getSelection()) {
                settings.addTarget(targetEntry.getKey());
            }
        }
        return settings;
    }

    private List<IResource> getResourcesRoots() {
        final List<IResource> resourcesRoots = new ArrayList<>();

        final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        switch (searchContainer.getSelectedScope()) {
            case ISearchPageContainer.WORKSPACE_SCOPE:
                Stream.of(wsRoot.getProjects())
                    .forEach(project -> resourcesRoots.add(project));
                break;
            case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
                Stream.of(searchContainer.getSelectedProjectNames())
                    .map(name -> wsRoot.getProject(name))
                    .forEach(project -> resourcesRoots.add(project));
                break;
            case ISearchPageContainer.SELECTION_SCOPE:
                final List<IResource> selectedResources = Selections
                        .getElements((IStructuredSelection) searchContainer.getSelection(), IResource.class);
                resourcesRoots.addAll(selectedResources);
                break;
            case ISearchPageContainer.WORKING_SET_SCOPE:
                Stream.of(searchContainer.getSelectedWorkingSets())
                        .flatMap(set -> (Stream<IAdaptable>) Stream.of(set.getElements()))
                        .map(adaptable -> adaptable.getAdapter(IResource.class))
                        .filter(notNull())
                        .distinct()
                        .forEach(resource -> resourcesRoots.add(resource));
                break;
            default:
                throw new IllegalStateException("Unrecognized search scope was selected");
        }
        return resourcesRoots;
    }

    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
        searchContainer.setActiveEditorCanProvideScopeSelection(visible);
        searchContainer.setPerformActionEnabled(visible && searchCanBePerformed());
    }

    private boolean searchCanBePerformed() {
        return !searchText.getText().isEmpty() && targetsButtons.values().stream().anyMatch(Button::getSelection);
    }

    @Override
    public boolean performAction() {
        final SearchSettings settings = getInput();
        new SearchSettingsPersister().writeSettings(settings);
        NewSearchUI.runQueryInBackground(new SearchQuery(settings));
        return true;
    }
}
