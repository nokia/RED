package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;

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

    private RobotSuiteFileSection parent;
    private String name;
    private Type type;
    private String value;
    private String comment;

    public RobotVariable(final RobotSuiteFileSection section, final Type type, final String name,
            final String value, final String comment) {
        this.parent = section;
        this.type = type;
        this.name = name;
        this.value = value;
        this.comment = comment;
    }

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
        return new PageActivatingOpeningStrategy(page, getSuiteFile().getFile(), parent, this);
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    @Override
    public List<RobotElement> getChildren() {
        return new ArrayList<>();
    }

    @Override
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
        this.parent = variablesSection;
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return parent.getSuiteFile();
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }
}
