<div align=center>

# MC Connect4 GUI Minigame

Minecraft GUI minigame created with my menu API [XaGui](https://xagui.xap3y.eu/docs/). \
Supports any bukkit platform from 1.8 up to 1.21

## Showcase

<img src="https://xap3y.eu/static/con4.png">

</div>

## Support
- Java version from 8 up to 22
- 1.8.8-1.21 Spigot
- 1.8.8-1.21 CraftBukkit (without clickable chat messages)
- 1.8.8-1.21 PaperSpigot
- 1.16.5-1.21 Purpur
- 
<details>
<summary>Tested on</summary>

- 1.8.8 Spigot, CraftBukkit, PaperSpigot
- 1.21 Spigot, CraftBukkit, PaperSpigot
- 1.20.6, 1.19.4, 1.16.5, 1.12.2, 1.13.2, 1.11.2, 1.10.2 PaperSpigot
- 1.21, 1.20.6, 1.16.5 Purpur

</details>



## Commands

`/cf` \
`/cf invite <player>` \
`/cf accept <player>` \
`/cf reject <player>` \
`/cf stats [player]` \
`/cf leaderboard` \
`/cf reload`

## Placeholders
Supports PlaceholderAPI and miniPlaceholers

`%connectfour_wins%` \
`%connectfour_losses%` \
`%connectfour_draws%` \
`%connectfour_played%` \
`%connectfour_winrate%` 

## Configuration

Automatically generated configuration file is located in `plugins/ConnectFour/config.yml` \
The configuration is explained in the file itself.

<div align=center>

## Support me

[![Ko-Fi]](https://ko-fi.com/encryptsl)

</div>

### Building from source
1. Clone the repository
2. Run `./mvn clean install -P Impl`
3. Copy the jar from `core/target/` into your plugins folder

[Ko-Fi]: https://storage.ko-fi.com/cdn/brandasset/kofi_s_tag_white.png