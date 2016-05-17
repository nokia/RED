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
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ACreationOfThreeExecUnitsTest;

public abstract class ACreationOfThreeKeywordExecutablesWithOneStepTest extends ACreationOfThreeExecUnitsTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//exec//new//threeKeywords//oneExecInEveryKeyword//";

    private final String extension;

    public ACreationOfThreeKeywordExecutablesWithOneStepTest(final String extension) {
        this.extension = extension;
    }

    @Override
    public List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> getExecutablesAllWithNames() {
        List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> units = createModelWithOneKeywordInside();
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
        List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> units = createModelWithOneKeywordInside();
        for (int index = 1; index < 3; index++) {
            String name = "UserKeyword";
            name += (index + 1);
            ((UserKeyword) units.get(index).getHolder()).getKeywordName().setText(name);
        }

        return units;
    }

    private List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> createModelWithOneKeywordInside() {
        List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> execUnits = new ArrayList<>(
                0);

        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        for (int index = 0; index < 3; index++) {
            RobotToken keywordName = new RobotToken();
            UserKeyword execUnit = new UserKeyword(keywordName);
            execUnit.addKeywordExecutionRow(new RobotExecutableRow<UserKeyword>());
            keywordTable.addKeyword(execUnit);
            execUnits.add(execUnit);
        }

        return execUnits;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setThreeExecUnitsWithOneLineEachOtherInsideCmpFile(
                convert("KeywordExecutionActionWithAllCombinationsNoCommentLine"));

        return store;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithTheFirstWithoutName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setThreeExecUnitsWithOneLineEachOtherInsideCmpFile(
                convert("KeywordExecutionActionWithAllCombinationsNoCommentLineWithoutOneKeywordName"));

        return store;
    }

    public String convert(final String fileName) {
        return PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
    }

    public String getExtension() {
        return extension;
    }
}
