### 1.7.9 Release (30.05.2021 - 01.08.2021)
* Added forcestart item
* Added murderer and detective pass (Command and Item to activate)
* Added new stat (new tables on mysql needs to be manually added murderer_pass, detective_pass *or* recreated)  
* Added special_items.yml
* Added 1.17 support
* Reuse XMaterial for specialitems
* Fix NoSuchMethodError when hasCooldown method is not existed in 1.11 and lowest versions
* Removed lobbyitems.yml
* Removed Rainbow Bed option
>>> beta1 
* Fixed party (parties v3.1.0) not working anymore
* Fixed when plugins scoreboards not re-appeared after game end
* Fixed join maxplayers did not match the arena with highest player amount
* Removed temp added scoreboard rewards

### 1.7.8 Release (15.04.2021 - 10.05.2021)
* Added /mm join maxplayers which tries to get the arena with the highest amount of players
* Added placeholder arena_players_online
* Added new RewardType scoreboard_add
* Changed Now the arena will be set to isDone false if the world is not exist, after the plugin loaded.
* Changed randomjoin mechanics -> Searches for starting -> random join -> Searches for waiting -> random join
* Fixed arrow trails are only on player

### 1.7.7 Release (31.03.2021 - 13.04.2021)
* Fixed NoSuchElementException when iterating through players in gold locations but there is no player available.
* Fixed issue when there was no murderer in game and it caused to not choose another one.
* Fix when arena selector GUI does not opened
* Fixed NPE on ArrowEvents and other version improvements

### 1.7.6 Release (19.03.2021)
* Fixed Last words & trails for non op players
* Fixed particle issues
* Fixed invisible bug for spectators that joined while ingame
* Fixed trail particle are random even on bow shoot  
* Changed config.yml Item values of states and added all states
* Added trails blacklist

### 1.7.5 Release (01.02.2021 - 14.03.2021)
* Added modifiable arena state names to language
* Added configurable map selector items
* Added legacy support
* Added support for multiple last words that will randomize
* Added a lot of new Trails (Should support every particle name that is supported since 1.8)
* Added TeleportArgument (/mma tp)
* Added new RewardType scoreboard_remove
* Changed trails will now randomize if you got perms for more than one  
* Cached armor stands will be removed after block break if type is matches
* Now special block holograms will get removed after game stop, after game start, it will show again
* Fixed CME error when removing special blocks

### 1.7.4 Release (25.01.2021 - 28.01.2021)
* Fix stack trace array length is 0 when trying to send exception to service
* Updated commonsbox to fix actionbar problems
* Fixed spectators can not see each other after joining game while being ingame

### 1.7.3 Release (18.01.2021 - 22.01.2021)
* Fixed mm items are not removed if inventory manager is enabled and the server is stopping
* Fixed 2nd last char of name gets removed if only one murderer

### 1.7.2 Release (10.01.2021 - 18.01.2021)
* Fix NPE for armor stand hologram location is null
* Fixed issue when sign instances section not found in file
* Fixed arena join permission
* Added 1.11+ compatibility
* Added murderer and detective shuffler to avoid the same characters when you are only playing with the same people
* Added option to disable death message completely 

### 1.7.1 Release (04.01.2021)
* Changed summary message for one murderer round
* Changed Innocents will be killed if they kill the detective (if ENABLE_KILL_DETECTIVE_IF_INNOCENT_KILLED enabled)
* Added customizable item name in arena selector gui

### 1.7.0 Release (29.12.2020)
* Rewritten scoreboardlib (fixed flickering and color matching) -> 122 chars for 1.14 up and 48 for lower versions
* Fixed issue that murderer is not removed from alive list if there are only 2 players left
* Fixed player teleport on void death while in lobby stage
>>> b2
* Attempt to fix scoreboard problems
>>> b1
* Fix NoSuchMethodError when CommandSender#spigot does not exists on 1.11
* Updated ScoreboardLib to fix problems with color matching
* Now spectators will not launch fireworks once game stopped

### 1.6.9 Release (28.11.2020)
* Fixed joining through a sign while hotbar slot for leave-item is active
* Fixed gold visuals broke the whole plugin while enabled

### 1.6.8 Release (10.11.2020)
* Fixed particle effects not working on 1.13+

### 1.6.7 Release (02.11.2020 - 08.11.2020)
* Added particle effect to gold spawn locations to show where gold will spawn
* Added death/spectator roles therefore users will not get rewards if they are spectator
* Added rainbow beds

### 1.6.6 Stable (31.10.2020)
* Fixed armorstands are not correctly displayed
* Fixed dead body heads 
* Fixed locales not working
* Fixed heads
* Fixed sign loading
* Fixed sign after arena validation
* Fixed new arena is not proper registered 
* Fixed scoreboard friendly fire caused players visible 
* Fixed arrows can damage entities outside the game
* Changed disabled arrow bouncing

### 1.6.5 (07.10.2020 - 29.10.2020)
* Added special blocks remove command /mma removeblock
* Fixed hologram spawn item height (bow) 
* Fixed the bow item got not removed on some cases
* Another fix for cauldron click event
* Fixed locations of special blocks on setup menu
* Optimized special block click performance
* Fixed stacked armorstands
* Fixed players will die under water

### 1.6.4 Hotfix (05.10.2020)
* Fixed cauldron click event
* Fixed detectives will get bow from prayer

### 1.6.3 Release (19.09.2020 - 02.10.2020)
* Updated locales to latest version
* Added the possibility to disable the innocent locator
* Added the possibility to change messages of locales on language.yml 
* Fixed adding cauldrons to the arena
* Removed HolographicDisplays dependency

### 1.6.2 Release (15.08.2020 - 13.09.2020)
* (Developer Alert - Changed class names)
* Added support for 1.16 hex colors
* Added ArenaSelector
* Added config option to play sound when adding score to user
* Added new reward type gold_pickup
* Fixed NPE when trying to teleport players to lobby location
* Fixed no permission message not found
* Fixed goldrush & goldban prayer did not reset on game end
* Fixed MYSQL stats can't update
* Fixed special blocks registering click twice
* Fixed IllegalPluginAccessException on plugin disable
* Fixed wrong order in trails
* Fixed local values got no reset
* Changed Death prefix can now be changed
* Changed message delay of "Waiting for players" from 15 to 45 seconds 
* Optimized player corpse heads on paper
* Reworked version checker

### 1.6.1 Release (01.07.2020 - 27.07.2020)
* Added missing 1.14 & 1.15 & 1.16 Materials
* Added simple head corpse when no CorpseReborn is installed
* Added new reward event -> death
* Changed Reduce message will now only come on debug mode
* Changed mysql updates to do only one instead of more than 10 (should improve performance)
* Fixed spectators that wasn't ingame got rewards
* Fixed reset of contributions not applied when leaving ingame
* Fixed in rare cases is the speed not the default one after changing it with spectator menu
* Fixed sword is flying further after player hit
* Fixed separate chat (and prefix for death players)

### 1.6.0 Release
* Fixed spectators preventing arrows
* Fixed editor value "spawn gold time"
* Added bungeemode Arena shuffler
* Added option to disable separate chat
* Added 1.16.x compatibility 
* Added possibility to change table name of mysql database
* Removed stats save on quitevent as it wasn't necessary
* Fixed different sword cooldown time than 5 seconds
* Fixed bungeemanager enable null text error
* Optimized murderer leave before they got the sword
* Optimized the death respawn location 
* Fixed prayer multi gold

### 1.5.5 Hotfix
* Fixed language list getting

### 1.5.4 Release
* Added new config values to change arrow amount of fake detective and bow on gold
* Fixed properties getting on language list
* Disabled creating of reducedDependencyPom

### 1.5.3 Release
* Added a config value to disable detective killing on innocent kill
* Added a config value to change the spawner mode
* Added a way to disable gold spawn limit
* Added a config value to change the get bow from gold value (default: 10)
* Added Murderer speed can be disabled
* Added Murderer speed multiplier can be changed
* Added Now you can change the hit range and max range of the flying sword
* Fixed an 1.15.x reporter service plugin crash
* Fixed murderer can kill other murderer with bow
* Fixed amount of arrows on gold pickup
* Changed default language value as there does not exist /mm admin
* Updated setup tips Feedback page link
* Optimized sword fly 
* Fixed Stats will now get saved on restart stage
* Fixed stats were not loaded on bungee mode
* Fixed chances are not displayed on new players for the first round (occurred for all who using mysql database)

### 1.5.2 Release 
* Fixed Commands.Main-Command.Heads not found 

### 1.5.1 Release (20.03.2020) 
* Fixed broken language
* Added PlaceholderAPI for some per player messages
* Fixed Parties Integration 
* Attempt to fix player respawn

### 1.5.0 Release (20.12.2019 - 17.03.2020) (by Tigerpanzer)
* Fixed spectator gui opened on physical actions
* Added option to control the time between gold spawns
* Added better option to allow only 1 murderer/detectives
* Fixed division by null error
* Fixed murderer can kill other murderer
* Fixed bungeecord shutdown when game ends (Thanks to barpec12)
* Added option to hide chances
* Changed the setup radius of enchanting table and cauldron to 15
* PlaceholderAPI placeholders are no longer case sensitive
* Added arena state placeholders for PlaceholderAPI:
   * %murdermystery_{arena id}:{data}% where data types are:
      * players - amount of players in arena
      * max_players - max players in arena
      * state - current state raw enum name eg. STARTING
      * state_pretty - prettified name of enum eg. Waiting (instead WAITING_FOR_PLAYERS)
      * mapname - name of arena map
* Fixed mysql database - Create a new one and your stats should be saved correctly (Thanks to add5tar)
* Fixed loading arena values (Some values had a wrong file location)
* Added party dependency, you can join with more players to one arena
* Fixed murder gets sword as spectator
* Changed murder speed boost from potion to walkspeed to hide particles
* Added Connect-To-Hub boolean to bungee.yml
* Added End-Location-Hub boolean to bungee.yml

### 1.4.1 Release (05.12.2019 - 13.12.2019) (by Tigerpanzer)
* Added a new MOTD Manager in the bungee.yml (Now you can define the states on yourself) 
* Now the values in bungee.yml will work
* Arena will not be stopped anymore on starting phase
  when there are null players (only resetting it to waiting for players)
* You can now activate short commands (/start and /leave)
* Fixed incompatibility with chatcontrol
* Minecraft 1.15 compatible
* Fixed the murderer draw when murderer leaves ingame
* Fixed SummaryMessage (murderer will now be strike out)

### 1.4.0 Release (26.10.2019 - 16.11.2019) (by Tigerpanzer)
* Fixed incompatibility with essentials respawn
* You can now choose your own cooldown for sword attack/fly and bow shoot
* Clearer way of error reporting
* Added option to change murderer sword item
* Added options to specify arrows amount
* Added a way to configure how many murderers & detectives are selected for a game (definable per arena)
* Changed the way of drop bow (The bow will be dropped if the last detective die)
* Fixed scoreboard innocents count
* Added compass distance to bow and player locator
* Fixed the +- message for minus points in score messages
* Fixed the exp and level save on InventoryManager
* Fixed instant respawn bug on last murderer

### 1.3.1 Release (11/18.10.2019) (by Tigerpanzer)
* Fixed that you can sleep in beds
* Fixed player spawning after a death in blocks
* Fixed arena stop when it will be called due to reload, stop, delete
* Fixed the NoSuchMethodException on arena sign load when the sign isn´t a wallsign
* Dropped 1.11 support

### 1.3.0 Release (21.09.2019 - 03.10.2019) (by Tigerpanzer)
* Changed the way how randomjoin works (now it will search for the most players first)
* Fixed wrong location of corpse, hologram and bow spawn after player left the game
* Fixed when you took death prayer you could die in next game
* Fixed getting damage from fire after you join a game
* Fixed the hero name in the summary message was always none
* Inventories will now properly regenerate on plugin disable
* Changed that player will be a spectator when the starting time is under 3
* Added option to disable fall damage in the arena
* Fixed sound after player death that could be heard in lobby
* Fixed the ArrayIndexOutOfBoundsException when the murder left before the game starts (crashing game)
* Now players can´t interact with armorstands when they are in the arena
* Now players can´t destroy itemframes, paintings, and armorstands when they are in the arena
* Proofread and updated locales ~Plajer

### 1.2.1 Release (11/13.09.2019) (by Tigerpanzer)
* Added a lobby time shorter when the maximum player size is reached
* The sword will be now available at cooldown to hit someone as murder (after a 1 sec throw
cooldown because the sword is not available if you throw it)
* Fixed 1.14 ClassNotFoundException error when a paper server version is used
* Fix GUI opening on physical actions (Especially on redstone block) (thanks to BestMark)
* Fix null on getting corpse data from corpse reborn (thanks to BestMark)

### 1.2.0 Release (08/09.09.2019) (by Tigerpanzer)
* Added name tags hide feature in game
* Game will now properly end when murderer dies by environment
* Detective will now drop bow when dies by environment
* Fixed 1.14 NoClassFound error when user tries to pick up an arrow
* You shouldn't be teleported to spawn on death in game (you'll stay at the death location)
* Murderer will no longer throw the sword if you interact physically with redstone mechanisms
* Fixed issue that you could join full games before they started (in game you join as spectator)
now proper full game permission check will occur and do the thing to allow or disallow you to join
* /mm randomjoin will now really join random arena not first one it find good to join
* Updated bunch of locales
* Fixed Russian locale was broken
* Added Slovak and Turkish locales (thanks to POEditor contributors)
* Fixed bug that auto respawn didn't work

### 1.1.6 Release (13/29.07.2019)
* Changed Chinese (Simplified) locale prefix from zh to cn and Chinese (Traditional) from zh_hk to zh_tw
* Respawn will now properly work on 1.14
* Fixed /vda typo in force start command
* Prettified special blocks setup messages
* Dropped leaderheads support, leaderheads now uses PlaceholderAPI to retrieve statistics and we do support
PAPI so please use that

### 1.1.5 Release (12.07.2019)
* Fixed errors in console when joining game via sign

### 1.1.4 Release (15.06.2019 - 10.07.2019)
* Plugin will no longer send error messages `failed to register events` if corpse reborn wasn't installed
* Join permission message outputs required permission node now
* Added Portuguese (BR) locale (`pt_br` in config.yml)
* Locales with special characters like Russian or Korean will now work properly and won't be seen as `?` characters
* Added configurable murderer thrown sword speed
* Added 1.14 sign game states (thanks to montlikadani)

### 1.1.3 Release (01/08.06.2019)
* Fixed boss bar when disabled could throw errors breaking the game
* Fixed PlaceholderAPI placeholders didn't work on in-game scoreboard
* Fixed locales never worked if there was no locales folder
* Fixed new arena name message wasn't sent in setup editor

### 1.1.2 Release (21/25.05.2019)
* Fixed that you couldn't edit arena via /mm <arena> edit
* You will now receive additional message when adding gold/player spawns that 4 is minimum spawns amount so you can add more
* Totally recoded arena setup gui UI and code structure, now all changes applies to arena instantly
* Arena setup map name option now is chat based not name tag based
* Added shift + right click to delete all gold/player spawns (useful if you made a mistke, better than nothing)
* Added sign spawn protection radius warning, non opped players can't join via signs if sign is in radius of spawn protection
* Debug section is no longer visible part of config, it can be added manually but won't be there by default from now
* Reload command is no longer discouraged and reloads both config and language file
* Sword does fly bit faster now (from 0.5 to 0.65 fly speed)
* Murderer no longer always receive lose statistic even on win
* Fixed boss bar displays game end message instead of waiting for players after start up
* /mma help and /mm help will display help pages now
* Players shouldn't be able to interact with flower pots and paintings anymore

### 1.1.1 Release (28.04.2019 - 18.05.2019)
* Relocate Plajer's Lair Services and TigerHix's ScoreboardLib packages in order to avoid conflict between our minigames if used on same server
* Fixed saving user data on plugin disable while using MySQL
* /mma delete and reload require confirmation before execution now, theymust be typed twice to execute
* Added permissions for increased chance of being murderer or detective, node: `murdermystery.role.murderer.<1/100>` or
`murdermystery.role.detective.<1/100>` the number is the amount of bonus points to give on arena join
* Fixed update notify message was sent to everybody without proper permission check
* First time database creation is now made async not in main server thread
* Implemented faster Hikari connection pool library and removed BoneCP, jar size is now 3 times smaller too
* Apache commons-io is now shaded into jar, 1.14 removed it
* Recoded whole commands structure
* Scoreboard colors were broken sometimes, this is now fixed (I hope so)

### 1.1.0 Release (28.04.2019)
* Fixed wins/loses weren't properly assigned on game end, only murderer received these stats
* You can no longer join the game if it's in restarting state
* You can no longer swap items via hands (i.e. main hand > off hand) when being in game
* Added `win` and `lose` sections to rewards.yml when player wins or loses the game
* CorpseReborn is made now soft-dependency, it will enable corpses feature if present but won't otherwise

### 1.0.3 Release (20.04.2019)
* Fixed MySQL database didn't work due to create statement failure
* Now /mma reload command will force stop all current games before Arenas reload, still command usage is discouraged! (uservoice report)
* Leaderheads stats placeholders length has been decreased to fit in the sign (uservoice report)

### 1.0.2 Release (19.04.2019)
* Fixed Hero in game summary was always `Nobody`
* Fixed error when nobody was alive and game has ended then the exception would occur
* Now when typing /mma forcestart with only 1 player game won't start
* Added few pro tips when editing arena via /mm <arena> edit

### 1.0.1 Release (18.04.2019)
* CorpseReborn and HolographicDisplays are now set as soft dependencies in plugin.yml to allow plugin loader
to load them before MurderMystery starts and avoid plugin start failure

### 1.0.0 Release (28.10.2018-16.04.2019)
* Added Russian, Spanish and Dutch locales support
* Detective bow will now spawn bit higher to avoid touching the floor
* Spectators can no longer pickup the bow
* Added arenas.yml instances section check to avoid errors
* Added death cases caused by fall, game won't break anymore when murderer or detective dies from fall
* /murdermystery and /murdermysteryadmin commands will now work in game for non ops
* Sword can no longer go through walls/solid blocks
* Bow no longer loses durability on arrow shoot
* New users will now start with default value of contributions for each role 1 not 0 that would
throw errors in lobby stage
* Fixed action bar colors were broken
* Fixed game end announce didn't happen
* Fixed sword glitch after throwing it
* Users cannot longer pickup anything from the ground
* Spectators cannot use special blocks anymore
* CorpseReborn and HolographicDisplays are no longer hardcoded dependencies in plugin.yml so without them
plugin will start with fancy message to install them without throwing not user-friendly exception
* Fixed kills, loses and wins statistics weren't added anytime
* Misc code performance improvements
* Removed Locale suggester
> Release Candidate 2 (28.03.2019)
* Setup GUI will now display 0 minimum players option if accidentally set
* Bow Time prayer will now add new arrow to the inventory not set and override current amount
* Improvement for bow cooldown display action bar
* Fixed corpses were instantly removed on spawn, timer was set in ticks not seconds
* Fixed spectator items didn't work on death (spigot bug)
* Fixed corpse will no longer spawn on ending location when leaving game via /mm leave
if you were fake detective or a detective
* Fixed bow couldn't be picked up by innocents
* Fixed more than one corpse was spawned on death
* Fixed you couldn't receive bow when you had more than 10 gold taken
(you have 9 gold and pickup 2 then you cannot get bow cause you have 11 not 10)
* Fixed more corpses could spawn on death by arrow
* Fixed last words didn't display on death other than by arrow
* Fixed message Game-Started which was copied from Village Defense
> Release Candidate 1 (24/26.03.2019)
* Now clicking corpses in game won't open their inventories
* Improved sign manager
* Removed ' character from default join permission node
* Crucial block center calculations fix - now holograms will display properly
* Fixed prayer particle display and arena setup
* Fixed single compensation prayer from Confessional did give gold but it was
useless as user gold statistics weren't increased
* Now using special blocks if game has ended/not started (in lobby) is blocked
* Bow Time prayer (receive bow + arrows) is applied to everyone
* Bow Time prayer has been nerfed, only 1 arrow is given not whole stack now
* Now player health will be set to full when joining the game
* Bow drop announcement will no longer appear when only 1 player has left in the game
* When game ends users will get reset their non persistent user data (like gained gold amount)
> Pre 5 (24.03.2019)
* Abandoned the direct try-catch exception handling in most of methods and event listeners in favour of logger
listening, code looks better now
* Fixed setup inventory didn't work
* Using new Scoreboard library, whole game will perform now much better without lags
* Now language.yml file is cached on load so it will drastically reduce lag of plugin
> Pre 4 (11.01.2019-06.02.2019)
* Added useSSL=false argument in mysql.yml address to avoid mysql warnings in console (not harmful)
* Added %MAX_PLAYERS% placeholder in scoreboard
> Pre 3 (30.10.2018/26.12.2018)
* Now arena is stopped 10 seconds after the game ends not 5 seconds after
* Arena is now properly clean up after the game when using bungee mode
* Fixed arrows weren't given to innocents when they got 10 gold ingots
* Fixed /mma forcestart wasn't working properly if there were not enough players to start
* In case of server crash players' inventories will be loaded on join from backup from Murder Mystery minigame if exists
* When murderer picked up gold, it's amount didn't increase - now it's fixed
> Pre 2 (28.10.2018)
* Fixed wrong cast error when you were damaged by skeleton's arrow (https://plajer.xyz/errorservice/viewer.php?id=336)
* Fixed NPE when murderer was null - I must use player objects instead of these terrible UUID's
> Pre 1 (11/20.10.2018)
* Minimum amount of players cannot be now less than 2 (game will automatically set it to 2 if so)
* Block destroying item frames and paintings
* Added mystery cauldron and mystery potions (soon more)
* Added praise the developer block (end portal and levers)
* Fixed only one gold ingot could be picked up from few ones in a stack
* Fixed scores weren't added to account
* Added bow trails
* Native 1.13 support added
* Added PAPI placeholders in scoreboard
* Added murdermystery.command.override permission
* Added setup video link to the game

### 0.0.8 Beta (06/07.10.2018)
* Added throwable sword for murderer
* Added multiple last words
* Fixed gold not spawning
* Fixed starting scoreboard gamestate wasn't displayed
* Murderer sword won't be taken now when he receives additional arrows
* Now actions that gives you score (like +15 score for gold pickup will properly format without %action% placeholder)
* Added 1.13 and 1.13.1 support
* Removed 1.9 and 1.10 support
* Added spectator settings
* API recode
* JavaDocs created
* Fixed throwable sword angle
* You cannot join game now infinitely through command
* Added corpses override option in config
* Now players that will leave game will be visible by other players outside game
* Fixed some potions amplifiers were 1 level higher, amplifiers are counted from 0

### 0.0.7 Beta (03.10.2018)
* Added PAPI placeholders in chat (chat formatting must be enabled)
* General code improvements and changes
* Added %MAPNAME% placeholder in scoreboard
* Added checking for minimum players amount in lobby to start
