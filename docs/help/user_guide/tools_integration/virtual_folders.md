## Working with virtual folders mounted via SSH

If there is a need to work with remote folders mounted as SSH, this can be
done in multiple way.

Note

    RED is parsing each file during validation thus you may experience reduce validation speed due to slower access to remote folders.

  * ### Folder mount on OS level

Folders can be mounted as separate drive or mount points in the OS file system
(eg. using sshfs for Windows).

This is preferred way to work with remote folders. RED uses local Robot and
Python to execute actions on remote files to get information about libraries
or variables from Python modules.

When including such folder into Project, use appropriate option in Advanced
menu while creating or importing such folder (link to folder).

  * ### Remote System Explorer plugin

Another way of accessing such remote locations is to use Eclipse plugin [
Remote System Explorer](https://marketplace.eclipse.org/content/remote-system-
explorer-ssh-telnet-ftp-and-dstore-protocols)

This plugin allows to access to remote folders only inside Eclipse
application. RED will not be able to read and validate python variable files,
generate libspecs from users libraries.

Warning

    It was seen that RSE might cause Eclipse to be unresponsive while waiting for establish connection.

