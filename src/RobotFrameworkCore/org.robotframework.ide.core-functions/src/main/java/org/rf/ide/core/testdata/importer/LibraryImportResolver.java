/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.ArgumentsDescriptor.Argument;
import org.rf.ide.core.libraries.LibraryConstructor;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder.RobotTokenAsArgExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder.StringAsArgExtractor;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.collect.ListMultimap;

public class LibraryImportResolver {

    private final RobotVersion version;

    private final Map<String, String> variablesMapping;

    private final BiFunction<LibrarySpecification, String, Boolean> isByPathImported;

    private final ListMultimap<String, LibrarySpecification> libsIndexedByNames;


    public LibraryImportResolver(final RobotVersion version, final Map<String, String> variablesMapping,
            final BiFunction<LibrarySpecification, String, Boolean> isByPathImported,
            final ListMultimap<String, LibrarySpecification> libsIndexedByNames) {
        this.version = version;
        this.variablesMapping = variablesMapping;
        this.isByPathImported = isByPathImported;
        this.libsIndexedByNames = libsIndexedByNames;
    }

    public Optional<ImportedLibrary> getImportedLibrary(final LibraryImport libImport) {
        final Optional<String> libNameOrPath = libImport.getLibraryNameToken()
                .map(RobotToken::getText)
                .map(RobotExpressions::unescapeSpaces);
        if (!libNameOrPath.isPresent()) {
            return Optional.empty();
        }

        final String nameOrPath = libNameOrPath.get();
        final List<RobotToken> arguments = libImport.getLibraryArgumentTokens();
        final Optional<String> alias = libImport.getLibraryAliasToken().map(RobotToken::getText);

        return getImportedLibrary(nameOrPath, arguments, alias);
    }

    private Optional<ImportedLibrary> getImportedLibrary(final String nameOrPath, final List<RobotToken> arguments,
            final Optional<String> alias) {
        return getImportedLibrary(nameOrPath, arguments).map(spec -> new ImportedLibrary(spec, alias));
    }

    private Optional<LibrarySpecification> getImportedLibrary(final String nameOrPath,
            final List<RobotToken> arguments) {
        return libsIndexedByNames.containsKey(nameOrPath)
                ? findSpecForName(nameOrPath, arguments)
                : findSpecForPath(nameOrPath, arguments);
    }

    private Optional<LibrarySpecification> findSpecForName(final String name, final List<RobotToken> arguments) {
        final List<LibrarySpecification> libraries = libsIndexedByNames.get(name);
        return libraries.stream().filter(spec -> hasMatchingArguments(spec, arguments)).findFirst();
    }

    private boolean hasMatchingArguments(final LibrarySpecification specification, final List<RobotToken> arguments) {
        final LibraryDescriptor libDescriptor = specification.getDescriptor();
        if (!libDescriptor.isDynamic()) {
            // static library will not have constructor arguments compared since the specification
            // will be the same regardless the arguments given
            return true;
        }

        final ArgumentsDescriptor argsDescriptor = Optional.ofNullable(specification.getConstructor())
                .map(LibraryConstructor::createArgumentsDescriptor)
                .orElse(ArgumentsDescriptor.createDescriptor());

        if (CallArgumentsBinder.canBind(version, argsDescriptor)) {
            final CallArgumentsBinder<String> libspecBinder = new CallArgumentsBinder<>(new StringAsArgExtractor(),
                    argsDescriptor);
            libspecBinder.bind(libDescriptor.getArguments());

            final CallArgumentsBinder<RobotToken> importBinder = new CallArgumentsBinder<>(
                    new RobotTokenAsArgExtractor(), argsDescriptor);
            importBinder.bind(arguments);

            if (!libspecBinder.hasBindings() || !importBinder.hasBindings()) {
                return false;
            }

            if (libDescriptor.isStandardRemoteLibrary()) {
                final Argument uriArg = argsDescriptor.get(0);
                final String specUriArg = extractUriValue(libspecBinder, uriArg);
                final String importUriArg = extractUriValue(importBinder, uriArg);

                return RemoteLocation.areEqual(specUriArg, importUriArg);

            } else {
                for (final Argument arg : argsDescriptor) {
                    final List<String> specArgs = extractValues(libspecBinder, arg);
                    final List<String> importArgs = extractValues(importBinder, arg);

                    if (!specArgs.equals(importArgs)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private <T> String extractUriValue(final CallArgumentsBinder<T> binder, final Argument arg) {
        return binder.getValuesBindedTo(arg)
                .stream()
                .map(val -> RobotExpressions.resolve(variablesMapping, val))
                .findFirst()
                .orElse(RemoteLocation.DEFAULT_ADDRESS);
    }

    private <T> List<String> extractValues(final CallArgumentsBinder<T> binder, final Argument arg) {
        return binder.getValuesBindedTo(arg)
                .stream()
                .map(val -> RobotExpressions.resolve(variablesMapping, val))
                .collect(toList());
    }

    private Optional<LibrarySpecification> findSpecForPath(final String path, final List<RobotToken> arguments) {
        return libsIndexedByNames.values()
                .stream()
                .filter(spec -> spec != null)
                .filter(spec -> spec.getDescriptor().isReferencedLibrary())
                .filter(spec -> isByPathImported.apply(spec, path))
                .filter(spec -> hasMatchingArguments(spec, arguments))
                .findFirst();
    }

    public static class ImportedLibrary {

        private final LibrarySpecification spec;

        private final Optional<String> alias;

        private ImportedLibrary(final LibrarySpecification spec, final Optional<String> alias) {
            this.spec = spec;
            this.alias = alias;
        }

        public LibrarySpecification getSpecification() {
            return spec;
        }

        public Optional<String> getAlias() {
            return alias;
        }
    }
}
