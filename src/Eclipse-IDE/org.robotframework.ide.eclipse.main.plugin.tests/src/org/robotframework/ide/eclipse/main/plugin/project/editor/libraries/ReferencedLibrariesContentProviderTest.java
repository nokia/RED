/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlArgumentsVariant.RedXmlRemoteArgumentsVariant;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlLibrary.RedXmlRemoteLib;
import org.robotframework.red.viewers.ElementAddingToken;

public class ReferencedLibrariesContentProviderTest {

    @Test
    public void whenContentProviderIsAskedForElementsOfEmptyConfig_itReturnsOnlyRemoteElementWithAddingToken() {
        final RobotProjectConfig cfg = new RobotProjectConfig();

        final ReferencedLibrariesContentProvider provider = new ReferencedLibrariesContentProvider();

        final Object[] elements = provider.getElements(cfg);

        assertThat(elements).hasSize(2);
        assertThat(elements[0]).isEqualTo(new RedXmlRemoteLib());
        assertThat(elements[1]).isInstanceOf(ElementAddingToken.class);

        assertThat(provider.hasChildren(elements[0])).isTrue();
        assertThat(provider.getChildren(elements[0])).isEmpty();
        assertThat(provider.hasChildren(elements[1])).isFalse();
        assertThat(provider.getChildren(elements[1])).isEmpty();
    }

    @Test
    public void whenContentProviderIsAskedForElementsOfConfigWithRemoteLocations_itReturnsThemAsChildren() {
        final RobotProjectConfig cfg = new RobotProjectConfig();
        cfg.addRemoteLocation(RemoteLocation.create("http://127.0.0.1:8271/"));
        cfg.addRemoteLocation(RemoteLocation.create("http://127.0.0.2:8272/"));

        final ReferencedLibrariesContentProvider provider = new ReferencedLibrariesContentProvider();

        final Object[] elements = provider.getElements(cfg);

        assertThat(elements).hasSize(2);
        assertThat(elements[0]).isEqualTo(new RedXmlRemoteLib());
        assertThat(elements[1]).isInstanceOf(ElementAddingToken.class);

        assertThat(provider.hasChildren(elements[0])).isTrue();
        assertThat(provider.getChildren(elements[0]))
                .extracting(r -> ((RedXmlRemoteArgumentsVariant) r).getRemoteLocation())
                .containsExactly(RemoteLocation.create("http://127.0.0.1:8271/"),
                        RemoteLocation.create("http://127.0.0.2:8272/"));
        assertThat(provider.hasChildren(elements[1])).isFalse();
        assertThat(provider.getChildren(elements[1])).isEmpty();

    }

    @Test
    public void whenContentProviderIsAskedForElementsOfConfigWithLibraries_itReturnsThemAsChildren() {
        final RobotProjectConfig cfg = new RobotProjectConfig();
        final ReferencedLibrary lib1 = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "path1");
        lib1.addArgumentsVariant(ReferencedLibraryArgumentsVariant.create("1", "2", "3"));
        lib1.addArgumentsVariant(ReferencedLibraryArgumentsVariant.create("x", "y"));
        cfg.addReferencedLibrary(lib1);
        cfg.addReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "OtherPyLib", "path2"));
        cfg.addReferencedLibrary(ReferencedLibrary.create(LibraryType.JAVA, "JavaLib", "path3"));
        cfg.addReferencedLibrary(ReferencedLibrary.create(LibraryType.VIRTUAL, "XmlLib", "path4"));

        final ReferencedLibrariesContentProvider provider = new ReferencedLibrariesContentProvider();

        final Object[] elements = provider.getElements(cfg);

        assertThat(elements).hasSize(6);
        assertThat(elements[0]).isEqualTo(new RedXmlRemoteLib());
        assertThat(((RedXmlLibrary) elements[1]).getLibrary())
                .isEqualTo(lib1);
        assertThat(((RedXmlLibrary) elements[2]).getLibrary())
                .isEqualTo(ReferencedLibrary.create(LibraryType.PYTHON, "OtherPyLib", "path2"));
        assertThat(((RedXmlLibrary) elements[3]).getLibrary())
                .isEqualTo(ReferencedLibrary.create(LibraryType.JAVA, "JavaLib", "path3"));
        assertThat(((RedXmlLibrary) elements[4]).getLibrary())
                .isEqualTo(ReferencedLibrary.create(LibraryType.VIRTUAL, "XmlLib", "path4"));
        assertThat(elements[5]).isInstanceOf(ElementAddingToken.class);

        assertThat(provider.hasChildren(elements[0])).isTrue();
        assertThat(provider.getChildren(elements[0])).isEmpty();
        assertThat(provider.hasChildren(elements[1])).isTrue();
        assertThat(provider.getChildren(elements[1]))
                .extracting(r -> ((RedXmlArgumentsVariant) r).getVariant())
                .containsExactly(ReferencedLibraryArgumentsVariant.create("1", "2", "3"),
                        ReferencedLibraryArgumentsVariant.create("x", "y"));
        assertThat(provider.hasChildren(elements[2])).isTrue();
        assertThat(provider.getChildren(elements[2])).isEmpty();
        assertThat(provider.hasChildren(elements[3])).isTrue();
        assertThat(provider.getChildren(elements[3])).isEmpty();
        assertThat(provider.hasChildren(elements[4])).isTrue();
        assertThat(provider.getChildren(elements[4])).isEmpty();
        assertThat(provider.hasChildren(elements[5])).isFalse();
        assertThat(provider.getChildren(elements[5])).isEmpty();
    }

    @Test
    public void whenContentProviderIsAskedForElementsOfNotAConfig_exceptionIsThrown() {
        final ReferencedLibrariesContentProvider provider = new ReferencedLibrariesContentProvider();

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> provider.getElements(new Object[] { "abc", "def", "ghi" }));
    }
}
