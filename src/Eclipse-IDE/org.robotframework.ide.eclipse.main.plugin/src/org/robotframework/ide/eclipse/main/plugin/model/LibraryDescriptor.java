package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;

import com.google.common.base.Objects;

public final class LibraryDescriptor {

    public static LibraryDescriptor ofStandardLibrary(final String libraryName) {
        return new LibraryDescriptor(libraryName);
    }

    public static LibraryDescriptor ofStandardRemoteLibrary(final RemoteLocation remoteLocation) {
        return new LibraryDescriptor("Remote", newArrayList(remoteLocation.getUri()));
    }

    public static LibraryDescriptor ofReferencedLibrary(final ReferencedLibrary refLibrary) {
        return new LibraryDescriptor(refLibrary.getName(), refLibrary.provideType(), refLibrary.getPath(),
                new ArrayList<>());
    }

    private final String name;

    private final String path;

    private final LibraryType type;

    private final List<String> arguments;

    public LibraryDescriptor(final String name) {
        this(name, null, null, new ArrayList<>());
    }

    public LibraryDescriptor(final String name, final List<String> arguments) {
        this(name, null, null, arguments);
    }

    public LibraryDescriptor(final String name, final LibraryType type, final String path) {
        this(name, type, path, new ArrayList<>());
    }

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

    public File getFilepath() {
        if (type == LibraryType.PYTHON) {
            return new File(path + "/" + name.replaceAll("\\.", "/"));
        } else {
            return new File(path);
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
        if (isStandardRemoteLibrary()) {
            return "Remote_" + arguments.get(0).replaceAll("[^A-Za-z0-9]", "_");
        } else {
            return name;
        }
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
