%module pixel32
%{
#include "pixel32.h"
%}
#line 1 "pixel32.pyp"
#include "SDL.h"
#line 3 "pixel32.pyp"
#include "def32.h"
#line 5 "pixel32.pyp"
#define PIXEL32_BEGIN      if (x<s->clip_rect.x || y<s->clip_rect.y || x>=(s->clip_rect.x+s->clip_rect.w) || y>=(s->clip_rect.y+s->clip_rect.h)) return;      Uint8 *dstp = (Uint8*)s->pixels + y*s->pitch+x*4;      Uint8 *srcp = (Uint8*)&c;      for (int n=0; n<4; n++) {
#line 11 "pixel32.pyp"
#define PIXEL32_END srcp++; dstp++; }
#line 13 "pixel32.pyp"
extern void pixel32_add(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y);
#line 18 "pixel32.pyp"
extern void pixel32_subtract(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y);
#line 23 "pixel32.pyp"
extern void pixel32_multiply(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y);
#line 28 "pixel32.pyp"
extern void pixel32_min(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y);
#line 33 "pixel32.pyp"
extern void pixel32_max(SDL_Surface *s, Uint32 c,Sint16 x, Sint16 y);
