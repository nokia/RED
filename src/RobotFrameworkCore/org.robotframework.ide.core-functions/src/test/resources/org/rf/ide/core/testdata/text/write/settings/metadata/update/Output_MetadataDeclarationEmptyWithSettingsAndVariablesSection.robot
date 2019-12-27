*** Settings ***

Default Tags
Force Tags	t3	t2			#a
Suite Setup
Test Setup    xyz
Test Setup	z    123
Test Template	tem
Test Timeout	6

Metadata	key	value
Library    PLib.MFile
Library    PLib/cd/ab.py
Variables    var1.py  2  5  3    #comm
Library    d2.ab    2    # comment1 a ksdj laksdj laksd jalk djlak jdlak dj
Library	OperatingSystem
Resource	res.robot
Variables	var.py
*** Test Cases ***
T1
    Log    d1
    #ok_b
    ab_k
    Ab K

*** Variables ***
${var}  2

    
    