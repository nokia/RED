package org.robotframework.ide.core.testData.model.parser.result;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.parser.result.ParseIssue.ParseIssueType;


/**
 * Helper builder for create coherent {@link ParseResult} object
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 */
public class ParseResultBuilder<InputType, OutputType> {

    private ParseStatus status = ParseStatus.NOT_STARTED;

    private OutputType createdElement;
    private InputType originalTestData;
    private InputType dataLeft;
    private boolean wasError = false;
    private boolean userSetExplicityResult = false;
    private final List<ParseIssue> issues = new LinkedList<ParseIssue>();


    /**
     * Please note that computation of result base of errors and
     * {@link #createdElement} if is not null, will be not performed
     * 
     * @param status
     * @return
     * @serial 1.0
     */
    public ParseResultBuilder<InputType, OutputType> setExplicitlyStatus(
            ParseStatus status) {
        this.userSetExplicityResult = true;
        this.status = status;
        return this;
    }


    /**
     * 
     * @param createdElement
     * @return
     */
    public ParseResultBuilder<InputType, OutputType> createdElement(
            OutputType createdElement) {
        this.createdElement = createdElement;
        return this;
    }


    /**
     * 
     * @param dataLeft
     * @return
     */
    public ParseResultBuilder<InputType, OutputType> dataLeft(InputType dataLeft) {
        this.dataLeft = dataLeft;
        return this;
    }


    /**
     * 
     * @param position
     * @param message
     * @return
     */
    public ParseResultBuilder<InputType, OutputType> addWarning(
            String position, String message) {
        this.issues.add(new ParseIssue(ParseIssueType.WARN, position, message));
        return this;
    }


    /**
     * 
     * @param position
     * @param message
     * @return
     */
    public ParseResultBuilder<InputType, OutputType> addError(String position,
            String message) {
        this.wasError = true;
        this.issues
                .add(new ParseIssue(ParseIssueType.ERROR, position, message));
        return this;
    }


    /**
     * 
     * @return
     */
    public ParseResult<InputType, OutputType> build() {

        ParseResult<InputType, OutputType> result = new ParseResult<InputType, OutputType>(
                originalTestData, computeStatus());
        result.setCreatedElement(createdElement);
        result.setDataLeft(dataLeft);
        for (ParseIssue issue : issues) {
            result.addIssue(issue);
        }

        return result;
    }


    private ParseStatus computeStatus() {
        ParseStatus computeStatus;
        if (userSetExplicityResult) {
            computeStatus = status;
        } else {
            if (wasError || createdElement == null) {
                computeStatus = ParseStatus.UNSUCCESFULY_PARSED;
            } else {
                computeStatus = ParseStatus.SUCCESSFULY_PARSED;
            }
        }

        return computeStatus;
    }
}
