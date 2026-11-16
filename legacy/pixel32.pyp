#include "SDL.h"

import def32

#define PIXEL32_BEGIN \
    if (x<s->clip_rect.x || y<s->clip_rect.y || x>=(s->clip_rect.x+s->clip_rect.w) || y>=(s->clip_rect.y+s->clip_rect.h)) return; \
    Uint8 *dstp = (Uint8*)s->pixels + y*s->pitch+x*4; \
    Uint8 *srcp = (Uint8*)&c; \
    for (int n=0; n<4; n++) { 
    
#define PIXEL32_END srcp++; dstp++; }

void pixel32_add(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y):
    PIXEL32_BEGIN
    DEF32_ADD(srcp,dstp)
    PIXEL32_END
    
void pixel32_subtract(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y):
    PIXEL32_BEGIN
    DEF32_SUBTRACT(srcp,dstp)
    PIXEL32_END

void pixel32_multiply(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y):
    PIXEL32_BEGIN
    DEF32_MULTIPLY(srcp,dstp)
    PIXEL32_END

void pixel32_min(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y):
    PIXEL32_BEGIN
    DEF32_MIN(srcp,dstp)
    PIXEL32_END

void pixel32_max(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y):
    PIXEL32_BEGIN
    DEF32_MAX(srcp,dstp)
    PIXEL32_END

