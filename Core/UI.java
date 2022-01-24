package tileworld.Core;

import edu.princeton.cs.introcs.StdDraw;
import java.awt.*;


public class UI {

    private int width;
    private int height;

    private final Font ARIAL_BLACK= new Font("Arial Black", Font.PLAIN, 16);
    private final Font AVENIR_NEXT = new Font("Avenir Next Condensed", Font.PLAIN, 16);
    private final Font AVENIR_NEXT_SMOL = new Font("Avenir Next Condensed", Font.PLAIN, 12);
    private final Font BODONI = new Font("Bodoni 72", Font.PLAIN, 16);
    private final Font BODONI_BIG = new Font("Bodoni 72", Font.PLAIN, 40);
    private final Font BODONI_SMOL = new Font("Bodoni 72", Font.PLAIN, 13);

    private final Color GRAY_A05 = new Color(200, 200, 200, 240);
    private final Color GRAY_O10 = new Color(150, 150, 150);
    private final Color GRAY_O20 = new Color(80,80, 80);
    private final Color GRAY_O005 = new Color(230, 230, 230);

    public UI(int width, int height) {
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
        StdDraw.clear();
        this.drawBGCircles();
        StdDraw.show();
    }

    public UI() {
        this(32, 32);
        StdDraw.clear();
        this.drawBGCircles();
        StdDraw.show();
    }

    public int menuChoiceMade() {
        while (!StdDraw.hasNextKeyTyped()) {
        }
        int i = StdDraw.nextKeyTyped();
        if (i == 49) {
            return 1;
        } else if (i == 50) {
            return 2;
        } else if (i == 51) {
            return 3;
        } else {
            return 0;
        }
    }

    private void drawBGCircles() {
        StdDraw.setPenColor(StdDraw.BOOK_BLUE);
        StdDraw.filledCircle(width / 4, height / 3 + 5, 32 / 10);
        StdDraw.filledCircle(width / 2, height / 3 + 5, 32 / 10);
        StdDraw.filledCircle(3 * width / 4, height / 3 + 5, 32 / 10);
        StdDraw.setFont(BODONI);
        StdDraw.setFont(BODONI_BIG);
        StdDraw.text(width / 2, 2 * height / 3, "Choose a save slot:");
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(width / 4, height / 3 + 5, "1");
        StdDraw.text(width / 2, height / 3 + 5, "2");
        StdDraw.text(3 * width / 4, height / 3 + 5, "3");
    }

}
