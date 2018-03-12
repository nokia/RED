/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.libraries;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
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

        assertThat(descriptor.isStandardLibrary()).isTrue();
        assertThat(descriptor.isStandardRemoteLibrary()).isTrue();
        assertThat(descriptor.isReferencedLibrary()).isFalse();
        assertThat(descriptor.getKeywordsScope()).isEqualTo(KeywordScope.STD_LIBRARY);

        assertThat(descriptor.equals(LibraryDescriptor
                .ofStandardRemoteLibrary(RemoteLocation.create("http://uri")))).isTrue();
        assertThat(descriptor.equals(LibraryDescriptor
                .ofStandardRemoteLibrary(RemoteLocation.create("http://otheruri")))).isFalse();

        assertThat(descriptor.hashCode()).isEqualTo(LibraryDescriptor
                .ofStandardRemoteLibrary(RemoteLocation.create("http://uri")).hashCode());
        assertThat(descriptor.hashCode()).isNotEqualTo(LibraryDescriptor
                .ofStandardRemoteLibrary(RemoteLocation.create("http://otheruri")).hashCode());
    }

    @Test
    public void testReferencedLibraryDescriptorProperties() {
        final LibraryDescriptor descriptor = LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/to/library"));

        assertThat(descriptor.getName()).isEqualTo("lib");
        assertThat(descriptor.getArguments()).isEmpty();
        assertThat(descriptor.getLibraryType()).isEqualTo(LibraryType.PYTHON);
        assertThat(descriptor.getPath()).isEqualTo("path/to/library");
        assertThat(descriptor.getKeywordsScope()).isEqualTo(KeywordScope.REF_LIBRARY);

        assertThat(descriptor.isStandardLibrary()).isFalse();
        assertThat(descriptor.isStandardRemoteLibrary()).isFalse();
        assertThat(descriptor.isReferencedLibrary()).isTrue();

        assertThat(descriptor.equals(LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/to/library")))).isTrue();
        assertThat(descriptor.equals(LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.JAVA, "lib", "path/to/library")))).isFalse();
        assertThat(descriptor.equals(LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "l", "path/to/library")))).isFalse();
        assertThat(descriptor.equals(LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/other")))).isFalse();

        assertThat(descriptor.hashCode()).isEqualTo(LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/to/library")).hashCode());
        assertThat(descriptor.hashCode()).isNotEqualTo(LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.JAVA, "lib", "path/to/library")).hashCode());
        assertThat(descriptor.hashCode()).isNotEqualTo(LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "l", "path/to/library")).hashCode());
        assertThat(descriptor.hashCode()).isNotEqualTo(LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/other")).hashCode());
    }

    @Test
    public void filepathIsProperlyProvided() {
        assertThat(LibraryDescriptor.ofStandardLibrary("StdLib").getFilepath()).isNull();
        assertThat(LibraryDescriptor.ofStandardRemoteLibrary(RemoteLocation.create("http://uri")).getFilepath())
                .isNull();
        assertThat(LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/to/library"))
                .getFilepath()).isEqualTo("path/to/library/lib");
        assertThat(LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "lib.a.b", "path/to/library"))
                .getFilepath()).isEqualTo("path/to/library/lib/a/b");
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
        final LibraryDescriptor descriptor = LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "lib", "path/to/library"));

        assertThat(descriptor.generateLibspecFileName()).matches("^lib_[0-9a-fA-F]{7}$");
    }
}
