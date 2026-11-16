#include "pixel32.h"
#line 1 "pixel32.pyp"
#include "SDL.h"
#line 3 "pixel32.pyp"
#include "def32.h"
#line 5 "pixel32.pyp"
#define PIXEL32_BEGIN      if (x<s->clip_rect.x || y<s->clip_rect.y || x>=(s->clip_rect.x+s->clip_rect.w) || y>=(s->clip_rect.y+s->clip_rect.h)) return;      Uint8 *dstp = (Uint8*)s->pixels + y*s->pitch+x*4;      Uint8 *srcp = (Uint8*)&c;      for (int n=0; n<4; n++) {
#line 11 "pixel32.pyp"
#define PIXEL32_END srcp++; dstp++; }
#line 13 "pixel32.pyp"
void pixel32_add(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y){
#line 14 "pixel32.pyp"
    PIXEL32_BEGIN;
#line 15 "pixel32.pyp"
    DEF32_ADD(srcp,dstp);
#line 16 "pixel32.pyp"
    PIXEL32_END;
}
#line 18 "pixel32.pyp"
void pixel32_subtract(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y){
#line 19 "pixel32.pyp"
    PIXEL32_BEGIN;
#line 20 "pixel32.pyp"
    DEF32_SUBTRACT(srcp,dstp);
#line 21 "pixel32.pyp"
    PIXEL32_END;
}
#line 23 "pixel32.pyp"
void pixel32_multiply(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y){
#line 24 "pixel32.pyp"
    PIXEL32_BEGIN;
#line 25 "pixel32.pyp"
    DEF32_MULTIPLY(srcp,dstp);
#line 26 "pixel32.pyp"
    PIXEL32_END;
}
#line 28 "pixel32.pyp"
void pixel32_min(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y){
#line 29 "pixel32.pyp"
    PIXEL32_BEGIN;
#line 30 "pixel32.pyp"
    DEF32_MIN(srcp,dstp);
#line 31 "pixel32.pyp"
    PIXEL32_END;
}
#line 33 "pixel32.pyp"
void pixel32_max(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y){
#line 34 "pixel32.pyp"
    PIXEL32_BEGIN;
#line 35 "pixel32.pyp"
    DEF32_MAX(srcp,dstp);
#line 36 "pixel32.pyp"
    PIXEL32_END;
}
