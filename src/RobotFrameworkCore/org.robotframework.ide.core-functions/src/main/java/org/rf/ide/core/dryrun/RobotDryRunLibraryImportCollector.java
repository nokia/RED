/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.dryrun.JsonMessageMapper.JsonMessageMapperException;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.execution.agent.event.LibraryImportEvent;
import org.rf.ide.core.execution.agent.event.MessageEvent;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author mmarzec
 */
public class RobotDryRunLibraryImportCollector {

    private static final String MESSAGE_KEY = "import_error";

    private static final TypeReference<Map<String, RobotDryRunLibraryError>> MESSAGE_TYPE = new TypeReference<Map<String, RobotDryRunLibraryError>>() {
    };

    private final List<RobotDryRunLibraryImport> importedLibraries = new LinkedList<>();

    private final Set<String> erroneousLibraryNames = new HashSet<>();

    private final Set<String> standardLibraryNames;

    public RobotDryRunLibraryImportCollector(final Set<String> standardLibraryNames) {
        this.standardLibraryNames = standardLibraryNames;
    }

    public void collectFromLibraryImportEvent(final LibraryImportEvent event) {
        if (event.getImporter().isPresent() && !erroneousLibraryNames.contains(event.getName())
                && !standardLibraryNames.contains(event.getName())) {
            if (event.getSource().isPresent()) {
                importedLibraries.add(new RobotDryRunLibraryImport(event.getName(), event.getSource().get(),
                        event.getImporter().get(), event.getArguments()));
            } else {
                importedLibraries.add(
                        new RobotDryRunLibraryImport(event.getName(), event.getImporter().get(), event.getArguments()));
            }
        }
    }

    public void collectFromMessageEvent(final MessageEvent event) {
        try {
            JsonMessageMapper.readValue(event, MESSAGE_KEY, MESSAGE_TYPE).ifPresent(importError -> {
                final RobotDryRunLibraryImport dryRunLibraryImport = new RobotDryRunLibraryImport(
                        importError.getName());
                dryRunLibraryImport.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
                dryRunLibraryImport.setAdditionalInfo(formatAdditionalInfo(importError.getError()));
                importedLibraries.add(dryRunLibraryImport);
                erroneousLibraryNames.add(importError.getName());
            });
        } catch (final IOException e) {
            throw new JsonMessageMapperException("Problem with mapping message for key '" + MESSAGE_KEY + "'", e);
        }
    }

    @VisibleForTesting
    static String formatAdditionalInfo(final String error) {
        return error.replaceAll("\\\\n", "\n").replaceAll("\\\\'", "'").replace(
                "<class 'robot.errors.DataError'>, DataError", "");
    }

    public List<RobotDryRunLibraryImport> getImportedLibraries() {
        return importedLibraries;
    }

}
