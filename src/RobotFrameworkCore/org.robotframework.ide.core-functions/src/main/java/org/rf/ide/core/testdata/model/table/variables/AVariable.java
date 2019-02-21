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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.collect.ImmutableSet;

public abstract class AVariable extends AModelElement<VariableTable>
        implements IVariableHolder, ICommentHolder, Serializable {

    private static final long serialVersionUID = 3690914169761757467L;

    public static final Set<String> ROBOT_VAR_IDENTIFICATORS = Stream
            .of(VariableType.SCALAR, VariableType.LIST, VariableType.DICTIONARY, VariableType.ENVIRONMENT)
            .map(VariableType::getIdentificator)
            .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));

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
        fixForTheType(declaration, (type != null && type.getType() != null) ? type.getType() : null, true);
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

    @Override
    public void setComment(final String comment) {
        final RobotToken tok = new RobotToken();
        tok.setText(comment);
        setComment(tok);
    }

    @Override
    public void setComment(final RobotToken comment) {
        this.comment.clear();
        addCommentPart(comment);
    }

    @Override
    public void removeCommentPart(final int index) {
        this.comment.remove(index);
    }

    @Override
    public void clearComment() {
        this.comment.clear();
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

    @Override
    public FilePosition getBeginPosition() {
        return getDeclaration() != null ? getDeclaration().getFilePosition() : FilePosition.createNotSet();
    }

    @Override
    public ModelType getModelType() {
        switch (type) {
            case SCALAR:
                return ModelType.SCALAR_VARIABLE_DECLARATION_IN_TABLE;
            case LIST:
                return ModelType.LIST_VARIABLE_DECLARATION_IN_TABLE;
            case DICTIONARY:
                return ModelType.DICTIONARY_VARIABLE_DECLARATION_IN_TABLE;
            case INVALID:
                return ModelType.UNKNOWN_VARIABLE_DECLARATION_IN_TABLE;
            default:
                return ModelType.UNKNOWN;
        }
    }

    public enum VariableScope {
        GLOBAL("Global Variables"),
        TEST_SUITE("Test Suite Variables"),
        TEST_CASE("Test Case Variables"),
        LOCAL("Local Variables");

        public static VariableScope fromSimpleName(final String scopeName) {
            switch (scopeName.toLowerCase()) {
                case "global":
                    return GLOBAL;
                case "suite":
                    return TEST_SUITE;
                case "test":
                    return TEST_CASE;
                case "local":
                    return LOCAL;
                default:
                    throw new IllegalArgumentException("Unrecognized scope: " + scopeName);
            }
        }

        private String name;

        private VariableScope(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum VariableType {
        SCALAR("$", RobotTokenType.VARIABLES_SCALAR_DECLARATION),
        LIST("@", RobotTokenType.VARIABLES_LIST_DECLARATION),
        DICTIONARY("&", RobotTokenType.VARIABLES_DICTIONARY_DECLARATION),
        ENVIRONMENT("%", RobotTokenType.VARIABLES_ENVIRONMENT_DECLARATION),
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
            for (final VariableType vt : values()) {
                if (vt.getType() == type) {
                    return vt;
                }
            }
            return null;
        }

        public static VariableType getTypeByChar(final char varId) {
            final String varIdText = "" + varId;
            for (final VariableType vt : values()) {
                if (vt.getIdentificator().equals(varIdText)) {
                    return vt;
                }
            }
            return null;
        }
    }
}
