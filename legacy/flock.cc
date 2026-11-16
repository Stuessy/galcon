#include "flock.h"
#line 1 "flock.pyp"
#include "SDL.h"
#line 2 "flock.pyp"
#include <stdlib.h>
#line 3 "flock.pyp"
#include <math.h>
#line 5 "flock.pyp"
#include "blit32.h"
#line 6 "flock.pyp"
#include "line32.h"
#line 8 "flock.pyp"
#define SW 640
#line 9 "flock.pyp"
#define SH 480
#line 11 "flock.pyp"
#define FPARTS 4096
#line 13 "flock.pyp"
#define COS(v) (cos(v))
#line 14 "flock.pyp"
#define SIN(v) (sin(v))
#line 15 "flock.pyp"
#define ABS(v) ((v)<0?(-v):(v))
#line 17 "flock.pyp"
#define TYPE_PLANET 1
#line 18 "flock.pyp"
#define TYPE_SHIP 2
#line 33 "flock.pyp"
fpart fparts[FPARTS];
#line 35 "flock.pyp"
void flock_init(){
#line 36 "flock.pyp"
    for( int n=0; n<FPARTS; n++) {
#line 37 "flock.pyp"
        fpart *p = &fparts[n];
#line 38 "flock.pyp"
        p->active = 0;
    }
}
#line 40 "flock.pyp"
fpart *flock_next(){
#line 41 "flock.pyp"
    static Uint32 cur=0;;
#line 42 "flock.pyp"
    for( int n = 0; n<FPARTS; n++) {
#line 43 "flock.pyp"
        fpart *p = &fparts[cur%FPARTS];
#line 44 "flock.pyp"
        cur ++;
#line 45 "flock.pyp"
        if( !p->active) {
#line 46 "flock.pyp"
            return p;
        }
    }
#line 47 "flock.pyp"
    printf ("uh oh! ran out of fparts!  $10 says things are gonna crash!\n");
#line 48 "flock.pyp"
    return 0;
}
#line 50 "flock.pyp"
void flock_loop(){
#line 52 "flock.pyp"
    for( int n=0; n<FPARTS; n++) {
#line 53 "flock.pyp"
        fpart *p = &fparts[n];
#line 54 "flock.pyp"
        if( p->active and p->loop) {
#line 55 "flock.pyp"
            if( p->type == TYPE_SHIP) {
#line 56 "flock.pyp"
                ship_loop(p);
            }
#line 57 "flock.pyp"
            else{
#line 58 "flock.pyp"
                (p->loop)(p);
            }
        }
    }
#line 61 "flock.pyp"
    for( int n=0; n<FPARTS; n++) {
#line 62 "flock.pyp"
        fpart *a = &fparts[n];
#line 63 "flock.pyp"
        if( a->active) {
#line 64 "flock.pyp"
            for( int m=n+1; m<FPARTS; m++) {
#line 65 "flock.pyp"
                fpart *b = &fparts[m];
#line 66 "flock.pyp"
                if( b->active) {
#line 67 "flock.pyp"
                    float dx,dy;
#line 68 "flock.pyp"
                    dx = b->x-a->x;
#line 69 "flock.pyp"
                    dy = b->y-a->y;
#line 70 "flock.pyp"
                    float r = a->r+b->r;
#line 71 "flock.pyp"
                    if( ABS(dx) > r or ABS(dy) > r) {
#line 72 "flock.pyp"
                        continue;
                    }
#line 74 "flock.pyp"
                    float dist = sqrt(dx*dx+dy*dy);
#line 77 "flock.pyp"
                    if( dist < r) {
#line 78 "flock.pyp"
                        if( a->target == b) {
#line 79 "flock.pyp"
                            a->hit = 1;
#line 80 "flock.pyp"
                            continue;
                        }
#line 81 "flock.pyp"
                        if( b->target == a) {
#line 82 "flock.pyp"
                            b->hit = 1;
#line 83 "flock.pyp"
                            continue;
                        }
#line 85 "flock.pyp"
                        if( dist == 0) {
#line 86 "flock.pyp"
                            dist = 1;
#line 87 "flock.pyp"
                            dx = 1;
                        }
#line 88 "flock.pyp"
                        float w = a->w+b->w;
#line 91 "flock.pyp"
                        float mx = (a->x*a->w+b->x*b->w)/w;
#line 92 "flock.pyp"
                        float my = (a->y*a->w+b->y*b->w)/w;
#line 94 "flock.pyp"
                        a->x = mx-(r*b->w*dx)/(dist*w);
#line 95 "flock.pyp"
                        a->y = my-(r*b->w*dy)/(dist*w);
#line 97 "flock.pyp"
                        b->x = mx+(r*a->w*dx)/(dist*w);
#line 98 "flock.pyp"
                        b->y = my+(r*a->w*dy)/(dist*w);
                    }
                }
            }
        }
    }
}
#line 105 "flock.pyp"
void flock_paint(SDL_Surface *s){
#line 106 "flock.pyp"
    for( int n=0; n<FPARTS; n++) {
#line 107 "flock.pyp"
        fpart *p = &fparts[n];
#line 108 "flock.pyp"
        if( p->active and p->paint) {
#line 115 "flock.pyp"
            (p->paint)(p,s);
        }
    }
}
#line 117 "flock.pyp"
fpart *planet_new(float x,float y, float r){
#line 118 "flock.pyp"
    fpart *p = flock_next();
#line 119 "flock.pyp"
    p->x = x;
#line 120 "flock.pyp"
    p->y = y;
#line 121 "flock.pyp"
    p->r = r;
#line 122 "flock.pyp"
    p->active = 1;
#line 123 "flock.pyp"
    p->loop = 0;
#line 124 "flock.pyp"
    p->paint = 0;
#line 125 "flock.pyp"
    p->w = 100000;
#line 126 "flock.pyp"
    p->type = TYPE_PLANET;
#line 127 "flock.pyp"
    p->target = 0;
#line 128 "flock.pyp"
    p->hit = 0;
#line 129 "flock.pyp"
    return p;
}
#line 131 "flock.pyp"
void planet_loop(fpart *p){
#line 132 "flock.pyp"
    if( p->x < 0) {
#line 133 "flock.pyp"
        p->x = 0;
    }
#line 134 "flock.pyp"
    if( p->y < 0) {
#line 135 "flock.pyp"
        p->y = 0;
    }
#line 137 "flock.pyp"
    if( p->x > SW) {
#line 138 "flock.pyp"
        p->x = SW;
    }
#line 139 "flock.pyp"
    if( p->y > SH) {
#line 140 "flock.pyp"
        p->y = SH;
    }
}
#line 143 "flock.pyp"
fpart *ship_new(float x, float y, float tx, float ty,fpart *target, int color){
#line 144 "flock.pyp"
    fpart *p = flock_next();
#line 145 "flock.pyp"
    p->x = x;
#line 146 "flock.pyp"
    p->y = y;
#line 147 "flock.pyp"
    p->tx = tx;
#line 148 "flock.pyp"
    p->ty = ty;
#line 149 "flock.pyp"
    p->v = 2.0;
#line 150 "flock.pyp"
    p->r = 6;
#line 151 "flock.pyp"
    p->active = 1;
#line 152 "flock.pyp"
    p->loop = ship_loop;
#line 153 "flock.pyp"
    p->paint = ship_paint;
#line 154 "flock.pyp"
    p->color = color;
#line 155 "flock.pyp"
    p->w = 1.0;
#line 156 "flock.pyp"
    p->type = TYPE_SHIP;
#line 157 "flock.pyp"
    p->target = target;
#line 158 "flock.pyp"
    p->hit = 0;
#line 159 "flock.pyp"
    return p;
}
#line 161 "flock.pyp"
void ship_loop(fpart *p){
#line 162 "flock.pyp"
    p->_x = p->x;
#line 163 "flock.pyp"
    p->_y = p->y;
#line 164 "flock.pyp"
    float dx,dy;
#line 165 "flock.pyp"
    dx = p->tx-p->x;
#line 166 "flock.pyp"
    dy = p->ty-p->y;
#line 167 "flock.pyp"
    float a = atan2(dy,dx);
#line 168 "flock.pyp"
    p->a = a;
#line 170 "flock.pyp"
    p->vx = cos(a);
#line 171 "flock.pyp"
    p->vy = sin(a);
#line 172 "flock.pyp"
    p->x += p->vx*p->v;
#line 173 "flock.pyp"
    p->y += p->vy*p->v;
}
#line 175 "flock.pyp"
#define SHIP_FRONT 6
#line 176 "flock.pyp"
#define SHIP_SIDE 3
#line 177 "flock.pyp"
#define SHIP_BACK 3
#line 179 "flock.pyp"
void ship_paint(fpart *p,SDL_Surface *s){
#line 181 "flock.pyp"
    float x,y,vx,vy;
#line 182 "flock.pyp"
    float x1,y1,x2,y2,x3,y3;
#line 183 "flock.pyp"
    x = p->x;
#line 184 "flock.pyp"
    y = p->y;
#line 189 "flock.pyp"
    float a = atan2(p->y-p->_y,p->x-p->_x);
#line 190 "flock.pyp"
    vx = cos(a);
#line 191 "flock.pyp"
    vy = sin(a);
#line 192 "flock.pyp"
    x1 = x+vx*SHIP_FRONT;
#line 193 "flock.pyp"
    y1 = y+vy*SHIP_FRONT;
#line 195 "flock.pyp"
    x -= vx*SHIP_BACK;
#line 196 "flock.pyp"
    y -= vy*SHIP_BACK;
#line 198 "flock.pyp"
    a += M_PI/2.0;
#line 199 "flock.pyp"
    vx = cos(a);
#line 200 "flock.pyp"
    vy = sin(a);
#line 201 "flock.pyp"
    x2 = x + vx*SHIP_SIDE;
#line 202 "flock.pyp"
    y2 = y + vy*SHIP_SIDE;
#line 203 "flock.pyp"
    x3 = x - vx*SHIP_SIDE;
#line 204 "flock.pyp"
    y3 = y - vy*SHIP_SIDE;
#line 207 "flock.pyp"
    ship_line(s,p->color,(int)x1,(int)y1,(int)x2,(int)y2);
#line 208 "flock.pyp"
    ship_line(s,p->color,(int)x2,(int)y2,(int)x3,(int)y3);
#line 209 "flock.pyp"
    ship_line(s,p->color,(int)x3,(int)y3,(int)x1,(int)y1);
}
#line 211 "flock.pyp"
void ship_line(SDL_Surface *s, Uint32 color, int x1, int y1, int x2, int y2){
#line 212 "flock.pyp"
    int dx = x2-x1;
#line 213 "flock.pyp"
    int dy = y2-y1;
#line 214 "flock.pyp"
    if( dx < 0) {
#line 215 "flock.pyp"
        dx = -dx;
    }
#line 216 "flock.pyp"
    if( dy < 0) {
#line 217 "flock.pyp"
        dy = -dy;
    }
#line 218 "flock.pyp"
    line32_add_inline(s,color,x1,y1,x2,y2);
#line 219 "flock.pyp"
    if( dx > dy) {
#line 220 "flock.pyp"
        line32_add_inline(s,color,x1,y1+1,x2,y2+1);
    }
#line 221 "flock.pyp"
    else{
#line 222 "flock.pyp"
        line32_add_inline(s,color,x1+1,y1,x2+1,y2);
    }
}
