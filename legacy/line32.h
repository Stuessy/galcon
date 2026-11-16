#ifndef LINE32_H
#define LINE32_H
#line 1 "line32.pyp"
#include "SDL.h"
#line 3 "line32.pyp"
#include "pixel32.h"
#line 5 "line32.pyp"
typedef void (*_line32_pixel)(SDL_Surface *s,Uint32 c,Sint16 x, Sint16 y);;
#line 7 "line32.pyp"
extern void _line32_draw(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2,_line32_pixel pixel);
#line 51 "line32.pyp"
extern void line32_add(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2);
#line 54 "line32.pyp"
extern void line32_subtract(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2);
#line 57 "line32.pyp"
extern void line32_multiply(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2);
#line 60 "line32.pyp"
extern void line32_min(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2);
#line 63 "line32.pyp"
extern void line32_max(SDL_Surface *s,Uint32 clr,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2);
#line 67 "line32.pyp"
#define PIXEL32_BEGIN_INLINE      Uint8 *dstp = (Uint8*)s->pixels + y*s->pitch+x*4;      Uint8 *srcp = (Uint8*)&c;      for (int pn=0; pn<4; pn++) {
#line 72 "line32.pyp"
#define PIXEL32_END_INLINE srcp++; dstp++; }
#line 74 "line32.pyp"
extern void line32_add_inline(SDL_Surface *s,Uint32 c,Sint16 x1,Sint16 y1,Sint16 x2,Sint16 y2);
#endif
