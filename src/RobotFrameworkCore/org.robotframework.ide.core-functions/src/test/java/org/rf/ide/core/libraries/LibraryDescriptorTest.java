/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.libraries;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;

public class LibraryDescriptorTest {

    @Test
    public void testStandardLibraryDescriptorProperties() {
        final LibraryDescriptor descriptor = LibraryDescriptor.ofStandardLibrary("StdLib");

        assertThat(descriptor.getName()).isEqualTo("StdLib");
        assertThat(descriptor.getArguments()).isEmpty();
        assertThat(descriptor.getLibraryType()).isEqualTo(LibraryType.PYTHON);
        assertThat(descriptor.getPath()).isNull();
        assertThat(descriptor.isDynamic()).isFalse();

        assertThat(descriptor.isStandardLibrary()).isTrue();
        assertThat(descriptor.isStandardRemoteLibrary()).isFalse();
        assertThat(descriptor.isReferencedLibrary()).isFalse();
        assertThat(descriptor.getKeywordsScope()).isEqualTo(KeywordScope.STD_LIBRARY);

        assertThat(descriptor.equals(LibraryDescriptor.ofStandardLibrary("StdLib"))).isTrue();
        assertThat(descriptor.equals(LibraryDescriptor.ofStandardLibrary("OtherStdLib"))).isFalse();

        assertThat(descriptor.hashCode()).isEqualTo(LibraryDescriptor.ofStandardLibrary("StdLib").hashCode());
        assertThat(descriptor.hashCode()).isNotEqualTo(LibraryDescriptor.ofStandardLibrary("OtherStdLib").hashCode());
    }

    @Test
    public void testRemoteStandardLibraryDescriptorProperties() {
        final LibraryDescriptor descriptor = LibraryDescriptor
                .ofStandardRemoteLibrary(RemoteLocation.create("http://uri"));

        assertThat(descriptor.getName()).isEqualTo("Remote");
        assertThat(descriptor.getArguments()).containsExactly("http://uri");
        assertThat(descriptor.getLibraryType()).isEqualTo(LibraryType.PYTHON);
        assertThat(descriptor.getPath()).isNull();
        assertThat(descriptor.isDynamic()).isTrue();

        assertThat(descriptor.isStandardLibrary()).isTrue();
        assertThat(descriptor.isStandardRemoteLibrary()).isTrue();
        assertThat(descriptor.isReferencedLibrary()).isFalse();
        assertThat(descriptor.getKeywordsScope()).isEqualTo(KeywordScope.STD_LIBRARY);

        assertThat(descriptor.equals(LibraryDescriptor.ofStandardRemoteLibrary(RemoteLocation.create("http://uri"))))
                .isTrue();
        assertThat(
                descriptor.equals(LibraryDescriptor.ofStandardRemoteLibrary(RemoteLocation.create("http://otheruri"))))
                        .isFalse();

        assertThat(descriptor.hashCode())
                .isEqualTo(LibraryDescriptor.ofStandardRemoteLibrary(RemoteLocation.create("http://uri")).hashCode());
        assertThat(descriptor.hashCode()).isNotEqualTo(
                LibraryDescriptor.ofStandardRemoteLibrary(RemoteLocation.create("http://otheruri")).hashCode());
    }

    @Test
    public void testReferencedLibraryDescriptorProperties_1() {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/to/library/lib.py");
        final ReferencedLibraryArgumentsVariant argsVariant = ReferencedLibraryArgumentsVariant.create();

        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(lib, argsVariant);

        assertThat(descriptor.getName()).isEqualTo("lib");
        assertThat(descriptor.getArguments()).isEmpty();
        assertThat(descriptor.getLibraryType()).isEqualTo(LibraryType.PYTHON);
        assertThat(descriptor.getPath()).isEqualTo("path/to/library/lib.py");
        assertThat(descriptor.getKeywordsScope()).isEqualTo(KeywordScope.REF_LIBRARY);
        assertThat(descriptor.isDynamic()).isFalse();

        assertThat(descriptor.isStandardLibrary()).isFalse();
        assertThat(descriptor.isStandardRemoteLibrary()).isFalse();
        assertThat(descriptor.isReferencedLibrary()).isTrue();

        assertThat(descriptor.equals(LibraryDescriptor.ofReferencedLibrary(
                lib, argsVariant))).isTrue();
        assertThat(descriptor.equals(LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.JAVA, "lib", "path/to/library/lib.py"), argsVariant))).isFalse();
        assertThat(descriptor.equals(LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "l", "path/to/library/lib.py"), argsVariant))).isFalse();
        assertThat(descriptor.equals(LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/other/lib.py"), argsVariant))).isFalse();

        assertThat(descriptor.hashCode()).isEqualTo(LibraryDescriptor.ofReferencedLibrary(
                lib, argsVariant).hashCode());
        assertThat(descriptor.hashCode()).isNotEqualTo(LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.JAVA, "lib", "path/to/library/lib.py"), argsVariant).hashCode());
        assertThat(descriptor.hashCode()).isNotEqualTo(LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "l", "path/to/library/lib.py"), argsVariant).hashCode());
        assertThat(descriptor.hashCode()).isNotEqualTo(
                LibraryDescriptor
                        .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/other/lib.py"),
                                argsVariant)
                        .hashCode());
    }

    @Test
    public void testReferencedLibraryDescriptorProperties_2() {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/to/library/lib.py");
        lib.setDynamic(true);
        final ReferencedLibraryArgumentsVariant argsVariant = ReferencedLibraryArgumentsVariant.create("1", "2", "3");

        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(lib, argsVariant);

        assertThat(descriptor.getName()).isEqualTo("lib");
        assertThat(descriptor.getArguments()).containsExactly("1", "2", "3");
        assertThat(descriptor.getLibraryType()).isEqualTo(LibraryType.PYTHON);
        assertThat(descriptor.getPath()).isEqualTo("path/to/library/lib.py");
        assertThat(descriptor.getKeywordsScope()).isEqualTo(KeywordScope.REF_LIBRARY);
        assertThat(descriptor.isDynamic()).isTrue();

        assertThat(descriptor.isStandardLibrary()).isFalse();
        assertThat(descriptor.isStandardRemoteLibrary()).isFalse();
        assertThat(descriptor.isReferencedLibrary()).isTrue();

        assertThat(descriptor.equals(LibraryDescriptor.ofReferencedLibrary(lib, argsVariant))).isTrue();
        assertThat(descriptor.equals(LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.JAVA, "lib", "path/to/library/lib.py"), argsVariant))).isFalse();
        assertThat(descriptor.equals(LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "l", "path/to/library/lib.py"), argsVariant))).isFalse();
        assertThat(descriptor.equals(LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/other/lib.py"), argsVariant))).isFalse();

        assertThat(descriptor.hashCode()).isEqualTo(LibraryDescriptor.ofReferencedLibrary(lib, argsVariant).hashCode());
        assertThat(descriptor.hashCode()).isNotEqualTo(LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.JAVA, "lib", "path/to/library/lib.py"), argsVariant).hashCode());
        assertThat(descriptor.hashCode()).isNotEqualTo(LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "l", "path/to/library/lib.py"), argsVariant).hashCode());
        assertThat(descriptor.hashCode()).isNotEqualTo(
                LibraryDescriptor
                        .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/other/lib.py"),
                                argsVariant)
                        .hashCode());
    }

    @Test
    public void pathIsProperlyProvided() {
        final ReferencedLibraryArgumentsVariant argsVariant = ReferencedLibraryArgumentsVariant.create();

        assertThat(LibraryDescriptor.ofStandardLibrary("StdLib").getPath()).isNull();
        assertThat(LibraryDescriptor.ofStandardRemoteLibrary(RemoteLocation.create("http://uri")).getPath()).isNull();
        assertThat(LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/to/library/lib.py"), argsVariant).getPath())
                        .isEqualTo("path/to/library/lib.py");
        assertThat(LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "lib.a.b", "path/to/library/lib/a.py"), argsVariant)
                .getPath()).isEqualTo("path/to/library/lib/a.py");
    }

    @Test
    public void filenameOfStandardLibraryIsJustItsName() {
        final LibraryDescriptor descriptor = LibraryDescriptor.ofStandardLibrary("StdLib");

        assertThat(descriptor.generateLibspecFileName()).isEqualTo("StdLib");
    }

    @Test
    public void filenameOfRemoteLibraryIsItsNameWithAbbreviatedHashedSuffix() {
        final LibraryDescriptor descriptor = LibraryDescriptor
                .ofStandardRemoteLibrary(RemoteLocation.create("http://uri"));

        assertThat(descriptor.generateLibspecFileName()).matches("^Remote_[0-9a-fA-F]{7}$");
    }

    @Test
    public void filenameOfReferencedLibraryIsItsNameWithAbbreviatedHashedSuffix() {
        final ReferencedLibraryArgumentsVariant argsVariant = ReferencedLibraryArgumentsVariant.create();

        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/to/library/lib.py"), argsVariant);

        assertThat(descriptor.generateLibspecFileName()).matches("^lib_[0-9a-fA-F]{7}$");
    }
}
