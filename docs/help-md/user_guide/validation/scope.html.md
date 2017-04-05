## Limiting validation scope

When working with bigger projects, it is good to limit validation scope to
folders relevant to current work to speed up validation and reduce test error
markers to be relevant to files under scope.

Excluded folders are still parsed so code completion is preserved, just
validation is omitted thus validation errors and warnings are skipped for
excluded folders.

Include/exclude can be triggered in Project Explorer under right click menu on
selected folders:

![](images/exclude_1.png)

Include/exclude can also be found in red.xml editor under Validation table -
include/exclude action is available as right click menu.

In case of big amounts of .txt or .tsv files which are not Robot test cases,
validation can be excluded on files bigger than size threshold.

![](images/exclude_2.png)

Note that exclude folders are grayed out, exclude state is inherited from
parent folders.

