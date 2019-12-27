/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.rf.ide.core.project.ImportSearchPaths.MarkedUri;
import org.rf.ide.core.project.ImportSearchPaths.PathRelativityPoint;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;

import com.google.common.io.Files;

public class ImportSearchPathsTest {

    @TempDir
    static File tempDir;


    private static URI importerUri;

    @BeforeAll
    public static void beforeSuite() throws IOException {
        importerUri = new File(tempDir, "importer.ext").toURI();

        final byte[] content = "file content".getBytes();

        new File(tempDir, "folder").mkdir();
        Files.write(content, new File(tempDir, "folder/file1.ext"));

        new File(tempDir, "python").mkdir();
        Files.write(content, new File(tempDir, "python/file2.ext"));

        new File(tempDir, "user").mkdir();
        Files.write(content, new File(tempDir, "user/file3.ext"));
    }

    @Test
    public void pathIsFoundForAbsoluteImport_1() {
        final URI importedUri = new File(tempDir, "folder/file1.ext").toURI();
        final ResolvedImportPath importPath = new ResolvedImportPath(importedUri);

        final PathsProvider pathsProvider = createProvider(newArrayList(new File(tempDir, "python")),
                newArrayList(new File(tempDir, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri).isPresent();
        assertThat(absMarkedUri.get().getRelativity()).isEqualTo(PathRelativityPoint.FILE);
        assertThat(absMarkedUri.get().getPath()).isEqualTo(importedUri);

        assertThat(absUri).hasValue(importedUri);
    }

    @Test
    public void pathIsFoundForAbsoluteImport_2() {
        final URI importedUri = new File(tempDir, "python/file2.ext").toURI();
        final ResolvedImportPath importPath = new ResolvedImportPath(importedUri);

        final PathsProvider pathsProvider = createProvider(newArrayList(new File(tempDir, "python")),
                newArrayList(new File(tempDir, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri).isPresent();
        assertThat(absMarkedUri.get().getRelativity()).isEqualTo(PathRelativityPoint.FILE);
        assertThat(absMarkedUri.get().getPath()).isEqualTo(importedUri);

        assertThat(absUri).hasValue(importedUri);
    }

    @Test
    public void pathIsFoundForAbsoluteImport_3() {
        final URI importedUri = new File(tempDir, "user/file3.ext").toURI();
        final ResolvedImportPath importPath = new ResolvedImportPath(importedUri);

        final PathsProvider pathsProvider = createProvider(newArrayList(new File(tempDir, "python")),
                newArrayList(new File(tempDir, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri).isPresent();
        assertThat(absMarkedUri.get().getRelativity()).isEqualTo(PathRelativityPoint.FILE);
        assertThat(absMarkedUri.get().getPath()).isEqualTo(importedUri);

        assertThat(absUri).hasValue(importedUri);
    }

    @Test
    public void pathIsFoundForImportRelativeToImportingFile() throws URISyntaxException {
        final URI importedUri = new File(tempDir, "folder/file1.ext").toURI();
        final ResolvedImportPath importPath = ResolvedImportPath.from(ImportPath.from("folder/file1.ext")).get();

        final PathsProvider pathsProvider = createProvider(newArrayList(new File(tempDir, "python")),
                newArrayList(new File(tempDir, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri).isPresent();
        assertThat(absMarkedUri.get().getRelativity()).isEqualTo(PathRelativityPoint.FILE);
        assertThat(absMarkedUri.get().getPath()).isEqualTo(importedUri);

        assertThat(absUri).hasValue(importedUri);
    }

    @Test
    public void pathIsFoundForImportRelativeToPythonPath() throws URISyntaxException {
        final URI importedUri = new File(tempDir, "python/file2.ext").toURI();
        final ResolvedImportPath importPath = ResolvedImportPath.from(ImportPath.from("file2.ext")).get();

        final PathsProvider pathsProvider = createProvider(newArrayList(new File(tempDir, "python")),
                newArrayList(new File(tempDir, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri).isPresent();
        assertThat(absMarkedUri.get().getRelativity()).isEqualTo(PathRelativityPoint.PYTHON_MODULE_SEARCH_PATH);
        assertThat(absMarkedUri.get().getPath()).isEqualTo(importedUri);

        assertThat(absUri).hasValue(importedUri);
    }

    @Test
    public void pathIsFoundForImportRelativeToUserPath() throws URISyntaxException {
        final URI importedUri = new File(tempDir, "user/file3.ext").toURI();
        final ResolvedImportPath importPath = ResolvedImportPath.from(ImportPath.from("file3.ext")).get();

        final PathsProvider pathsProvider = createProvider(newArrayList(new File(tempDir, "python")),
                newArrayList(new File(tempDir, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri).isPresent();
        assertThat(absMarkedUri.get().getRelativity()).isEqualTo(PathRelativityPoint.USER_DEFINED_SEARCH_PATH);
        assertThat(absMarkedUri.get().getPath()).isEqualTo(importedUri);

        assertThat(absUri).hasValue(importedUri);
    }

    @Test
    public void nothingIsProvidedForNonExistingRelativeImport() throws URISyntaxException {
        final ResolvedImportPath importPath = ResolvedImportPath.from(ImportPath.from("file4.ext")).get();

        final PathsProvider pathsProvider = createProvider(newArrayList(new File(tempDir, "python")),
                newArrayList(new File(tempDir, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri).isNotPresent();

        assertThat(absUri).isNotPresent();
    }

    private static PathsProvider createProvider(final List<File> pythonPaths, final List<File> userPaths) {
        return new PathsProvider() {

            @Override
            public boolean targetExists(final URI uri) {
                return new File(uri).exists();
            }

            @Override
            public List<File> providePythonModulesSearchPaths() {
                return pythonPaths;
            }

            @Override
            public List<File> provideUserSearchPaths() {
                return userPaths;
            }
        };
    }

}
