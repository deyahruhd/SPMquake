[SPMquake](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2181015-squake-quake-style-movement-for-minecraft)
======

*[Squeek Pro Mode Quake]*

A fork of Squake that brings CPM-style air control, strafejumping, and trickjumping to Minecraft.

## What is different about this fork?

The mod is both a reproduction of the movement mechanics of the Challenge Pro Mode Arena mod for Quake 3 Arena, and an attempt to balance the mod and
bring it more in-line with vanilla survival mechanics.

As such, the mod implements several Quake-specific mechanics not included in Q1/GoldSrc engine derivatives, unlike Squake:
- CPM-like movement scheme. The movement in this mod is a combination of two air control styles, where diagonal directions (W + A/D) and forward (W) activate Q3A air physics, while the sole strafe keys (A/D) activate Q1 air physics.
- Crouch sliding. Players can crouch slide on gravel and grass path blocks to make sharp turns. Unlike in Quake 4, it is not possible to gain speed from this movement.
- Wall clipping. This is, in the Q3A engine, a "reintroduction" of a bug where the player can retain the speed they were traveling after colliding with the edge of level geometry. In SPMquake, this would mean that slightly grazing the edge of blocks will preserve your speed throughout the collision, and slingshot you as you slide away from the block.
- Material friction. The more slicked a surface is, the more speed players can gain from circle jumps off that surface. Modpack makers and modders can add blocks with perfect slickness but these blocks should be made rare for this reason.
- Knockback ground boosting. A slight oversight in Q3A amplified by CPMA, players could use plasma to gain zero friction movement on any surface for a short period of time, and gain an absurd amount of speed by a plasma ground boost (PGB). In SPMquake, this is activated upon receiving knockback from an explosion, and if you time your inputs right you can accelerate yourself quickly.
- Ramp jumping. Players can gain three times jump height by cleverly timing jumps near a ramp or set of stairs to trigger the jump motion twice. In SPMquake, this effect can be activated if a player performs a secondary jump very shortly after the first, which can usually be achieved using stairs.

SPMquake also gives modpack developers options to help balance the mod for modpacks. In particular:
- The server configuration is always force-mirrored to the client's, preventing players from changing parameters and essentially cheating.
- Modpack creators can assign armor item(s) the ability to give players SPMquake physics, gating the movement behind an item.
- Flexible configuration for hunger-based or general speed caps: Speed is initially uncapped for as long as the player has more than three hunger shanks by default, and bunnyhops at sufficiently great speeds will consume large amounts of player saturation. Once they reach below three hunger shanks, players will face a soft speed cap which drags them to a pre-defined limit.

Lastly, SPMquake brings a few quality of life fixes and additions to the game to improve the bunnyhopping experience:
- Addition of a toggleable on-screen button indicator showing the relevant movement keys the player is pressing.
- Disabling the vanilla sprinting mechanic if the movement physics are active for a specific player.
- Convertion of server-sided explosion knockback into client-sided knockback (so that rocket jumps are more responsive)
- Prevents servers from force-setting players velocities to 0 through knockback-related code. This fixes issues with players receiving damage and immediately losing all their speed.