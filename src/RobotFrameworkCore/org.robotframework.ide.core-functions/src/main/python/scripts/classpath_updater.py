from java.io import File
from java.lang import ClassLoader
from java.lang.reflect import Method
from java.net import URL
from java.net import URLClassLoader
from java.lang import System
from java.lang import Thread

sysloader = None

def get_java_version():
    # java version is reported as 1.X.___ for Java < 9 and as X.___ otherwise
    version_string = System.getProperty('java.version')
    version_splitted = version_string.split('.')
    main_version_index = 1 if version_splitted[0] == '1' else 0
    return int(version_splitted[main_version_index])


class ClassPathUpdater :
##########################################################
# Purpose: Allow runtime additions of new Class/jars either from local files or URL. Adapted for Java9+
######################################################

    def add_file(self, path):
        # make a URL out of 'path'
        return self.add_url(File(path).toURL())

    def add_url(self, url):
        return self.add_url_pre9(url) if get_java_version() < 9 else self.add_url_post9(url)
    
    def add_url_pre9(self, url):
        return self.add_url_with_url_classloader(ClassLoader.getSystemClassLoader(), url)
    
    def add_url_post9(self, url):
        # in Java 9+ the system class loader is no longer implementing URLClassLoader, so this
        # trick with added url's does not work anymore for dynamic classes loading, so we define
        # our own class loader and set it as context class loader for current thread
        global sysloader
        if not sysloader:
            sysloader = Java9ClassLoader()
            Thread.currentThread().setContextClassLoader(sysloader);
        return self.add_url_with_url_classloader(sysloader, url)
    
    def add_url_with_url_classloader(self, classloader, url):
        method = URLClassLoader.getDeclaredMethod("addURL", [URL])
        method.setAccessible(1)
        method.invoke(classloader, [url])
        return url

    
class Java9ClassLoader(URLClassLoader):
    
    def __init__(self):
        URLClassLoader.__init__(self, [], ClassLoader.getSystemClassLoader())        