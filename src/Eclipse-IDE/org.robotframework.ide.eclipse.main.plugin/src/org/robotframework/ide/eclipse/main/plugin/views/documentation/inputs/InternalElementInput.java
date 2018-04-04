/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IWorkbenchPage;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.libs.LibrariesBuilder;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsFormatter;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.WorkspaceFileUri;


abstract class InternalElementInput<T extends RobotFileInternalElement> extends DocumentationViewInput {

    protected final T element;

    protected final String selectedLabel;

    private final List<Integer> indexesPath;

    protected InternalElementInput(final T element) {
        this(element, null);
    }

    protected InternalElementInput(final T element, final String selectedLabel) {
        this.element = element;
        this.selectedLabel = selectedLabel;
        this.indexesPath = createIndexesPath(element);
    }

    @Override
    public boolean contains(final Object wrappedInput) {
        return wrappedInput == element;
    }

    @Override
    public void prepare() {
        // nothing to prepare
    }

    @Override
    public final String provideHtml() {
        final RobotRuntimeEnvironment environment = element.getSuiteFile().getProject().getRuntimeEnvironment();
        final String header = createHeader();
        final Documentation doc = createDocumentation();

        return new DocumentationsFormatter(environment).format(header, doc, this::localKeywordsLinker);
    }

    protected abstract String createHeader();

    protected abstract Documentation createDocumentation();

    protected String localKeywordsLinker(final String name) {
        try {
            final IFile file = element.getSuiteFile().getFile();
            return WorkspaceFileUri.createShowKeywordDocUri(file, name).toString();
        } catch (final URISyntaxException e) {
            return "#";
        }
    }

    private static <T extends RobotFileInternalElement> List<Integer> createIndexesPath(final T element) {
        final List<Integer> address = new ArrayList<>();

        RobotElement current = element;
        while (!(current instanceof RobotSuiteFile)) {
            address.add(0, current.getParent().getChildren().indexOf(current));
            current = current.getParent();
        }
        return address;
    }

    @Override
    public final void showInput(final IWorkbenchPage page) {
        final boolean stillExist = elementStillExists();
        if (stillExist) {
            element.getOpenRobotEditorStrategy().run(page, selectedLabel);
        } else {
            // the file could have been reparsed, so that the element is no longer part
            // of the actual model; we are trying to find something that has the same
            // indexes path in same RobotSuiteModel, or throw an exception if it is
            // not possible

            final Supplier<? extends DocumentationInputOpenException> exceptionSupplier =
                    () -> new DocumentationInputOpenException("Unable to open given input. It looks like element '"
                            + element.getName() + "' does no longer exist");
            final RobotFileInternalElement elementWithSameAddress = findElementWithSameAddress()
                    .orElseThrow(exceptionSupplier);

            if (elementsPathToFileHaveMatchingNamesAndClasses(element, elementWithSameAddress)) {
                elementWithSameAddress.getOpenRobotEditorStrategy().run(page, selectedLabel);
            } else {
                throw exceptionSupplier.get();
            }
        }
    }

    private boolean elementStillExists() {
        RobotElement current = element;
        while (!(current instanceof RobotSuiteFile)) {
            if (!containsExactlyThisInstance(current)) {
                return false;
            }
            current = current.getParent();
        }
        return true;
    }

    private boolean containsExactlyThisInstance(final RobotElement current) {
        for (final RobotElement child : current.getParent().getChildren()) {
            if (child == current) {
                return true;
            }
        }
        return false;
    }

    private Optional<RobotFileInternalElement> findElementWithSameAddress() {
        RobotFileInternalElement current = element.getSuiteFile();

        for (final int index : indexesPath) {
            if (0 <= index && index < current.getChildren().size()) {
                current = (RobotFileInternalElement) current.getChildren().get(index);
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(current);
    }

    private static <T extends RobotFileInternalElement> boolean elementsPathToFileHaveMatchingNamesAndClasses(
            final T element1, final T element2) {
        RobotFileInternalElement current1 = element1;
        RobotFileInternalElement current2 = element2;

        while (!(current1 instanceof RobotSuiteFile) && !(current2 instanceof RobotSuiteFile)) {
            if (current1.getClass() != current2.getClass() || !current1.getName().equals(current2.getName())) {
                return false;
            }
            current1 = (RobotFileInternalElement) current1.getParent();
            current2 = (RobotFileInternalElement) current2.getParent();
        }
        return true;
    }

    @Override
    public IFile generateHtmlLibdoc() {
        return new LibrariesBuilder(new BuildLogger()).buildHtmlLibraryDoc(element.getSuiteFile().getFile());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        } else {
            final InternalElementInput<?> that = (InternalElementInput<?>) obj;
            return this.element.equals(that.element) && Objects.equals(this.selectedLabel, that.selectedLabel);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(element, selectedLabel);
    }
}
