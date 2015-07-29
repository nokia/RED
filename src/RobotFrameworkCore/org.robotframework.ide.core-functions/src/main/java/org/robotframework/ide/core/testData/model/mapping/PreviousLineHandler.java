package org.robotframework.ide.core.testData.model.mapping;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.TxtRobotFileParser.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class PreviousLineHandler {

    private final ElementsUtility utility;


    public PreviousLineHandler() {
        this.utility = new ElementsUtility();
    }

    private final Stack<ParsingState> storedStack = new Stack<>();


    public LineContinueType computeLineContinue(
            final Stack<ParsingState> parsingStates, boolean isNewLine,
            final RobotLine currentLine, final RobotToken currentToken) {
        LineContinueType continueType = LineContinueType.NONE;

        if (isNewLine) {
            if (currentToken.getTypes().contains(
                    RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
                ParsingState currentState = utility
                        .getCurrentStatus(parsingStates);
                if (currentState == ParsingState.SETTING_TABLE_INSIDE) {
                    if (utility.isTheFirstColumn(currentLine, currentToken)) {
                        continueType = LineContinueType.SETTING_TABLE_ELEMENT;
                    }
                }
            }
        }

        return continueType;
    }


    public boolean isSomethingToDo(final LineContinueType type) {
        return (type != LineContinueType.NONE);
    }


    public void restorePreviousStack(final LineContinueType continueType,
            final Stack<ParsingState> parsingStates,
            final RobotLine currentLine, final RobotToken currentToken) {
        if (continueType == LineContinueType.SETTING_TABLE_ELEMENT) {
            parsingStates.clear();
            removeLastCommentState(storedStack);
            parsingStates.addAll(storedStack);
        }
    }


    public void flushNew(final Stack<ParsingState> parsingStates) {
        clear();
        storedStack.addAll(parsingStates);
    }


    public void clear() {
        storedStack.clear();
    }

    public static enum LineContinueType {
        NONE, SETTING_TABLE_ELEMENT;
    }


    @VisibleForTesting
    protected void removeLastCommentState(
            final Stack<ParsingState> parsingStates) {
        for (int i = parsingStates.size() - 1; i >= 0; i--) {
            ParsingState state = parsingStates.get(i);
            if (state == ParsingState.COMMENT) {
                parsingStates.remove(i);
            } else {
                break;
            }
        }
    }
}
