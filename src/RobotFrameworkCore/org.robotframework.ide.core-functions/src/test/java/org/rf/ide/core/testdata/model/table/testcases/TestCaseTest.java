/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.testcases;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.execution.debug.contexts.ModelBuilder;

public class TestCaseTest {

    @Test
    public void templateKeywordIsReturnedFromLocalSettingInRf31() {
        final TestCase test = ModelBuilder.modelForFile(new RobotVersion(3, 1))
                .withTestCasesTable()
                .withTestCase("case")
                .withTemplate("keyword", "to", "use")
                .build()
                .getTestCaseTable()
                .getTestCases()
                .get(0);
        
        assertThat(test.getTemplateKeywordName()).hasValue("keyword to use");
    }

    @Test
    public void templateKeywordIsReturnedFromLocalSetting_evenWhenGlobalIsDefinedInRf31() {
        final TestCase test = ModelBuilder.modelForFile(new RobotVersion(3, 1))
                .withSettingsTable()
                .withTestTemplate("global kw")
                .withTestCasesTable()
                .withTestCase("test")
                .withTemplate("keyword")
                .build()
                .getTestCaseTable()
                .getTestCases()
                .get(0);

        assertThat(test.getTemplateKeywordName()).hasValue("keyword");
    }

    @Test
    public void templateKeywordIsReturnedFromSettingsTable_whenThereIsNoLocalTemplateInRf31() {
        final TestCase test = ModelBuilder.modelForFile(new RobotVersion(3, 1))
                .withSettingsTable()
                .withTestTemplate("global kw")
                .withTestCasesTable()
                .withTestCase("test")
                .build()
                .getTestCaseTable()
                .getTestCases()
                .get(0);

        assertThat(test.getTemplateKeywordName()).hasValue("global kw");
    }

    @Test
    public void templateKeywordIsNotReturned_whenTemplatesAreDuplicatedInRf31() {
        final TestCase test = ModelBuilder.modelForFile(new RobotVersion(3, 1))
                .withTestCasesTable()
                .withTestCase("test")
                .withTemplate("keyword1")
                .withTemplate("keyword2")
                .build()
                .getTestCaseTable()
                .getTestCases()
                .get(0);

        assertThat(test.getTemplateKeywordName()).isEmpty();
    }

    @Test
    public void templateKeywordIsNotReturned_whenGlobalIsDefinedButLocalCancelsItInRf31() {
        final TestCase test = ModelBuilder.modelForFile(new RobotVersion(3, 1))
                .withSettingsTable()
                .withTestTemplate("global kw")
                .withTestCasesTable()
                .withTestCase("test")
                .withTemplate("NONE")
                .build()
                .getTestCaseTable()
                .getTestCases()
                .get(0);

        assertThat(test.getTemplateKeywordName()).isEmpty();
    }

    @Test
    public void templateKeywordIsNotReturned_whenGlobalIsNoneInRf31() {
        final TestCase test = ModelBuilder.modelForFile(new RobotVersion(3, 1))
                .withSettingsTable()
                .withTestTemplate("NONE")
                .withTestCasesTable()
                .withTestCase("test")
                .build()
                .getTestCaseTable()
                .getTestCases()
                .get(0);

        assertThat(test.getTemplateKeywordName()).isEmpty();
    }

    @Test
    public void templateKeywordIsReturnedFromLocalSettingInRf32() {
        final TestCase test = ModelBuilder.modelForFile(new RobotVersion(3, 2))
                .withTestCasesTable()
                .withTestCase("case")
                .withTemplate("keyword")
                .build()
                .getTestCaseTable()
                .getTestCases()
                .get(0);

        assertThat(test.getTemplateKeywordName()).hasValue("keyword");
    }

    @Test
    public void templateKeywordIsReturnedFromGlobalSetting_whenLocalHasUnexpectedArgumentsInRf32() {
        final TestCase test = ModelBuilder.modelForFile(new RobotVersion(3, 2))
                .withSettingsTable()
                .withTestTemplate("global kw")
                .withTestCasesTable()
                .withTestCase("test")
                .withTemplate("keyword", "to", "us")
                .build()
                .getTestCaseTable()
                .getTestCases()
                .get(0);

        assertThat(test.getTemplateKeywordName()).hasValue("global kw");
    }
}
