## Note on future of the project

Due to the combination of me losing interest in Forge modding, and core modding in general being incredibly arduous on Forge, this project is no longer actively maintained.

I'm otherwise open to the prospect of occasionally contributing code to any future fork of the project.

Note: If you want a different mod that gates the movement behind a full-fledged content tree, a handful of these movement mechanics will eventually be included as a key mechanic in my (actively developed) Fabric mod, [Alchym](https://github.com/deyahruhd/alchym).

SPMquake
======

*[Squeek Pro Mode Quake]*

A fork of Squake that brings CPM-style air control, strafejumping, and trickjumping to Minecraft.

## What is different about this fork?

The mod aims to reproduce many movement mechanics of the Challenge Pro Mode Arena mod for Quake III Arena.

Since Quake 3 and Quake 1, and GoldSrc are idTech engine derivative games, they share some movement mechanics that many people may be familiar with, like trimping and bunnyhopping. However, Quake III Arena adds many more movement tricks resulting from its differences to GoldSrc.

A full list of what movement mechanics SPMquake reproduces: 
- CPM-like movement scheme. The movement in this mod is a combination of three air control styles, where diagonal directions (+forward/+back + +left/+right) activate Q3A air physics, the sole strafe keys (+left/+right) activate Q1 air physics (bunnyhopping), and the sole forward and backward keys (+forward/+back) activates an "air steer" mode for adjusting air trajectories.
- Quake 3- and Quake 1-exclusive air acceleration. If desired, you can choose to have VQ3 air acceleration (slow and mostly linear, but fast), Q1 air acceleration (air control), or even the anisotropic CPM movement scheme without the unique +forward/+back air-steering mechanic.
- Wall clipping. This is, in the Q3A engine, a "reintroduction" of a bug where the player can retain the speed they were traveling after colliding with the edge of level geometry. In SPMquake, this would mean that slightly grazing the edge of blocks will preserve your speed throughout the collision, and slingshot you as you slide away from the block.
- Material friction. The more slicked a surface is, the more speed players can gain from circle jumps off that surface. With a perfectly frictionless block, you conserve all momentum gained from ground acceleration - in other words, you go very, very fast. In CPM DeFRaG this is known as *slick*.
- Knockback ground boosting. A slight oversight in Q3A amplified by CPMA, players could use plasma or explosions to gain slicked movement on any surface for a short period of time, and gain an absurd amount of speed by a ground boost. In SPMquake, this is activated upon receiving knockback from an explosion, and if you time your inputs right you can accelerate yourself quickly.
- Ramp jumping. Stairs are treated as 45 degree ramps and will deflect players' velocity by such a ramp if they collide with them. This allows you to gain speed by falling onto a flight of stairs, or perform a trimp jump by hitting the flight of stairs horizontally.
- Overbounce. A strange bug resulting from how the Q3A physics engine calculates where it should place the player on the ground. In certain circumstances, the interaction of the ground placement code and the velocity clipping code allows you to bounce off the ground after a fall, which either bounces you directly back upwards (a vertical overbounce), or transform your vertical momentum into horizontal momentum (a horizontal overbounce).
  There is a built-in detector in SPMquake to determine if the player will trigger an overbounce while falling. 
- Dashing. A mechanic from the CPMA clone WarSow, which allows you to dash in any of the 8 directions specifiable by combinations of +forward, +back, +left, and +right, while also conserving all of your momentum.

## Mutator System

All of these movement mechanics are implemented through the use of *mutators*, inspired by the components of the same name from Unreal Tournament. Mutators are movement components that modify the player's movement physics either unconditionally or conditionally (based on a heirarchical system).

A movement set is a combination of several mutators, which can be decomposed into *overrides*, *bases*, and *passives*.

- **Override** mutators activate when a certain movement input is pressed and/or held down, and act on a player's ground-acceleration, air-acceleration, pre-movement, and post-movement stages. If they specify, they can choose to completely override a movement input, in which any lower-priority mutators will cease to activate in response - hence being called *overrides*.
  Override mutators are always considered first when processing player input into acceleration.
- **Base** mutators activate on all movement inputs, and implement how the player should generally accelerate on the ground or in the air. If a higher-priority override mutator overrides a movement input, then the underlying base mutator will not receive that input, and thus not activate. Base mutators are always considered second-in-line, after processing every override.
- **Passive** mutators do not respond to any movement inputs, but always activate on every physics tick. They are typically used to implement mechanics that do not involve using the movement keys, like wall clipping and overbouncing. Passives are always considered last in the hierarchy of mutators for a movement set.

Through this system, it should be theoretically possible to implement any game's movement physics as long as that game's movement logic follow this general procedure:

- *Physics tick starts*
- Ground acceleration/air acceleration/water acceleration (player's velocity is changed) **^**
- Pre-movement logic
- Movement (player is displaced by velocity, and clipped against the world's blocks) **^^**
- Post-movement logic
- *Physics tick ends.* 

**^** Water acceleration isn't accounted for yet (defaults to vanilla movement), eventually this will be fixed if I or someone else implements Quake water movement.

**^^** Note that Minecraft updates at 20 ticks per second, and on each tick the player is moved exactly by their velocity vector. The movement step is not explicitly overridable by mutators. If you need to get around this (for example: performing *n* sub-tick movements and other logic within those sub-ticks), modify the pre-movement step to perform the movement step, and set the player's velocity to 0 at the end so that they are not displaced by the movement step.
