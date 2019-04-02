import java.util.ArrayList;
import java.util.Scanner;
// Robot Game - Procedural Version
public class Main {
    private static int energy, xPos, yPos;
    //Coordinates:  map[y][x]
    public static char[][] map;
    public static char[][] mapSeen;

    public static void loadMaze() {  //Creates maze from given text file
        String fileName = "game.txt";
        Scanner loader = new Scanner(Main.class.getResourceAsStream(fileName));
        loadStats(loader);
        loader.nextLine(); //Discard the rest of the stats' line; it's not part of the maze
        loadWorld(loader);
    }
    
    public static void loadWorld(Scanner loader) {
        ArrayList<char[]> land = new ArrayList<>();
        int mapWidth = 0;
        while (loader.hasNext()) {
            String nextLine = loader.nextLine();
            char[] nextRow = nextLine.toCharArray();
            mapWidth = Math.max(mapWidth, nextRow.length);
            land.add(nextRow);
        }
        map = land.toArray(new char[land.size()][]);
        mapSeen = new char[land.size()][mapWidth];
    }
    
    public static void loadStats(Scanner loader) {
        energy = loader.nextInt();
        yPos = loader.nextInt();
        xPos = loader.nextInt();
    }
    
    private static void doInitialDisplay() {
        System.out.println("Energy:  " + energy); //Display energy levels first
        for(int y = 0; y < map.length; y++) {
            for(int x = 0; x < map[y].length; x++) {
                if(xPos == x && yPos == y) System.out.print('o'); //Robot is here
                else System.out.print(map[y][x]); //Robot is not here, show what is
            }
            System.out.println(); //Map row has been printed, added the newline to avoid a one-line map display
        }
    }
    
    private static void playGame() {
        doInitialVision(); //Do the initial vision - populate mapSeen with what the robot can see from the start.
        startGameLoop();
    }
    
    private static void doInitialVision() {
        for(int y = yPos - 3; y <= yPos + 3; y++) {
            if(y < 0 || y >= mapSeen.length) continue; //Y-coordinate is out of bounds, ignore
            for(int x = xPos - 3; x <= xPos + 3; x++) { //+- 3 because of capturing the surrounding question marks as well
                if(x < 0 || x >= mapSeen[y].length) continue; //X-coordinate is out of bounds, ignore
                if(Math.max(Math.abs(y - yPos), Math.abs(x - xPos)) > 2) {
                    mapSeen[y][x] = '?'; //Space is 3 away in any direction- on the ? border.
                }
                else if(x >= map[y].length) {
                    mapSeen[y][x] = ' '; //mapSeen is a square 2D array, map may not be.  Allow for that.
                }
                else mapSeen[y][x] = map[y][x];
            }
        }
    }
    
    private static void startGameLoop() {
        Scanner in = new Scanner(System.in); //Command input.  Using nextLine() for commands.
        while(true) { //Infinite loop - will be terminated by System.exit() calls.
            showWorld(); //Display the visible world
            char command = getCommand(in);
            processCommand(command);
        }
    }
    
    private static void showWorld() {
        int[] minCoords = findBoundaryCoordinates(); //Find the visibility edge coordinates- so there is not excessive space surrounding the map display.
        //minCoords will be a 4-entry array - the 3rd and 4th being maximum coordinates, so we know when to quit.
        //Coordinates in minCoords will be minX, minY, maxX, maxY.
        System.out.println("Energy:  " + energy); //Start with energy display
        for(int y = minCoords[1]; y <= minCoords[3]; y++) {
            for(int x = minCoords[0]; x <= minCoords[2]; x++) {
                if(xPos == x && yPos == y) System.out.print('o'); //Robot is here
                else if(mapSeen[y][x] == 0) System.out.print(' '); //Robot hasn't seen this.  Character value 0 is a nonprinting character; space is desired.
                else System.out.print(mapSeen[y][x]);
            }
            System.out.println(); //Map row has been printed, add newline.
        }
    }
    
    private static int[] findBoundaryCoordinates() {
        int minX = mapSeen[0].length, minY = mapSeen.length, maxX = 0, maxY = 0; //Set mins at max and maxes at min; this ensures they are accurately read.
        for(int y = 0; y < mapSeen.length; y++) {
            for(int x = 0; x < mapSeen[0].length; x++) {
                if(mapSeen[y][x] == 0) continue;
                if(y < minY) minY = y;
                if(y > maxY) maxY = y;
                if(x < minX) minX = x;
                if(x > maxX) maxX = x;
            }
        }
        return new int[]{minX, minY, maxX, maxY};
    }
    
    private static char getCommand(Scanner in) {    
        String cmd = in.nextLine().trim(); //Remove any leading/trailing whitespace
        if(cmd.isEmpty()) System.exit(0); //User entered a visibly empty line.  Close the program.
        if(cmd.length() != 1) return 0; //All commands are 1 character long.  Invalid length detected, return an invalid command - will be ignored.
        return cmd.charAt(0); //Invalid commands will be ignored later.
    }
    
    private static void processCommand(char command) {
        //Switch statement would be better but functions are restricted to 12 lines, making switch not viable
        if(command == 'u') {
            processMove(0, -1); //Movement direction- coords x, y.  If invalid, no move will happen.
        }else if(command == 'd') {
            processMove(0, 1);
        }else if(command == 'l') {
            processMove(-1, 0);
        }else if(command == 'r') {
            processMove(1, 0);
        } //No ELSE clause; we ignore any invalid command.  Game loop will run again, next command will be requested.
    }
    
    private static void processMove(int x, int y) {
        x += xPos;
        y += yPos; //Adjust coords to target move position, rather than direction
        if(isBlocked(x, y)) return; //Trying to move into a wall or off the map (if the maze is broken) is a no-go.  Ignore, ask for next command.
        moveTo(x, y); //Execute the move
    }
    
    private static boolean isBlocked(int x, int y) {
        if(x < 0 || x >= mapSeen[0].length || y < 0 || y >= mapSeen.length) return true; //Off the map grid (if the maze is broken).  Can't go there, it's blocked.
        if(x > map[y].length) return true; //Off the map (if the maze is broken).  Can't go there.
        if(mapSeen[y][x] == '#' || mapSeen[y][x] == '%') return true; //Boundary and Obstacle are not pathable.  Can't go there.
        return false; //We've covered all the obstructions.  We can go anywhere else.
        //Last two lines could be replaced with a simple return statement on the final IF condition; however, it's more readable this way.
    }
    
    private static void moveTo(int x, int y) {
        xPos = x; //Move the robot
        yPos = y;
        energy--; //Deduct energy cost of moving the robot
        processLanding(mapSeen[y][x]); //What did we land on?  Process it.  May involve win/lose condition, for goal/trap.
        //processLanding() will also, unless we're on a direction, place the '.' marker for our path.  It'll be hidden by the robot symbol until we move.
        updateVision();
        if(energy < 1) lose();
    }
    
    private static void processLanding(char c) {
        if(c == 'u' || c == 'l' || c == 'r' || c == 'd') return; //Landed on a direction.  No special actions, no dot laying.  No nothing.
        if(c == '+') energy += 10; //Landed on a battery, add energy.
        if(c == '-') energy -= 10; //Landed on a trap, deduct energy.
        if(c == '*') win(); //Landed on the goal, win.
        if(c == '#' || c == '%') throw new IllegalStateException("Landed on a boundary/obstacle- move shouldn't have been possible."); //Just to be safe
        mapSeen[yPos][xPos] = '.'; //Lay the '.' marker.  This will overwrite batteries, traps, and invalid map characters.
        //Win will have already System.exit()ed us on a goal, directions will have already returned us.  Neither of them will be overwritten.
        //We might overwrite another marker; this is ignored, allowing (inefficient) backtracing.
    }
    
    private static void updateVision() {
        for(int y = yPos - 3; y <= yPos + 3; y++) {
            if(y < 0 || y >= mapSeen.length) continue; //Y-coordinate is out of bounds, ignore
            for(int x = xPos - 3; x <= xPos + 3; x++) { //+- 3 because it's capturing the surrounding question marks as well
                if(x < 0 || x >= mapSeen[y].length) continue; //X-coordinate is out of bounds, ignore
                if(mapSeen[y][x] != '?' && mapSeen[y][x] != 0) continue; //Cell has already been examined, don't overwrite game state data.
                //If cell is a 0, it will now be a ?.  If cell is a ?, it MAY be turned into a map datapoint.
                if(Math.max(Math.abs(y - yPos), Math.abs(x - xPos)) > 2) mapSeen[y][x] = '?'; //Space is 3 away in any direction- on the ? border.
                else if(x >= map[y].length) mapSeen[y][x] = ' '; //mapSeen is a square 2D array, map may not be.  Allow for that.
                else mapSeen[y][x] = map[y][x];
            }
        }
    }
    
    private static void lose() {
        finish("Sorry, you lost.  Game over!");
    }
    
    private static void win() {
        finish("Congratulations!  You won the game!");
    }
    
    private static void finish(String message) {
        showWorld(); //Display the final world state, as the robot knows it
        System.out.println(message); //Display the completion message (win/loss)
        System.exit(0); //Close the program; the game is over.
    }

    public static void main(String[] args) {
        loadMaze();
        doInitialDisplay();
        playGame();
    }
}
