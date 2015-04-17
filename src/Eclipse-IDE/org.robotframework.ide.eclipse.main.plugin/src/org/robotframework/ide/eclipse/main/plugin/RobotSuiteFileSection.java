package org.robotframework.ide.eclipse.main.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable.Type;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

public class RobotSuiteFileSection implements RobotElement {

    private final IFile file;
    private final String name;
    private final boolean readOnly;
    private final List<RobotElement> variables = new ArrayList<>();
    private final RobotElement parent;

    public RobotSuiteFileSection(final RobotSuiteFile parent, final String name,
            final boolean readOnly) {
        this.parent = parent;
        this.file = parent.getFile();
        this.name = name;
        this.readOnly = readOnly;
    }

    public RobotVariable createListVariable(final String name, final String value, final String comment) {
        final RobotVariable robotVariable = new RobotVariable(this, Type.LIST, name, value, comment);
        variables.add(robotVariable);
        return robotVariable;
    }

    public RobotVariable createScalarVariable(final String name, final String value, final String comment) {
        final RobotVariable robotVariable = new RobotVariable(this, Type.SCALAR, name, value, comment);
        variables.add(robotVariable);
        return robotVariable;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() == getClass()) {
            final RobotSuiteFileSection other = (RobotSuiteFileSection) obj;
            return Objects.equals(file, other.file) && name.equals(other.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImageDescriptor getImage() {
        return RobotImages.getRobotCasesFileSectionImage();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new OpenStrategy() {

            @Override
            public void run() {
                final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
                final IEditorDescriptor desc = editorRegistry.findEditor(RobotFormEditor.ID);
                try {
                    final RobotFormEditor editor = (RobotFormEditor) page.openEditor(new FileEditorInput(file),
                            desc.getId());
                    editor.activatePage(RobotSuiteFileSection.this);
                } catch (final PartInitException e) {
                    throw new RuntimeException("Unable to open editor for file: " + file.getName(), e);
                }
            }
        };
    }

    public IFile getFile() {
        return file;
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    @Override
    public List<RobotElement> getChildren() {
        return variables;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean contains(final RobotElement element) {
        for (final RobotElement variable : variables) {
            if (variable.equals(element) || element.contains(variable)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return (RobotSuiteFile) this.getParent();
    }
}
