### 1.0.0 Pre release 2 (28.10.2018)
* Fixed wrong cast error when you were damaged by skeleton's arrow (https://plajer.xyz/errorservice/viewer.php?id=336)
* Fixed NPE when murderer was null - I must use player objects instead of these terrible UUID's

### 1.0.0 Pre release (11/20.10.2018)
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