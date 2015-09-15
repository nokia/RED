package org.robotframework.ide.eclipse.main.plugin.assist;

import org.robotframework.ide.core.testData.importer.AVariableImported;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;

public class RedVariableProposal {

    private final String name;

    private final String source;

    private final String value;

    private final String comment;

    private final VariableType type;

    private RedVariableProposal(final String name, final String source, final String value, final String comment,
            final VariableType type) {
        this.name = name;
        this.source = source;
        this.value = value;
        this.comment = comment;
        this.type = type;
    }

    static RedVariableProposal create(final RobotVariable robotVariable) {
        return new RedVariableProposal(robotVariable.getPrefix() + robotVariable.getName() + robotVariable.getSuffix(),
                robotVariable.getSuiteFile().getName(), robotVariable.getValue(), robotVariable.getComment(),
                VariableType.LOCAL);
    }

    static RedVariableProposal create(final AVariableImported<?> variable, final String filePath) {
        return new RedVariableProposal(variable.getName(), filePath, variable.getValue().toString(), "",
                VariableType.IMPORTED);
    }

    static RedVariableProposal create(final String variable, final String path) {
        return new RedVariableProposal(variable, path, "", "", VariableType.IMPORTED);
    }

    static RedVariableProposal createBuiltIn(final String name, final String value) {
        return new RedVariableProposal(name, "BuiltIn", value, "", VariableType.BUILTIN);
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    public String getValue() {
        return value;
    }

    public String getComment() {
        return comment;
    }

    public VariableType getType() {
        return type;
    }

    public enum VariableType {
        LOCAL,
        IMPORTED,
        BUILTIN
    }
}
