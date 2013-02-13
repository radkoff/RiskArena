 ______    ___   _______  ___   _    _______  ______    _______  __    _  _______ 
|    _ |  |   | |       ||   | | |  |   _   ||    _ |  |       ||  |  | ||   _   |
|   | ||  |   | |  _____||   |_| |  |  |_|  ||   | ||  |    ___||   |_| ||  |_|  |
|   |_||_ |   | | |_____ |      _|  |       ||   |_||_ |   |___ |       ||       |
|    __  ||   | |_____  ||     |_   |   _   ||    __  ||    ___||  _    ||   _   |
|   |  | ||   |  _____| ||    _  |  |  | |  ||   |  | ||   |___ | | |   ||  | |  |
|___|  |_||___| |_______||___| |_|  |__| |__||___|  |_||_______||_|  |__||__| |__|
Evan Radkoff (eradkoff12@gmail.com)
==================================================================================

Risk Arena is game engine for the board game Risk, intended to simulate war games between AI players (called "RiskBots") whose strategies are determined by programmers using a Java decision-making API.

--------------- How to run RiskArena ---------------
The program should run as-is. The only software dependence is the dom4j library (http://dom4j.sourceforge.net), so if you are using an IDE like Eclipse be sure to point it towards the dom4j-1.6.1.jar resource located in /lib. Also note I have not tested RiskArena with anything other than Java SE 6.
The first panel can be used to configure map and player information. If there is at least one human player, RiskArena will play through a single game. This mode is best for seeing/testing the behavior of RiskBots first hand. If all players are bots, however, RiskArena goes in "war games" mode and brings you to another configuration panel. Here you can choose how many games are to be simulated, whether you would like to watch individual games, and more. During war games you can see a panel showing the current standings.

--------------- RiskBots ---------------
As was previously mentioned, the RiskBot API allows you the programmer to implement your own game-playing strategy. While using the API to interact with the game is very simple, your strategy can be as complicated as you'd like it to be. If you need refreshing on the rules of Risk before making your own RiskBot, see the included Risk Manual pdf.
For the complete details on how to make a RiskBot, see HOWTO.txt.

--------------- Maps ---------------
RiskArena is not constricted to the standard world map. The maps currently included are Earth and North America. These files are stored as simple XML, and if you'd like to create your own consult src/maps/HOWTO.txt for more details.

--------------- Logs ---------------
If a game has any human players, or if it is configured to do so, complete HTML logs are saved in logs/game_reports/. In addition, all war games have result summaries saved in logs/war_games/.

--------------- Contributions ---------------
RiskArena is obviously open source and can be found at http://github.com/radkoff/RiskArena. Any and all contributions to improving this are welcomed! For a list of things I'd like to see happen see TODO.txt, or if you feel like bug hunting see BUGS.txt.
