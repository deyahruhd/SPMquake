SPMquake
======

*[Squeek Pro Mode Quake]*

A fork of Squake that brings CPM-style air control, strafejumping, and trickjumping to Minecraft.

***(This mod is functional but still WIP. it's perfectly installable in modded instances but don't expect every new thing to work perfectly. ;) )***

## What is different about this fork?

The mod is both a reproduction of the movement mechanics of the Challenge Pro Mode Arena mod for Quake 3 Arena, and an attempt to provide measures to balance the mod and bring it more in-line with vanilla survival mechanics.

As such, the mod implements several Quake-specific mechanics not included in Q1/GoldSrc engine derivatives, unlike Squake:
- CPM-like movement scheme. The movement in this mod is a combination of two air control styles, where diagonal directions (W + A/D) and forward (W) activate Q3A air physics, while the sole strafe keys (A/D) activate Q1 air physics. ***(Sole W/S to activate air steering will be added soon when I have the time; unlike Q1 and Q3A it isn't a simple change of acceleration parameters)***
- Crouch sliding. Players can crouch slide on gravel and grass path blocks to make sharp turns. Unlike in Quake 4, it is not possible to gain speed from this movement ***(likely configurable in the future, but this will require a revamp of how it is currently done)***
- Wall clipping. This is, in the Q3A engine, a "reintroduction" of a bug where the player can retain the speed they were traveling after colliding with the edge of level geometry. In SPMquake, this would mean that slightly grazing the edge of blocks will preserve your speed throughout the collision, and slingshot you as you slide away from the block.
- Material friction. The more slicked a surface is, the more speed players can gain from circle jumps off that surface. Modpack makers and modders can add blocks with perfect slickness but these blocks should be made rare unless you want your entire base to be a slick map.
- Knockback ground boosting. A slight oversight in Q3A amplified by CPMA, players could use plasma to gain zero friction movement on any surface for a short period of time, and gain an absurd amount of speed by a plasma ground boost (PGB). In SPMquake, this is activated upon receiving knockback from an explosion, and if you time your inputs right you can accelerate yourself quickly.
- 'Double' jumping. Players can gain three times jump height by cleverly timing jumps within a short period of time to invoke the jumping motion twice, gaining a higher jump. In SPMquake, this effect can be activated if a player performs a secondary jump very shortly after the first, which can usually be achieved using stairs. ***(At the moment, this doesn't work properly both due to a weird interaction with wall clipping plus the vanilla "step up" code. It's also broken in multiplayer for some reason. But I'll figure out a way to fix it)***

SPMquake also gives modpack developers options to help balance the mod for modpacks. In particular:
- The server configuration is always force-mirrored to the client's, preventing players from changing parameters and essentially cheating.
- Modpack creators can assign armor item(s) the ability to give players SPMquake physics, gating the movement behind an item.
- Flexible configuration for hunger-based or general speed caps: Speed is initially uncapped for as long as the player has more than three hunger shanks by default, and bunnyhops at sufficiently great speeds will consume large amounts of player saturation. Once they reach below three hunger shanks, players will face a soft speed cap which drags them to a pre-defined limit. ***(Speed decrease from lack of hunger isn't implemented yet, and I removed the code for hard/soft caps accidentally; this will be added back in soon.)***

Lastly, SPMquake brings a few quality of life fixes and additions to the game to improve the bunnyhopping experience:
- Addition of a toggleable on-screen button indicator showing the relevant movement keys the player is pressing.
- Disabling the vanilla sprinting mechanic if the movement physics are active for a specific player.
- Convertion of server-sided explosion knockback into client-sided knockback. If you have a weapon intended to launch you with explosion knockback expect it to feel much better.
- Prevents servers from force-setting players velocities to 0 through knockback-related code (in technicality, it adds in the knockback received, inversely proportional to the player's speed). This fixes issues with players receiving damage and immediately losing all their speed and also using Punch bows to gain ridiculous amounts of speed.
