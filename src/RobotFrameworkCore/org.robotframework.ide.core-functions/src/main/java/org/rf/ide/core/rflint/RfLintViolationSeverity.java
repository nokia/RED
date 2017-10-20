package org.rf.ide.core.rflint;


public enum RfLintViolationSeverity {
    ERROR,
    WARNING,
    IGNORE,
    OTHER;

    public static RfLintViolationSeverity from(final String severity) {
        if ("E".equals(severity)) {
            return ERROR;
        } else if ("W".equals(severity)) {
            return WARNING;
        } else if ("I".equals(severity)) {
            return IGNORE;
        }
        return OTHER;
    }
}
