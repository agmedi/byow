package tileworld.WorldsDemo;
import org.junit.Test;
import static org.junit.Assert.*;

import tileworld.TileEngine.TERenderer;
import tileworld.TileEngine.TETile;
import tileworld.TileEngine.Tileset;

import javax.swing.text.Position;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    private static final int WIDTH = 45;
    private static final int HEIGHT = 40;
    private static final long SEED = 2863123;
    private static final Random RANDOM = new Random(SEED);

    /**
     * Adds a hexagon of side length s
     * x & y is bottom leftmost position
     * of rectangle that envelops the hexagon
     */
    public static void addHexagon(TETile[][] world, int x, int y, int s, TETile t) {

        int maxCols = s + 2 * (s - 1);

        for (int i = 0; i < s; i++) {
            addRow(world, s + 2 * i, maxCols, x, y + i, t);
        }

        for (int i = 0; i < s; i++) {
            addRow(world, maxCols - 2 * i, maxCols, x, y + s + i, t);
        }
    }

    /**
     * Adds a row of length l centered along a row of width w
     * x is leftmost position of row
     */
    private static void addRow(TETile[][] world, int l, int w, int x, int y, TETile t) {
        int space = (w - l) / 2;
        int start = x + space;
        int end = x + w - space;

        for (int i = start; i < end; i++) {
            world[i][y] = t;
        }
    }

    private static void initWorld(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(3);
        switch (tileNum) {
            case 0: return Tileset.MOUNTAIN;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.GRASS;
            default: return Tileset.WATER;
        }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] tiles = new TETile[WIDTH][HEIGHT];
        initWorld(tiles);

        for (int i = 0; i < 3; i++) {
            addHexagon(tiles, 4, 10 + i * 6, 3, randomTile());
        }

        for (int i = 0; i < 4; i++) {
            addHexagon(tiles, 8, 7 + i * 6, 3, randomTile());
        }

        for (int i = 0; i < 5; i++) {
            addHexagon(tiles, 12, 4 + i * 6, 3, randomTile());
        }

        for (int i = 0; i < 4; i++) {
            addHexagon(tiles, 16, 7 + i * 6, 3, randomTile());
        }

        for (int i = 0; i < 3; i++) {
            addHexagon(tiles, 20, 10 + i * 6, 3, randomTile());
        }

        ter.renderFrame(tiles);
    }
}
