/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.io.ObjectStreamException;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesTableModelUpdater;
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

import com.google.common.base.Splitter;

public class RobotKeywordDefinition extends RobotCodeHoldingElement<UserKeyword> {

    private static final long serialVersionUID = 1L;

    public static final String ARGUMENTS = "Arguments";
    public static final String DOCUMENTATION = "Documentation";
    public static final String TAGS = "Tags";
    public static final String TIMEOUT = "Timeout";
    public static final String TEARDOWN = "Teardown";
    public static final String RETURN = "Return";

    private final UserKeyword keyword;
    
    public RobotKeywordDefinition(final RobotKeywordsSection parent, final UserKeyword keyword) {
        super(parent);
        this.keyword = keyword;
    }

    @Override
    public IExecutablesTableModelUpdater<UserKeyword> getModelUpdater() {
        return new KeywordTableModelUpdater();
    }

    @Override
    public UserKeyword getLinkedElement() {
        return keyword;
    }

    @Override
    public String getName() {
        return keyword.getDeclaration().getText();
    }

    @Override
    public RobotKeywordCall createKeywordCall(final int codeHoldingElementIndex, final String name,
            final List<String> args, final String comment) {
        final RobotExecutableRow<UserKeyword> robotExecutableRow = new RobotExecutableRow<>();
        robotExecutableRow.setParent(getLinkedElement());
        robotExecutableRow.setAction(RobotToken.create(name));
        for (int i = 0; i < args.size(); i++) {
            robotExecutableRow.setArgument(i, args.get(i));
        }

        final RobotKeywordCall call = new RobotKeywordCall(this, robotExecutableRow);

        final int modelTableIndex = countRowsOfTypeUpTo(ModelType.USER_KEYWORD_EXECUTABLE_ROW, codeHoldingElementIndex);
        if (modelTableIndex >= 0 && modelTableIndex < keyword.getKeywordExecutionRows().size()) {
            getLinkedElement().addKeywordExecutionRow(robotExecutableRow, modelTableIndex);
        } else {
            getLinkedElement().addKeywordExecutionRow(robotExecutableRow);
        }

        if (codeHoldingElementIndex >= 0 && codeHoldingElementIndex < getChildren().size()) {
            getChildren().add(codeHoldingElementIndex, call);
        } else {
            getChildren().add(call);
        }
        return call;
    }
    
    @Override
    public RobotDefinitionSetting createSetting(final int index, final String settingName, final List<String> args,
            final String comment) {
        final AModelElement<?> newModelElement = new KeywordTableModelUpdater().createSetting(getLinkedElement(),
                settingName, comment, args);
        final RobotDefinitionSetting setting = new RobotDefinitionSetting(this, newModelElement);

        if (newModelElement != null) {
            if (newModelElement.getModelType() == ModelType.USER_KEYWORD_SETTING_UNKNOWN) {
                newModelElement.getDeclaration().setText(settingName);
            }

            getChildren().add(index, setting);
        }
        return setting;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void insertKeywordCall(final int codeHoldingElementIndex, final RobotKeywordCall keywordCall) {

        RobotKeywordCall newCall = null;

        if (keywordCall.getLinkedElement().getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW
                || keywordCall.getLinkedElement().getModelType() == ModelType.UNKNOWN) {    // unknown when copy/paste empty lines
            newCall = new RobotKeywordCall(this, keywordCall.getLinkedElement());
            final RobotExecutableRow<UserKeyword> robotExecutableRow = (RobotExecutableRow<UserKeyword>) keywordCall
                    .getLinkedElement();
            final int modelTableIndex = countRowsOfTypeUpTo(ModelType.USER_KEYWORD_EXECUTABLE_ROW, codeHoldingElementIndex);
            if (modelTableIndex >= 0 && modelTableIndex < keyword.getKeywordExecutionRows().size()) {
                keyword.addKeywordExecutionRow(robotExecutableRow, modelTableIndex);
            } else {
                keyword.addKeywordExecutionRow(robotExecutableRow);
            }
        } else {
            newCall = new RobotDefinitionSetting(this, keywordCall.getLinkedElement());
            new KeywordTableModelUpdater().insert(keyword, 0, keywordCall.getLinkedElement());
        }

        if (codeHoldingElementIndex >= 0 && codeHoldingElementIndex < getChildren().size()) {
            getChildren().add(codeHoldingElementIndex, newCall);
        } else {
            getChildren().add(newCall);
        }
    }

    public void link() {
        // settings
        for (final KeywordArguments argument : keyword.getArguments()) {
            getChildren().add(new RobotDefinitionSetting(this, argument));
        }
        for (final KeywordDocumentation documentation : keyword.getDocumentation()) {
            getChildren().add(new RobotDefinitionSetting(this, documentation));
        }
        for (final KeywordTags tags : keyword.getTags()) {
            getChildren().add(new RobotDefinitionSetting(this, tags));
        }
        for (final KeywordTimeout timeout : keyword.getTimeouts()) {
            getChildren().add(new RobotDefinitionSetting(this, timeout));
        }
        for (final KeywordTeardown teardown : keyword.getTeardowns()) {
            getChildren().add(new RobotDefinitionSetting(this, teardown));
        }
        for (final KeywordReturn returnSetting : keyword.getReturns()) {
            getChildren().add(new RobotDefinitionSetting(this, returnSetting));
        }
        
        // body
        for (final RobotExecutableRow<UserKeyword> execRow : keyword.getKeywordExecutionRows()) {
            getChildren().add(new RobotKeywordCall(this, execRow));
        }
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
