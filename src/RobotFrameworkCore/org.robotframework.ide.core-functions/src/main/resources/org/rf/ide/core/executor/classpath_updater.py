class ClassPathUpdater :
##########################################################
# from http://forum.java.sun.com/thread.jspa?threadID=300557
#
# Author: SG Langer Jan 2007 translated the above Java to this
#       Jython class
# Purpose: Allow runtime additions of new Class/jars either from
#       local files or URL
######################################################
    import java.lang.reflect.Method
    import java.io.File
    import java.net.URL
    import java.net.URLClassLoader

    def add_file(self, path):
        # make a URL out of 'path'
        file_path = self.java.io.File(path)
        return self.add_url(file_path.toURL())

    def add_url(self, url):
        sysloader =  self.java.lang.ClassLoader.getSystemClassLoader()
        sysclass = self.java.net.URLClassLoader
        method = sysclass.getDeclaredMethod("addURL", [self.java.net.URL])
        method.setAccessible(1)
        method.invoke(sysloader, [url])
        return url
    