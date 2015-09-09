/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.core.testData.model.table.variables.DictionaryVariable;
import org.robotframework.ide.core.testData.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.model.table.variables.ListVariable;
import org.robotframework.ide.core.testData.model.table.variables.ScalarVariable;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public class RobotVariable implements RobotElement, Serializable {

    private transient RobotVariablesSection parent;
    private String name;
    private Type type;
    private String value;
    private String comment;

    private IVariableHolder holder;

    RobotVariable(final RobotVariablesSection section, final Type type, final String name,
            final String value, final String comment) {
        this.parent = section;
        this.type = type;
        this.name = name;
        this.value = value;
        this.comment = comment;
    }

    RobotVariable(final RobotVariablesSection parent) {
        this.parent = parent;
    }

    public void link(final IVariableHolder variableHolder) {
        this.holder = variableHolder;
        this.type = Type.fromVarType(variableHolder.getType());
        this.name = variableHolder.getName();
        if(variableHolder.getType() == VariableType.SCALAR) {

            final List<RobotToken> values = ((ScalarVariable) variableHolder).getValues();
            this.value = values.isEmpty() ? "" : values.get(0).getText().toString();
        } else if (variableHolder.getType() == VariableType.LIST) {

            final List<RobotToken> tokens = ((ListVariable) variableHolder).getItems();
            this.value = Joiner.on(" | ").join(Iterables.transform(tokens, TokenFunctions.tokenToString()));
        } else if (variableHolder.getType() == VariableType.SCALAR_AS_LIST) {

            final List<RobotToken> tokens = ((ScalarVariable) variableHolder).getValues();
            this.value = Joiner.on(" | ").join(Iterables.transform(tokens, TokenFunctions.tokenToString()));
        } else if(variableHolder.getType() == VariableType.DICTIONARY) {

            final List<DictionaryKeyValuePair> dictionaryPairs = ((DictionaryVariable) variableHolder).getItems();
            this.value = Joiner.on(" | ").join(Iterables.transform(dictionaryPairs, TokenFunctions.pairToString()));
        } else {
            this.value = "";
        }
        this.comment = Joiner.on(" | ")
                .join(Iterables.transform(variableHolder.getComment(), TokenFunctions.tokenToString()));
    }

    @Override
    public String toString() {
        return getPrefix() + name + getSuffix() + "= " + value + "# " + comment;
    }

    @Override
    public ImageDescriptor getImage() {
        return type.getImage();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new PageActivatingOpeningStrategy(page, getSuiteFile().getFile(), parent, this);
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

    @Override
    public List<RobotElement> getChildren() {
        return newArrayList();
    }

    public String getPrefix() {
        return type.getMark() + "{";
    }

    public String getSuffix() {
        return "}";
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return parent.getSuiteFile();
    }

    public enum Type {
        SCALAR("$", RedImages.getRobotScalarVariableImage()),
        LIST("@",RedImages.getRobotListVariableImage()),
        DICTIONARY("&", RedImages.getRobotDictionaryVariableImage()),
        UNKNOWN("?", RedImages.getRobotUknownVariableImage());

        public static Type fromVarType(final VariableType varType) {
            switch (varType) {
                case DICTIONARY:
                    return Type.DICTIONARY;
                case LIST:
                    return Type.LIST;
                case SCALAR:
                case SCALAR_AS_LIST:
                    return Type.SCALAR;
                default:
                    return Type.UNKNOWN;
            }
        }

        private String mark;

        private ImageDescriptor image;

        private Type(final String mark, final ImageDescriptor image) {
            this.mark = mark;
            this.image = image;
        }

        public String getMark() {
            return mark;
        }

        public ImageDescriptor getImage() {
            return image;
        }
    }
}
