import math, random
random.seed(4)
def tufo(cx, cy, rx, ry, a0, a1, n, amp):
    pts=[]
    for i in range(n+1):
        t=a0+(a1-a0)*i/n
        r=1+ (amp if i%2 else -amp*0.3)
        pts.append((cx+rx*r*math.cos(t), cy+ry*r*math.sin(t)))
    return pts
def path(pts): return "M"+" L".join(f"{x:.1f} {y:.1f}" for x,y in pts)
# contorno da cabeça com pelos nas bochechas
cab=[]
for i in range(73):
    t=math.radians(-90+i*5)
    rx,ry=300,285
    # bochechas mais largas abaixo do olho
    w=1+0.10*math.exp(-((math.sin(t)-0.35)**2)/0.08)
    r=w*(1+ (0.035 if i%2 and 0.0<math.sin(t)<0.8 else 0))
    cab.append((500+rx*r*math.cos(t), 450+ry*r*math.sin(t)*(0.96 if math.sin(t)<0 else 1.0)))
fios=[]
def fio(x,y,ang,l,w,c):
    x2=x+l*math.cos(ang); y2=y+l*math.sin(ang)
    mx=(x+x2)/2+ l*0.15*math.cos(ang+1.5); my=(y+y2)/2+l*0.15*math.sin(ang+1.5)
    fios.append(f'<path d="M{x:.1f} {y:.1f} Q{mx:.1f} {my:.1f} {x2:.1f} {y2:.1f}" stroke="{c}" stroke-width="{w}" fill="none" stroke-linecap="round" opacity=".55"/>')
# pelos da cabeça: fluem do centro do focinho para fora
for _ in range(900):
    t=random.uniform(0,2*math.pi); r=math.sqrt(random.random())
    x=500+290*r*math.cos(t); y=440+270*r*math.sin(t)
    if ((x-500)/170)**2+((y-575)/130)**2<1: continue
    ang=math.atan2(y-560,x-500)+random.uniform(-.25,.25)
    fio(x,y,ang,random.uniform(14,34),random.uniform(1.6,3),"#2a2a2a")
for _ in range(700):
    x=random.uniform(110,900); y=random.uniform(760,1000)
    if ((x-500)/420)**2+((y-1010)/290)**2>1: continue
    ang=math.radians(90+ (x-500)/12)+random.uniform(-.3,.3)
    fio(x,y,ang,random.uniform(22,46),random.uniform(1.8,3.4),"#2a2a2a")
corpo=[]
for i in range(41):
    t=math.radians(180+i*4.5)
    r=1+(0.03 if i%2 else 0)
    corpo.append((500+410*r*math.cos(t), 1010+300*r*math.sin(t)))
svg=f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 1000" width="1000" height="1000">
<defs>
 <radialGradient id="cab" cx="42%" cy="34%" r="70%"><stop offset="0" stop-color="#fafafa"/><stop offset=".5" stop-color="#bdbdbd"/><stop offset=".85" stop-color="#6e6e6e"/><stop offset="1" stop-color="#3c3c3c"/></radialGradient>
 <radialGradient id="corpo" cx="42%" cy="10%" r="90%"><stop offset="0" stop-color="#dcdcdc"/><stop offset=".55" stop-color="#8a8a8a"/><stop offset="1" stop-color="#2a2a2a"/></radialGradient>
 <radialGradient id="foc" cx="46%" cy="32%" r="72%"><stop offset="0" stop-color="#ffffff"/><stop offset=".7" stop-color="#cfcfcf"/><stop offset="1" stop-color="#8c8c8c"/></radialGradient>
 <radialGradient id="ore" cx="50%" cy="55%" r="55%"><stop offset="0" stop-color="#1e1e1e"/><stop offset="1" stop-color="#8a8a8a"/></radialGradient>
 <radialGradient id="olho" cx="35%" cy="30%" r="70%"><stop offset="0" stop-color="#555"/><stop offset=".5" stop-color="#0b0b0b"/></radialGradient>
</defs>
<rect width="1000" height="1000" fill="#000"/>
<path d="{path(corpo)} Z" fill="url(#corpo)"/>
<ellipse cx="270" cy="235" rx="58" ry="52" fill="#9c9c9c"/><ellipse cx="272" cy="242" rx="32" ry="28" fill="url(#ore)"/>
<ellipse cx="730" cy="235" rx="58" ry="52" fill="#8a8a8a"/><ellipse cx="728" cy="242" rx="32" ry="28" fill="url(#ore)"/>
<path d="{path(cab)} Z" fill="url(#cab)"/>
<clipPath id="ch"><path d="{path(cab)} Z"/></clipPath><g clip-path="url(#ch)"><path d="M760 330 C840 470 790 640 640 730 C740 610 770 470 760 330Z" fill="#000" opacity=".28"/><path d="M240 330 C175 460 210 620 330 720 C265 600 245 470 240 330Z" fill="#000" opacity=".12"/></g>
<clipPath id="cc"><path d="{path(cab)} Z"/><path d="{path(corpo)} Z"/></clipPath><g clip-path="url(#cc)">{''.join(fios)}</g>
<ellipse cx="500" cy="575" rx="172" ry="132" fill="url(#foc)"/>
<ellipse cx="418" cy="600" rx="92" ry="78" fill="#fff" opacity=".35"/><ellipse cx="584" cy="602" rx="92" ry="78" fill="#777" opacity=".25"/>
<g fill="#333">{''.join(f'<circle cx="{x}" cy="{y}" r="3.5"/>' for x,y in [(400,575),(385,600),(415,605),(395,625),(600,575),(615,600),(585,605),(605,625),(430,590),(570,590)])}</g>
<path d="M432 498 C452 466 548 466 568 498 C586 528 546 556 500 558 C454 556 414 528 432 498 Z" fill="#101010"/>
<path d="M455 500 C470 486 492 486 500 494" stroke="#9a9a9a" stroke-width="7" fill="none" stroke-linecap="round"/>
<ellipse cx="466" cy="527" rx="14" ry="10" fill="#000"/><ellipse cx="534" cy="527" rx="14" ry="10" fill="#000"/>
<path d="M500 558 L500 612 M500 612 C468 634 432 628 410 612 M500 612 C532 634 568 628 590 612" stroke="#1c1c1c" stroke-width="8" fill="none" stroke-linecap="round"/>
<path d="M462 620 h76 v78 c0 16 -9 24 -20 24 h-36 c-11 0 -20 -8 -20 -24 Z" fill="#f4f4f4" stroke="#121212" stroke-width="7"/>
<path d="M500 620 v100" stroke="#121212" stroke-width="6"/>
<path d="M470 640 v60 M512 640 v60" stroke="#999" stroke-width="4"/>
<ellipse cx="368" cy="372" rx="27" ry="23" fill="url(#olho)"/><circle cx="359" cy="363" r="6.5" fill="#fff"/>
<ellipse cx="632" cy="372" rx="27" ry="23" fill="url(#olho)"/><circle cx="623" cy="363" r="6.5" fill="#fff"/>
<path d="M330 360 C342 336 392 330 406 352" stroke="#2c2c2c" stroke-width="8" fill="none"/>
<path d="M594 352 C608 330 658 336 670 360" stroke="#2c2c2c" stroke-width="8" fill="none"/>
<path d="M335 392 C355 402 385 402 400 392" stroke="#555" stroke-width="5" fill="none"/>
<path d="M600 392 C615 402 645 402 665 392" stroke="#555" stroke-width="5" fill="none"/>
<g stroke="#111" stroke-width="3" fill="none" stroke-linecap="round">
<path d="M378 572 C300 552 220 556 150 580"/><path d="M380 592 C300 594 230 612 170 646"/><path d="M386 612 C320 630 268 668 232 712"/><path d="M380 582 C310 570 250 572 200 588"/>
<path d="M622 572 C700 552 780 556 850 580"/><path d="M620 592 C700 594 770 612 830 646"/><path d="M614 612 C680 630 732 668 768 712"/><path d="M620 582 C690 570 750 572 800 588"/>
</g>
</svg>'''
open("castor.svg","w").write(svg)
