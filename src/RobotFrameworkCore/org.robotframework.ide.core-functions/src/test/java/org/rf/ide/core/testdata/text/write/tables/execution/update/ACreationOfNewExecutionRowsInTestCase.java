package org.rf.ide.core.testdata.text.write.tables.execution.update;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

/**
 * @author wypych
 */
public abstract class ACreationOfNewExecutionRowsInTestCase {

    public static final String PRETTY_NEW_DIR_LOCATION_NEW_UNITS = "testCases//exec//update//";

    private final String extension;

    public ACreationOfNewExecutionRowsInTestCase(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_givenTestCaseTableWithOneExecLine_whenAddNewExecutable_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS + "Input_OneTestCaseWithOneExecRow."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS + "Output_OneTestCaseWithOneExecRow."
                + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(0);
        final RobotExecutableRow<TestCase> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("LogMe"));
        executionRow.addArgument(RobotToken.create("arg1"));
        testCase.addTestExecutionRow(executionRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_givenTestCaseTableWithThreeExecLine_whenAddNewExecutableInTheMiddle_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS + "Input_OneTestCaseWithThreeExecRow."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS + "Output_OneTestCaseWithThreeExecRow."
                + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(0);

        final RobotExecutableRow<TestCase> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("Log_new"));
        executionRow.addArgument(RobotToken.create("arg_new"));
        testCase.addTestExecutionRow(executionRow, 2);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    @Test
    public void test_givenTestCaseTableWithThreeExecLine_whenAddNewExecutableAsLast_thenCheckIfTableIsCorrectlyDumped()
            throws Exception {
        // prepare
        final String inFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS + "Input_OneTestCaseWithThreeExecRowAddExecAsLast."
                + getExtension();
        final String outputFileName = PRETTY_NEW_DIR_LOCATION_NEW_UNITS
                + "Output_OneTestCaseWithThreeExecRowAddExecAsLast." + getExtension();
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());

        // test data prepare
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();
        final TestCase testCase = testCaseTable.getTestCases().get(0);

        final RobotExecutableRow<TestCase> executionRow = new RobotExecutableRow<>();
        executionRow.setAction(RobotToken.create("Log_new"));
        executionRow.addArgument(RobotToken.create("arg_new"));
        testCase.addTestExecutionRow(executionRow);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(outputFileName, modelFile);
    }

    public String getExtension() {
        return this.extension;
    }
}
