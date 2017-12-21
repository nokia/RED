# Copyright 2010 Orbitz WorldWide
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Modified by Mikko Korpela under NSN copyrights
#  Copyright 2008-2012 Nokia Siemens Networks Oyj
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# Ammended by Timothy Alexander <dragonfyre13@gmail.com>
# (StreamHandler class added)
#   Copyright 2013 Timothy Alexander
#   Licensed under the Apache License, Version 2.0
#      http://www.apache.org/licenses/LICENSE-2.0

#
# Modified by Mateusz Marzec under NSN copyrights
# Copyright 2015 Nokia Solutions and Networks
# * Licensed under the Apache License, Version 2.0,
# * see license.txt file for details.
#


'''A Robot Framework listener that sends information to a socket'''

import os
import sys
import socket
import inspect
import copy
import json
import time
import traceback
import platform
import re
try:
    from collections import OrderedDict
except ImportError:
    from ordereddict import OrderedDict
try:
    from collections.abc import Mapping
except:
    from collections import Mapping

# Setting Output encoding to UTF-8 and ignoring the platform specs
import robot.utils.encoding
robot.utils.encoding.OUTPUT_ENCODING = 'UTF-8'
# RF 2.6.3 and RF 2.5.7
robot.utils.encoding._output_encoding = robot.utils.encoding.OUTPUT_ENCODING

from robot.libraries.BuiltIn import BuiltIn
from robot import version
try:
    # RF 2.7.5
    from robot.running import EXECUTION_CONTEXTS


    def _is_logged(level):
        current = EXECUTION_CONTEXTS.current
        if current is None:
            return True
        out = current.output
        if out is None:
            return True
        return out._xmllogger._log_message_is_logged(level)
except ImportError:
    # RF 2.5.6
    # RF 2.6.3
    def _is_logged(level):
        # Needs to be imported in the function as OUTPUT is not a constant
        from robot.output import OUTPUT

        if OUTPUT is None:
            return True
        return OUTPUT._xmllogger._log_message_is_logged(level)

        
def _fix_unicode(max_length, data):
    if sys.version_info < (3, 0, 0) and isinstance(data, unicode):
        return _truncate(max_length, data.encode('utf-8'))
    elif sys.version_info < (3, 0, 0) and isinstance(data, basestring):
        return _truncate(max_length, data.encode('unicode_escape'))
    elif sys.version_info >= (3, 0, 0) and isinstance(data, str):
        return _truncate(max_length, data)
    elif isinstance(data, OrderedDict):
        return OrderedDict((_fix_unicode(max_length, k), _fix_unicode(max_length, data[k])) for k in data)
    elif isinstance(data, Mapping):
        return dict((_fix_unicode(max_length, k), _fix_unicode(max_length, data[k])) for k in data)
    elif isinstance(data, tuple):
        return tuple(list(_fix_unicode(max_length, el) for el in data))
    elif isinstance(data, list):
        return list(_fix_unicode(max_length, el) for el in data)
    elif data is None:
        return _fix_unicode(max_length, 'None')
    else:
        return _fix_unicode(max_length, str(data))

    
def _truncate(max_length, s):
    return s[:max_length] + ' <truncated>' if len(s) > max_length else s


def _label_with_types(data):
    value_type = type(data).__name__
    if isinstance(data, Mapping):
        return (value_type, dict((k, _label_with_types(data[k])) for k in data))
    elif isinstance(data, list):
        return (value_type, list(_label_with_types(el) for el in data))
    elif isinstance(data, tuple):
        return (value_type, tuple(list(_label_with_types(el) for el in data)))
    else:
        return (value_type, data)


class RedResponseMessage:
    
    DO_START = 'do_start'
    OPERATING_MODE = 'operating_mode'
    PROTOCOL_VERSION = 'protocol_version'
    TERMINATE = 'terminate'
    DISCONNECT = 'disconnect'
    CONTINUE = 'continue'
    PAUSE = 'pause'
    RESUME = 'resume'
    EVALUATE_CONDITION = 'evaluate_condition'
    GET_VARIABLES = 'get_variables'
    CHANGE_VARIABLE = 'change_variable'
    
class AgentEventMessage:
    
    AGENT_INITIALIZING = 'agent_initializing'
    READY_TO_START = 'ready_to_start'
    VERSION = 'version'
    START_SUITE = 'start_suite'
    END_SUITE = 'end_suite'
    START_TEST = 'start_test'
    END_TEST = 'end_test'
    PRE_START_KEYWORD = "pre_start_keyword"
    START_KEYWORD = 'start_keyword'
    PRE_END_KEYWORD = "pre_end_keyword"
    END_KEYWORD = 'end_keyword'
    SHOULD_CONTINUE = 'should_continue'
    CONDITION_RESULT = 'condition_result'
    VARIABLES = 'variables'
    PAUSED = 'paused'
    RESUMED = 'resumed'
    RESOURCE_IMPORT = 'resource_import'
    LIBRARY_IMPORT = 'library_import'
    MESSAGE = 'message'
    LOG_MESSAGE = 'log_message'
    LOG_FILE = 'log_file'
    REPORT_FILE = 'report_file'
    OUTPUT_FILE = 'output_file'
    CLOSE = 'close'
    
class AgentMode:
    
    RUN = 'run'
    DEBUG = 'debug'
    
class PausingPoint:
    
    PRE_START_KEYWORD = 'PRE_START_KEYWORD'
    START_KEYWORD = 'START_KEYWORD'
    PRE_END_KEYWORD = 'PRE_END_KEYWORD'
    END_KEYWORD = 'END_KEYWORD'

class TestRunnerAgent:
    """Pass all listener events to a remote listener

    If called with one argument, that argument is a port, localhost is used as host, 30 seconds is a connection timeout
    If called with two, the first is a host, the second is a port, 30 seconds is a connection timeout
    If called with three, the first is a host, the second is a port, the third is a connection timeout
    """
    ROBOT_LISTENER_API_VERSION = 2
    
    CONNECTION_SLEEP_BETWEEN_TRIALS = 2
    
    RED_AGENT_PROTOCOL_VERSION = 2
    
    MAX_VARIABLE_VALUE_TEXT_LENGTH = 2048

    def __init__(self, *args):
        if len(args) == 1:
            host, port, connection_timeout = 'localhost', int(args[0]), 30
        elif len(args) == 2:
            host, port, connection_timeout = args[0], int(args[1]), 30
        else:
            host, port, connection_timeout = args[0], int(args[1]), int(args[2])

        self._last_pause_check = time.time()
        self._is_connected, self.sock, self.decoder_encoder = self._connect(host, port, connection_timeout)
        
        if self._is_connected:
            self._handshake()
            self._built_in = BuiltIn()
        else:
            self._mode = None
        
    def _handshake(self):
        self._send_to_server(AgentEventMessage.AGENT_INITIALIZING)
        self._mode, wait_for_signal = self._receive_operating_mode()
        
        self._send_version()
        _, response = self._wait_for_reponse(RedResponseMessage.PROTOCOL_VERSION)
        is_correct = response[RedResponseMessage.PROTOCOL_VERSION]['is_correct']
        error = response[RedResponseMessage.PROTOCOL_VERSION]['error']
        
        if is_correct and wait_for_signal:
            self._send_to_server(AgentEventMessage.READY_TO_START)
            self._wait_for_reponse(RedResponseMessage.DO_START)
        elif not is_correct:
            self._close_connection()
            self._print_error_message(error +
                 '\nClosing connection. Please use agent script as exported from RED instance you\'re using')
            
    def _receive_operating_mode(self):
        _, response = self._wait_for_reponse(RedResponseMessage.OPERATING_MODE)
        operating_mode = response[RedResponseMessage.OPERATING_MODE]
        return operating_mode['mode'].lower(), operating_mode['wait_for_start_allowance']
        
    def _send_version(self):
        robot_version = 'Robot Framework ' + version.get_full_version()
        info = {'cmd_line': ' '.join(sys.argv), 
                'python' : sys.version, 
                'robot' : robot_version, 
                'protocol' : self.RED_AGENT_PROTOCOL_VERSION}
        self._send_to_server(AgentEventMessage.VERSION, info)

    def start_suite(self, name, attrs):
        attrs_copy = copy.copy(attrs)
        del attrs_copy['doc']
        attrs_copy['is_dir'] = os.path.isdir(attrs['source'])
        
        self._send_to_server(AgentEventMessage.START_SUITE, name, attrs_copy)

    def end_suite(self, name, attrs):
        attrs_copy = copy.copy(attrs)
        del attrs_copy['doc']
        attrs_copy['is_dir'] = os.path.isdir(attrs['source'])
        
        self._send_to_server(AgentEventMessage.END_SUITE, name, attrs_copy)

    def start_test(self, name, attrs):
        attrs_copy = copy.copy(attrs)
        del attrs_copy['doc']
        self._send_to_server(AgentEventMessage.START_TEST, name, attrs_copy)

    def end_test(self, name, attrs):
        attrs_copy = copy.copy(attrs)
        del attrs_copy['doc']
        self._send_to_server(AgentEventMessage.END_TEST, name, attrs_copy)

    def start_keyword(self, name, attrs):
        if not self._is_connected:
            return
        
        # we're cutting args from original attrs dictionary, because it may contain 
        # objects which are not json-serializable and we don't need them anyway
        attrs_copy = copy.copy(attrs)
        del attrs_copy['args']
        del attrs_copy['doc']
        del attrs_copy['assign']
        
        # this is done in order to reuse json encoded objects as they are sent twice in DEBUG mode
        json_obj = self._encode_to_json((name, attrs_copy))
        if self._mode == AgentMode.DEBUG:
            self._send_to_server_json(AgentEventMessage.PRE_START_KEYWORD, json_obj)
            if self._should_pause(PausingPoint.PRE_START_KEYWORD):
                self._wait_for_resume()

        self._send_to_server_json(AgentEventMessage.START_KEYWORD, json_obj)
        if self._should_ask_for_pause_on_start():
            if self._should_pause(PausingPoint.START_KEYWORD):
                self._wait_for_resume()
            
    def _should_ask_for_pause_on_start(self):
        if not self._is_connected:
            return False
        elif self._mode == AgentMode.RUN:
            # in run mode we will check for pause only from time to time (in at least 2 sec intervals)
            current_time = time.time()
            if current_time - self._last_pause_check > 2:
                self._last_pause_check = current_time
                return True
            else:
                return False
        else:
            return True

    def end_keyword(self, name, attrs):
        if not self._is_connected:
            return
        
        attrs_copy = copy.copy(attrs)
        del attrs_copy['args']
        del attrs_copy['doc']
        del attrs_copy['assign']
        
        json_obj = self._encode_to_json((name, attrs_copy))
        if self._mode == AgentMode.DEBUG:
            self._send_to_server_json(AgentEventMessage.PRE_END_KEYWORD, json_obj)
            if self._should_pause(PausingPoint.PRE_END_KEYWORD):
                self._wait_for_resume()
        
        self._send_to_server_json(AgentEventMessage.END_KEYWORD, json_obj)
        if self._should_ask_for_pause_on_end():
            if self._should_pause(PausingPoint.END_KEYWORD):
                self._wait_for_resume()
            
    def _should_ask_for_pause_on_end(self):
        return self._is_connected and self._mode == AgentMode.DEBUG
            
    def _should_pause(self, pausing_point):
        self._send_to_server(AgentEventMessage.SHOULD_CONTINUE, {'pausing_point' : pausing_point})
        
        possible_responses = [
            RedResponseMessage.CONTINUE, 
            RedResponseMessage.PAUSE,
            RedResponseMessage.TERMINATE, 
            RedResponseMessage.DISCONNECT]
        if self._mode == AgentMode.DEBUG and pausing_point in [PausingPoint.PRE_START_KEYWORD, PausingPoint.PRE_END_KEYWORD]:
            possible_responses.append(RedResponseMessage.EVALUATE_CONDITION)
        
        response_name, response = self._wait_for_reponse(*possible_responses)
        while True:
            if response_name == RedResponseMessage.TERMINATE:
                sys.exit()
            elif response_name == RedResponseMessage.DISCONNECT:
                self._close_connection()
                return False
            elif response_name == RedResponseMessage.CONTINUE:
                return False
            elif response_name == RedResponseMessage.PAUSE:
                return True
            elif response_name == RedResponseMessage.EVALUATE_CONDITION:
                return self._evaluate_condition(response)
                
    def _evaluate_condition(self, condition):
        try:
            elements = condition[RedResponseMessage.EVALUATE_CONDITION]
            keywordName, argList = elements[0], elements[1:]

            result = self._built_in.run_keyword_and_return_status(keywordName, *argList)
            self._send_to_server(AgentEventMessage.CONDITION_RESULT, {'result': result})
            return result
        except Exception as e:
            msg = 'Error occurred when evaluating breakpoint condition. ' + str(e)
            self._send_to_server(AgentEventMessage.CONDITION_RESULT, {'error': msg})
            return True

    def _wait_for_resume(self):
        if not self._is_connected:
            return
        possible_responses = [
            RedResponseMessage.RESUME,
            RedResponseMessage.TERMINATE, 
            RedResponseMessage.DISCONNECT] 
        if self._mode == AgentMode.DEBUG:
            possible_responses.append(RedResponseMessage.CHANGE_VARIABLE)
        
        self._send_variables()
        while True:
            self._send_to_server(AgentEventMessage.PAUSED)
            response_name, response = self._wait_for_reponse(*possible_responses)
        
            if response_name == RedResponseMessage.RESUME:
                self._send_to_server(AgentEventMessage.RESUMED)
                return
            elif response_name == RedResponseMessage.TERMINATE:
                sys.exit()
            elif response_name == RedResponseMessage.DISCONNECT:
                self._close_connection()
                return
            elif response_name == RedResponseMessage.CHANGE_VARIABLE:
                try:
                    self._change_variable_value(response)
                    self._send_variables()
                except ValueError as e:
                    self._print_error_message(str(e))
                    self._send_variables(str(e))
                    
    def _change_variable_value(self, response):
        try:
            arguments = response[RedResponseMessage.CHANGE_VARIABLE]
            var_name = arguments['name']
            scope = arguments['scope']
            
            if scope not in ['local', 'test_case', 'test_suite', 'global']:
                raise ValueError('Unable to change value of variable ' + var_name + ' inside unrecognized scope ' + str(scope))
                
            new_values = arguments['values']
            level = arguments['level'] + 1 # adding one because globals are not taken into account
            
            if 'path' in arguments:
                self._change_variable_inner_value(var_name, level, arguments['path'], new_values)
            else:
                self._change_variable_on_top_level(var_name, scope, level, new_values)

        except Exception as e:
            raise ValueError('Unable to change value of variable ' + var_name + '. ' + str(e))
            
    def _change_variable_on_top_level(self, var_name, scope, level, new_values):
        # WARNING : this method uses protected RF methods/fields so it is sensitive for RF changes;
        # currently works fine for RF 2.9 - 3.0
        if scope == 'local':
            name = self._built_in._get_var_name(var_name)
            value = self._built_in._get_var_value(name, new_values)
            
            self._built_in._variables._scopes[level][name] = value
            self._built_in._log_set_variable(name, value)

        elif scope == 'test_case':
            self._built_in.set_test_variable(var_name, *new_values)

        elif scope == 'test_suite':
            if level >= len(self._built_in._variables._scopes) - len(list(self._built_in._variables._scopes_until_suite)):
                # variable in lowest suite, so we'll use keyword
                self._built_in.set_suite_variable(var_name, *new_values)
            else:
                # variable in higher order suite
                name = self._built_in._get_var_name(var_name)
                value = self._built_in._get_var_value(name, new_values)
                
                self._built_in._variables._scopes[level][name] = value
                self._built_in._variables._variables_set._scopes[level - 1][name] = value
                self._built_in._log_set_variable(name, value)

        else:
            self._built_in.set_global_variable(var_name, *new_values)
    
    def _change_variable_inner_value(self, name, level, path, new_values):
        # WARNING : this method uses protected RF methods/fields so it is sensitive for RF changes;
        # currently works fine for RF 2.9 - 3.0
        types = {'scalar' : '$', 'list' : '@', 'dict' : '&'}
        value = self._built_in._get_var_value(types[path[-1][0]] + '{temp_name}', new_values)
        
        old_value = self._built_in._variables._scopes[level].as_dict()[name]
        
        self._change_inner_value(old_value, path[:-1], value)
        
    def _change_inner_value(self, object, path, value):
        val_kind, addr = path[0]
        
        if val_kind == 'list' and isinstance(object, (list, tuple)) or val_kind == 'dict' and isinstance(object, Mapping):
            if len(path) == 1:
                object[addr] = value
            else:
                self._change_inner_value(object[addr], path[1:], value)
        else:
            raise RuntimeError('Requested to change value in ' + val_kind + ' object type, but ' + type(object).__name__ + ' found')

    def _send_variables(self, error=None):
        vars = self._collect_variables()
        if error:
            self._send_to_server(AgentEventMessage.VARIABLES, {'var_scopes': vars, 'error': error})
        else:
            self._send_to_server(AgentEventMessage.VARIABLES, {'var_scopes': vars})
            
    def _collect_variables(self):
        # WARNING : this method uses protected RF methods/fields so it is sensitive for RF changes;
        # currently works fine for RF 2.9 - 3.0
        variables = self._built_in._variables
        frames = variables._scopes
        
        all_frames = []
        
        i = 0
        last_suite_index = frames.index(variables._suite)
        test_index = frames.index(variables._test) if variables._test else -1
        for current_frame in frames:

            current_frame_values = {}
            frame_vars = OrderedDict()
            for variable in current_frame.store:
                value = current_frame.store[variable]
                var, _ = current_frame.store._decorate(variable, value)
                if i == 0:
                    identified_scope = 'global'
                elif var in previous_frame_values and value == previous_frame_values[var][1]:
                    identified_scope = previous_frame_values[var][0]
                elif i <= last_suite_index:
                    identified_scope = 'suite'
                elif i == test_index:
                    identified_scope = 'test' if '$' + var[1:] in variables._variables_set._test else 'local'
                else:
                    identified_scope = 'local'
                
                
                if inspect.ismodule(value) or inspect.isfunction(value) or inspect.isclass(value):
                    type_name = type(value).__name__
                    frame_vars[var] = (type_name, type_name + '@' + str(id(value)), identified_scope)
                else:
                    try:
                        labeled = _label_with_types(value)
                        fixed = _fix_unicode(self.MAX_VARIABLE_VALUE_TEXT_LENGTH, labeled)
                        if isinstance(value, (list, tuple, Mapping)):
                            frame_vars[var] = (fixed[0], fixed[1], identified_scope)
                        else:
                            frame_vars[var] = (fixed[0], str(fixed[1]), identified_scope)
                    except:
                        frame_vars[var] = (type(value).__name__, '<error retrieving value>', identified_scope)

                current_frame_values[var] = (identified_scope, value)    
            
            all_frames.append(frame_vars)
            previous_frame_values = current_frame_values
            i += 1
        all_frames.reverse()
        return all_frames

    def resource_import(self, name, attributes):
        self._send_to_server(AgentEventMessage.RESOURCE_IMPORT, name, attributes)

    def library_import(self, name, attributes):
        # equals org.python.core.ClasspathPyImporter.PYCLASSPATH_PREFIX
        if 'Jython' in platform.python_implementation():
            import org.python.core.imp as jimp
            if attributes['source']:
                if '__pyclasspath__' in attributes['source']:
                    res = attributes['source'].split('__pyclasspath__')[1].replace(os.sep, '')
                    attributes['source'] = str(jimp.getSyspathJavaLoader().getResources(res).nextElement())
            else:
                try:
                    source_uri = jimp.getSyspathJavaLoader().getResources(name + '.class').nextElement()
                    attributes['source'] = str(source_uri)
                except:
                    pass

            source_uri_txt = attributes['source']
            if source_uri_txt and 'file:/' in source_uri_txt:
                from java.io import File as File
                from java.net import URL as URL
                filePath = re.split('.*(?=file[:])', source_uri_txt)
                if len(filePath) > 1:
                    path = re.split('[!][/]', filePath[1])[0]
                    f = File(URL(path).getFile())
                    source_uri_txt = f.getAbsolutePath()
                attributes['source'] = source_uri_txt
        self._send_to_server(AgentEventMessage.LIBRARY_IMPORT, name, attributes)

    def message(self, message):
        if message['level'] in ('ERROR', 'FAIL', 'NONE'):
            self._send_to_server(AgentEventMessage.MESSAGE, message)

    def log_message(self, message):
        if _is_logged(message['level']):
            message['message'] = _truncate(self.MAX_VARIABLE_VALUE_TEXT_LENGTH, message['message'])
            self._send_to_server(AgentEventMessage.LOG_MESSAGE, message)

    def log_file(self, path):
        self._send_to_server(AgentEventMessage.LOG_FILE, path)

    def output_file(self, path):
        self._send_to_server(AgentEventMessage.OUTPUT_FILE, path)

    def report_file(self, path):
        self._send_to_server(AgentEventMessage.REPORT_FILE, path)

    def summary_file(self, path):
        pass

    def debug_file(self, path):
        pass

    def close(self):
        self._send_to_server(AgentEventMessage.CLOSE)
        self._close_connection()

    def _print_error_message(self, message):
        sys.stderr.write('[ ERROR ] ' + message + '\n')
        sys.stderr.flush()

    def _connect(self, host, port, connection_timeout):
        '''Establish a connection for sending data'''
        trials = 1
        start = time.time()
        
        while int(time.time() - start) < connection_timeout:
            sock = None
            try:
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                sock.connect((host, port))
                return True, sock, MessagesDecoderEncoder(sock)
            except socket.error as e:
                print('TestRunnerAgent: connection trial #%s failed' % trials)
                print('\tUnable to open socket to "%s:%s"'  % (host, port))
                print('\terror: %s' % str(e))
                time.sleep(self.CONNECTION_SLEEP_BETWEEN_TRIALS)
            trials += 1
        return False, None, None
    
    def _encode_to_json(self, obj):
        try:
            if self.decoder_encoder:
                return self.decoder_encoder._encode(obj)
        except Exception:
            traceback.print_exc(file=sys.stdout)
            sys.stdout.flush()
            raise

    def _send_to_server(self, name, *args):
        try:
            if self.decoder_encoder:
                packet = {name: args}
                self.decoder_encoder.dump(packet)
        except Exception:
            traceback.print_exc(file=sys.stdout)
            sys.stdout.flush()
            raise

    def _send_to_server_json(self, name, json_encoded_obj):
        try:
            if self.decoder_encoder:
                self.decoder_encoder._write('{"%s": %s}' % (name, json_encoded_obj))
        except Exception:
            traceback.print_exc(file=sys.stdout)
            sys.stdout.flush()
            raise
        
    def _wait_for_reponse(self, *expected_responses):
        response = self._receive_from_server()
        response_key = next(iter(response))
        while not response_key in expected_responses:
            response = self._receive_from_server()
        return response_key, response
    
    def _receive_from_server(self):
        try:
            if self.decoder_encoder:
                return self.decoder_encoder.load()
        except Exception:
            traceback.print_exc(file=sys.stdout)
            sys.stdout.flush()
            raise
            
    def _close_connection(self):
        if self._is_connected:
            self._mode = None
            self._is_connected = False
            
            self.decoder_encoder.close()
            self.decoder_encoder = None
            
            self.sock.close()           
            self.sock = None
            

class MessagesDecoderEncoder(object):
    
    def __init__(self, sock):
        self._string_encoder = (lambda s : s) if sys.version_info < (3, 0, 0) else (lambda s : bytes(s, 'UTF-8'))
        self._string_decoder = (lambda s : s) if sys.version_info < (3, 0, 0) else (lambda s : str(s, 'UTF-8')) 
        self._json_encoder = json.JSONEncoder(separators=(',', ':'), sort_keys=True).encode
        self._json_decoder = json.JSONDecoder(strict=False).decode
        # IronPython does not return right object type if not binary mode
        self._file_to_write = sock.makefile('wb')
        self._file_to_read = sock.makefile('rb')

    def _encode(self, obj):
        return self._json_encoder(obj)

    def dump(self, obj):
        self._write(self._json_encoder(obj))
        
    def _write(self, json_encoded_obj):
        if not self._can_write():
            return
        json_string = json_encoded_obj + '\n'
        self._file_to_write.write(self._string_encoder(json_string))
        self._file_to_write.flush()
            
    def load(self):
        if not self._can_read():
            return
        json_string = self._file_to_read.readline();
        return self._json_decoder(self._string_decoder(json_string))
    
    def _can_write(self):
        return self._file_to_write is not None
    
    def _can_read(self):
        return self._file_to_read is not None
    
    def close(self):
        if self._can_write():
            self._file_to_write.close()
        if self._can_read():
            self._file_to_read.close()