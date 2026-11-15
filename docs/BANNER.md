# Banner LynowBOM

Arquivo final: `assets/lynowbom-banner.png` (2172 × 724). Mascote isolado: `assets/project-animal.png`.

Composição baseada nos banners de LynowCut e LynowCatch: fundo quase preto `#090A08`, tile arredondado com o animal em gravura à esquerda e logotipo em Inter Display Black, com `Lynow` em marfim `#F3EEE1` e `BOM` em `#1FBF80`.

## Como a arte foi produzida

Diferente dos banners anteriores, a gravura do castor foi gerada por código, sem ferramenta de geração de imagens:

1. `assets/arte/castor.py` desenha o castor como formas vetoriais sombreadas (SVG com gradientes e fios de pelo).
2. `assets/arte/gravura.py` converte os tons em linhas de buril com espessura variável, hachura cruzada nas sombras e pontilhado nas meias-luzes, sobre o tile verde.
3. `assets/arte/banner.py` monta o banner e ajusta o tamanho do logotipo às margens.

```bash
python castor.py
python gravura.py castor.svg "#14A06B" castor-tile.png
python banner.py castor-tile.png BOM "#1FBF80" lynowbom-banner.png
```

Requer Python 3, Pillow, NumPy, `rsvg-convert` e a fonte Inter Display.

## Para refazer com geração de imagens

Se preferir igualar o acabamento ao dos outros banners, use o mesmo fluxo do LynowCatch com este prompt e substitua apenas o tile:

> Use case: logo-brand. Wide GitHub repository banner for LynowBOM matching input banner composition: ultra-wide 3:1 near-black #090a08 background, large rounded square mascot tile at left, huge heavy clean sans-serif logotype at right. Mascot: a beaver bust in detailed ivory engraving style with fine cross-hatching, front-facing, prominent incisors, on emerald green #14A06B tile. Exact single word text: LynowBOM. 'Lynow' in warm off-white and 'BOM' in bright emerald #1FBF80. No tagline, no border, no shadow.
