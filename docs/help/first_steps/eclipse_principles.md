## Eclipse principles

### Eclipse platform overview

The Eclipse platform itself is structured as subsystems which are implemented
in one or more plug-ins. The subsystems are built on top of a small runtime
engine. The figure below depicts a simplified view.

![](eclipse_principles/arch-npi.jpg)

#### Workbench

The term Workbench refers to the desktop development environment. The
Workbench aims to achieve seamless tool integration and controlled openness by
providing a common paradigm for the creation, management, and navigation of
workspace resources.

Each Workbench window contains one or more perspectives. Perspectives contain
views and editors and control what appears in certain menus and tool bars.
More than one Workbench window can exist on the desktop at any given time.

### Perspective & View

Each Workbench window contains one or more perspectives. A perspective defines
the initial set and layout of views in the Workbench window. Within the
window, each perspective shares the same set of editors. Each perspective
provides a set of functionality aimed at accomplishing a specific type of task
or works with specific types of resources. For example, the Java perspective
combines views that you would commonly use while editing Java source files,
while the Debug perspective contains the views that you would use while
debugging Java programs. As you work in the Workbench, you will probably
switch perspectives frequently.

Perspectives control what appears in certain menus and toolbars. They define
visible action sets, which you can change to customize a perspective. You can
save a perspective that you build in this manner, making your own custom
perspective that you can open again later.

Views support editors and provide alternative presentations as well as ways to
navigate the information in your Workbench. For example, the Project Explorer
and other navigation views display projects and other resources that you are
working with. Views also have their own menus. To open the menu for a view,
click the icon at the left end of the view's title bar. Some views also have
their own toolbars. The actions represented by buttons on view toolbars only
affect the items within that view. A view might appear by itself, or stacked
with other views in a tabbed notebook. You can change the layout of a
perspective by opening and closing views and by docking them in different
positions in the Workbench window.

![](eclipse_principles/main_view.png)

### Workspace & Project

The central hub for user's data files is called a workspace.

The workspace contains a collection of resources. From the user's perspective,
there are three different types of resources: projects, folders, and files. A
project is a collection of any number of files and folders. It is a container
for organizing other resources that relate to a specific area. Files and
folders are just like files and directories in the file system. A folder
contains other folders or files. A file contains an arbitrary sequence of
bytes. Its content is not interpreted by the platform.

A workspace's resources are organized into a tree structure, with projects at
the top, and folders and files underneath. A special resource, the workspace
root resource, serves as the root of the resource tree. The workspace root is
created internally when a workspace is created and exists as long as the
workspace exists.

A workspace can have any number of projects, each of which can be stored in a
different location in some file system.

The workspace resource namespace is always case-sensitive and case-preserving.
Thus the workspace allows multiple sibling resources to exist with names that
differ only in case. The workspace also doesn't put any restrictions on valid
characters in resource names, the length of resource names, or the size of
resources on disk. Of course, if you store resources in a file system that is
not case-sensitive, or that does have restrictions on resource names, then
those restrictions will show through when you actually try to create and
modify resources.

[Return to Help index](http://nokia.github.io/RED/help/)
