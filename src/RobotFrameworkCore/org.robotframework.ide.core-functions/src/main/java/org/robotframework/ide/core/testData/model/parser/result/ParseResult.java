package org.robotframework.ide.core.testData.model.parser.result;

import java.util.LinkedList;
import java.util.List;


/**
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputType>
 * @param <OutputType>
 */
public class ParseResult<InputType, OutputType> {

    private final ParseStatus status = ParseStatus.NOT_STARTED;

    private OutputType createdElement = null;
    @SuppressWarnings("unused")
    private final InputType originalTestData;

    private final List<ParseIssue> warns = new LinkedList<ParseIssue>();
    private final List<ParseIssue> errors = new LinkedList<ParseIssue>();


    public ParseResult(InputType originalTestData) {
        this.originalTestData = originalTestData;
    }


    public void addIssue(ParseIssue issue) {
        if (issue.getType() == ParseIssue.ParseIssueType.WARN) {
            warns.add(issue);
        } else if (issue.getType() == ParseIssue.ParseIssueType.ERROR) {
            errors.add(issue);
        }
    }


    public boolean wasParsed() {
        return status == ParseStatus.SUCCESSFULY_PARSED;
    }


    public ParseStatus getStatus() {
        return status;
    }


    public void setCreatedElement(OutputType createdElement) {
        this.createdElement = createdElement;
    }


    public OutputType getCreatedElement() {
        return createdElement;
    }


    public List<ParseIssue> getWARNs() {
        return warns;
    }


    public List<ParseIssue> getERRORs() {
        return errors;
    }
}
