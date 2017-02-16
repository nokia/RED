/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;

import com.google.common.io.Files;

/**
 * @author mmarzec
 */
public class RobotDryRunLibraryImportCollector {

    private final List<RobotDryRunLibraryImport> importedLibraries = new LinkedList<>();

    private final Set<String> standardLibrariesNames;

    private RobotDryRunLibraryImport currentLibraryImportWithFail;

    public RobotDryRunLibraryImportCollector(final Set<String> standardLibrariesNames) {
        this.standardLibrariesNames = standardLibrariesNames;
    }

    public void collectFromLibraryImportEvent(final String libraryName, final String importer, final String source,
            final List<String> args) {
        if (importer != null) {
            RobotDryRunLibraryImport dryRunLibraryImport = null;
            if (source != null) {
                if (currentLibraryImportWithFail != null
                        && libraryName.equals(currentLibraryImportWithFail.getName())) {
                    dryRunLibraryImport = currentLibraryImportWithFail;
                } else {
                    dryRunLibraryImport = new RobotDryRunLibraryImport(libraryName, source, importer, args);
                }
            } else {
                dryRunLibraryImport = new RobotDryRunLibraryImport(libraryName, importer, args);
            }
            final int index = importedLibraries.indexOf(dryRunLibraryImport);
            if (index < 0) {
                if (!standardLibrariesNames.contains(libraryName)) {
                    importedLibraries.add(dryRunLibraryImport);
                }
            } else {
                importedLibraries.get(index).addImporterPath(importer);
            }
        }
        resetCurrentLibraryImportWithFail();
    }

    public void collectFromFailMessageEvent(final String message) {
        if (message != null) {
            final String libraryName = extractLibName(message);
            if (!libraryName.isEmpty()) {
                final String failReason = extractFailReason(message);
                resetCurrentLibraryImportWithFail();
                final RobotDryRunLibraryImport dryRunLibraryImport = new RobotDryRunLibraryImport(libraryName);
                final int libIndex = importedLibraries.indexOf(dryRunLibraryImport);
                if (libIndex < 0) {
                    dryRunLibraryImport.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
                    dryRunLibraryImport.setAdditionalInfo(failReason);
                    importedLibraries.add(dryRunLibraryImport);
                } else {
                    importedLibraries.get(libIndex).setStatus(DryRunLibraryImportStatus.NOT_ADDED);
                    importedLibraries.get(libIndex).setAdditionalInfo(failReason);
                }
                currentLibraryImportWithFail = dryRunLibraryImport;
            }
        }
    }

    public void collectFromErrorMessageEvent(final String message) {
        if (message != null) {
            final String nameStartTxt = "': Test library '";
            final int nameStartIndex = message.lastIndexOf(nameStartTxt);
            final int endIndex = message.lastIndexOf("' does not exist");
            if (nameStartIndex > 0 && endIndex > nameStartIndex) {
                final String libName = message.substring(nameStartIndex + nameStartTxt.length(), endIndex);
                final String importer = extractImporter(message, nameStartIndex);
                final RobotDryRunLibraryImport dryRunLibraryImport = new RobotDryRunLibraryImport(libName);
                final int libIndex = importedLibraries.indexOf(dryRunLibraryImport);
                if (libIndex < 0) {
                    dryRunLibraryImport.addImporterPath(importer);
                    dryRunLibraryImport.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
                    dryRunLibraryImport.setAdditionalInfo(message);
                    importedLibraries.add(dryRunLibraryImport);
                } else {
                    importedLibraries.get(libIndex).addImporterPath(importer);
                    importedLibraries.get(libIndex).setStatus(DryRunLibraryImportStatus.NOT_ADDED);
                    importedLibraries.get(libIndex).setAdditionalInfo(message);
                }
            }
        }
    }

    private String extractImporter(final String message, final int nameStartIndex) {
        final String errorStartTxt = "Error in file '";
        final int errorStartIndex = message.indexOf(errorStartTxt);
        return errorStartIndex == -1 ? "" : message.substring(errorStartIndex + errorStartTxt.length(), nameStartIndex);
    }

    private String extractLibName(final String message) {
        final String libName = extractElement(message, "LIB_ERROR:");
        if (Files.isFile().apply(new File(libName))) {
            return Files.getNameWithoutExtension(libName);
        }
        return libName;
    }

    private String extractElement(final String message, final String key) {
        final int keyIndex = message.indexOf(key);
        if (keyIndex >= 0) {
            final int endIndex = message.indexOf(",", keyIndex);
            if (endIndex >= 0 && endIndex > keyIndex) {
                return message.substring(keyIndex + key.length(), endIndex).trim();
            }
        }
        return "";
    }

    private String extractFailReason(final String message) {
        if (message != null) {
            final String failReason = message.replaceAll("\\\\n", "\n").replaceAll("\\\\'", "'");
            final String startText = "VALUE_START";
            final String endText = "VALUE_END";
            final int beginIndex = failReason.indexOf(startText);
            final int endIndex = failReason.lastIndexOf(endText);
            if (beginIndex >= 0 && endIndex > 0) {
                return failReason.substring(beginIndex + startText.length() + 1, endIndex)
                        .replace("<class 'robot.errors.DataError'>, DataError(", "");
            }
            return failReason;
        }
        return "";
    }

    private void resetCurrentLibraryImportWithFail() {
        currentLibraryImportWithFail = null;
    }

    public List<RobotDryRunLibraryImport> getImportedLibraries() {
        return importedLibraries;
    }

}
