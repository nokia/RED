/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class VariableDeclaration extends AContainerOperation {

    private final static Pattern COMPUTATION_PATTERN = Pattern
            .compile("((?!\\s).)+(\\s)*([+]|[-]|[*]|[/]|[:]|[>]|[<]|[=]|[&]|[%]|\\^|\\!|[|])+[=]*(\\s)*((?!\\s).)+");

    private final static Pattern NUMBER_PATTERN = Pattern.compile("^(//s)*([+]|[-])?(([0-9])+)([.]([0-9])+)?(//s)*$");

    private final static Pattern BINARY_NUMBER_PATTERN = Pattern.compile("^(//s)*0[b|B](0|1)+(//s)*$");

    private final static Pattern OCTAL_NUMBER_PATTERN = Pattern.compile("^(//s)*0[o|O][0-8]+(//s)*$");

    private final static Pattern HEX_NUMBER_PATTERN = Pattern.compile("^(//s)*0[x|X]([0-9]|[a-f]|[A-F])+(//s)*$");

    private final static Pattern EXPONENT_NUMBER_PATTERN = Pattern
            .compile("^(//s)*([+]|[-])?(([0-9])+)[e|E]([+]|[-])?([0-9])+(//s)*$");

    private final static Pattern PYTHON_METHOD_INVOKE_PATTERN = Pattern
            .compile("^(//s)*([a-z]|[A-Z]).*[.].+([(].*[)])$");

    private final static Pattern PYTHON_GET_INVOKE_PATTERN = Pattern.compile("^(//s)*([a-z]|[A-Z]).*[.].+$");

    private IElementDeclaration levelUpElement;

    private TextPosition escape;

    private TextPosition variableIdentificator;

    private final TextPosition variableStart;

    private final TextPosition variableEnd;

    private FilePosition robotTokenPosition;

    public VariableDeclaration(final TextPosition variableStart, final TextPosition variableEnd) {
        this.variableStart = variableStart;
        this.variableEnd = variableEnd;
    }

    public boolean isEscaped() {
        return (escape != null);
    }

    public TextPosition getEscape() {
        return escape;
    }

    public void setEscape(final TextPosition escape) {
        this.escape = escape;
    }

    public TextPosition getTypeIdentificator() {
        return variableIdentificator;
    }

    public VariableType getRobotType() {
        char c = (char) -1;
        final String text = getTypeIdentificator().getText();
        if (!text.isEmpty()) {
            c = text.charAt(0);
        }

        final VariableType robotType = VariableType.getTypeByChar(c);

        return robotType;
    }

    public void setTypeIdentificator(final TextPosition variableIdentficator) {
        this.variableIdentificator = variableIdentficator;
    }

    @Override
    public void setRobotTokenPosition(final FilePosition robotTokenPosition) {
        this.robotTokenPosition = robotTokenPosition;
    }

    private FilePosition getRobotTokenPosition() {
        return robotTokenPosition;
    }

    public TextPosition getVariableText() {
        int start = variableStart.getStart();
        if (variableIdentificator != null) {
            start = variableIdentificator.getStart();
        }
        final int end = variableEnd.getEnd();
        return new TextPosition(variableStart.getFullText(), start, end);
    }

    public Optional<TextPosition> getTextWithoutComputation() {
        return new VariableComputationHelper().extractVariableName(this);
    }

    @Override
    public FilePosition getStartFromFile() {
        FilePosition position = findRobotTokenPosition();
        position = new FilePosition(position.getLine(), position.getColumn() + variableIdentificator.getStart(),
                position.getOffset() + variableIdentificator.getStart());
        return position;
    }

    @Override
    public TextPosition getStart() {
        return variableStart;
    }

    public TextPosition getVariableName() {
        TextPosition varName = new TextPosition(variableStart.getFullText(), variableStart.getEnd() + 1,
                variableEnd.getStart() - 1);
        if (!isDynamic()) {
            final JoinedTextDeclarations nameJoined = new JoinedTextDeclarations();
            final List<IElementDeclaration> elementsDeclarationInside = super.getElementsDeclarationInside();
            for (final IElementDeclaration elem : elementsDeclarationInside) {
                if (elem.isComplex()) {
                    break;
                } else {
                    nameJoined.addElementDeclarationInside(elem);
                }
            }

            final TextPosition joined = nameJoined.join();
            if (joined != null) {
                varName = nameJoined.join();
            }
        }

        return varName;
    }

    public TextPosition getObjectName() {
        TextPosition objectName = null;
        if (getVariableType() == GeneralVariableType.PYTHON_SPECIFIC_INVOKE_VALUE_GET) {
            final TextPosition variableName = getVariableName();
            final String variableText = variableName.getText();
            final int dotCharPos = variableText.indexOf('.');
            if (dotCharPos >= 0) {
                objectName = new TextPosition(variableName.getFullText(), variableName.getStart(),
                        variableName.getStart() + dotCharPos - 1);
            }
        }
        return objectName;
    }

    public RobotToken asToken() {
        final RobotToken token = new RobotToken();
        final String text = getVariableText().getText();
        token.setText(text);
        token.setType(getRobotType().getType());
        final FilePosition fp = getStartFromFile();
        token.setLineNumber(fp.getLine());
        token.setStartColumn(fp.getColumn());
        token.setStartOffset(fp.getOffset());

        return token;
    }

    /**
     * check if variable depends on other variables
     * 
     * @return
     */
    public boolean isDynamic() {
        boolean result = false;
        final List<IElementDeclaration> elementsDeclarationInside = super.getElementsDeclarationInside();
        for (final IElementDeclaration iElementDeclaration : elementsDeclarationInside) {
            if (iElementDeclaration instanceof VariableDeclaration) {
                result = true;
                break;
            }
        }

        return result;
    }

    @Override
    public TextPosition getEnd() {
        return variableEnd;
    }

    @Override
    public FilePosition getEndFromFile() {
        FilePosition position = findRobotTokenPosition();
        position = new FilePosition(position.getLine(), position.getColumn() + variableEnd.getEnd(),
                position.getOffset() + variableEnd.getEnd());
        return position;
    }

    @Override
    public FilePosition findRobotTokenPosition() {
        FilePosition position = getRobotTokenPosition();
        if (position == null) {
            position = this.levelUpElement.findRobotTokenPosition();
        }

        return position;
    }

    @Override
    public void setLevelUpElement(final IElementDeclaration levelUpElement) {
        this.levelUpElement = levelUpElement;
    }

    @Override
    public IElementDeclaration getLevelUpElement() {
        return levelUpElement;
    }

    @Override
    public boolean isComplex() {
        return true;
    }

    public IVariableType getVariableType() {
        IVariableType type = null;
        if (isDynamic()) {
            type = GeneralVariableType.DYNAMIC;
        } else {
            final String variableNameText = getVariableName().getText();
            final VariableType varType = getRobotType();
            if (varType == VariableType.SCALAR || varType == VariableType.SCALAR_AS_LIST) {
                if (EXPONENT_NUMBER_PATTERN.matcher(variableNameText).find()) {
                    type = Number.EXPONENT_NUMBER;
                } else if (COMPUTATION_PATTERN.matcher(variableNameText).find()) {
                    type = GeneralVariableType.COMPUTATION;
                } else if (NUMBER_PATTERN.matcher(variableNameText).find()) {
                    type = Number.NORMAL_NUMBER;
                } else if (BINARY_NUMBER_PATTERN.matcher(variableNameText).find()) {
                    type = Number.BINARY_NUMBER;
                } else if (OCTAL_NUMBER_PATTERN.matcher(variableNameText).find()) {
                    type = Number.OCTAL_NUMBER;
                } else if (HEX_NUMBER_PATTERN.matcher(variableNameText).find()) {
                    type = Number.HEX_NUMBER;
                } else {
                    if (PYTHON_METHOD_INVOKE_PATTERN.matcher(variableNameText).find()) {
                        type = GeneralVariableType.PYTHON_SPECIFIC_INVOKE_METHOD;
                    } else if (PYTHON_GET_INVOKE_PATTERN.matcher(variableNameText).find()) {
                        type = GeneralVariableType.PYTHON_SPECIFIC_INVOKE_VALUE_GET;
                    }
                }
            }

            if (type == null) {
                type = GeneralVariableType.NORMAL_TEXT;
            }
        }

        return type;
    }

    public interface IVariableType {

    }

    public enum GeneralVariableType implements IVariableType {
        DYNAMIC,
        NORMAL_TEXT,
        PYTHON_SPECIFIC_INVOKE_VALUE_GET,
        PYTHON_SPECIFIC_INVOKE_METHOD,
        COMPUTATION;
    }

    public enum Number implements IVariableType {
        NORMAL_NUMBER,
        BINARY_NUMBER,
        OCTAL_NUMBER,
        HEX_NUMBER,
        EXPONENT_NUMBER;
    }
}
