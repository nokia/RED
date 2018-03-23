/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.model.table.variables.UnknownVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.base.Strings;

public class RobotVariable implements RobotFileInternalElement, Serializable {

    private static final long serialVersionUID = 1L;

    // this has to be transient in order not to try serializing whole model instead of simply
    // its small part
    private transient RobotVariablesSection parent;

    private AVariable holder;

    public RobotVariable(final RobotVariablesSection parent, final AVariable variableHolder) {
        this.parent = parent;
        this.holder = variableHolder;
    }

    @Override
    public RobotVariablesSection getParent() {
        return parent;
    }

    public void setParent(final RobotVariablesSection variablesSection) {
        this.parent = variablesSection;
    }

    @Override
    public AVariable getLinkedElement() {
        return holder;
    }

    public void setLinkedElement(final AVariable holder) {
        this.holder = holder;
    }

    @Override
    public List<RobotElement> getChildren() {
        return newArrayList();
    }

    @Override
    public int getIndex() {
        return parent == null ? -1 : parent.getChildren().indexOf(this);
    }

    public String getPrefix() {
        return holder.getType().getIdentificator() + "{";
    }

    public String getSuffix() {
        return "}";
    }

    public VariableType getType() {
        return holder.getType();
    }

    @Override
    public String getName() {
        return Strings.nullToEmpty(holder.getName());
    }

    public String getValue() {
        if (getType() == VariableType.SCALAR) {
            final ScalarVariable variable = (ScalarVariable) holder;
            final List<RobotToken> values = variable.getValues();
            return values.isEmpty() ? "" : values.get(0).getText();

        } else if (getType() == VariableType.SCALAR_AS_LIST) {
            final ScalarVariable variable = (ScalarVariable) holder;
            final List<RobotToken> values = variable.getValues();

            return values.stream().map(RobotToken::getText).collect(joining(", ", "[", "]"));

        } else if (getType() == VariableType.LIST) {
            final ListVariable variable = (ListVariable) holder;
            final List<RobotToken> values = variable.getItems();
            return values.stream().map(RobotToken::getText).collect(joining(", ", "[", "]"));

        } else if (getType() == VariableType.DICTIONARY) {
            final DictionaryVariable variable = (DictionaryVariable) holder;
            final List<DictionaryKeyValuePair> values = variable.getItems();
            return values.stream().map(pair -> pair.getKey().getText() + " = " + pair.getValue().getText()).collect(
                    joining(", ", "{", "}"));

        } else if (getType() == VariableType.INVALID) {
            final UnknownVariable variable = (UnknownVariable) holder;
            final List<RobotToken> values = variable.getItems();
            return values.stream().map(RobotToken::getText).collect(joining(", ", "[", "]"));
        }
        throw new IllegalStateException("Variable defined in variables table cannot have type " + getType().name());
    }

    @Override
    public String getComment() {
        return CommentServiceHandler.consolidate(holder, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
    }

    @Override
    public ImageDescriptor getImage() {
        switch (getType()) {
            case SCALAR:
                return RedImages.getRobotScalarVariableImage();
            case SCALAR_AS_LIST:
                return RedImages.getRobotScalarVariableImage();
            case LIST:
                return RedImages.getRobotListVariableImage();
            case DICTIONARY:
                return RedImages.getRobotDictionaryVariableImage();
            case INVALID:
                return RedImages.getRobotUnknownVariableImage();
            default:
                return null;
        }
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy() {
        return new PageActivatingOpeningStrategy(this);
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return parent.getSuiteFile();
    }

    @Override
    public Position getPosition() {
        final int begin = holder.getDeclaration().getStartOffset();

        int maxStart = -1;
        int end = -1;
        for (final RobotToken token : holder.getElementTokens()) {
            if (token.getStartOffset() > maxStart) {
                maxStart = token.getStartOffset();
                end = maxStart + token.getText().length();
            }
        }
        // it may happen that begin == -1 when there is no declaration (only comment in variable table)
        final int beginToUse = begin == -1 ? maxStart : begin;
        return new Position(beginToUse, end - beginToUse);
    }

    @Override
    public DefinitionPosition getDefinitionPosition() {
        return new DefinitionPosition(holder.getDeclaration().getFilePosition(),
                holder.getDeclaration().getText().length());
    }

    @Override
    public Optional<? extends RobotElement> findElement(final int offset) {
        final AVariable linkedElement = holder;
        if (!linkedElement.getBeginPosition().isNotSet() && linkedElement.getBeginPosition().getOffset() <= offset
                && offset <= linkedElement.getEndPosition().getOffset()) {
            return Optional.of(this);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        // for debugging purposes only
        return getPrefix() + getName() + getSuffix() + "= " + getValue() + "# " + getComment();
    }
}
