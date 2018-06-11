/**
 * @author 潘学海
 */

package Gomoku;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class Display extends JPanel {
    private final int boundXL, boundXR, boundYU, boundYD;
    private final int[] stoneCenterX;
    private final int[] stoneCenterY;
    private final Board board;
    private final JLabel messageLabel;
    private final List<Integer> indexOfHighlightedStones;
    
    public static final int sideLength = 40;
    public static final int starRadius = 5;
    public static final int stoneRadius = 18;
    private static final Color backgroundColor = new Color(244, 240, 220);
    private static final Color black = new Color(32, 32, 32);
    private static final Color white = new Color(220, 220, 220);
    private static final Color gray = new Color(160, 160, 160);
    private static final Font indexFont = new Font(Font.DIALOG, Font.PLAIN, 3 * stoneRadius / 4);
    
    
    public Display(int x, int y, Board board) {
        super();
        this.board = board;
        indexOfHighlightedStones = new ArrayList<Integer>();
        messageLabel = new JLabel("");
        boundXL = x;
        boundYU = y;
        stoneCenterX = new int[Board.n + 2];
        stoneCenterY = new int[Board.n + 2];
        for (int i = 0; i <= Board.n + 1; ++i) {
            stoneCenterX[i] = boundXL + sideLength * (i - 1);
            stoneCenterY[i] = boundYU + sideLength * (i - 1);
        }
        boundXR = stoneCenterX[Board.n];
        boundYD = stoneCenterY[Board.n];
        
        messageLabel.setBounds(boundXR + 7 * sideLength / 2, boundYU, 5 * sideLength, sideLength);
        messageLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, sideLength / 2));
        add(messageLabel);
    }
    
    
    public void newGame() {
        board.newGame();
        indexOfHighlightedStones.clear();
        Graphics2D g2D = (Graphics2D) getGraphics();
        paintBoard(g2D);
        paintPlayer(g2D);
    }
    
    
    public void reset() {
        board.reset();
        indexOfHighlightedStones.clear();
    }
    
    
    public void loadGame(File file) throws IOException, BadInputStoneException {
        board.loadGame(file);
        paintStonesWithIndexFromHistory((Graphics2D) getGraphics());
    }
    
    
    public void saveGame(File file) throws IOException {
        board.saveGame(file);
    }
    
    
    public void gameOver(int winnerNumber) {
        reset();
        String message;
        if (winnerNumber == 1 || winnerNumber == 2)
            message = "玩家 " + winnerNumber + " 胜利";
        else
            message = "平局";
        messageLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "游戏结束", JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    public void admitDefeat() {
        gameOver(3 - board.getNextPlayerNumber());
    }
    
    
    public void choosePlayerColor() {
        if (board.getHistorySize() == 3) {
            String message = "玩家 2 选择执子颜色";
            messageLabel.setText(message);
            String[] options = {"执黑", "执白", "继续"};
            int state = JOptionPane.showOptionDialog(this,
                                                     message,
                                                     "",
                                                     JOptionPane.YES_NO_CANCEL_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE,
                                                     null,
                                                     options,
                                                     options[0]);
            if (state == JOptionPane.YES_OPTION)
                board.choosePlayer1Color(StoneType.WHITE);
            else if (state == JOptionPane.NO_OPTION)
                board.choosePlayer1Color(StoneType.BLACK);
        }
        else if (!board.isPlayerColorChosen() && board.getHistorySize() == 5) {
            String message = "玩家 1 选择执子颜色";
            messageLabel.setText(message);
            String[] options = {"执黑", "执白"};
            int state = JOptionPane.showOptionDialog(this,
                                                     message,
                                                     "",
                                                     JOptionPane.OK_CANCEL_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE,
                                                     null,
                                                     options,
                                                     options[0]);
            if (state == JOptionPane.YES_OPTION)
                board.choosePlayer1Color(StoneType.BLACK);
            else
                board.choosePlayer1Color(StoneType.WHITE);
        }
    }
    
    
    public void putStone(int i, int j) throws GameNotStartedException, StoneOutOfBoardRangeException, StoneAlreadyPlacedException {
        board.putStone(i, j);
        Graphics2D g2D = (Graphics2D) getGraphics();
        paintStone(g2D, board.getLastStone());
        List<Integer> indexOfRowStones = board.getIndexOfRowStones();
        paintStoneIndexHighlight(g2D, indexOfRowStones);
        if (!board.isPlayerColorChosen())
            choosePlayerColor();
        if (board.isGameOver()) {
            if (indexOfRowStones.size() >= 5)
                gameOver(3 - board.getNextPlayerNumber());
            else
                gameOver(0);
        }
        else
            paintPlayer(g2D);
    }
    
    
    public void retractStone() {
        try {
            Stone lastStone = board.retractStone();
            Graphics2D g2D = (Graphics2D) getGraphics();
            eraseStone(g2D, lastStone.getI(), lastStone.getJ());
            paintStoneIndexHighlight(g2D, board.getIndexOfRowStones());
            paintPlayer(g2D);
        }
        catch (GameNotStartedException ignored) {
        }
    }
    
    
    public void putStoneFromMouse(int x, int y) throws GameNotStartedException, StoneOutOfBoardRangeException, StoneAlreadyPlacedException {
        int i = getIFromX(x), j = getJFromY(y);
        int xGrid = getXFromI(i), yGrid = getYFromJ(j);
        if ((x - xGrid) * (x - xGrid) + (y - yGrid) * (y - yGrid) < stoneRadius * stoneRadius)
            putStone(i, j);
    }
    
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintBoard((Graphics2D) g);
    }
    
    
    private void paintBoard(Graphics2D g2D) {
        g2D.setColor(backgroundColor);
        g2D.fillRect(stoneCenterX[0], stoneCenterY[0], sideLength * (Board.n + 1), sideLength * (Board.n + 1));
        g2D.setColor(Color.BLACK);
        g2D.setStroke(new BasicStroke(5.0f));
        g2D.drawLine(stoneCenterX[0], stoneCenterY[0], stoneCenterX[Board.n + 1], stoneCenterY[0]);
        g2D.drawLine(stoneCenterX[0], stoneCenterY[Board.n + 1], stoneCenterX[Board.n + 1], stoneCenterY[Board.n + 1]);
        g2D.drawLine(stoneCenterX[0], stoneCenterY[0], stoneCenterX[0], stoneCenterY[Board.n + 1]);
        g2D.drawLine(stoneCenterX[Board.n + 1], stoneCenterY[0], stoneCenterX[Board.n + 1], stoneCenterY[Board.n + 1]);
        g2D.setStroke(new BasicStroke(2.0f));
        for (int i = 1; i <= Board.n; ++i) {
            g2D.drawLine(stoneCenterX[i], boundYU, stoneCenterX[i], boundYD);
            g2D.drawLine(boundXL, stoneCenterY[i], boundXR, stoneCenterY[i]);
        }
        fillCircle(g2D, 8, 8, starRadius);
        fillCircle(g2D, 4, 4, starRadius);
        fillCircle(g2D, 4, 12, starRadius);
        fillCircle(g2D, 12, 4, starRadius);
        fillCircle(g2D, 12, 12, starRadius);
    }
    
    
    private void eraseStone(Graphics2D g2D, int i, int j) {
        try {
            int centerX = getXFromI(i);
            int centerY = getYFromJ(j);
            g2D.setColor(backgroundColor);
            g2D.fillRect(centerX - sideLength / 2, centerY - sideLength / 2, sideLength, sideLength);
            g2D.setColor(Color.BLACK);
            g2D.setStroke(new BasicStroke(2.0f));
            if (i > 1)
                g2D.drawLine(centerX - sideLength / 2, centerY, centerX, centerY);
            if (i < Board.n)
                g2D.drawLine(centerX, centerY, centerX + sideLength / 2, centerY);
            if (j > 1)
                g2D.drawLine(centerX, centerY - sideLength / 2, centerX, centerY);
            if (j < Board.n)
                g2D.drawLine(centerX, centerY, centerX, centerY + sideLength / 2);
            
            if (isStar(i, j))
                fillCircle(g2D, i, j, starRadius);
        }
        catch (StoneOutOfBoardRangeException ignored) {
        }
    }
    
    
    private void paintStone(Graphics2D g2D, Stone stone) {
        if (stone.getType() != StoneType.SPACE) {
            g2D.setStroke(new BasicStroke(1.0f));
            g2D.setColor(gray);
            drawCircle(g2D, stone.getI(), stone.getJ(), stoneRadius);
            g2D.setColor(getColorFromType(stone.getType()));
            fillCircle(g2D, stone.getI(), stone.getJ(), stoneRadius);
        }
    }
    
    
    private void paintStoneIndex(Graphics2D g2D, Stone stone, int index, Color color) {
        g2D.setColor(color);
        try {
            drawCenteredString(g2D, Integer.toString(index + 1), getXFromI(stone.getI()), getYFromJ(stone.getJ()));
        }
        catch (StoneOutOfBoardRangeException ignored) {
        }
    }
    
    
    private void paintStoneIndex(Graphics2D g2D, Stone stone, int index, boolean highlight) {
        g2D.setFont(indexFont);
        if (highlight)
            paintStoneIndex(g2D, stone, index, Color.RED);
        else
            paintStoneIndex(g2D, stone, index, getOppositeColorFromType(stone.getType()));
    }
    
    
    private void paintStoneWithIndex(Graphics2D g2D, Stone stone, int index, boolean highlight) {
        if (stone.getType() != StoneType.SPACE) {
            paintStone(g2D, stone);
            paintStoneIndex(g2D, stone, index, highlight);
        }
    }
    
    
    private void eraseStoneIndexHighlight(Graphics2D g2D) {
        indexOfHighlightedStones.forEach(index -> {
            try {
                paintStoneIndex(g2D, board.getStoneFromIndex(index), index, false);
            }
            catch (ArrayIndexOutOfBoundsException ignored) {
            }
        });
        indexOfHighlightedStones.clear();
    }
    
    
    private void paintStoneIndexHighlight(Graphics2D g2D, List<Integer> indexOfStones) {
        eraseStoneIndexHighlight(g2D);
        indexOfStones.forEach(index -> {
            try {
                paintStoneIndex(g2D, board.getStoneFromIndex(index), index, true);
            }
            catch (ArrayIndexOutOfBoundsException ignored) {
            }
        });
        indexOfHighlightedStones.addAll(indexOfStones);
    }
    
    
    private void paintStonesWithIndexFromHistory(Graphics2D g2D) {
        paintBoard(g2D);
        if (board.hasNoHistory())
            return;
        indexOfHighlightedStones.clear();
        Stack<Stone> history = board.getHistory();
        int i = 0;
        for (Stone stone : history) {
            paintStoneWithIndex(g2D, stone, i, false);
            ++i;
        }
        paintStoneIndexHighlight(g2D, board.getIndexOfRowStones());
        paintPlayer(g2D);
    }
    
    
    private void paintNextStoneColor(Graphics2D g2D) {
        g2D.setStroke(new BasicStroke(1.0f));
        g2D.setColor(gray);
        g2D.drawOval(boundXR + 5 * sideLength / 2 - stoneRadius, boundYU + sideLength / 2 - stoneRadius, 2 * stoneRadius, 2 * stoneRadius);
        g2D.setColor(getColorFromType(board.getNextStoneType()));
        g2D.fillOval(boundXR + 5 * sideLength / 2 - stoneRadius, boundYU + sideLength / 2 - stoneRadius, 2 * stoneRadius, 2 * stoneRadius);
    }
    
    
    private void paintMessage(Graphics2D g2D, String message) {
        paintNextStoneColor(g2D);
        messageLabel.setText(message);
    }
    
    
    private void paintPlayer(Graphics2D g2D) {
        paintMessage(g2D, "玩家 " + board.getNextPlayerNumber() + (board.getNextStoneType() == StoneType.BLACK ? " 执黑" : " 执白"));
    }
    
    
    private void fillCircle(Graphics2D g2D, int i, int j, int r) {
        g2D.fillOval(stoneCenterX[i] - r, stoneCenterY[j] - r, 2 * r, 2 * r);
    }
    
    
    private void drawCircle(Graphics2D g2D, int i, int j, int r) {
        g2D.drawOval(stoneCenterX[i] - r, stoneCenterY[j] - r, 2 * r, 2 * r);
    }
    
    
    private void drawCenteredString(Graphics2D g2D, String text, int x, int y) {
        FontMetrics metrics = g2D.getFontMetrics();
        x -= metrics.stringWidth(text) / 2;
        y -= metrics.getHeight() / 2 - metrics.getAscent();
        g2D.drawString(text, x, y);
    }
    
    
    public static boolean isStar(int i, int j) {
        return ((i == 8 && j == 8) ||
                (i == 4 && j == 4) ||
                (i == 4 && j == 12) ||
                (i == 12 && j == 4) ||
                (i == 12 && j == 12));
    }
    
    
    public int getBoundXL() {
        return boundXL;
    }
    
    
    public int getBoundXR() {
        return boundXR;
    }
    
    
    public int getBoundYU() {
        return boundYU;
    }
    
    
    public int getBoundYD() {
        return boundYD;
    }
    
    
    public int getXFromI(int i) throws StoneOutOfBoardRangeException {
        if (i < 0 || i > Board.n + 1)
            throw new StoneOutOfBoardRangeException();
        return stoneCenterX[i];
    }
    
    
    public int getYFromJ(int j) throws StoneOutOfBoardRangeException {
        if (j < 0 || j > Board.n + 1)
            throw new StoneOutOfBoardRangeException();
        return stoneCenterY[j];
    }
    
    
    public int getIFromX(int x) {
        return Math.round(((float) (x - boundXL)) / sideLength) + 1;
    }
    
    
    public int getJFromY(int y) {
        return Math.round(((float) (y - boundYU)) / sideLength) + 1;
    }
    
    
    public static Color getColorFromType(StoneType type) {
        return (type == StoneType.BLACK ? black : white);
    }
    
    
    public static Color getOppositeColorFromType(StoneType type) {
        return (type != StoneType.BLACK ? black : white);
    }
}