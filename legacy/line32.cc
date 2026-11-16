#include "line32.h"
#line 1 "line32.pyp"
#include "SDL.h"
#line 3 "line32.pyp"
#include "pixel32.h"
#line 7 "line32.pyp"
void _line32_draw(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2,_line32_pixel pixel){
#line 8 "line32.pyp"
    Sint16 dx,dy,xi1,xi2,yi1,yi2,d,n,a,p,x,y,c;;
#line 9 "line32.pyp"
    dx = x2-x1;
#line 10 "line32.pyp"
    if( (dx<0)) {
#line 11 "line32.pyp"
        dx = -dx;;
    }
#line 12 "line32.pyp"
    dy = y2-y1;;
#line 13 "line32.pyp"
    if( (dy<0)) {
#line 14 "line32.pyp"
        dy = -dy;;
    }
#line 15 "line32.pyp"
    if( (x2 >= x1)) {
#line 16 "line32.pyp"
        xi1=1; xi2=1;;
    }
#line 17 "line32.pyp"
    else{
#line 18 "line32.pyp"
        xi1=-1;xi2=-1;;
    }
#line 20 "line32.pyp"
    if( (y2 >= y1)) {
#line 21 "line32.pyp"
        yi1=1;yi2=1;;
    }
#line 22 "line32.pyp"
    else{
#line 23 "line32.pyp"
        yi1=-1;yi2=-1;;
    }
#line 25 "line32.pyp"
    if( (dx >= dy)) {
#line 26 "line32.pyp"
        xi1=0;yi2 = 0;;
#line 27 "line32.pyp"
        d = dx;
#line 28 "line32.pyp"
        n = dx/2;
#line 29 "line32.pyp"
        a = dy;
#line 30 "line32.pyp"
        p = dx;
    }
#line 31 "line32.pyp"
    else{
#line 32 "line32.pyp"
        xi2=0;yi1 = 0;;
#line 33 "line32.pyp"
        d = dy;
#line 34 "line32.pyp"
        n = dy/2;
#line 35 "line32.pyp"
        a = dx;
#line 36 "line32.pyp"
        p = dy;
    }
#line 38 "line32.pyp"
    x=x1;y=y1;;
#line 39 "line32.pyp"
    c = 0;
#line 40 "line32.pyp"
    while( (c <= p)) {
#line 41 "line32.pyp"
        pixel(s,clr,x,y);
#line 42 "line32.pyp"
        n += a;
#line 43 "line32.pyp"
        if( (n > d)) {
#line 44 "line32.pyp"
            n -= d;
#line 45 "line32.pyp"
            x += xi1;
#line 46 "line32.pyp"
            y += yi1;
        }
#line 47 "line32.pyp"
        x += xi2;
#line 48 "line32.pyp"
        y += yi2;
#line 49 "line32.pyp"
        c += 1;
    }
}
#line 51 "line32.pyp"
void line32_add(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2){
#line 52 "line32.pyp"
    _line32_draw(s,clr,x1,y1,x2,y2,pixel32_add);
}
#line 54 "line32.pyp"
void line32_subtract(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2){
#line 55 "line32.pyp"
    _line32_draw(s,clr,x1,y1,x2,y2,pixel32_subtract);
}
#line 57 "line32.pyp"
void line32_multiply(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2){
#line 58 "line32.pyp"
    _line32_draw(s,clr,x1,y1,x2,y2,pixel32_multiply);
}
#line 60 "line32.pyp"
void line32_min(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2){
#line 61 "line32.pyp"
    _line32_draw(s,clr,x1,y1,x2,y2,pixel32_min);
}
#line 63 "line32.pyp"
void line32_max(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2){
#line 64 "line32.pyp"
    _line32_draw(s,clr,x1,y1,x2,y2,pixel32_max);
}
#line 67 "line32.pyp"
#define PIXEL32_BEGIN_INLINE      Uint8 *dstp = (Uint8*)s->pixels + y*s->pitch+x*4;      Uint8 *srcp = (Uint8*)&c;      for (int pn=0; pn<4; pn++) {
#line 72 "line32.pyp"
#define PIXEL32_END_INLINE srcp++; dstp++; }
#line 74 "line32.pyp"
void line32_add_inline(SDL_Surface *s,Uint32 c,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2){
#line 75 "line32.pyp"
    Sint16 dx,dy,xi1,xi2,yi1,yi2,d,n,a,p,x,y,cz;;
#line 77 "line32.pyp"
    x = x1;
#line 78 "line32.pyp"
    y = y1;
#line 79 "line32.pyp"
    if (x<s->clip_rect.x || y<s->clip_rect.y || x>=(s->clip_rect.x+s->clip_rect.w) || y>=(s->clip_rect.y+s->clip_rect.h)) return;;
#line 80 "line32.pyp"
    x = x2;
#line 81 "line32.pyp"
    y = y2;
#line 82 "line32.pyp"
    if (x<s->clip_rect.x || y<s->clip_rect.y || x>=(s->clip_rect.x+s->clip_rect.w) || y>=(s->clip_rect.y+s->clip_rect.h)) return;;
#line 85 "line32.pyp"
    dx = x2-x1;
#line 86 "line32.pyp"
    if( (dx<0)) {
#line 87 "line32.pyp"
        dx = -dx;;
    }
#line 88 "line32.pyp"
    dy = y2-y1;;
#line 89 "line32.pyp"
    if( (dy<0)) {
#line 90 "line32.pyp"
        dy = -dy;;
    }
#line 91 "line32.pyp"
    if( (x2 >= x1)) {
#line 92 "line32.pyp"
        xi1=1; xi2=1;;
    }
#line 93 "line32.pyp"
    else{
#line 94 "line32.pyp"
        xi1=-1;xi2=-1;;
    }
#line 96 "line32.pyp"
    if( (y2 >= y1)) {
#line 97 "line32.pyp"
        yi1=1;yi2=1;;
    }
#line 98 "line32.pyp"
    else{
#line 99 "line32.pyp"
        yi1=-1;yi2=-1;;
    }
#line 101 "line32.pyp"
    if( (dx >= dy)) {
#line 102 "line32.pyp"
        xi1=0;yi2 = 0;;
#line 103 "line32.pyp"
        d = dx;
#line 104 "line32.pyp"
        n = dx/2;
#line 105 "line32.pyp"
        a = dy;
#line 106 "line32.pyp"
        p = dx;
    }
#line 107 "line32.pyp"
    else{
#line 108 "line32.pyp"
        xi2=0;yi1 = 0;;
#line 109 "line32.pyp"
        d = dy;
#line 110 "line32.pyp"
        n = dy/2;
#line 111 "line32.pyp"
        a = dx;
#line 112 "line32.pyp"
        p = dy;
    }
#line 114 "line32.pyp"
    x=x1;y=y1;;
#line 115 "line32.pyp"
    cz = 0;
#line 116 "line32.pyp"
    while( (cz <= p)) {
#line 117 "line32.pyp"
        PIXEL32_BEGIN_INLINE;
#line 118 "line32.pyp"
        DEF32_ADD(srcp,dstp);
#line 119 "line32.pyp"
        PIXEL32_END_INLINE;
#line 121 "line32.pyp"
        n += a;
#line 122 "line32.pyp"
        if( (n > d)) {
#line 123 "line32.pyp"
            n -= d;
#line 124 "line32.pyp"
            x += xi1;
#line 125 "line32.pyp"
            y += yi1;
        }
#line 126 "line32.pyp"
        x += xi2;
#line 127 "line32.pyp"
        y += yi2;
#line 128 "line32.pyp"
        cz += 1;
    }
}
