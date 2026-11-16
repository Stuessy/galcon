# try:
#     import psyco
#     psyco.profile()
#     print 'psyco installed'
# except:
#     print 'psyco not installed'

import random
import os
import pygame
from pygame.locals import *

from cnst import *

import plasma
import pygame32
import blit32

rr = random.randrange

import colorsys


def hsv_to_rgb(h,s,v):
    r,g,b = colorsys.hsv_to_rgb(h%360/360.0,s/100.0,v/100.0)
    return int(255*r),int(255*g),int(255*b)
    
import flock

def sign(a):
    if a<0: return -1
    if a>0: return 1
    return 0

class Planet:
    def __init__(self,bounds = None,letter=None,production=None):
        self.letter = letter
        #self.size = rr(24,80)
        #self.size = rr(12,32)
        #self.size = rr(80,120)
        if production == None:
            production = rr(1,101)
        self.chproduction(production)
        if not hasattr(Planet,'_init'):
            Planet._init = True
            Planet.shader = pygame.image.load(os.path.join("data","pshader.png")).convert_alpha()
            Planet.atmos = pygame.image.load(os.path.join("data","patmos.png")).convert_alpha()
            
        img = pygame.Surface((PS,PS),0,8) #rr(PS/3,PS)
        plasma.plasma(img,2**rr(8,14))
        #img = pygame.transform.scale(img,(PS,PS))
        
        self.image = img
        self.itype = rr(0,2)
        self.irotate = 0 #rr(0,360)
        #self.color = (8,8,8) #surface
        h = rr(0,360)
        ##self.color = hsv_to_rgb(h,100,100) #water/gas
        ##self._color2 = hsv_to_rgb(h+120+rr(0,120),rr(25,50),rr(50,100)) #water/gas
        ##self._color3 = hsv_to_rgb(rr(0,360),50,rr(50,100)) #atmosphere
        
        self.name = self._name()
        
        if bounds == None:
            x,y = rr(0,SW),rr(0,SH) 
        else:
            x,y = rr(bounds.left,bounds.right),rr(bounds.top,bounds.bottom)
        self.rect = pygame.Rect(0,0,self.size,self.size)
        self.rect.centerx,self.rect.centery = x,y
        self.part = flock.planet_new(x,y,self.r)
        
        self.real_ships = self.ships = rr(10,30) #1000 #rr(3,32)
        self.real_morale = self.morale = 50 #rr(1,100)
        
        self.owner = rr(0,PLAYERS)
        #self.color = COLORS[self.owner]
        self.chowner(self.owner)
        self.attacks = [0 for n in xrange(0,PLAYERS)]
        self.frame = rr(0,FPS*23)
        
        self.images = {}
        self.near = []
        
    def chproduction(self,p):
        self.production = p
        #self.size = rr(24,36,2)+1
        self.r = 12+int(self.production*10/100)
        self.size = self.r*2+1
        #self.r = self.size/2

        
    def chowner(self,owner):
        self.owner = owner
        self.color = COLORS[owner]
        self._color2 = COLORS2[owner]
        self._color3 = COLORS3[owner]

        
    def _name(self):
        const = 'bdfgjklmnprstvxz'
        vowel = 'aeiou'
        letters = const+vowel
        p = random.choice(letters)
        name = p
        for i in xrange(0,rr(3,8)):
            if p in const: n = random.choice(vowel)
            else: n = random.choice(const)
            name = name+n
            p = n
        return name
        
    def _planet(self,size=None):
        if size == None: size = self.size
        img = pygame.Surface((PS,PS)).convert()
        
        itype = self.itype
        c = self.color
        c2 = self._color2 #surface
        c3 = self._color3 #atmosphere
        if itype == 0: #water + land
            pal = [ [c[0]*v/256,c[1]*v/256,c[2]*v/256] for v in xrange(0,256) ]
            for n in xrange(0,128):
                pal[n] = c2
        elif itype == 1: #mixed gasses
            pal = [ [c2[0]*(255-v)/256+c[0]*v/256,
                c2[1]*(255-v)/256+c[1]*v/256,
                c2[2]*(255-v)/256+c[2]*v/256] for v in xrange(0,256) ]
                
        pal = [ [gamma(p[0]),gamma(p[1]),gamma(p[2])] for p in pal]
        
        self.image.set_palette(pal)
        img.blit(self.image,(0,0))
        img.blit(Planet.shader,(0,0))
        img.set_colorkey((255,0,255))
        img = img.convert_alpha()
        
        
        img = pygame.transform.rotozoom(img,self.irotate,1.0*size/PS)
        #img = pygame.transform.scale(img,(size,size))
        
        #img = img.subsurface(((img.get_width()-size)/2,(img.get_height()-size)/2,size,size))
        return img
        
    def _atmos(self,size=None):
        if size == None: size = self.size
        
        c3 = self._color3 #atmosphere
        ss = PS+PA*2
        img2 = pygame.Surface((ss,ss)).convert()
        img2.blit(Planet.atmos,(0,0))
        img3 = pygame.Surface((ss,ss)).convert()
        img3.fill(c3)
        
        
        blit32.blit32_multiply(pygame32.pygame32_obj_to_surface(img3),None,pygame32.pygame32_obj_to_surface(img2),None)
        
        img = img2
        
        img = pygame.transform.rotozoom(img,self.irotate,1.0*size/PS)
        #size = img2.get_width()*size/PS
        #img = img.subsurface(((img.get_width()-size)/2,(img.get_height()-size)/2,size,size))
        
        return img
        
        
    def render(self,surf,pos,size=None):
        if size == None: size = self.size
        
        key = 'planet.%d'%self.owner
        if key not in self.images:
            self.images[key] = self._planet(size)
        img = self.images[key]
        
        surf.blit(img,(pos[0]-img.get_width()/2,pos[1]-img.get_height()/2))
        
        key = 'atmos.%d'%self.owner
        if key not in self.images:
            self.images[key] = self._atmos(size)
        img = self.images[key]
        
        blit32.blit32_add(pygame32.pygame32_obj_to_surface(img),None,pygame32.pygame32_obj_to_surface(surf),
            pygame32.pygame32_obj_to_rect(pygame.Rect((pos[0]-img.get_width()/2,pos[1]-img.get_height()/2,img.get_width(),img.get_height()))))
        
    def loop(self):
        #production, morale, ships
        
        m_ships = self.ships + self.attacks[self.owner]
        a_ships = 0
        a_ships -= self.attacks[self.owner]
        for n in self.attacks: a_ships += n
        
        for p in self.near:
            if p.owner == self.owner:
                m_ships += p.ships
            elif p.owner != NEUTRAL:
                a_ships += p.ships
        self.real_ships = m_ships-a_ships
        
        total = m_ships+a_ships
        morale = 100
        if total != 0:
            morale = round(m_ships*100/total)
        self.real_morale = morale
            
        #morale can change _N_ notches per second
        dm = sign(morale-self.morale)
        if dm < 0 and (self.frame%(FPS/16))==0: self.morale -= 1
        if dm > 0 and (self.frame%(FPS/4))==0: self.morale += 1
        self.morale = max(0,self.morale)
        self.morale = min(100,self.morale)
        
        #ships generate a whole batch in _N_ seconds
        self.ships += self.production * self.morale / (100.0*FPS*100.0*0.5)
        self.ships = max(0,self.ships)
        #self.ships = min(100,self.ships)
        
        self.frame += 1

        
        
def gamma(v):
    return v
    v = v/256.0
    v = v**(1.0/2.2)
    v *= 255.0
    
    return int(v)

if __name__ == '__main__':
    screen = pygame.display.set_mode((SW,SH),0,32)
    
    flock.init()
    _quit = False
    frame = 0
    while not _quit:
        for e in pygame.event.get():
            if e.type is KEYDOWN or e.type is QUIT: _quit = True
        p = Planet()
        #screen.fill((0,0,0)) #128,128,128))
        #img = p.color((rr(64,256),rr(64,256),rr(64,256)),rr(64,128))
        #screen.blit(img,(rr(0,SW-img.get_width()),rr(0,SH-img.get_height())))
        
        if frame < 32:
            b = 16
            x,y = (rr(b,SW-b),rr(b,SH-b))
            p.render(screen,(x,y))
            
        pygame.display.flip()
        pygame.time.wait(10)
        frame += 1
        
    