package org.robotframework.ide.core.testData.parser;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.TestDataFile;
import org.robotframework.ide.core.testData.parser.result.ParseResult;


/**
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class RobotFrameworkFileParser<InputFormatType extends IParsePositionMarkable> {

    private final ITestDataParserProvider<InputFormatType> tableParsingProviders;


    /**
     * @param tableParsingProviders
     *            belongs to input format provider of tables parsers
     * @throws MissingTableParserException
     *             in case required by parser and declared by
     *             {@link ITestDataParserProvider} table parser is missing.
     * @throws IllegalArgumentException
     *             in case {@code tableParsingProviders} is null
     */
    public RobotFrameworkFileParser(
            final ITestDataParserProvider<InputFormatType> tableParsingProviders)
            throws MissingTableParserException {
        preCheckParsingProvider(tableParsingProviders);
        this.tableParsingProviders = tableParsingProviders;
    }


    private void preCheckParsingProvider(
            final ITestDataParserProvider<InputFormatType> tableDataParserProvider)
            throws MissingTableParserException {
        if (tableDataParserProvider != null) {
            List<String> missingTablesParsers = reportMissingParsers(tableDataParserProvider);

            if (!missingTablesParsers.isEmpty()) {
                throw new MissingTableParserException(missingTablesParsers);
            }
        } else {
            throw new IllegalArgumentException(
                    "Parameter tableDataParserProvider is null.");
        }
    }

    /**
     * Indicate that one or more of parsers for parsing table is missing
     * 
     * @author wypych
     * @serial RobotFramework 2.8.6
     * @serial 1.0
     * 
     */
    public static class MissingTableParserException extends Exception {

        private static final long serialVersionUID = 821244880500019131L;


        /**
         * @param missingTablesParsers
         */
        public MissingTableParserException(List<String> missingTablesParsers) {
            super("Tables do not have any parsers declared "
                    + missingTablesParsers);
        }
    }


    private List<String> reportMissingParsers(
            final ITestDataParserProvider<InputFormatType> tableDataParserProvider) {
        List<String> missingTableNames = new LinkedList<String>();
        if (tableParsingProviders.getKeywordsTableParser() == null) {
            missingTableNames.add("keywords table");
        }

        if (tableParsingProviders.getSettingsTableParser() == null) {
            missingTableNames.add("setting table");
        }

        if (tableParsingProviders.getTestCasesTableParser() == null) {
            missingTableNames.add("test case table");
        }

        if (tableParsingProviders.getVariablesTableParser() == null) {
            missingTableNames.add("variables table");
        }

        return missingTableNames;
    }


    /**
     * 
     * @param fileContent
     * @return
     */
    public ParseResult<InputFormatType, TestDataFile> parse(
            final InputFormatType fileContent) {
        TestDataFile mappedFile = new TestDataFile();
        ParserResultBuilder<InputFormatType, TestDataFile> resultBuilder = new ParserResultBuilder<InputFormatType, TestDataFile>();
        resultBuilder.addProducedModelElement(mappedFile);

        return resultBuilder.build();
    }
}
