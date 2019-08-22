/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;


public class RobotProjectConfigEvents {

    public static final String ROBOT_CONFIG_MARKER_CHANGED = "robot/redxml/marker/changed";

    public static final String ROBOT_CONFIG_ENV_LOADING_STARTED = "robot/redxml/env/loading_started";
    public static final String ROBOT_CONFIG_ENV_LOADED = "robot/redxml/env/loaded";

    public static final String ROBOT_CONFIG_VAR_MAP_DETAIL_CHANGED = "robot/redxml/detail/varmap/changed/*";
    public static final String ROBOT_CONFIG_VAR_MAP_NAME_CHANGED = "robot/redxml/detail/varmap/changed/name";
    public static final String ROBOT_CONFIG_VAR_MAP_VALUE_CHANGED = "robot/redxml/detail/varmap/changed/value";
    public static final String ROBOT_CONFIG_VAR_MAP_STRUCTURE_CHANGED = "robot/redxml/structural/varmap/changed";

    public static final String ROBOT_CONFIG_VAR_FILE_PATH_CHANGED = "robot/redxml/detail/varfile/changed/path";
    public static final String ROBOT_CONFIG_VAR_FILE_STRUCTURE_CHANGED = "robot/redxml/structural/varfile/changed";

    public static final String ROBOT_CONFIG_LIBRARY_ADDED_REMOVED = "robot/redxml/structural/libs/added_removed";
    public static final String ROBOT_CONFIG_LIBRARY_MODE_CHANGED = "robot/redxml/detail/libs/changed";

    public static final String ROBOT_CONFIG_LIBRARIES_ARGUMENTS_ADDED = "robot/redxml/structural/libs/args/added";
    public static final String ROBOT_CONFIG_LIBRARIES_ARGUMENTS_REMOVED = "robot/redxml/structural/libs/args/removed";
    public static final String ROBOT_CONFIG_LIBRARIES_ARGUMENTS_CHANGED = "robot/redxml/detail/libs/args/changed";

    public static final String ROBOT_CONFIG_PYTHONPATH_CHANGED = "robot/redxml/detail/pythonpath/changed";
    public static final String ROBOT_CONFIG_PYTHONPATH_STRUCTURE_CHANGED = "robot/redxml/structural/pythonpath/changed";

    public static final String ROBOT_CONFIG_CLASSPATH_CHANGED = "robot/redxml/detail/classpath/changed";
    public static final String ROBOT_CONFIG_CLASSPATH_STRUCTURE_CHANGED = "robot/redxml/structural/classpath/changed";

    public static final String ROBOT_CONFIG_VALIDATION_EXCLUSIONS_STRUCTURE_CHANGED = "robot/redxml/structural/validationexclusions/changed";

}
