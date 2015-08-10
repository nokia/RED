package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecificationReader;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class LibspecsFolder {

    private static final String LIBSPEC_FILE_EXTENSION = ".libspec";

    private static final String FOLDER_NAME = "libspecs";

    private final IFolder folder;

    public LibspecsFolder(final IFolder folder) {
        this.folder = folder;
    }

    public IFolder getResource() {
        return folder;
    }

    public static LibspecsFolder get(final IProject project) {
        return new LibspecsFolder(project.getFolder(FOLDER_NAME));
    }

    public static LibspecsFolder createIfNeeded(final IProject project) throws CoreException {
        final LibspecsFolder libspecsFolder = get(project);
        if (!libspecsFolder.exists()) {
            libspecsFolder.folder.create(IResource.FORCE | IResource.DERIVED, true, null);
        }
        return libspecsFolder;
    }

    public boolean exists() {
        return folder.exists();
    }

    public void removeNonSpecResources() throws CoreException {
        if (!folder.exists()) {
            return;
        }
        for (final IResource resource : folder.members(IContainer.INCLUDE_HIDDEN)) {
            if (resource.exists() && !resource.getName().endsWith(LIBSPEC_FILE_EXTENSION)) {
                resource.delete(true, null);
            }
        }
    }

    public void removeContent() throws CoreException {
        if (!folder.exists()) {
            return;
        }
        for (final IResource resource : folder.members(IContainer.INCLUDE_HIDDEN)) {
            if (resource.exists()) {
                resource.delete(true, null);
            }
        }
    }

    public void remove() throws CoreException {
        if (exists()) {
            final IResource project = folder.getProject();
            folder.delete(true, null);
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        }
    }

    public boolean shouldRegenerateLibspecs(final IResourceDelta delta, final int kind) {
        if (delta == null) {
            return true;
        }

        final IFile cfgFile = folder.getProject().getFile(RobotProjectConfig.FILENAME);
        // full build is being perfomed or config file has changed
        return kind == IncrementalProjectBuilder.FULL_BUILD
                || delta.findMember(cfgFile.getProjectRelativePath()) != null
                || delta.findMember(folder.getProjectRelativePath()) != null;
    }

    public List<IFile> collectSpecsWithDifferentVersion(final List<String> stdLibs, final String version)
            throws CoreException {
        final List<IFile> toRecreate = newArrayList(Lists.transform(stdLibs, new Function<String, IFile>() {
            @Override
            public IFile apply(final String libName) {
                return folder.getFile(libName + LIBSPEC_FILE_EXTENSION);
            }
        }));

        for (final IResource resource : folder.members()) {
            if (resource.getType() == IResource.FILE && resource.getName().endsWith(LIBSPEC_FILE_EXTENSION)) {
                final IFile specFile = (IFile) resource;
                final LibrarySpecification spec = LibrarySpecificationReader.readSpecification(specFile);
                if (version.startsWith("Robot Framework " + spec.getVersion())) {
                    toRecreate.remove(specFile);
                }
            }
        }
        return toRecreate;
    }

    public IFile getSpecFile(final String libraryName) {
        return getFile(libraryName + LIBSPEC_FILE_EXTENSION);
    }

    public IFile getFile(final String name) {
        return folder.getFile(name);
    }

    public IResource[] members() throws CoreException {
        return folder.members();
    }
}
