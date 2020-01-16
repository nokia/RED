/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SourceDocumentFormatter;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SuiteSourceEditorSelectionFixer;
import org.robotframework.red.junit.jupiter.BooleanPreference;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, PreferencesExtension.class })
public class OnSaveSourceFormattingTriggerTest {

    @Project(files = { "suite.robot", "suite.tsv" })
    static IProject project;

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED, value = false)
    @Test
    public void formattingIsNotStarted_whenItIsDisabled() throws Exception {
        final RobotSuiteFile suite = spy(new RobotModel().createSuiteFile(getFile(project, "suite.robot")));

        final SourceDocumentFormatter formatter = mock(SourceDocumentFormatter.class);
        final SuiteSourceEditorSelectionFixer selectionUpdater = mock(SuiteSourceEditorSelectionFixer.class);

        final OnSaveSourceFormattingTrigger trigger = spy(new OnSaveSourceFormattingTrigger());
        doReturn(formatter).when(trigger).getFormatter(suite);
        trigger.formatSourceIfRequired(Document::new, selectionUpdater, suite, new NullProgressMonitor());

        verifyNoInteractions(formatter);
        verifyNoInteractions(selectionUpdater);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED, value = true)
    @Test
    public void formattingIsNotStarted_whenSourceFileHasTsvExtension() throws Exception {
        final RobotSuiteFile suite = new RobotModel().createSuiteFile(getFile(project, "suite.tsv"));
        final SourceDocumentFormatter formatter = mock(SourceDocumentFormatter.class);
        final SuiteSourceEditorSelectionFixer selectionUpdater = mock(SuiteSourceEditorSelectionFixer.class);

        final OnSaveSourceFormattingTrigger trigger = spy(new OnSaveSourceFormattingTrigger());
        doReturn(formatter).when(trigger).getFormatter(suite);
        trigger.formatSourceIfRequired(Document::new, selectionUpdater, suite, new NullProgressMonitor());

        verifyNoInteractions(formatter);
        verifyNoInteractions(selectionUpdater);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED, value = true)
    @Test
    public void formattingIsStartedForWholeDocument_whenItIsEnabled() throws Exception {
        final RobotSuiteFile suite = new RobotModel().createSuiteFile(getFile(project, "suite.robot"));
        final SourceDocumentFormatter formatter = mock(SourceDocumentFormatter.class);
        final SuiteSourceEditorSelectionFixer selectionUpdater = mock(SuiteSourceEditorSelectionFixer.class);

        final Document document = new Document("line1", "line2", "line3");
        final OnSaveSourceFormattingTrigger trigger = spy(new OnSaveSourceFormattingTrigger());
        doReturn(formatter).when(trigger).getFormatter(suite);
        trigger.formatSourceIfRequired(() -> document, selectionUpdater, suite, new NullProgressMonitor());

        verify(formatter).format(document);
        verify(selectionUpdater).saveSelection(document);
        verify(selectionUpdater).fixSelection(document);
        verifyNoMoreInteractions(selectionUpdater);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED, value = true)
    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_CHANGED_LINES_ONLY_ENABLED, value = true)
    @Test
    public void formattingIsStartedForChangedLines_whenItIsEnabled() throws Exception {
        final RobotSuiteFile suite = new RobotModel().createSuiteFile(getFile(project, "suite.robot"));
        final SourceDocumentFormatter formatter = mock(SourceDocumentFormatter.class);

        final SuiteSourceEditorSelectionFixer selectionUpdater = mock(SuiteSourceEditorSelectionFixer.class);

        final Document document = new Document("line1", "line2", "line3");
        final OnSaveSourceFormattingTrigger trigger = spy(new OnSaveSourceFormattingTrigger());
        doReturn(formatter).when(trigger).getFormatter(suite);
        trigger.formatSourceIfRequired(() -> document, selectionUpdater, suite, new NullProgressMonitor());

        verify(formatter).format(document, newArrayList(0, 1, 2));
        verify(selectionUpdater).saveSelection(document);
        verify(selectionUpdater).fixSelection(document);
        verifyNoMoreInteractions(selectionUpdater);
    }

}
