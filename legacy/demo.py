import random
import os
import pygame
from pygame.locals import *

from cnst import *

from pgu import timer

import flock
import pygame32

rr = random.randrange

import ship

import planet

import plasma
import blit32
import pygame32

import bkgr
        
if __name__ == '__main__':
    screen = pygame.display.set_mode((SW,SH),0,32) #32)#
    pygame.font.init()
    
    img = pygame.Surface((8,8)).convert()
    img.fill((255,128,64))
    
    flock.init()
    
    _quit = False
    frame = 0
    
    bkgr = bkgr.generate()
    
    t = timer.Timer(FPS)
        
    ships = []
    for n in xrange(0,16):
        ships.append(ship.Ship())
        
    planets = []
    for n in xrange(0,32):
        p = planet.Planet()
        planets.append(p)
    fnt = pygame.font.Font(os.path.join("data","zeroes_3.ttf"),15)
    
    while not _quit:
        for e in pygame.event.get():
            if e.type is KEYDOWN or e.type is QUIT: _quit = True
            #if e.type is MOUSEBUTTONDOWN:
            #    parts.explode(e.pos[0],e.pos[1],24,256,pygame32.pygame32_obj_to_surface(img))
                
        #x,y = pygame.mouse.get_pos()
        #parts.explode(x,y,24,8,pygame32.pygame32_obj_to_surface(img))

        flock.loop()
        
        #screen.fill((0,0,0))
        screen.blit(bkgr,(0,0))
        
        if frame <= FPS:
            for p in planets:
                x,y = p.pos = p.part.x,p.part.y
                p.render(screen,p.pos)
                img = fnt.render(p.name,1,(0x55,0xff,0x55))
                screen.blit(img,(x-img.get_width()/2,y+p.size/2))
        if frame == FPS:
            bkgr.blit(screen,(0,0))

        flock.paint(pygame32.pygame32_obj_to_surface(screen))
        pygame.display.flip()
        
        t.tick()
        frame += 1
        
        