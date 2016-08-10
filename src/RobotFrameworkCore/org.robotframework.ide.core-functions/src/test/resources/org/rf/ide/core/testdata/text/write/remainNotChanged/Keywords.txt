*** Keywords ***
Key1
	[Arguments]	${x}	${y}
	[Documentation]		doc
	[Timeout]		20 hour		ok		#job done
	[Tags]	no way		play
	Log	ok						ok
 |	|	|	Log		ok
	[Teardown]	done	really	#ok
	[Unknow]		done1					done
	[Return]	None
	

	

	
	
	
	
	
Key2
	[Arguments]		${x}	${y}
	[Documentation]		doc
	[Timeout]		20 hour		ok		#job done
	[Tags]		no way		play
	Log		ok						ok
	Log	ok
   Send POM
   ...   cId:${cId}
   ...   uId:${uId}
   ...   ci:${ci}
   ...   o:0    # Eddd
   ...   u:${ul2}
   ...   u:${u2}

   ${msg}=	Get Pq
   ...   lId:1
   ...   uex:${uIndex}
   ...   nn:2
   ...   s[0].sCl:${lll1}
   ...   s[0].sCl.container.u.ca.hal:1
   ...   ser[0].sCex:2
   ...   sern[0].uel1}
   ...   se{ln2}
   ...   se:1
   ...   s:1
   ...   s
	${err}=	${out}=	Execute SSH
	...	p
	...	c   
	[Teardown]	done	really	#ok
	[Unknow]		done1					done
	[Return]	None
	

*** Keywords ***




	
	

Key2	[Arguments]	${x}	${y}
	[Documentation]
	[Timeout]	20 hour		ok		#job done
	[Tags]		no way		play
	Log		ok						ok
	Log		\	ok
	Send PC_CPUdata
    ...  num2:0
    #...  numLost:${num1}
    #...  numLost2:${num2}
	...	numCfg:0		
	[Teardown]	done	really	#ok
	[Unknow]		done1					done
	[Return]	None
	

