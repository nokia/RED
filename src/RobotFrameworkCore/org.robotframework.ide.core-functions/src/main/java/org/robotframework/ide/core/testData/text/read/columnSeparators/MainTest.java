package org.robotframework.ide.core.testData.text.read.columnSeparators;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;
import org.robotframework.ide.core.testData.RobotFileDumper;
import org.robotframework.ide.core.testData.RobotParser;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotProjectHolder;


public class MainTest {

    public static void main(String[] args) throws IOException {
        mainTest(args);
    }


    public static void mainResTest(String[] args) throws IOException {
        List<PythonInstallationDirectory> whereArePythonInterpreters = RobotRuntimeEnvironment
                .whereArePythonInterpreters();
        RobotRuntimeEnvironment runtime = RobotRuntimeEnvironment
                .create(whereArePythonInterpreters.get(0));

        long start_time = System.nanoTime();
        runtime.getVariablesFromFile(
                "D://userdata//wypych//Desktop//var_as_properties.py",
                Arrays.asList(new String[0]));
        long end_time = System.nanoTime();
        long time_m = end_time - start_time;
        System.out.println("Time [ms]: "
                + TimeUnit.NANOSECONDS.toMillis(time_m));
        System.out.println("time_t [ns]: " + time_m);
    }


    public static void mainTest(String[] args) throws IOException {
        String file = "D:\\userdata\\wypych\\Desktop\\D2.txt";
        // String file = "D:\\userdata\\wypych\\Desktop\\BigScript.robot";
        List<PythonInstallationDirectory> whereArePythonInterpreters = RobotRuntimeEnvironment
                .whereArePythonInterpreters();
        RobotRuntimeEnvironment runtime = RobotRuntimeEnvironment
                .create(whereArePythonInterpreters.get(0));

        long start_time = System.nanoTime();
        RobotProjectHolder robotProject = new RobotProjectHolder(runtime);
        RobotParser parser = new RobotParser(robotProject);
        List<RobotFileOutput> parse = parser.parse(new File(file));
        RobotFileOutput robotFileOutput = parse.get(0);
        RobotFile fileModel = robotFileOutput.getFileModel();

        StringBuilder dump = new RobotFileDumper().dump(robotFileOutput);
        System.out.println("DUMP: " + dump);
        long end_time = System.nanoTime();

        long time_m = end_time - start_time;
        System.out.println("Time [ms]: "
                + TimeUnit.NANOSECONDS.toMillis(time_m));
        System.out.println("time_t [ns]: " + time_m);
    }
}
