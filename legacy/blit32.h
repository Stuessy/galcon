#ifndef BLIT32_H
#define BLIT32_H
#line 1 "blit32.pyp"
#include "SDL.h"
#line 3 "blit32.pyp"
#include "def32.h"
#line 5 "blit32.pyp"
extern int _blit32_bound(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect, SDL_Rect *srcrect, SDL_Rect *dstrect);
#line 55 "blit32.pyp"
#define BLIT32_BEGIN  SDL_Rect srcrect,dstrect;      if (_blit32_bound(src,_srcrect,dst,_dstrect,&srcrect,&dstrect)) return;      for( int y=srcrect.y; y<srcrect.y+srcrect.h; y++) {          Uint8 *srcp = (Uint8*)src->pixels+y*src->pitch+srcrect.x*4;          Uint8 *dstp = (Uint8*)dst->pixels+(dstrect.y+y-srcrect.y)*dst->pitch+dstrect.x*4;          for( int x=srcrect.x*4; x<(srcrect.x+srcrect.w)*4; x++) {
#line 61 "blit32.pyp"
#define BLIT32_END srcp++; dstp++; } }
#line 63 "blit32.pyp"
extern void blit32_add(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect);
#line 68 "blit32.pyp"
extern void blit32_add_alpha(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect);
#line 74 "blit32.pyp"
extern void blit32_subtract(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect);
#line 79 "blit32.pyp"
extern void blit32_multiply(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect);
#line 85 "blit32.pyp"
extern void blit32_min(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect);
#line 91 "blit32.pyp"
extern void blit32_max(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect);
#endif
