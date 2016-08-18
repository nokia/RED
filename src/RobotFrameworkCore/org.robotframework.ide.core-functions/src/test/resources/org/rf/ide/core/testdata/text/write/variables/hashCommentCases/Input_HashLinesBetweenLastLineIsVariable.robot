*** Variable ***
${var}    1
${var1}    text with space
${path}  C:\new\file.txt
${ref}    /ref/ref/file.jpg    #image
${a}    ${a}
${B}

#list
@{list}  a1    a2    a3    a4

#dict
&{newDict}    music=rock    tree=birch-tree  vege=tomato  #some stupid dict
	

${plus_one}  ${a+1}
@{EMPTY LIST}    ${EMPTY}    ${SPACE}

${this is param}=  8    
@{list with equals sign}=    item1  item2  ${b}    

    value to empty param name    #I forgot to add param name  
    
    
@{multiline}    line1
...  line2
...  line3    
    
${very long param name with vary long value and commnet}    This is very long param value with very long param name. Some part of it should be visible  # this is long comment too. But not some much because I am lazy      

${xyz}    6
*** Test Cases ***