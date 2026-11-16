#ifndef PYGAME32_H
#define PYGAME32_H
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
extern SDL_Surface *pygame32_obj_to_surface(PyObject *obj);
#line 13 "pygame32.pyp"
#define RECTS 4096
#line 14 "pygame32.pyp"
extern SDL_Rect rects[RECTS];
#line 16 "pygame32.pyp"
extern SDL_Rect *pygame32_obj_to_rect(PyObject *obj);
#line 27 "pygame32.pyp"
extern SDL_Rect *torect(PyObject *obj);
#line 30 "pygame32.pyp"
extern SDL_Surface *tosurface(PyObject *obj);
#endif
