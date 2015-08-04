package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;

public enum ProblemCategory {
    PROJECT_CONFIGURATION_FILE_DOES_NOT_EXIST(
        "This problem occurs when project has no " + RobotProjectConfig.FILENAME + " configuration file.\n"),
    PROJECT_CONFIGURATION_FILE_READING_PROBLEM(
        "This problem occurs when the " + RobotProjectConfig.FILENAME + " file cannot be read.\n"
        + "Either there is a problem accessing the file or its structure is broken.\n"),
    MISSING_ROBOT_ENVIRONMENT(
        "This problems occurs when there is no Robot Environment defined. It should be\n"
        + "defined in preferences and project may override this setting in its \n"
        + "configuration file"),
    CHOSEN_ENVIRONMENT_IS_NOT_A_PYTHON_INSTALLATION(
        "This problem occurs when location of specified Robot Environment does not seem\n"
        + "to be a python main directory."),
    CHOSEN_ENVIRONMENT_WITH_PYTHON_INSTALLATION_HAS_NO_ROBOT_INSTALLED(
         "This problem occurs when location of specified Robot Environment is a python\n"
         + "directory, but it seem to have no Robot modules installed."),
    LIBRARY_SPECIFICATION_FILE_CANNOT_BE_GENERATED("This problems occurs when for some reason"
        + " Robot framework is unable to generate library specification file.");

    private String id;
    private String description;

    private ProblemCategory(final String description) {
        this.id = "red.problem.category." + name();
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        final String name = name().toLowerCase();
        return (Character.toUpperCase(name.charAt(0)) + name.substring(1)).replaceAll("_", " ");
    }
}
