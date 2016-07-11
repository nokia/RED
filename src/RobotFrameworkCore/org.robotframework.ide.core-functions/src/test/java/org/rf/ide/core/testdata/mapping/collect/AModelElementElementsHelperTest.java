/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.collect;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class AModelElementElementsHelperTest {

    @Test
    public void test_collect_forSingleInTwoPositionsToken() {
        RobotToken dec = new RobotToken();
        ScalarVariable var = new ScalarVariable("d2", dec, VariableScope.GLOBAL);
        RobotToken dec2 = new RobotToken();
        ScalarVariable var1 = new ScalarVariable("d1", dec2, VariableScope.GLOBAL);

        assertThat(AModelElementElementsHelper.collect(Arrays.asList(var, var1))).containsExactly(dec, dec2);
    }

    @Test
    public void test_collect_forEmptyModelFile() {
        ScalarVariable var = new ScalarVariable("d", null, VariableScope.GLOBAL);

        assertThat(AModelElementElementsHelper.collect(Arrays.asList(var))).isEmpty();
    }

    @Test
    public void test_collectFromAModel_forSingleToken() {
        RobotToken dec = new RobotToken();
        ScalarVariable var = new ScalarVariable("d", dec, VariableScope.GLOBAL);

        assertThat(AModelElementElementsHelper.collectFromAModel(var)).containsOnly(dec);
    }

    @Test
    public void test_collectFromAModel_forEmptyModelFile() {
        ScalarVariable var = new ScalarVariable("d", null, VariableScope.GLOBAL);

        assertThat(AModelElementElementsHelper.collectFromAModel(var)).isEmpty();
    }
}
