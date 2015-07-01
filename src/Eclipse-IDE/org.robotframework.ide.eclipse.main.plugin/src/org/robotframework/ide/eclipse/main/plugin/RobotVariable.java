package org.robotframework.ide.eclipse.main.plugin;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

public class RobotVariable implements RobotElement {

    public enum Type {
        SCALAR {
            @Override
            public String getMark() {
                return "$";
            }

            @Override
            public ImageDescriptor getImage() {
                return RobotImages.getRobotScalarVariableImage();
            }
        },
        LIST {
            @Override
            public String getMark() {
                return "@";
            }

            @Override
            public ImageDescriptor getImage() {
                return RobotImages.getRobotListVariableImage();
            }
        },
        DICTIONARY {
            @Override
            public String getMark() {
                return "&";
            }

            @Override
            public ImageDescriptor getImage() {
                return RobotImages.getRobotDictionaryVariableImage();
            }
        };

        public abstract String getMark();

        public abstract ImageDescriptor getImage();
    }

    public static boolean isVariable(final String expression) {
        for (final Type type : EnumSet.allOf(Type.class)) {
            if (expression.startsWith(type.getMark() + "{") && expression.endsWith("}") ) {
                return true;
            }
        }
        return false;
    }

    private RobotSuiteFileSection section;
    private String name;
    private Type type;
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
            final int index1 = getIndexInParent(this);
            final int index2 = getIndexInParent(other);

            return getParent() == other.getParent() && index1 == index2 && section.equals(other.section)
                    && name.equals(other.name) && value.equals(other.value)
                    && comment.equals(other.comment);
        }
        return false;
    }

    private int getIndexInParent(final RobotVariable var) {
        for (int i = 0; i < var.getParent().getChildren().size(); i++) {
            if (var.getParent().getChildren().get(i) == var) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(section, name);
    }

    // representation for debug; provide own specialized method if some kind of
    // string representation is desired
    @Override
    public String toString() {
        return getPrefix() + name + getSuffix() + "= " + value + "# " + comment;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImageDescriptor getImage() {
        return type.getImage();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new PageActivatingOpeningStrategy(page, getSuiteFile().getFile(), section, this);
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

    public void setParent(final RobotSuiteFileSection variablesSection) {
        this.section = variablesSection;
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return section.getSuiteFile();
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }
}
