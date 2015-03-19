package org.robotframework.ide.core.testData.model.parser.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.model.parser.result.ParseIssue.ParseIssueType;


/**
 * @author wypych
 * @see ParseResultBuilder
 * @serial 1.0
 */
public class TestParserResultBuilder {

    private ParseResultBuilder<String, String> builder;


    @Test
    public void test_noModelObject_but_also_noErrors_shouldSay_UnsuccessfulyProcessed() {
        ParseResult<String, String> result = builder.build();

        assertThat(result.getStatus()).isEqualTo(
                ParseStatus.UNSUCCESFULY_PARSED);
    }


    @Test
    public void test_noModelObject_but_also_noErrors_explicityResultOK() {
        ParseResult<String, String> result = builder.setExplicitlyStatus(
                ParseStatus.SUCCESSFULY_PARSED).build();

        assertThat(result.getStatus())
                .isEqualTo(ParseStatus.SUCCESSFULY_PARSED);
    }


    @Test
    public void test_modelObjectCreated_but_we_got_oneError_UnsuccessfulyProcessed() {
        String createdElement = "CREATED";
        String errorPosition = "POS_1";
        String errorMessage = "MSG_1";

        ParseResult<String, String> result = builder
                .createdElement(createdElement)
                .addError(errorPosition, errorMessage).build();

        assertThat(result.getCreatedElement()).isEqualTo(createdElement);
        assertThat(result.getERRORs()).hasSize(1).contains(
                new ParseIssue(ParseIssueType.ERROR, errorPosition,
                        errorMessage), atIndex(0));
        assertThat(result.getStatus()).isEqualTo(
                ParseStatus.UNSUCCESFULY_PARSED);
    }


    @Test
    public void test_modelObjectCreated_but_we_got_oneError_explicityResultOK() {
        String createdElement = "CREATED";
        String errorPosition = "POS_1";
        String errorMessage = "MSG_1";

        ParseResult<String, String> result = builder
                .setExplicitlyStatus(ParseStatus.SUCCESSFULY_PARSED)
                .createdElement(createdElement)
                .addError(errorPosition, errorMessage).build();

        assertThat(result.getCreatedElement()).isEqualTo(createdElement);
        assertThat(result.getERRORs()).hasSize(1).contains(
                new ParseIssue(ParseIssueType.ERROR, errorPosition,
                        errorMessage), atIndex(0));
        assertThat(result.getStatus())
                .isEqualTo(ParseStatus.SUCCESSFULY_PARSED);
    }


    @Test
    public void test_modelObjectCreated_oneWarn_noErrors_shouldSayOK() {
        String createdElement = "CREATED";
        String warnPosition = "POS_1";
        String warnMessage = "MSG_1";

        ParseResult<String, String> result = builder
                .setExplicitlyStatus(ParseStatus.SUCCESSFULY_PARSED)
                .createdElement(createdElement)
                .addWarning(warnPosition, warnMessage).build();

        assertThat(result.getCreatedElement()).isEqualTo(createdElement);
        assertThat(result.getERRORs()).isEmpty();
        assertThat(result.getWARNs()).hasSize(1).contains(
                new ParseIssue(ParseIssueType.WARN, warnPosition, warnMessage),
                atIndex(0));
        assertThat(result.getStatus())
                .isEqualTo(ParseStatus.SUCCESSFULY_PARSED);
    }


    @Before
    public void setUp() {
        builder = new ParseResultBuilder<String, String>();
    }


    @After
    public void tearDown() {
        builder = null;
    }
}
