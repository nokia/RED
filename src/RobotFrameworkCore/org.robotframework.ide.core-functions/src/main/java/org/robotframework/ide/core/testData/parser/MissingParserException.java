package org.robotframework.ide.core.testData.parser;

import java.util.Iterator;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.IRobotSectionTable;


/**
 * Indicate missing parser - in our condition it means that application was
 * incorrect started.
 * 
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class MissingParserException extends Exception {

    private static final long serialVersionUID = -4149973798847042529L;


    /**
     * @param missingParserForTables
     *            i.e. Keyword table
     */
    public MissingParserException(
            List<IRobotSectionTable> missingParserForTables) {
        super("Missing table" + getTimesLaterIfRequired(missingParserForTables)
                + " " + formatTableNames(missingParserForTables) + " parser"
                + getTimesLaterIfRequired(missingParserForTables));
    }


    private static String formatTableNames(
            final List<IRobotSectionTable> missingParserForTables) {
        StringBuilder str = new StringBuilder();
        str.append('[');
        Iterator<IRobotSectionTable> i = missingParserForTables.iterator();
        while(i.hasNext()) {
            str.append(i.next().getTableName());
            if (i.hasNext()) {
                str.append(',').append(' ');
            }
        }

        str.append(']');

        return str.toString();
    }


    private static String getTimesLaterIfRequired(
            final List<IRobotSectionTable> missingParserForTables) {
        return missingParserForTables.size() > 1 ? "s" : "";
    }
}
