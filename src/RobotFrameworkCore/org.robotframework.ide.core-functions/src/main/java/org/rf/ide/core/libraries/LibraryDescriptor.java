/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.libraries;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public final class LibraryDescriptor {

    public static LibraryDescriptor ofStandardLibrary(final String libraryName) {
        return new LibraryDescriptor(libraryName, LibraryType.PYTHON, null, new ArrayList<>());
    }

    public static LibraryDescriptor ofStandardRemoteLibrary(final RemoteLocation remoteLocation) {
        return new LibraryDescriptor("Remote", LibraryType.PYTHON, null, newArrayList(remoteLocation.getUri()));
    }

    public static LibraryDescriptor ofReferencedLibrary(final ReferencedLibrary refLibrary) {
        return new LibraryDescriptor(refLibrary.getName(), refLibrary.provideType(), refLibrary.getPath(),
                new ArrayList<>());
    }

    private final String name;

    private final String path;

    private final LibraryType type;

    private final List<String> arguments;

    public LibraryDescriptor(final String name, final LibraryType type, final String path,
            final List<String> arguments) {
        this.name = name;
        this.type = type;
        this.path = path;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public LibraryType getLibraryType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public String getFilepath() {
        if (path != null && type == LibraryType.PYTHON) {
            return path + "/" + name.replaceAll("\\.", "/");
        } else {
            return path;
        }
    }

    public List<String> getArguments() {
        return arguments;
    }

    public boolean isStandardRemoteLibrary() {
        return name.toLowerCase().equals("remote");
    }

    public boolean isStandardLibrary() {
        return path == null;
    }

    public boolean isReferencedLibrary() {
        return !isStandardLibrary();
    }

    public KeywordScope getKeywordsScope() {
        return isStandardLibrary() ? KeywordScope.STD_LIBRARY : KeywordScope.REF_LIBRARY;
    }

    public String generateLibspecFileName() {
        if (path == null && arguments.isEmpty()) {
            return name;
        }

        @SuppressWarnings("deprecation")
        final Hasher hasher = Hashing.sha1().newHasher();
        if (path != null) {
            hasher.putString(path, Charsets.UTF_8);
        }
        for (final String arg : arguments) {
            hasher.putString(arg, Charsets.UTF_8);
        }

        // we use 7-digits abbreviation of SHA-1 hash; from approximation formulas of birthday
        // paradox the probability that two hashes will collide when there are 256 libraries with
        // the same name in project is < 2 ** (-13) which is around 0.012%
        final String hashAbbreviated = hasher.hash().toString().substring(0, 7);
        return name + "_" + hashAbbreviated;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == LibraryDescriptor.class) {
            final LibraryDescriptor that = (LibraryDescriptor) obj;
            return Objects.equal(this.name, that.name) && Objects.equal(this.type, that.type)
                    && Objects.equal(this.path, that.path) && Objects.equal(this.arguments, that.arguments);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, type, path, arguments);
    }
}
