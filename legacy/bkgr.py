import random
import os
import pygame
from pygame.locals import *

from cnst import *

rr = random.randrange


import plasma
import pygame32
import blit32



def generate():
    img = pygame.Surface((SW,SH),0,8)
    plasma.plasma(img,2048)
    
    img2 = pygame.Surface((SW,SH),0,8)
    for n in xrange(0,4):
        plasma.blur(pygame32.tosurface(img),pygame32.tosurface(img2))
        img.blit(img2,(0,0))
        
    img2 = img.convert()
    
    
    c1 = (0x07,0x5a,0x9a)
    c2 = (0x8f,0x28,0xd7)
    for y in xrange(0,SH):
        b = 255-y*255/SH
        pal = [[
            (c2[0]*(255-b)/256+c1[0]*b/256)*v/256,
            (c2[1]*(255-b)/256+c1[1]*b/256)*v/256,
            (c2[2]*(255-b)/256+c1[2]*b/256)*v/256,
            ] for v in xrange(0,256)]
        img.set_palette(pal)
        img2.blit(img.subsurface(0,y,SW,1),(0,y))
        
    pal = [(v,v,v) for v in xrange(0,256)]
    img.set_palette(pal)
        
    for n in xrange(0,1024):
        x,y = rr(0,SW),rr(0,SH)
        if img.get_at((x,y))[0]>200:
            ww = rr(1,3)
            img2.fill((255,255,255),(x,y,ww,ww))
            
    star = pygame.image.load(os.path.join("data","star.png")).convert()
    for n in xrange(0,rr(12,15)):
        #img2.blit(star,(rr(0,SW),rr(0,SH)))
        blit32.blit32_add(pygame32.tosurface(star),
            None,
            pygame32.tosurface(img2),
            pygame32.torect(pygame.Rect(rr(0,SW),rr(0,SH),1,1)))
    
    return img2

if __name__ == '__main__':
    screen = pygame.display.set_mode((SW,SH),0,32)
    bkgr = generate()
    screen.blit(bkgr,(0,0))
    pygame.display.flip()
    _quit = False
    while not _quit:
        for e in pygame.event.get():
            if e.type is KEYDOWN or e.type is QUIT: _quit = True
