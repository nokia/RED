package org.robotframework.ide.core.testData.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.parser.result.MessageType;
import org.robotframework.ide.core.testData.parser.result.ParseProcessResult;
import org.robotframework.ide.core.testData.parser.result.ParseResult;
import org.robotframework.ide.core.testData.parser.result.ParserMessage;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * 
 * @author wypych
 * @see ParserResultBuilder#addDataConsumed(Object)
 * @see ParserResultBuilder#addErrorMessage(ByteBufferInputStream, String)
 * @see ParserResultBuilder#addInformationMessage(ByteBufferInputStream, String)
 * @see ParserResultBuilder#addParsingStatus(org.robotframework.ide.core.testData.parser.result.ParseProcessResult)
 * @see ParserResultBuilder#addProducedModelElement(Object)
 * @see ParserResultBuilder#addWarningMessage(ByteBufferInputStream, String)
 * @see ParserResultBuilder#build()
 */
public class TestParserResultBuilder {

    private ParserResultBuilder<ByteBufferInputStream, String> prb;


    @Test
    public void test_buildResult_noErrors_noWarns_outputObjectExists_result_SUCCESS() {
        // prepare
        String output = "out";

        prb.addProducedModelElement(output);

        // execute
        ParseResult<ByteBufferInputStream, String> pr = prb.build();

        // verify
        assertThat(pr).isNotNull();
        assertThat(pr.getDataConsumed()).isNull();
        assertThat(pr.getParserMessages()).isEmpty();
        assertThat(pr.getProducedModelElement()).isEqualTo(output);
        assertThat(pr.getResult()).isEqualTo(
                ParseProcessResult.PARSED_WITH_SUCCESS);
    }


    @Test
    public void test_buildResult_noErrors_oneWarns_outputObjectExists_result_PARTIAL_SUCCESS() {
        // prepare
        String warnLocalization = "in_the_middle";
        String warnMessage = "what?";
        String output = "out";

        prb.addWarningMessage(warnLocalization, warnMessage)
                .addProducedModelElement(output);

        // execute
        ParseResult<ByteBufferInputStream, String> pr = prb.build();

        // verify
        assertThat(pr).isNotNull();
        assertThat(pr.getDataConsumed()).isNull();
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParserMessage(MessageType.WARN, warnLocalization,
                        warnMessage), atIndex(0));
        assertThat(pr.getProducedModelElement()).isEqualTo(output);
        assertThat(pr.getResult())
                .isEqualTo(ParseProcessResult.PARTIAL_SUCCESS);
    }


    @Test
    public void test_buildResult_oneError_noWarns_outputObjectExists_result_FAILED() {
        // prepare
        String errorLocalization = "in_the_middle";
        String errorMessage = "what?";
        String output = "out";

        prb.addErrorMessage(errorLocalization, errorMessage)
                .addProducedModelElement(output);

        // execute
        ParseResult<ByteBufferInputStream, String> pr = prb.build();

        // verify
        assertThat(pr).isNotNull();
        assertThat(pr.getDataConsumed()).isNull();
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParserMessage(MessageType.ERROR, errorLocalization,
                        errorMessage), atIndex(0));
        assertThat(pr.getProducedModelElement()).isEqualTo(output);
        assertThat(pr.getResult()).isEqualTo(ParseProcessResult.FAILED);
    }


    @Test
    public void test_buildResult_oneError_noWarns_noOutputObject_result_FAILED() {
        // prepare
        String errorLocalization = "in_the_middle";
        String errorMessage = "what?";

        prb.addErrorMessage(errorLocalization, errorMessage);

        // execute
        ParseResult<ByteBufferInputStream, String> pr = prb.build();

        // verify
        assertThat(pr).isNotNull();
        assertThat(pr.getDataConsumed()).isNull();
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParserMessage(MessageType.ERROR, errorLocalization,
                        errorMessage), atIndex(0));
        assertThat(pr.getProducedModelElement()).isNull();
        assertThat(pr.getResult()).isEqualTo(ParseProcessResult.FAILED);
    }


    @Test
    public void test_buildResult_userSetStatus_noErrors_oneWarns_noOutputObject_resultAsSet() {
        // prepare
        ParseProcessResult ppr = ParseProcessResult.PARSED_WITH_SUCCESS;
        String warnLocalization = "in_the_middle";
        String warnMessage = "what?";

        prb.addWarningMessage(warnLocalization, warnMessage).addParsingStatus(
                ppr);

        // execute
        ParseResult<ByteBufferInputStream, String> pr = prb.build();

        // verify
        assertThat(pr).isNotNull();
        assertThat(pr.getDataConsumed()).isNull();
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParserMessage(MessageType.WARN, warnLocalization,
                        warnMessage), atIndex(0));
        assertThat(pr.getProducedModelElement()).isNull();
        assertThat(pr.getResult()).isEqualTo(ppr);
    }


    @Test
    public void test_buildResult_userSetStatus_noErrorExists_oneWarn_outputObjectExists_resultAsSet() {
        // prepare
        ParseProcessResult ppr = ParseProcessResult.PARSED_WITH_SUCCESS;
        String warnLocalization = "in_the_middle";
        String warnMessage = "what?";
        String output = "out";

        prb.addWarningMessage(warnLocalization, warnMessage)
                .addParsingStatus(ppr).addProducedModelElement(output);

        // execute
        ParseResult<ByteBufferInputStream, String> pr = prb.build();

        // verify
        assertThat(pr).isNotNull();
        assertThat(pr.getDataConsumed()).isNull();
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParserMessage(MessageType.WARN, warnLocalization,
                        warnMessage), atIndex(0));
        assertThat(pr.getProducedModelElement()).isEqualTo(output);
        assertThat(pr.getResult()).isEqualTo(ppr);

    }


    @Test
    public void test_buildResult_userSetStatus_oneErrorExists_noWarn_outputObjectExists_resultAsSet() {
        // prepare
        ParseProcessResult ppr = ParseProcessResult.PARTIAL_SUCCESS;
        String errorLocalization = "in_the_middle";
        String errorMessage = "what?";
        String output = "out";

        prb.addErrorMessage(errorLocalization, errorMessage)
                .addParsingStatus(ppr).addProducedModelElement(output);

        // execute
        ParseResult<ByteBufferInputStream, String> pr = prb.build();

        // verify
        assertThat(pr).isNotNull();
        assertThat(pr.getDataConsumed()).isNull();
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParserMessage(MessageType.ERROR, errorLocalization,
                        errorMessage), atIndex(0));
        assertThat(pr.getProducedModelElement()).isEqualTo(output);
        assertThat(pr.getResult()).isEqualTo(ppr);
    }


    @Test
    public void test_buildResult_userSetStatus_oneErrorExists_noWarns_noOutputObject_resultAsSet() {
        // prepare
        ParseProcessResult ppr = ParseProcessResult.PARTIAL_SUCCESS;
        String errorLocalization = "in_the_middle";
        String errorMessage = "what?";

        prb.addErrorMessage(errorLocalization, errorMessage).addParsingStatus(
                ppr);

        // execute
        ParseResult<ByteBufferInputStream, String> pr = prb.build();

        // verify
        assertThat(pr).isNotNull();
        assertThat(pr.getDataConsumed()).isNull();
        assertThat(pr.getParserMessages()).hasSize(1).contains(
                new ParserMessage(MessageType.ERROR, errorLocalization,
                        errorMessage), atIndex(0));
        assertThat(pr.getProducedModelElement()).isNull();
        assertThat(pr.getResult()).isEqualTo(ppr);
    }


    @Test
    public void test_buildResult_noErrorsAndWarns_noOutputObject_result_FAILED() {
        // execute
        ParseResult<ByteBufferInputStream, String> pr = prb.build();

        // verify
        assertThat(pr).isNotNull();
        assertThat(pr.getDataConsumed()).isNull();
        assertThat(pr.getParserMessages()).hasSize(0);
        assertThat(pr.getProducedModelElement()).isNull();
        assertThat(pr.getResult()).isEqualTo(ParseProcessResult.FAILED);
    }


    @Before
    public void setUp() {
        prb = new ParserResultBuilder<ByteBufferInputStream, String>();
    }


    @After
    public void tearDown() {
        prb = null;
    }
}
