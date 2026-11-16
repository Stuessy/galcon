#include "stdlib.h"

#include "SDL.h"

#ifndef PYGAME_H
#define PYGAME_H
#include "pygame.h"
#endif

Uint8 setpixel(SDL_Surface *s, int x, int y, Uint8 c):
    Uint8 *pixels = (Uint8*)s->pixels
    *(pixels+s->pitch*y+x) = c
    
Uint8 getpixel(SDL_Surface *s, int x, int y):
    Uint8 *pixels = (Uint8*)s->pixels
    return *(pixels+s->pitch*y+x)
    
int rr(int min, int max):
    if (max - min) <= 0:
        return min
    int r = (rand()%(max-min))+min
    //printf("%d\n",r)
    return r

#define right (w)
#define bottom (h)
#define centerx (w/2)
#define centery (h/2)

void plasma(PyObject *obj, int l):
    SDL_Surface *s = PySurface_AsSurface(obj)
    int w = s->w
    int h = s->h
    setpixel(s,0,0,rr(0,256))
    setpixel(s,right-1,0,rr(0,256))
    setpixel(s,0,bottom-1,rr(0,256))
    setpixel(s,right-1,bottom-1,rr(0,256))
    _plasma(s,l)
    
SDL_Surface *subsurface(SDL_Surface *src,int x,int y,int w,int h,SDL_Surface *dst):
    dst->pixels = (Uint8*)src->pixels+y*src->pitch+x
    dst->w = w
    dst->h = h
    dst->pitch = src->pitch
    return dst

Uint8 _bound(Sint16 v):
    if v < 0: 
        return 0
    if v > 255: 
        return 255
    return v


void blur(SDL_Surface *a, SDL_Surface *b):
    for int y=1;y<a->h-1;y++:
        int x = 1
        Uint8 p1,p2,p3,p4,p5,p6,p7,p8,p9
        p1 = getpixel(a,x-1,y+1)
        p2 = getpixel(a,x+0,y+1)
        p3 = getpixel(a,x+1,y+1)
        p4 = getpixel(a,x-1,y+0)
        p5 = getpixel(a,x+0,y+0)
        p6 = getpixel(a,x+1,y+0)
        p7 = getpixel(a,x-1,y-1)
        p8 = getpixel(a,x+0,y-1)
        p9 = getpixel(a,x+1,y-1)
        for x=1; x<a->w-1; x++:
            int v = ((p5)*4+(p8+p4+p6+p2)*2+(p7+p9+p1+p3))/16
            setpixel(b,x,y,v)
            p1 = p2
            p2 = p3
            p3 = getpixel(a,x+1,y+1)
            p4 = p5
            p5 = p6
            p6 = getpixel(a,x+1,y+0)
            p7 = p8
            p8 = p9
            p9 = getpixel(a,x+1,y-1)

            
    return

void _plasma(SDL_Surface *s,int l):
    int w = s->w
    int h = s->h
    int p7 = getpixel(s,0,0)
    int p9 = getpixel(s,right-1,0)
    int p1 = getpixel(s,0,bottom-1)
    int p3 = getpixel(s,right-1,bottom-1)
    
    setpixel(s,centerx,0,_bound((p7+p9)/2))
    setpixel(s,0,centery,_bound((p7+p1)/2))
    setpixel(s,centerx,centery,_bound((p7+p9+p1+p3)/4+rr(-l,l)))
    setpixel(s,right-1,centery,_bound((p9+p3)/2))
    setpixel(s,centerx,bottom-1,_bound((p1+p3)/2))
    if w <= 3 && h <= 3: 
        return
    SDL_Surface sub
    l /= 2
    _plasma(subsurface(s,0,0,centerx+1,centery+1,&sub),l)
    _plasma(subsurface(s,centerx,0,right-centerx,centery+1,&sub),l)
    _plasma(subsurface(s,0,centery,centerx+1,bottom-centery,&sub),l)
    _plasma(subsurface(s,centerx,centery,right-centerx,bottom-centery,&sub),l)


extern int blur_junk;


    
