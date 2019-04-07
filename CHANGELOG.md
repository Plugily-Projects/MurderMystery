### 1.0.0 Release (28.10.2018-07.04.2019)
* Added Russian and Spanish locales support
* Detective bow will now spawn bit higher to avoid touching the floor
* Spectators can no longer pickup the bow
* Added arenas.yml instances section check to avoid errors
* Added death cases caused by fall, game won't break anymore when murderer or detective dies from fall
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