# Third-party assets

`core/src/main/resources/mario-atlas.png` is a 512 x 512 runtime atlas assembled
from Super Mario World sprite sheets indexed by
[Mario Universe](https://www.mariouniverse.com/sprites-snes-smw/). It contains
only the three backgrounds, directional animation frames, terrain variants,
blocks, items, effects, and goal pieces used by `MarioScreen`. Left-facing
frames are mirrored atlas entries so renderers never need negative texture
coordinates.

Source sheets and embedded credits:

- `mario-4.png`: Small and Super Mario frames; ripped by Mister Man.
- `goombas.png`: Galoomba frames; ripped by NES Boy.
- `objects-animated.png`: coin frames; ripped by Barack Obama.
- `objects-2.png`: solid block; ripped by Barack Obama.
- `tiles.png`: grass and soil edges/corners, question and used blocks, the
  breakable turn block, mushroom, and pipe pieces; ripped by Viper.
- `tiles-ground.png`: the four concave/reflex grass corners; ripped by Viper.
- `goal-post.png`: goal-post, tape, and sparkle pieces; ripped by DogToon64.
- `backgrounds.png`: mountain, forest, and angular-hill backgrounds; ripped by
  Beam Luinsir Yosho.

Super Mario World characters and artwork are owned by Nintendo. This notice
records the atlas provenance and does not replace or modify any upstream terms.
