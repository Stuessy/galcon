# This file was created automatically by SWIG 1.3.29.
# Don't modify this file, modify the SWIG interface instead.
# This file is compatible with both classic and new-style classes.

import _flock
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


SW = _flock.SW
SH = _flock.SH
FPARTS = _flock.FPARTS
TYPE_PLANET = _flock.TYPE_PLANET
TYPE_SHIP = _flock.TYPE_SHIP
class fpart(_object):
    __swig_setmethods__ = {}
    __setattr__ = lambda self, name, value: _swig_setattr(self, fpart, name, value)
    __swig_getmethods__ = {}
    __getattr__ = lambda self, name: _swig_getattr(self, fpart, name)
    __repr__ = _swig_repr
    __swig_setmethods__["x"] = _flock.fpart_x_set
    __swig_getmethods__["x"] = _flock.fpart_x_get
    if _newclass:x = property(_flock.fpart_x_get, _flock.fpart_x_set)
    __swig_setmethods__["y"] = _flock.fpart_y_set
    __swig_getmethods__["y"] = _flock.fpart_y_get
    if _newclass:y = property(_flock.fpart_y_get, _flock.fpart_y_set)
    __swig_setmethods__["_x"] = _flock.fpart__x_set
    __swig_getmethods__["_x"] = _flock.fpart__x_get
    if _newclass:_x = property(_flock.fpart__x_get, _flock.fpart__x_set)
    __swig_setmethods__["_y"] = _flock.fpart__y_set
    __swig_getmethods__["_y"] = _flock.fpart__y_get
    if _newclass:_y = property(_flock.fpart__y_get, _flock.fpart__y_set)
    __swig_setmethods__["v"] = _flock.fpart_v_set
    __swig_getmethods__["v"] = _flock.fpart_v_get
    if _newclass:v = property(_flock.fpart_v_get, _flock.fpart_v_set)
    __swig_setmethods__["tx"] = _flock.fpart_tx_set
    __swig_getmethods__["tx"] = _flock.fpart_tx_get
    if _newclass:tx = property(_flock.fpart_tx_get, _flock.fpart_tx_set)
    __swig_setmethods__["ty"] = _flock.fpart_ty_set
    __swig_getmethods__["ty"] = _flock.fpart_ty_get
    if _newclass:ty = property(_flock.fpart_ty_get, _flock.fpart_ty_set)
    __swig_setmethods__["vx"] = _flock.fpart_vx_set
    __swig_getmethods__["vx"] = _flock.fpart_vx_get
    if _newclass:vx = property(_flock.fpart_vx_get, _flock.fpart_vx_set)
    __swig_setmethods__["vy"] = _flock.fpart_vy_set
    __swig_getmethods__["vy"] = _flock.fpart_vy_get
    if _newclass:vy = property(_flock.fpart_vy_get, _flock.fpart_vy_set)
    __swig_setmethods__["a"] = _flock.fpart_a_set
    __swig_getmethods__["a"] = _flock.fpart_a_get
    if _newclass:a = property(_flock.fpart_a_get, _flock.fpart_a_set)
    __swig_setmethods__["r"] = _flock.fpart_r_set
    __swig_getmethods__["r"] = _flock.fpart_r_get
    if _newclass:r = property(_flock.fpart_r_get, _flock.fpart_r_set)
    __swig_setmethods__["w"] = _flock.fpart_w_set
    __swig_getmethods__["w"] = _flock.fpart_w_get
    if _newclass:w = property(_flock.fpart_w_get, _flock.fpart_w_set)
    __swig_setmethods__["active"] = _flock.fpart_active_set
    __swig_getmethods__["active"] = _flock.fpart_active_get
    if _newclass:active = property(_flock.fpart_active_get, _flock.fpart_active_set)
    __swig_setmethods__["type"] = _flock.fpart_type_set
    __swig_getmethods__["type"] = _flock.fpart_type_get
    if _newclass:type = property(_flock.fpart_type_get, _flock.fpart_type_set)
    __swig_setmethods__["hit"] = _flock.fpart_hit_set
    __swig_getmethods__["hit"] = _flock.fpart_hit_get
    if _newclass:hit = property(_flock.fpart_hit_get, _flock.fpart_hit_set)
    __swig_setmethods__["image"] = _flock.fpart_image_set
    __swig_getmethods__["image"] = _flock.fpart_image_get
    if _newclass:image = property(_flock.fpart_image_get, _flock.fpart_image_set)
    __swig_setmethods__["color"] = _flock.fpart_color_set
    __swig_getmethods__["color"] = _flock.fpart_color_get
    if _newclass:color = property(_flock.fpart_color_get, _flock.fpart_color_set)
    __swig_setmethods__["loop"] = _flock.fpart_loop_set
    __swig_getmethods__["loop"] = _flock.fpart_loop_get
    if _newclass:loop = property(_flock.fpart_loop_get, _flock.fpart_loop_set)
    __swig_setmethods__["paint"] = _flock.fpart_paint_set
    __swig_getmethods__["paint"] = _flock.fpart_paint_get
    if _newclass:paint = property(_flock.fpart_paint_get, _flock.fpart_paint_set)
    __swig_setmethods__["target"] = _flock.fpart_target_set
    __swig_getmethods__["target"] = _flock.fpart_target_get
    if _newclass:target = property(_flock.fpart_target_get, _flock.fpart_target_set)
    def __init__(self, *args): 
        this = _flock.new_fpart(*args)
        try: self.this.append(this)
        except: self.this = this
    __swig_destroy__ = _flock.delete_fpart
    __del__ = lambda self : None;
fpart_swigregister = _flock.fpart_swigregister
fpart_swigregister(fpart)

flock_init = _flock.flock_init
flock_next = _flock.flock_next
flock_loop = _flock.flock_loop
flock_paint = _flock.flock_paint
planet_new = _flock.planet_new
planet_loop = _flock.planet_loop
ship_new = _flock.ship_new
ship_loop = _flock.ship_loop
SHIP_FRONT = _flock.SHIP_FRONT
SHIP_SIDE = _flock.SHIP_SIDE
SHIP_BACK = _flock.SHIP_BACK
ship_paint = _flock.ship_paint
ship_line = _flock.ship_line

cvar = _flock.cvar

