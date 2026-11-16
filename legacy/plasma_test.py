import random
import pygame
from pygame.locals import *

from cnst import *

import plasma

if __name__ == '__main__':
    #SW,SH = 128,128
    screen = pygame.display.set_mode((SW,SH),0,8)
    p = [(v,v,v) for v in xrange(0,256)]
    pygame.display.set_palette(p)
    screen.set_palette(p)
    
    _quit = False
    while not _quit:
        for e in pygame.event.get():
            if e.type is KEYDOWN or e.type is QUIT: _quit = True
        plasma.plasma(screen,1024)
        pygame.display.flip()
    