#ifndef PARTS_H
#define PARTS_H
#line 1 "parts.pyp"
#include "SDL.h"
#line 2 "parts.pyp"
#include <stdlib.h>
#line 3 "parts.pyp"
#include <math.h>
#line 5 "parts.pyp"
#include "blit32.h"
#line 7 "parts.pyp"
#define PARTS 1024
#line 9 "parts.pyp"
extern int prr(int min, int max);
#line 16 "parts.pyp"
struct part{
#line 17 "parts.pyp"
    float x,y,vx,vy;
#line 18 "parts.pyp"
    int active;
#line 19 "parts.pyp"
    Uint8 alpha;
#line 20 "parts.pyp"
    SDL_Surface *image;
#line 21 "parts.pyp"
    int frame;
#line 22 "parts.pyp"
    void (*loop)(part *);
};
#line 24 "parts.pyp"
extern part parts[PARTS];
#line 26 "parts.pyp"
extern void parts_init();
#line 31 "parts.pyp"
extern part *parts_next();
#line 36 "parts.pyp"
extern void parts_loop();
#line 42 "parts.pyp"
extern void parts_paint(SDL_Surface *s);
#line 60 "parts.pyp"
extern void explode_loop(part *p);
#line 70 "parts.pyp"
extern void explode(float x, float y, float r, int cnt, SDL_Surface *image);
#endif
