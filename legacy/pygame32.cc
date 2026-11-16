#include "pygame32.h"
#line 1 "pygame32.pyp"
#include "SDL.h"
#line 3 "pygame32.pyp"
#ifndef PYGAME_H
#line 4 "pygame32.pyp"
#define PYGAME_H
#line 5 "pygame32.pyp"
#include "pygame.h"
#line 6 "pygame32.pyp"
#endif
#line 8 "pygame32.pyp"
SDL_Surface *pygame32_obj_to_surface(PyObject *obj){
#line 9 "pygame32.pyp"
    SDL_Surface *s = PySurface_AsSurface(obj);
#line 10 "pygame32.pyp"
    return s;
}
#line 13 "pygame32.pyp"
#define RECTS 4096
#line 14 "pygame32.pyp"
SDL_Rect rects[RECTS];
#line 16 "pygame32.pyp"
SDL_Rect *pygame32_obj_to_rect(PyObject *obj){
#line 17 "pygame32.pyp"
    static int _rect = 0;
#line 18 "pygame32.pyp"
    GAME_Rect *_r = &PyRect_AsRect(obj);
#line 19 "pygame32.pyp"
    SDL_Rect *r = &rects[_rect%RECTS];
#line 20 "pygame32.pyp"
    _rect ++;
#line 21 "pygame32.pyp"
    r->x = _r->x;
#line 22 "pygame32.pyp"
    r->y = _r->y;
#line 23 "pygame32.pyp"
    r->w = _r->w;
#line 24 "pygame32.pyp"
    r->h = _r->h;
#line 25 "pygame32.pyp"
    return r;
}
#line 27 "pygame32.pyp"
SDL_Rect *torect(PyObject *obj){
#line 28 "pygame32.pyp"
    return pygame32_obj_to_rect(obj);
}
#line 30 "pygame32.pyp"
SDL_Surface *tosurface(PyObject *obj){
#line 31 "pygame32.pyp"
    return pygame32_obj_to_surface(obj);
}
