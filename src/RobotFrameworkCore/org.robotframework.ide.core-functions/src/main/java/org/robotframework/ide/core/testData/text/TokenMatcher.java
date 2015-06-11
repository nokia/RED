package org.robotframework.ide.core.testData.text;

import static org.robotframework.ide.core.testData.text.NamedElementsStore.CARRITAGE_RETURN;
import static org.robotframework.ide.core.testData.text.NamedElementsStore.LINE_FEED;

import java.util.List;

import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class TokenMatcher {

    public RobotToken match(TokenizatorOutput out, StringBuilder currentText,
            LinearPosition lp, RobotTokenType lineSeparator) {
        RobotToken rt = matchSpecialChars(out, currentText, lp);
        if (rt.getType() == RobotTokenType.UNKNOWN) {
            rt = matchSpecialWords(out, currentText, lp);
        }

        if (rt.getType() == RobotTokenType.UNKNOWN) {
            rt = matchLineEnd(currentText, lp);
        }

        if (rt.getType() == RobotTokenType.UNKNOWN) {
            rt = buildToken(currentText, lp, RobotTokenType.VALUE);
        }

        return rt;
    }


    private RobotToken matchSpecialChars(TokenizatorOutput out,
            StringBuilder currentText, LinearPosition lp) {
        RobotTokenType type = RobotTokenType.UNKNOWN;

        boolean shouldBeTreatAsSpecial = false;
        if (currentText.length() > 0) {
            char c = currentText.charAt(0);
            type = NamedElementsStore.SPECIAL_ROBOT_TOKENS_STORE.get(c);
            if (type == null) {
                type = NamedElementsStore.SPECIAL_ROBOT_SINGLE_CHAR_STORE
                        .get(c);
                if (type == null) {
                    type = RobotTokenType.UNKNOWN;
                } else {
                    shouldBeTreatAsSpecial = true;
                }
            }
        }

        RobotToken rt = buildToken(currentText, lp, type);
        if (shouldBeTreatAsSpecial) {
            out.addNextIndexOfSpecial(rt);
        }

        if (type == RobotTokenType.PIPE || type == RobotTokenType.SPACE
                || type == RobotTokenType.TABULATOR) {
            out.addNextIndexOfSeparator(rt);
        }

        return rt;
    }


    private RobotToken matchSpecialWords(TokenizatorOutput out,
            StringBuilder currentText, LinearPosition lp) {

        boolean isSpecialWord = false;
        String text = currentText.toString().toLowerCase().intern();
        RobotTokenType type = NamedElementsStore.SPECIAL_WORDS_STORE.get(text);
        if (type == null) {
            type = RobotTokenType.UNKNOWN;
        } else {
            isSpecialWord = true;
        }

        RobotToken rt = buildToken(currentText, lp, type);
        if (isSpecialWord) {
            out.addNextIndexOfSpecial(rt);
        }

        return rt;
    }


    private RobotToken matchLineEnd(StringBuilder currentText, LinearPosition lp) {
        RobotToken rt = new RobotToken(RobotTokenType.UNKNOWN);

        if (currentText.length() == 1) {
            char c = currentText.charAt(0);
            if (c == CARRITAGE_RETURN || c == LINE_FEED) {
                rt = buildEndOfLineToken(lp.getLine(), lp.getColumn(),
                        currentText);
            }
        } else if (currentText.length() == 2) {
            char c = currentText.charAt(0);
            if (c == CARRITAGE_RETURN) {
                rt = buildEndOfLineToken(lp.getLine(), lp.getColumn(),
                        currentText);
            }
        }

        return rt;
    }


    public RobotTokenType matchLineSeparator(List<RobotToken> tokens,
            StringBuilder currentText, LinearPosition linearPosition,
            int nearestStartLineToken) {
        RobotTokenType type = RobotTokenType.UNKNOWN;
        int pipeOrOtherSign = nearestStartLineToken + 1;
        if (pipeOrOtherSign < tokens.size()) {
            RobotToken theFirstTokenAfterStartLine = tokens
                    .get(pipeOrOtherSign);
            if (theFirstTokenAfterStartLine.getType() == RobotTokenType.PIPE) {
                if (pipeOrOtherSign + 3 == tokens.size()) {
                    RobotTokenType theSecondTokenType = tokens.get(
                            pipeOrOtherSign + 1).getType();
                    if (theSecondTokenType == RobotTokenType.SPACE
                            || theSecondTokenType == RobotTokenType.TABULATOR) {
                        type = RobotTokenType.PIPE_SEPARATOR;
                    } else {
                        type = RobotTokenType.DOUBLE_SPACE_OR_TAB_SEPARATOR;
                    }
                }
            } else {
                type = RobotTokenType.DOUBLE_SPACE_OR_TAB_SEPARATOR;
            }
        }

        if (type != RobotTokenType.UNKNOWN) {
            RobotToken startLineToken = tokens.get(nearestStartLineToken);
            RobotToken separatorForLine = new RobotToken(type);
            separatorForLine.setStartPos(startLineToken.getStartPos());
            separatorForLine.setEndPos(startLineToken.getEndPos());
            startLineToken.addNextSubToken(separatorForLine, false);
        }

        return type;
    }


    private RobotToken buildToken(StringBuilder currentText, LinearPosition lp,
            RobotTokenType type) {
        RobotToken rt;
        rt = new RobotToken(type);
        rt.setStartPos(lp);
        LinearPosition endLp = new LinearPosition(lp.getLine(), lp.getColumn()
                + currentText.length());
        rt.setEndPos(endLp);
        rt.setText(currentText);
        return rt;
    }


    private RobotToken buildEndOfLineToken(int currentLine, int currentColumn,
            StringBuilder text) {
        RobotToken rt = new RobotToken(RobotTokenType.END_OF_LINE);
        LinearPosition startLp = new LinearPosition(currentLine, currentColumn);
        rt.setStartPos(startLp);
        LinearPosition endLp = new LinearPosition(currentLine, currentColumn
                + text.length());
        rt.setEndPos(endLp);
        rt.setText(text);

        return rt;
    }
}
