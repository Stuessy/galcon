#include "parts.h"
#line 1 "parts.pyp"
#include "SDL.h"
#line 2 "parts.pyp"
#include <stdlib.h>
#line 3 "parts.pyp"
#include <math.h>
#line 5 "parts.pyp"
#include "blit32.h"
#line 7 "parts.pyp"
#define PARTS 1024
#line 9 "parts.pyp"
int prr(int min, int max){
#line 10 "parts.pyp"
    if( (max - min) <= 0) {
#line 11 "parts.pyp"
        return min;
    }
#line 12 "parts.pyp"
    int r = (rand()%(max-min))+min;
#line 13 "parts.pyp"
    return r;
}
#line 24 "parts.pyp"
part parts[PARTS];
#line 26 "parts.pyp"
void parts_init(){
#line 27 "parts.pyp"
    for( int n=0; n<PARTS; n++) {
#line 28 "parts.pyp"
        part *p = &parts[n];
#line 29 "parts.pyp"
        p->active = 0;
    }
}
#line 31 "parts.pyp"
part *parts_next(){
#line 32 "parts.pyp"
    static Uint32 cur=0;;
#line 33 "parts.pyp"
    cur++;
#line 34 "parts.pyp"
    return &parts[cur%PARTS];
}
#line 36 "parts.pyp"
void parts_loop(){
#line 37 "parts.pyp"
    for( int n=0; n<PARTS; n++) {
#line 38 "parts.pyp"
        part *p = &parts[n];
#line 39 "parts.pyp"
        if( p->active) {
#line 40 "parts.pyp"
            (p->loop)(p);
        }
    }
}
#line 42 "parts.pyp"
void parts_paint(SDL_Surface *s){
#line 43 "parts.pyp"
    for( int n=0; n<PARTS; n++) {
#line 44 "parts.pyp"
        part *p = &parts[n];
#line 45 "parts.pyp"
        if( p->active) {
#line 46 "parts.pyp"
            SDL_Rect r;
#line 47 "parts.pyp"
            r.x = (int)p->x;
#line 48 "parts.pyp"
            r.y = (int)p->y;
#line 49 "parts.pyp"
            r.x -= p->image->w/2;
#line 50 "parts.pyp"
            r.y -= p->image->h/2;
#line 51 "parts.pyp"
            if( p->alpha == 255) {
#line 52 "parts.pyp"
                blit32_add(p->image,0,s,&r);
            }
#line 53 "parts.pyp"
            else{
#line 55 "parts.pyp"
                p->image->format->alpha = p->alpha;
#line 56 "parts.pyp"
                blit32_add_alpha(p->image,0,s,&r);
            }
        }
    }
}
#line 60 "parts.pyp"
void explode_loop(part *p){
#line 61 "parts.pyp"
    if( p->frame == 32) {
#line 62 "parts.pyp"
        p->active = 0;
    }
#line 63 "parts.pyp"
    p->x += p->vx;
#line 64 "parts.pyp"
    p->y += p->vy;
#line 65 "parts.pyp"
    if( p->frame > 24) {
#line 66 "parts.pyp"
        p->alpha = 255-(p->frame-24)*31;
    }
#line 67 "parts.pyp"
    p->frame ++;
}
#line 70 "parts.pyp"
void explode(float x, float y, float r, int cnt, SDL_Surface *image){
#line 71 "parts.pyp"
    for( int n = 0; n<cnt; n++) {
#line 72 "parts.pyp"
        part *p = parts_next();
#line 73 "parts.pyp"
        p->active = 1;
#line 74 "parts.pyp"
        p->x = x;
#line 75 "parts.pyp"
        p->y = y;
#line 76 "parts.pyp"
        float a = prr(0,360)*M_PI*2.0/360.0;
#line 77 "parts.pyp"
        float v = prr(0,(int)(1.0*100.0))/100.0;
#line 79 "parts.pyp"
        p->vx = v * cos(a);
#line 80 "parts.pyp"
        p->vy = v * sin(a);
#line 82 "parts.pyp"
        float n = prr(0,(int)(r*100))/100.0;
#line 83 "parts.pyp"
        p->x += n*p->vx*cos(a);
#line 84 "parts.pyp"
        p->y += n*p->vy*sin(a);
#line 86 "parts.pyp"
        p->loop = explode_loop;
#line 87 "parts.pyp"
        p->image = image;
#line 88 "parts.pyp"
        p->frame = prr(-5,5);
#line 89 "parts.pyp"
        p->alpha = 255;
    }
}
