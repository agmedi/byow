package tileworld.Core;

import tileworld.TileEngine.TETile;
import tileworld.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;

public class Encounter {

    private static final int TILE_SIZE = 16;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;
    private TETile type;
    private int tileInd;
    private Random rand;
    private TETile[] tileArr = {Tileset.GRASS, Tileset.WATER, Tileset.FLOWER,
            Tileset.MOUNTAIN, Tileset.TREE, Tileset.SAND};
    private TETile[][] miniWorld;
    private Point playerPos;
    private Point exit;

    private static final Font BODONI_SMOL = new Font("Bodoni 72", Font.PLAIN, 15);
    MiniRoom mainroom;

    Encounter(Random r) {
        miniWorld = new TETile[80][40];
        rand = r;
        int t = RandomUtils.uniform(rand, 6);
        tileInd = t;
        type = tileArr[t];
        initialize(80, 50);
        initWorld();

        mainroom = makeFirstRoom();

        boolean continu = true;

        while (continu) {
            StdDraw.clear(Color.BLACK);
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                getMoveCommand(c);
            }
            if (playerPos.equals(exit)) {
                continu = false;
            }
            addHeader();
            renderFrame(miniWorld);
            StdDraw.show();
        }


    }

    private void getMoveCommand(char m) {
        if (m == 'W' || m == 'w') {
            if (validMove(playerPos.x, playerPos.y + 1)) {
                move(playerPos.x, playerPos.y + 1);
            }
        } else if (m == 'S' || m == 's') {
            if (validMove(playerPos.x, playerPos.y - 1)) {
                move(playerPos.x, playerPos.y - 1);
            }
        } else if (m == 'a' || m == 'A') {
            if (validMove(playerPos.x - 1, playerPos.y)) {
                move(playerPos.x - 1, playerPos.y);
            }
        } else if (m == 'd' || m == 'D') {
            if (validMove(playerPos.x + 1, playerPos.y)) {
                move(playerPos.x + 1, playerPos.y);
            }
        }
    }

    private void move(int x, int y) {
        miniWorld[x][y] = Tileset.AVATAR;
        miniWorld[playerPos.x][playerPos.y] = type;
        playerPos = new Point(x, y);
    }

    private boolean validMove(int x, int y) {
        return miniWorld[x][y].equals(type) || miniWorld[x][y].equals(Tileset.NOTHING);
    }


    public void renderFrame(TETile[][] world) {
        int numXTiles = world.length;
        int numYTiles = world[0].length;
        for (int x = 0; x < numXTiles; x += 1) {
            for (int y = 0; y < numYTiles; y += 1) {
                if (world[x][y] == null) {
                    throw new IllegalArgumentException("Tile at position x=" + x + ", y=" + y
                            + " is null.");
                }
                world[x][y].draw(x + xOffset, y + yOffset);
            }
        }
    }

    private void addHeader() {
        StdDraw.setFont(BODONI_SMOL);
        StdDraw.setPenColor(Color.WHITE);
        String[] rooms = {"grass", "water", "flower", "mountain", "tree", "sand"};
        StdDraw.textRight(75, 45, "You are in: " + rooms[tileInd] + " room");

    }

    public void initialize(int w, int h, int xOff, int yOff) {
        this.width = w;
        this.height = h;
        this.xOffset = xOff;
        this.yOffset = yOff;
        StdDraw.setCanvasSize(width * TILE_SIZE, height * TILE_SIZE);
        Font font = new Font("Monaco", Font.BOLD, TILE_SIZE - 2);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);

        StdDraw.clear(new Color(0, 0, 0));

        StdDraw.enableDoubleBuffering();
    }

    public void initialize(int w, int h) {
        initialize(w, h, 0, 0);
    }

    private MiniRoom makeFirstRoom() {
        int x = RandomUtils.uniform(rand, 10, 20);
        int y = RandomUtils.uniform(rand, 30, 40);
        int l = RandomUtils.uniform(rand, 10, 20);
        int w = RandomUtils.uniform(rand, 30, 50);
        Point start = new Point(x, y);
        MiniRoom firstObj = new MiniRoom(miniWorld, type, start, l, w, 0, 'f', rand);
        return firstObj;
    }

    private void initWorld() {
        for (int x = 0; x < 80; x += 1) {
            for (int y = 0; y < 40; y += 1) {
                miniWorld[x][y] = Tileset.NOTHING;
            }
        }
    }

    private class MiniRoom {

        private int height;
        private int width;
        private HashMap<Point, Character> entries = new HashMap<>();
        private Random RANDOM;
        private final String[] cornerNames = {"ul", "ur", "ll", "lr"};
        Point start;
        private HashMap<String, Point> cornerMap = new HashMap<>(4);
        TETile[][] world;
        char side;
        private TETile floorType;

        MiniRoom(TETile[][] tiles, TETile f, Point p, int h, int w, int n, char c, Random r) {
            height = h;
            width = w;
            RANDOM = r;
            start = p;
            world = tiles;
            side = c;
            floorType = f;

            makeFirst(p);
        }

        private void makeFirst(Point p) {
            int ll = Math.max(p.y - height + 1, 0);
            cornerMap.put("ul", new Point(p.x, p.y));
            cornerMap.put("ur", new Point(p.x + width, p.y));
            cornerMap.put("ll", new Point(p.x, ll));
            cornerMap.put("lr", new Point(p.x + width, ll));
            makeWalls();
            makeFloors();
            addInOut();
        }

        private void addInOut() {
            addOpening('l');
            addOpening('r');
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
                world[start.x][start.y] = floorType;
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
                    world[ll.x + 1 + i][ll.y + 1 + j] = floorType;
                }
            }

        }

        private void addOpening(char s) {
            int leftX = cornerMap.get("ul").x;
            int rightX = cornerMap.get("lr").x;
            int upY = cornerMap.get("ul").y;
            int downY = cornerMap.get("lr").y;
            switch (s) {
                case 'l' : {
                    int minY = downY + 2;
                    int maxY = upY - 2;
                    int choice = RandomUtils.uniform(RANDOM, minY, maxY + 1);
                    if (validatePoint(leftX - 1, choice) && isNothing(leftX - 1, choice)) {
                        world[cornerMap.get("ul").x][choice] = Tileset.AVATAR;
                        playerPos = new Point(cornerMap.get("ul").x, choice);
                    }
                    break;
                }
                case 'r' : {
                    int minY = downY + 2;
                    int maxY = upY - 2;
                    int choice = RandomUtils.uniform(RANDOM, minY, maxY + 1);
                    if (validatePoint(rightX + 1, choice) && isNothing(rightX + 1, choice)) {
                        world[cornerMap.get("ur").x][choice] = floorType;
                        exit = new Point(cornerMap.get("ur").x + 1, choice);

                    }
                    break;
                }
                case 'u' : {
                    int minX = leftX + 2;
                    int maxX = rightX - 2;
                    int choice = RandomUtils.uniform(RANDOM, minX, maxX + 1);
                    if (validatePoint(choice, upY + 1) && isNothing(choice, upY + 1)) {
                        world[choice][cornerMap.get("ul").y] = Tileset.AVATAR;
                        playerPos = new Point(choice, cornerMap.get("ul").y);
                    }
                    break;
                }
                default : {
                    int minX = leftX + 2;
                    int maxX = rightX - 2;
                    int choice = RandomUtils.uniform(RANDOM, minX, maxX + 1);
                    if (validatePoint(choice, downY - 1) && isNothing(choice, downY - 1)) {
                        world[choice][cornerMap.get("ll").y] = floorType;
                        exit = new Point(choice, cornerMap.get("ll").y - 1);
                    }
                    break;
                }
            }
        }

        private boolean validatePoint(int x, int y) {
            return x >= 0 && x < world.length && y >= 0 && y < world[0].length;
        }

    }
}
