package org.robotframework.ide.eclipse.main.plugin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

class FileSectionsEmiter {

    private final RobotSuiteFile file;

    public FileSectionsEmiter(final RobotSuiteFile robotSuiteFile) {
        this.file = robotSuiteFile;
    }

    public InputStream emit() {
        return new ByteArrayInputStream(emitString().getBytes(Charsets.UTF_8));
    }

    private String emitString() {
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

                builder.append(getCellsSeparator());
                builder.append(variable.getValue());

                if (!variable.getComment().isEmpty()) {
                    builder.append(getCellsSeparator());
                    builder.append("# ");
                    builder.append(variable.getComment());
                }

                builder.append('\n');
            } else if (element instanceof RobotSetting) {
                final RobotSetting setting = (RobotSetting) element;
                
                builder.append(setting.getName());
                
                if (!setting.getArguments().isEmpty()) {
                    builder.append(getCellsSeparator());
                    builder.append(Joiner.on(getCellsSeparator()).join(setting.getArguments()));
                }

                if (!setting.getComment().isEmpty()) {
                    builder.append(getCellsSeparator());
                    builder.append("# ");
                    builder.append(setting.getComment());
                }
                builder.append('\n');
            }
        }
    }

    private String getCellsSeparator() {
        return Strings.repeat(" ", 4);
    }
}
