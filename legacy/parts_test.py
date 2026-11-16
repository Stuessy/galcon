import random
import pygame
from pygame.locals import *

from cnst import *

import parts
import pygame32

from pgu import timer

if __name__ == '__main__':
    screen = pygame.display.set_mode((SW,SH),0,32)
    
    img = pygame.Surface((8,8)).convert()
    img.fill((255,128,64))
    
    parts.init()
    
    _quit = False
    frame = 0
    
    t = timer.Timer(FPS)
        
    for n in xrange(0,8):
        parts.explode(SW/2,SH/2,24,256,pygame32.pygame32_obj_to_surface(img))

    while not _quit:
        for e in pygame.event.get():
            if e.type is KEYDOWN or e.type is QUIT: _quit = True
            if e.type is MOUSEBUTTONDOWN:
                parts.explode(e.pos[0],e.pos[1],24,256,pygame32.pygame32_obj_to_surface(img))
                
        x,y = pygame.mouse.get_pos()
        parts.explode(x,y,24,8,pygame32.pygame32_obj_to_surface(img))

        parts.loop()
        
        screen.fill((0,0,0))
        parts.paint(pygame32.pygame32_obj_to_surface(screen))
        pygame.display.flip()
        
        t.tick()
        frame += 1
        
        