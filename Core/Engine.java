package tileworld.Core;

import tileworld.TileEngine.TERenderer;
import tileworld.TileEngine.TETile;
import tileworld.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Engine {

    private static final int WIDTH = 80;
    private static final int HEIGHT = 40;

    private Long seed;
    private long inpSeed;
    private Random RANDOM;
    private TETile[][] world;
    private TERenderer ter;
    private HashMap<Point, Character> entries;
    private ArrayDeque<Point> fringe;
    private Character[] sideArray;

    private Boolean isPlayerMove;
    private Point playerPosition;
    private ArrayList<Point> doors;
    private TETile[] TILES = {Tileset.NOTHING, Tileset.WALL, Tileset.FLOOR,
            Tileset.AVATAR, Tileset.LOCKED_DOOR};

    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private static final Font ARIAL_BLACK = new Font("Arial Black", Font.PLAIN, 16);
    private static final Font AVENIR_NEXT = new Font("Avenir Next Condensed", Font.PLAIN, 16);
    private static final Font AVENIR_NEXT_SMOL = new Font("Avenir Next Condensed", Font.PLAIN, 12);
    private static final Font BODONI = new Font("Bodoni 72", Font.PLAIN, 16);
    private static final Font BODONI_BIG = new Font("Bodoni 72", Font.PLAIN, 40);
    private static final Font BODONI_SMOL = new Font("Bodoni 72", Font.PLAIN, 15);

    private static final Color GRAY_A05 = new Color(200, 200, 200, 240);
    private static final Color GRAY_O10 = new Color(150, 150, 150);
    private static final Color GRAY_O20 = new Color(80, 80, 80);
    private static final Color GRAY_O005 = new Color(230, 230, 230);

    private boolean gameOver = false;

    public Engine() {
    }

    private void relaxHallway(Point p, Character c) {
        Integer[] arr = new Integer[]{0, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4};
        if (c.equals('u')) {
            if (checkPointExpansion(p.x, p.y, true, true)) {
                hallway(p, RandomUtils.uniform(RANDOM, 3, 20), RandomUtils.uniform(RANDOM, 3, 20),
                        arr[RandomUtils.uniform(RANDOM, 0, 11)], true, c);
            } else {
                world[p.x][p.y - 1] = Tileset.WALL;
            }
        } else if (c.equals('d')) {
            if (checkPointExpansion(p.x, p.y, true, false)) {
                hallway(p, RandomUtils.uniform(RANDOM, 3, 20), RandomUtils.uniform(RANDOM, 3, 20),
                        arr[RandomUtils.uniform(RANDOM, 0, 11)], true, c);
            } else {
                world[p.x][p.y + 1] = Tileset.WALL;
            }
        } else if (c.equals('l')) {
            if (checkPointExpansion(p.x, p.y, false, false)) {
                hallway(p, RandomUtils.uniform(RANDOM, 3, 20), RandomUtils.uniform(RANDOM, 3, 20),
                        arr[RandomUtils.uniform(RANDOM, 0, 11)], false, c);
            } else {
                world[p.x + 1][p.y] = Tileset.WALL;
            }
        } else if (c.equals('r')) {
            if (checkPointExpansion(p.x, p.y, false, true)) {
                hallway(p, RandomUtils.uniform(RANDOM, 3, 20), RandomUtils.uniform(RANDOM, 3, 20),
                        arr[RandomUtils.uniform(RANDOM, 0, 11)], false, c);
            } else {
                world[p.x - 1][p.y] = Tileset.WALL;
            }
        }
    }

    private Integer[] hallwayHelperVertical(Point start, int length) {
        int x = start.x; int y = start.y;
        int randomHeight;
        if (length < 3) {
            randomHeight = 2;
        } else {
            randomHeight = RandomUtils.uniform(RANDOM, 2, length);
        }
        int maxHeight = y;
        while (checkVertical(x, maxHeight) && maxHeight - y < randomHeight) {
            maxHeight++;
        }
        int minHeight = y;
        while (checkVertical(x, minHeight) && maxHeight - minHeight < length) {
            minHeight--;
        }
        int realLength = maxHeight - minHeight;
        if (realLength < 3) {
            for (int i = 0; i <= realLength; i++) {
                insertWall(x - 1, maxHeight - i);
                insertFloor(x, maxHeight - i);
                insertWall(x + 1, maxHeight - i);
            }
        } else {
            for (int i = 0; i < realLength; i++) {
                insertWall(x - 1, maxHeight - i);
                insertFloor(x, maxHeight - i);
                insertWall(x + 1, maxHeight - i);
            }
        }
        return new Integer[]{minHeight, maxHeight, realLength};
    }

    private Integer[] hallwayHelperHorizontal(Point start, int width) {
        int x = start.x; int y = start.y;
        int randomHeight;
        if (width < 3) {
            randomHeight = 2;
        } else {
            randomHeight = RandomUtils.uniform(RANDOM, 2, width);
        }
        int maxHeight = x;
        while (checkHorizontal(maxHeight, y) && maxHeight - x < randomHeight) {
            maxHeight++;
        }
        int minHeight = x;
        while (checkHorizontal(minHeight, y) && maxHeight - minHeight < width) {
            minHeight--;
        }
        int realWidth = maxHeight - minHeight;
        for (int i = 0; i < realWidth; i++) {
            insertWall(maxHeight - i, y - 1);
            insertFloor(maxHeight - i, y);
            insertWall(maxHeight - i, y + 1);
        }
        return new Integer[]{minHeight, maxHeight, realWidth};
    }

    private void verticalHallway(Point start, int length, int numSides, Character c) {
        int x = start.x;
        Integer[] arr = hallwayHelperVertical(start, length);
        int minHeight = arr[0]; int maxHeight = arr[1]; int realLength = arr[2];
        if (numSides < 2) {
            if (numSides == 0) {
                if (c.equals('u')) {
                    world[x][maxHeight] = Tileset.WALL;
                    world[x][minHeight + 1] = Tileset.FLOOR;
                } else {
                    world[x][maxHeight] = Tileset.FLOOR;
                    world[x][minHeight + 1] = Tileset.WALL;
                }
            } else if (c.equals('u')) {
                expandPoint(x, maxHeight, true, true);
                world[x][minHeight + 1] = Tileset.FLOOR;
            } else {
                expandPoint(x, minHeight, true, false);
                world[x][maxHeight] = Tileset.FLOOR;
            }
        } else {
            if (entries.isEmpty()) {
                expandPoint(x, minHeight, true, false);
                expandPoint(x, maxHeight, true, true);
            } else if (c.equals('u')) {
                expandPoint(x, maxHeight, true, true);
                world[x][minHeight + 1] = Tileset.FLOOR;
            } else {
                if (checkForNothing(x, minHeight)) {
                    expandPoint(x, minHeight, true, false);
                    world[x][maxHeight] = Tileset.FLOOR;
                } else {
                    world[x][minHeight] = Tileset.WALL;
                    world[x][maxHeight] = Tileset.FLOOR;
                }
            }
            numSides -= 2;
            if (numSides == 2) {
                int randomYL;
                int randomYR;
                if (realLength < 4) {
                    randomYL = 1;
                    randomYR = 1;
                } else {
                    randomYL = RandomUtils.uniform(RANDOM, 1, realLength - 2);
                    randomYR = RandomUtils.uniform(RANDOM, 1, realLength - 2);
                }
                if (checkPointExpansion(x - 2, minHeight + randomYL + 1, false, false)) {
                    world[x - 1][minHeight + randomYL + 1] = Tileset.FLOOR;
                    entries.put(new Point(x - 2, minHeight + randomYL + 1), sideArray[2]);
                    fringe.addLast(new Point(x - 2, minHeight + randomYL + 1));
                }
                if (checkPointExpansion(x + 2, minHeight + randomYR + 1, false, true)) {
                    world[x + 1][minHeight + randomYR + 1] = Tileset.FLOOR;
                    entries.put(new Point(x + 2, minHeight + randomYR + 1), sideArray[3]);
                    fringe.addLast(new Point(x + 2, minHeight + randomYR + 1));
                }
            } else if (numSides == 1) {
                int randomY;
                if (realLength < 4) {
                    randomY = 1;
                } else {
                    randomY = RandomUtils.uniform(RANDOM, 1, realLength - 2);
                }
                if (RANDOM.nextInt() % 2 == 0) {
                    if (checkPointExpansion(x - 2, minHeight + randomY + 1, false, false)) {
                        world[x - 1][minHeight + randomY + 1] = Tileset.FLOOR;
                        entries.put(new Point(x - 2, minHeight + randomY + 1), sideArray[2]);
                        fringe.addLast(new Point(x - 2, minHeight + randomY + 1));
                    }
                } else {
                    if (checkPointExpansion(x + 2, minHeight + randomY + 1, false, true)) {
                        world[x + 1][minHeight + randomY + 1] = Tileset.FLOOR;
                        entries.put(new Point(x + 2, minHeight + randomY + 1), sideArray[3]);
                        fringe.addLast(new Point(x + 2, minHeight + randomY + 1));
                    }
                }
            }
        }
    }

    private void horizontalHallway(Point start, int width, int numSides, Character c) {
        int y = start.y;
        Integer[] arr = hallwayHelperHorizontal(start, width);
        int minHeight = arr[0]; int maxHeight = arr[1]; int realWidth = arr[2];
        if (numSides < 2) {
            if (numSides == 0) {
                if (c.equals('l')) {
                    world[maxHeight][y] = Tileset.FLOOR;
                    world[minHeight + 1][y] = Tileset.WALL;
                } else {
                    world[maxHeight][y] = Tileset.WALL;
                    world[minHeight + 1][y] = Tileset.FLOOR;
                }
            } else if (c.equals('l')) {
                expandPoint(minHeight, y, false, false);
                world[maxHeight][y] = Tileset.FLOOR;
            } else {
                expandPoint(maxHeight, y, false, true);
                world[minHeight + 1][y] = Tileset.FLOOR;
            }
        } else {
            if (entries.isEmpty()) {
                expandPoint(minHeight, y, false, false);
                expandPoint(maxHeight, y, false, true);
            } else  if (c.equals('l')) {
                if (checkForNothing(minHeight, y)) {
                    expandPoint(minHeight, y, false, false);
                    world[maxHeight][y] = Tileset.FLOOR;
                } else {
                    world[minHeight][y] = Tileset.WALL;
                    world[maxHeight][y] = Tileset.FLOOR;
                }
            } else {
                expandPoint(maxHeight, y, false, true);
                world[minHeight + 1][y] = Tileset.FLOOR;
            }
            numSides -= 2;
            if (numSides == 2) {
                int randomXL;
                int randomXR;
                if (realWidth < 4) {
                    randomXL = 1;
                    randomXR = 1;
                } else {
                    randomXL = RandomUtils.uniform(RANDOM, 1, realWidth - 2);
                    randomXR = RandomUtils.uniform(RANDOM, 1, realWidth - 2);
                }
                if (checkPointExpansion(minHeight + randomXL + 1, y - 2, true, false)) {
                    world[minHeight + randomXL + 1][y - 1] = Tileset.FLOOR;
                    entries.put(new Point(minHeight + randomXL + 1, y - 2), sideArray[1]);
                    fringe.addLast(new Point(minHeight + randomXL + 1, y - 2));
                }
                if (checkPointExpansion(minHeight + randomXR + 1, y + 2, true, true)) {
                    world[minHeight + randomXR + 1][y + 1] = Tileset.FLOOR;
                    entries.put(new Point(minHeight + randomXR + 1, y + 2), sideArray[0]);
                    fringe.addLast(new Point(minHeight + randomXR + 1, y + 2));
                }
            } else if (numSides == 1) {
                int randomX;
                if (realWidth < 4) {
                    randomX = 1;
                } else {
                    randomX = RandomUtils.uniform(RANDOM, 1, realWidth - 2);
                }
                if (RANDOM.nextInt() % 2 == 0) {
                    if (checkPointExpansion(minHeight + randomX + 1, y - 1, true, false)) {
                        world[minHeight + randomX + 1][y - 1] = Tileset.FLOOR;
                        entries.put(new Point(minHeight + randomX + 1, y - 2), sideArray[1]);
                        fringe.addLast(new Point(minHeight + randomX + 1, y - 2));
                    }
                } else {
                    if (checkPointExpansion(minHeight + randomX + 1, y + 1, true, true)) {
                        world[minHeight + randomX + 1][y + 1] = Tileset.FLOOR;
                        entries.put(new Point(minHeight + randomX + 1, y + 2), sideArray[0]);
                        fringe.addLast(new Point(minHeight + randomX + 1, y + 2));
                    }
                }
            }
        }
    }

    private void hallway(Point start, int length,
                         int width, int numSides, boolean orientation, Character c) {
        if (orientation) {
            verticalHallway(start, length, numSides, c);
        } else {
            horizontalHallway(start, width, numSides, c);
        }
    }

    private boolean checkVertical(int x, int y) {
        return y > 0 && y < HEIGHT - 1 && world[x - 1][y].equals(Tileset.NOTHING)
                && world[x][y].equals(Tileset.NOTHING) && world[x + 1][y].equals(Tileset.NOTHING);
    }

    private boolean checkHorizontal(int x, int y) {
        return x > 0 && x < WIDTH - 1 && world[x][y - 1].equals(Tileset.NOTHING)
                && world[x][y].equals(Tileset.NOTHING) && world[x][y + 1].equals(Tileset.NOTHING);
    }

    private boolean checkPointExpansion(int x, int y, boolean orientation, boolean direction) {
        if (orientation) {
            if (direction) {
                return checkForNothing(x, y) && checkForNothing(x, y + 1)
                        && checkForNothing(x - 1, y) && checkForNothing(x - 1, y + 1)
                        && checkForNothing(x + 1, y) && checkForNothing(x + 1, y + 1);
            } else {
                return checkForNothing(x, y) && checkForNothing(x, y - 1)
                        && checkForNothing(x - 1, y) && checkForNothing(x - 1, y - 1)
                        && checkForNothing(x + 1, y) && checkForNothing(x + 1, y - 1);
            }
        } else {
            if (direction) {
                return checkForNothing(x, y) && checkForNothing(x + 1, y)
                        && checkForNothing(x, y + 1) && checkForNothing(x + 1, y + 1)
                        && checkForNothing(x, y - 1) && checkForNothing(x + 1, y - 1);
            } else {
                return checkForNothing(x, y) && checkForNothing(x - 1, y)
                        && checkForNothing(x, y - 1) && checkForNothing(x - 1, y - 1)
                        && checkForNothing(x, y + 1) && checkForNothing(x - 1, y + 1);
            }
        }
    }

    private boolean checkForNothing(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && world[x][y].equals(Tileset.NOTHING);
    }

    private boolean checkforWall(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && world[x][y].equals(Tileset.WALL);
    }

    private boolean checkForFloor(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && world[x][y].equals(Tileset.FLOOR);
    }

    private boolean checkforAvatar(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && world[x][y].equals(Tileset.AVATAR);
    }

    private boolean checkforLockedDoor(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0
                && y < HEIGHT && world[x][y].equals(Tileset.LOCKED_DOOR);
    }

    private void expandPoint(int x, int y, boolean orientation, boolean direction) {
        if (orientation) {
            if (direction) {
                if (checkPointExpansion(x, y + 1, true, true)) {
                    entries.put(new Point(x, y + 1), sideArray[0]);
                    fringe.addLast(new Point(x, y + 1));
                } else {
                    world[x][y] = Tileset.WALL;
                }
            } else {
                if (checkPointExpansion(x, y, true, false)) {
                    entries.put(new Point(x, y), sideArray[1]);
                    fringe.addLast(new Point(x, y));
                } else {
                    if (y >= 39) {
                        world[x][y] = Tileset.WALL;
                    } else {
                        world[x][y + 1] = Tileset.WALL;
                    }
                }
            }
        } else {
            if (direction) {
                if (checkPointExpansion(x + 1, y, false, true)) {
                    entries.put(new Point(x + 1, y), sideArray[3]);
                    fringe.addLast(new Point(x + 1, y));
                } else {
                    world[x][y] = Tileset.WALL;
                }
            } else {
                if (checkPointExpansion(x, y, false, false)) {
                    entries.put(new Point(x, y), sideArray[2]);
                    fringe.addLast(new Point(x, y));
                } else {
                    world[x + 1][y] = Tileset.WALL;
                }
            }
        }
    }

    private void insertWall(int x, int y) {
        world[x][y] = Tileset.WALL;
    }

    private void insertFloor(int x, int y) {
        world[x][y] = Tileset.FLOOR;
    }

    public class Room {

        private int height;
        private int width;
        private HashMap<Point, Character> entries = new HashMap<>();
        private Random RANDOM;
        private int numEntries;
        private final String[] cornerNames = {"ul", "ur", "ll", "lr"};
        Point start;
        private HashMap<String, Point> cornerMap = new HashMap<>(4);
        TETile[][] world;
        char side;

        private boolean haveSpace;

        Room(TETile[][] tiles, Point p, int h, int w, int n, char c, Random r) {
            height = h;
            width = w;
            RANDOM = r;
            start = p;
            numEntries = n;
            world = tiles;
            haveSpace = true;
            side = c;

            if (side == 'f') {
                int ll = Math.max(p.y - height + 1, 0);
                cornerMap.put("ul", p);
                cornerMap.put("ur", new Point(p.x + w - 1, p.y));
                cornerMap.put("ll", new Point(p.x, ll));
                cornerMap.put("lr", new Point(p.x + w - 1, ll));
                makeWalls();
                makeFloors();
                addOpenings();
            } else {
                checkSpace();

                if (haveSpace) {
                    entries = new HashMap<>(numEntries);
                    Point[] corners = findCorners();
                    for (int i = 0; i < 4; i++) {
                        cornerMap.put(cornerNames[i], corners[i]);
                    }
                    if (validateCorners()) {
                        width = corners[1].x - corners[0].x;
                        height = corners[0].y - corners[2].y;
                        makeWalls();
                        makeFloors();
                        addOpenings();
                    } else {
                        closeOpening(start);
                        haveSpace = false;
                    }
                } else {
                    closeOpening(start);
                }
            }


        }

        private void checkSpace() {

            if (start.x <= 3 || start.x >= world.length - 2
                    || start.y <= 3 || start.y >= world[0].length - 2) {
                haveSpace = false;
                return;
            }
            switch (side) {
                case 'u' :
                    if (!isNothing(start.x - 1, start.y + 1)
                            || !isNothing(start.x + 1, start.y + 1)) {
                        haveSpace = false;
                        return;
                    } else if (!isNothing(start.x, start.y + 1)
                            || !isNothing(start.x - 1, start.y)
                            || !isNothing(start.x + 1, start.y)) {
                        haveSpace = false;
                        return;
                    }
                    break;

                case 'd' :
                    if (!isNothing(start.x - 1, start.y) || !isNothing(start.x + 1, start.y)
                            || !isNothing(start.x, start.y - 1)) {
                        haveSpace = false;
                        return;
                    } else if (!isNothing(start.x - 1, start.y - 1)
                            || !isNothing(start.x + 1, start.y - 1)) {
                        haveSpace = false;
                        return;
                    }
                    break;

                case 'r' :
                    if (!isNothing(start.x + 1, start.y) || !isNothing(start.x, start.y + 1)
                            || !isNothing(start.x, start.y - 1)) {
                        haveSpace = false;
                        return;
                    } else if (!isNothing(start.x + 1, start.y + 1)
                            || !isNothing(start.x + 1, start.y - 1)) {
                        haveSpace = false;
                        return;
                    }
                    break;

                default :
                    if (!isNothing(start.x - 1, start.y) || !isNothing(start.x, start.y + 1)
                            || isWall(start.x, start.y - 1)) {
                        haveSpace = false;
                        return;
                    } else if (!isNothing(start.x - 1, start.y + 1)
                            || !isNothing(start.x - 1, start.y - 1)) {
                        haveSpace = false;
                        return;
                    }

            }

        }

        private Point[] addCorners(Point[] crnrs, int rX, int lX, int uY, int dY) {
            crnrs[0] = new Point(lX, uY);
            crnrs[1] = new Point(rX, uY);
            crnrs[2] = new Point(lX, dY);
            crnrs[3] = new Point(rX, dY);
            return crnrs;
        }
        private Point[] findCorners() {
            Point[] corner = new Point[4];
            switch (side) {
                case 'u': {
                    int leftXUpperBound = start.x - 1;
                    int leftXLowerBound = Math.max(0, leftXUpperBound - width + 2);
                    int xGoal = RandomUtils.uniform(RANDOM, leftXLowerBound, leftXUpperBound + 1);
                    int x = leftXUpperBound;
                    while (x > xGoal && validatePoint(x - 1, start.y)
                            && isNothing(x - 1, start.y)) {
                        x--; }
                    int rightEdge = Math.min(world.length - 1, x + width - 1);
                    int topEdge = Math.min(world[0].length - 1, start.y + height - 1);
                    while (validatePoint(rightEdge, start.y) && !isNothing(rightEdge, start.y)) {
                        rightEdge--; }
                    while (validatePoint(x, topEdge) && validatePoint(rightEdge, topEdge)
                            && ((!isNothing(x, topEdge) || !isNothing(rightEdge, topEdge)))) {
                        topEdge--; }
                    corner = addCorners(corner, rightEdge, x, topEdge, start.y);
                    break; }
                case 'd' : {
                    int upperXBound = start.x - 1;
                    int lowerXBound = Math.max(0, upperXBound - width + 2);
                    int xGoal = RandomUtils.uniform(RANDOM, lowerXBound, upperXBound + 1);
                    int x = upperXBound;
                    while (x > xGoal && validatePoint(x - 1, start.y)
                            && isNothing(x - 1, start.y)) {
                        x--; }
                    int rightEdge = Math.min(x + width - 1, world.length - 1);
                    int bottomEdge = Math.max(0, start.y - height + 1);
                    while (validatePoint(rightEdge, start.y) && !isNothing(rightEdge, start.y)) {
                        rightEdge--; }
                    while (validatePoint(x, bottomEdge) && validatePoint(rightEdge, bottomEdge)
                            && (!isNothing(x, bottomEdge) || !isNothing(rightEdge, bottomEdge))) {
                        bottomEdge++; }
                    corner = addCorners(corner, rightEdge, x, start.y, bottomEdge);
                    break; }
                case 'r' : {
                    int lowerYBound = start.y + 1;
                    int upperYBound = Math.min(world[0].length - 1, lowerYBound + height - 2);
                    int yGoal = RandomUtils.uniform(RANDOM, lowerYBound, upperYBound + 1);
                    int y = lowerYBound;
                    while (y < yGoal && validatePoint(start.x, y + 1)
                            && isNothing(start.x, y + 1)) {
                        y++; }
                    int rightEdge = Math.min(world.length - 1, start.x + width - 1);
                    int bottomEdge = Math.max(0, y - height + 1);
                    while (validatePoint(rightEdge, y) && !isNothing(rightEdge, y)) {
                        rightEdge--; }
                    while (validatePoint(start.x, bottomEdge)
                            && validatePoint(rightEdge, bottomEdge)
                            && (!isNothing(start.x, bottomEdge)
                            || !isNothing(rightEdge, bottomEdge))) {
                        bottomEdge++; }
                    corner = addCorners(corner, rightEdge, start.x, y, bottomEdge);
                    break; }
                default : {
                    int lowerYBound = start.y + 1;
                    int upperYBound = Math.min(world[0].length - 1, lowerYBound + height - 2);
                    int yGoal = RandomUtils.uniform(RANDOM, lowerYBound, upperYBound + 1);
                    int y = lowerYBound;
                    while (y < yGoal && validatePoint(start.x, y + 1)
                            && !isNothing(start.x, y + 1)) {
                        y++; }
                    int leftEdge = Math.max(0, start.x - width + 1);
                    int bottomEdge = Math.max(0, y - height + 1);
                    while (validatePoint(leftEdge, y) && !isNothing(leftEdge, y)) {
                        leftEdge++; }
                    while (validatePoint(start.x, bottomEdge)
                            && validatePoint(leftEdge, bottomEdge)
                            && (!isNothing(start.x, bottomEdge)
                            || !isNothing(leftEdge, bottomEdge))) {
                        bottomEdge++; }
                    corner = addCorners(corner, start.x, leftEdge, y, bottomEdge);
                    break; }
            }
            return corner;
        }

        public boolean hadSpace() {
            return haveSpace;
        }

        private boolean isWall(int x, int y) {
            return world[x][y].equals(Tileset.WALL);
        }

        private boolean isFloor(int x, int y) {
            return world[x][y].equals(Tileset.FLOOR);
        }

        private boolean isNothing(int x, int y) {
            return world[x][y].equals(Tileset.NOTHING);
        }

        private void makeWalls() {
            Point ul = cornerMap.get("ul");
            Point ll = cornerMap.get("ll");
            Point lr = cornerMap.get("lr");
            Point ur = cornerMap.get("ur");
            int w = ur.x - ul.x;
            width = w;
            int h = ul.y - ll.y;
            height = h;

            for (int i = 0; i <= w; i++) {
                world[ul.x + i][ul.y] = Tileset.WALL;
                world[ul.x + i][ll.y] = Tileset.WALL;
            }

            for (int i = 0; i <= h; i++) {
                world[ul.x][ll.y + i] = Tileset.WALL;
                world[ur.x][lr.y + i] = Tileset.WALL;
            }

            if (side != 'f') {
                world[start.x][start.y] = Tileset.FLOOR;
            }
        }

        private void makeFloors() {
            Point ul = cornerMap.get("ul");
            Point ll = cornerMap.get("ll");
            Point lr = cornerMap.get("lr");
            Point ur = cornerMap.get("ur");
            int w = ur.x - ul.x - 2;
            int h = ul.y - ll.y - 2;

            for (int i = 0; i <= w; i++) {
                for (int j = 0; j <= h; j++) {
                    world[ll.x + 1 + i][ll.y + 1 + j] = Tileset.FLOOR;
                }
            }

        }

        private void addOpenings() {
            char[] sides = new char[3];

            switch (side) {
                case 'u' :
                    sides[0] = 'u';
                    sides[1] = 'l';
                    sides[2] = 'r';
                    break;
                case 'd' :
                    sides[0] = 'd';
                    sides[1] = 'l';
                    sides[2] = 'r';
                    break;
                case 'l' :
                    sides[0] = 'u';
                    sides[1] = 'd';
                    sides[2] = 'l';
                    break;
                default :
                    sides[0] = 'u';
                    sides[1] = 'd';
                    sides[2] = 'r';
            }

            addOpening(sides[0]);
            addOpening(sides[1]);
            addOpening(sides[2]);
        }

        private void addOpening(char s) {
            int leftX = cornerMap.get("ul").x;
            int rightX = cornerMap.get("lr").x;
            int upY = cornerMap.get("ul").y;
            int downY = cornerMap.get("lr").y;
            switch (s) {
                case 'l' : {
                    int minY = downY + 1;
                    int maxY = upY - 1;
                    int choice = RandomUtils.uniform(RANDOM, minY, maxY + 1);
                    if (validatePoint(leftX - 1, choice) && isNothing(leftX - 1, choice)) {
                        world[cornerMap.get("ul").x][choice] = Tileset.FLOOR;
                        entries.put(new Point(cornerMap.get("ul").x - 1, choice), 'l');
                    }
                    break;
                }
                case 'r' : {
                    int minY = downY + 1;
                    int maxY = upY - 1;
                    int choice = RandomUtils.uniform(RANDOM, minY, maxY + 1);
                    if (validatePoint(rightX + 1, choice) && isNothing(rightX + 1, choice)) {
                        world[cornerMap.get("ur").x][choice] = Tileset.FLOOR;
                        entries.put(new Point(cornerMap.get("ur").x + 1, choice), 'r');
                    }
                    break;
                }
                case 'u' : {
                    int minX = leftX + 1;
                    int maxX = rightX - 1;
                    int choice = RandomUtils.uniform(RANDOM, minX, maxX + 1);
                    if (validatePoint(upY + 1, choice) && isNothing(upY + 1, choice)) {
                        world[choice][cornerMap.get("ul").y] = Tileset.FLOOR;
                        entries.put(new Point(choice, cornerMap.get("ul").y + 1), 'u');
                    }
                    break;
                }
                default : {
                    int minX = leftX + 1;
                    int maxX = rightX - 1;
                    int choice = RandomUtils.uniform(RANDOM, minX, maxX + 1);
                    if (validatePoint(downY - 1, choice) && isNothing(downY - 1, choice)) {
                        world[choice][cornerMap.get("ll").y] = Tileset.FLOOR;
                        entries.put(new Point(choice, cornerMap.get("ll").y - 1), 'd');
                    }
                    break;
                }
            }
        }

        private void closeOpening(Point p) {
            if (validatePoint(p.x, p.y - 1) && isFloor(p.x, p.y - 1)) {
                world[p.x][p.y - 1] = Tileset.WALL;
            } else if (validatePoint(p.x, p.y + 1) && isFloor(p.x, p.y + 1)) {
                world[p.x][p.y + 1] = Tileset.WALL;
            } else if (validatePoint(p.x + 1, p.y) && isFloor(p.x + 1, p.y)) {
                world[p.x + 1][p.y] = Tileset.WALL;
            } else if (validatePoint(p.x - 1, p.y) && isFloor(p.x - 1, p.y)) {
                world[p.x - 1][p.y] = Tileset.WALL;
            }
        }

        public HashMap<Point, Character> entryMap() {
            return entries;
        }

        public boolean hasEntries() {
            return entryMap().size() > 0;
        }

        private boolean validateCorners() {
            if (cornerMap.get("ur").x - cornerMap.get("ul").x < 4) {
                return false;
            } else if (cornerMap.get("ul").y - cornerMap.get("ll").y < 4) {
                return false;
            } else if (cornerMap.get("ul").x > cornerMap.get("ur").x) {
                return false;
            } else if (cornerMap.get("ul").y < cornerMap.get("ll").y) {
                return false;
            }
            return true;
        }

        private boolean validatePoint(int x, int y) {
            return x >= 0 && x < world.length && y >= 0 && y < world[0].length;
        }
    }

    private void getUserInput() {
        if (StdDraw.hasNextKeyTyped()) {
            Character c = StdDraw.nextKeyTyped();
            if (c == 'm' || c == 'M') {
                boolean continu = true;
                while (continu) {
                    StdDraw.clear();
                    drawMenu();
                    StdDraw.show();
                    continu = !menuChoiceMade();
                }
                ter.initialize(WIDTH, 50);
            } else if (c == ':') {
                while (!StdDraw.hasNextKeyTyped()) {
                    int x = 5;
                }
                Character q = StdDraw.nextKeyTyped();
                if (q.equals('Q') || q.equals('q')) {
                    quit();
                }
            } else {
                performMovement(c);
            }
        }
    }

    private void updateBoardState(Point p) {
        world[playerPosition.x][playerPosition.y] = Tileset.FLOOR;
        world[p.x][p.y] = Tileset.AVATAR;
        playerPosition = p;
    }


    private void load(int n) {
        world = new TETile[WIDTH][HEIGHT];
        ter = new TERenderer();
        ter.initialize(WIDTH, 50);
        String s;
        switch (n) {
            case 2 : {
                s = "./save_data2.txt";
                break;
            }
            case 3 : {
                s = "./save_data3.txt";
                break;
            }

            default : {
                s = "./save_data1.txt";
            }
        }

        File file = new File(s);
        if (!file.exists()) {
            ter.initialize(WIDTH, 50);
            titleScreen();
        } else {
            Path filePath = Paths.get(s);
            doors = new ArrayList<>();
            try {
                Scanner scnr = new Scanner(filePath, ENCODING.name());
                String seedAsString = scnr.nextLine();
                inpSeed = Long.parseLong(seedAsString);
                seed = inpSeed;
                RANDOM = new Random(seed);
                for (int h = HEIGHT - 1; h >= 0; h--) {
                    String currRow = scnr.nextLine();
                    addRow(currRow, h);
                }
            } catch (IOException e) {
                //
            }
        }
    }

    private void addRow(String s, int row) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String single = Character.toString(c);
            int index = Integer.parseInt(single);
            world[i][row] = TILES[index];
            if (index == 3) {
                playerPosition = new Point(i, row);
            }
            if (index == 4) {
                doors.add(new Point(i, row));
            }
        }
    }

    private void quit() {
        gameOver = true;
    }

    private void drawMenu() {
        StdDraw.clear();
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.setFont(BODONI_BIG);
        StdDraw.text(40, 45, "MAIN MENU");
        StdDraw.setFont(BODONI);
        StdDraw.text(40, 30, "Continue [C]");
        StdDraw.text(40, 20, "Save [S]");
        StdDraw.text(40, 10, "Save and Quit [Q]");
    }

    private boolean menuChoiceMade() {
        if (StdDraw.hasNextKeyTyped()) {
            char c = StdDraw.nextKeyTyped();
            if (c == 'c' || c == 'C') {
                return true;
            } else if (c == 's' || c == 'S') {
                UI test = new UI(WIDTH, HEIGHT);
                save(test.menuChoiceMade());
                ter.initialize(WIDTH, 50);
                return true;
            } else if (c == 'q' || c == 'Q') {
                quit();
                return true;
            }
            return true;
        }
        return false;
    }

    private void titleScreen() {
        boolean continu = true;
        while (continu) {
            StdDraw.clear();
            StdDraw.setFont(BODONI_BIG);
            StdDraw.text(40, 30, "New Game [N]");
            StdDraw.text(40, 20, "Load Game [L]");
            StdDraw.text(40, 10, "Quit [Q]");
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'L') {
                    UI test = new UI(WIDTH, HEIGHT);
                    load(test.menuChoiceMade());
                    continu = false;
                } else if (c == 'N') {
                    newGame();
                    continu = false;

                } else if (c == 'Q') {
                    continu = false;
                    quit();
                }
            }
            StdDraw.show();
        }
    }

    private void newGame() {
        int afterS = 0;
        String s = "";
        boolean continu = true;
        while (continu) {
            if (StdDraw.hasNextKeyTyped()) {
                char nk = StdDraw.nextKeyTyped();
                if (nk == 's' || nk == 'S') {
                    continu = false;
                } else {
                    s += nk;
                }
            }
        }
        seed = Long.parseLong(s);
        RANDOM = new Random(seed);
        world = new TETile[WIDTH][HEIGHT];
        ter = new TERenderer();
        ter.initialize(WIDTH, 50);
        entries = new HashMap<>();
        fringe = new ArrayDeque<>();
        sideArray = new Character[]{'u', 'd', 'l', 'r'};
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        if (RANDOM.nextInt() % 2 == 0) {
            hallway(new Point(RandomUtils.uniform(RANDOM, 10, 30),
                            RandomUtils.uniform(RANDOM, 20, 40)),
                    RandomUtils.uniform(RANDOM, 1, 20),
                    RandomUtils.uniform(RANDOM, 1, 20), 4, true, 'f');
        } else {
            makeFirstRoom();
        }
        while (!fringe.isEmpty()) {
            if (RANDOM.nextInt() % 2 == 0) {
                Point p = fringe.removeFirst();
                relaxHallway(p, entries.get(p));
            } else {
                Point e = fringe.remove();
                int h = RandomUtils.uniform(RANDOM, 4, 15);
                int w = RandomUtils.uniform(RANDOM, 4, 15);
                int n = RandomUtils.uniform(RANDOM, 4);
                Room r = new Room(world, e, h, w, n, entries.get(e), RANDOM);
                if (r.hasEntries()) {
                    for (Point pt : r.entryMap().keySet()) {
                        fringe.addLast(pt);
                        entries.put(pt, r.entryMap().get(pt));
                    }
                }
            }
        }
        encounters();
    }

    private void drawHeader() {
        StdDraw.setFont(BODONI_SMOL);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.textRight(75, 45, "You are in: Main Room");
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        if (checkForFloor(x, y)) {
            StdDraw.textLeft(5, 45, "floor");
        } else if (checkforWall(x, y)) {
            StdDraw.textLeft(5, 45, "wall");
        } else if (checkforLockedDoor(x, y)) {
            StdDraw.textLeft(5, 45, "door");
        } else if (checkforAvatar(x, y)) {
            StdDraw.textLeft(5, 45, "you");
        } else if (checkForNothing(x, y)) {
            StdDraw.textLeft(5, 45, "nothing");
        }
        StdDraw.show();
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        titleScreen();
        while (!gameOver) {
            ter.renderFrame(world);
            drawHeader();
            getUserInput();
        }
        UI test = new UI(WIDTH, HEIGHT);
        save(test.menuChoiceMade());
        System.exit(0);
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     * @source for iterating through a string:
     * https://stackoverflow.com/questions/
     * 8894258/fastest-way-to-iterate-over-all-the-chars-in-a-string/11876086
     * @source for converting string to int:
     * https://stackoverflow.com/questions/
     * 5585779/how-do-i-convert-a-string-to-an-int-in-java
     */
    public TETile[][] interactWithInputString(String input) {
        if (input.charAt(0) == 'l' || input.charAt(0) == 'L') {
            load(1);
            for (int i = 1; i < input.length(); i++) {
                if (input.charAt(i) == ':') {
                    break;
                }
                performMovement(input.charAt(i));
            }
        } else {
            int afterS = 0;
            String s = "";
            for (int i = 1; i < input.length(); i++) {
                if (input.charAt(i) == 's' || input.charAt(i) == 'S') {
                    afterS = i + 1;
                    break;
                }
                s += input.charAt(i);
            }
            seed = Long.parseLong(s);
            RANDOM = new Random(seed);
            world = new TETile[WIDTH][HEIGHT];
            ter = new TERenderer();
            ter.initialize(WIDTH, 50);
            entries = new HashMap<>();
            fringe = new ArrayDeque<>();
            sideArray = new Character[]{'u', 'd', 'l', 'r'};
            for (int x = 0; x < WIDTH; x += 1) {
                for (int y = 0; y < HEIGHT; y += 1) {
                    world[x][y] = Tileset.NOTHING;
                }
            }
            if (RANDOM.nextInt() % 2 == 0) {
                Point p = new Point(RandomUtils.uniform(RANDOM, 10, 30),
                        RandomUtils.uniform(RANDOM, 20, 40));
                hallway(p, RandomUtils.uniform(RANDOM, 1, 20),
                        RandomUtils.uniform(RANDOM, 1, 20), 4, true, 'f');
                playerPosition = p;
            } else {
                makeFirstRoom();
            }
            while (!fringe.isEmpty()) {
                if (RANDOM.nextInt() % 2 == 0) {
                    Point p = fringe.removeFirst();
                    relaxHallway(p, entries.get(p));
                } else {
                    Point e = fringe.remove();
                    int h = RandomUtils.uniform(RANDOM, 4, 15);
                    int w = RandomUtils.uniform(RANDOM, 4, 15);
                    int n = RandomUtils.uniform(RANDOM, 4);
                    Room r = new Room(world, e, h, w, n, entries.get(e), RANDOM);
                    if (r.hasEntries()) {
                        for (Point pt : r.entryMap().keySet()) {
                            fringe.addLast(pt);
                            entries.put(pt, r.entryMap().get(pt));
                        }
                    }
                }
            }
            encounters();
            if (afterS < input.length()) {
                for (int i = afterS; i < input.length(); i++) {
                    if (input.charAt(i) == ':') {
                        break;
                    }
                    performMovement(input.charAt(i));
                }
            }
        }
        save(1);
        drawWorld();
        return world;
    }

    private void makeFirstRoom() {
        int x = RandomUtils.uniform(RANDOM, 10, 30);
        int y = RandomUtils.uniform(RANDOM, 10, 20);
        int l = RandomUtils.uniform(RANDOM, 4, 15);
        int w = RandomUtils.uniform(RANDOM, 4, 15);
        int n = RandomUtils.uniform(RANDOM, 1, 4);
        Point start = new Point(x, y);
        playerPosition = start;
        Room firstObj = new Room(world, start, l, w, n, 'f', RANDOM);
        for (Point p : firstObj.entryMap().keySet()) {
            fringe.addLast(p);
            entries.put(p, firstObj.entryMap().get(p));
        }
    }

    private void drawWorld() {
        ter.renderFrame(world);
    }

    private Boolean validateMove(Character move) {
        if (move.equals('w') || move.equals('W')) {
            return checkForFloor(playerPosition.x, playerPosition.y + 1);
        } else if (move.equals('a') || move.equals('A')) {
            return checkForFloor(playerPosition.x - 1, playerPosition.y);
        } else if (move.equals('s') || move.equals('S')) {
            return checkForFloor(playerPosition.x, playerPosition.y - 1);
        } else if (move.equals('d') || move.equals('D')) {
            return checkForFloor(playerPosition.x + 1, playerPosition.y);
        }
        return false;
    }

    private Boolean isLockedDoor(Character move) {
        if (move.equals('w') || move.equals('W')) {
            return checkforLockedDoor(playerPosition.x, playerPosition.y + 1);
        } else if (move.equals('a') || move.equals('A')) {
            return checkforLockedDoor(playerPosition.x - 1, playerPosition.y);
        } else if (move.equals('s') || move.equals('S')) {
            return checkforLockedDoor(playerPosition.x, playerPosition.y - 1);
        } else if (move.equals('d') || move.equals('D')) {
            return checkforLockedDoor(playerPosition.x + 1, playerPosition.y);
        }
        return false;
    }

    private void performMovement(Character move) {
        Point prevPlayer = playerPosition;
        if (isLockedDoor(move)) {
            Encounter enc = new Encounter(RANDOM);
            Point p = doors.get(RandomUtils.uniform(RANDOM, 0, doors.size() - 1));
            drawHeader();
            if (checkForFloor(p.x, p.y + 1)) {
                if (RANDOM.nextInt() % 2 == 0) {
                    playerPosition = new Point(p.x, p.y + 1);
                    world[prevPlayer.x][prevPlayer.y] = Tileset.FLOOR;
                    world[playerPosition.x][playerPosition.y] = Tileset.AVATAR;
                } else {
                    playerPosition = new Point(p.x, p.y - 1);
                    world[prevPlayer.x][prevPlayer.y] = Tileset.FLOOR;
                    world[playerPosition.x][playerPosition.y] = Tileset.AVATAR;
                }
            } else {
                if (RANDOM.nextInt() % 2 == 0) {
                    playerPosition = new Point(p.x + 1, p.y);
                    world[prevPlayer.x][prevPlayer.y] = Tileset.FLOOR;
                    world[playerPosition.x][playerPosition.y] = Tileset.AVATAR;
                } else {
                    playerPosition = new Point(p.x - 1, p.y);
                    world[prevPlayer.x][prevPlayer.y] = Tileset.FLOOR;
                    world[playerPosition.x][playerPosition.y] = Tileset.AVATAR;
                }
            }
        } else if (validateMove(move)) {
            if (move.equals('w') || move.equals('W')) {
                updateBoardState(new Point(playerPosition.x, playerPosition.y + 1));
            } else if (move.equals('a') || move.equals('A')) {
                updateBoardState(new Point(playerPosition.x - 1, playerPosition.y));
            } else if (move.equals('s') || move.equals('S')) {
                updateBoardState(new Point(playerPosition.x, playerPosition.y - 1));
            } else if (move.equals('d') || move.equals('D')) {
                updateBoardState(new Point(playerPosition.x + 1, playerPosition.y));
            }
            drawWorld();
        }
    }

    /* @source for saving a string to .txt:
    https://stackoverflow.com/questions/1053467/
    how-do-i-save-a-string-to-a-text-file-using-java */
    private void save(Integer slot) {
        try (PrintWriter f = new PrintWriter("./save_data" + slot + ".txt")) {
            f.println(seed);
            for (int h = HEIGHT - 1; h >= 0; h--) {
                String s = "";
                for (int w = 0; w < WIDTH; w++) {
                    if (world[w][h].equals(Tileset.NOTHING)) {
                        s += "0";
                    } else if (world[w][h].equals(Tileset.WALL)) {
                        s += "1";
                    } else if (world[w][h].equals(Tileset.FLOOR)) {
                        s += "2";
                    } else if (world[w][h].equals(Tileset.AVATAR)) {
                        s += "3";
                    } else {
                        s += "4";
                    }
                }
                f.println(s);
            }
        } catch (IOException e) {
            //
        }
    }

    private void encounters() {
        List<Point> enc = new LinkedList<>();
        doors = new ArrayList<>();
        for (Point p : entries.keySet()) {
            if (checkForFloor(p.x, p.y)) {
                enc.add(p);
            }
        }
        while (enc.size() > 5) {
            int ran = RandomUtils.uniform(RANDOM, 0, enc.size() - 1);
            playerPosition = enc.remove(ran);
        }
        for (Point p : enc) {
            doors.add(p);
            world[p.x][p.y] = Tileset.LOCKED_DOOR;
        }
        world[playerPosition.x][playerPosition.y] = Tileset.AVATAR;
    }

}
