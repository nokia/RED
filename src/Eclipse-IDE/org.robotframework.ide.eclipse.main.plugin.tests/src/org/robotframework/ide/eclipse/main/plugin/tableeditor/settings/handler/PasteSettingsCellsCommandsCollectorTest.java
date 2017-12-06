/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;

public class PasteSettingsCellsCommandsCollectorTest {

    @Test
    public void testCollectPasteCommands_pasteThreeColumnsBetweenTwoMetadatas() {

        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Metadata  ver1  2.0  # comment1")
                .appendLine("Metadata  ver2  3.0  # comment2")
                .build();

        final RobotSettingsSection section = file.findSection(RobotSettingsSection.class).get();
        final List<RobotSetting> metadataSettings = section.getMetadataSettings();

        final int[] columnsToCopy = new int[] { 0, 1, 2 };
        final int[] rowsToCopy = new int[] { 0 };

        final RobotSetting[] settingsFromClipboard = getSettingsByRowNumbers(metadataSettings, rowsToCopy)
                .toArray(new RobotSetting[0]);
        final PositionCoordinateSerializer[] cellPositionsFromClipboard = createPositionsToCopy(columnsToCopy,
                rowsToCopy);

        final int[] columnsToPaste = new int[] { 0, 1, 2 };
        final int[] rowsToPaste = new int[] { 1 };

        final List<RobotElement> selectedSettings = getSettingsByRowNumbers(metadataSettings, rowsToPaste);
        final SelectionLayerAccessor selectionLayerAccessor = createSelectionToPaste(columnsToPaste, rowsToPaste, 3);

        final DummyPasteSettingsCellsCommandsCollector commandsCollector = createDummyCommandsCollector(
                settingsFromClipboard, cellPositionsFromClipboard);

        commandsCollector.collectPasteCommands(selectionLayerAccessor, selectedSettings, null);

        final List<PasteCommandsInput> pasteCommandsInputs = commandsCollector.getPasteCommandsInputs();
        verifyPasteCommandsInputSize(columnsToPaste, rowsToPaste, pasteCommandsInputs);
        verifySelectedSettingsInPasteCommandsInput(pasteCommandsInputs, selectedSettings, columnsToPaste);
        verifyPasteCommandsInputValues(pasteCommandsInputs, settingsFromClipboard, columnsToCopy.length,
                columnsToPaste.length - 1, rowsToPaste);
        assertEquals(settingsFromClipboard[0].getComment(),
                pasteCommandsInputs.get(pasteCommandsInputs.size() - 1).getValueToPaste());
    }

    @Test
    public void testCollectPasteCommands_pasteTwoColumnsFromTwoMetadatasToAnotherTwoMetadatas() {

        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Metadata  ver1  1.0  # comment1")
                .appendLine("Metadata  ver2  2.0  # comment2")
                .appendLine("Metadata  ver3  3.0  # comment3")
                .appendLine("Metadata  ver4  4.0  # comment4")
                .build();

        final RobotSettingsSection section = file.findSection(RobotSettingsSection.class).get();
        final List<RobotSetting> metadataSettings = section.getMetadataSettings();

        final int[] columnsToCopy = new int[] { 0, 1 };
        final int[] rowsToCopy = new int[] { 0, 1 };

        final RobotSetting[] settingsFromClipboard = getSettingsByRowNumbers(metadataSettings, rowsToCopy)
                .toArray(new RobotSetting[0]);
        final PositionCoordinateSerializer[] cellPositionsFromClipboard = createPositionsToCopy(columnsToCopy,
                rowsToCopy);

        final int[] columnsToPaste = new int[] { 0, 1 };
        final int[] rowsToPaste = new int[] { 2, 3 };

        final List<RobotElement> selectedSettings = getSettingsByRowNumbers(metadataSettings, rowsToPaste);
        final SelectionLayerAccessor selectionLayerAccessor = createSelectionToPaste(columnsToPaste, rowsToPaste, 3);

        final DummyPasteSettingsCellsCommandsCollector commandsCollector = createDummyCommandsCollector(
                settingsFromClipboard, cellPositionsFromClipboard);

        commandsCollector.collectPasteCommands(selectionLayerAccessor, selectedSettings, null);

        final List<PasteCommandsInput> pasteCommandsInputs = commandsCollector.getPasteCommandsInputs();
        verifyPasteCommandsInputSize(columnsToPaste, rowsToPaste, pasteCommandsInputs);
        verifySelectedSettingsInPasteCommandsInput(pasteCommandsInputs, selectedSettings, columnsToPaste);
        verifyPasteCommandsInputValues(pasteCommandsInputs, settingsFromClipboard, columnsToCopy.length,
                columnsToPaste.length, rowsToPaste);
    }

    @Test
    public void testCollectPasteCommands_pasteTwoColumnsFromTwoMetadatasToOneMetadata() {

        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Metadata  ver1  1.0  # comment1")
                .appendLine("Metadata  ver2  2.0  # comment2")
                .appendLine("Metadata  ver3  3.0  # comment3")
                .build();

        final RobotSettingsSection section = file.findSection(RobotSettingsSection.class).get();
        final List<RobotSetting> metadataSettings = section.getMetadataSettings();

        final int[] columnsToCopy = new int[] { 0, 1 };
        final int[] rowsToCopy = new int[] { 0, 1 };

        final RobotSetting[] settingsFromClipboard = getSettingsByRowNumbers(metadataSettings, rowsToCopy)
                .toArray(new RobotSetting[0]);
        final PositionCoordinateSerializer[] cellPositionsFromClipboard = createPositionsToCopy(columnsToCopy,
                rowsToCopy);

        final int[] columnsToPaste = new int[] { 0, 1 };
        final int[] rowsToPaste = new int[] { 2 };

        final List<RobotElement> selectedSettings = getSettingsByRowNumbers(metadataSettings, rowsToPaste);
        final SelectionLayerAccessor selectionLayerAccessor = createSelectionToPaste(columnsToPaste, rowsToPaste, 3);

        final DummyPasteSettingsCellsCommandsCollector commandsCollector = createDummyCommandsCollector(
                settingsFromClipboard, cellPositionsFromClipboard);

        commandsCollector.collectPasteCommands(selectionLayerAccessor, selectedSettings, null);

        final List<PasteCommandsInput> pasteCommandsInputs = commandsCollector.getPasteCommandsInputs();
        verifyPasteCommandsInputSize(columnsToPaste, rowsToPaste, pasteCommandsInputs);
        verifySelectedSettingsInPasteCommandsInput(pasteCommandsInputs, selectedSettings, columnsToPaste);
        verifyPasteCommandsInputValues(pasteCommandsInputs, settingsFromClipboard, columnsToCopy.length,
                columnsToPaste.length, rowsToPaste);
    }

    @Test
    public void testCollectPasteCommands_pasteOneColumnFromOneMetadataToTwoColumnsInTwoMetadatas() {

        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Metadata  ver1  1.0  # comment1")
                .appendLine("Metadata  ver2  2.0  # comment2")
                .appendLine("Metadata  ver3  3.0  # comment3")
                .build();

        final RobotSettingsSection section = file.findSection(RobotSettingsSection.class).get();
        final List<RobotSetting> metadataSettings = section.getMetadataSettings();

        final int[] columnsToCopy = new int[] { 0 };
        final int[] rowsToCopy = new int[] { 0 };

        final RobotSetting[] settingsFromClipboard = getSettingsByRowNumbers(metadataSettings, rowsToCopy)
                .toArray(new RobotSetting[0]);
        final PositionCoordinateSerializer[] cellPositionsFromClipboard = createPositionsToCopy(columnsToCopy,
                rowsToCopy);

        final int[] columnsToPaste = new int[] { 0, 1 };
        final int[] rowsToPaste = new int[] { 1, 2 };

        final List<RobotElement> selectedSettings = getSettingsByRowNumbers(metadataSettings, rowsToPaste);
        final SelectionLayerAccessor selectionLayerAccessor = createSelectionToPaste(columnsToPaste, rowsToPaste, 3);

        final DummyPasteSettingsCellsCommandsCollector commandsCollector = createDummyCommandsCollector(
                settingsFromClipboard, cellPositionsFromClipboard);

        commandsCollector.collectPasteCommands(selectionLayerAccessor, selectedSettings, null);

        final List<PasteCommandsInput> pasteCommandsInputs = commandsCollector.getPasteCommandsInputs();
        verifyPasteCommandsInputSize(columnsToPaste, rowsToPaste, pasteCommandsInputs);
        verifySelectedSettingsInPasteCommandsInput(pasteCommandsInputs, selectedSettings, columnsToPaste);
        verifyPasteCommandsInputValues(pasteCommandsInputs, settingsFromClipboard, columnsToCopy.length,
                columnsToPaste.length, rowsToPaste);
    }
    
    @Test
    public void testCollectPasteCommands_pasteOneColumnFromOneMetadataToOneColumnInTwoMetadatas() {

        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Metadata  ver1  1.0  # comment1")
                .appendLine("Metadata  ver2  2.0  # comment2")
                .appendLine("Metadata  ver3  3.0  # comment3")
                .build();

        final RobotSettingsSection section = file.findSection(RobotSettingsSection.class).get();
        final List<RobotSetting> metadataSettings = section.getMetadataSettings();

        final int[] columnsToCopy = new int[] { 0 };
        final int[] rowsToCopy = new int[] { 0 };

        final RobotSetting[] settingsFromClipboard = getSettingsByRowNumbers(metadataSettings, rowsToCopy)
                .toArray(new RobotSetting[0]);
        final PositionCoordinateSerializer[] cellPositionsFromClipboard = createPositionsToCopy(columnsToCopy,
                rowsToCopy);

        final int[] columnsToPaste = new int[] { 1 };
        final int[] rowsToPaste = new int[] { 0, 1 };

        final List<RobotElement> selectedSettings = getSettingsByRowNumbers(metadataSettings, rowsToPaste);
        final SelectionLayerAccessor selectionLayerAccessor = createSelectionToPaste(columnsToPaste, rowsToPaste, 3);

        final DummyPasteSettingsCellsCommandsCollector commandsCollector = createDummyCommandsCollector(
                settingsFromClipboard, cellPositionsFromClipboard);

        commandsCollector.collectPasteCommands(selectionLayerAccessor, selectedSettings, null);

        final List<PasteCommandsInput> pasteCommandsInputs = commandsCollector.getPasteCommandsInputs();
        verifyPasteCommandsInputSize(columnsToPaste, rowsToPaste, pasteCommandsInputs);
        verifySelectedSettingsInPasteCommandsInput(pasteCommandsInputs, selectedSettings, columnsToPaste);
        verifyPasteCommandsInputValues(pasteCommandsInputs, settingsFromClipboard, columnsToCopy.length,
                columnsToPaste.length, rowsToPaste);
    }

    private void verifyPasteCommandsInputSize(final int[] columnsToPaste, final int[] rowsToPaste,
            final List<PasteCommandsInput> pasteCommandsInputs) {
        assertTrue(pasteCommandsInputs.size() == (columnsToPaste.length * rowsToPaste.length));
    }

    private void verifyPasteCommandsInputValues(final List<PasteCommandsInput> pasteCommandsInputs,
            final RobotSetting[] settingsFromClipboard, final int argsToCopySize, final int argsToPasteSize,
            final int[] rowsToPaste) {
        int pasteCommandInputsShift = 0;
        int settingFromClipboardIndex = 0;
        for (int i = 0; i < rowsToPaste.length; i++) {
            final RobotSetting settingFromClipboard = settingsFromClipboard[settingFromClipboardIndex];
            if (settingFromClipboardIndex + 1 < settingsFromClipboard.length) {
                settingFromClipboardIndex++;
            }
            final List<String> arguments = settingFromClipboard.getArguments();
            int settingFromClipboardColumnIndex = 0;
            for (int j = 0; j < argsToPasteSize; j++) {
                assertEquals(arguments.get(settingFromClipboardColumnIndex),
                        pasteCommandsInputs.get(j + pasteCommandInputsShift).getValueToPaste());
                if (settingFromClipboardColumnIndex + 1 < argsToCopySize) {
                    settingFromClipboardColumnIndex++;
                }
            }
            pasteCommandInputsShift += argsToPasteSize;
        }
    }

    private void verifySelectedSettingsInPasteCommandsInput(final List<PasteCommandsInput> pasteCommandsInputs,
            final List<RobotElement> selectedSettings, final int[] columnsToPaste) {
        int pasteCommandInputsShift = 0;
        for (final RobotElement selectedSetting : selectedSettings) {
            verifySelectedSetting(pasteCommandsInputs.subList(pasteCommandInputsShift,
                    pasteCommandInputsShift + columnsToPaste.length), selectedSetting, columnsToPaste);
            pasteCommandInputsShift += columnsToPaste.length;
        }
    }

    private void verifySelectedSetting(final List<PasteCommandsInput> pasteCommandsInputs,
            final RobotElement selectedSetting, final int[] columnsToPaste) {
        for (int i = 0; i < pasteCommandsInputs.size(); i++) {
            final PasteCommandsInput pasteCommandsInput = pasteCommandsInputs.get(i);
            assertEquals(selectedSetting, pasteCommandsInput.getSelectedElement());
            assertTrue(columnsToPaste[i] == pasteCommandsInput.getSelectedElementColumnIndex());
        }
    }

    private List<RobotElement> getSettingsByRowNumbers(final List<RobotSetting> settings, final int[] rows) {
        final List<RobotElement> result = newArrayList();
        for (int i = 0; i < rows.length; i++) {
            result.add(settings.get(rows[i]));
        }
        return result;
    }

    private SelectionLayerAccessor createSelectionToPaste(final int[] columnsToPaste, final int[] rowsToPaste,
            final int columnsCount) {
        final SelectionLayer selectionLayer = mock(SelectionLayer.class);
        when(selectionLayer.getColumnCount()).thenReturn(columnsCount);
        final PositionCoordinate[] selectedPositions = new PositionCoordinate[columnsToPaste.length
                * rowsToPaste.length];
        int positionCounter = 0;
        for (int i = 0; i < columnsToPaste.length; i++) {
            for (int j = 0; j < rowsToPaste.length; j++) {
                selectedPositions[positionCounter] = new PositionCoordinate(null, columnsToPaste[i], rowsToPaste[j]);
                positionCounter++;
            }
        }
        when(selectionLayer.getSelectedCellPositions()).thenReturn(selectedPositions);

        return new SelectionLayerAccessor(null, selectionLayer, null);
    }

    private PositionCoordinateSerializer[] createPositionsToCopy(final int[] columnsToCopy, final int[] rowsToCopy) {
        final PositionCoordinateSerializer[] cellPositionsToCopy = new PositionCoordinateSerializer[columnsToCopy.length
                * rowsToCopy.length];
        int positionCounter = 0;
        for (int i = 0; i < columnsToCopy.length; i++) {
            for (int j = 0; j < rowsToCopy.length; j++) {
                cellPositionsToCopy[positionCounter] = new PositionCoordinateSerializer(
                        new PositionCoordinate(null, columnsToCopy[i], rowsToCopy[j]));
                positionCounter++;
            }
        }
        return cellPositionsToCopy;
    }

    private DummyPasteSettingsCellsCommandsCollector createDummyCommandsCollector(
            final RobotSetting[] settingsFromClipboard,
            final PositionCoordinateSerializer[] cellPositionsFromClipboard) {
        final DummyPasteSettingsCellsCommandsCollector commandsCollector = new DummyPasteSettingsCellsCommandsCollector();
        commandsCollector.setSettingsFromClipboard(settingsFromClipboard);
        commandsCollector.setCellPositionsFromClipboard(cellPositionsFromClipboard);
        return commandsCollector;
    }

    class DummyPasteSettingsCellsCommandsCollector extends PasteSettingsCellsCommandsCollector {

        private RobotSetting[] settingsFromClipboard;

        private PositionCoordinateSerializer[] cellPositionsFromClipboard;

        private final List<PasteCommandsInput> pasteCommandsInputs = newArrayList();

        @Override
        protected boolean hasPositionsCoordinatesInClipboard(final RedClipboard clipboard) {
            return true;
        }

        @Override
        protected PositionCoordinateSerializer[] getPositionsCoordinatesFromClipboard(final RedClipboard clipboard) {
            return cellPositionsFromClipboard;
        }

        @Override
        protected boolean hasRobotElementsInClipboard(final RedClipboard clipboard) {
            return true;
        }

        @Override
        protected RobotElement[] getRobotElementsFromClipboard(final RedClipboard clipboard) {
            return settingsFromClipboard;
        }

        @Override
        protected List<EditorCommand> collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
                final List<String> valuesToPaste, final int selectedElementColumnIndex, final int tableColumnsCount) {
            pasteCommandsInputs.add(new PasteCommandsInput(selectedElement, selectedElementColumnIndex, valuesToPaste));
            return new ArrayList<>();
        }

        public void setSettingsFromClipboard(final RobotSetting[] settingsFromClipboard) {
            this.settingsFromClipboard = settingsFromClipboard;
        }

        public void setCellPositionsFromClipboard(final PositionCoordinateSerializer[] cellPositionsFromClipboard) {
            this.cellPositionsFromClipboard = cellPositionsFromClipboard;
        }

        public List<PasteCommandsInput> getPasteCommandsInputs() {
            return pasteCommandsInputs;
        }

    }

    class PasteCommandsInput {

        private final RobotElement selectedElement;

        private final int selectedElementColumnIndex;

        private final List<String> valuesToPaste;

        public PasteCommandsInput(final RobotElement selectedElement, final int selectedElementColumnIndex,
                final List<String> valuesToPaste) {
            this.selectedElement = selectedElement;
            this.selectedElementColumnIndex = selectedElementColumnIndex;
            this.valuesToPaste = valuesToPaste;
        }

        public RobotElement getSelectedElement() {
            return selectedElement;
        }

        public int getSelectedElementColumnIndex() {
            return selectedElementColumnIndex;
        }

        public List<String> getValuesToPaste() {
            return valuesToPaste;
        }

        public String getValueToPaste() {
            return valuesToPaste.get(0);
        }

    }
}
