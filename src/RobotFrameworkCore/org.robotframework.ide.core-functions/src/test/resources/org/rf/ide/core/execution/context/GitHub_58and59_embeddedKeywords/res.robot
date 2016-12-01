*** Settings ***
Resource	res_nested.robot
*** Keywords ***
test.txt
	Log	txt_res	

test
	log	doNow
	test.txt

And total fee is '${total_fee}'
	Log  here
	Set Test Variable  ${total_fee}   ${total_fee}