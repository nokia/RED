/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.rf.ide.core.testdata.model.presenter.update.KeywordTableModelUpdater;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class RobotKeywordDefinition extends RobotCodeHoldingElement {

    private static final long serialVersionUID = 1L;

    public static final String ARGUMENTS = "Arguments";
    public static final String DOCUMENTATION = "Documentation";
    public static final String TAGS = "Tags";
    public static final String TIMEOUT = "Timeout";
    public static final String TEARDOWN = "Teardown";
    public static final String RETURN = "Return";

    private UserKeyword keyword;
    
    public RobotKeywordDefinition(final RobotKeywordsSection parent, final String name) {
        super(parent, name);
    }

    @Override
    public UserKeyword getLinkedElement() {
        return keyword;
    }

    @Override
    public RobotKeywordCall createKeywordCall(final String callName, final int modelTableIndex,
            final int codeHoldingElementIndex) {
        final RobotKeywordCall call = new RobotKeywordCall(this, callName, new ArrayList<String>(), "");

        final RobotExecutableRow<UserKeyword> robotExecutableRow = new RobotExecutableRow<>();
        if (modelTableIndex >= 0 && modelTableIndex < keyword.getKeywordExecutionRows().size()) {
            getLinkedElement().addKeywordExecutionRow(robotExecutableRow, modelTableIndex);
        } else {
            getLinkedElement().addKeywordExecutionRow(robotExecutableRow);
        }
        call.link(robotExecutableRow);

        if (codeHoldingElementIndex >= 0 && codeHoldingElementIndex < getChildren().size()) {
            getChildren().add(codeHoldingElementIndex, call);
        } else {
            getChildren().add(call);
        }
        return call;
    }
    
    public RobotDefinitionSetting createKeywordDefinitionSetting(final int index, final String name,
            final List<String> args, final String comment) {
        final RobotDefinitionSetting setting = new RobotDefinitionSetting(this, omitSquareBrackets(name), args,
                comment);

        final AModelElement<?> newModelElement = new KeywordTableModelUpdater().create(getLinkedElement(), name,
                comment, args);
        setting.link(newModelElement);

        getChildren().add(index, setting);
        return setting;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void insertKeywordCall(final int modelTableIndex, final int codeHoldingElementIndex, final RobotKeywordCall keywordCall) {

        RobotKeywordCall newCall = null;

        if (keywordCall.getLinkedElement().getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
            newCall = new RobotKeywordCall(this, keywordCall.getName(), keywordCall.getArguments(),
                    keywordCall.getComment());
            final RobotExecutableRow<UserKeyword> robotExecutableRow = (RobotExecutableRow<UserKeyword>) keywordCall
                    .getLinkedElement();
            if (modelTableIndex >= 0 && modelTableIndex < keyword.getKeywordExecutionRows().size()) {
                keyword.addKeywordExecutionRow(robotExecutableRow, modelTableIndex);
            } else {
                keyword.addKeywordExecutionRow(robotExecutableRow);
            }
            newCall.link(robotExecutableRow);
        } else {
            newCall = new RobotDefinitionSetting(this, keywordCall.getName(), keywordCall.getArguments(),
                    keywordCall.getComment());
            new KeywordTableModelUpdater().updateParent(keyword, keywordCall.getLinkedElement());
            newCall.link(keywordCall.getLinkedElement());
        }

        if (codeHoldingElementIndex >= 0 && codeHoldingElementIndex < getChildren().size()) {
            getChildren().add(codeHoldingElementIndex, newCall);
        } else {
            getChildren().add(newCall);
        }
    }

    public void link(final UserKeyword keyword) {
        this.keyword = keyword;
        
        // settings
        for (final KeywordArguments argument : keyword.getArguments()) {
            final String name = argument.getDeclaration().getText().toString();
            final List<String> args = newArrayList(
                    Lists.transform(argument.getArguments(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(argument, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotDefinitionSetting setting = new RobotDefinitionSetting(this, omitSquareBrackets(name), args, comment);
            setting.link(argument);
            getChildren().add(setting);
        }
        for (final KeywordDocumentation documentation : keyword.getDocumentation()) {
            final String name = documentation.getDeclaration().getText().toString();
            final List<String> args = newArrayList(
                    Lists.transform(documentation.getDocumentationText(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(documentation, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotDefinitionSetting setting = new RobotDefinitionSetting(this, omitSquareBrackets(name), args, comment);
            setting.link(documentation);
            getChildren().add(setting);
        }
        for (final KeywordTags tags : keyword.getTags()) {
            final String name = tags.getDeclaration().getText().toString();
            final List<String> args = newArrayList(Lists.transform(tags.getTags(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(tags, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotDefinitionSetting setting = new RobotDefinitionSetting(this, omitSquareBrackets(name), args, comment);
            setting.link(tags);
            getChildren().add(setting);
        }
        for (final KeywordTimeout timeout : keyword.getTimeouts()) {
            final String name = timeout.getDeclaration().getText().toString();
            final List<String> args = timeout.getTimeout() == null ? new ArrayList<String>()
                    : newArrayList(timeout.getTimeout().getText().toString());
            args.addAll(Lists.transform(timeout.getMessage(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(timeout, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotDefinitionSetting setting = new RobotDefinitionSetting(this, omitSquareBrackets(name), args, comment);
            setting.link(timeout);
            getChildren().add(setting);
        }
        for (final KeywordTeardown teardown : keyword.getTeardowns()) {
            final String name = teardown.getDeclaration().getText().toString();
            final List<String> args = teardown.getKeywordName() == null ? new ArrayList<String>()
                    : newArrayList(teardown.getKeywordName().getText().toString());
            args.addAll(Lists.transform(teardown.getArguments(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(teardown, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotDefinitionSetting setting = new RobotDefinitionSetting(this, omitSquareBrackets(name), args, comment);
            setting.link(teardown);
            getChildren().add(setting);
        }
        for (final KeywordReturn returnSetting : keyword.getReturns()) {
            final String name = returnSetting.getDeclaration().getText().toString();
            final List<String> args = returnSetting.getReturnValues() == null ? new ArrayList<String>()
                    : newArrayList(Lists.transform(returnSetting.getReturnValues(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(returnSetting, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotDefinitionSetting setting = new RobotDefinitionSetting(this, omitSquareBrackets(name), args, comment);
            setting.link(returnSetting);
            getChildren().add(setting);
        }
        
        // body
        for (final RobotExecutableRow<UserKeyword> execRow : keyword.getKeywordExecutionRows()) {
            final String callName = execRow.getAction().getText().toString();
            final List<String> args = newArrayList(
                    Lists.transform(execRow.getArguments(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(execRow, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotKeywordCall call = new RobotKeywordCall(this, callName, args, comment);
            getChildren().add(call);
            call.link(execRow);
        }
    }

    private static String omitSquareBrackets(final String nameInBrackets) {
        return nameInBrackets.substring(1, nameInBrackets.length() - 1);
    }

    @Override
    public RobotKeywordsSection getParent() {
        return (RobotKeywordsSection) super.getParent();
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getUserKeywordImage();
    }

    public boolean hasArguments() {
        return getArgumentsSetting() != null;
    }

    public RobotDefinitionSetting getArgumentsSetting() {
        return findSetting(ARGUMENTS);
    }

    public boolean hasReturnValue() {
        return getReturnValueSetting() != null;
    }

    public RobotDefinitionSetting getReturnValueSetting() {
        return findSetting(RETURN);
    }

    public boolean hasDocumentation() {
        return getDocumentationSetting() != null;
    }

    public RobotDefinitionSetting getDocumentationSetting() {
        return findSetting(DOCUMENTATION);
    }

    public boolean hasTags() {
        return getTagsSetting() != null;
    }

    public RobotDefinitionSetting getTagsSetting() {
        return findSetting(TAGS);
    }

    public boolean hasTeardownValue() {
        return getTeardownSetting() != null;
    }

    public RobotDefinitionSetting getTeardownSetting() {
        return findSetting(TEARDOWN);
    }

    public boolean hasTimeoutValue() {
        return getTimeoutSetting() != null;
    }

    public RobotDefinitionSetting getTimeoutSetting() {
        return findSetting(TIMEOUT);
    }

    private RobotDefinitionSetting findSetting(final String name) {
        for (final RobotKeywordCall call : getChildren()) {
            if (call instanceof RobotDefinitionSetting && call.getName().equalsIgnoreCase(name)) {
                return (RobotDefinitionSetting) call;
            }
        }
        return null;
    }

    public String getDocumentation() {
        final RobotDefinitionSetting documentationSetting = getDocumentationSetting();
        if (documentationSetting != null) {
            final KeywordDocumentation documentation = (KeywordDocumentation) documentationSetting.getLinkedElement();
            final StringBuilder docBuilder = new StringBuilder();
            for (final RobotToken token : documentation.getDocumentationText()) {
                docBuilder.append(token.getText().toString());
            }
            return docBuilder.toString();
        }
        return "<not documented>";
    }

    public ArgumentsDescriptor createArgumentsDescriptor() {
        return ArgumentsDescriptor.createDescriptor(getArguments());
    }

    private List<String> getArguments() {
        // embedded arguments are not provided for descriptor or documentation
        final List<String> args = newArrayList();
        final RobotDefinitionSetting argumentsSetting = getArgumentsSetting();
        if (argumentsSetting != null) {
            final KeywordArguments arguments = (KeywordArguments) argumentsSetting.getLinkedElement();
            for (final RobotToken token : arguments.getArguments()) {
                args.add(toPythonicNotation(token));
            }
        }
        return args;
    }

    private String toPythonicNotation(final RobotToken token) {
        final List<IRobotTokenType> types = token.getTypes();
        String text = EmbeddedKeywordNamesSupport.removeRegex(token.getText().toString());
        String defaultValue = null;
        if (text.contains("=")) {
            final List<String> splitted = Splitter.on('=').limit(2).splitToList(text);
            text = splitted.get(0);
            defaultValue = splitted.get(1);
        }
        if (types.contains(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION) && text.startsWith("&{")
                && text.endsWith("}")) {
            return "**" + text.substring(2, text.length() - 1);
        } else if (types.contains(RobotTokenType.VARIABLES_LIST_DECLARATION) && text.startsWith("@{")
                && text.endsWith("}")) {
            return "*" + text.substring(2, text.length() - 1);
        } else if (types.contains(RobotTokenType.VARIABLES_SCALAR_DECLARATION) && text.startsWith("${")
                && text.endsWith("}")) {
            defaultValue = defaultValue == null ? "" : "=" + defaultValue;
            return text.substring(2, text.length() - 1) + defaultValue;
        } else {
            return text;
        }
    }

    public boolean isDeprecated() {
        return Pattern.compile("^\\*deprecated[^\\n\\r]*\\*.*").matcher(getDocumentation().toLowerCase()).find();
    }

    @Override
    public Position getPosition() {
        final FilePosition begin = keyword.getBeginPosition();
        final FilePosition end = keyword.getEndPosition();

        if(begin.isNotSet() || end.isNotSet()) {
            return new Position(0, 0);
        }
        
        return new Position(begin.getOffset(), end.getOffset() - begin.getOffset());
    }

    @Override
    public DefinitionPosition getDefinitionPosition() {
        return new DefinitionPosition(keyword.getKeywordName().getFilePosition(),
                keyword.getKeywordName().getText().length());
    }

    public List<VariableDeclaration> getEmbeddedArguments() {
        final VariableExtractor extractor = new VariableExtractor();
        final MappingResult extractedVars = extractor.extract(keyword.getKeywordName(), getSuiteFile().getName());
        return extractedVars.getCorrectVariables();
    }

    public boolean hasEmbeddedArguments() {
        return !getEmbeddedArguments().isEmpty();
    }

    public KeywordSpecification createSpecification() {
        final KeywordSpecification keywordSpecification = new KeywordSpecification();
        keywordSpecification.setName(getName());
        keywordSpecification.setArguments(getArguments());
        keywordSpecification.setFormat("ROBOT");
        keywordSpecification.setDocumentation(getDocumentation());
        return keywordSpecification;
    }
    
    public int findExecutableRowIndex(final RobotKeywordCall keywordCallWithExeRow) {
        return getExecutableRows().indexOf(keywordCallWithExeRow);
    }

    public List<RobotKeywordCall> getExecutableRows() {
        return newArrayList(filter(getChildren(), new Predicate<RobotKeywordCall>() {

            @Override
            public boolean apply(final RobotKeywordCall call) {
                return call.getLinkedElement().getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW;
            }
        }));
    }

    @SuppressWarnings("unchecked")
    private Object readResolve() throws ObjectStreamException {
        // after deserialization we fix parent relationship in direct children
        for (final RobotKeywordCall call : getChildren()) {
            call.setParent(this);
            ((AModelElement<UserKeyword>) call.getLinkedElement()).setParent(keyword);
        }
        return this;
    }

    @Override
    public String toString() {
        // for debugging purposes only
        return getName();
    }
}
