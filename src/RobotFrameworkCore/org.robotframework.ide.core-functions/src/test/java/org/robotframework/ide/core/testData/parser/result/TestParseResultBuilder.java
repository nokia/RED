package org.robotframework.ide.core.testData.parser.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.parser.result.ParseResult.ParseResultMessage;
import org.robotframework.ide.core.testData.parser.result.ParseResult.ParseResultMessage.ParseResultMessageType;


/**
 * 
 * @author wypych
 * @see ParseResultBuilder
 * @see ParseResult
 * @see ParseStatus
 */
public class TestParseResultBuilder {

    private ParseResultBuilder<String, String> resultBuilder;


    @Test
    public void test_parsingNoErrorsAndWarns_produceOutputNotExists() {
        // prepare
        String model = "model";

        // execute
        ParseResult<String, String> pr = resultBuilder.build();

        // check
        assertThat(pr.getStatus()).isEqualTo(ParseStatus.UNSUCCESSFULY_PARSED);
        assertThat(pr.getParserMessages()).isEmpty();
        assertThat(pr.getOriginalDataUsedForBuild()).isNull();
        assertThat(pr.getProducedElementModel()).isNull();
    }


    @Test
    public void test_parsingNoErrorsAndWarns_produceOutputExists() {
        // prepare
        String model = "model";

        // execute
        ParseResult<String, String> pr = resultBuilder.addProducedModelElement(
                model).build();

        // check
        assertThat(pr.getStatus()).isEqualTo(ParseStatus.SUCCESSFULY_PARSED);
        assertThat(pr.getParserMessages()).isEmpty();
        assertThat(pr.getOriginalDataUsedForBuild()).isNull();
        assertThat(pr.getProducedElementModel()).isEqualTo(model);
    }


    @Test
    public void test_parsingWarnError_produceOutputExists() {
        // prepare
        String warnLocalization = "line 23";
        String warnMessage = "bit sign incorrect";
        String model = "model";

        // execute
        ParseResult<String, String> pr = resultBuilder
                .addWarningMessage(warnLocalization, warnMessage)
                .addProducedModelElement(model).build();

        // check
        assertThat(pr.getStatus()).isEqualTo(ParseStatus.PARTIAL_PARSED);
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParseResultMessage(ParseResultMessageType.WARN,
                        warnMessage, warnLocalization), atIndex(0));
        assertThat(pr.getOriginalDataUsedForBuild()).isNull();
        assertThat(pr.getProducedElementModel()).isEqualTo(model);
    }


    @Test
    public void test_parsingWarnError_produceOutputNotExists() {
        // prepare
        String warnLocalization = "line 23";
        String warnMessage = "bit sign incorrect";
        // execute
        ParseResult<String, String> pr = resultBuilder.addWarningMessage(
                warnLocalization, warnMessage).build();

        // check
        assertThat(pr.getStatus()).isEqualTo(ParseStatus.UNSUCCESSFULY_PARSED);
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParseResultMessage(ParseResultMessageType.WARN,
                        warnMessage, warnLocalization), atIndex(0));
        assertThat(pr.getOriginalDataUsedForBuild()).isNull();
        assertThat(pr.getProducedElementModel()).isNull();
    }


    @Test
    public void test_parsingOneError_produceOutputNotExists() {
        // prepare
        String errorLocalization = "line 2";
        String errorMessage = "bit sign missing";
        // execute
        ParseResult<String, String> pr = resultBuilder.addErrorMessage(
                errorLocalization, errorMessage).build();

        // check
        assertThat(pr.getStatus()).isEqualTo(ParseStatus.UNSUCCESSFULY_PARSED);
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParseResultMessage(ParseResultMessageType.ERROR,
                        errorMessage, errorLocalization), atIndex(0));
        assertThat(pr.getOriginalDataUsedForBuild()).isNull();
        assertThat(pr.getProducedElementModel()).isNull();
    }


    @Test
    public void test_parsingOneError_produceOutputExists() {
        // prepare
        String errorLocalization = "line 2";
        String errorMessage = "bit sign missing";
        String module = "module_1";
        // execute
        ParseResult<String, String> pr = resultBuilder
                .addErrorMessage(errorLocalization, errorMessage)
                .addProducedModelElement(module).build();

        // check
        assertThat(pr.getStatus()).isEqualTo(ParseStatus.UNSUCCESSFULY_PARSED);
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParseResultMessage(ParseResultMessageType.ERROR,
                        errorMessage, errorLocalization), atIndex(0));
        assertThat(pr.getOriginalDataUsedForBuild()).isNull();
        assertThat(pr.getProducedElementModel()).isEqualTo(module);
    }


    @Test
    public void test_parsingStatusExplicitySet_oneErrorNoWarnings() {
        // prepare
        String errorLocalization = "line 2";
        String errorMessage = "bit sign missing";

        // execute
        ParseResult<String, String> pr = resultBuilder
                .addExplicityStatus(ParseStatus.SUCCESSFULY_PARSED)
                .addErrorMessage(errorLocalization, errorMessage).build();

        // check
        assertThat(pr.getStatus()).isEqualTo(ParseStatus.SUCCESSFULY_PARSED);
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParseResultMessage(ParseResultMessageType.ERROR,
                        errorMessage, errorLocalization), atIndex(0));
        assertThat(pr.getOriginalDataUsedForBuild()).isNull();
        assertThat(pr.getProducedElementModel()).isNull();
    }


    @Test
    public void test_parsingStatusExplicitySet_noErrors_oneWarning() {
        // prepare
        String warnLocalization = "line 23";
        String warnMessage = "field is not present";

        // execute
        ParseResult<String, String> pr = resultBuilder
                .addExplicityStatus(ParseStatus.SUCCESSFULY_PARSED)
                .addWarningMessage(warnLocalization, warnMessage).build();

        // check
        assertThat(pr.getStatus()).isEqualTo(ParseStatus.SUCCESSFULY_PARSED);
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParseResultMessage(ParseResultMessageType.WARN,
                        warnMessage, warnLocalization), atIndex(0));
        assertThat(pr.getOriginalDataUsedForBuild()).isNull();
        assertThat(pr.getProducedElementModel()).isNull();
    }


    @Before
    public void setUp() {
        resultBuilder = new ParseResultBuilder<String, String>();
    }


    @After
    public void tearDown() {
        // for Garbage Collector helps
        resultBuilder = null;
    }
}
