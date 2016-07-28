'''
* Copyright 2015 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.

'''

__author__ = 'wypych'

import logging
import platform
import getpass
import getopt
import sys
import os
import re
import xml.etree.ElementTree as ET
from sets import ImmutableSet


class RedRelease(object):
    def __init__(self):
        self.myLogger = MyLogger()

    def usage(self):
        print 'Usage: \n'
        print '\t-s, --start-dir newVersion=<version_to_set> dir=<path> \t\t\tstart directory to search for update poms, META-INF/MANIFEST.MF, feature.xml and category.xml\'s'
        print '\tExample:\n'
        print '\t\t\tRedReleaseVersionUpdater.py --start-dir newVersion=0.4.0 dir=/home/svn/Eclipse-IDE\n'

    def check_python_version(self):
        python_ver = sys.version_info
        if sys.version_info >= (2, 7, 0) and sys.version_info < (3, 0):
            self.myLogger.logInfo('Python version ' + str(python_ver) + ' is OK.')
        else:
            self.myLogger.logError('Python version is not required version between 2.7.0 and 3.0')
            exit(ExitCode.EXIT_CODE_WRONG_PYTHON_VERSION)

    def get_expected_parsed_arguments(self):
        options = None
        arguments = None
        cli_args = sys.argv[1:]
        if len(cli_args) < 1:
            self.usage()
            exit(ExitCode.EXIT_CODE_MISSING_ARGUMENTS)

        try:
            options, arguments = getopt.getopt(cli_args, shortopts='s', longopts=['start-dir'])
        except getopt.GetoptError, e:
            self.usage()
            exit(ExitCode.EXIT_CODE_INCORRECT_ARGUMENTS)

        start_dir = None
        newVersion = None
        for argument in arguments:
            if (argument.startswith('dir=')):
                start_dir = argument.replace('dir=', '')
            elif argument.startswith('newVersion='):
                newVersion = argument.replace('newVersion=', '')
            else:
                self.myLogger.logError('Incorrect argument ' + str(argument))
                exit(ExitCode.EXIT_CODE_INCORRECT_ARGUMENTS)

        if start_dir is None or newVersion is None:
            self.myLogger.logError('Missing argument dir= or newVersion=')
            exit(ExitCode.EXIT_CODE_MISSING_ARGUMENTS)

        return start_dir, newVersion

    def update_feature_xml(self, files_to_update, newVersion):
        for redItem in files_to_update:
            feature_xml_file = redItem.feature_xml_file
            if feature_xml_file is not None:
                self.myLogger.logInfo('Updating feature.xml ' + str(feature_xml_file))
                root = ET.parse(feature_xml_file)
                xml_namespace = ''
                xml_namespace_found = re.search(re.compile('{.*}'), str(root.getroot().tag))
                if xml_namespace_found is not None:
                    xml_namespace = xml_namespace_found.group(0) + ':'
                sthDone = False
                ids = root.findall('.//' + xml_namespace + '*[@id]')
                root_ids = root.findall('[@id]')
                for rids in root_ids:
                    ids.append(rids)
                for elem_id in range(0, len(ids)):
                    elem = ids[elem_id]
                    attrib = elem.attrib
                    if attrib['id'].startswith('org.robotframework.ide.eclipse'):
                        sthDone = True
                        old_version = None
                        if 'version' in attrib:
                            old_version = attrib['version']
                            if old_version != '0.0.0':
                                attrib['version'] = newVersion + '.qualifier'
                            else:
                                old_version = None
                        if 'url' in attrib and old_version is not None:
                            attrib['url'] = attrib['url'].replace(old_version, newVersion + '.qualifier')

                if sthDone:
                    wasWriteSuccessfully = False
                    try:
                        newFile = open(feature_xml_file + '_temp', 'w')
                        root.write(newFile, encoding='utf-8', xml_declaration=True, method='xml',
                                   default_namespace=xml_namespace.replace('{', '', 1).replace('}', '', 1))
                        newFile.close()
                        wasWriteSuccessfully = True
                    except Exception, e:
                        self.myLogger.logError(str(e))
                    if wasWriteSuccessfully:
                        os.remove(feature_xml_file)
                        os.rename(feature_xml_file + '_temp', feature_xml_file)

    def update_red_product(self, files_to_update, newVersion):
        for redItem in files_to_update:
            product_xml_file = redItem.product_xml_file
            if product_xml_file is not None:
                self.myLogger.logInfo('Updating product file ' + str(product_xml_file))
                root = ET.parse(product_xml_file)
                xml_namespace = ''
                xml_namespace_found = re.search(re.compile('{.*}'), str(root.getroot().tag))
                if xml_namespace_found is not None:
                    xml_namespace = xml_namespace_found.group(0) + ':'
                sthDone = False
                ids = root.findall('.//' + xml_namespace + '*[@id]', root)
                for elem_id in range(0, len(ids)):
                    elem = ids[elem_id]
                    attrib = elem.attrib
                    if attrib['id'].startswith('org.robotframework.ide.eclipse'):
                        sthDone = True
                        old_version = None
                        if 'version' in attrib:
                            old_version = attrib['version']
                            attrib['version'] = newVersion + '.qualifier'
                        if 'url' in attrib and old_version is not None:
                            attrib['url'] = attrib['url'].replace(old_version, newVersion + '.qualifier')

                if 'version' in root.getroot().attrib:
                    sthDone = True
                    root.getroot().attrib['version'] = newVersion + '.qualifier'

                if sthDone:
                    wasWriteSuccessfully = False
                    try:
                        newFile = open(product_xml_file + '_temp', 'w')
                        root.write(newFile, encoding='utf-8', xml_declaration=True, method='xml',
                                   default_namespace=xml_namespace.replace('{', '', 1).replace('}', '', 1))
                        newFile.close()
                        wasWriteSuccessfully = True
                    except Exception, e:
                        self.myLogger.logError(str(e))
                    if wasWriteSuccessfully:
                        os.remove(product_xml_file)
                        os.rename(product_xml_file + '_temp', product_xml_file)

    def update_category_xml(self, files_to_update, newVersion):
        for redItem in files_to_update:
            category_xml_file = redItem.category_xml_file
            if category_xml_file is not None:
                self.myLogger.logInfo('Updating category.xml ' + str(category_xml_file))
                root = ET.parse(category_xml_file)
                xml_namespace = ''
                xml_namespace_found = re.search(re.compile('{.*}'), str(root.getroot().tag))
                if xml_namespace_found is not None:
                    xml_namespace = xml_namespace_found.group(0) + ':'
                sthDone = False
                ids = root.findall('.//' + xml_namespace + '*[@id]', root)
                for elem_id in range(0, len(ids)):
                    elem = ids[elem_id]
                    attrib = elem.attrib
                    if attrib['id'].startswith('org.robotframework.ide.eclipse'):
                        sthDone = True
                        old_version = None
                        if 'version' in attrib:
                            old_version = attrib['version']
                            attrib['version'] = newVersion + '.qualifier'
                        if 'url' in attrib and old_version is not None:
                            attrib['url'] = attrib['url'].replace(old_version, newVersion + '.qualifier')

                if sthDone:
                    wasWriteSuccessfully = False
                    try:
                        newFile = open(category_xml_file + '_temp', 'w')
                        root.write(newFile, encoding='utf-8', xml_declaration=True, method='xml',
                                   default_namespace=xml_namespace.replace('{', '', 1).replace('}', '', 1))
                        newFile.close()
                        wasWriteSuccessfully = True
                    except Exception, e:
                        self.myLogger.logError(str(e))
                    if wasWriteSuccessfully:
                        os.remove(category_xml_file)
                        os.rename(category_xml_file + '_temp', category_xml_file)

    def get_list_of_files(self, start_dir):
        files_to_update = []
        for d in os.listdir(start_dir):
            current_dir = start_dir + os.path.sep + d
            pom_file = current_dir + os.path.sep + 'pom.xml'
            item = RedItem(pom_file)
            if (os.path.exists(pom_file)):
                root = ET.parse(pom_file).getroot()
                pattern = re.compile('{http://maven.apache.org/POM/.*}project')
                if (re.match(pattern, str(root.tag))) is not None:
                    pom_namespace = re.search(re.compile('{http://maven.apache.org/POM/.*}'), str(root.tag)).group(0)
                    for r in root.findall(pom_namespace + 'artifactId'):
                        if str(r.text).startswith('org.robotframework.ide.eclipse'):
                            files_to_update.append(item)
                        if (os.path.exists(current_dir + os.path.sep + 'META-INF' + os.path.sep + 'MANIFEST.MF')):
                            item.addManifestMF(current_dir + os.path.sep + 'META-INF' + os.path.sep + 'MANIFEST.MF')
                        if (os.path.exists(current_dir + os.path.sep + 'feature.xml')):
                            item.addFeatureXML(current_dir + os.path.sep + 'feature.xml')
                        if (os.path.exists(current_dir + os.path.sep + 'category.xml')):
                            item.addCategoryXML(current_dir + os.path.sep + 'category.xml')
                        if (os.path.exists(current_dir + os.path.sep + 'RED.product')):
                            item.addProductFile(current_dir + os.path.sep + 'RED.product')

        return files_to_update

    ''' files_to_update: RedItems '''

    def update_manifest_mf(self, files_to_update, newVersion):
        for redItem in files_to_update:
            manifest_mf_file = redItem.manifest_mf_file
            if manifest_mf_file is not None:
                self.myLogger.logInfo('Updating manifest file ' + str(manifest_mf_file))
                md = ManifestDescriptor.build(manifest_mf_file)
                new_mf_file = open(manifest_mf_file + '_temp', mode='w')
                items_to_update_in_manifest = ImmutableSet(self.get_update(md))
                if len(items_to_update_in_manifest) > 0:
                    for manifest_line in md.lines:
                        if manifest_line.property_type in items_to_update_in_manifest:
                            self.myLogger.logInfo('===> Changing [' + str(manifest_line.property_type()) + '] ')
                            if manifest_line.property_type == ManifestProperty.BUNDLE_VERSION:
                                if len(manifest_line.property_sub_descriptor) > 0:
                                    property = manifest_line.property_sub_descriptor[0]
                                    if property.name is not None:
                                        property.name = newVersion + '.qualifier\n'
                            elif manifest_line.property_type == ManifestProperty.FRAGMENT_HOST:
                                if len(manifest_line.property_sub_descriptor) > 0:
                                    property = manifest_line.property_sub_descriptor[0]
                                    if 'org.robotframework.ide.eclipse.main' in property.name:
                                        property.parameters['bundle-version'] = ['\"' + newVersion + '\"']
                            elif manifest_line.property_type == ManifestProperty.REQUIRED_BUNDLES:
                                for property in manifest_line.property_sub_descriptor:
                                    if 'org.robotframework.ide.eclipse.main' in property.name:
                                        if 'bundle-version' in property.parameters:
                                            property.parameters['bundle-version'] = ['\"' + newVersion + '\"']
                    self.myLogger.logInfo('Dumping manifest file ' + str(manifest_mf_file))
                    for manifest_line_id in range(0, len(md.lines)):
                        addNewLine = True
                        manifest_line = md.lines[manifest_line_id]
                        if manifest_line.property_type == ManifestProperty.UNKNOWN:
                            if manifest_line.original_line.endswith('\n'):
                                addNewLine = False
                            new_mf_file.write(manifest_line.original_line)
                            continue
                        else:
                            if manifest_line.property_name is not None:
                                new_mf_file.write(manifest_line.property_name + ':')
                            for index in range(0, len(manifest_line.property_sub_descriptor)):
                                property = manifest_line.property_sub_descriptor[index]
                                if index == 0:
                                    if not (property.name.startswith(' ') or property.name.startswith('\t')):
                                        new_mf_file.write(' ')
                                new_mf_file.write(property.name)
                                if property.name.endswith('\n'):
                                    addNewLine = False
                                if len(property.parameters) > 0:
                                    new_mf_file.write(';')
                                for paramIndex in range(0, len(property.parameters.keys())):
                                    paramKey = property.parameters.keys()[paramIndex]
                                    new_mf_file.write(paramKey + '=')
                                    key_values = property.parameters[paramKey]
                                    for value_index in range(0, len(key_values)):
                                        new_mf_file.write(key_values[value_index])
                                        if key_values[value_index].endswith('\n'):
                                            addNewLine = False
                                        if value_index + 1 < len(key_values):
                                            new_mf_file.write('=')
                        if manifest_line_id + 1 < len(md.lines):
                            next_line = md.lines[manifest_line_id + 1]
                            if next_line.property_type == manifest_line.property_type and next_line.property_name is None:
                                new_mf_file.write(',')
                        if addNewLine:
                            new_mf_file.write('\n')
                new_mf_file.close()
                os.remove(manifest_mf_file)
                os.rename(new_mf_file.name, manifest_mf_file)

    def get_update(self, manifest_desc):
        result = []
        for md_line in manifest_desc.lines:
            if md_line.property_type == ManifestProperty.BUNDLE_SYMBOLIC_NAME:
                for property_desc in md_line.property_sub_descriptor:
                    if 'org.robotframework.ide.eclipse' in property_desc.name:
                        result.append(ManifestProperty.BUNDLE_VERSION)
            if md_line.property_type == ManifestProperty.FRAGMENT_HOST:
                for property_desc in md_line.property_sub_descriptor:
                    if 'org.robotframework.ide.eclipse' in property_desc.name:
                        result.append(ManifestProperty.FRAGMENT_HOST)
            if md_line.property_type == ManifestProperty.REQUIRED_BUNDLES:
                for property_desc in md_line.property_sub_descriptor:
                    if 'org.robotframework.ide.eclipse' in property_desc.name:
                        result.append(ManifestProperty.REQUIRED_BUNDLES)
        return result

    ''' files_to_update: RedItems '''

    def update_poms(self, files_to_update, newVersion):
        for redItem in files_to_update:
            pom_file = redItem.pom_file
            self.myLogger.logInfo('Updating pom ' + str(pom_file))
            root = ET.parse(pom_file)
            pom_namespace = re.search(re.compile('{.*}'), str(root.getroot().tag)).group(0)

            # project version update
            project_version = root.find('' + pom_namespace + 'version', root)
            if project_version is not None:
                self.myLogger.logInfo('Update version of project')
                project_version.text = newVersion + '-SNAPSHOT'

            # update parent
            parent_artifactId = root.find('.//' + pom_namespace + 'parent/' + pom_namespace + 'artifactId', root)
            parent_version = root.find('.//' + pom_namespace + 'parent/' + pom_namespace + 'version', root)
            if parent_artifactId is not None and parent_version is not None:
                if parent_artifactId.text.startswith('org.robotframework.ide.eclipse'):
                    self.myLogger.logInfo('Update version of parent project')
                    parent_version.text = newVersion + '-SNAPSHOT'

            # update dependencies
            dependencies = root.findall('.' + pom_namespace + 'dependencies/' + pom_namespace + 'dependency')
            for dep in dependencies:
                dep_artId = dep.find('' + pom_namespace + 'artifactId')
                if dep_artId is not None:
                    if dep_artId.text.startswith('org.robotframework.ide.eclipse'):
                        dep_version = dep.find('' + pom_namespace + 'version')
                        if dep_version is not None:
                            self.myLogger.logInfo('Update version of dependency ' + dep_artId.text)
                            dep_version.text = newVersion + '-SNAPSHOT'
                     
            # update target configuration (for eclipse parent pom)       
            target = root.find('.//' + pom_namespace + 'target/' + pom_namespace + 'artifact')
            if target is not None:
                target_artId = target.find(pom_namespace + 'artifactId')
                if target_artId is not None and target_artId.text.startswith('org.robotframework.ide.eclipse'):
                    target_version = target.find(pom_namespace + 'version')
                    if target_version is not None:
                        self.myLogger.logInfo('Update version of dependency ' + target_artId.text)
                        target_version.text = newVersion + '-SNAPSHOT'

            wasWriteSuccessfully = False
            try:
                newFile = open(pom_file + '_temp', 'w')
                root.write(newFile, encoding='utf-8', xml_declaration=True, method='xml',
                           default_namespace=pom_namespace.replace('{', '', 1).replace('}', '', 1))
                newFile.close()
                wasWriteSuccessfully = True
            except Exception, e:
                self.myLogger.logError(str(e))
            if wasWriteSuccessfully:
                os.remove(pom_file)
                os.rename(pom_file + '_temp', pom_file)


class ManifestDescriptor(object):
    def __init__(self, manifest_mf_file):
        self.manifest_mf_file = manifest_mf_file
        self.lines = []

    @staticmethod
    def build(manifest_mf_file):
        md = ManifestDescriptor(manifest_mf_file)

        pattern_property_name = '(^((?!:(\\s)).)+:)'
        pattern_property_value_at_line = '((\\s|$)((?!:).)*)'
        pattern_manifest_property = re.compile(pattern_property_name + pattern_property_value_at_line)
        md_file = open(manifest_mf_file, mode='r')
        lastPropertyType = ManifestProperty.UNKNOWN
        for line in md_file.readlines():
            property_match = pattern_manifest_property.match(line)
            part_parameter = ''
            ml = None
            if property_match is not None:
                property_name = property_match.group(1).replace(':', '')
                part_parameter = line.replace(property_match.group(1), '')
                if part_parameter is None:
                    part_parameter = ''
                lastPropertyType = ManifestProperty.build(property_name)
                ml = ManifestLine(line, lastPropertyType, property_name)
            else:
                ml = ManifestLine(line, lastPropertyType, None)
                part_parameter = line

            if len(part_parameter) > 0:
                if part_parameter.startswith(' ') or part_parameter.startswith('\t'):
                    ml.property_sub_descriptor = PropertySubDescriptor.build(part_parameter)
            md.lines.append(ml)
        md_file.close()

        return md


class ManifestProperty(object):
    @staticmethod
    def UNKNOWN():
        return ''

    @staticmethod
    def BUNDLE_SYMBOLIC_NAME():
        return 'Bundle-SymbolicName'

    @staticmethod
    def BUNDLE_VERSION():
        return 'Bundle-Version'

    @staticmethod
    def FRAGMENT_HOST():
        return 'Fragment-Host'

    @staticmethod
    def REQUIRED_BUNDLES():
        return 'Require-Bundle'

    @staticmethod
    def build(text):
        return {str(ManifestProperty.BUNDLE_SYMBOLIC_NAME()): ManifestProperty.BUNDLE_SYMBOLIC_NAME,
                str(
                        ManifestProperty.BUNDLE_VERSION()): ManifestProperty.BUNDLE_VERSION,
                str(
                        ManifestProperty.FRAGMENT_HOST()): ManifestProperty.FRAGMENT_HOST,
                str(
                        ManifestProperty.REQUIRED_BUNDLES()): ManifestProperty.REQUIRED_BUNDLES} \
            .get(text, ManifestProperty.UNKNOWN)


class ManifestLine(object):
    def __init__(self, original_line, property_type, property_name=None):
        self.original_line = original_line
        self.property_name = property_name
        self.property_type = property_type
        self.property_sub_descriptor = []

    def __str__(self):
        return 'originalLine=' + self.original_line.replace('\n', '') + ' , property_name=' + str(
                self.property_name) + ' , property_type=' + str(self.property_type) + ', property=' + str(
                self.property_sub_descriptor)


class PropertySubDescriptor(object):
    def __init__(self, name=None):
        self.name = name
        self.parameters = {}

    def __str__(self):
        return 'name=' + self.name + ', parameters=' + str(self.parameters)

    @staticmethod
    def build(text):
        p = list()
        elements = text.split(',')
        for e in elements:
            if e != '\r' and e != '\n':
                newP = PropertySubDescriptor()
                p.append(newP)
                for c in e.split(';'):
                    if newP.name is None:
                        newP.name = c
                    else:
                        keyValueParam = c.split('=')
                        key = keyValueParam[0]
                        if len(keyValueParam) >= 1:
                            newP.parameters[key] = []
                        for i in range(1, len(keyValueParam)):
                            newP.parameters[key].append(keyValueParam[i])
        return p


class RedItem(object):
    def __init__(self, pom_file):
        self.pom_file = pom_file
        self.manifest_mf_file = None
        self.feature_xml_file = None
        self.category_xml_file = None
        self.product_xml_file = None

    def addManifestMF(self, manifest_mf_file):
        self.manifest_mf_file = manifest_mf_file

    def addFeatureXML(self, feature_xml_file):
        self.feature_xml_file = feature_xml_file

    def addCategoryXML(self, category_xml_file):
        self.category_xml_file = category_xml_file

    def addProductFile(self, product_xml_file):
        self.product_xml_file = product_xml_file

    def __str__(self):
        return 'RedItem [pom=' + str(self.pom_file) + ', manifest.mf=' + str(
                self.manifest_mf_file) + ', feature.xml=' + str(self.feature_xml_file) + ', category.xml=' + str(
                self.category_xml_file) + ', *.product=' + str(self.product_xml_file) + ']'


class ExitCode(object):
    EXIT_CODE_MISSING_ARGUMENTS = 1
    EXIT_CODE_INCORRECT_ARGUMENTS = 2
    EXIT_CODE_WRONG_PYTHON_VERSION = 3


class MyLogger(object):
    def __init__(self):
        self.logger = self._create_logger('RedVersionUpdater')

    def _create_logger(self, loggerName, filename=None):
        LOG_FORMAT = '%(asctime)-15s %(node_name)s %(user)-8s %(levelname)-8s %(message)s'
        log_format = logging.Formatter(LOG_FORMAT)
        std_out_logger = logging.StreamHandler(sys.stdout)
        std_out_logger.setFormatter(log_format)
        logger = logging.getLogger(loggerName)
        if filename is not None and filename is not '':
            file_logger = logging.FileHandler(filename=filename)
            file_logger.setFormatter(log_format)
            logger.addHandler(file_logger)

        logger.extra = {'node_name': platform.node(), 'user': getpass.getuser()}
        logger.addHandler(std_out_logger)
        logger.setLevel(logging.INFO)
        return logger

    def logInfo(self, msg):
        self.logger.info(msg=msg, extra=self.logger.extra)

    def logError(self, msg):
        self.logger.error(msg=msg, extra=self.logger.extra)

    def logWarn(self, msg):
        self.logger.warn(msg=msg, extra=self.logger.extra)

    def logDebug(self, msg):
        self.logger.debug(msg=msg, extra=self.logger.extra)


if __name__ == '__main__':
    redUpdater = RedRelease()
    redUpdater.check_python_version()
    start_dir, newVersion = redUpdater.get_expected_parsed_arguments()
    filesToUpdate = redUpdater.get_list_of_files(start_dir)
    redUpdater.update_poms(filesToUpdate, newVersion)
    redUpdater.update_manifest_mf(filesToUpdate, newVersion)
    redUpdater.update_category_xml(filesToUpdate, newVersion)
    redUpdater.update_red_product(filesToUpdate, newVersion)
    redUpdater.update_feature_xml(filesToUpdate, newVersion)
