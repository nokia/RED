package org.robotframework.ide.core.testData.text;

import java.util.LinkedList;
import java.util.List;


public class RobotToken {

    private LinearPosition startPos = new LinearPosition();
    private LinearPosition endPos = new LinearPosition();
    private StringBuilder text = new StringBuilder();
    private List<RobotToken> subTokens = new LinkedList<>();
    private RobotTokenType type = RobotTokenType.UNKNOWN;


    public RobotToken(RobotTokenType type) {
        this.type = type;
    }


    public LinearPosition getStartPos() {
        return startPos;
    }


    public void setStartPos(LinearPosition startPos) {
        this.startPos = startPos;
    }


    public LinearPosition getEndPos() {
        return endPos;
    }


    public void setEndPos(LinearPosition endPos) {
        this.endPos = endPos;
    }


    public StringBuilder getText() {
        StringBuilder toReturn = text;
        if (text.length() == 0) {
            for (RobotToken rt : subTokens) {
                toReturn.append(rt.getText());
            }
        }

        return toReturn;
    }


    public void setText(StringBuilder text) {
        this.text = text;
    }


    public RobotTokenType getType() {
        return type;
    }


    public void setType(RobotTokenType type) {
        this.type = type;
    }


    public void addNextSubToken(RobotToken rt) {
        addNextSubToken(rt, true);
    }


    public void addNextSubToken(RobotToken rt, boolean withSetPosition) {
        if (withSetPosition) {
            if (this.startPos.getLine() == -1) {
                this.startPos = rt.getStartPos();
            }
            this.endPos = rt.getEndPos();
        }

        subTokens.add(rt);
    }


    public List<RobotToken> getSubTokens() {
        return subTokens;
    }


    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        format(0, strBuilder);

        return strBuilder.toString();
    }


    private void format(int indent, StringBuilder strBuilder) {
        StringBuilder spaceBeforeText = new StringBuilder();
        spaceBeforeText.append('%').append(indent + 1).append('s');

        StringBuilder formattedText = new StringBuilder();
        formattedText
                .append(spaceBeforeText)
                .append("RobotToken[type := %s, text := '%s', startPos := %s, endPos := %s");

        strBuilder.append(String.format(formattedText.toString(), " ", type,
                getText(), startPos, endPos));

        if (!subTokens.isEmpty()) {
            strBuilder.append(
                    String.format("%n" + spaceBeforeText.toString(), ' '))
                    .append('[');
        }

        for (int i = 0; i < subTokens.size(); i++) {
            RobotToken rt = subTokens.get(i);
            rt.format(indent + 1, strBuilder);
        }

        if (!subTokens.isEmpty()) {
            strBuilder.append(
                    String.format("%n" + spaceBeforeText.toString(), ' '))
                    .append(']');
        } else {
            strBuilder.append("]").append(String.format("%n"));
        }
    }
}
