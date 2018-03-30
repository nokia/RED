/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.SourceOpeningSupport;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordDefinitionInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordSpecificationInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.LibrarySpecificationInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SuiteFileInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TestCaseInput;

class DocumentationViewLinksListener implements LocationListener {

    private final IWorkbenchPage page;
    private final IWorkbenchBrowserSupport browserSupport;
    private final DocumentationView view;

    public DocumentationViewLinksListener(final IWorkbenchPage page, final IWorkbenchBrowserSupport browserSupport,
            final DocumentationView view) {
        this.page = page;
        this.browserSupport = browserSupport;
        this.view = view;
    }

    @Override
    public void changing(final LocationEvent event) {
        try {
            event.doit = false;
            changingLocationTo(event);

        } catch (final UnableToOpenUriException e) {
            StatusManager.getManager().handle(
                    new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, "Cannot open '" + event.location + "'", e),
                    StatusManager.BLOCK);
        }
    }

    private void changingLocationTo(final LocationEvent event) {
        final URI locationUri = toUri(event.location);
        if (isAboutBlankUri(locationUri)) {
            // moving to generated site set by Browser#setText method
            event.doit = true;
        } else {
            final OpenableUri uriWrapper = createOpenableUri(locationUri);
            uriWrapper.open();
        }
    }

    private OpenableUri createOpenableUri(final URI locationUri) {
        if (FragmentUri.isAboutBlankFragmentUri(locationUri)) {
            return new FragmentUri(locationUri, this::executeScript);

        } else if (LibraryUri.isLibrarySourceUri(locationUri)) {
            return new LibraryUri(locationUri, this::openLibrarySource);

        } else if (LibraryUri.isLibraryDocUri(locationUri)) {
            return new LibraryUri(locationUri, this::openLibraryDoc);

        } else if (WorkspaceFileUri.isFileUri(locationUri)) {
            return new WorkspaceFileUri(locationUri, this::openFile);

        } else if (WorkspaceFileUri.isFileSrcUri(locationUri)) {
            return new WorkspaceFileUri(locationUri, this::openFileSource);

        } else if (WorkspaceFileUri.isFileDocUri(locationUri)) {
            return new WorkspaceFileUri(locationUri, this::openFileDoc);

        } else {
            // all other links will be handled by workbench browser
            return new ExternalBrowserUri(locationUri, browserSupport);
        }
    }

    private URI toUri(final String location) {
        try {
            return new URI(location);
        } catch (final URISyntaxException e) {
            throw new UnableToOpenUriException("Syntax error in uri '" + location + "'", e);
        }
    }

    private static boolean isAboutBlankUri(final URI uri) {
        return Objects.equals(uri.getScheme(), "about") && Objects.equals(uri.getSchemeSpecificPart(), "blank")
                && uri.getFragment() == null;
    }

    private void executeScript(final String script) {
        view.getBrowser().execute(script);
    }

    private void openLibrarySource(final RobotProject robotProject, final Optional<LibrarySpecification> libSpec,
            final Optional<KeywordSpecification> kwSpec) {

        final IProject project = robotProject.getProject();
        final RobotModel model = (RobotModel) robotProject.getParent();
        if (libSpec.isPresent() && kwSpec.isPresent()) {
            SourceOpeningSupport.open(page, model, project, libSpec.get(), kwSpec.get());

        } else if (libSpec.isPresent()) {
            SourceOpeningSupport.open(page, model, project, libSpec.get());

        } else {
            throw new UnableToOpenUriException("Unable to open library source. Cannot find given library/keyword");
        }
    }

    private void openLibraryDoc(final RobotProject robotProject, final Optional<LibrarySpecification> libSpec,
            final Optional<KeywordSpecification> kwSpec) {
        if (libSpec.isPresent() && kwSpec.isPresent()) {
            view.displayDocumentation(new KeywordSpecificationInput(robotProject, libSpec.get(), kwSpec.get()));

        } else if (libSpec.isPresent()) {
            view.displayDocumentation(new LibrarySpecificationInput(robotProject, libSpec.get()));

        } else {
            throw new UnableToOpenUriException(
                    "Unable to open library documentation. Cannot find given library/keyword");
        }
    }

    private void openFile(final Optional<IFile> file, @SuppressWarnings("unused") final Map<String, String> params) {
        if (file.isPresent()) {
            SourceOpeningSupport.tryToOpenInEditor(page, file.get());
        } else {
            throw new UnableToOpenUriException("Unable to find given file in workspace");
        }
    }

    private void openFileSource(final Optional<IFile> file, final Map<String, String> params) {
        final Optional<? extends RobotFileInternalElement> element = findElement(file, params);
        if (element.isPresent()) {
            element.get().getOpenRobotEditorStrategy().run(page);
        } else {
            throw new UnableToOpenUriException("Unable to find given element");
        }
    }

    private void openFileDoc(final Optional<IFile> file, final Map<String, String> params) {
        final Optional<? extends RobotFileInternalElement> element = findElement(file, params);

        Optional<? extends DocumentationViewInput> input;
        if (params.containsKey(WorkspaceFileUri.SUITE_PARAM)) {
            input = element.map(e -> new SuiteFileInput((RobotSuiteFile) e));

        } else if (params.containsKey(WorkspaceFileUri.KEYWORD_PARAM)) {
            input = element.map(e -> new KeywordDefinitionInput((RobotKeywordDefinition) e));

        } else if (params.containsKey(WorkspaceFileUri.TEST_PARAM)) {
            input = element.map(e -> new TestCaseInput((RobotCase) e));

        } else {
            input = Optional.empty();
        }

        if (input.isPresent()) {
            view.displayDocumentation(input.get());
        } else {
            throw new UnableToOpenUriException("Unable to find given element");
        }
    }

    private Optional<? extends RobotFileInternalElement> findElement(final Optional<IFile> file,
            final Map<String, String> params) {
        final RobotModel model = RedPlugin.getModelManager().getModel();
        final Stream<RobotSuiteFile> fileModel = file.map(model::createSuiteFile).map(Stream::of).orElseGet(
                () -> Stream.empty());

        if (params.containsKey(WorkspaceFileUri.SUITE_PARAM)) {
            return fileModel.findFirst();

        } else if (params.containsKey(WorkspaceFileUri.KEYWORD_PARAM)) {
            return fileModel.map(RobotSuiteFile::getUserDefinedKeywords)
                    .flatMap(Collection::stream)
                    .filter(kw -> kw.getName().equals(params.get(WorkspaceFileUri.KEYWORD_PARAM)))
                    .findFirst();
        } else if (params.containsKey(WorkspaceFileUri.TEST_PARAM)) {
            return fileModel.map(RobotSuiteFile::getTestCases)
                    .flatMap(Collection::stream)
                    .filter(test -> test.getName().equals(params.get(WorkspaceFileUri.TEST_PARAM)))
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void changed(final LocationEvent event) {
        // nothing to do
    }

    @FunctionalInterface
    static interface OpenableUri {

        void open() throws UnableToOpenUriException;
    }

    static class UnableToOpenUriException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public UnableToOpenUriException(final String message) {
            super(message);
        }

        public UnableToOpenUriException(final String message, final Exception cause) {
            super(message, cause);
        }
    }
}