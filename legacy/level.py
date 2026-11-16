import random
import os
import pygame
from pygame.locals import *

from cnst import *

import flock
import pygame32

rr = random.randrange

import ship

import planet

import plasma
import blit32
import pygame32

import bkgr

import parts
        
from pgu import engine

BORDER = 40
PADDING = 8
PBORDER = 9 #6

class Level(engine.State):
    def __init__(self,game,next,counts,pre='get ready',sub=None,easy=0):
        self.game= game
        self.counts = counts[:]
        self.next = next
        self.pre = pre
        self.sub = sub
        self.easy = easy
        self.tutorial = (self.counts[2]==0)
    def init(self):
        
        if self.game.sound:
            pygame.mixer.music.load(os.path.join("data","galcon.ogg"))
            pygame.mixer.music.play(-1)
            
        flock.flock_init()
        parts.parts_init()
        
        self.frame = 0
        
        #self._bkgr = bkgr.generate()
        self._bkgr = pygame.image.load(os.path.join("data","bkgr.png")).convert()
        
        self.ships = []
        self.planets = planets = []
        sz = 16
        top = BORDER+sz
        left = PADDING+sz
        rect = pygame.Rect(left,top,SW-left*2,SH-top*2)
        
        n = 0
        owner = len(self.counts)-1
        cnts = self.counts[:]
        cnts.reverse()
        for cnt in cnts:
            for m in xrange(0,cnt):
                prod = None
                if m == 0 and owner != 0:
                    prod = 100
                p = planet.Planet(rect,letter=chr(65+n),production=prod)
                planets.append(p)
                p.part.r = p.r+PBORDER
                p.chowner(owner)
                if m == 0 and owner != 0:
                    p.morale = 100
                    p.ships = 100
                    if self.easy and owner == COMPUTER:
                        p.ships = 25
                    if owner == 1:
                        p.rect.centerx = p.part.x = left
                    if owner == 2:
                        p.rect.centerx = p.part.x = SW-left
                n += 1
            owner -= 1
        
        self.font = pygame.font.Font(os.path.join("data","zeroes_3.ttf"),20)
        self.font_small = pygame.font.Font(os.path.join("data","zeroes_3.ttf"),15)
    
        #to stabilize the planet locations
        for n in xrange(0,FPS):
            top = BORDER
            left = PADDING
            r = pygame.Rect(left,top,SW-left*2,SH-top*2)
            for p in self.planets:
                pr = p.rect
                pr.centerx,pr.centery = p.part.x,p.part.y
                pr.clamp_ip(r)
                p.part.x,p.part.y = pr.centerx,pr.centery
            flock.flock_loop()
            
        for p in self.planets:
            p.part.r = p.r
            
        for p1 in self.planets[:]:
            ds = []
            for p2 in self.planets[:]:
                if p1 != p2:
                    dx,dy = p2.rect.centerx-p1.rect.centerx,p2.rect.centery-p1.rect.centery
                    dist = (dx*dx+dy*dy)**0.5
                    ds.append((dist,p2))
            ds.sort()
            #ds.reverse()
            near = ds[0:5]
            for d,p in near:
                p1.near.append(p)
            #print len(p1.near)
            
        for p1 in self.planets[:]:
            for p2 in self.planets[:]:
                if p1 in p2.near and p2 not in p1.near:
                    p1.near.append(p2)
                    
            
        self.a_hover = None
        self.a_from = None
        self.a_to = None
        self.a_ships = None
        self.a_percent = 50
        
        self.part1 = pygame.image.load(os.path.join("data","part1.png")).convert()
        
        self.stat = None
        
        self.first = True
        self.end_wait = 0

    def paint(self,screen):
        self.bkgr = self._bkgr.convert()
        for p in self.planets:
            p.render(self.bkgr,(p.rect.centerx,p.rect.centery))
            #img = fnt.render(p.name,1,(0x55,0xff,0x55))
            #screen.blit(img,(x-img.get_width()/2,y+p.size/2))
        
        self.update(screen)
        
        if self.first:
            self.first = False
            import states
            return states.Pause(self.game,self.pre,self.sub,self)
            
        
    def update(self,screen):
        screen.blit(self.bkgr,(0,0))
        flock.flock_paint(pygame32.pygame32_obj_to_surface(screen))
        parts.parts_paint(pygame32.pygame32_obj_to_surface(screen))
        
        if self.a_from:
            self.a_ships = round(self.a_from.ships*self.a_percent/100)
        
        c = (0x55,0xff,0x55)
        c2 = (0xff,0xff,0xff)
        bg = (0,0,0)
        
#         for p1 in self.planets:
#            for p2 in p1.near:
#                pygame.draw.line(screen,c,(p1.rect.centerx,p1.rect.centery),(p2.rect.centerx,p2.rect.centery))
        
        space = 8
        width = 1
        if self.a_from:
            p = self.a_from
            x,y = p.rect.centerx,p.rect.centery
            draw_circle(screen,c,(x,y),p.r+space,width)
        if self.a_to:
            p = self.a_to
            x,y = p.rect.centerx,p.rect.centery
            draw_circle(screen,c,(x,y),p.r+space,width)
        if self.a_from and self.a_to:
            p = self.a_from
            x1,y1 = p.rect.centerx,p.rect.centery
            p = self.a_to
            x2,y2 = p.rect.centerx,p.rect.centery
            dx,dy = x2-x1,y2-y1
            dist = (dx*dx+dy*dy)**0.5
            dx,dy = dx/dist,dy/dist
            r = self.a_from.r+space
            x1,y1=x1+r*dx,y1+r*dy
            r = self.a_to.r+space
            x2,y2=x2-r*dx,y2-r*dy
            pygame.draw.line(screen,c,(x1,y1),(x2,y2),4)

        stat = self.stat
        if stat != None:
            for p in self.planets:
                if stat == 'letter': text= p.__dict__[stat]
                else: text = str(int(p.__dict__[stat]))
                img = self.font_small.render(text,1,bg)
                x,y = p.rect.centerx-img.get_width()/2,p.rect.centery-img.get_height()/2
                y += 2
                screen.blit(img,(x+1,y+1))
                
                img = self.font_small.render(text,1,c2)
                screen.blit(img,(x,y))
                
        
        tab = '    '
        if self.a_hover:
            p = self.a_hover
            data = []
            data.append('planet: %s'%p.name)
            data.append('production: %d'%p.production)
            data.append('morale: %d'%p.morale)
            data.append('ships: %d'%p.ships)
            text = tab.join(data)
            img = self.font.render(text,1,c)
            screen.blit(img,((SW-img.get_width())/2,(BORDER-img.get_height())/2))
            
            fh = 15
            fn = 3
            data = []
            data.append(p.name)
            data.append('%d / %d / %d'%(p.production,p.morale,p.ships))
            border = 8
            if p.rect.centerx < SW/2:
                x = p.rect.right+border
                y = p.rect.centery-fh-fn
                for text in data:
                    img = self.font_small.render(text,1,bg)
                    screen.blit(img,(x+1,y+1))
                    img = self.font_small.render(text,1,c)
                    screen.blit(img,(x,y))
                    y += fh
            else:
                y = p.rect.centery-fh-fn
                for text in data:
                    img = self.font_small.render(text,1,bg)
                    x = p.rect.left-(img.get_width()+border)
                    screen.blit(img,(x+1,y+1))
                    img = self.font_small.render(text,1,c)
                    screen.blit(img,(x,y))
                    y += fh
                    

            
        if 1: #self.a_from or self.a_to or self.a_ships or self.a_percent:
            data = []
            if self.a_from:
                data.append("From: %s"%self.a_from.name)
            if self.a_to:
                data.append("To: %s"%self.a_to.name)
                
            if self.a_ships:
                data.append("Ships: %d (%d%%)"%(self.a_ships,self.a_percent))
            else:
                data.append("Ships: %d%%"%self.a_percent)
            text = tab.join(data)
            img = self.font.render(text,1,c)
            screen.blit(img,((SW-img.get_width())/2,SH-BORDER+(BORDER-img.get_height())/2))
            
        
        pygame.display.flip()

    def loop(self):
        self.computer(COMPUTER)
        #self.computer(HUMAN)
        
        flock.flock_loop()
        parts.parts_loop()
        
        for p in self.planets:
            p.loop()
            
        for e in self.ships[:]:
            if e.part.hit:
                p = e.target
                p.attacks[e.owner] -= 1
                if p.owner == e.owner:
                    p.ships += 1
                else:
                    self.game.sfx['blast'].play()
                    p.ships -= 1
                    x,y = e.part.x,e.part.y
                    parts.explode(x,y,1,16,pygame32.tosurface(self.part1))
                    if p.ships < 0:
                        self.game.sfx['takeover'].play()
                        p.ships = abs(p.ships)
                        p.chowner(e.owner)
                        x,y = p.rect.centerx,p.rect.centery
                        parts.explode(x,y,p.r,512,pygame32.tosurface(self.part1))
                        self.repaint()
                    
                    
                e.part.active = 0
                self.ships.remove(e)
                
                
        
        #x,y = pygame.mouse.get_pos()
        #parts.explode(x,y,24,8,pygame32.tosurface(self.part1))
        
        count = [0 for n in xrange(0,PLAYERS)]
        for p in self.planets:
            count[p.owner] += 1
        wait = FPS*3
        test = COMPUTER
        if self.tutorial:
            test = NEUTRAL
        if not count[test]:
            self.end_wait += 1
            if self.end_wait == wait:
                import states
                return states.Delay(self.game,"good job!",self.next)
        elif not count[HUMAN]:
            self.end_wait += 1
            if self.end_wait == wait:
                import states
                #return states.Delay(self.game,"game over!",self.next)
                return states.Prompt(self.game,"try again? y/n",Level(self.game,self.next,self.counts,self.pre,self.sub),self.next)
        else:
            self.end_wait = 0
        

        
    def event(self,e):
        
        if e.type is MOUSEMOTION:
            self.a_hover = None
            for p in self.planets:
                if p.rect.collidepoint(e.pos):
                    self.a_hover = p
                    
        if e.type is MOUSEBUTTONDOWN:# and e.button == 1:
            chosen = None
            
            for p in self.planets:
                if p.rect.collidepoint(e.pos):
                    chosen = p
                    
            reset = False
            
            if chosen == None:
                reset = True
            elif self.a_from == None and chosen.owner != HUMAN:
                reset = True
            elif self.a_from == None:
                self.a_from = chosen
            elif self.a_from == chosen:
                #reset = True
                self.a_to = None
            elif self.a_to == None:
                self.a_to = chosen
            elif self.a_to != chosen:
                self.a_to = chosen
            elif self.a_to == chosen:
                self.attack()
            else:
                reset = True
                
            if reset:
                self.a_from, self.a_to = None,None
                
                    
#         if e.type is MOUSEBUTTONDOWN and e.button == 3:
#             prev = self.a_to
#             self.a_to = None
#             for p in self.planets:
#                 if p.rect.collidepoint(e.pos):
#                     self.a_to = p
#             if prev != None and prev == self.a_to:
#                 self.attack()
                    
        if e.type is KEYDOWN and (e.key in xrange(K_0,K_9+1) or e.key in xrange(K_KP0,K_KP9+1)):
            if e.key in xrange(K_0,K_9+1):
                v = e.key-K_0
            else:
                v = e.key-K_KP0
            self.a_percent = v*10
            if self.a_percent == 0:
                self.a_percent = 100
#             if self.a_ships == None:
#                 self.a_ships = 0
#             if self.a_ships < 10:
#                 self.a_ships = self.a_ships*10+v
            
#         if e.type is KEYDOWN and e.key in (K_DELETE,K_BACKSPACE,K_KP_PERIOD):
#             self.a_ships /= 10
            
#         if e.type is KEYDOWN and e.key in (K_RETURN,K_KP_ENTER,K_SPACE):
#             self.attack()
            
        if e.type is KEYDOWN and e.key == K_TAB:
            stats = [None,'production','morale','ships']#,'letter']
            i = stats.index(self.stat)
            i = (i+1)%len(stats)
            self.stat = stats[i]
            
        if e.type is KEYDOWN and e.key == K_ESCAPE:
            import states
            return states.Prompt(self.game,'quit? y/n',self.next,self)
        
        if e.type is KEYDOWN and e.key == K_F12:
            import states
            return states.Delay(self.game,"cheater!",self.next)
                    
        if self.a_from == self.a_to:
            self.a_from,self.a_to = None,None
    
    def attack(self):
        if self.a_from:
            self.a_ships = round(self.a_from.ships*self.a_percent/100)
        if not (self.a_from and self.a_to and self.a_ships):
            print 'SFX beep!'
            return
        if self.a_from.owner == HUMAN:
            self._attack(self.a_from,self.a_to,self.a_ships)
        self.a_from,self.a_to,self.a_ships = None,None,None
                    
    def _attack(self,a_from,a_to,a_ships):
        a_ships = int(min(a_ships,a_from.ships))
        
        for n in xrange(0,a_ships):
            e = ship.Ship(a_from,a_to)
            self.ships.append(e)
            a_from.ships -= 1
            a_to.attacks[a_from.owner] += 1
        self.game.sfx['launch'].play()
        

    def computer(self,owner):
        for p1 in self.planets:
            if p1.owner == owner and (p1.frame % (FPS/2)) ==0:
                random.shuffle(p1.near)
                for p2 in p1.near:
                    #don't bother if we have no ships
                    if p1.ships < 10:
                        break
                    
                    #or really low morale
                    if p1.real_morale < 25:
                        break
                    
                    #send reinforcements for moral reasons
                    if p2.owner == p1.owner and (p1.real_morale-p2.real_morale) > 10:
                        ships = p1.ships*1/4
                        self._attack(p1,p2,ships)
                    
                    #send reinforcements for lack of ships
                    elif p2.owner == p1.owner and (p1.real_ships-p2.real_ships) > 10: # and p2.production < p1.production:
                        ships = (p1.real_ships-p2.real_ships)/2
                        self._attack(p1,p2,ships)
                    
                    #just attack 'em!
                    elif p2.owner != p1.owner:
                        ships = p1.ships*3/4
                        self._attack(p1,p2,ships)
                    
                

def draw_circle(screen,c,pos,r,w):
    x,y = pos
    for dx,dy in [(-1,0),(1,0),(0,-1),(0,1),(-1,-1),(1,1),(-1,1),(1,-1)]:
        pygame.draw.circle(screen,c,(x+dx,y+dy),r,w)
                    
                    
                    
                    
                    
        