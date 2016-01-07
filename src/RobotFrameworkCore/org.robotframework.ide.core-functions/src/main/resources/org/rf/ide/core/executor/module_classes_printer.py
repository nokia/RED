import sys
import os.path
import inspect
import pkgutil
import imp

'''
This script prints all the classes contained in given module and submodules rooted in given path. 
In case of package modules the path has to point to __init__.py file of this module.
'''
original_path = sys.argv[1]
if original_path.startswith('"') and original_path.endswith('"'):
    original_path = original_path[1:-1]

modules = []
parent = os.path.dirname(original_path)
path = parent
if original_path.endswith('__init__.py'):
    module_name = os.path.basename(path)
    parent = os.path.dirname(path)
    sys.path.append(parent)
    
    module_file, module_path, module_desc = imp.find_module(module_name)
    root_module = imp.load_module(module_name, module_file, module_path, module_desc)
    
    modules = [root_module]
    for loader, name, _ in pkgutil.walk_packages([path]):
        modules.append(loader.find_module(name).load_module(name))

elif original_path.endswith('.py'):
    module_name = os.path.basename(original_path)[:-3]
    sys.path.append(path)
        
    module_file, module_path, module_desc = imp.find_module(module_name)
    root_module = imp.load_module(module_name, module_file, module_path, module_desc)
    
    modules = [root_module]

else:
    raise Exception('Unrecognized library path: ' + original_path)

for module in modules:
    for n, obj in inspect.getmembers(module):
        if inspect.isclass(obj) and obj.__module__.startswith(module_name):
            print(obj.__module__ + '.' + obj.__name__)

