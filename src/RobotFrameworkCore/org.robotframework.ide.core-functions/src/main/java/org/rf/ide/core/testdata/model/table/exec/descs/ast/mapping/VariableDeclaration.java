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

    private static final Pattern COMPUTATION_PATTERN = Pattern
            .compile("((?!\\s).)+(\\s)*([+]|[-]|[*]|[/]|[:]|[>]|[<]|[=]|[&]|[%]|\\^|\\!|[|])+[=]*(\\s)*((?!\\s).)+");

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^(//s)*([+]|[-])?(([0-9])+)([.]([0-9])+)?(//s)*$");

    private static final Pattern BINARY_NUMBER_PATTERN = Pattern.compile("^(//s)*0[b|B](0|1)+(//s)*$");

    private static final Pattern OCTAL_NUMBER_PATTERN = Pattern.compile("^(//s)*0[o|O][0-8]+(//s)*$");

    private static final Pattern HEX_NUMBER_PATTERN = Pattern.compile("^(//s)*0[x|X]([0-9]|[a-f]|[A-F])+(//s)*$");

    private static final Pattern EXPONENT_NUMBER_PATTERN = Pattern
            .compile("^(//s)*([+]|[-])?(([0-9])+)[e|E]([+]|[-])?([0-9])+(//s)*$");

    private static final Pattern PYTHON_METHOD_INVOKE_PATTERN = Pattern
            .compile("^(//s)*([a-z]|[A-Z]).*[.].+([(].*[)])$");

    private static final Pattern PYTHON_GET_INVOKE_PATTERN = Pattern.compile("^(//s)*([a-z]|[A-Z]).*[.].+$");

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
        return escape != null;
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
        final String text = getTypeIdentificator().getText();
        final char c = !text.isEmpty() ? text.charAt(0) : (char) -1;
        return VariableType.getTypeByChar(c);
    }

    public void setTypeIdentificator(final TextPosition variableIdentificator) {
        this.variableIdentificator = variableIdentificator;
    }

    @Override
    public void setRobotTokenPosition(final FilePosition robotTokenPosition) {
        this.robotTokenPosition = robotTokenPosition;
    }

    private FilePosition getRobotTokenPosition() {
        return robotTokenPosition;
    }

    public TextPosition getVariableText() {
        final int start = variableIdentificator != null ? variableIdentificator.getStart() : variableStart.getStart();
        final int end = variableEnd.getEnd();
        return new TextPosition(variableStart.getFullText(), start, end);
    }

    public Optional<TextPosition> getTextWithoutComputation() {
        return VariableComputationHelper.extractVariableName(this);
    }

    @Override
    public FilePosition getStartFromFile() {
        final FilePosition position = findRobotTokenPosition();
        return new FilePosition(position.getLine(), position.getColumn() + variableIdentificator.getStart(),
                position.getOffset() + variableIdentificator.getStart());
    }

    @Override
    public TextPosition getStart() {
        return variableStart;
    }

    public TextPosition getVariableName() {
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
                return joined;
            }
        }

        return new TextPosition(variableStart.getFullText(), variableStart.getEnd() + 1, variableEnd.getStart() - 1);
    }

    public TextPosition getObjectName() {
        if (getVariableType() == VariableDeclarationType.PYTHON_SPECIFIC_INVOKE_VALUE_GET) {
            final TextPosition variableName = getVariableName();
            final String variableText = variableName.getText();
            final int dotCharPos = variableText.indexOf('.');
            if (dotCharPos >= 0) {
                return new TextPosition(variableName.getFullText(), variableName.getStart(),
                        variableName.getStart() + dotCharPos - 1);
            }
        }
        return null;
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
        final List<IElementDeclaration> elementsDeclarationInside = super.getElementsDeclarationInside();
        return elementsDeclarationInside.stream().anyMatch(declaration -> declaration instanceof VariableDeclaration);
    }

    @Override
    public TextPosition getEnd() {
        return variableEnd;
    }

    @Override
    public FilePosition getEndFromFile() {
        final FilePosition position = findRobotTokenPosition();
        return new FilePosition(position.getLine(), position.getColumn() + variableEnd.getEnd(),
                position.getOffset() + variableEnd.getEnd());
    }

    @Override
    public FilePosition findRobotTokenPosition() {
        final FilePosition position = getRobotTokenPosition();
        return position != null ? position : levelUpElement.findRobotTokenPosition();
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

    @Override
    public String toString() {
        return String.format("VariableDeclaration [start=%s, end=%s]", getStart(), getEnd());
    }

    public VariableDeclarationType getVariableType() {
        if (isDynamic()) {
            return VariableDeclarationType.DYNAMIC;
        } else {
            final String variableNameText = getVariableName().getText();
            final VariableType varType = getRobotType();
            if (varType == VariableType.SCALAR) {
                if (EXPONENT_NUMBER_PATTERN.matcher(variableNameText).find()) {
                    return VariableDeclarationType.EXPONENT_NUMBER;
                } else if (COMPUTATION_PATTERN.matcher(variableNameText).find()) {
                    return VariableDeclarationType.COMPUTATION;
                } else if (NUMBER_PATTERN.matcher(variableNameText).find()) {
                    return VariableDeclarationType.NORMAL_NUMBER;
                } else if (BINARY_NUMBER_PATTERN.matcher(variableNameText).find()) {
                    return VariableDeclarationType.BINARY_NUMBER;
                } else if (OCTAL_NUMBER_PATTERN.matcher(variableNameText).find()) {
                    return VariableDeclarationType.OCTAL_NUMBER;
                } else if (HEX_NUMBER_PATTERN.matcher(variableNameText).find()) {
                    return VariableDeclarationType.HEX_NUMBER;
                } else if (PYTHON_METHOD_INVOKE_PATTERN.matcher(variableNameText).find()) {
                    return VariableDeclarationType.PYTHON_SPECIFIC_INVOKE_METHOD;
                } else if (PYTHON_GET_INVOKE_PATTERN.matcher(variableNameText).find()) {
                    return VariableDeclarationType.PYTHON_SPECIFIC_INVOKE_VALUE_GET;
                }
            }

            return VariableDeclarationType.NORMAL_TEXT;
        }
    }

    public enum VariableDeclarationType {
        DYNAMIC,
        NORMAL_TEXT,
        PYTHON_SPECIFIC_INVOKE_VALUE_GET,
        PYTHON_SPECIFIC_INVOKE_METHOD,
        COMPUTATION,
        NORMAL_NUMBER(true),
        BINARY_NUMBER(true),
        OCTAL_NUMBER(true),
        HEX_NUMBER(true),
        EXPONENT_NUMBER(true);

        private VariableDeclarationType() {
            this(false);
        }

        private VariableDeclarationType(final boolean isNumber) {
            this.isNumber = isNumber;
        }

        private boolean isNumber;

        public boolean isNumber() {
            return isNumber;
        }
    }
}
