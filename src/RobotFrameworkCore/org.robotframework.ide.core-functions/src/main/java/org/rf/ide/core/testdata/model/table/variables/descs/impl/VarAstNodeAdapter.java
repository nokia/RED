/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import java.util.Set;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.ExpressionAstNode.VarSyntaxIssue;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.base.Preconditions;


class VarAstNodeAdapter implements VariableUse {

    private final VariableSyntaxValidator validator;

    private final ExpressionAstNode node;

    VarAstNodeAdapter(final VariableSyntaxValidator validator, final ExpressionAstNode node) {
        Preconditions.checkArgument(node.isVar());
        this.validator = validator;
        this.node = node;
    }

    @Override
    public VariableType getType() {
        return VariableType.getTypeByChar(getContent().charAt(0));
    }

    @Override
    public String getBaseName() {
        final String internal = getContentWithoutBraces();
        final StringBuilder baseName = new StringBuilder();

        for (final char ch : internal.toCharArray()) {
            if (Character.isAlphabetic(ch) || Character.isDigit(ch) || ch == '_' || ch == ' ') {
                baseName.append(ch);
            } else {
                break;
            }
        }
        return baseName.toString().trim();
    }

    @Override
    public RobotToken asToken() {
        return RobotToken.create(getContent(), getRegion().getStart());
    }

    @Override
    public FileRegion getRegion() {
        return node.getRegion();
    }

    @Override
    public boolean isDefinedIn(final Set<String> variableDefinitions) {
        if (node.hasMissingClosingParen() || node.isDynamic()) {
            return false;

        } else if (getType() == VariableType.ENVIRONMENT) {
            return true;

        } else if (contains(getContentWithoutBraces(), variableDefinitions)) {
            return true;
        }
        final String baseName = getBaseName();
        return contains(baseName, variableDefinitions)
                || Pattern.matches("[0-9]+|0[bB][0-1]+|0[oO][0-7]+|0[xX][0-9a-fA-F]+", baseName);
    }

    private boolean contains(final String name, final Set<String> varDefs) {
        final String normalized = VariablesAnalyzer.normalizeName(name);
        if (getType() == VariableType.SCALAR) {
            return varDefs.contains("${" + normalized + "}") || varDefs.contains("@{" + normalized + "}")
                    || varDefs.contains("&{" + normalized + "}");
        } else {
            return varDefs.contains("${" + normalized + "}")
                    || varDefs.contains(getType().getIdentificator() + "{" + normalized + "}");
        }
    }

    @Override
    public boolean isDynamic() {
        return node.isDynamic();
    }

    @Override
    public boolean isIndexed() {
        return node.isIndexed();
    }

    @Override
    public boolean isPlainVariable() {
        return node.isPlainVariableFollowedBySuffix("");
    }

    @Override
    public boolean isPlainVariableAssign() {
        return node.isPlainVariableFollowedBySuffix("", "=");
    }

    @Override
    public void validate() throws VariableUseSyntaxException {
        validator.validate(this);
    }

    boolean isInvalid() {
        return node.isInvalid();
    }

    VarSyntaxIssue getErrorType() {
        return node.getErrorType();
    }

    private String getContent() {
        return node.getText();
    }

    private String getContentWithoutBraces() {
        final String content = node.getTextWithoutItem().substring(2).trim();
        return node.hasMissingClosingParen() ? content : content.substring(0, content.length() - 1).trim();
    }
}
