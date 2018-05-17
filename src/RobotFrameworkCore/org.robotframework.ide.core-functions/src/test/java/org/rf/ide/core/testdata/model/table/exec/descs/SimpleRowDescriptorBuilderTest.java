/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class SimpleRowDescriptorBuilderTest {

    @Test
    public void test_ifVariableAreCorrectlyRecognized_beforeItWillBeFlushToDisc() {
        // given
        final RobotFileOutput out = new RobotFileOutput(RobotVersion.from("3.0"));
        out.setProcessedFile(new File("robot.robot"));
        final RobotFile model = new RobotFile(out);
        model.includeKeywordTableSection();
        final KeywordTable keywordTable = model.getKeywordTable();
        final UserKeyword userKeyword = keywordTable.createUserKeyword("dbfoo");

        final RobotExecutableRow<UserKeyword> oneRow = new RobotExecutableRow<>();
        final RobotToken createdScalar = RobotToken.create("${c}");
        oneRow.setAction(createdScalar);

        final RobotToken action = RobotToken.create("Set Variable");
        oneRow.addArgument(action);
        final RobotToken settedText = RobotToken.create("foobar");
        oneRow.addArgument(settedText);
        userKeyword.addElement(oneRow);

        // execute
        final IExecutableRowDescriptor<UserKeyword> lineDesc = oneRow.buildLineDescription();

        // verify
        assertThat(oneRow.isExecutable()).isTrue();
        assertThat(lineDesc.getAction().getToken().getText()).isEqualTo(action.getText());
        assertThat(lineDesc.getCreatedVariables()).hasSize(1);
        assertThat(lineDesc.getCreatedVariables().get(0).asToken().getText()).isEqualTo(createdScalar.getText());
        assertThat(lineDesc.getKeywordArguments()).hasSize(1);
        assertThat(lineDesc.getKeywordArguments().get(0).getText()).isEqualTo(settedText.getText());
    }
}
