package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class RobotSuiteFile implements RobotElement {

    private final RobotElement parent;

    private final IFile file;

    private List<RobotElement> sections = null;

    private RobotEditorClosedListener listener;


    public RobotSuiteFile(final RobotElement parent, final IFile file) {
        this.parent = parent;
        this.file = file;
    }

    public RobotSuiteFileSection createRobotSection(final String name) {
        final RobotSuiteFileSection section;
        final boolean readOnly = !isEditable();
        if (name.equals(RobotVariablesSection.SECTION_NAME)) {
            section = new RobotVariablesSection(this, readOnly);
        } else if (name.equals(RobotSuiteSettingsSection.SECTION_NAME)) {
            section = new RobotSuiteSettingsSection(this, readOnly);
        } else if (name.equals(RobotCasesSection.SECTION_NAME)) {
            section = new RobotCasesSection(this, readOnly);
        } else if (name.equals(RobotKeywordsSection.SECTION_NAME)) {
            section = new RobotKeywordsSection(this, readOnly);
        } else {
            section = new RobotSuiteFileSection(this, name, readOnly);
        }

        if (getSections().contains(section)) {
            return (RobotSuiteFileSection) sections.get(sections.indexOf(section));
        } else {
            sections.add(section);
            return section;
        }
    }

    public List<RobotElement> getSections() {
        if (sections == null) {
            sections = new ArrayList<>();
            try {
                sections.addAll(createParser().parseRobotFileSections(this));

                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        final IPartService service = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                .getPartService();
                        listener = new RobotEditorClosedListener();
                        ContextInjectionFactory.inject(listener, getContext().getActiveLeaf());
                        service.addPartListener(listener);
                    }
                });
            } catch (final IOException e) {
                throw new RuntimeException("Unable to read sections");
            }
        }
        return sections;
    }

    private IEclipseContext getContext() {
        return (IEclipseContext) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IEclipseContext.class);
    }

    void dispose() {
        ContextInjectionFactory.uninject(listener, getContext().getActiveLeaf());
        sections = null;
        listener = null;
    }

    protected FileSectionsParser createParser() {
        return new FileSectionsParser(file);
    }

    protected void refreshOnFileChange() {
        sections = null;
        getSections();
    }

    List<RobotElementChange> synchronizeChanges(final IResourceDelta delta) {
        if ((delta.getFlags() & IResourceDelta.MARKERS) != IResourceDelta.MARKERS) {
            refreshOnFileChange();
        }
        return new ArrayList<>();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() == getClass()) {
            final RobotSuiteFile other = (RobotSuiteFile) obj;
            return Objects.equals(file, other.file);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public ImageDescriptor getImage() {
        return RobotImages.getRobotImage();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new OpenStrategy();
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    public IFile getFile() {
        return file;
    }

    @Override
    public List<RobotElement> getChildren() {
        return sections == null ? Lists.<RobotElement> newArrayList() : sections;
    }

    public boolean isEditable() {
        return !file.isReadOnly();
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return this;
    }

    public RobotProject getProject() {
        return RobotFramework.getModelManager().getModel().createRobotProject(file.getProject());
    }

    public void commitChanges(final IProgressMonitor monitor) throws CoreException {
        file.setContents(new FileSectionsEmiter(this).emit(), true, true, monitor);
    }

    public Optional<RobotElement> findSection(final Class<? extends RobotElement> sectionClass) {
        return Iterables.tryFind(getSections(), new Predicate<RobotElement>() {
            @Override
            public boolean apply(final RobotElement element) {
                return sectionClass.isInstance(element);
            }
        });
    }

    public List<LibrarySpecification> getImportedLibraries() {
        final Optional<RobotElement> section = findSection(RobotSuiteSettingsSection.class);
        final List<String> alreadyImported = newArrayList();
        if (section.isPresent()) {
            final List<RobotElement> importSettings = ((RobotSuiteSettingsSection) section.get()).getImportSettings();
            for (final RobotElement element : importSettings) {
                final RobotSetting setting = (RobotSetting) element;
                if (SettingsGroup.LIBRARIES == setting.getGroup()) {
                    final String name = setting.getArguments().isEmpty() ? null : setting.getArguments().get(0);
                    if (name != null) {
                        alreadyImported.add(name);
                    }
                }
            }
        }

        final List<LibrarySpecification> imported = newArrayList();
        for (final LibrarySpecification spec : getProject().getStandardLibraries()) {
            if (spec.isAccessibleWithoutImport() || alreadyImported.contains(spec.getName())) {
                imported.add(spec);
            }
        }
        for (final LibrarySpecification spec : getProject().getReferencedLibraries()) {
            if (alreadyImported.contains(spec.getName())) {
                imported.add(spec);
            }
        }
        return imported;
    }

    public List<RobotKeywordDefinition> getUserDefinedKeywords() {
        final List<RobotKeywordDefinition> userKeywords = newArrayList();
        final Optional<RobotElement> sectionOptional = findSection(RobotKeywordsSection.class);
        if (sectionOptional.isPresent()) {
            final RobotKeywordsSection section = (RobotKeywordsSection) sectionOptional.get();
            return section.getUserDefinedKeywords();
        }
        return userKeywords;
    }
}
