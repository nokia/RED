/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.jvmutils.process;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.executor.RedSystemProperties;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public class WindowsProcessTreeHandler extends AProcessTreeHandler {

    WindowsProcessTreeHandler(final OSProcessHelper helper) {
        super(helper);
    }

    @Override
    public boolean isSupported(final Process process) {
        final String procClassName = process.getClass().getName();
        return (procClassName.equals("java.lang.Win32Process") || procClassName.equals("java.lang.ProcessImpl"));
    }

    @Override
    public long getProcessPid(final Process process) {
        long pid = ProcessInformation.PROCESS_NOT_FOUND;
        try {
            final Field f = process.getClass().getDeclaredField("handle");
            f.setAccessible(true);
            long handle = f.getLong(process);

            Kernel32 kernel = Kernel32.INSTANCE;
            HANDLE winHandle = new HANDLE();
            winHandle.setPointer(Pointer.createConstant(handle));
            pid = kernel.GetProcessId(winHandle);
        } catch (Throwable e) {
        }

        return pid;
    }

    @Override
    public List<String> getChildPidsCommand(final long processPid) {
        return Arrays.asList("cmd.exe", "/c", "wmic", "process", "where", "ParentProcessID=" + processPid, "get",
                "ProcessId", "/format:csv");
    }

    @Override
    public List<String> getKillProcessCommand(final ProcessInformation procInformation) {
        return Arrays.asList("cmd.exe", "/c", "wmic", "process", "where", "ProcessID=" + procInformation.pid(),
                "delete");
    }

    @Override
    public List<String> getKillProcessTreeCommand(final ProcessInformation procInformation) {
        return Arrays.asList("cmd.exe", "/c", "wmic", "process", "where",
                "\"ProcessID=" + procInformation.pid() + " or ParentProcessID=" + procInformation.pid() + "\"",
                "delete");
    }

    @Override
    public boolean isSupportedOS() {
        return RedSystemProperties.isWindowsPlatform();
    }

}
