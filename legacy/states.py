import pygame
from pygame.locals import *
from pgu import engine

from cnst import *

import pgu.text

class BPause(engine.State):
    def __init__(self,game,next):
        self.game,self.next = game,next
        
    def event(self,e):
        if e.type is KEYDOWN:
            return self.next
        
class Prompt(engine.State):
    def __init__(self,game,text,yes,no):
        self.game,self.text,self.yes,self.no = game,text,yes,no
        self.font = self.game.font
        
    def paint(self,screen):
        self.bkgr = screen.convert()
        pgu.text.writec(screen,self.font,(255,255,255),self.text)
        pygame.display.flip()
        
    def event(self,e):
        if e.type is KEYDOWN and e.key == K_y:
            screen = pygame.display.get_surface()
            screen.blit(self.bkgr,(0,0))
            return self.yes
        if e.type is KEYDOWN and e.key == K_n:
            screen = pygame.display.get_surface()
            screen.blit(self.bkgr,(0,0))
            return self.no
        
class Pause(engine.State):
    def __init__(self,game,text,sub,next):
        self.game,self.text,self.next = game,text,next
        self.sub = sub
        self.font = self.game.font
        
    def paint(self,screen):
        self.bkgr = screen.convert()
        text = self.text
        img = self.font.render(text,1,(255,255,255))
        
        lines = [text]
        if self.sub != None:
            lines.append('')
            lines.extend(self.sub.split("\n"))
        
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
        if e.type is KEYDOWN:
            screen = pygame.display.get_surface()
            screen.blit(self.bkgr,(0,0))
            return self.next
        if e.type is MOUSEBUTTONDOWN:
            screen = pygame.display.get_surface()
            screen.blit(self.bkgr,(0,0))
            return self.next
        
class Delay(engine.State):
    def __init__(self,game,text,next):
        self.game,self.text,self.next = game,text,next
        self.font = self.game.font
        self.frame = 0
        
    def paint(self,screen):
        self.bkgr = screen.convert()
        pgu.text.writec(screen,self.font,(255,255,255),self.text)
        pygame.display.flip()
        
    def loop(self):
        self.frame += 1
        
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

        
