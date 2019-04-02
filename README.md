# Goal-Seeking-Robot

Quirky game where you are in control of the robot and lead it in search of the goal.

Specifications of the game:
There is an artificial environment that a robot can walk freely on. The user controls the robot's movement.
The environment will be loaded automatically from a given .txt file. The environment is a 2D character set.
Your program should read a 2D array of characters. Your program should store the entire map inside an array.
To move the robot, you give the commands like u for up and d for down and... If the location is blocked you can
produce a new command. After each move, the energy of the robot reduces by one. If you receive to the goal point
within the number of allowed steps (i.e. The total energy), you win. If you run out of steps, you lose. If you enter 
a blank line the program exits. Your program should store the states of the robot on private static member variables.
Create procedural and an object-oriented versions of the game.
