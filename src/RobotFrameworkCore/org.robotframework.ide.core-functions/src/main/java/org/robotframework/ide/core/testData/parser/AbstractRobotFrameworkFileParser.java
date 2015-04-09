package org.robotframework.ide.core.testData.parser;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.model.table.IRobotSectionTable;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariablesTable;
import org.robotframework.ide.core.testData.parser.result.ParseResult;


/**
 * Parser for single Robot Framework file.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputFormatType>
 *            format of input data i.e. byte buffer or xml
 * @see IParsePositionMarkable
 */
public abstract class AbstractRobotFrameworkFileParser<InputFormatType extends IParsePositionMarkable> {

    protected final ITestDataParserProvider<InputFormatType> parsersProvider;


    /**
     * @param parsersProvider
     *            providers of parsers of table for current input format
     * @throws MissingParserException
     *             in case at least one of table parser is not declared
     * @throws IllegalArgumentException
     *             when parsers provider is null
     */
    public AbstractRobotFrameworkFileParser(
            final ITestDataParserProvider<InputFormatType> parsersProvider)
            throws MissingParserException, IllegalArgumentException {
        if (parsersProvider == null) {
            throw new IllegalArgumentException("Parsers provider is null.");
        }
        checkParsersProvider(parsersProvider);
        this.parsersProvider = parsersProvider;
    }


    private void checkParsersProvider(
            final ITestDataParserProvider<InputFormatType> parsersProvider)
            throws MissingParserException {
        List<IRobotSectionTable> missingTablesParsers = new LinkedList<IRobotSectionTable>();
        if (parsersProvider.getSettingsTableParser() == null) {
            missingTablesParsers.add(new SettingTable());
        }

        if (parsersProvider.getKeywordsTableParser() == null) {
            missingTablesParsers.add(new KeywordTable());
        }

        if (parsersProvider.getTestCasesTableParser() == null) {
            missingTablesParsers.add(new TestCaseTable());
        }

        if (parsersProvider.getVariablesTableParser() == null) {
            missingTablesParsers.add(new VariablesTable());
        }

        if (!missingTablesParsers.isEmpty()) {
            throw new MissingParserException(missingTablesParsers);
        }
    }


    /**
     * Handle logic related to split current data format to more significant
     * part of data in case it is required. It should use parsers provided for
     * each section to split correctly logic for each part. Implementators of
     * this method should do not take too much effort for validation part - it
     * should be just simple parse what we can get.
     * 
     * @param testData
     *            original data get from file it could by bytes or even xml
     *            nodes
     * @return result object, which contains errors and/or parsed object
     */
    public abstract ParseResult<InputFormatType, TestDataFile> parse(
            InputFormatType testData);
}
