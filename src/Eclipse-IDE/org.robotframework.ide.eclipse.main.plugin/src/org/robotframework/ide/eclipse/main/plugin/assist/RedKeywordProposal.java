/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.custom.StyleRange;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor.Argument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Range;

public class RedKeywordProposal extends KeywordEntity implements AssistProposal {

    private final String bddPrefix;

    private final String documentation;

    private final ProposalMatch match;

    // calculating if keyword should be qualified may be time consuming, so instead of precomputing
    // this information for each proposal we're pushing the calculation into this lambda, which is
    // calculated only when proposal is chosen and the decision about qualified name has to be taken
    // see RED-474 issue
    private final Predicate<RedKeywordProposal> shouldUseQualifiedName;

    // micro-cached content
    private String content;

    @VisibleForTesting
    RedKeywordProposal(final String sourceName, final String sourceAlias, final KeywordScope scope,
            final String bddPrefix, final String name, final ArgumentsDescriptor argumentsDescriptor,
            final String documentation, final boolean isDeprecated, final IPath exposingFilePath,
            final Predicate<RedKeywordProposal> shouldUseQualifiedName, final ProposalMatch match) {

        super(scope, sourceName, name, sourceAlias, isDeprecated, argumentsDescriptor, exposingFilePath);
        this.bddPrefix = bddPrefix;

        this.documentation = documentation;

        this.shouldUseQualifiedName = shouldUseQualifiedName;
        this.match = match;
    }

    @Override
    public String getContent() {
        if (content == null) {
            if (shouldUseQualifiedName.apply(this)) {
                content = bddPrefix + getAlias() + "." + getNameFromDefinition();
            } else {
                content = bddPrefix + getNameFromDefinition();
            }
        }
        return content;
    }

    @Override
    public List<String> getArguments() {
        if (EmbeddedKeywordNamesSupport.hasEmbeddedArguments(getNameFromDefinition())) {
            return new ArrayList<>();
        } else {
            final List<String> arguments = newArrayList(
                    transform(getArgumentsDescriptor().getRequiredArguments(), new Function<Argument, String>() {

                        @Override
                        public String apply(final Argument arg) {
                            return arg.getName();
                        }
                    }));

            final Range<Integer> noOfArgs = getArgumentsDescriptor().getPossibleNumberOfArguments();
            final boolean mayHaveMoreArguments = !noOfArgs.hasUpperBound()
                    || noOfArgs.upperEndpoint() > arguments.size();
            if (mayHaveMoreArguments) {
                arguments.add("");
            }
            return arguments;
        }
    }

    @Override
    public ImageDescriptor getImage() {
        return isUserKeyword() ? RedImages.getUserKeywordImage() : RedImages.getKeywordImage();
    }

    @Override
    public String getLabel() {
        if (isUserKeyword()) {
            return getNameFromDefinition() + " - " + getExposingFilepath().lastSegment();
        } else {
            return getNameFromDefinition() + " - " + getAlias();
        }
    }

    @Override
    public StyledString getStyledLabel() {
        final StyledString label = new StyledString(getNameFromDefinition(),
                isDeprecated() ? Stylers.Common.STRIKEOUT_STYLER : Stylers.Common.EMPTY_STYLER);
        for (final Range<Integer> matchingRange : match) {
            for (final StyleRange styleRange : label.getStyleRanges()) {
                final int length = Math.min(matchingRange.upperEndpoint() - matchingRange.lowerEndpoint(),
                        label.length() - matchingRange.lowerEndpoint());
                label.setStyle(matchingRange.lowerEndpoint(), length,
                        Stylers.mixingStyler(styleRange, Stylers.Common.MARKED_PREFIX_STYLER));
            }
        }
        if (isUserKeyword()) {
            label.append(" - " + getExposingFilepath().lastSegment(), Stylers.Common.ECLIPSE_DECORATION_STYLER);
        } else {
            label.append(" - " + getAlias(), Stylers.Common.ECLIPSE_DECORATION_STYLER);
        }
        return label;
    }

    @Override
    public boolean hasDescription() {
        return true;
    }

    @Override
    public String getDescription() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Name: ").append(getNameFromDefinition()).append("\n");
        final String source;
        if (isUserKeyword()) {
            source = "User defined (" + getExposingFilepath().toString() + ")";
        } else if (getAlias().equals(getSourceName())) {
            source = "Library (" + getSourceName() + ")";
        } else {
            source = "Library (" + getAlias() + " - alias for " + getSourceName() + ")";
        }
        builder.append("Source: ").append(source).append("\n");
        builder.append("Arguments: ").append(getArgumentsDescriptor().getDescription()).append("\n\n");
        builder.append(documentation);

        return builder.toString();
    }

    private boolean isUserKeyword() {
        final KeywordScope scope = getScope(getExposingFilepath());
        return scope == KeywordScope.LOCAL || scope == KeywordScope.RESOURCE;
    }
}
