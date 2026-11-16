%module flock
%{
#include "flock.h"
%}
#line 1 "flock.pyp"
#include "SDL.h"
#line 2 "flock.pyp"
#include <stdlib.h>
#line 3 "flock.pyp"
#include <math.h>
#line 5 "flock.pyp"
#include "blit32.h"
#line 6 "flock.pyp"
#include "line32.h"
#line 8 "flock.pyp"
#define SW 640
#line 9 "flock.pyp"
#define SH 480
#line 11 "flock.pyp"
#define FPARTS 4096
#line 13 "flock.pyp"
#define COS(v) (cos(v))
#line 14 "flock.pyp"
#define SIN(v) (sin(v))
#line 15 "flock.pyp"
#define ABS(v) ((v)<0?(-v):(v))
#line 17 "flock.pyp"
#define TYPE_PLANET 1
#line 18 "flock.pyp"
#define TYPE_SHIP 2
#line 22 "flock.pyp"
struct fpart{
#line 23 "flock.pyp"
    float x,y,_x,_y,v,tx,ty,vx,vy,a,r,w;
#line 24 "flock.pyp"
    int active;
#line 25 "flock.pyp"
    int type;
#line 26 "flock.pyp"
    int hit;
#line 27 "flock.pyp"
    SDL_Surface *image;
#line 28 "flock.pyp"
    Uint32 color;;
#line 29 "flock.pyp"
    void (*loop)(fpart *);
#line 30 "flock.pyp"
    void (*paint)(fpart *,SDL_Surface *);
#line 31 "flock.pyp"
    fpart *target;
};
#line 33 "flock.pyp"
extern fpart fparts[FPARTS];
#line 35 "flock.pyp"
extern void flock_init();
#line 40 "flock.pyp"
extern fpart *flock_next();
#line 50 "flock.pyp"
extern void flock_loop();
#line 105 "flock.pyp"
extern void flock_paint(SDL_Surface *s);
#line 117 "flock.pyp"
extern fpart *planet_new(float x,float y, float r);
#line 131 "flock.pyp"
extern void planet_loop(fpart *p);
#line 143 "flock.pyp"
extern fpart *ship_new(float x, float y, float tx, float ty,fpart *target, int color);
#line 161 "flock.pyp"
extern void ship_loop(fpart *p);
#line 175 "flock.pyp"
#define SHIP_FRONT 6
#line 176 "flock.pyp"
#define SHIP_SIDE 3
#line 177 "flock.pyp"
#define SHIP_BACK 3
#line 179 "flock.pyp"
extern void ship_paint(fpart *p,SDL_Surface *s);
#line 211 "flock.pyp"
extern void ship_line(SDL_Surface *s, Uint32 color, int x1, int y1, int x2, int y2);
