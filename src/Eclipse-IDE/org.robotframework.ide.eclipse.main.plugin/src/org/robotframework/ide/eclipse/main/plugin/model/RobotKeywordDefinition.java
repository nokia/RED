/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toCollection;

import java.io.ObjectStreamException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jface.resource.ImageDescriptor;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.Documentation;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesTableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.KeywordTableModelUpdater;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.base.Splitter;

public class RobotKeywordDefinition extends RobotCodeHoldingElement<UserKeyword> {

    private static final long serialVersionUID = 1L;

    public RobotKeywordDefinition(final RobotKeywordsSection parent, final UserKeyword keyword) {
        super(parent, keyword);
    }

    @Override
    public IExecutablesTableModelUpdater<UserKeyword> getModelUpdater() {
        return new KeywordTableModelUpdater();
    }

    @Override
    protected ModelType getExecutableRowModelType() {
        return ModelType.USER_KEYWORD_EXECUTABLE_ROW;
    }

    @Override
    public RobotTokenType getSettingDeclarationTokenTypeFor(final String name) {
        return RobotTokenType.findTypeOfDeclarationForKeywordSettingTable(name);
    }

    public void link() {
        final UserKeyword keyword = getLinkedElement();

        for (final AModelElement<UserKeyword> el : keyword.getAllElements()) {
            if (el instanceof RobotExecutableRow) {
                getChildren().add(new RobotKeywordCall(this, el));
            } else if (el instanceof RobotEmptyRow) {
                getChildren().add(new RobotEmptyLine(this, el));
            } else {
                getChildren().add(new RobotDefinitionSetting(this, el));
            }
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

    public RobotDefinitionSetting getArgumentsSetting() {
        return findSetting(ModelType.USER_KEYWORD_ARGUMENTS).orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeUnitSettings(final RobotKeywordCall call) {
        getLinkedElement().removeElement((AModelElement<UserKeyword>) call.getLinkedElement());
    }

    public String getDocumentation() {
        return findSetting(ModelType.USER_KEYWORD_DOCUMENTATION).map(RobotKeywordCall::getLinkedElement)
                .map(KeywordDocumentation.class::cast)
                .map(DocumentationServiceHandler::toShowConsolidated)
                .orElse("<not documented>");
    }

    public Documentation createDocumentation() {
        // TODO : provide format depending on source
        final Set<String> keywords = getSuiteFile().getUserDefinedKeywords()
                .stream()
                .map(RobotKeywordDefinition::getName)
                .collect(toCollection(() -> new HashSet<>()));
        return new Documentation(DocFormat.ROBOT, getDocumentation(), keywords);
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

    public List<VariableDeclaration> getEmbeddedArguments() {
        final VariableExtractor extractor = new VariableExtractor();
        final MappingResult extractedVars = extractor.extract(getLinkedElement().getDeclaration(),
                getSuiteFile().getName());
        return extractedVars.getCorrectVariables();
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
    @Override
    public void moveChildDown(final RobotKeywordCall keywordCall) {
        final int index = keywordCall.getIndex();
        Collections.swap(getChildren(), index, index + 1);

        getLinkedElement().moveElementDown((AModelElement<UserKeyword>) keywordCall.getLinkedElement());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void moveChildUp(final RobotKeywordCall keywordCall) {
        final int index = keywordCall.getIndex();
        Collections.swap(getChildren(), index, index - 1);

        getLinkedElement().moveElementUp((AModelElement<UserKeyword>) keywordCall.getLinkedElement());
    }

    @SuppressWarnings("unchecked")
    private Object readResolve() throws ObjectStreamException {
        // after deserialization we fix parent relationship in direct children
        for (final RobotKeywordCall call : getChildren()) {
            call.setParent(this);
            ((AModelElement<UserKeyword>) call.getLinkedElement()).setParent(getLinkedElement());
        }
        return this;
    }

    @Override
    public String toString() {
        // for debugging purposes only
        return getName();
    }
}
