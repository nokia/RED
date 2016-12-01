*** Settings ***
Resource	res.robot
Resource	res2.robot
Resource	res.data.robot
Library	Lib.py
Library	LibA.py	WITH NAME	dataAccessLayer

*** Test Cases ***
test
	Given And total fee is 'nie'
	And total fee is '10.00'
	and value is 10.00
	value is 10.00
	test.txt
	test
	res.test.txt
	res.test
	res.data.Put
	And res.test.txt
	res.data.nonDot
	Lib.opa.opa_hop
	dataAccessLayer.opa.opa_hop
	KeyNested
	
*** Keywords ***
total fee is '${total_fee}'
	Set Test Variable  ${total_fee}   ${total_fee}
	
And value is ${c}
	BuiltIn.Should Be Equal	10.00	${c}
value is ${c}
	Log	${c}
#res.test
#	log  haha ok
res.test.txt
	Log	hahaha res.test.txt
	
test.txt
	Log	txt	
res.data
	Log	ok res.data
	
res.data.nonDot
	Log	jupi here