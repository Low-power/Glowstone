![Built with Love](http://forthebadge.com/images/badges/built-with-love.svg)

# Glowstone

This is a fork or original Glowstone that supporting Java 7.

A free reimplementation of Minecraft server software.

## Introduction

Glowstone is a lightweight, from scratch, open source
[Minecraft](http://minecraft.net) server written in Java that supports plugins
written for the Bukkit API.

The main goals of the project are to provide a lightweight implementation
of the Bukkit API and Minecraft server where exact vanilla functionality is
not needed or higher performance is desired than the official software can
deliver. Glowstone makes use of a thread-per-world model and performs
synchronization only when necessitated by the Bukkit API.

Still have questions? Check out our [FAQ](https://github.com/GlowstoneMC/Glowstone/wiki/Frequently-Asked-Questions).

## Features

Glowstone has a few key advantages over CraftBukkit:
 * It is **100% open source**. While CraftBukkit and most other mods are open
   source, they rely on decompiled proprietary Minecraft source code.
   Glowstone's code is completely original and free.
 * Because of this, it is easy to contribute to Glowstone's development. The
   barrier of entry to contributions is lower because there is no need to work
   around decompiled source or maintain a minimal diff.
 * Glowstone supports all plugins written for the Bukkit, Spigot and some
   Paper APIs natively. In practice, some plugins may try to make use of
   parts of the API which are not yet implemented, but in a completed state
   Glowstone would support all Bukkit plugins.
 * Glowstone's simplicity affords it a performance improvement over CraftBukkit
   and other servers, making it especially suited for situations where a large
   amount of players must be supported but vanilla game features are not needed.
 
However, there are several drawbacks:
 * Glowstone **is not finished**. Nothing is guaranteed to work, though many things
   are likely to. If in doubt, file an issue.
 * Bukkit plugins which expect the presence of CraftBukkit-specific code
   (that are in the `org.bukkit.craftbukkit` or `net.minecraft.server` packages)
   will not work on Glowstone unless they are designed to fail gracefully.
 * Glowstone is not produced by the Bukkit team, and while we do make an effort
   to produce quality work, Glowstone does not undergo the same rigorious testing
   as the Bukkit project.
   
For a current list of features, [check the wiki](https://github.com/GlowstoneMC/Glowstone/wiki/Current-Features).

## Credits

 * [The Minecraft Coalition](http://wiki.vg/) and [`#mcdevs`](https://github.com/mcdevs) -
   protocol and file formats research.
 * [The Bukkit team](https://bukkit.org) for their outstandingly well-designed
   plugin API.
 * [The SpigotMC team](https://spigotmc.org/) for updating and enhancing
   the Bukkit plugin API.
 * [AquiferMC](https://aquifermc.org/) for further enhancing the Bukkit API.
 * [The SpongePowered Team](https://www.spongepowered.org/) for
   creating the Sponge API.
 * [Trustin Lee](https://github.com/trustin) - author of the
   [Netty](http://netty.io/) library.
 * [Graham Edgecombe](https://github.com/grahamedgecombe/) - author of the
   original [Lightstone](https://github.com/grahamedgecombe/lightstone).
 * [Tad Hardesty](https://github.com/SpaceManiac) and [all the contributors](https://github.com/GlowstoneMC/Glowstone-Legacy/graphs/contributors) to Glowstone Legacy.
 * All the people behind [Maven](https://maven.apache.org/team-list.html) and [Java](https://java.net/people).
 * [Notch](http://notch.tumblr.com/) and
   [Mojang](http://mojang.com/about) - for making such an awesome game in the first
   place!

## Copyright

Glowstone is open-source software released under the MIT license. Please see
the `LICENSE` file for details.

Glowkit is open-source software released under the GPL license. Please see
the `LICENSE.txt` file in the Glowkit repository for details.

