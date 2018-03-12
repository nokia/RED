/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;

public class RedClipboardTest {

    private RedClipboard clipboard;

    @Before
    public void beforeTest() {
        clipboard = new RedClipboard(Display.getCurrent());
    }

    @After
    public void afterTest() {
        clipboard.clear();
        clipboard.dispose();
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToInsertSomeUnregisteredTypeOfData() {
        new RedClipboard().insertContent(Integer.valueOf(42));
    }

    @Test
    public void whenThereIsNoClipboardWrapped_thereAreNoElements() {
        assertThat(new RedClipboard().hasText()).isFalse();
        assertThat(new RedClipboard().hasPositionsCoordinates()).isFalse();
        assertThat(new RedClipboard().hasCases()).isFalse();
        assertThat(new RedClipboard().hasKeywordDefinitions()).isFalse();
        assertThat(new RedClipboard().hasKeywordCalls()).isFalse();
        assertThat(new RedClipboard().hasGeneralSettings()).isFalse();
        assertThat(new RedClipboard().hasImportSettings()).isFalse();
        assertThat(new RedClipboard().hasMetadataSettings()).isFalse();
        assertThat(new RedClipboard().hasSettings()).isFalse();
        assertThat(new RedClipboard().hasVariables()).isFalse();
    }

    @Test
    public void clipboardHasTextTest() {
        assertThat(clipboard.hasText()).isFalse();

        clipboard.insertContent(createVariables());
        assertThat(clipboard.hasText()).isFalse();

        clipboard.insertContent("text");
        assertThat(clipboard.hasText()).isTrue();

        clipboard.dispose();
        assertThat(clipboard.hasText()).isFalse();
    }

    @Test
    public void getTextFromClipboardTest() {
        assertThat(clipboard.getText()).isNull();
        assertThat(clipboard.getPositionsCoordinates()).isNull();

        clipboard.insertContent("text", createPositionCoordinates());
        assertThat(clipboard.getText()).isEqualTo("text");
        assertThat(clipboard.getPositionsCoordinates()).isEqualTo(createPositionCoordinates());

        clipboard.insertContent(createVariables());
        assertThat(clipboard.getText()).isNull();
        assertThat(clipboard.getPositionsCoordinates()).isNull();
    }

    @Test
    public void clipboardHasPositionCoordinatesTest() {
        assertThat(clipboard.hasPositionsCoordinates()).isFalse();

        clipboard.insertContent("text");
        assertThat(clipboard.hasPositionsCoordinates()).isFalse();

        clipboard.insertContent(createPositionCoordinates());
        assertThat(clipboard.hasPositionsCoordinates()).isTrue();

        clipboard.dispose();
        assertThat(clipboard.hasPositionsCoordinates()).isFalse();
    }

    @Test
    public void getPositionCoordinatesFromClipboardTest() {
        assertThat(clipboard.getPositionsCoordinates()).isNull();

        clipboard.insertContent(createPositionCoordinates());
        assertThat(clipboard.getPositionsCoordinates()).isEqualTo(createPositionCoordinates());

        clipboard.insertContent("text");
        assertThat(clipboard.getPositionsCoordinates()).isNull();
    }

    @Test
    public void clipboardHasVariablesTest() {
        assertThat(clipboard.hasVariables()).isFalse();

        clipboard.insertContent("text");
        assertThat(clipboard.hasVariables()).isFalse();

        clipboard.insertContent(createVariables());
        assertThat(clipboard.hasVariables()).isTrue();

        clipboard.dispose();
        assertThat(clipboard.hasVariables()).isFalse();
    }

    @Test
    public void getVariablesFromClipboardTest() {
        assertThat(clipboard.getVariables()).isNull();

        clipboard.insertContent(createVariables());
        final RobotVariable[] varsFromClipboard = clipboard.getVariables();
        assertThat(varsFromClipboard).hasSize(3);
        assertThat(varsFromClipboard[0].getName()).isEqualTo("var1");
        assertThat(varsFromClipboard[0].getParent()).isNull();
        assertThat(varsFromClipboard[1].getName()).isEqualTo("var2");
        assertThat(varsFromClipboard[1].getParent()).isNull();
        assertThat(varsFromClipboard[2].getName()).isEqualTo("var3");
        assertThat(varsFromClipboard[2].getParent()).isNull();

        clipboard.insertContent("text");
        assertThat(clipboard.getVariables()).isNull();
    }

    @Test
    public void clipboardHasCasesTest() {
        assertThat(clipboard.hasCases()).isFalse();

        clipboard.insertContent("text");
        assertThat(clipboard.hasCases()).isFalse();

        clipboard.insertContent(createCases());
        assertThat(clipboard.hasCases()).isTrue();

        clipboard.dispose();
        assertThat(clipboard.hasCases()).isFalse();
    }

    @Test
    public void getCasesFromClipboardTest() {
        assertThat(clipboard.getCases()).isNull();

        clipboard.insertContent(createCases());
        final RobotCase[] casesFromClipboard = clipboard.getCases();
        assertThat(casesFromClipboard).hasSize(2);
        assertThat(casesFromClipboard[0].getName()).isEqualTo("case 1");
        assertThat(casesFromClipboard[0].getParent()).isNull();
        assertThat(casesFromClipboard[1].getName()).isEqualTo("case 2");
        assertThat(casesFromClipboard[1].getParent()).isNull();

        clipboard.insertContent("text");
        assertThat(clipboard.getCases()).isNull();
    }

    @Test
    public void clipboardHasKeywordDefinitionsTest() {
        assertThat(clipboard.hasKeywordDefinitions()).isFalse();

        clipboard.insertContent("text");
        assertThat(clipboard.hasKeywordDefinitions()).isFalse();

        clipboard.insertContent(createKeywords());
        assertThat(clipboard.hasKeywordDefinitions()).isTrue();

        clipboard.dispose();
        assertThat(clipboard.hasKeywordDefinitions()).isFalse();
    }

    @Test
    public void getKeywordDefinitionsFromClipboardTest() {
        assertThat(clipboard.getKeywordDefinitions()).isNull();

        clipboard.insertContent(createKeywords());
        final RobotKeywordDefinition[] keywordsFromClipboard = clipboard.getKeywordDefinitions();
        assertThat(keywordsFromClipboard).hasSize(3);
        assertThat(keywordsFromClipboard[0].getName()).isEqualTo("kw 1");
        assertThat(keywordsFromClipboard[0].getParent()).isNull();
        assertThat(keywordsFromClipboard[1].getName()).isEqualTo("kw 2");
        assertThat(keywordsFromClipboard[1].getParent()).isNull();
        assertThat(keywordsFromClipboard[2].getName()).isEqualTo("kw 3");
        assertThat(keywordsFromClipboard[2].getParent()).isNull();

        clipboard.insertContent("text");
        assertThat(clipboard.getKeywordDefinitions()).isNull();
    }

    @Test
    public void clipboardHasKeywordCallsTest() {
        assertThat(clipboard.hasKeywordCalls()).isFalse();

        clipboard.insertContent("text");
        assertThat(clipboard.hasKeywordCalls()).isFalse();

        clipboard.insertContent(createGeneralSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasKeywordCalls()).isFalse();

        clipboard.insertContent(createCalls());
        assertThat(clipboard.hasKeywordCalls()).isTrue();

        clipboard.dispose();
        assertThat(clipboard.hasKeywordCalls()).isFalse();
    }

    @Test
    public void getKeywordCallsFromClipboardTest() {
        assertThat(clipboard.getKeywordCalls()).isNull();

        clipboard.insertContent(createCalls());
        final RobotKeywordCall[] callsFromClipboard = clipboard.getKeywordCalls();
        assertThat(callsFromClipboard).hasSize(2);
        assertThat(callsFromClipboard[0].getName()).isEqualTo("Log");
        assertThat(callsFromClipboard[0].getArguments()).containsExactly("10");
        assertThat(callsFromClipboard[0].getParent()).isNull();
        assertThat(callsFromClipboard[1].getName()).isEqualTo("Log");
        assertThat(callsFromClipboard[1].getArguments()).containsExactly("20");
        assertThat(callsFromClipboard[1].getParent()).isNull();

        clipboard.insertContent("text");
        assertThat(clipboard.getKeywordCalls()).isNull();
    }

    @Test
    public void clipboardHasSettingsCallsTest() {
        assertThat(clipboard.hasSettings()).isFalse();

        clipboard.insertContent("text");
        assertThat(clipboard.hasSettings()).isFalse();

        clipboard.insertContent(createGeneralSettings());
        assertThat(clipboard.hasSettings()).isTrue();

        clipboard.insertContent(createGeneralSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasSettings()).isFalse();

        clipboard.insertContent(createImportSettings());
        assertThat(clipboard.hasSettings()).isTrue();

        clipboard.insertContent(createImportSettingsMixedWithGeneral());
        assertThat(clipboard.hasSettings()).isTrue();

        clipboard.insertContent(createImportSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasSettings()).isFalse();

        clipboard.insertContent(createMetadataSettings());
        assertThat(clipboard.hasSettings()).isTrue();

        clipboard.insertContent(createMetadataSettingsMixedWithGeneral());
        assertThat(clipboard.hasSettings()).isTrue();

        clipboard.insertContent(createMetadataSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasSettings()).isFalse();

        clipboard.dispose();
        assertThat(clipboard.hasSettings()).isFalse();
    }

    @Test
    public void clipboardHasGeneralSettingsCallsTest() {
        assertThat(clipboard.hasGeneralSettings()).isFalse();

        clipboard.insertContent("text");
        assertThat(clipboard.hasGeneralSettings()).isFalse();

        clipboard.insertContent(createGeneralSettings());
        assertThat(clipboard.hasGeneralSettings()).isTrue();

        clipboard.insertContent(createGeneralSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasGeneralSettings()).isFalse();

        clipboard.insertContent(createImportSettings());
        assertThat(clipboard.hasGeneralSettings()).isFalse();

        clipboard.insertContent(createImportSettingsMixedWithGeneral());
        assertThat(clipboard.hasGeneralSettings()).isFalse();

        clipboard.insertContent(createImportSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasGeneralSettings()).isFalse();

        clipboard.insertContent(createMetadataSettings());
        assertThat(clipboard.hasGeneralSettings()).isFalse();

        clipboard.insertContent(createMetadataSettingsMixedWithGeneral());
        assertThat(clipboard.hasGeneralSettings()).isFalse();

        clipboard.insertContent(createMetadataSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasSettings()).isFalse();

        clipboard.dispose();
        assertThat(clipboard.hasSettings()).isFalse();
    }

    @Test
    public void clipboardHasImportSettingsCallsTest() {
        assertThat(clipboard.hasImportSettings()).isFalse();

        clipboard.insertContent("text");
        assertThat(clipboard.hasImportSettings()).isFalse();

        clipboard.insertContent(createGeneralSettings());
        assertThat(clipboard.hasImportSettings()).isFalse();

        clipboard.insertContent(createGeneralSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasImportSettings()).isFalse();

        clipboard.insertContent(createImportSettings());
        assertThat(clipboard.hasImportSettings()).isTrue();

        clipboard.insertContent(createImportSettingsMixedWithGeneral());
        assertThat(clipboard.hasImportSettings()).isFalse();

        clipboard.insertContent(createImportSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasImportSettings()).isFalse();

        clipboard.insertContent(createMetadataSettings());
        assertThat(clipboard.hasImportSettings()).isFalse();

        clipboard.insertContent(createMetadataSettingsMixedWithGeneral());
        assertThat(clipboard.hasImportSettings()).isFalse();

        clipboard.insertContent(createMetadataSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasImportSettings()).isFalse();

        clipboard.dispose();
        assertThat(clipboard.hasImportSettings()).isFalse();
    }

    @Test
    public void clipboardHasMetadataSettingsCallsTest() {
        assertThat(clipboard.hasMetadataSettings()).isFalse();

        clipboard.insertContent("text");
        assertThat(clipboard.hasMetadataSettings()).isFalse();

        clipboard.insertContent(createGeneralSettings());
        assertThat(clipboard.hasMetadataSettings()).isFalse();

        clipboard.insertContent(createGeneralSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasMetadataSettings()).isFalse();

        clipboard.insertContent(createImportSettings());
        assertThat(clipboard.hasMetadataSettings()).isFalse();

        clipboard.insertContent(createImportSettingsMixedWithGeneral());
        assertThat(clipboard.hasMetadataSettings()).isFalse();

        clipboard.insertContent(createImportSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasMetadataSettings()).isFalse();

        clipboard.insertContent(createMetadataSettings());
        assertThat(clipboard.hasMetadataSettings()).isTrue();

        clipboard.insertContent(createMetadataSettingsMixedWithGeneral());
        assertThat(clipboard.hasMetadataSettings()).isFalse();

        clipboard.insertContent(createMetadataSettingsMixedWithKeywordCalls());
        assertThat(clipboard.hasMetadataSettings()).isFalse();

        clipboard.dispose();
        assertThat(clipboard.hasMetadataSettings()).isFalse();
    }

    @Test
    public void clipboardHasMultipleDataTest() {
        assertThat(clipboard.hasText()).isFalse();
        assertThat(clipboard.hasPositionsCoordinates()).isFalse();

        clipboard.insertContent(createVariables());
        assertThat(clipboard.hasText()).isFalse();
        assertThat(clipboard.hasPositionsCoordinates()).isFalse();

        clipboard.insertContent("text", createPositionCoordinates());
        assertThat(clipboard.hasText()).isTrue();
        assertThat(clipboard.hasPositionsCoordinates()).isTrue();

        clipboard.dispose();
        assertThat(clipboard.hasText()).isFalse();
        assertThat(clipboard.hasPositionsCoordinates()).isFalse();
    }

    @Test
    public void getMultipleDataFromClipboardTest() {
        assertThat(clipboard.getText()).isNull();

        clipboard.insertContent("text");
        assertThat(clipboard.getText()).isEqualTo("text");

        clipboard.insertContent(createVariables());
        assertThat(clipboard.getText()).isNull();

    }

    private static Object createPositionCoordinates() {
        return new PositionCoordinateSerializer[] {
                new PositionCoordinateSerializer(new PositionCoordinate(null, 1, 2)),
                new PositionCoordinateSerializer(new PositionCoordinate(null, 3, 4)),
                new PositionCoordinateSerializer(new PositionCoordinate(null, 5, 6)),
        };
    }

    private static Object createKeywords() {
        final RobotSuiteFile model = createModel();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().toArray(new RobotKeywordDefinition[0]);
    }

    private static Object createCases() {
        final RobotSuiteFile model = createModel();
        return model.findSection(RobotCasesSection.class).get().getChildren().toArray(new RobotCase[0]);
    }

    private static Object createCalls() {
        final List<RobotKeywordCall> calls = new ArrayList<>();
        for (final RobotCase robotCase : (RobotCase[]) createCases()) {
            calls.addAll(robotCase.getChildren());
        }
        return calls.toArray(new RobotKeywordCall[0]);
    }

    private static Object createVariables() {
        final RobotSuiteFile model = createModel();
        return model.findSection(RobotVariablesSection.class).get().getChildren().toArray(new RobotVariable[0]);
    }

    private static Object createImportSettings() {
        final RobotSuiteFile model = createModel();
        return model.findSection(RobotSettingsSection.class).get().getImportSettings().toArray(new RobotSetting[0]);
    }

    private static Object createImportSettingsMixedWithKeywordCalls() {
        final List<RobotKeywordCall> calls = new ArrayList<>();
        for (final RobotSetting setting : (RobotSetting[]) createImportSettings()) {
            calls.add(setting);
        }
        for (final RobotCase robotCase : (RobotCase[]) createCases()) {
            calls.addAll(robotCase.getChildren());
        }
        return calls.toArray(new RobotKeywordCall[0]);
    }

    private static Object createImportSettingsMixedWithGeneral() {
        final List<RobotKeywordCall> calls = new ArrayList<>();
        for (final RobotSetting setting : (RobotSetting[]) createImportSettings()) {
            calls.add(setting);
        }
        for (final RobotSetting setting : (RobotSetting[]) createGeneralSettings()) {
            calls.add(setting);
        }
        return calls.toArray(new RobotKeywordCall[0]);
    }

    private static Object createMetadataSettings() {
        final RobotSuiteFile model = createModel();
        return model.findSection(RobotSettingsSection.class).get().getMetadataSettings().toArray(new RobotSetting[0]);
    }

    private static Object createMetadataSettingsMixedWithKeywordCalls() {
        final List<RobotKeywordCall> calls = new ArrayList<>();
        for (final RobotSetting setting : (RobotSetting[]) createMetadataSettings()) {
            calls.add(setting);
        }
        for (final RobotCase robotCase : (RobotCase[]) createCases()) {
            calls.addAll(robotCase.getChildren());
        }
        return calls.toArray(new RobotKeywordCall[0]);
    }

    private static Object createMetadataSettingsMixedWithGeneral() {
        final List<RobotKeywordCall> calls = new ArrayList<>();
        for (final RobotSetting setting : (RobotSetting[]) createMetadataSettings()) {
            calls.add(setting);
        }
        for (final RobotSetting setting : (RobotSetting[]) createGeneralSettings()) {
            calls.add(setting);
        }
        return calls.toArray(new RobotKeywordCall[0]);
    }

    private static Object createGeneralSettings() {
        final RobotSuiteFile model = createModel();
        return model.findSection(RobotSettingsSection.class).get().getGeneralSettings().toArray(new RobotSetting[0]);
    }

    private static Object createGeneralSettingsMixedWithKeywordCalls() {
        final List<RobotKeywordCall> calls = new ArrayList<>();
        for (final RobotSetting setting : (RobotSetting[]) createGeneralSettings()) {
            calls.add(setting);
        }
        for (final RobotCase robotCase : (RobotCase[]) createCases()) {
            calls.addAll(robotCase.getChildren());
        }
        return calls.toArray(new RobotKeywordCall[0]);
    }

    private static RobotSuiteFile createModel() {
        return new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw 1")
                .appendLine("  Log  1")
                .appendLine("kw 2")
                .appendLine("  Log  2")
                .appendLine("kw 3")
                .appendLine("  Log  3")
                .appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  Log  10")
                .appendLine("case 2")
                .appendLine("  Log  20")
                .appendLine("*** Variables ***")
                .appendLine("${var1}  1")
                .appendLine("@{var2}  1  2  3")
                .appendLine("&{var3}  a=1  b=2  c=3")
                .appendLine("*** Settings ***")
                .appendLine("Library  Collections")
                .appendLine("Resource  res1.robot")
                .appendLine("Metadata  data1")
                .appendLine("Metadata  data2")
                .appendLine("Metadata  data3")
                .appendLine("Test Timeout  50")
                .appendLine("Suite Setup  Log  123")
                .build();
    }
}
