import re
import sys
import os

if len(sys.argv)>2:
    pattern = "^\d{8} (\d\d:){2}\d\d.\d{3}"
    path_to_log = sys.argv[1]
    path_to_ref = sys.argv[2]

    if os.path.exists(path_to_log) and os.path.exists(path_to_ref):
       
        with open(path_to_log) as ref_file:
            ref_content = ref_file.read()
        with open(path_to_ref) as org_file:
            org_content = org_file.read()
        ref_content =  re.sub( pattern, "", ref_content,flags=re.MULTILINE)
        org_content =  re.sub( pattern, "", org_content,flags=re.MULTILINE)

        
        if org_content == ref_content:
            print "OK!"
        else:
            ref_content = ref_content.split("\n")
            org_content = org_content.split("\n")
         
      
            if len(ref_content)>len(org_content):
                org_content.append("EOF")
            if len(ref_content)<len(org_content):
                ref_content.append("EOF")
          
            for i in range(len(ref_content)):
                if ref_content[i]!=org_content[i]:
                    print "Different line: " + str(i+1)
                    print "Should be:" + ref_content[i]
                    print "But is:" + org_content[i]
                    break
            
    else:
        print "One of file not exists:" + path_to_log + " or:" + path_to_ref
else:
    print "Not enough arguments!"