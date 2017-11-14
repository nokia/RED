/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rf.ide.core.project.ImportSearchPaths.MarkedUri;
import org.rf.ide.core.project.ImportSearchPaths.PathRelativityPoint;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ImportSearchPathsTest {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    private static File root;

    private static URI importerUri;

    @BeforeClass
    public static void beforeSuite() throws IOException {
        root = folder.getRoot();
        importerUri = folder.newFile("importer.ext").toURI();

        folder.newFolder("folder");
        Files.asCharSink(get(root, "folder", "file1.ext"), Charsets.UTF_8).write("file content");

        folder.newFolder("python");
        Files.asCharSink(get(root, "python", "file2.ext"), Charsets.UTF_8).write("file content");

        folder.newFolder("user");
        Files.asCharSink(get(root, "user", "file3.ext"), Charsets.UTF_8).write("file content");
    }

    @Test
    public void pathIsFoundForAbsoluteImport_1() {
        final URI importedUri = get(root, "folder", "file1.ext").toURI();
        final ResolvedImportPath importPath = new ResolvedImportPath(importedUri);

        final PathsProvider pathsProvider = createProvider(
                newArrayList(get(root, "python")),
                newArrayList(get(root, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);
        final URI uri = pathsSupport.getAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri.isPresent()).isTrue();
        assertThat(absMarkedUri.get().getRelativity()).isEqualTo(PathRelativityPoint.FILE);
        assertThat(absMarkedUri.get().getPath()).isEqualTo(importedUri);

        assertThat(absUri.isPresent()).isTrue();
        assertThat(absUri.get()).isEqualTo(importedUri);

        assertThat(uri).isEqualTo(importedUri);
    }

    @Test
    public void pathIsFoundForAbsoluteImport_2() {
        final URI importedUri = get(root, "python", "file2.ext").toURI();
        final ResolvedImportPath importPath = new ResolvedImportPath(importedUri);

        final PathsProvider pathsProvider = createProvider(newArrayList(get(root, "python")),
                newArrayList(get(root, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);
        final URI uri = pathsSupport.getAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri.isPresent()).isTrue();
        assertThat(absMarkedUri.get().getRelativity()).isEqualTo(PathRelativityPoint.FILE);
        assertThat(absMarkedUri.get().getPath()).isEqualTo(importedUri);

        assertThat(absUri.isPresent()).isTrue();
        assertThat(absUri.get()).isEqualTo(importedUri);

        assertThat(uri).isEqualTo(importedUri);
    }

    @Test
    public void pathIsFoundForAbsoluteImport_3() {
        final URI importedUri = get(root, "user", "file3.ext").toURI();
        final ResolvedImportPath importPath = new ResolvedImportPath(importedUri);

        final PathsProvider pathsProvider = createProvider(newArrayList(get(root, "python")),
                newArrayList(get(root, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);
        final URI uri = pathsSupport.getAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri.isPresent()).isTrue();
        assertThat(absMarkedUri.get().getRelativity()).isEqualTo(PathRelativityPoint.FILE);
        assertThat(absMarkedUri.get().getPath()).isEqualTo(importedUri);

        assertThat(absUri.isPresent()).isTrue();
        assertThat(absUri.get()).isEqualTo(importedUri);

        assertThat(uri).isEqualTo(importedUri);
    }

    @Test
    public void pathIsFoundForImportRelativeToImportingFile() {
        final URI importedUri = get(root, "folder", "file1.ext").toURI();
        final ResolvedImportPath importPath = ResolvedImportPath.from(ImportPath.from("folder/file1.ext")).get();

        final PathsProvider pathsProvider = createProvider(newArrayList(get(root, "python")),
                newArrayList(get(root, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);
        final URI uri = pathsSupport.getAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri.isPresent()).isTrue();
        assertThat(absMarkedUri.get().getRelativity()).isEqualTo(PathRelativityPoint.FILE);
        assertThat(absMarkedUri.get().getPath()).isEqualTo(importedUri);

        assertThat(absUri.isPresent()).isTrue();
        assertThat(absUri.get()).isEqualTo(importedUri);

        assertThat(uri).isEqualTo(importedUri);
    }

    @Test
    public void pathIsFoundForImportRelativeToPythonPath() {
        final URI importedUri = get(root, "python", "file2.ext").toURI();
        final ResolvedImportPath importPath = ResolvedImportPath.from(ImportPath.from("file2.ext")).get();

        final PathsProvider pathsProvider = createProvider(newArrayList(get(root, "python")),
                newArrayList(get(root, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);
        final URI uri = pathsSupport.getAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri.isPresent()).isTrue();
        assertThat(absMarkedUri.get().getRelativity()).isEqualTo(PathRelativityPoint.PYTHON_MODULE_SEARCH_PATH);
        assertThat(absMarkedUri.get().getPath()).isEqualTo(importedUri);

        assertThat(absUri.isPresent()).isTrue();
        assertThat(absUri.get()).isEqualTo(importedUri);

        assertThat(uri).isEqualTo(importedUri);
    }

    @Test
    public void pathIsFoundForImportRelativeToUserPath() {
        final URI importedUri = get(root, "user", "file3.ext").toURI();
        final ResolvedImportPath importPath = ResolvedImportPath.from(ImportPath.from("file3.ext")).get();

        final PathsProvider pathsProvider = createProvider(newArrayList(get(root, "python")),
                newArrayList(get(root, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);
        final URI uri = pathsSupport.getAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri.isPresent()).isTrue();
        assertThat(absMarkedUri.get().getRelativity()).isEqualTo(PathRelativityPoint.USER_DEFINED_SEARCH_PATH);
        assertThat(absMarkedUri.get().getPath()).isEqualTo(importedUri);

        assertThat(absUri.isPresent()).isTrue();
        assertThat(absUri.get()).isEqualTo(importedUri);

        assertThat(uri).isEqualTo(importedUri);
    }

    @Test
    public void nothingIsProvidedForNonExistingRelativeImport() {
        final ResolvedImportPath importPath = ResolvedImportPath.from(ImportPath.from("file4.ext")).get();

        final PathsProvider pathsProvider = createProvider(newArrayList(get(root, "python")),
                newArrayList(get(root, "user")));
        final ImportSearchPaths pathsSupport = new ImportSearchPaths(pathsProvider);

        final Optional<MarkedUri> absMarkedUri = pathsSupport.findAbsoluteMarkedUri(importerUri, importPath);
        final Optional<URI> absUri = pathsSupport.findAbsoluteUri(importerUri, importPath);

        assertThat(absMarkedUri.isPresent()).isFalse();
        assertThat(absUri.isPresent()).isFalse();

        try {
            pathsSupport.getAbsoluteUri(importerUri, importPath);
            fail();
        } catch (final NoSuchElementException e) {
            // expected
        }
    }

    private static File get(final File root, final String... segments) {
        if (segments == null || segments.length == 0) {
            return root;
        }
        return get(new File(root, segments[0]), Arrays.copyOfRange(segments, 1, segments.length));
    }

    private static PathsProvider createProvider(final List<File> pythonPaths, final List<File> userPaths) {
        return new PathsProvider() {

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
