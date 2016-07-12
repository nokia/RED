/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.InOrder;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.setting.UnknownSetting;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class SettingsTokenCollectorTest {

    @Test
    public void test_noSettingTable_shouldReturn_empty() {
        // prepare
        final RobotFileOutput fileOut = mock(RobotFileOutput.class);
        final RobotFile modelFile = mock(RobotFile.class);
        final SettingTable table = mock(SettingTable.class);

        when(fileOut.getFileModel()).thenReturn(modelFile);
        when(modelFile.getSettingTable()).thenReturn(table);
        when(table.isPresent()).thenReturn(false);

        // execute
        List<RobotToken> collect = new SettingsTokenCollector().collect(fileOut);

        // verify
        assertThat(collect).isEmpty();

        InOrder order = inOrder(fileOut, modelFile, table);
        order.verify(fileOut, times(1)).getFileModel();
        order.verify(modelFile, times(1)).getSettingTable();
        order.verify(table, times(1)).isPresent();
        order.verifyNoMoreInteractions();
    }

    @Test
    public void test_noSettingsInTable_shouldReturn_empty() {
        // prepare
        final RobotFileOutput fileOut = mock(RobotFileOutput.class);
        final RobotFile modelFile = mock(RobotFile.class);
        final SettingTable table = mock(SettingTable.class);

        when(fileOut.getFileModel()).thenReturn(modelFile);
        when(modelFile.getSettingTable()).thenReturn(table);
        when(table.isPresent()).thenReturn(true);
        when(table.getHeaders()).thenReturn(new ArrayList<TableHeader<? extends ARobotSectionTable>>(0));
        when(table.getDefaultTags()).thenReturn(new ArrayList<DefaultTags>(0));
        when(table.getDocumentation()).thenReturn(new ArrayList<SuiteDocumentation>(0));
        when(table.getForceTags()).thenReturn(new ArrayList<ForceTags>(0));
        when(table.getImports()).thenReturn(new ArrayList<AImported>(0));
        when(table.getMetadatas()).thenReturn(new ArrayList<Metadata>(0));
        when(table.getSuiteSetups()).thenReturn(new ArrayList<SuiteSetup>(0));
        when(table.getSuiteTeardowns()).thenReturn(new ArrayList<SuiteTeardown>(0));
        when(table.getTestSetups()).thenReturn(new ArrayList<TestSetup>(0));
        when(table.getTestTeardowns()).thenReturn(new ArrayList<TestTeardown>(0));
        when(table.getTestTemplates()).thenReturn(new ArrayList<TestTemplate>(0));
        when(table.getTestTimeouts()).thenReturn(new ArrayList<TestTimeout>(0));
        when(table.getUnknownSettings()).thenReturn(new ArrayList<UnknownSetting>(0));

        // execute
        List<RobotToken> collect = new SettingsTokenCollector().collect(fileOut);

        // verify
        assertThat(collect).isEmpty();

        InOrder order = inOrder(fileOut, modelFile, table);
        order.verify(fileOut, times(1)).getFileModel();
        order.verify(modelFile, times(1)).getSettingTable();
        order.verify(table, times(1)).isPresent();
        order.verify(table, times(1)).getHeaders();
        order.verify(table, times(1)).getDefaultTags();
        order.verify(table, times(1)).getDocumentation();
        order.verify(table, times(1)).getForceTags();
        order.verify(table, times(1)).getImports();
        order.verify(table, times(1)).getMetadatas();
        order.verify(table, times(1)).getSuiteSetups();
        order.verify(table, times(1)).getSuiteTeardowns();
        order.verify(table, times(1)).getTestSetups();
        order.verify(table, times(1)).getTestTeardowns();
        order.verify(table, times(1)).getTestTemplates();
        order.verify(table, times(1)).getTestTimeouts();
        order.verify(table, times(1)).getUnknownSettings();
        order.verifyNoMoreInteractions();
    }

    @Test
    public void test_SuiteSetups_settingsInTable_shouldReturn_tokensFromSetting() {
        // prepare
        final RobotFileOutput fileOut = mock(RobotFileOutput.class);
        final RobotFile modelFile = mock(RobotFile.class);
        final SettingTable table = mock(SettingTable.class);

        when(fileOut.getFileModel()).thenReturn(modelFile);
        when(modelFile.getSettingTable()).thenReturn(table);
        when(table.isPresent()).thenReturn(true);
        when(table.getHeaders()).thenReturn(new ArrayList<TableHeader<? extends ARobotSectionTable>>(0));
        when(table.getDefaultTags()).thenReturn(new ArrayList<DefaultTags>(0));
        when(table.getDocumentation()).thenReturn(new ArrayList<SuiteDocumentation>(0));
        when(table.getForceTags()).thenReturn(new ArrayList<ForceTags>(0));
        when(table.getImports()).thenReturn(new ArrayList<AImported>(0));
        when(table.getMetadatas()).thenReturn(new ArrayList<Metadata>(0));

        final RobotToken tok = new RobotToken();
        final List<RobotToken> setupElems = new ArrayList<>();
        setupElems.add(tok);
        final SuiteSetup setup = mock(SuiteSetup.class);
        when(setup.getElementTokens()).thenReturn(setupElems);
        final List<SuiteSetup> setups = new ArrayList<SuiteSetup>(1);
        setups.add(setup);
        when(table.getSuiteSetups()).thenReturn(setups);
        when(table.getSuiteTeardowns()).thenReturn(new ArrayList<SuiteTeardown>(0));
        when(table.getTestSetups()).thenReturn(new ArrayList<TestSetup>(0));
        when(table.getTestTeardowns()).thenReturn(new ArrayList<TestTeardown>(0));
        when(table.getTestTemplates()).thenReturn(new ArrayList<TestTemplate>(0));
        when(table.getTestTimeouts()).thenReturn(new ArrayList<TestTimeout>(0));
        when(table.getUnknownSettings()).thenReturn(new ArrayList<UnknownSetting>(0));

        // execute
        List<RobotToken> collect = new SettingsTokenCollector().collect(fileOut);

        // verify
        assertThat(collect).hasSize(1);
        assertThat(collect.get(0)).isSameAs(tok);

        InOrder order = inOrder(fileOut, modelFile, table, setup);
        order.verify(fileOut, times(1)).getFileModel();
        order.verify(modelFile, times(1)).getSettingTable();
        order.verify(table, times(1)).isPresent();
        order.verify(table, times(1)).getHeaders();
        order.verify(table, times(1)).getDefaultTags();
        order.verify(table, times(1)).getDocumentation();
        order.verify(table, times(1)).getForceTags();
        order.verify(table, times(1)).getImports();
        order.verify(table, times(1)).getMetadatas();
        order.verify(table, times(1)).getSuiteSetups();
        order.verify(setup, times(1)).getElementTokens();
        order.verify(table, times(1)).getSuiteTeardowns();
        order.verify(table, times(1)).getTestSetups();
        order.verify(table, times(1)).getTestTeardowns();
        order.verify(table, times(1)).getTestTemplates();
        order.verify(table, times(1)).getTestTimeouts();
        order.verify(table, times(1)).getUnknownSettings();
        order.verifyNoMoreInteractions();
    }
}
