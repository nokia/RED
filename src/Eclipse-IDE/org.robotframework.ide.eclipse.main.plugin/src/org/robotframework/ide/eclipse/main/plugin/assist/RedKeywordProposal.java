/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Joiner;

public class RedKeywordProposal {

    private final String sourceName;
    private final KeywordType type;
    private final String name;
    private final String decoration;
    private final boolean hasDescription;
    private final String documentation;

    private final LazyProvider<List<String>> argumentsProvider;
    private final LazyProvider<String> htmlDocumentationProvider;

    private RedKeywordProposal(final String sourceName, final KeywordType type, final String name,
            final String decoration, final boolean hasDescription, final LazyProvider<List<String>> argumentsProvider,
            final LazyProvider<String> htmlDocumentationProvider, final String documentation) {
        this.sourceName = sourceName;
        this.type = type;
        this.name = name;
        this.decoration = decoration;
        this.hasDescription = hasDescription;
        this.documentation = documentation;

        this.htmlDocumentationProvider = htmlDocumentationProvider;
        this.argumentsProvider = argumentsProvider;
    }

    static RedKeywordProposal create(final LibrarySpecification spec, final KeywordSpecification keyword) {
        final String libName = spec.getName();
        final KeywordType type = "Reserved".equals(libName) ? KeywordType.RESERVED : KeywordType.STD_LIBRARY;
        final boolean hasDescription = !"Reserved".equals(libName);

        final LazyProvider<String> htmlDocuProvider = new LazyProvider<String>() {
            @Override
            public String provide() {
                return keyword.getDocumentationAsHtml();
            }
        };
        final LazyProvider<List<String>> argsProvider = new LazyProvider<List<String>>() {
            @Override
            public List<String> provide() {
                return keyword.getArguments() == null ? new ArrayList<String>() : keyword.getArguments();
            }
        };
        return new RedKeywordProposal(libName, type, keyword.getName(), "- " + libName, hasDescription, argsProvider,
                htmlDocuProvider, keyword.getDocumentation());
    }

    static RedKeywordProposal create(final RobotKeywordDefinition userKeyword) {
        return createUserDefinedProposal(userKeyword, "- user defined");
    }

    static RedKeywordProposal createExternal(final RobotSuiteFile file, final RobotKeywordDefinition userKeyword) {
        return createUserDefinedProposal(userKeyword, "- " + file.getName());
    }

    private static RedKeywordProposal createUserDefinedProposal(final RobotKeywordDefinition userKeyword,
            final String decoration) {
        final LazyProvider<String> htmlDocuProvider = new LazyProvider<String>() {
            @Override
            public String provide() {
                return "<p>to be implemented</p>";
            }
        };
        final LazyProvider<List<String>> argsProvider = new LazyProvider<List<String>>() {
            @Override
            public List<String> provide() {
                return Arrays.asList("not", "yet", "implemented");
            }
        };
        return new RedKeywordProposal("User Defined", KeywordType.USER_DEFINED, userKeyword.getName(), decoration, true,
                argsProvider, htmlDocuProvider, "to be implemented");
    }

    public String getSourceName() {
        return sourceName;
    }

    public KeywordType getType() {
        return type;
    }

    public String getLabel() {
        return name;
    }

    public String getLabelDecoration() {
        return decoration;
    }

    public String getContent() {
        return name;
    }

    public boolean hasDescription() {
        return hasDescription;
    }

    public ImageDescriptor getImage() {
        return type.getImage();
    }

    public String getHtmlDocumentation() {
        return htmlDocumentationProvider.provide();
    }

    public String getArgumentsLabel() {
        return "[" + Joiner.on(" | ").join(argumentsProvider.provide()) + "]";
    }

    private static interface LazyProvider<T> {
        T provide();
    }

    public String getDocumentation() {
        return documentation;
    }

    public enum KeywordType {
        RESERVED(null), STD_LIBRARY(RedImages.getKeywordImage()), USER_DEFINED(RedImages.getUserKeywordImage());

        private ImageDescriptor image;

        private KeywordType(final ImageDescriptor image) {
            this.image = image;
        }

        private ImageDescriptor getImage() {
            return image;
        }
    }
}
