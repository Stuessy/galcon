# pyplus - magic py++ to c++ converter.  
# Copyright (C) 2006 Phil Hassey
# 
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

import re
import sys
import os
import filecmp
import glob
from stat import *

# all of these options can be overridden by placing
# a file named FLAGS in the dir with your .pyp files

TAB = 4 #number of spaces in a tab
NL = '\n' #new line string (this can't be overridden)
LINES = 1 #if #line directives should be put in all the generated code

PYP_EXT = 'pyp'
HEADER_EXT = 'h'
SOURCE_EXT = 'cc'
PY_EXT = 'py'
PYC_EXT = 'pyc'
PYD_EXT = 'so'
SWIG_EXT = 'i'

CC = 'g++'
CCFLAGS = '-I/usr/include/python2.4'
LFLAGS = '-lstdc++'

SWIG = 'swig'
SWIGFLAGS = '-c++'

LD = 'ld'
LDFLAGS = ''

# load up options from FLAGS
f = None
try:
    f = open("FLAGS","rt")
except:
    pass
if f != None:
    for line in f.readlines():
        line = line.strip()
        if line == '': continue
        if '=' not in line: continue
        key,value = line.split("=",1)
        key,value = key.strip(),value.strip()
        if key in ('TAB','LINES'):
            value = int(value)
        globals()[key]=value
    f.close()
TAB = ' '*TAB

def main():
    if len(sys.argv) < 2:
        print """python pyplus.py command [module]
        
pyplus - magic py++ to c++ converter.  
Copyright (C) 2006 Phil Hassey
        
code    - generate source and header files
build   - code ; build an executable
run     - code ; build ; run the executable
swig    - code ; use swig to generate python bindings
          and create shared objects
clean   - clean up all generated files

you may put optional compilation flags into a file
named FLAGS.  see top of pyplus.py for details.
"""

        return
    command = sys.argv[1]
    _modules = []
    if len(sys.argv) > 2:
        app = sys.argv[2]
        _modules = sys.argv[2:]
    else:
        app = 'main'
        for fname in glob.glob("*.%s"%PYP_EXT):
            _modules.append(fname.replace(".%s"%PYP_EXT,""))
    _cmds = {
        'code':['code'],
        'build':['code','build'],
        'run':['code','build','run'],
        'swig':['code','swig'],
        'clean':['clean'],
        }
    todo = _cmds[command]
    
    modules = {}
    
    #find dependancies
    while len(_modules):
        name = _modules.pop()
        data = modules[name] = Data(name)
        data.parse()
         
        #make a list of its imports
        for name in data.imports:
            if name not in _modules and name not in modules.keys():
                _modules.append(name)
    
    #check if we need to rebuild any files...
    for name,data in modules.items():
        for ext,key in [(HEADER_EXT,'do_header'),(SOURCE_EXT,'do_source'),('o','do_obj')]:
            fname = "%s.%s"%(name,ext)
            rebuild = False
            #if app == name: rebuild = True
            if not os.path.exists(fname):
                rebuild = True
            else:
                s = os.stat(fname)
                if s[ST_MTIME] < data.stat[ST_MTIME]:
                    rebuild = True
            setattr(data,key,rebuild)
        data.header_changed = False
                
    #clean up everything!
    if 'clean' in todo:
        for name in modules.keys():
            for fname in [
                '%s.%s'%(name,SOURCE_EXT),'%s.%s'%(name,HEADER_EXT),'%s.o'%name,
                '%s.%s'%(name,SWIG_EXT),'%s_wrap.%s'%(name,SOURCE_EXT),'%s_wrap.o'%(name),
                '%s.py'%name,'%s.pyc'%name,
                '_%s.%s'%(name,PYD_EXT),
                'lib_%s.a'%(name),
                ]:
                
                if os.path.exists(fname):
                    unlink(fname)
        if os.path.exists(app):
            unlink(app)
        
    
    #re-build headers for files that have changed
    if 'code' in todo:
        for name,data in modules.items():
            if data.do_header:
                fname,tmp = "%s.%s"%(name,HEADER_EXT),"%s.%s-tmp"%(name,HEADER_EXT)
                if os.path.exists(fname):
                    os.rename(fname,tmp)
                write(fname,write_header,data)
                if not os.path.exists(tmp) or not filecmp.cmp(fname,tmp):
                    data.header_changed = True
                if os.path.exists(tmp):
                    unlink(tmp)
                data.do_source = True
    
    #for the changed headers, make sure those who import them get rebuilt
    for name,data in modules.items():
        if data.header_changed:
            for data2 in modules.values():
                if name in data2.imports: data2.do_obj = True
    
    #re-build sources for files that have changed
    if 'code' in todo:
        for name,data in modules.items():
            if data.do_source:
                write("%s.%s"%(name,SOURCE_EXT),write_source,data)
                data.do_obj = True
                
    #SWIG
    if 'swig' in todo:
        for name,data in modules.items():
            if data.do_obj:
                fname = "%s.%s"%(name,SWIG_EXT)
                if os.path.exists(fname):
                    unlink(fname)
                write(fname,write_swig,data)
                fname = "%s.%s"%(name,PY_EXT)
                if os.path.exists(fname):
                    unlink(fname)
                fname = "%s_wrap.%s"%(name,SOURCE_EXT)
                if os.path.exists(fname):
                    unlink(fname)
                if system("%s -python %s -o %s_wrap.%s %s.%s "%(SWIG,SWIGFLAGS,name,SOURCE_EXT,name,SWIG_EXT)):
                    return
                if system("%s -c %s %s.%s %s_wrap.%s"%(CC,CCFLAGS,name,SOURCE_EXT,name,SOURCE_EXT)):
                    return
                
                if system("%s rc lib_%s.a %s.o"%('ar',name,name)):
                    return
        
        for name,data in modules.items():
            if data.do_obj:
                deps = data.imports[:]
                ldeps = 0
                while ldeps != len(deps):
                    ldeps = len(deps)
                    for name2 in deps:
                        data2 = modules[name2]
                        for name3 in data2.imports:
                            if name3 not in deps:
                                deps.append(name3)
                
                objs = []
                for n in deps:
                    if n != name:
                        #fname = "%s.o"%n
                        fname = '-l_%s'%n
                        objs.append(fname)
                
                if system("%s -shared -L. %s.o %s_wrap.o %s %s -o _%s.%s"%(LD,name,name," ".join(objs),LDFLAGS,name,PYD_EXT)):
                    return
    
    #build apps
    if 'build' in todo:
        #re-compile sources
        for name,data in modules.items():
            if data.do_obj:
                fname = "%s.o"%name
                if os.path.exists(fname):
                    unlink(fname)
                if system("%s -c %s %s.%s"%(CC,CCFLAGS,name,SOURCE_EXT)):
                    return
            
        #create executable
        objs = []
        for name in modules.keys():
            fname = "%s.o"%name 
            objs.append(fname)
        if system("%s %s %s -o %s"%(CC," ".join(objs),LFLAGS,app)):
            return
        
    #run module
    if 'run' in todo:
        system("./%s"%app)
    
def log(*x):
    print ' '.join(x)

def system(cmd):
    log(cmd)
    return os.system(cmd)

def unlink(fname):
    log('unlink',fname)
    os.unlink(fname)
    
def write_source(f,data):
    f.write(("#include \"%s.%s\""%(data.name,HEADER_EXT))+NL)
    render(f,data,RenderSource())
    
def write_header(f,data):
    define = ("%s_H"%(data.name)).upper()
    f.write(("#ifndef %s"%define)+NL)
    f.write(("#define %s"%define)+NL)
    render(f,data,RenderHeader())
    f.write("#endif"+NL)
    
def write_swig(f,data):
    f.write(("%%module %s"%data.name)+NL)
    f.write("%{"+NL)
    f.write(("#include \"%s.%s\""%(data.name,HEADER_EXT))+NL)
    f.write("%}"+NL)
    render(f,data,RenderHeader())

def write(fname,fnc,data):
    print 'writing',fname
    f = open(fname,'wt')
    fnc(f,data)
    f.close()

def _strip_colon(line):
    if line[-1] == ':':
        line = line[:-1].strip()
    return line

class RenderSource:
    def __init__(self):
        self.in_class = None
        self.in_inline = False
        
    def handle(self,item,action,line):
        if self.in_inline and (item,action) != ('inline','end'): return
        m = '%s_%s'%(item,action)
        if hasattr(self,m): return getattr(self,m)(line)
        #print m
        
    def _generic_begin(self,line,term,parens=False):
        if parens or line[-1] != ')':
            line = term+'('+line[len(term):]+')'
        line = line + ' {'
        return line
    
    def inline_begin(self,line):
        self.in_inline = True
    def inline_end(self,line):
        self.in_inline = False
    
    def for_begin(self,line): return self._generic_begin(_strip_colon(line),'for')
    def for_line(self,line): return line+';'
    def for_end(self,line): return '}'
    
    def while_begin(self,line): return self._generic_begin(_strip_colon(line),'while',True)
    def while_line(self,line): return line+';'
    def while_end(self,line): return '}'
    
    def if_begin(self,line): return self._generic_begin(_strip_colon(line),'if',True)
    def if_line(self,line): return line+';'
    def if_end(self,line): return '}'
    
    def elseif_begin(self,line): return self._generic_begin(_strip_colon(line),'else if',True)
    def elseif_line(self,line): return line+';'
    def elseif_end(self,line): return '}'
    
    def else_begin(self,line): return _strip_colon(line)+'{'
    def else_line(self,line): return line+';'
    def else_end(self,line): return '}'
    
    def switch_begin(self,line): return self._generic_begin(_strip_colon(line),'switch',True)
    def switch_line(self,line): return line+';'
    def switch_end(self,line): return '}'
    
    def case_begin(self,line): return line
    def case_line(self,line): return line+';'
    def case_end(self,line): return 'break;'
    
    def default_begin(self,line): return line
    def default_line(self,line): return line+';'
    def default_end(self,line): return 'break;'

    #def class_begin(self,line): return _strip_colon(line)+'{'
    #def class_line(self,line): return line+';'
    #def class_end(self,line): return '};'
    def class_begin(self,line):
        regex = re.compile("class\s+([a-zA-Z0-9_]+)[\s:].*")
        self.in_class = regex.match(line).group(1)
        return '//'+_strip_colon(line)
    def class_end(self,line):
        self.in_class = None
        
    #def struct_begin(self,line): return _strip_colon(line)+'{'
    #def struct_line(self,line): return line+';'
    #def struct_end(self,line): return '};'
    
    def directive_do(self,line): return line
    def comment_do(self,line): return line
    def variable_do(self,line): return line+';'
    
    def function_begin(self,line):
        if self.in_class:
            regex = re.compile("^virtual\s")
            line = re.sub(regex,'',line)
            regex = re.compile("([\*\s])([a-zA-Z0-9_]+)(\()")
            return re.sub(regex,"\\1%s::\\2\\3"%self.in_class,_strip_colon(line))+'{'
        return _strip_colon(line)+'{'
    def function_line(self,line): return line+';'
    def function_end(self,line): return '}'
    
    def body_line(self,line): return line+';'
    def export_do(self,line): return line[7:]+';'

class RenderHeader:
    def __init__(self):
        self.in_class = None
        self.in_inline = False
        self.render = RenderSource()
    def handle(self,item,action,line):
        if self.in_inline and item != 'inline':
            return self.render.handle(item,action,line)
        m = '%s_%s'%(item,action)
        if hasattr(self,m): return getattr(self,m)(line)
    def class_begin(self,line): 
        regex = re.compile("class\s+([a-zA-Z0-9_]+)[\s:].*")
        self.in_class = regex.match(line).group(1)
        return _strip_colon(line)+'{'
    def class_line(self,line): return line+';'
    def class_end(self,line): 
        self.in_class = None
        return '};'
    
    def struct_begin(self,line): return _strip_colon(line)+'{'
    def struct_line(self,line): return line+';'
    def struct_end(self,line): return '};'
    
    def typedef_do(self,line): return line+';'
    
    def directive_do(self,line): return line
    
    def export_do(self,line): return 'extern '+line[7:]+';'
    def function_begin(self,line): 
        if self.in_class:
            return _strip_colon(line)+';'
        return 'extern '+_strip_colon(line)+';'
    
    def inline_begin(self,line): 
        self.in_inline = True
        if self.in_class:
            regex = re.compile("([\*\s])([a-zA-Z0-9_]+)(\()")
            return re.sub(regex,"\\1%s::\\2\\3"%self.in_class,_strip_colon(line))+'{'
        return _strip_colon(line)+'{'
    def inline_line(self,line): return line+';'
    def inline_end(self,line): 
        self.in_inline = False
        return '}'
        
        
class Parse:
    def __init__(self):
        self._clean_regex = [re.compile(exp) for exp in [
            '\/\/.*$',
            ]]
        self._match_regex = [(re.compile(exp),value) for exp,value in [
            ('^export\s(.*)$','export'),
            ('^for([\s\(]+.*):$','for'),
            ('^while([\s\(]+.*):$','while'),
            ('^if([\s\(]+.*):$','if'),
            ('^else if([\s\(]+.*):$','elseif'),
            ('^else\s*\:$','else'),
            ('^switch([\s\(]+.*):$','switch'),
            ('^case\s+.*:$','case'),
            ('^default\s*:$','default'),
            ('^class([\s\(]+.*):$','class'),
            ('^struct([\s\(]+.*):$','struct'),
            ('^typedef([\s\(]+.*)$','typedef'),
            ('^public:$',None), #this isn't a function :)
            ('^protected:$',None), #this isn't a function :)
            ('^private:$',None), #this isn't a function :)
            ('^inline\s(.*):$','inline'),
            ('^(.*):$','function'),
            ('^#.*$','directive'),
            ]]
        
    def clean(self,line):
        for regex in self._clean_regex:
            line = re.sub(regex,'',line)
        return line
        
    def match(self,line):
        for regex,value in self._match_regex:
            m = regex.match(line)
            if m:
                return value
            
class Data:
    def __init__(self,name):
        self.name = name
        fname = self.fname = "%s.%s"%(name,PYP_EXT)
        self.imports = []
        self.lines = []
        self.stat = os.stat(fname)
        
    def parse(self):
        re_tab = re.compile('^'+TAB)
        re_import = re.compile('^import ([a-zA-Z0-9_]+)')
        lines = self.lines
        
        fname = self.fname
        log('reading',fname)
        f = open(fname,"rt")
    
        parse = Parse()

        lines.append((0,None))
        num = 0
        continued = False
        for line in f.readlines():
            num += 1
            line = line.rstrip()
            
            if line == '': continue
            tabs = 0
            while re_tab.match(line):
                line = re.sub(re_tab,'',line)
                tabs += 1
            line = parse.clean(line).strip()
            if line == '': continue
            
            if continued:
                c_line = c_line[:-1] + ' ' + tabs*TAB + line
                if line[-1] == '\\':
                    continue
                lines.append((c_tabs,c_line,c_num))
                continued = False
                continue
            
            m = re_import.match(line)
            if m: #import
                _import = m.group(1)
                line = "#include \"%s.%s\""%(_import,HEADER_EXT)
                self.imports.append(_import)
            elif line[-1] == '\\': #continued
                c_tabs = tabs
                c_line = line
                c_num = num
                continued = True
                continue
            lines.append((tabs,line,num))
        lines.append((0,None))

        
def render(f,data,render):
    lines = data.lines
    n = 1
    stack = ['body']
    
    parse = Parse()
    
    for tabs,line,num in lines[1:-1]:
        cur = stack[-1]
        out = None
        _cur = parse.match(line)
        _tabs = lines[n+1][0]
        
        if _cur != None:
            if _tabs > tabs:
                cur = _cur
                #m = '%s_begin'%cur
                #if hasattr(render,m): out = getattr(render,m)(line)
                out = render.handle(cur,'begin',line)
                stack.append(cur)
            else:
                #m = '%s_do'%_cur
                #if hasattr(render,m): out = getattr(render,m)(line)
                out = render.handle(_cur,'do',line)
        elif _tabs-tabs > 0 :
            raise data.fname,line,'no match, has indent'
        else:
            #m = '%s_line'%cur
            #if hasattr(render,m): out = getattr(render,m)(line)
            out = render.handle(cur,'line',line)
            
        if out != None: 
            if LINES:
                f.write('#line %d "%s"'%(num,data.fname)+NL)
            f.write(tabs*TAB+out+NL)
                    
        while _tabs < tabs:
            tabs -= 1
            cur = stack.pop(-1)
            out = None
            #m = '%s_end'%cur
            #if hasattr(render,m): out = getattr(render,m)()
            out = render.handle(cur,'end',None)
            if out != None: f.write(tabs*TAB+out+NL)
        n += 1
        
    
    
if __name__ == '__main__':
    main()
