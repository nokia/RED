/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ACreationOfThreeExecUnitsTest;

public class CreationOfThreeKeywordExecutablesWithOneStepTest extends ACreationOfThreeExecUnitsTest {

    public CreationOfThreeKeywordExecutablesWithOneStepTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Override
    public List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> getExecutablesAllWithNames() {
        final List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> units = createModelWithOneKeywordInside();
        for (int index = 0; index < 3; index++) {
            String name = "UserKeyword";
            if (index > 0) {
                name += (index + 1);
            }

            ((UserKeyword) units.get(index).getHolder()).getKeywordName().setText(name);
        }

        return units;
    }

    @Override
    public List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> getExecutablesTheFirstWithoutName() {
        final List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> units = createModelWithOneKeywordInside();
        for (int index = 1; index < 3; index++) {
            String name = "UserKeyword";
            name += (index + 1);
            ((UserKeyword) units.get(index).getHolder()).getKeywordName().setText(name);
        }

        return units;
    }

    private List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> createModelWithOneKeywordInside() {
        final List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> execUnits = new ArrayList<>(
                0);

        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");
        modelFile.includeKeywordTableSection();
        final KeywordTable keywordTable = modelFile.getKeywordTable();

        for (int index = 0; index < 3; index++) {
            final RobotToken keywordName = new RobotToken();
            final UserKeyword execUnit = new UserKeyword(keywordName);
            execUnit.addElement(new RobotExecutableRow<UserKeyword>());
            keywordTable.addKeyword(execUnit);
            execUnits.add(execUnit);
        }

        return execUnits;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setThreeExecUnitsWithOneLineEachOtherInsideCmpFile(
                convert("ExecActionAllCombinationsNoCommentLine"));

        return store;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithTheFirstWithoutName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setThreeExecUnitsWithOneLineEachOtherInsideCmpFile(
                convert("ExecActionAllCombinationsNoCommentLineMissingKeywordName"));

        return store;
    }

    private String convert(final String fileName) {
        return "keywords/exec/new/threeKws/oneExecInEachKw/" + fileName + "." + getExtension();
    }
}
