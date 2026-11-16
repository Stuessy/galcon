#include "SDL.h"
#include <stdlib.h>
#include <math.h>

import blit32
import line32

#define SW 640
#define SH 480

#define FPARTS 4096

#define COS(v) (cos(v))
#define SIN(v) (sin(v))
#define ABS(v) ((v)<0?(-v):(v))

#define TYPE_PLANET 1
#define TYPE_SHIP 2



struct fpart:
    float x,y,_x,_y,v,tx,ty,vx,vy,a,r,w
    int active
    int type
    int hit
    SDL_Surface *image
    Uint32 color;
    void (*loop)(fpart *)
    void (*paint)(fpart *,SDL_Surface *)
    fpart *target

export fpart fparts[FPARTS]

void flock_init():
    for int n=0; n<FPARTS; n++:
        fpart *p = &fparts[n]
        p->active = 0

fpart *flock_next():
    static Uint32 cur=0;
    for int n = 0; n<FPARTS; n++:
        fpart *p = &fparts[cur%FPARTS]
        cur ++ 
        if !p->active:
            return p
    printf ("uh oh! ran out of fparts!  $10 says things are gonna crash!\n")
    return 0
    
void flock_loop():

    for int n=0; n<FPARTS; n++:
        fpart *p = &fparts[n]
        if p->active and p->loop:
            if p->type == TYPE_SHIP:
                ship_loop(p)
            else:
                (p->loop)(p)
    
    
    for int n=0; n<FPARTS; n++:
        fpart *a = &fparts[n]
        if a->active:
            for int m=n+1; m<FPARTS; m++: //n+1 ensures that we dont repeat anything
                fpart *b = &fparts[m]
                if b->active:
                    float dx,dy
                    dx = b->x-a->x
                    dy = b->y-a->y
                    float r = a->r+b->r
                    if ABS(dx) > r or ABS(dy) > r:
                        continue
                    
                    float dist = sqrt(dx*dx+dy*dy)
                    //if b->r > r:
                    //    r = b->r
                    if dist < r:
                        if a->target == b:
                            a->hit = 1
                            continue
                        if b->target == a:
                            b->hit = 1
                            continue
                    
                        if dist == 0:
                            dist = 1
                            dx = 1
                        float w = a->w+b->w
                        
                        
                        float mx = (a->x*a->w+b->x*b->w)/w
                        float my = (a->y*a->w+b->y*b->w)/w
                        
                        a->x = mx-(r*b->w*dx)/(dist*w)
                        a->y = my-(r*b->w*dy)/(dist*w)
                        
                        b->x = mx+(r*a->w*dx)/(dist*w)
                        b->y = my+(r*a->w*dy)/(dist*w)
                        
                        
                    
                    

        
void flock_paint(SDL_Surface *s):
    for int n=0; n<FPARTS; n++:
        fpart *p = &fparts[n]
        if p->active and p->paint:
            //SDL_Rect r
            //r.x = (int)p->x
            //r.y = (int)p->y
            //r.x -= p->image->w/2
            //r.y -= p->image->h/2
            //blit32_add(p->image,0,s,&r)
            (p->paint)(p,s)
    
fpart *planet_new(float x,float y, float r):
    fpart *p = flock_next()
    p->x = x
    p->y = y
    p->r = r
    p->active = 1
    p->loop = 0 //planet_loop
    p->paint = 0
    p->w = 100000
    p->type = TYPE_PLANET
    p->target = 0
    p->hit = 0
    return p
    
void planet_loop(fpart *p):
    if p->x < 0: 
        p->x = 0
    if p->y < 0: 
        p->y = 0
        
    if p->x > SW: 
        p->x = SW
    if p->y > SH: 
        p->y = SH
    
    
fpart *ship_new(float x, float y, float tx, float ty,fpart *target, int color):
    fpart *p = flock_next()
    p->x = x
    p->y = y
    p->tx = tx
    p->ty = ty
    p->v = 2.0
    p->r = 6
    p->active = 1
    p->loop = ship_loop
    p->paint = ship_paint
    p->color = color
    p->w = 1.0
    p->type = TYPE_SHIP
    p->target = target
    p->hit = 0
    return p
    
void ship_loop(fpart *p):
    p->_x = p->x
    p->_y = p->y
    float dx,dy
    dx = p->tx-p->x
    dy = p->ty-p->y
    float a = atan2(dy,dx)
    p->a = a
    
    p->vx = cos(a)
    p->vy = sin(a)
    p->x += p->vx*p->v
    p->y += p->vy*p->v
    
#define SHIP_FRONT 6
#define SHIP_SIDE 3
#define SHIP_BACK 3
    
void ship_paint(fpart *p,SDL_Surface *s):
    //printf ("%f %f %f %f\n",p->x,p->y,p->vx,p->vy)
    float x,y,vx,vy
    float x1,y1,x2,y2,x3,y3
    x = p->x
    y = p->y
    
    //float a = p->a
    //vx = p->vx
    //vy = p->vy
    float a = atan2(p->y-p->_y,p->x-p->_x)
    vx = cos(a)
    vy = sin(a)
    x1 = x+vx*SHIP_FRONT
    y1 = y+vy*SHIP_FRONT
    
    x -= vx*SHIP_BACK
    y -= vy*SHIP_BACK
    
    a += M_PI/2.0
    vx = cos(a)
    vy = sin(a)
    x2 = x + vx*SHIP_SIDE
    y2 = y + vy*SHIP_SIDE
    x3 = x - vx*SHIP_SIDE
    y3 = y - vy*SHIP_SIDE
    
    
    ship_line(s,p->color,(int)x1,(int)y1,(int)x2,(int)y2)
    ship_line(s,p->color,(int)x2,(int)y2,(int)x3,(int)y3)
    ship_line(s,p->color,(int)x3,(int)y3,(int)x1,(int)y1)
    
void ship_line(SDL_Surface *s, Uint32 color, int x1, int y1, int x2, int y2):
    int dx = x2-x1
    int dy = y2-y1
    if dx < 0:
        dx = -dx
    if dy < 0:
        dy = -dy
    line32_add_inline(s,color,x1,y1,x2,y2)
    if dx > dy:
        line32_add_inline(s,color,x1,y1+1,x2,y2+1)
    else:
        line32_add_inline(s,color,x1+1,y1,x2+1,y2)
