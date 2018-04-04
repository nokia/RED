/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationViewLinksSupport.OpenableUri;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

public class LibraryUri implements OpenableUri {

    private static final String SCHEME = "library";
    private static final String SHOW_DOC_PARAM = "show_doc";
    private static final String SHOW_SRC_PARAM = "show_source";

    public static boolean isLibraryDocUri(final URI uri) {
        return uri.getScheme().equals(SCHEME) && uri.getQuery() != null && uri.getQuery().contains(SHOW_DOC_PARAM);
    }

    public static boolean isLibrarySourceUri(final URI uri) {
        return uri.getScheme().equals(SCHEME) && uri.getQuery() != null && uri.getQuery().contains(SHOW_SRC_PARAM);
    }

    public static URI createShowLibraryDocUri(final String projectName, final String libraryName)
            throws URISyntaxException {
        final Map<String, String> values = ImmutableMap.of(SHOW_DOC_PARAM, Boolean.toString(true));
        return createUri(newArrayList(projectName, libraryName), values);
    }

    public static URI createShowLibrarySourceUri(final String projectName, final String libraryName)
            throws URISyntaxException {
        final Map<String, String> values = ImmutableMap.of(SHOW_SRC_PARAM, Boolean.toString(true));
        return createUri(newArrayList(projectName, libraryName), values);
    }

    public static URI createShowKeywordDocUri(final String projectName, final String libraryName,
            final String keywordName) throws URISyntaxException {
        final Map<String, String> values = ImmutableMap.of(SHOW_DOC_PARAM, Boolean.toString(true));
        return createUri(newArrayList(projectName, libraryName, keywordName), values);
    }

    public static URI createShowKeywordSourceUri(final String projectName, final String libraryName,
            final String keywordName) throws URISyntaxException {
        final Map<String, String> values = ImmutableMap.of(SHOW_SRC_PARAM, Boolean.toString(true));
        return createUri(newArrayList(projectName, libraryName, keywordName), values);
    }

    private static URI createUri(final List<String> segments, final Map<String, String> values)
            throws URISyntaxException {
        final String path = "/" + String.join("/", segments);
        final String query = Joiner.on('&').withKeyValueSeparator('=').join(values);
        return new URI(SCHEME, null, path, query, null);
    }

    private final URI uri;

    private final SpecificationsConsumer specsConsumer;

    LibraryUri(final URI uri, final SpecificationsConsumer specsConsumer) {
        this.uri = uri;
        this.specsConsumer = specsConsumer;
    }

    @Override
    public void open() {
        final String[] path = uri.getPath().split("/");

        final String projectName = path[1];
        final String libName = path[2];
        final String kwName = path.length >= 4 ? path[3] : null;

        final RobotModel model = RedPlugin.getModelManager().getModel();
        final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        final IProject project = wsRoot.getProject(projectName);
        final RobotProject robotProject = model.createRobotProject(project);

        final Optional<LibrarySpecification> libSpec = robotProject.getLibrarySpecificationsStream()
                .filter(s -> s.getName().equals(libName))
                .findFirst();
        final Optional<KeywordSpecification> kwSpec = libSpec.map(Stream::of)
                .orElseGet(Stream::empty)
                .flatMap(LibrarySpecification::getKeywordsStream)
                .filter(s -> s.getName().equals(kwName))
                .findFirst();

        specsConsumer.accept(robotProject, libSpec, kwSpec);
    }

    @FunctionalInterface
    static interface SpecificationsConsumer {

        void accept(RobotProject project, Optional<LibrarySpecification> libSpec,
                Optional<KeywordSpecification> kwSpec);

    }
}