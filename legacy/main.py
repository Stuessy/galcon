import random
import os
import pygame
from pygame.locals import *

from cnst import *

from pgu import timer

from pgu import engine

import states

class Sound:
    def __init__(self,fname,vol):
        self.sound = None
        if pygame.mixer.get_init():
            self.sound = pygame.mixer.Sound(fname)
            self.sound.set_volume(vol*0.5)
    def play(self):
        if self.sound:
            #if self.sound.get_num_channels() < 2:
            self.sound.stop()
            self.sound.play()

class Game(engine.Game):
    def __init__(self):
        pass
    
    def init(self):
        self.timer = timer.Timer(FPS)
        self.full = 0
        self.sound = 0
        pygame.mixer.init()
        if pygame.mixer.get_init():
            self.sound = 1
        self.music = 1
        self.sfx = {}
        #for name,vol in [('blast',0.25),('launch',1.0),('takeover',1.0)]:
        for name,vol in [('blast',0.25),('launch',0.0),('takeover',1.0)]:
            self.sfx[name] = Sound(os.path.join("data","%s.wav"%name),vol)
        self.screen = pygame.display.set_mode((SW,SH),self.full*FULLSCREEN,32)
        pygame.display.set_caption("galcon - phil hassey - alt-enter = fullscreen")
        pygame.font.init()
        
        self.font = pygame.font.Font(os.path.join("data","zeroes_3.ttf"),32)

        
    def tick(self):
        self.timer.tick()
        
    def event(self,e):
        if e.type is QUIT:
            self.state = engine.Quit(self)
            return 1
        if e.type is KEYDOWN and e.key == K_RETURN and (pygame.key.get_mods()&(KMOD_LALT|KMOD_RALT))!=0:
            self.full ^= 1
            tmp = pygame.Surface((SW,SH)).convert()
            tmp.blit(self.screen,(0,0))
            self.screen = pygame.display.set_mode((SW,SH),self.full*FULLSCREEN,32)
            self.screen.blit(tmp,(0,0))
            return 1
        if e.type is KEYDOWN and e.key == K_F2:
            self.music ^= 1
            if self.sound:
                pygame.mixer.music.set_volume(1.0*self.music)
            return 1
        if e.type is KEYDOWN and e.key == K_F10 and self.state.__class__ != states.BPause:
            self.state = states.BPause(self,self.state)
            return 1
            
            
g = Game()
#import level
#g.run(level.Level(g))
import menu
g.run(menu.Menu(g))
#import profile
#profile.run("g.run(level.Level(g))")