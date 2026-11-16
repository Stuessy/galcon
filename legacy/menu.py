import random
import os
import pygame
from pygame.locals import *

from cnst import *

from pgu import engine
import pgu.text
import level
import states

class Menu(engine.State):
    def init(self):
        
        
        self.font = pygame.font.Font(os.path.join("data","zeroes_3.ttf"),48)
        
        self.font_small = pygame.font.Font(os.path.join("data","zeroes_3.ttf"),24)
        
        self.font_title = pygame.font.Font(os.path.join("data","SAVEDBYZ.TTF"),80)
        
        self.bkgr = pygame.image.load(os.path.join("data","bkgr.png")).convert()
        
        self.level = 0
        self.levels = [
            ('easy',[24,1,1]),
            ('medium',[22,1,3]),
            ('hard',[19,1,6]),
            ('crazy',[15,1,10]),
            ]
        
        self.cur = 0
        self.items = [
            ('tutorial','tutorial'),
            ('play','play < L >'),
            #('2 player','2 player'),
            ('help','help'),
            ('quit','quit'),
            ]
        
    def paint(self,screen):
        if self.game.sound:
            pygame.mixer.music.stop()
        
        screen.blit(self.bkgr,(0,0))
        
        c = (0x55,0xff,0x55)
        
        fnt = self.font_title
        x,y = 0,8
        text = "galcon 2006"
        img = fnt.render(text,1,c)
        x = (SW-img.get_width())/2
        #screen.blit(img,(x,y))
        pgu.text.write(screen,fnt,(x,y),c,text)
        
        #y += 120
        y += 134
        
        n = 0
        fnt = self.font
        rects = self.rects = []
        for name,text in self.items:
            text = text.replace('L',self.levels[self.level][0])
            c = (0xaa,0xaa,0xaa)
            if n == self.cur:
                c = (0xff,0xff,0xff)
            img = fnt.render(text,1,c)
            x = (SW-img.get_width())/2
            #screen.blit(img,(x,y))
            pgu.text.write(screen,fnt,(x,y),c,text)
            rects.append((name,pygame.Rect(x,y,img.get_width(),img.get_height())))
            y += 48
            n += 1
            
        y = SH-48-16
        c = (0xaa,0xaa,0xaa)
        fnt = self.font_small
        for text in ['use left/right to change level','ludum dare #8 - phil hassey']:
        
            img = fnt.render(text,1,c)
            x = (SW-img.get_width())/2
            #screen.blit(img,(x,y))
            pgu.text.write(screen,fnt,(x,y),c,text)
            y += 24
        
            
        pygame.display.flip()
        
    def event(self,e):
        if e.type is MOUSEMOTION:
            n = 0
            for name,rect in self.rects:
                if rect.collidepoint(e.pos):
                    if self.cur != n:
                        self.cur = n
                        self.repaint()
                n+=1
            
        if e.type is MOUSEBUTTONDOWN:
            return self.action(self.items[self.cur][0])
        
        if e.type is KEYDOWN and e.key == K_DOWN:
            self.cur = (self.cur+1)%len(self.items)
            self.repaint()
        if e.type is KEYDOWN and e.key == K_UP:
            self.cur = (self.cur-1+len(self.items))%len(self.items)
            self.repaint()
            
        if e.type is KEYDOWN and e.key == K_RIGHT:
            self.level = (self.level+1)%len(self.levels)
            self.repaint()
        if e.type is KEYDOWN and e.key == K_LEFT:
            self.level = (self.level-1+len(self.levels))%len(self.levels)
            self.repaint()

        if e.type is KEYDOWN and e.key == K_RETURN:
            return self.action(self.items[self.cur][0])
        
            
    def action(self,name):
        self.game.level = self.level
        if name == 'tutorial':
            return Tutorial(self.game,self)
        if name == 'play':
            return level.Level(self.game,self,self.levels[self.level][1])
        if name == 'help':
            return Help(self.game,self)
        if name == 'quit':
            return engine.Quit(self.game)
        
class Help(engine.State):
    def __init__(self,game,next):
        self.game= game
        self.next = next
        self.font = self.game.font
        
    def init(self):
        self.bkgr = pygame.image.load(os.path.join("data","bkgr.png")).convert()
    def paint(self,screen):
        screen.blit(self.bkgr,(0,0))
        text = 'test'
        img = self.font.render(text,1,(255,255,255))
        self.sub = """help

click to select planets
and deploy attacks.

use numbers to change
percent of ships to
send.

use tab to show stats
on planet faces.

f2 - toggle music
alt-enter - full screen

have fun!"""
        lines = self.sub.split("\n")
        
        w,h = img.get_width(),img.get_height()
        h = h*2/3
        x,y = 0,(SH-(h*len(lines)))/2
        
        for text in lines:
            img = self.font.render(text,1,(255,255,255))
            x = (SW-img.get_width())/2
            pgu.text.write(screen,self.font,(x,y),(255,255,255),text)
            y += h
        pygame.display.flip()

    def event(self,e):
        #if self.frame < FPS*1: return
        
        if e.type is KEYDOWN:
            screen = pygame.display.get_surface()
            screen.blit(self.bkgr,(0,0))
            return self.next
        if e.type is MOUSEBUTTONDOWN:
            screen = pygame.display.get_surface()
            screen.blit(self.bkgr,(0,0))
            return self.next

class Tutorial(engine.State):
    def __init__(self,game,next):
        self.game= game
        self.next = next
    def init(self):
        self.items = [
            ([3,1,0],'tutorial #1',
"""you are the green player.
to send ships, click on the planet
you want to send from, then double
click on the planet you want to
send to.

use number keys to change the
size of your attacks."""),
            
            ([3,1,1],'tutorial #2',
"""to play a real game, you will
have to face the red enemy.  try
to take over that planet.

if there are good neutral planets
(high production) you may 
want to take over those first.
(big planet = high production)

reinforce your planets so the
enemy cannot take over.
"""),
            ([10,1,1],'tutorial #3',
"""production is impacted
by your planet morale.

morale goes up when being 
reinforced or near friendly
planets.

morale goes down when being
attacked or near enemy
planets.

use the tab key to show stats
on the planet face."""),
           ]
        self.cur = 0
            
    def loop(self):
        if self.cur >= len(self.items):
            return states.Pause(self.game,'congratulations!',
"""you have completed the tutorial.
you may now try out a real game.
i hope you have fun!""",self.next)
        cnt,pre,sub = self.items[self.cur]
        self.cur += 1
        return level.Level(self.game,self,cnt,pre,sub,1)
