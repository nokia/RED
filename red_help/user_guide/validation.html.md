## Validation

### General information

Validation is a mechanism to read and parse Robot test cases in order to
provide feedback about Robot defined spell check. Validation mechanism walk
down the project tree and read all test files format supported by RED (.robot,
.txt and .tsv), mark all unknown/undefined keywords, variable misuse, missing
resources etc.  

### Validation execution

Validation mechanism is executed any time when edited file is changed with
slight delay while **Build Automatic ** option is selected from Project. Whole
project validation can be manually started using option **Project -> Clean**.  

The file which currently edited within Suite Editor is constantly validated in
background giving quick feedback about potential problems.

