import sys
import os

if len(sys.argv)>2:
    if os.path.exists(sys.argv[1]) and  os.path.exists(sys.argv[2]):
        status=[]
      
        f=open (sys.argv[1], 'r')
        result_file = f.readlines()
        f.close()


        f=open (sys.argv[2], 'r')
        schema_file = f.readlines()
        f.close()
    
        if result_file and schema_file:
            counter =len(schema_file)

            if len(result_file)>len(schema_file):
                status.append("Result file ({0}) is bigger than schema file ({1}).".format(str(len(result_file)), str(len(schema_file))))
                counter =len(schema_file)
            if len(schema_file)>len(result_file):
                status.append("Schema file ({0}) is bigger than result file ({1}).".format(str(len(schema_file)), str(len(result_file))))
                counter= len(resul_file)

            for i in range (counter):
                if result_file[i]!=schema_file[i]:
                    status.append("First difference is in line {0}.".format(str(i+1)))
                    break
                
            else:
                if status:
                    status.append("First difference is in line {0}.".format(str(counter+1)))
                
                else:
                    status.append("OK")

            print " ".join(status)

    else:
        print "One of path to file does not exist: " + sys.argv[1] + " or: " + sys.argv[2] 
else:
    print "Script requires 2 arguments: path to log and schema."
