package org.robotframework.ide.eclipse.main.plugin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class FileSectionsEmiter {

    private final RobotSuiteFile file;

    public FileSectionsEmiter(final RobotSuiteFile robotSuiteFile) {
        this.file = robotSuiteFile;
    }

    public InputStream emit() {
        return new ByteArrayInputStream(emitString().getBytes(Charsets.UTF_8));
    }

    public String emitString() {
        final StringBuilder builder = new StringBuilder();

        for (final RobotElement element : file.getChildren()) {
            final RobotSuiteFileSection section = (RobotSuiteFileSection) element;

            builder.append("\n");
            builder.append("*** ");
            builder.append(section.getName());
            builder.append(" ***");
            builder.append("\n");

            emitSection(section, builder);
        }
        if (builder.length() > 0) {
            builder.delete(0, 1);
        }
        return builder.toString();
    }

    private void emitSection(final RobotSuiteFileSection section, final StringBuilder builder) {
        for (final RobotElement element : section.getChildren()) {
            if (element instanceof RobotVariable) {
                final RobotVariable variable = (RobotVariable) element;
                
                builder.append(variable.getPrefix());
                builder.append(variable.getName());
                builder.append(variable.getSuffix());


                if (!variable.getValue().isEmpty()) {
                    builder.append(getCellsSeparator());
                    builder.append(variable.getValue());
                }

                if (!variable.getComment().isEmpty()) {
                    builder.append(getCellsSeparator());
                    builder.append("# ");
                    builder.append(variable.getComment());
                }
                builder.append('\n');
            } else if (element instanceof RobotSetting) {
                final RobotSetting setting = (RobotSetting) element;
                emitKeywordCall(builder, setting);
            } else if (element instanceof RobotCase) {
                builder.append(element.getName());
                builder.append('\n');

                for (final RobotElement el : element.getChildren()) {
                    emitKeywordCall(builder, getCellsSeparator(), (RobotKeywordCall) el);
                }
                builder.append('\n');
            } else if (element instanceof RobotKeywordDefinition) {
                builder.append(element.getName());
                builder.append('\n');

                for (final RobotElement el : element.getChildren()) {
                    emitKeywordCall(builder, getCellsSeparator(), (RobotKeywordCall) el);
                }
                builder.append('\n');
            }
        }
    }

    private void emitKeywordCall(final StringBuilder builder, final RobotKeywordCall call) {
        emitKeywordCall(builder, "", call);
    }

    private void emitKeywordCall(final StringBuilder builder, final String indent, final RobotKeywordCall call) {
        builder.append(indent);
        final String name = call instanceof RobotDefinitionSetting ? "[" + call.getName() + "]" : call.getName();
        builder.append(name);

        if (!call.getArguments().isEmpty()) {
            builder.append(getCellsSeparator());
            builder.append(Joiner.on(getCellsSeparator()).join(call.getArguments()));
        }

        if (!call.getComment().isEmpty()) {
            builder.append(getCellsSeparator());
            builder.append("# ");
            builder.append(call.getComment());
        }
        builder.append('\n');
    }

    private String getCellsSeparator() {
        return Strings.repeat(" ", 4);
    }
}
