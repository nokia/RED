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
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.InOrder;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class VariablesTokenCollectorTest {

    @Test
    public void test_noVariableTable_shouldReturn_empty() {
        // prepare
        final RobotFileOutput fileOut = mock(RobotFileOutput.class);
        final RobotFile modelFile = mock(RobotFile.class);
        final VariableTable table = mock(VariableTable.class);

        when(fileOut.getFileModel()).thenReturn(modelFile);
        when(modelFile.getVariableTable()).thenReturn(table);
        when(table.isPresent()).thenReturn(false);

        // execute
        List<RobotToken> collect = new VariablesTokenCollector().collect(fileOut);

        // verify
        assertThat(collect).isEmpty();

        InOrder order = inOrder(fileOut, modelFile, table);
        order.verify(fileOut, times(1)).getFileModel();
        order.verify(modelFile, times(1)).getVariableTable();
        order.verify(table, times(1)).isPresent();
        order.verifyNoMoreInteractions();
    }

    @Test
    public void test_noVariablesInTable_shouldReturn_empty() {
        // prepare
        final RobotFileOutput fileOut = mock(RobotFileOutput.class);
        final RobotFile modelFile = mock(RobotFile.class);
        final VariableTable table = mock(VariableTable.class);

        when(fileOut.getFileModel()).thenReturn(modelFile);
        when(modelFile.getVariableTable()).thenReturn(table);
        when(table.isPresent()).thenReturn(true);
        when(table.getHeaders()).thenReturn(new ArrayList<TableHeader<? extends ARobotSectionTable>>(0));
        when(table.getVariables()).thenReturn(new ArrayList<AVariable>(0));

        // execute
        List<RobotToken> collect = new VariablesTokenCollector().collect(fileOut);

        // verify
        assertThat(collect).isEmpty();

        InOrder order = inOrder(fileOut, modelFile, table);
        order.verify(fileOut, times(1)).getFileModel();
        order.verify(modelFile, times(1)).getVariableTable();
        order.verify(table, times(1)).isPresent();
        order.verify(table, times(1)).getHeaders();
        order.verify(table, times(1)).getVariables();
        order.verifyNoMoreInteractions();
    }

    @Test
    public void test_oneVariableInTable_logicCheck() {
        // prepare
        final RobotFileOutput fileOut = mock(RobotFileOutput.class);
        final RobotFile modelFile = mock(RobotFile.class);
        final VariableTable table = mock(VariableTable.class);
        @SuppressWarnings("unchecked")
        final TableHeader<VariableTable> header = mock(TableHeader.class);
        final List<TableHeader<? extends ARobotSectionTable>> headers = new ArrayList<>(1);
        headers.add(header);
        RobotToken headerDec = new RobotToken();
        when(header.getElementTokens()).thenReturn(Arrays.asList(headerDec));
        final AVariable var = mock(AVariable.class);
        RobotToken varDec = new RobotToken();
        when(var.getElementTokens()).thenReturn(Arrays.asList(varDec));

        when(fileOut.getFileModel()).thenReturn(modelFile);
        when(modelFile.getVariableTable()).thenReturn(table);
        when(table.isPresent()).thenReturn(true);

        when(table.getHeaders()).thenReturn(headers);
        when(table.getVariables()).thenReturn(Arrays.asList(var));

        // execute
        List<RobotToken> collect = new VariablesTokenCollector().collect(fileOut);

        // verify
        assertThat(collect).hasSize(2);
        assertThat(collect.get(0)).isSameAs(headerDec);
        assertThat(collect.get(1)).isSameAs(varDec);

        InOrder order = inOrder(fileOut, modelFile, table, header, var);
        order.verify(fileOut, times(1)).getFileModel();
        order.verify(modelFile, times(1)).getVariableTable();
        order.verify(table, times(1)).isPresent();
        order.verify(table, times(1)).getHeaders();
        order.verify(header, times(1)).getElementTokens();
        order.verify(table, times(1)).getVariables();
        order.verify(var, times(1)).getElementTokens();
        order.verifyNoMoreInteractions();
    }
}
