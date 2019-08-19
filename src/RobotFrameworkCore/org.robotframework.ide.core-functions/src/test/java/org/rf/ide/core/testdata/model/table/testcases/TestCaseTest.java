/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.testcases;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TestCaseTest {

    @Test
    public void templateKeywordIsReturnedFromLocalSetting() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        final TestCaseTable table = new TestCaseTable(file);
        final TestCase test = new TestCase(RobotToken.create("case"));
        test.setParent(table);

        final LocalSetting<TestCase> template = test.newTemplate(0);
        template.addToken("keyword");
        template.addToken("to");
        template.addToken("use");

        assertThat(test.getTemplateKeywordName()).hasValue("keyword to use");
    }

    @Test
    public void templateKeywordIsReturnedFromLocalSetting_evenWhenGlobalIsDefined() {
        final TestTemplate globalTemplate = new TestTemplate(RobotToken.create("TestCase Template"));
        globalTemplate.setKeywordName("global kw");

        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        file.includeSettingTableSection();
        file.getSettingTable().addTestTemplate(globalTemplate);
        final TestCaseTable table = new TestCaseTable(file);
        final TestCase test = new TestCase(RobotToken.create("test"));
        test.setParent(table);

        final LocalSetting<TestCase> template = test.newTemplate(0);
        template.addToken("keyword");

        assertThat(test.getTemplateKeywordName()).hasValue("keyword");
    }

    @Test
    public void templateKeywordIsReturnedFromSettingsTable_whenThereIsNoLocalTemplate() {
        final TestTemplate globalTemplate = new TestTemplate(RobotToken.create("TestCase Template"));
        globalTemplate.setKeywordName("global kw");

        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        file.includeSettingTableSection();
        file.getSettingTable().addTestTemplate(globalTemplate);
        final TestCaseTable table = new TestCaseTable(file);
        final TestCase test = new TestCase(RobotToken.create("test"));
        test.setParent(table);

        assertThat(test.getTemplateKeywordName()).hasValue("global kw");
    }

    @Test
    public void templateKeywordIsNotReturned_whenTemplatesAreDuplicated() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        final TestCaseTable table = new TestCaseTable(file);
        final TestCase test = new TestCase(RobotToken.create("test"));
        test.setParent(table);

        final LocalSetting<TestCase> template1 = test.newTemplate(0);
        template1.addToken("keyword1");
        final LocalSetting<TestCase> template2 = test.newTemplate(1);
        template2.addToken("keyword2");

        assertThat(test.getTemplateKeywordName()).isEmpty();
    }

    @Test
    public void templateKeywordIsNotReturned_whenGlobalIsDefinedButLocalCancelsIt() {
        final TestTemplate globalTemplate = new TestTemplate(RobotToken.create("TestCase Template"));
        globalTemplate.setKeywordName("global kw");

        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        file.includeSettingTableSection();
        file.getSettingTable().addTestTemplate(globalTemplate);
        final TestCaseTable table = new TestCaseTable(file);
        final TestCase test = new TestCase(RobotToken.create("test"));
        test.setParent(table);

        final LocalSetting<TestCase> template = test.newTemplate(0);
        template.addToken("NONE");

        assertThat(test.getTemplateKeywordName()).isEmpty();
    }
}
