/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshKeywordDefinitionCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeEditorFormFragment;
import org.robotframework.red.viewers.ElementsAddingEditingSupport.NewElementsCreator;

import com.google.common.base.Supplier;

public class KeywordsEditorFormFragment extends CodeEditorFormFragment {

    private HeaderFilterMatchesCollection matches;

    public KeywordsEditorFormFragment() {
        super(RobotKeywordsSection.SECTION_NAME);
    }
    
    @Override
    protected ITreeContentProvider createContentProvider() {
        return new KeywordsContentProvider();
    }

    @Override
    protected String getViewerMenuId() {
        return "org.robotframework.ide.eclipse.editor.page.keywords.contextMenu";
    }

    @Override
    protected String getHeaderMenuId() {
        return "org.robotframework.ide.eclipse.editor.page.keywords.header.contextMenu";
    }

    @Override
    protected boolean sectionIsDefined() {
        return fileModel.findSection(RobotKeywordsSection.class).isPresent();
    }

    @Override
    protected RobotSuiteFileSection getSection() {
        return fileModel.findSection(RobotKeywordsSection.class).orNull();
    }

    @Override
    protected NewElementsCreator<RobotElement> provideNewElementsCreator() {
        return new NewElementsCreator<RobotElement>() {

            @Override
            public RobotElement createNew(final Object parent) {
                if (parent instanceof RobotKeywordsSection) {
                    final RobotKeywordsSection section = (RobotKeywordsSection) parent;
                    commandsStack.execute(new CreateFreshKeywordDefinitionCommand(section, true));
                    return section.getChildren().get(section.getChildren().size() - 1);
                } else if (parent instanceof RobotKeywordDefinition) {
                    final RobotKeywordDefinition definition = (RobotKeywordDefinition) parent;
                    commandsStack.execute(new CreateFreshKeywordCallCommand(definition));
                    return definition.getChildren().get(definition.getChildren().size() - 1);
                }
                return null;
            }
        };
    }

    @Override
    protected int calculateLongestArgumentsList() {
        final RobotSuiteFileSection section = getSection();
        int max = RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
        if (section != null) {
            for (final RobotElement element : section.getChildren()) {
                final RobotKeywordDefinition keyword = (RobotKeywordDefinition) element;
                if (keyword.hasArguments()) {
                    max = Math.max(max, keyword.getArgumentsSetting().getArguments().size());
                }

                for (final RobotElement nestedElement : element.getChildren()) {
                    final RobotKeywordCall call = (RobotKeywordCall) nestedElement;
                    max = Math.max(max, call.getArguments().size());
                }
            }
        }
        return max;
    }

    @Override
    protected Supplier<HeaderFilterMatchesCollection> getMatchesProvider() {
        return new Supplier<HeaderFilterMatchesCollection>() {
            @Override
            public HeaderFilterMatchesCollection get() {
                return matches;
            }
        };
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        final KeywordsMatchesCollection keywordMatches = new KeywordsMatchesCollection();
        keywordMatches.collect((RobotElement) viewer.getInput(), filter);
        return keywordMatches;
    }

    @Inject
    @Optional
    private void whenKeywordIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            viewer.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenKeywordIsAdded(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            viewer.setComparator(null);
            viewer.getTree().setSortColumn(null);
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_STRUCTURAL_ALL) final RobotKeywordDefinition definition) {
        if (definition.getSuiteFile() == fileModel) {
            viewer.refresh(definition);
            viewer.update(definition, null);
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenKeywordDetailIsChanged(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_CHANGE_ALL) final RobotKeywordDefinition definition) {
        if (definition.getSuiteFile() == fileModel) {
            viewer.update(definition, null);
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallDetailIsChanged(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotKeywordCall keywordCall) {
        if (keywordCall.getParent() instanceof RobotKeywordDefinition && keywordCall.getSuiteFile() == fileModel) {
            viewer.update(keywordCall, null);
            setDirty();
        }
    }
}
