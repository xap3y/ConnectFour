# MC Connect4 GUI Minigame

Minecraft GUI minigame created with my menu API [XaGui](https://xagui.xap3y.eu/docs/). \
Supports any bukkit platform from 1.8 up to 1.20.6 \
 \
1.21 is only supported on CraftBukkit and Spigot platforms. \
PaperSpigot 1.21 will be supported soon! Because I have to setup gradle multiproject to support paper 1.21 API

## Support
- Java version from 8 up to 22
- 1.8.8-1.21 Spigot
- 1.8.8-1.21 CraftBukkit (without clickable chat messages)
- 1.8.8-1.20.6 PaperSpigot (1.21 soon!)

<details>
<summary>Tested on</summary>

- 1.8.8 Spigot, CraftBukkit, PaperSpigot (java 8)
- 1.21 Spigot, CraftBukkit (java 21, 22)
- 1.20.6 PaperSpigot (java 17)
- 1.16.5 PaperSpigot (java 11)
- 1.12.2 PaperSpigot (java 11)
- 1.13.2 PaperSpigot (java 11)
</details>

## Showcase 

<img src="https://xap3y.eu/static/con4.png">

## TODO
- [ ] Token falling animation
- [ ] Win animation
- [ ] Stats storaging
- [ ] AI
- [ ] Configurable win rewards
- [ ] Gradle multiproject

## How to play

`/cf invite <player>`
`/cf accept`
`/cf reject`

### Building from source
1. Clone the repository
2. Run `./gradlew shadowJar` or `gradlew.bat shadowJar`
3. Copy the jar from `build/libs` to your plugins folder