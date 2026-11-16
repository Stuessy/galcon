#include "SDL.h"

import def32

int _blit32_bound(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect, SDL_Rect *srcrect, SDL_Rect *dstrect):
    // you may pass in null for _srcrect
    // but it better be a valid _srcrect
    // because i don't want to check it
    if _srcrect:
        *srcrect = *_srcrect
    else:
        srcrect->x = 0
        srcrect->y = 0
        srcrect->w = src->w
        srcrect->h = src->h
    // you may pass in null for _dstrect
    // however, if it is off screen, or whatever
    // i'll do my best to keep from writing 
    // out of bounds data
    if _dstrect:
        *dstrect = *_dstrect
    else:
        dstrect->x = 0
        dstrect->y = 0
        dstrect->w = 0
        dstrect->h = 0
    
    int diff
    diff = dst->clip_rect.x - dstrect->x 
    if diff > 0:
        dstrect->x += diff
        srcrect->x += diff
        if diff >= srcrect->w:
            return 1
        srcrect->w -= diff
    diff = dst->clip_rect.y - dstrect->y
    if diff > 0:
        dstrect->y += diff
        srcrect->y += diff
        if diff >= srcrect->h:
            return 1
        srcrect->h -= diff
    diff = (dstrect->x+srcrect->w) - (dst->clip_rect.x+dst->clip_rect.w)
    if diff > 0:
        if diff >= srcrect->w:
            return 1
        srcrect->w -= diff
    diff = (dstrect->y+srcrect->h) - (dst->clip_rect.y+dst->clip_rect.h)
    if diff > 0:
        if diff >= srcrect->h:
            return 1
        srcrect->h -= diff
    return 0
    
#define BLIT32_BEGIN  SDL_Rect srcrect,dstrect; \
    if (_blit32_bound(src,_srcrect,dst,_dstrect,&srcrect,&dstrect)) return; \
    for( int y=srcrect.y; y<srcrect.y+srcrect.h; y++) { \
        Uint8 *srcp = (Uint8*)src->pixels+y*src->pitch+srcrect.x*4; \
        Uint8 *dstp = (Uint8*)dst->pixels+(dstrect.y+y-srcrect.y)*dst->pitch+dstrect.x*4; \
        for( int x=srcrect.x*4; x<(srcrect.x+srcrect.w)*4; x++) {
#define BLIT32_END srcp++; dstp++; } }

void blit32_add(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect):
    BLIT32_BEGIN
    DEF32_ADD(srcp,dstp)
    BLIT32_END

void blit32_add_alpha(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect):
    Uint8 alpha = src->format->alpha
    BLIT32_BEGIN
    DEF32_ADD_ALPHA(srcp,dstp)
    BLIT32_END

void blit32_subtract(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect):
    BLIT32_BEGIN
    DEF32_SUBTRACT(srcp,dstp)
    BLIT32_END

void blit32_multiply(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect):
    BLIT32_BEGIN
    DEF32_MULTIPLY(srcp,dstp)
    BLIT32_END


void blit32_min(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect):
    BLIT32_BEGIN
    DEF32_MIN(srcp,dstp)
    BLIT32_END


void blit32_max(SDL_Surface *src, SDL_Rect *_srcrect, SDL_Surface *dst, SDL_Rect *_dstrect):
    BLIT32_BEGIN
    DEF32_MAX(srcp,dstp)
    BLIT32_END

