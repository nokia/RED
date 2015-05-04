package org.robotframework.ide.eclipse.main.plugin;


public class RobotModelEvents {

    public static final String ROBOT_SUITE_FILE_ALL = "robot/model/editor/file/structural/*";

    public static final String ROBOT_SUITE_STRUCTURAL_SECTION_ALL = "robot/model/editor/file/structural/section/*";

    public static final String ROBOT_SUITE_SECTION_ADDED = "robot/model/editor/file/structural/section/added";

    public static final String ROBOT_SUITE_SECTION_REMOVED = "robot/model/editor/file/structural/section/removed";


    public static final String ROBOT_VARIABLE_STRUCTURAL_ALL = "robot/model/editor/file/structural/variable/*";

    public static final String ROBOT_VARIABLE_ADDED = "robot/model/editor/file/structural/variable/added";

    public static final String ROBOT_VARIABLE_REMOVED = "robot/model/editor/file/structural/variable/removed";

    public static final String ROBOT_VARIABLE_MOVED = "robot/model/editor/file/structural/variable/moved";

    public static final String ROBOT_VARIABLES_SORTED = "robot/model/editor/file/structural/variable/sorted";


    public static final String ROBOT_VARIABLE_DETAIL_CHANGE_ALL = "robot/model/editor/file/detail/variable/changed/*";

    public static final String ROBOT_VARIABLE_TYPE_CHANGE = "robot/model/editor/file/detail/variable/changed/type";

    public static final String ROBOT_VARIABLE_NAME_CHANGE = "robot/model/editor/file/detail/variable/changed/name";

    public static final String ROBOT_VARIABLE_VALUE_CHANGE = "robot/model/editor/file/detail/variable/changed/value";

    public static final String ROBOT_VARIABLE_COMMENT_CHANGE = "robot/model/editor/file/detail/variable/changed/comment";

    
    public static final String ROBOT_SETTINGS_STRUCTURAL_ALL = "robot/model/editor/file/structural/setting/*";

    public static final String ROBOT_SETTING_ADDED = "robot/model/editor/file/structural/setting/added";

    public static final String ROBOT_SETTING_REMOVED = "robot/model/editor/file/structural/setting/removed";


    public static final String ROBOT_SETTING_DETAIL_CHANGE_ALL = "robot/model/editor/file/detail/setting/changed/*";

    public static final String ROBOT_SETTING_ARGUMENT_CHANGED = "robot/model/editor/file/detail/setting/changed/argument";

    public static final String ROBOT_SETTING_COMMENT_CHANGED = "robot/model/editor/file/detail/setting/changed/comment";


    public static final String EXTERNAL_MODEL_CHANGE = "robot/model/external";

}
