#include "SDL.h"

import pixel32

typedef void (*_line32_pixel)(SDL_Surface *s,Uint32 c,Sint16 x, Sint16 y);

void _line32_draw(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2,_line32_pixel pixel):
    Sint16 dx,dy,xi1,xi2,yi1,yi2,d,n,a,p,x,y,c;
    dx = x2-x1
    if (dx<0):
        dx = -dx;
    dy = y2-y1; 
    if (dy<0):
        dy = -dy;
    if (x2 >= x1):
        xi1=1; xi2=1;
    else: 
        xi1=-1;xi2=-1;
    
    if (y2 >= y1):
        yi1=1;yi2=1;
    else: 
        yi1=-1;yi2=-1;
    
    if (dx >= dy):
        xi1=0;yi2 = 0;
        d = dx
        n = dx/2
        a = dy
        p = dx
    else:
        xi2=0;yi1 = 0;
        d = dy
        n = dy/2
        a = dx
        p = dy
        
    x=x1;y=y1;
    c = 0
    while (c <= p):
        pixel(s,clr,x,y)
        n += a
        if (n > d): 
            n -= d
            x += xi1
            y += yi1
        x += xi2
        y += yi2
        c += 1

void line32_add(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2):
    _line32_draw(s,clr,x1,y1,x2,y2,pixel32_add)
    
void line32_subtract(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2):
    _line32_draw(s,clr,x1,y1,x2,y2,pixel32_subtract)

void line32_multiply(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2):
    _line32_draw(s,clr,x1,y1,x2,y2,pixel32_multiply)

void line32_min(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2):
    _line32_draw(s,clr,x1,y1,x2,y2,pixel32_min)

void line32_max(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2):
    _line32_draw(s,clr,x1,y1,x2,y2,pixel32_max)


#define PIXEL32_BEGIN_INLINE \
    Uint8 *dstp = (Uint8*)s->pixels + y*s->pitch+x*4; \
    Uint8 *srcp = (Uint8*)&c; \
    for (int pn=0; pn<4; pn++) { 
    
#define PIXEL32_END_INLINE srcp++; dstp++; }

void line32_add_inline(SDL_Surface *s,Uint32 c,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2):
    Sint16 dx,dy,xi1,xi2,yi1,yi2,d,n,a,p,x,y,cz;
    
    x = x1
    y = y1
    if (x<s->clip_rect.x || y<s->clip_rect.y || x>=(s->clip_rect.x+s->clip_rect.w) || y>=(s->clip_rect.y+s->clip_rect.h)) return;
    x = x2
    y = y2
    if (x<s->clip_rect.x || y<s->clip_rect.y || x>=(s->clip_rect.x+s->clip_rect.w) || y>=(s->clip_rect.y+s->clip_rect.h)) return;

    
    dx = x2-x1
    if (dx<0):
        dx = -dx;
    dy = y2-y1; 
    if (dy<0):
        dy = -dy;
    if (x2 >= x1):
        xi1=1; xi2=1;
    else: 
        xi1=-1;xi2=-1;
    
    if (y2 >= y1):
        yi1=1;yi2=1;
    else: 
        yi1=-1;yi2=-1;
    
    if (dx >= dy):
        xi1=0;yi2 = 0;
        d = dx
        n = dx/2
        a = dy
        p = dx
    else:
        xi2=0;yi1 = 0;
        d = dy
        n = dy/2
        a = dx
        p = dy
        
    x=x1;y=y1;
    cz = 0
    while (cz <= p):
        PIXEL32_BEGIN_INLINE
        DEF32_ADD(srcp,dstp)
        PIXEL32_END_INLINE
        
        n += a
        if (n > d): 
            n -= d
            x += xi1
            y += yi1
        x += xi2
        y += yi2
        cz += 1
