/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

import java.io.Serializable;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.IWorkbenchPage;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.model.table.variables.UnknownVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class RobotVariable implements RobotFileInternalElement, Serializable {

    private transient RobotVariablesSection parent;

    // FIXME : this would cause problems when copy&paste
    private IVariableHolder holder;

    RobotVariable(final RobotVariablesSection parent, final IVariableHolder variableHolder) {
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

    public void fixParents() {
        // nothing to do
    }

    public IVariableHolder getLinkedElement() {
        return holder;
    }

    public void setLinkedElement(final IVariableHolder holder) {
        this.holder = holder;
    }

    @Override
    public List<RobotElement> getChildren() {
        return newArrayList();
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
            return "[" + Joiner.on(", ").join(transform(values, TokenFunctions.tokenToString())) + "]";

        } else if (getType() == VariableType.LIST) {
            final ListVariable variable = (ListVariable) holder;
            final List<RobotToken> values = variable.getItems();
            return "[" + Joiner.on(", ").join(transform(values, TokenFunctions.tokenToString())) + "]";

        } else if (getType() == VariableType.DICTIONARY) {
            final DictionaryVariable variable = (DictionaryVariable) holder;
            final List<DictionaryKeyValuePair> values = variable.getItems();
            return "{" + Joiner.on(", ").join(transform(values, TokenFunctions.pairToString(" = "))) + "}";

        } else if (getType() == VariableType.INVALID) {
            final UnknownVariable variable = (UnknownVariable) holder;
            final List<RobotToken> values = variable.getItems();
            return "[" + Joiner.on(", ").join(transform(values, TokenFunctions.tokenToString())) + "]";
        }
        return "";
    }

    @Override
    public String getComment() {
        return Joiner.on(" ").join(Iterables.transform(holder.getComment(), TokenFunctions.tokenToString()));
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
                return RedImages.getRobotUknownVariableImage();
            default:
                return null;
        }
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new PageActivatingOpeningStrategy(page, getSuiteFile().getFile(), parent, this);
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
        for (final RobotToken token : ((AVariable) holder).getElementTokens()) {
            if (token.getStartOffset() > maxStart) {
                maxStart = token.getStartOffset();
                end = maxStart + token.getText().length();
            }
        }
        return new Position(begin, end - begin);
    }

    @Override
    public DefinitionPosition getDefinitionPosition() {
        return new DefinitionPosition(holder.getDeclaration().getFilePosition(),
                holder.getDeclaration().getText().length());
    }

    @Override
    public Optional<? extends RobotElement> findElement(final int offset) {
        return Optional.absent();
    }

    @Override
    public String toString() {
        // for debugging purposes only
        return getPrefix() + getName() + getSuffix() + "= " + getValue() + "# " + getComment();
    }
}
