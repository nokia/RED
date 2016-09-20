/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.resource.ImageDescriptor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesTableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.KeywordTableModelUpdater;
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
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;

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

        final List<PrioriterizedKeywordsSettings> settings = newArrayList(EnumSet.allOf(PrioriterizedKeywordsSettings.class));
        Collections.sort(settings);

        // settings
        for (final PrioriterizedKeywordsSettings setting : settings) {
            for (final AModelElement<UserKeyword> element : setting.getModelElements(keyword)) {
                getChildren().add(new RobotDefinitionSetting(this, element));
            }
        }
        
        // body
        for (final RobotExecutableRow<UserKeyword> execRow : keyword.getKeywordExecutionRows()) {
            getChildren().add(new RobotKeywordCall(this, execRow));
        }
    }

    @Override
    public void fixChildrenOrder() {
        Collections.sort(getChildren(), new Comparator<RobotKeywordCall>() {

            @Override
            public int compare(final RobotKeywordCall call1, final RobotKeywordCall call2) {
                if (call1 instanceof RobotDefinitionSetting && call2 instanceof RobotDefinitionSetting) {
                    final ModelType modelType1 = call1.getLinkedElement().getModelType();
                    final ModelType modelType2 = call2.getLinkedElement().getModelType();
                    return PrioriterizedKeywordsSettings.from(modelType1)
                            .compareTo(PrioriterizedKeywordsSettings.from(modelType2));
                } else if (call1 instanceof RobotDefinitionSetting && !(call2 instanceof RobotDefinitionSetting)) {
                    return -1;
                } else if (!(call1 instanceof RobotDefinitionSetting) && call2 instanceof RobotDefinitionSetting) {
                    return 1;
                } else {
                    // sort is stable so we don't care about those cases
                    return 0;
                }
            }
        });
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
        return findSetting(ModelType.USER_KEYWORD_ARGUMENTS);
    }

    public String getDocumentation() {
        final RobotDefinitionSetting documentationSetting = findSetting(ModelType.USER_KEYWORD_DOCUMENTATION);
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

    @Override
    public void moveChildDown(final RobotKeywordCall keywordCall) {
        final int index = keywordCall.getIndex();
        Collections.swap(getChildren(), index, index + 1);

        @SuppressWarnings("unchecked")
        final RobotExecutableRow<UserKeyword> linkedCall = (RobotExecutableRow<UserKeyword>) keywordCall
                .getLinkedElement();
        getLinkedElement().moveDownExecutableRow(linkedCall);
    }

    @Override
    public void moveChildUp(final RobotKeywordCall keywordCall) {
        final int index = keywordCall.getIndex();
        Collections.swap(getChildren(), index, index - 1);

        @SuppressWarnings("unchecked")
        final RobotExecutableRow<UserKeyword> linkedCall = (RobotExecutableRow<UserKeyword>) keywordCall
                .getLinkedElement();
        getLinkedElement().moveUpExecutableRow(linkedCall);
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

    static enum PrioriterizedKeywordsSettings {
        // the order is defined by enums definitions order (see Enum.compareTo() method javadoc)
        ARGUMENTS,
        DOCUMENTATION,
        TAGS,
        TIMEOUT,
        TEARDOWN,
        RETURN,
        UNKNOWN;

        public List<? extends AModelElement<UserKeyword>> getModelElements(final UserKeyword keyword) {
            switch (this) {
                case ARGUMENTS: return keyword.getArguments();
                case DOCUMENTATION: return keyword.getDocumentation();
                case TAGS: return keyword.getTags();
                case TIMEOUT: return keyword.getTimeouts();
                case TEARDOWN: return keyword.getTeardowns();
                case RETURN: return keyword.getReturns();
                case UNKNOWN: return keyword.getUnknownSettings();
                default: return new ArrayList<>();
            }
        }

        public static PrioriterizedKeywordsSettings from(final ModelType modelType) {
            switch (modelType) {
                case USER_KEYWORD_ARGUMENTS: return ARGUMENTS;
                case USER_KEYWORD_DOCUMENTATION: return DOCUMENTATION;
                case USER_KEYWORD_TAGS: return TAGS;
                case USER_KEYWORD_TIMEOUT: return TIMEOUT;
                case USER_KEYWORD_TEARDOWN: return TEARDOWN;
                case USER_KEYWORD_RETURN: return RETURN;
                case USER_KEYWORD_SETTING_UNKNOWN: return UNKNOWN;
                default:
                    throw new IllegalArgumentException("No setting defined for " + modelType.name());
            }
        }
    }
}
