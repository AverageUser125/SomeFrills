# SomeFrills Mod

It's in the name. Simple and effective Hypixel Skyblock mod for modern versions (1.20+) of Minecraft.
But still has some frills, of course.

## Features

<details>
<summary>Click to expand feature list</summary>

- **General**
-
    - This project is a lightweight collection of small quality-of-life and gameplay tweaks for Hypixel Skyblock.

- **Tweaks**
    - BreakResetFix — fixes an issue where block-breaking state could be reset incorrectly.
    - CameraTweaks — small adjustments to camera behavior to improve usability.
    - DoubleUseFix — prevents accidental double-activation of items/abilities.
    - MiddleClickOverride — repurposes middle-click behavior for convenience in some situations.
    - NoAbilityPlace — prevents placing blocks when certain abilities are active.
    - NoGhostBlocks — mitigates ghost/block desync issues that can leave unplaceable blocks.

- **Misc**
    - Aliases — shorthand command aliases and convenience shortcuts.
    - FilterMessages — filters or hides unwanted chat messages.
    - Freecam — a free camera mode for looking around without moving your player.
    - GlowPlayer — highlights players with a glow effect.
    - NpcLocator — shows locations of important NPCs on-screen or via markers.
    - SaveCursorPosition — remembers and restores your cursor position in certain UIs.
    - GlowBlock — highlights blocks (glow) to make them easier to spot.
    - GlowMob — highlights mobs with a glow effect.

- **Solvers**
    - ChocolateFactory — automated assistance / helper for Chocolate Factory.
    - ExperimentSolver — automated solver for experiment table puzzles.


- **Fishing**
    - (No fishing-specific features in the current build.)


- **Hunting**
    - (No hunting-specific features in the current build.)


- **Dungeons**
    - (No dungeon-specific features in the current build.)


- **Kuudra**
    - (No Kuudra-specific features in the current build.)


- **Slayer**
    - (No slayer-specific features in the current build.)


- **Mining**
    - CorpseHighlight — highlights mineshaft corpse locations.
    - GemstoneDesyncFix — fixes desyncs related to gemstone pickups/visibility.
    - GhostVision — visual aid to show dwarven mines ghosts.
    - NoMiningTrace — removes persistent mining traces that can clutter the world or cause desync.


- **Farming**
    - AutoPestSetHome — automatically sets a home or waypoint when a pest spawns.
    - AutoWarpHome — automatically warps home when all pests are killed.
    - SpaceFarmer — allows farminig with space bar instead of mouse.


</details>

<details>
<summary>Click to expand command list</summary>

 - **Mod Commands** (accessed under `/somefrills`, or `/sf` for short)
    - `/somefrills` or `/sf` — opens the mod configuration screen where all features can be enabled/disabled and configured.
    - `/sf settings` — Opens the settings GUI.
    - `/sf glowplayer` — Manage glowing players.
    - `/sf glowmob` — Manage glowing mobs.
    - `/sf glowblock` — Manage glowing blocks.
    - `/sf npclocator` — Locate important NPCs.
    - `/sf freecam` — Activate free camera mode.

</details>

## Installation

- Head over to the [versions page](https://github.com/AverageUser125/SomeFrills/releases/), download the most recent release for
  your Minecraft version, and add it to your "mods" folder.
- Additional dependencies needed to launch the mod:
    - [Fabric API](https://modrinth.com/mod/fabric-api)
    - [Mod Menu](https://modrinth.com/mod/modmenu) (Optional, settings can be accessed with `/somefrills`, or `/sf`)
- To finish off, open the mod's settings screen, and configure it to your liking.

## Incompatibilities

- Some highlight features might not render correctly with Iris shaders active.
- The mod may not work with third party loaders such as Lunar or Feather, only Fabric is supported.
    - If you aren't using Fabric already, it is recommended you upgrade to either
      the [Modrinth App](https://modrinth.com/app) or the [Prism Launcher](https://prismlauncher.org/). Automatic mod
      updates, instance management, and most
      importantly: [no selling of your personal data](https://www.lunarclient.com/do-not-sell-or-share-my-personal-information).

## Credits

- [Orbit](https://github.com/MeteorDevelopment/orbit): Event system which keeps the mod blazing fast.
- [Skyblocker](https://github.com/SkyblockerMod/Skyblocker): Has more info on how YACL works than its own wiki, also a
  source for the odd function such as reading scoreboard lines.
- [clientcommands](https://github.com/Earthcomputer/clientcommands): Taught me rendering magic with mixins.
- [Skyhanni-REPO](https://github.com/hannibal002/SkyHanni-REPO): For NPC location data, and general Skyblock info.
- [Meteor Client](https://github.com/MeteorDevelopment/meteor-client): For ALOT!