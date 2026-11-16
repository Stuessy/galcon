#include "SDL.h"
#include <stdlib.h>
#include <math.h>

import blit32

#define PARTS 1024

int prr(int min, int max):
    if (max - min) <= 0:
        return min
    int r = (rand()%(max-min))+min
    return r


struct part:
    float x,y,vx,vy
    int active
    Uint8 alpha
    SDL_Surface *image
    int frame
    void (*loop)(part *)

export part parts[PARTS]

void parts_init():
    for int n=0; n<PARTS; n++:
        part *p = &parts[n]
        p->active = 0

part *parts_next():
    static Uint32 cur=0;
    cur++
    return &parts[cur%PARTS]
    
void parts_loop():
    for int n=0; n<PARTS; n++:
        part *p = &parts[n]
        if p->active:
            (p->loop)(p)
        
void parts_paint(SDL_Surface *s):
    for int n=0; n<PARTS; n++:
        part *p = &parts[n]
        if p->active:
            SDL_Rect r
            r.x = (int)p->x
            r.y = (int)p->y
            r.x -= p->image->w/2
            r.y -= p->image->h/2
            if p->alpha == 255:
                blit32_add(p->image,0,s,&r)
            else:
                //SDL_SetAlpha(p->image,0,p->alpha)
                p->image->format->alpha = p->alpha
                blit32_add_alpha(p->image,0,s,&r)
    
    
    
void explode_loop(part *p):
    if p->frame == 32:
        p->active = 0
    p->x += p->vx
    p->y += p->vy
    if p->frame > 24:
        p->alpha = 255-(p->frame-24)*31
    p->frame ++
    
    
void explode(float x, float y, float r, int cnt, SDL_Surface *image):
    for int n = 0; n<cnt; n++:
        part *p = parts_next()
        p->active = 1
        p->x = x
        p->y = y
        float a = prr(0,360)*M_PI*2.0/360.0
        float v = prr(0,(int)(1.0*100.0))/100.0
        
        p->vx = v * cos(a)
        p->vy = v * sin(a)
        
        float n = prr(0,(int)(r*100))/100.0
        p->x += n*p->vx*cos(a)
        p->y += n*p->vy*sin(a)
        
        p->loop = explode_loop
        p->image = image
        p->frame = prr(-5,5)
        p->alpha = 255
