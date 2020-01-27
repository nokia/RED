/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.exec.descs.ExecutableRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class RobotExecutableRow<T> extends CommonStep<T> implements ICommentHolder, Serializable {

    private static final long serialVersionUID = -4158729064542423691L;

    private RobotToken action = new RobotToken();

    private final List<RobotToken> arguments = new ArrayList<>();

    private final List<RobotToken> comments = new ArrayList<>();

    public static boolean isExecutable(final FileFormat fileFormat, final List<RobotToken> tokens) {
        final RobotToken action = !tokens.isEmpty() ? tokens.get(0) : null;
        if (action == null) {
            return false;
        }
        if (!action.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            final String text = action.getText().trim();

            if (text.equals("\\")) {
                return tokens.size() > 1 && !tokens.get(1).getTypes().contains(RobotTokenType.START_HASH_COMMENT);

            } else if (text.isEmpty()) {
                if (fileFormat == FileFormat.TSV
                        || action.getTypes().contains(RobotTokenType.FOR_WITH_END_CONTINUATION)) {
                    return tokens.size() > 1 && !tokens.get(1).getTypes().contains(RobotTokenType.START_HASH_COMMENT);
                }
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setParent(final T parent) {
        super.setParent(parent);
        fixMissingTypes();
    }

    public RobotToken getAction() {
        return action;
    }

    public void setAction(final RobotToken action) {
        this.action = updateOrCreate(this.action, action, getActionType());
        fixMissingTypes();
    }

    private IRobotTokenType getActionType() {
        final T parent = getParent();
        if (parent != null && parent.getClass() == TestCase.class) {
            return RobotTokenType.TEST_CASE_ACTION_NAME;
        } else if (parent != null && parent.getClass() == Task.class) {
            return RobotTokenType.TASK_ACTION_NAME;
        } else if (parent != null && parent.getClass() == UserKeyword.class) {
            return RobotTokenType.KEYWORD_ACTION_NAME;
        }
        return RobotTokenType.UNKNOWN;
    }

    private IRobotTokenType getArgumentType() {
        final T parent = getParent();
        if (parent != null && parent.getClass() == TestCase.class) {
            return RobotTokenType.TEST_CASE_ACTION_ARGUMENT;
        } else if (parent != null && parent.getClass() == Task.class) {
            return RobotTokenType.TASK_ACTION_ARGUMENT;
        } else if (parent != null && parent.getClass() == UserKeyword.class) {
            return RobotTokenType.KEYWORD_ACTION_ARGUMENT;
        }
        return RobotTokenType.UNKNOWN;
    }

    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public void setArgument(final int index, final String argument) {
        setArgument(index, RobotToken.create(argument));
    }

    public void setArgument(final int index, final RobotToken argument) {
        updateOrCreateTokenInside(arguments, index, argument, getArgumentType());

        fixMissingTypes();
    }

    public void addArgument(final int index, final RobotToken argument) {
        fixForTheType(argument, getArgumentType(), true);
        arguments.add(index, argument);

        fixMissingTypes();
    }

    public void addArgument(final RobotToken argument) {
        fixForTheType(argument, getArgumentType(), true);
        arguments.add(argument);

        fixMissingTypes();
    }

    public void removeArgument(final int index) {
        arguments.remove(index);
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
        fixComment(getComment(), rt);
        this.comments.add(rt);
    }

    @Override
    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comments);
    }

    @Override
    public void setComment(final String comment) {
        setComment(RobotToken.create(comment));
    }

    @Override
    public void setComment(final RobotToken comment) {
        this.comments.clear();
        addCommentPart(comment);
    }

    @Override
    public void removeCommentPart(final int index) {
        this.comments.remove(index);
    }

    @Override
    public void clearComment() {
        this.comments.clear();
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public RobotToken getDeclaration() {
        return action;
    }

    @Override
    public ModelType getModelType() {
        final List<IRobotTokenType> types = getAction().getTypes();
        if (types.contains(RobotTokenType.TEST_CASE_ACTION_NAME)) {
            return ModelType.TEST_CASE_EXECUTABLE_ROW;
        } else if (types.contains(RobotTokenType.TASK_ACTION_NAME)) {
            return ModelType.TASK_EXECUTABLE_ROW;
        } else if (types.contains(RobotTokenType.KEYWORD_ACTION_NAME)) {
            return ModelType.USER_KEYWORD_EXECUTABLE_ROW;
        } else if (types.contains(RobotTokenType.UNKNOWN)) {
            final AModelElement<?> parent = (AModelElement<?>) getParent();
            if (parent != null) {
                final ModelType parentType = parent.getModelType();
                if (parentType == ModelType.TEST_CASE) {
                    return ModelType.TEST_CASE_EXECUTABLE_ROW;
                } else if (parentType == ModelType.TASK) {
                    return ModelType.TASK_EXECUTABLE_ROW;
                } else if (parentType == ModelType.USER_KEYWORD) {
                    return ModelType.USER_KEYWORD_EXECUTABLE_ROW;
                }
            }
        }

        return ModelType.UNKNOWN;
    }

    @Override
    public FilePosition getBeginPosition() {
        final FilePosition position = getAction().getFilePosition();
        if (position.isNotSet()) {
            // this may be comment row
            final List<RobotToken> tokens = getElementTokens();
            if (tokens.size() > 1) {
                return tokens.get(1).getFilePosition();
            }
        }
        return position;
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        tokens.add(getAction());
        if (comments.isEmpty()) {
            tokens.addAll(trimEmptyTokensAtListEnd(arguments));
        } else {
            tokens.addAll(arguments);
            tokens.addAll(trimEmptyTokensAtListEnd(comments));
        }

        return tokens;
    }

    private List<RobotToken> trimEmptyTokensAtListEnd(final List<RobotToken> tokens) {
        final ListIterator<RobotToken> iterator = tokens.listIterator(tokens.size());
        while (iterator.hasPrevious()) {
            if (iterator.previous().isEmpty()) {
                iterator.remove();
            } else {
                break;
            }
        }
        return tokens;
    }

    public boolean isExecutable() {
        if (action == null) {
            return false;
        }
        if (getParent() instanceof IExecutableStepsHolder) {
            @SuppressWarnings("unchecked")
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> parent = (IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>>) getParent();
            final FileFormat fileFormat = Optional.ofNullable(parent)
                    .map(IExecutableStepsHolder::getHolder)
                    .map(AModelElement::getParent)
                    .map(ARobotSectionTable.class::cast)
                    .map(ARobotSectionTable::getParent)
                    .map(RobotFile::getParent)
                    .map(RobotFileOutput::getFileFormat)
                    .orElse(null);

            return RobotExecutableRow.isExecutable(fileFormat, getElementTokens());
        } else {
            return !action.getTypes().contains(RobotTokenType.START_HASH_COMMENT);
        }
    }

    public IExecutableRowDescriptor<T> buildLineDescription() {
        return new ExecutableRowDescriptorBuilder().buildLineDescriptor(this);
    }

    @Override
    public boolean removeElementToken(final int index) {
        return super.removeElementFromList(arguments, index);
    }

    @Override
    public void createToken(final int index) {
        final int argsIndex = index - 1;
        final int commentsIndex = index - arguments.size() - 1;

        if (index == 0) {
            arguments.add(0, action);
            action = RobotToken.create("");
            fixMissingTypes();
            fixTemplateArgumentsTypes();

        } else if (0 <= argsIndex && argsIndex <= arguments.size()) {
            arguments.add(argsIndex, RobotToken.create("", getArgumentType()));
            fixTemplateArgumentsTypes();

        } else if (1 <= commentsIndex && commentsIndex <= comments.size()) {
            comments.add(commentsIndex, RobotToken.create("", RobotTokenType.COMMENT_CONTINUE));
        }
    }

    @Override
    public void updateToken(final int index, final String newValue) {
        final int argsIndex = index - 1;
        final int commentsIndex = index - 1 - arguments.size();

        if (index == 0 && newValue.trim().startsWith("#")) {
            throw new IllegalArgumentException();

        } else if (index == 0) {
            action.setText(newValue);

        } else if (0 <= argsIndex && argsIndex < arguments.size()) {
            arguments.get(argsIndex).setText(newValue);

            if (newValue.trim().startsWith("#")) {
                for (int i = arguments.size() - 1; i >= argsIndex; i--) {
                    comments.add(0, arguments.remove(i));
                }
                fixCommentsTypes();
            }

        } else if (comments.isEmpty() && arguments.size() <= argsIndex) {
            final int repeat = argsIndex - arguments.size();
            for (int i = 0; i < repeat; i++) {
                addArgument(RobotToken.create("\\"));
            }
            if (newValue.trim().startsWith("#")) {
                addCommentPart(RobotToken.create(newValue));
            } else {
                addArgument(RobotToken.create(newValue));
            }

        } else if (0 <= commentsIndex && commentsIndex < comments.size()) {
            comments.get(commentsIndex).setText(newValue);

            while (!comments.isEmpty() && !comments.get(0).getText().trim().startsWith("#")) {
                final RobotToken toMove = comments.remove(0);
                toMove.setType(getArgumentType());
                arguments.add(toMove);
            }
            fixCommentsTypes();

        } else if (comments.size() <= commentsIndex) {
            final int repeat = commentsIndex - comments.size() + 1;
            for (int i = 0; i < repeat; i++) {
                addCommentPart(RobotToken.create("\\"));
            }
            comments.get(commentsIndex).setText(newValue);
        }
    }

    @Override
    public void deleteToken(final int index) {
        final int argsIndex = index - 1;
        final int commentsIndex = index - 1 - arguments.size();

        if (index == 0 && arguments.isEmpty()) {
            throw new IllegalArgumentException();

        } else if (index == 0) {
            action = arguments.remove(0);
            fixMissingTypes();

        } else if (0 <= argsIndex && argsIndex < arguments.size()) {
            arguments.remove(argsIndex);

        } else if (0 <= commentsIndex && commentsIndex < comments.size()) {
            comments.remove(commentsIndex);

            while (!comments.isEmpty() && !comments.get(0).getText().trim().startsWith("#")) {
                final RobotToken toMove = comments.remove(0);
                toMove.setType(getArgumentType());
                arguments.add(toMove);
            }
            fixCommentsTypes();
        }
    }

    @Override
    public void rewriteFrom(final CommonStep<?> other) {
        final RobotExecutableRow<?> otherRow = (RobotExecutableRow<?>) other;
        this.action = otherRow.action;
        this.arguments.clear();
        this.arguments.addAll(otherRow.arguments);
        this.comments.clear();
        this.comments.addAll(otherRow.comments);
    }

    private void fixMissingTypes() {
        if (getParent() != null) {
            if (action != null && (!getArguments().isEmpty() || action.isNotEmpty() || !getComment().isEmpty())) {
                action.getTypes().remove(RobotTokenType.UNKNOWN);
                fixForTheType(action, getActionType(), true);
            }

            for (final RobotToken token : getArguments()) {
                token.getTypes().remove(RobotTokenType.UNKNOWN);
                token.getTypes().remove(getActionType());
                fixForTheType(token, getArgumentType(), true);
            }
        }
    }

    private void fixTemplateArgumentsTypes() {
        if (getParent() != null) {
            if (getParent().getClass() == TestCase.class) {
                fixTemplateArgumentsTypes(((TestCase) getParent()).getTemplateKeywordName().isPresent(),
                        RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
            } else if (getParent().getClass() == Task.class) {
                fixTemplateArgumentsTypes(((Task) getParent()).getTemplateKeywordName().isPresent(),
                        RobotTokenType.TASK_TEMPLATE_ARGUMENT);
            }
        }
    }

    public void fixTemplateArgumentsTypes(final boolean isTemplateUsed, final RobotTokenType templateArgumentType) {
        if (!action.getTypes().contains(RobotTokenType.FOR_TOKEN)
                && !action.getTypes().contains(RobotTokenType.FOR_END_TOKEN)) {
            final List<RobotToken> elementTokens = getElementTokens();
            for (int i = 0; i < elementTokens.size(); i++) {
                final RobotToken token = elementTokens.get(i);
                token.getTypes().remove(RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
                token.getTypes().remove(RobotTokenType.TASK_TEMPLATE_ARGUMENT);
                if (isTemplateUsed && !token.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                    if (i == 0 && !token.getTypes().contains(RobotTokenType.FOR_CONTINUE_TOKEN)
                            && !token.getTypes().contains(RobotTokenType.FOR_WITH_END_CONTINUATION)) {
                        token.getTypes().add(templateArgumentType);
                    } else if (i != 0) {
                        token.getTypes().remove(RobotTokenType.FOR_CONTINUE_TOKEN);
                        token.getTypes().remove(RobotTokenType.FOR_WITH_END_CONTINUATION);
                        token.getTypes().add(templateArgumentType);
                    }
                }
            }
        }
    }

    private void fixCommentsTypes() {
        for (int i = 0; i < comments.size(); i++) {
            final RobotToken token = comments.get(i);
            if (i == 0) {
                token.setType(RobotTokenType.START_HASH_COMMENT);

            } else if (i > 0) {
                token.setType(RobotTokenType.COMMENT_CONTINUE);
            }
        }
    }
}
