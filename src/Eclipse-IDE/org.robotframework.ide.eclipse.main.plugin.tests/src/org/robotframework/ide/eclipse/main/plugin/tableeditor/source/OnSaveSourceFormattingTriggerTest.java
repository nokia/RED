/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Region;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SuiteSourceEditorFormatter;
import org.robotframework.red.junit.PreferenceUpdater;
import org.robotframework.red.junit.ProjectProvider;

public class OnSaveSourceFormattingTriggerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(OnSaveSourceFormattingTriggerTest.class);

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    private static RobotModel model = new RobotModel();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("suite.robot");
        projectProvider.createFile("suite.tsv");
    }

    @AfterClass
    public static void afterSuite() {
        model = null;
    }

    @Test
    public void formattingIsNotStarted_whenItIsDisabled() {
        preferenceUpdater.setValue(RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED, false);

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite.robot"));
        final SuiteSourceEditorFormatter formatter = mock(SuiteSourceEditorFormatter.class);

        final OnSaveSourceFormattingTrigger trigger = new OnSaveSourceFormattingTrigger(formatter);
        trigger.formatSourceIfRequired(Document::new, suite, new NullProgressMonitor());

        verifyZeroInteractions(formatter);
    }

    @Test
    public void formattingIsNotStarted_whenSourceFileHasTsvExtension() throws Exception {
        preferenceUpdater.setValue(RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED, true);

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite.tsv"));
        final SuiteSourceEditorFormatter formatter = mock(SuiteSourceEditorFormatter.class);

        final OnSaveSourceFormattingTrigger trigger = new OnSaveSourceFormattingTrigger(formatter);
        trigger.formatSourceIfRequired(Document::new, suite, new NullProgressMonitor());

        verifyZeroInteractions(formatter);
    }

    @Test
    public void formattingIsStartedForWholeDocument_whenItIsEnabled() {
        preferenceUpdater.setValue(RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED, true);
        preferenceUpdater.setValue(RedPreferences.SAVE_ACTIONS_CHANGED_LINES_ONLY_ENABLED, false);

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite.robot"));
        final SuiteSourceEditorFormatter formatter = mock(SuiteSourceEditorFormatter.class);

        final Document document = new Document("line1", "line2", "line3");
        final OnSaveSourceFormattingTrigger trigger = new OnSaveSourceFormattingTrigger(formatter);
        trigger.formatSourceIfRequired(() -> document, suite, new NullProgressMonitor());

        verify(formatter).format(document, new Region(0, document.getLength()));
        verifyNoMoreInteractions(formatter);
    }

    @Test
    public void formattingIsStartedForChangedLines_whenItIsEnabled() {
        preferenceUpdater.setValue(RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED, true);
        preferenceUpdater.setValue(RedPreferences.SAVE_ACTIONS_CHANGED_LINES_ONLY_ENABLED, true);

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite.robot"));
        final SuiteSourceEditorFormatter formatter = mock(SuiteSourceEditorFormatter.class);

        final Document document = new Document("line1", "line2", "line3");
        final OnSaveSourceFormattingTrigger trigger = new OnSaveSourceFormattingTrigger(formatter);
        trigger.formatSourceIfRequired(() -> document, suite, new NullProgressMonitor());

        verify(formatter).format(document, newArrayList(0, 1, 2));
        verifyNoMoreInteractions(formatter);
    }

}
