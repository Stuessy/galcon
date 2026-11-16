#include "blit32.h"
#line 1 "blit32.pyp"
#include "SDL.h"
#line 3 "blit32.pyp"
#include "def32.h"
#line 5 "blit32.pyp"
int _blit32_bound(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect, SDL_Rect *srcrect, SDL_Rect *dstrect){
#line 9 "blit32.pyp"
    if( _srcrect) {
#line 10 "blit32.pyp"
        *srcrect = *_srcrect;
    }
#line 11 "blit32.pyp"
    else{
#line 12 "blit32.pyp"
        srcrect->x = 0;
#line 13 "blit32.pyp"
        srcrect->y = 0;
#line 14 "blit32.pyp"
        srcrect->w = src->w;
#line 15 "blit32.pyp"
        srcrect->h = src->h;
    }
#line 20 "blit32.pyp"
    if( _dstrect) {
#line 21 "blit32.pyp"
        *dstrect = *_dstrect;
    }
#line 22 "blit32.pyp"
    else{
#line 23 "blit32.pyp"
        dstrect->x = 0;
#line 24 "blit32.pyp"
        dstrect->y = 0;
#line 25 "blit32.pyp"
        dstrect->w = 0;
#line 26 "blit32.pyp"
        dstrect->h = 0;
    }
#line 28 "blit32.pyp"
    int diff;
#line 29 "blit32.pyp"
    diff = dst->clip_rect.x - dstrect->x;
#line 30 "blit32.pyp"
    if( diff > 0) {
#line 31 "blit32.pyp"
        dstrect->x += diff;
#line 32 "blit32.pyp"
        srcrect->x += diff;
#line 33 "blit32.pyp"
        if( diff >= srcrect->w) {
#line 34 "blit32.pyp"
            return 1;
        }
#line 35 "blit32.pyp"
        srcrect->w -= diff;
    }
#line 36 "blit32.pyp"
    diff = dst->clip_rect.y - dstrect->y;
#line 37 "blit32.pyp"
    if( diff > 0) {
#line 38 "blit32.pyp"
        dstrect->y += diff;
#line 39 "blit32.pyp"
        srcrect->y += diff;
#line 40 "blit32.pyp"
        if( diff >= srcrect->h) {
#line 41 "blit32.pyp"
            return 1;
        }
#line 42 "blit32.pyp"
        srcrect->h -= diff;
    }
#line 43 "blit32.pyp"
    diff = (dstrect->x+srcrect->w) - (dst->clip_rect.x+dst->clip_rect.w);
#line 44 "blit32.pyp"
    if( diff > 0) {
#line 45 "blit32.pyp"
        if( diff >= srcrect->w) {
#line 46 "blit32.pyp"
            return 1;
        }
#line 47 "blit32.pyp"
        srcrect->w -= diff;
    }
#line 48 "blit32.pyp"
    diff = (dstrect->y+srcrect->h) - (dst->clip_rect.y+dst->clip_rect.h);
#line 49 "blit32.pyp"
    if( diff > 0) {
#line 50 "blit32.pyp"
        if( diff >= srcrect->h) {
#line 51 "blit32.pyp"
            return 1;
        }
#line 52 "blit32.pyp"
        srcrect->h -= diff;
    }
#line 53 "blit32.pyp"
    return 0;
}
#line 55 "blit32.pyp"
#define BLIT32_BEGIN  SDL_Rect srcrect,dstrect;      if (_blit32_bound(src,_srcrect,dst,_dstrect,&srcrect,&dstrect)) return;      for( int y=srcrect.y; y<srcrect.y+srcrect.h; y++) {          Uint8 *srcp = (Uint8*)src->pixels+y*src->pitch+srcrect.x*4;          Uint8 *dstp = (Uint8*)dst->pixels+(dstrect.y+y-srcrect.y)*dst->pitch+dstrect.x*4;          for( int x=srcrect.x*4; x<(srcrect.x+srcrect.w)*4; x++) {
#line 61 "blit32.pyp"
#define BLIT32_END srcp++; dstp++; } }
#line 63 "blit32.pyp"
void blit32_add(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect){
#line 64 "blit32.pyp"
    BLIT32_BEGIN;
#line 65 "blit32.pyp"
    DEF32_ADD(srcp,dstp);
#line 66 "blit32.pyp"
    BLIT32_END;
}
#line 68 "blit32.pyp"
void blit32_add_alpha(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect){
#line 69 "blit32.pyp"
    Uint8 alpha = src->format->alpha;
#line 70 "blit32.pyp"
    BLIT32_BEGIN;
#line 71 "blit32.pyp"
    DEF32_ADD_ALPHA(srcp,dstp);
#line 72 "blit32.pyp"
    BLIT32_END;
}
#line 74 "blit32.pyp"
void blit32_subtract(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect){
#line 75 "blit32.pyp"
    BLIT32_BEGIN;
#line 76 "blit32.pyp"
    DEF32_SUBTRACT(srcp,dstp);
#line 77 "blit32.pyp"
    BLIT32_END;
}
#line 79 "blit32.pyp"
void blit32_multiply(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect){
#line 80 "blit32.pyp"
    BLIT32_BEGIN;
#line 81 "blit32.pyp"
    DEF32_MULTIPLY(srcp,dstp);
#line 82 "blit32.pyp"
    BLIT32_END;
}
#line 85 "blit32.pyp"
void blit32_min(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect){
#line 86 "blit32.pyp"
    BLIT32_BEGIN;
#line 87 "blit32.pyp"
    DEF32_MIN(srcp,dstp);
#line 88 "blit32.pyp"
    BLIT32_END;
}
#line 91 "blit32.pyp"
void blit32_max(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect){
#line 92 "blit32.pyp"
    BLIT32_BEGIN;
#line 93 "blit32.pyp"
    DEF32_MAX(srcp,dstp);
#line 94 "blit32.pyp"
    BLIT32_END;
}
