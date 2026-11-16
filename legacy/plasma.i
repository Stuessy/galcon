%module plasma
%{
#include "plasma.h"
%}
#line 1 "plasma.pyp"
#include "stdlib.h"
#line 3 "plasma.pyp"
#include "SDL.h"
#line 5 "plasma.pyp"
#ifndef PYGAME_H
#line 6 "plasma.pyp"
#define PYGAME_H
#line 7 "plasma.pyp"
#include "pygame.h"
#line 8 "plasma.pyp"
#endif
#line 10 "plasma.pyp"
extern Uint8 setpixel(SDL_Surface *s, int x, int y, Uint8 c);
#line 14 "plasma.pyp"
extern Uint8 getpixel(SDL_Surface *s, int x, int y);
#line 18 "plasma.pyp"
extern int rr(int min, int max);
#line 25 "plasma.pyp"
#define right (w)
#line 26 "plasma.pyp"
#define bottom (h)
#line 27 "plasma.pyp"
#define centerx (w/2)
#line 28 "plasma.pyp"
#define centery (h/2)
#line 30 "plasma.pyp"
extern void plasma(PyObject *obj, int l);
#line 40 "plasma.pyp"
extern SDL_Surface *subsurface(SDL_Surface *src,int x,int y,int w,int h,SDL_Surface *dst);
#line 47 "plasma.pyp"
extern Uint8 _bound(Sint16 v);
#line 55 "plasma.pyp"
extern void blur(SDL_Surface *a, SDL_Surface *b);
#line 84 "plasma.pyp"
extern void _plasma(SDL_Surface *s,int l);
