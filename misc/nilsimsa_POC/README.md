
## locality-sensitive hashing algorithm & Robot Framework
##About
Proof-Of-Concept with using locality-sensitive hashing algorithm to find TCs and KWs which are similar to each other.
This may indicate that some parts are the same (by copy/paste) or similar (same logic with different variables) which is valuable input for test refactoring and simplification.

##Script
###Usage
nilsimsa_POC.py <file or directory with RF tests/resources> 
nilsimsa_POC.py -h for more options
###Dependencies
Script uses parser from Robot Framework Lint [https://github.com/boakley/robotframework-lint] and nilsimsa implementation [https://github.com/diffeo/py-nilsimsa]
###Logic
Script parses every TC and KW from input files/folders, stores only non-commented TC/KW lines.
For each item, nilsimsa digest is computed and compared in pairs with rest of the items.
Result file consist similarity metric (-128 to 128, higher->more similar) for compared pairs sorted descending  
###Computational complexity
number of hash computations=N-elements

number of hash comparisions= N*(N-1)/2
## Docs
https://en.wikipedia.org/wiki/Nilsimsa_Hash

https://github.com/boakley/robotframework-lint

https://github.com/diffeo/py-nilsimsa
