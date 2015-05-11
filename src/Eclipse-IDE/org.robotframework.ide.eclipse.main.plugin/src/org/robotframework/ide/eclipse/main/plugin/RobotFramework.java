package org.robotframework.ide.eclipse.main.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotEnvironments;

public class RobotFramework extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.robotframework.ide.eclipse.main.plugin";

    private static RobotFramework plugin;

    private final List<File> installedPythons = new ArrayList<>();

    public static RobotFramework getDefault() {
        return plugin;
    }

    static ImageDescriptor getImageDescriptor(final String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

    public static RobotModelManager getModelManager() {
        return RobotModelManager.getInstance();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        final Job loadRobotStdLibs = new Job("Initializing robot framework") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                // RobotRuntimeEnvironment.whereIsDefaultPython();
                // final IPath metadataDirPath = getStateLocation();
                //
                // try {
                // ResourcesPlugin.getWorkspace().getRoot().findMember(metadataDirPath).refreshLocal(1,
                // monitor);
                // } catch (final CoreException e1) {
                // // TODO Auto-generated catch block
                // e1.printStackTrace();
                // }
                //
                // final IPath stdLibsPath =
                // metadataDirPath.append(".robotframeworks");
                //
                // final IFile file =
                // ResourcesPlugin.getWorkspace().getRoot().getFile(stdLibsPath);
                // if (file.exists()) {
                // final List<String> currentPaths =
                // InstalledRuntimesFileParser.parseRuntimePaths(file);
                // installedPythons.addAll(Collections2.transform(currentPaths,
                // new Function<String, File>() {
                // @Override
                // public File apply(final String path) {
                // return new File(path);
                // }
                // }));
                // } else {
                // try {
                // final File python =
                // RobotRuntimeEnvironment.whereIsDefaultPython();
                // installedPythons.add(python);
                // if (python == null) {
                // file.create(new ByteArrayInputStream(new byte[0]), true,
                // monitor);
                // } else {
                // final InputStream source = new
                // ByteArrayInputStream(python.getAbsolutePath().getBytes());
                // file.create(source, true, monitor);
                // }
                // } catch (final CoreException e) {
                // getLog().log(
                // new Status(IStatus.ERROR, PLUGIN_ID,
                // "Unable to create metadata file with robot frameworks paths",
                // e));
                // return Status.CANCEL_STATUS;
                // }
                // }
                return Status.OK_STATUS;
            }
        };
        loadRobotStdLibs.setPriority(Job.LONG);
        loadRobotStdLibs.schedule();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
        installedPythons.clear();
        RobotModelManager.getInstance().dispose();
    }

    public RobotRuntimeEnvironment getActiveRobotInstallation() {
        return InstalledRobotEnvironments.getActiveRobotInstallation(getPreferenceStore());
    }
}
