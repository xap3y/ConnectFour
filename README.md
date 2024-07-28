<div align=center>

# MC Connect4 GUI Minigame

Minecraft GUI minigame created with my menu API [XaGui](https://xagui.xap3y.eu/docs/). \
Supports any bukkit platform from 1.8 up to 1.21


[![bStats Servers](https://img.shields.io/bstats/servers/22557)](https://bstats.org/plugin/bukkit/ConnectFour/22557)
[![CodeFactor](https://www.codefactor.io/repository/github/xap3y/connectfour/badge)](https://www.codefactor.io/repository/github/xap3y/connectfour) \
![Spiget Downloads](https://img.shields.io/spiget/downloads/117864)
[![Test build maven](https://github.com/xap3y/ConnectFour/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/xap3y/ConnectFour/actions/workflows/maven-publish.yml)

## Showcase

<img src="https://xap3y.eu/static/con4.png">

</div>

## Support
- Java version from 8 up to 22
- 1.8.8-1.21 Spigot
- 1.8.8-1.21 CraftBukkit (without clickable chat messages)
- 1.8.8-1.21 PaperSpigot
- 1.16.5-1.21 Purpur

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
The configuration is explained in the file itself. <br>

You can also modify messages in `plugins/ConnectFour/lang/messages-en.yml`

<div align=center>

## Support me

[![Ko-Fi]](https://ko-fi.com/xap3y)

</div>

### Building from source
1. Clone the repository
2. Run `./mvn clean install -P Impl`
3. Copy the jar from `core/target/` into your plugins folder

[Ko-Fi]: https://storage.ko-fi.com/cdn/brandasset/kofi_s_tag_white.png

### Contributing

1. Fork this repository
2. Create a new branch for your changes: git checkout -b my-feature
3. Commit your changes: git commit -am "Add my feature"
4. Push the branch: git push origin my-feature
5. Open a pull request
