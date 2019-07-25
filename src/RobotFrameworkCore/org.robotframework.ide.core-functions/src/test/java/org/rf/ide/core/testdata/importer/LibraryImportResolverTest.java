/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.libraries.LibraryConstructor;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.importer.LibraryImportResolver.ImportedLibrary;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

public class LibraryImportResolverTest {

    @Test
    public void noLibraryIsFoundForNameImport_whenThereAreNoLibs() {
        final ListMultimap<String, LibrarySpecification> libs = ArrayListMultimap.create();

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library"));

        assertThat(imported).isEmpty();
    }

    @Test
    public void noLibraryIsFoundForNameImport_whenThereAreNoMatchingLibs() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newStaticSpec("other_library", new ArrayList<>(), new ArrayList<>()),
                newStaticSpec("different_library", new ArrayList<>(), new ArrayList<>()));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library"));

        assertThat(imported).isEmpty();
    }

    @Test
    public void noLibraryIsFoundForNameImport_whenThereIsADynamicLibButArgumentsDoNotMatch_1() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library"));

        assertThat(imported).isEmpty();
    }

    @Test
    public void noLibraryIsFoundForNameImport_whenThereIsADynamicLibButArgumentsDoNotMatch_2() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library", "2"));

        assertThat(imported).isEmpty();
    }

    @Test
    public void noLibraryIsFoundForNameImport_whenThereIsADynamicLibButArgumentsDoNotMatch_3() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library", "b=1"));

        assertThat(imported).isEmpty();
    }

    @Test
    public void noLibraryIsFoundForNameImport_whenThereIsADynamicLibArgumentsDoMatchButNotAllAreProvided() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a", "b"), newArrayList("1", "2")));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library", "1"));

        assertThat(imported).isEmpty();
    }

    @Test
    public void libraryIsFoundForNameImport_whenThereIsADynamicLibAndArgumentsDoMatch_1() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library", "1"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification())
                .isEqualTo(newDynamicSpec("library", newArrayList("a"), newArrayList("1")));
    }

    @Test
    public void libraryIsFoundForNameImport_whenThereIsADynamicLibAndArgumentsDoMatch_2() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library", "a=1"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification())
                .isEqualTo(newDynamicSpec("library", newArrayList("a"), newArrayList("1")));
    }

    @Test
    public void libraryIsFoundForNameImport_whenThereIsADynamicLibAndArgumentsDoMatch_3() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a", "b"), newArrayList("1", "2")));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library", "b=2", "a=1"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification())
                .isEqualTo(newDynamicSpec("library", newArrayList("a", "b"), newArrayList("1", "2")));
    }

    @Test
    public void libraryIsFoundForNameImport_whenThereIsADynamicLibAndArgumentsDoMatch_4() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a"), newArrayList("1")),
                newDynamicSpec("library", newArrayList("b"), newArrayList("2")),
                newDynamicSpec("library", newArrayList("c"), newArrayList("3")));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library", "2"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification())
                .isEqualTo(newDynamicSpec("library", newArrayList("b"), newArrayList("2")));
    }

    @Test
    public void libraryIsFoundForNameImport_whenThereIsAStaticLib() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newStaticSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library", "b", "2"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification())
                .isEqualTo(newStaticSpec("library", newArrayList("a"), newArrayList("1")));
    }

    @Test
    public void libraryIsFoundForNameImport_whenThereIsAStaticLibWithAlias() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newStaticSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver
                .getImportedLibrary(newAliasedLibImport("library", "alias", "2"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).contains("alias");
        assertThat(imported.get().getSpecification())
                .isEqualTo(newStaticSpec("library", newArrayList("a"), newArrayList("1")));
    }

    @Test
    public void noLibraryIsFoundForPathImport_whenThereAreNoLibs() {
        final ListMultimap<String, LibrarySpecification> libs = ArrayListMultimap.create();

        final LibraryImportResolver resolver = createByPathResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library.py"));

        assertThat(imported).isEmpty();
    }

    @Test
    public void noLibraryIsFoundForPathImport_whenThereIsADynamicLibButArgumentsDoNotMatch_1() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByPathResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library.py"));

        assertThat(imported).isEmpty();
    }

    @Test
    public void noLibraryIsFoundForPathImport_whenThereIsADynamicLibButArgumentsDoNotMatch_2() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByPathResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library.py", "2"));

        assertThat(imported).isEmpty();
    }

    @Test
    public void noLibraryIsFoundForPathImport_whenThereIsADynamicLibButArgumentsDoNotMatch_3() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByPathResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library.py", "b=1"));

        assertThat(imported).isEmpty();
    }

    @Test
    public void noLibraryIsFoundForPathImport_whenThereIsADynamicLibArgumentsDoMatchButNotAllAreProvided() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a", "b"), newArrayList("1", "2")));

        final LibraryImportResolver resolver = createByPathResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library.py", "1"));

        assertThat(imported).isEmpty();
    }

    @Test
    public void libraryIsFoundForPathImport_whenThereIsADynamicLibAndArgumentsDoMatch_1() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByPathResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library.py", "1"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification())
                .isEqualTo(newDynamicSpec("library", newArrayList("a"), newArrayList("1")));
    }

    @Test
    public void libraryIsFoundForPathImport_whenThereIsADynamicLibAndArgumentsDoMatch_2() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByPathResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library.py", "a=1"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification())
                .isEqualTo(newDynamicSpec("library", newArrayList("a"), newArrayList("1")));
    }

    @Test
    public void libraryIsFoundForPathImport_whenThereIsADynamicLibAndArgumentsDoMatch_3() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a", "b"), newArrayList("1", "2")));

        final LibraryImportResolver resolver = createByPathResolver(libs);
        final Optional<ImportedLibrary> imported = resolver
                .getImportedLibrary(newLibImport("library.py", "b=2", "a=1"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification())
                .isEqualTo(newDynamicSpec("library", newArrayList("a", "b"), newArrayList("1", "2")));
    }

    @Test
    public void libraryIsFoundForPathImport_whenThereIsADynamicLibAndArgumentsDoMatch_4() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newDynamicSpec("library", newArrayList("a"), newArrayList("1")),
                newDynamicSpec("library", newArrayList("b"), newArrayList("2")),
                newDynamicSpec("library", newArrayList("c"), newArrayList("3")));

        final LibraryImportResolver resolver = createByPathResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library.py", "2"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification())
                .isEqualTo(newDynamicSpec("library", newArrayList("b"), newArrayList("2")));
    }

    @Test
    public void libraryIsFoundForPathNameImport_whenThereIsAStaticLib() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newStaticSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByPathResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("library.py", "b", "2"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification())
                .isEqualTo(newStaticSpec("library", newArrayList("a"), newArrayList("1")));
    }

    @Test
    public void libraryIsFoundForPathImport_whenThereIsAStaticLibWithAlias() {
        final ListMultimap<String, LibrarySpecification> libs = libs(
                newStaticSpec("library", newArrayList("a"), newArrayList("1")));

        final LibraryImportResolver resolver = createByPathResolver(libs);
        final Optional<ImportedLibrary> imported = resolver
                .getImportedLibrary(newAliasedLibImport("library.py", "alias", "2"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).contains("alias");
        assertThat(imported.get().getSpecification())
                .isEqualTo(newStaticSpec("library", newArrayList("a"), newArrayList("1")));
    }

    @Test
    public void remoteLibraryIsFoundForNameImport_whenSpecForDefaultUriExistAndImportHaveNoArguments()
            throws Exception {
        final ListMultimap<String, LibrarySpecification> libs = libs(newRemoteSpec(RemoteLocation.DEFAULT_ADDRESS));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("Remote"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification()).isEqualTo(newRemoteSpec(RemoteLocation.DEFAULT_ADDRESS));
    }

    @Test
    public void remoteLibraryIsFoundForNameImport_whenSpecForDefaultUriExistAndImportHaveNoArgumentsButAlias()
            throws Exception {
        final ListMultimap<String, LibrarySpecification> libs = libs(newRemoteSpec(RemoteLocation.DEFAULT_ADDRESS));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newAliasedLibImport("Remote", "alias"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).contains("alias");
        assertThat(imported.get().getSpecification()).isEqualTo(newRemoteSpec(RemoteLocation.DEFAULT_ADDRESS));
    }

    @Test
    public void remoteLibraryIsFoundForNameImport_whenSpecForDefaultUriExistAndImportHaveTimeoutArgument()
            throws Exception {
        final ListMultimap<String, LibrarySpecification> libs = libs(newRemoteSpec(RemoteLocation.DEFAULT_ADDRESS));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("Remote", "timeout=45"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification()).isEqualTo(newRemoteSpec(RemoteLocation.DEFAULT_ADDRESS));
    }

    @Test
    public void remoteLibraryIsFoundWhenImportedDifferently() throws Exception {
        assertRemoteLibSpecIsFoundFor(newLibImport("Remote", "http://127.0.0.1:9000/"));
        assertRemoteLibSpecIsFoundFor(newLibImport("Remote", "127.0.0.1:9000/"));
        assertRemoteLibSpecIsFoundFor(newLibImport("Remote", "uri=http://127.0.0.1:9000/"));
        assertRemoteLibSpecIsFoundFor(newLibImport("Remote", "uri=127.0.0.1:9000/"));
        assertRemoteLibSpecIsFoundFor(newLibImport("Remote", "uri=127.0.0.1:9000/"));
        assertRemoteLibSpecIsFoundFor(newLibImport("Remote", "http://127.0.0.1:9000/", "30"));
        assertRemoteLibSpecIsFoundFor(newLibImport("Remote", "uri=http://127.0.0.1:9000/", "timeout=30"));
        assertRemoteLibSpecIsFoundFor(newLibImport("Remote", "uri=127.0.0.1:9000/", "timeout=30"));
        assertRemoteLibSpecIsFoundFor(newLibImport("Remote", "timeout=30", "uri=http://127.0.0.1:9000/"));
        assertRemoteLibSpecIsFoundFor(newLibImport("Remote", "timeout=30", "uri=127.0.0.1:9000/"));
        assertRemoteLibSpecIsFoundFor(newLibImport("Remote", "timeout=30", "timeout=45", "uri=127.0.0.1:9000/"));
        assertRemoteLibSpecIsFoundFor(newLibImport("Remote", "uri=127.0.0.2.8000/", "uri=127.0.0.1:9000/"));
    }

    @Test
    public void remoteLibraryIsNotFoundWhenImportedWrongly() throws Exception {
        assertRemoteLibSpecIsNotFoundFor(newLibImport("Remote", "http://127.0.0.1:9000/", "30", "60"));
        assertRemoteLibSpecIsNotFoundFor(newLibImport("Remote", "uri=127.0.0.1:9000/", "uri=127.0.0.2.8000/"));
        assertRemoteLibSpecIsNotFoundFor(newLibImport("Remote", "://127.0.0.1:9000/"));
        assertRemoteLibSpecIsNotFoundFor(newLibImport("Remote", "http://"));
        assertRemoteLibSpecIsNotFoundFor(newLibImport("Remote", "uri=://127.0.0.1:9000/"));
        assertRemoteLibSpecIsNotFoundFor(newLibImport("Remote", "uri=http://"));
        assertRemoteLibSpecIsNotFoundFor(newLibImport("Remote", "urri=127.0.0.1:9000/"));
        assertRemoteLibSpecIsNotFoundFor(newLibImport("Remote", "uri127.0.0.1:9000/"));
        assertRemoteLibSpecIsNotFoundFor(newLibImport("Remote", "^://127.0.0.1:9000/"));
        assertRemoteLibSpecIsNotFoundFor(newLibImport("Remote", "uri=127.0.0.1:9000/%"));
    }

    @Test
    public void remoteLibraryIsFoundForNameImport_whenUsingVariableMappings_1() throws Exception {
        final ListMultimap<String, LibrarySpecification> libs = libs(newRemoteSpec("http://127.0.0.1:9000/"));

        final Map<String, String> mappings = ImmutableMap.of("${remotevar}", "127.0.0.1:9000");
        final LibraryImportResolver resolver = createByNameResolver(libs, mappings);
        final Optional<ImportedLibrary> imported = resolver
                .getImportedLibrary(newLibImport("Remote", "http://${remotevar}/"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification()).isEqualTo(newRemoteSpec("http://127.0.0.1:9000/"));
    }

    @Test
    public void remoteLibraryIsFoundForNameImport_whenUsingVariableMappings_2() throws Exception {
        final ListMultimap<String, LibrarySpecification> libs = libs(newRemoteSpec("http://127.0.0.1:9000/"));

        final Map<String, String> mappings = ImmutableMap.of("${remotevar}", "http://127.0.0.1:9000/");
        final LibraryImportResolver resolver = createByNameResolver(libs, mappings);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(newLibImport("Remote", "${remotevar}"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification()).isEqualTo(newRemoteSpec("http://127.0.0.1:9000/"));
    }

    @Test
    public void remoteLibraryIsFoundForNameImport_whenUsingVariableMappings_3() throws Exception {
        final ListMultimap<String, LibrarySpecification> libs = libs(newRemoteSpec("http://127.0.0.1:9000/"));

        final Map<String, String> mappings = ImmutableMap.of("${remotevar}", "127.0.0.1:9000");
        final LibraryImportResolver resolver = createByNameResolver(libs, mappings);
        final Optional<ImportedLibrary> imported = resolver
                .getImportedLibrary(newLibImport("Remote", "uri=http://${remotevar}/"));

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification()).isEqualTo(newRemoteSpec("http://127.0.0.1:9000/"));
    }

    private static void assertRemoteLibSpecIsFoundFor(final LibraryImport libImport) {
        final ListMultimap<String, LibrarySpecification> libs = libs(newRemoteSpec("http://127.0.0.1:9000/"));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(libImport);

        assertThat(imported).isNotEmpty();
        assertThat(imported.get().getAlias()).isEmpty();
        assertThat(imported.get().getSpecification()).isEqualTo(newRemoteSpec("http://127.0.0.1:9000/"));
    }

    private static void assertRemoteLibSpecIsNotFoundFor(final LibraryImport libImport) {
        final ListMultimap<String, LibrarySpecification> libs = libs(newRemoteSpec("http://127.0.0.1:9000/"));

        final LibraryImportResolver resolver = createByNameResolver(libs);
        final Optional<ImportedLibrary> imported = resolver.getImportedLibrary(libImport);

        assertThat(imported).isEmpty();
    }

    private static ListMultimap<String, LibrarySpecification> libs(final LibrarySpecification... specs) {
        return Multimaps.index(Arrays.asList(specs), LibrarySpecification::getName);
    }

    private static LibrarySpecification newStaticSpec(final String name, final List<String> consArguments,
            final List<String> usedArguments) {
        final LibrarySpecification spec = LibrarySpecification.create(name);
        spec.setConstructor(LibraryConstructor.create("doc", consArguments));
        spec.setDescriptor(new LibraryDescriptor(name, LibraryType.PYTHON, "/" + name + ".py", usedArguments, false));
        return spec;
    }

    private static LibrarySpecification newDynamicSpec(final String name, final List<String> consArguments,
            final List<String> usedArguments) {
        final LibrarySpecification spec = LibrarySpecification.create(name);
        spec.setConstructor(LibraryConstructor.create("doc", consArguments));
        spec.setDescriptor(new LibraryDescriptor(name, LibraryType.PYTHON, "/" + name + ".py", usedArguments, true));
        return spec;
    }

    private static LibrarySpecification newRemoteSpec(final String usedUri) {
        final LibrarySpecification spec = LibrarySpecification.create("Remote");
        spec.setConstructor(
                LibraryConstructor.create("doc", newArrayList("uri=" + RemoteLocation.DEFAULT_ADDRESS, "timeout=30")));
        spec.setDescriptor(new LibraryDescriptor("Remote", LibraryType.PYTHON, null, newArrayList(usedUri), true));
        return spec;
    }

    private static LibraryImport newLibImport(final String name, final String... arguments) {
        return newAliasedLibImport(name, null, arguments);
    }

    private static LibraryImport newAliasedLibImport(final String pathOrName, final String alias,
            final String... arguments) {
        final LibraryImport libImport = new LibraryImport(RobotToken.create("Library"));
        libImport.setPathOrName(pathOrName);
        if (alias != null) {
            final LibraryAlias libAlias = new LibraryAlias(
                    RobotToken.create("WITH NAME", RobotTokenType.SETTING_LIBRARY_ALIAS));
            libAlias.setLibraryAlias(RobotToken.create(alias, RobotTokenType.SETTING_LIBRARY_ALIAS_VALUE));
            libImport.setAlias(libAlias);
        }
        for (final String arg : arguments) {
            libImport.addArgument(arg);
        }
        return libImport;
    }

    private static LibraryImportResolver createByNameResolver(
            final ListMultimap<String, LibrarySpecification> indexedLibs) {
        return createByNameResolver(indexedLibs, new HashMap<>());
    }

    private static LibraryImportResolver createByNameResolver(
            final ListMultimap<String, LibrarySpecification> indexedLibs, final Map<String, String> varMappings) {
        return new LibraryImportResolver(new RobotVersion(3, 1), varMappings, (s, p) -> false, indexedLibs);
    }

    private static LibraryImportResolver createByPathResolver(
            final ListMultimap<String, LibrarySpecification> indexedLibs) {
        return new LibraryImportResolver(new RobotVersion(3, 1), new HashMap<>(), (s, p) -> true, indexedLibs);
    }
}
