import random
import os
import pygame
from pygame.locals import *

from cnst import *

from pgu import timer

import flock
import pygame32

rr = random.randrange

class Ship:
    def __init__(self,p1=None,p2=None):
        self.color = (0x255,0x55,0x55)
        
        #pos = self.pos = rr(0,SW),rr(0,SH)
        part = None
        if p1 == None or p2 == None:
            x1,y1,x2,y2 = rr(0,SW),rr(0,SH),rr(0,SW),rr(0,SH)
        else:
            x1,y1 = p1.rect.centerx,p1.rect.centery
            x2,y2 = p2.rect.centerx,p2.rect.centery
            part = p2.part
            self.target = p2
            self.owner = p1.owner
            self.color = p1.color
            
        screen = pygame.display.get_surface()
        self.part = flock.ship_new(x1,y1,x2,y2,part,
            screen.map_rgb(self.color))
        
        self.part.v = 1.0 #1.7
        
import planet
        
if __name__ == '__main__':
    screen = pygame.display.set_mode((SW,SH),0,32)
    
    img = pygame.Surface((8,8)).convert()
    img.fill((255,128,64))
    
    flock.init()
    
    _quit = False
    frame = 0
    
    t = timer.Timer(FPS)
        
    ships = []
    for n in xrange(0,128):
        ships.append(Ship())
        
    while not _quit:
        for e in pygame.event.get():
            if e.type is KEYDOWN or e.type is QUIT: _quit = True
            #if e.type is MOUSEBUTTONDOWN:
            #    parts.explode(e.pos[0],e.pos[1],24,256,pygame32.pygame32_obj_to_surface(img))
                
        #x,y = pygame.mouse.get_pos()
        #parts.explode(x,y,24,8,pygame32.pygame32_obj_to_surface(img))

        flock.loop()
        
        screen.fill((0,0,0))
        
        flock.paint(pygame32.pygame32_obj_to_surface(screen))
        pygame.display.flip()
        
        t.tick()
        frame += 1
        
        