/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public abstract class AVariable extends AModelElement<VariableTable> implements IVariableHolder, Serializable {

    protected VariableType type;

    protected VariableScope scope = VariableScope.LOCAL;

    private final RobotToken declaration;

    private String name;

    private final List<RobotToken> comment = new ArrayList<>();

    protected AVariable(final VariableType type, final String name, final RobotToken declaration,
            final VariableScope scope) {
        this.type = type;
        this.name = name;
        this.declaration = declaration;
        this.scope = scope;
    }

    @Override
    public VariableType getType() {
        return type;
    }

    @Override
    public VariableScope getScope() {
        return scope;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
        this.fixComment(comment, rt);
        this.comment.add(rt);
    }

    public void addCommentPart(final RobotToken rt, final int position) {
        this.fixComment(comment, rt);
        this.comment.set(position, rt);
    }

    public void removeCommentPart(final RobotToken rt) {
        this.comment.remove(rt);
    }

    public boolean moveCommentPartLeft(final RobotToken rt) {
        return MoveElementHelper.moveLeft(comment, rt);
    }

    public boolean moveCommentPartRight(final RobotToken rt) {
        return MoveElementHelper.moveRight(comment, rt);
    }

    @Override
    public RobotToken getDeclaration() {
        return declaration;
    }

    public abstract AVariable copy();

    public enum VariableScope {
        /**
         * 
         */
        GLOBAL,
        /**
         * 
         */
        TEST_SUITE,
        /**
         * 
         */
        TEST_CASE,
        /**
         * 
         */
        LOCAL;
    }

    public enum VariableType {
        /**
         * 
         */
        SCALAR("$", RobotTokenType.VARIABLES_SCALAR_DECLARATION),
        /**
         * Deprecated
         */
        SCALAR_AS_LIST("$", RobotTokenType.VARIABLES_SCALAR_AS_LIST_DECLARATION),
        /**
         * 
         */
        LIST("@", RobotTokenType.VARIABLES_LIST_DECLARATION),
        /**
         * 
         */
        DICTIONARY("&", RobotTokenType.VARIABLES_DICTIONARY_DECLARATION),
        /**
         * 
         */
        ENVIRONMENT("%", RobotTokenType.VARIABLES_ENVIRONMENT_DECLARATION),
        /**
         * 
         */
        INVALID("\\s", RobotTokenType.VARIABLES_UNKNOWN_DECLARATION);

        private final String identificator;

        private final RobotTokenType type;

        private VariableType(final String identificator, final RobotTokenType type) {
            this.identificator = identificator;
            this.type = type;
        }

        public String getIdentificator() {
            return identificator;
        }

        public RobotTokenType getType() {
            return type;
        }

        public static VariableType getTypeByTokenType(final IRobotTokenType type) {
            VariableType varType = null;
            for (final VariableType vt : values()) {
                if (vt.getType() == type) {
                    varType = vt;
                    break;
                }
            }
            return varType;
        }

        public static VariableType getTypeByChar(final char varId) {
            VariableType varType = null;
            final String varIdText = "" + varId;
            for (final VariableType vt : values()) {
                if (vt.getIdentificator().equals(varIdText)) {
                    varType = vt;
                    break;
                }
            }
            return varType;
        }
    }

    @Override
    public ModelType getModelType() {
        ModelType modelType = ModelType.UNKNOWN;

        if (type == VariableType.DICTIONARY) {
            modelType = ModelType.DICTIONARY_VARIABLE_DECLARATION_IN_TABLE;
        } else if (type == VariableType.INVALID) {
            modelType = ModelType.UNKNOWN_VARIABLE_DECLARATION_IN_TABLE;
        } else if (type == VariableType.LIST) {
            modelType = ModelType.LIST_VARIABLE_DECLARATION_IN_TABLE;
        } else if (type == VariableType.SCALAR || type == VariableType.SCALAR_AS_LIST) {
            modelType = ModelType.SCALAR_VARIABLE_DECLARATION_IN_TABLE;
        }

        return modelType;
    }

    @Override
    public FilePosition getBeginPosition() {
        return (getDeclaration() != null) ? getDeclaration().getFilePosition() : FilePosition.createNotSet();
    }
}
