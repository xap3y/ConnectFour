# MC Connect4 GUI Minigame

Minecraft GUI minigame created with my menu API [XaGui](https://xagui.xap3y.eu/docs/). \
Supports any bukkit platform from 1.8 up to 1.21 

> [!WARNING]  
> PaperSpigot 1.21 is supported, but not stable, can be buggy

## Support
- Java version from 8 up to 22
- 1.8.8-1.21 Spigot
- 1.8.8-1.21 CraftBukkit (without clickable chat messages)
- 1.8.8-1.21 PaperSpigot

<details>
<summary>Tested on</summary>

- 1.8.8 Spigot, CraftBukkit, PaperSpigot
- 1.21 Spigot, CraftBukkit, PaperSpigot
- 1.20.6 PaperSpigot
- 1.16.5 PaperSpigot 
- 1.12.2 PaperSpigot 
- 1.13.2 PaperSpigot

</details>

## Showcase

<img src="https://xap3y.eu/static/con4.png">

## TODO
- [ ] Token falling animation
- [ ] Win animation
- [ ] Stats storaging
- [ ] Configurable win rewards

## How to play

`/cf invite <player>` \
`/cf accept` \
`/cf reject`

### Building from source
1. Clone the repository
2. Run `./mvn clean install -P Impl`
3. Copy the jar from `core/target/` into your plugins folder
