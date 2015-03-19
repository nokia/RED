package org.robotframework.ide.core.testData.model.parser.result;

import java.util.LinkedList;
import java.util.List;


/**
 * Holder of parsing process final result.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @see ParseResultBuilder
 * @param <InputType>
 *            {{@link #dataLeft} will be also this type
 * @param <OutputType>
 */
public class ParseResult<InputType, OutputType> {

    private final ParseStatus status;

    private OutputType createdElement = null;
    @SuppressWarnings("unused")
    private final InputType originalTestData;
    private InputType dataLeft;

    private final List<ParseIssue> warns = new LinkedList<ParseIssue>();
    private final List<ParseIssue> errors = new LinkedList<ParseIssue>();


    /**
     * @param originalTestData
     *            data get at the beginning
     * 
     * @serial 1.0
     */
    public ParseResult(InputType originalTestData, ParseStatus status) {
        this.originalTestData = originalTestData;
        this.status = status;
    }


    /**
     * here you can put information about errors or warns found during
     * processing of data
     * 
     * @param issue
     * 
     * @serial 1.0
     */
    public void addIssue(ParseIssue issue) {
        if (issue.getType() == ParseIssue.ParseIssueType.WARN) {
            warns.add(issue);
        } else if (issue.getType() == ParseIssue.ParseIssueType.ERROR) {
            errors.add(issue);
        }
    }


    /**
     * @return true if {@link #status} is {@link ParseStatus#SUCCESSFULY_PARSED}
     * 
     * @serial 1.0
     */
    public boolean wasParsed() {
        return status == ParseStatus.SUCCESSFULY_PARSED;
    }


    /**
     * @return explicitly returns status of parsing process
     * 
     * @serial 1.0
     */
    public ParseStatus getStatus() {
        return status;
    }


    /**
     * @param createdElement
     *            model element mapped
     * 
     * @serial 1.0
     */
    public void setCreatedElement(OutputType createdElement) {
        this.createdElement = createdElement;
    }


    /**
     * @return model element mapped
     * 
     * @serial 1.0
     */
    public OutputType getCreatedElement() {
        return createdElement;
    }


    /**
     * @return all warnings
     * 
     * @serial 1.0
     */
    public List<ParseIssue> getWARNs() {
        return warns;
    }


    /**
     * @return all errors
     * 
     * @serial 1.0
     */
    public List<ParseIssue> getERRORs() {
        return errors;
    }


    /**
     * @return part of data not processed by parser
     * 
     * @serial 1.0
     */
    public InputType getDataLeft() {
        return dataLeft;
    }


    /**
     * @param dataLeft
     *            part of data not processed by parser
     * 
     * @serial 1.0
     */
    public void setDataLeft(InputType dataLeft) {
        this.dataLeft = dataLeft;
    }
}
