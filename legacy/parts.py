# This file was created automatically by SWIG 1.3.29.
# Don't modify this file, modify the SWIG interface instead.
# This file is compatible with both classic and new-style classes.

import _parts
import new
new_instancemethod = new.instancemethod
def _swig_setattr_nondynamic(self,class_type,name,value,static=1):
    if (name == "thisown"): return self.this.own(value)
    if (name == "this"):
        if type(value).__name__ == 'PySwigObject':
            self.__dict__[name] = value
            return
    method = class_type.__swig_setmethods__.get(name,None)
    if method: return method(self,value)
    if (not static) or hasattr(self,name):
        self.__dict__[name] = value
    else:
        raise AttributeError("You cannot add attributes to %s" % self)

def _swig_setattr(self,class_type,name,value):
    return _swig_setattr_nondynamic(self,class_type,name,value,0)

def _swig_getattr(self,class_type,name):
    if (name == "thisown"): return self.this.own()
    method = class_type.__swig_getmethods__.get(name,None)
    if method: return method(self)
    raise AttributeError,name

def _swig_repr(self):
    try: strthis = "proxy of " + self.this.__repr__()
    except: strthis = ""
    return "<%s.%s; %s >" % (self.__class__.__module__, self.__class__.__name__, strthis,)

import types
try:
    _object = types.ObjectType
    _newclass = 1
except AttributeError:
    class _object : pass
    _newclass = 0
del types


PARTS = _parts.PARTS
prr = _parts.prr
class part(_object):
    __swig_setmethods__ = {}
    __setattr__ = lambda self, name, value: _swig_setattr(self, part, name, value)
    __swig_getmethods__ = {}
    __getattr__ = lambda self, name: _swig_getattr(self, part, name)
    __repr__ = _swig_repr
    __swig_setmethods__["x"] = _parts.part_x_set
    __swig_getmethods__["x"] = _parts.part_x_get
    if _newclass:x = property(_parts.part_x_get, _parts.part_x_set)
    __swig_setmethods__["y"] = _parts.part_y_set
    __swig_getmethods__["y"] = _parts.part_y_get
    if _newclass:y = property(_parts.part_y_get, _parts.part_y_set)
    __swig_setmethods__["vx"] = _parts.part_vx_set
    __swig_getmethods__["vx"] = _parts.part_vx_get
    if _newclass:vx = property(_parts.part_vx_get, _parts.part_vx_set)
    __swig_setmethods__["vy"] = _parts.part_vy_set
    __swig_getmethods__["vy"] = _parts.part_vy_get
    if _newclass:vy = property(_parts.part_vy_get, _parts.part_vy_set)
    __swig_setmethods__["active"] = _parts.part_active_set
    __swig_getmethods__["active"] = _parts.part_active_get
    if _newclass:active = property(_parts.part_active_get, _parts.part_active_set)
    __swig_setmethods__["alpha"] = _parts.part_alpha_set
    __swig_getmethods__["alpha"] = _parts.part_alpha_get
    if _newclass:alpha = property(_parts.part_alpha_get, _parts.part_alpha_set)
    __swig_setmethods__["image"] = _parts.part_image_set
    __swig_getmethods__["image"] = _parts.part_image_get
    if _newclass:image = property(_parts.part_image_get, _parts.part_image_set)
    __swig_setmethods__["frame"] = _parts.part_frame_set
    __swig_getmethods__["frame"] = _parts.part_frame_get
    if _newclass:frame = property(_parts.part_frame_get, _parts.part_frame_set)
    __swig_setmethods__["loop"] = _parts.part_loop_set
    __swig_getmethods__["loop"] = _parts.part_loop_get
    if _newclass:loop = property(_parts.part_loop_get, _parts.part_loop_set)
    def __init__(self, *args): 
        this = _parts.new_part(*args)
        try: self.this.append(this)
        except: self.this = this
    __swig_destroy__ = _parts.delete_part
    __del__ = lambda self : None;
part_swigregister = _parts.part_swigregister
part_swigregister(part)

parts_init = _parts.parts_init
parts_next = _parts.parts_next
parts_loop = _parts.parts_loop
parts_paint = _parts.parts_paint
explode_loop = _parts.explode_loop
explode = _parts.explode

cvar = _parts.cvar

