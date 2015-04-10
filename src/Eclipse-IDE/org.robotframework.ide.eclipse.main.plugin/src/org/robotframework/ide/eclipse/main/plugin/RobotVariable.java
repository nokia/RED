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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesEditorPage;

public class RobotVariable implements RobotElement {

    enum Type {
        SCALAR {
            @Override
            public String getMark() {
                return "$";
            }
        },
        LIST {
            @Override
            public String getMark() {
                return "@";
            }
        };

        public abstract String getMark();
    }

    private final RobotSuiteFileSection section;
    private String name;
    private final Type type;
    private String value;
    private String comment;

    public RobotVariable(final RobotSuiteFileSection section, final Type type, final String name,
            final String value, final String comment) {
        this.section = section;
        this.type = type;
        this.name = name;
        this.value = value;
        this.comment = comment;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() == getClass()) {
            final RobotVariable other = (RobotVariable) obj;
            return section.equals(other.section) && name.equals(other.name) && value.equals(other.value)
                    && comment.equals(other.comment);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(section, name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImageDescriptor getImage() {
        return RobotImages.getRobotVariableImage();
    }
    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new OpenStrategy() {

            @Override
            public void run() {
                final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
                final IEditorDescriptor desc = editorRegistry.findEditor(RobotFormEditor.ID);
                try {
                    final RobotFormEditor editor = (RobotFormEditor) page.openEditor(new FileEditorInput(getFile()),
                            desc.getId());
                    final VariablesEditorPage variablesPage = (VariablesEditorPage) editor.activatePage(section);
                    variablesPage.revealVariable(RobotVariable.this);
                } catch (final PartInitException e) {
                    throw new RuntimeException("Unable to open editor for file: " + getFile().getName(), e);
                }
            }
        };
    }

    protected IFile getFile() {
        return section.getFile();
    }

    @Override
    public RobotElement getParent() {
        return section;
    }

    @Override
    public List<RobotElement> getChildren() {
        return new ArrayList<>();
    }

    public String getComment() {
        return comment;
    }

    public String getValue() {
        return value;
    }

    public String getPrefix() {
        return type.getMark() + "{";
    }

    public String getSuffix() {
        return "}";
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
