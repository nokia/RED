/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;

import static java.util.Collections.unmodifiableSet;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SuiteContext extends DefaultContext {

    private final String suiteName;

    private final URI locationUri;

    private final boolean isDirectory;

    private final String errorMessage;

    private final Function<URI, Optional<RobotFile>> associatedFileModelProvider;

    private final LinkedHashSet<URI> loadedResources;

    public SuiteContext(final String suiteName) {
        this(suiteName, null, true, null, uri -> Optional.empty());
    }

    public SuiteContext(final String suiteName, final boolean isDirectory, final String errorMessage) {
        this(suiteName, null, isDirectory, errorMessage, uri -> Optional.empty());
    }

    public SuiteContext(final String suiteName, final URI locationUri, final boolean isDirectory,
            final String errorMessage) {
        this(suiteName, locationUri, isDirectory, errorMessage, uri -> Optional.empty());
    }

    public SuiteContext(final String suiteName, final URI locationUri, final boolean isDirectory,
            final Function<URI, Optional<RobotFile>> associatedFileModelProvider) {
        this(suiteName, locationUri, isDirectory, null, associatedFileModelProvider);
    }

    private SuiteContext(final String suiteName, final URI locationUri, final boolean isDirectory,
            final String errorMessage,
            final Function<URI, Optional<RobotFile>> associatedFileModelProvider) {
        this.suiteName = suiteName;
        this.locationUri = locationUri;
        this.isDirectory = isDirectory;
        this.errorMessage = errorMessage;
        this.loadedResources = new LinkedHashSet<>();
        this.associatedFileModelProvider = associatedFileModelProvider;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public Set<URI> getLoadedResources() {
        return unmodifiableSet(loadedResources);
    }

    public void addLoadedResource(final URI resourceUri) {
        loadedResources.add(resourceUri);
    }

    public void addLoadedResources(final Collection<URI> resourceUris) {
        loadedResources.addAll(resourceUris);
    }

    @Override
    public boolean isErroneous() {
        return errorMessage != null;
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    @Override
    public Optional<URI> getAssociatedPath() {
        return Optional.ofNullable(locationUri);
    }

    @Override
    public StackFrameContext moveTo(final RunningKeyword keyword, final RobotBreakpointSupplier breakpointSupplier) {
        if (keyword.isSetup() || keyword.isTeardown()) {

            if (isErroneous()) {
                final String errorMsg = String.format(
                        ErrorMessages.errorOfSuitePrePostKwNotFound(keyword.isSetup()), keyword.asCall());
                return new SetupTeardownContext(errorMsg, this);
            }

            final Optional<RobotFile> fileModel = associatedFileModelProvider.apply(locationUri);
            if (!fileModel.isPresent()) {
                // this can only happen for directory-suites; the directory can exist so
                // SuiteDirContext was found, but __init__ file does not
                final String msg = ErrorMessages.errorOfSuitePrePostKwNotFoundBecauseOfMissingInit(keyword.isSetup());
                final String errorMsg = String.format(msg, keyword.asCall(), suiteName,
                        new File(getAssociatedPath().get()).getAbsolutePath());
                return new SetupTeardownContext(errorMsg, this);
            }
            final URI fileUri = fileModel.get().getParent().getProcessedFile().toURI();

            final SettingTable settingsTable = fileModel.get().getSettingTable();
            if (settingsTable == null) {
                final String msg = ErrorMessages
                        .errorOfSuitePrePostKwNotFoundBecauseOfMissingSetting(keyword.isSetup());
                final String errorMsg = String.format(msg, keyword.asCall());
                return new SetupTeardownContext(fileUri, 1, errorMsg, this);
            }

            final List<? extends AKeywordBaseSetting<SettingTable>> setupTeardowns = keyword.isSetup()
                    ? settingsTable.getSuiteSetups()
                    : settingsTable.getSuiteTeardowns();
            final AKeywordBaseSetting<?> setting = setupTeardowns.isEmpty() ? null : setupTeardowns.get(0);

            if (setting == null) {
                final String msg = ErrorMessages
                        .errorOfSuitePrePostKwNotFoundBecauseOfMissingSetting(keyword.isSetup());
                final String errorMsg = String.format(msg, keyword.asCall());
                final int line = settingsTable.getHeaders().get(0).getTableHeader().getLineNumber();

                return new SetupTeardownContext(fileUri, line, errorMsg, this);
            }

            final RobotToken keywordToken = setting.getKeywordName();
            if (keywordToken == null || keywordToken.getText().isEmpty()) {
                final String msg = ErrorMessages
                        .errorOfSuitePrePostKwNotFoundBecauseOfMissingSetting(keyword.isSetup());
                final String errorMsg = String.format(msg, keyword.asCall());
                final int line = setting.getDeclaration().getLineNumber();

                return new SetupTeardownContext(fileUri, line, errorMsg, this, breakpointSupplier);
            }

            if (CallChecker.isCallOf(keywordToken.getText(), keyword)) {
                return new SetupTeardownContext(fileUri, keywordToken.getLineNumber(), this, breakpointSupplier);
            } else {
                final String msg = ErrorMessages.errorOfSuitePrePostKwNotFoundBecauseOfDifferentCall(keyword.isSetup());
                final String errorMsg = String.format(msg, keyword.asCall(), keywordToken.getText());
                return new SetupTeardownContext(fileUri, keywordToken.getLineNumber(), errorMsg, this,
                        breakpointSupplier);
            }
        } else {
            throw new IllegalDebugContextStateException(
                    "Only suite setup or teardown keyword call is possible in current context");
        }
    }
}
