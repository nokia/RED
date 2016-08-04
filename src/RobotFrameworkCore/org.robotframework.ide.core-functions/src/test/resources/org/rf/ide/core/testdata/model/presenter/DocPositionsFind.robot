*** Settings ***
Documentation	foobar1	foobar2	foobar3	foobar4
...	newFoobar1
...	newFoobar2
...	newFoobar3
...
...	newFoobar4

*** Keywords ***
KW1
	[Documentation]	doc1
	...	doc2	doc3
	Log	OK_1
	[Return]	NEW

KW2
	[Documentation]	doc32
	...	doc22	doc32
	Log	OK_12
	[Return]	NEW2

*** Test Cases ***
TC1
	[Documentation]	doc1
	...	doc2	doc3
	Log	OK_1
	[Teardown]	NEW

TC2
	[Documentation]	doc32
	...	doc22	doc32
	Log	OK_12
	[Teardown]	NEW2

