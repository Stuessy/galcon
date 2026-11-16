#include "plasma.h"
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
Uint8 setpixel(SDL_Surface *s, int x, int y, Uint8 c){
#line 11 "plasma.pyp"
    Uint8 *pixels = (Uint8*)s->pixels;
#line 12 "plasma.pyp"
    *(pixels+s->pitch*y+x) = c;
}
#line 14 "plasma.pyp"
Uint8 getpixel(SDL_Surface *s, int x, int y){
#line 15 "plasma.pyp"
    Uint8 *pixels = (Uint8*)s->pixels;
#line 16 "plasma.pyp"
    return *(pixels+s->pitch*y+x);
}
#line 18 "plasma.pyp"
int rr(int min, int max){
#line 19 "plasma.pyp"
    if( (max - min) <= 0) {
#line 20 "plasma.pyp"
        return min;
    }
#line 21 "plasma.pyp"
    int r = (rand()%(max-min))+min;
#line 23 "plasma.pyp"
    return r;
}
#line 25 "plasma.pyp"
#define right (w)
#line 26 "plasma.pyp"
#define bottom (h)
#line 27 "plasma.pyp"
#define centerx (w/2)
#line 28 "plasma.pyp"
#define centery (h/2)
#line 30 "plasma.pyp"
void plasma(PyObject *obj, int l){
#line 31 "plasma.pyp"
    SDL_Surface *s = PySurface_AsSurface(obj);
#line 32 "plasma.pyp"
    int w = s->w;
#line 33 "plasma.pyp"
    int h = s->h;
#line 34 "plasma.pyp"
    setpixel(s,0,0,rr(0,256));
#line 35 "plasma.pyp"
    setpixel(s,right-1,0,rr(0,256));
#line 36 "plasma.pyp"
    setpixel(s,0,bottom-1,rr(0,256));
#line 37 "plasma.pyp"
    setpixel(s,right-1,bottom-1,rr(0,256));
#line 38 "plasma.pyp"
    _plasma(s,l);
}
#line 40 "plasma.pyp"
SDL_Surface *subsurface(SDL_Surface *src,int x,int y,int w,int h,SDL_Surface *dst){
#line 41 "plasma.pyp"
    dst->pixels = (Uint8*)src->pixels+y*src->pitch+x;
#line 42 "plasma.pyp"
    dst->w = w;
#line 43 "plasma.pyp"
    dst->h = h;
#line 44 "plasma.pyp"
    dst->pitch = src->pitch;
#line 45 "plasma.pyp"
    return dst;
}
#line 47 "plasma.pyp"
Uint8 _bound(Sint16 v){
#line 48 "plasma.pyp"
    if( v < 0) {
#line 49 "plasma.pyp"
        return 0;
    }
#line 50 "plasma.pyp"
    if( v > 255) {
#line 51 "plasma.pyp"
        return 255;
    }
#line 52 "plasma.pyp"
    return v;
}
#line 55 "plasma.pyp"
void blur(SDL_Surface *a, SDL_Surface *b){
#line 56 "plasma.pyp"
    for( int y=1;y<a->h-1;y++) {
#line 57 "plasma.pyp"
        int x = 1;
#line 58 "plasma.pyp"
        Uint8 p1,p2,p3,p4,p5,p6,p7,p8,p9;
#line 59 "plasma.pyp"
        p1 = getpixel(a,x-1,y+1);
#line 60 "plasma.pyp"
        p2 = getpixel(a,x+0,y+1);
#line 61 "plasma.pyp"
        p3 = getpixel(a,x+1,y+1);
#line 62 "plasma.pyp"
        p4 = getpixel(a,x-1,y+0);
#line 63 "plasma.pyp"
        p5 = getpixel(a,x+0,y+0);
#line 64 "plasma.pyp"
        p6 = getpixel(a,x+1,y+0);
#line 65 "plasma.pyp"
        p7 = getpixel(a,x-1,y-1);
#line 66 "plasma.pyp"
        p8 = getpixel(a,x+0,y-1);
#line 67 "plasma.pyp"
        p9 = getpixel(a,x+1,y-1);
#line 68 "plasma.pyp"
        for( x=1; x<a->w-1; x++) {
#line 69 "plasma.pyp"
            int v = ((p5)*4+(p8+p4+p6+p2)*2+(p7+p9+p1+p3))/16;
#line 70 "plasma.pyp"
            setpixel(b,x,y,v);
#line 71 "plasma.pyp"
            p1 = p2;
#line 72 "plasma.pyp"
            p2 = p3;
#line 73 "plasma.pyp"
            p3 = getpixel(a,x+1,y+1);
#line 74 "plasma.pyp"
            p4 = p5;
#line 75 "plasma.pyp"
            p5 = p6;
#line 76 "plasma.pyp"
            p6 = getpixel(a,x+1,y+0);
#line 77 "plasma.pyp"
            p7 = p8;
#line 78 "plasma.pyp"
            p8 = p9;
#line 79 "plasma.pyp"
            p9 = getpixel(a,x+1,y-1);
        }
    }
#line 82 "plasma.pyp"
    return;
}
#line 84 "plasma.pyp"
void _plasma(SDL_Surface *s,int l){
#line 85 "plasma.pyp"
    int w = s->w;
#line 86 "plasma.pyp"
    int h = s->h;
#line 87 "plasma.pyp"
    int p7 = getpixel(s,0,0);
#line 88 "plasma.pyp"
    int p9 = getpixel(s,right-1,0);
#line 89 "plasma.pyp"
    int p1 = getpixel(s,0,bottom-1);
#line 90 "plasma.pyp"
    int p3 = getpixel(s,right-1,bottom-1);
#line 92 "plasma.pyp"
    setpixel(s,centerx,0,_bound((p7+p9)/2));
#line 93 "plasma.pyp"
    setpixel(s,0,centery,_bound((p7+p1)/2));
#line 94 "plasma.pyp"
    setpixel(s,centerx,centery,_bound((p7+p9+p1+p3)/4+rr(-l,l)));
#line 95 "plasma.pyp"
    setpixel(s,right-1,centery,_bound((p9+p3)/2));
#line 96 "plasma.pyp"
    setpixel(s,centerx,bottom-1,_bound((p1+p3)/2));
#line 97 "plasma.pyp"
    if( w <= 3 && h <= 3) {
#line 98 "plasma.pyp"
        return;
    }
#line 99 "plasma.pyp"
    SDL_Surface sub;
#line 100 "plasma.pyp"
    l /= 2;
#line 101 "plasma.pyp"
    _plasma(subsurface(s,0,0,centerx+1,centery+1,&sub),l);
#line 102 "plasma.pyp"
    _plasma(subsurface(s,centerx,0,right-centerx,centery+1,&sub),l);
#line 103 "plasma.pyp"
    _plasma(subsurface(s,0,centery,centerx+1,bottom-centery,&sub),l);
#line 104 "plasma.pyp"
    _plasma(subsurface(s,centerx,centery,right-centerx,bottom-centery,&sub),l);
}
#line 107 "plasma.pyp"
extern int blur_junk;;
