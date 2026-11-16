#include "SDL.h"

#ifndef PYGAME_H
#define PYGAME_H
#include "pygame.h"
#endif

SDL_Surface *pygame32_obj_to_surface(PyObject *obj):
    SDL_Surface *s = PySurface_AsSurface(obj)
    return s
    

#define RECTS 4096
export SDL_Rect rects[RECTS]

SDL_Rect *pygame32_obj_to_rect(PyObject *obj):
    static int _rect = 0
    GAME_Rect *_r = &PyRect_AsRect(obj)
    SDL_Rect *r = &rects[_rect%RECTS]
    _rect ++
    r->x = _r->x
    r->y = _r->y
    r->w = _r->w
    r->h = _r->h
    return r
    
SDL_Rect *torect(PyObject *obj):
    return pygame32_obj_to_rect(obj)
    
SDL_Surface *tosurface(PyObject *obj):
    return pygame32_obj_to_surface(obj)
    
