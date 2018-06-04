/**
 * @author 潘学海
 */

package Gomoku;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

public class Gomoku extends JFrame {
    private final Board board;
    private final Display display;
    private final JButton retractButton;
    private final JButton newGameButton;
    private final JButton loadOrSaveGameButton;
    private final JButton showRuleButton;
    
    public static final String swap2Rule = "一. 假先方在棋盘任意下三手（二黑一白），假后方有三种选择：\n" +
                                           "     1. 选黑。\n" +
                                           "     2. 选白。\n" +
                                           "     3. 下四、五两手（一黑一白），再假先方选择黑或白。\n" +
                                           "二. 黑白双方轮流落子。\n" +
                                           "三. 首选在横、竖、斜方向上成五（连续五个己方棋子）者为胜。\n" +
                                           "四. 超过五子以上不算赢也不算输。";
    
    
    public Gomoku() {
        super("五子棋");
        board = new Board();
        display = new Display(60, 60, board);
        setContentPane(display);
        
        newGameButton = new JButton("新游戏");
        loadOrSaveGameButton = new JButton("载入游戏");
        retractButton = new JButton("悔棋");
        showRuleButton = new JButton("游戏规则");
        retractButton.setEnabled(false);
        
        Font font = new Font(Font.DIALOG, Font.PLAIN, Display.sideLength);
        newGameButton.setFont(font);
        loadOrSaveGameButton.setFont(font);
        retractButton.setFont(font);
        showRuleButton.setFont(font);
        
        newGameButton.addActionListener(e -> {
            if (!board.isGameStarted())
                display.newGame();
            else
                display.admitDefeat();
        });
        loadOrSaveGameButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (board.isGameStarted()) {
                int state = fileChooser.showSaveDialog(this);
                if (state != JFileChooser.APPROVE_OPTION)
                    return;
                try {
                    display.saveGame(fileChooser.getSelectedFile());
                }
                catch (IOException exception) {
                    JOptionPane.showMessageDialog(this, "保存游戏失败");
                }
            }
            else {
                int state = fileChooser.showOpenDialog(this);
                if (state == JFileChooser.CANCEL_OPTION)
                    return;
                try {
                    display.loadGame(fileChooser.getSelectedFile());
                }
                catch (IOException exception) {
                    JOptionPane.showMessageDialog(this, "文件读取错误，载入游戏失败。");
                }
                catch (BadInputStoneException exception) {
                    JOptionPane.showMessageDialog(this, "文件数据损坏，载入游戏失败。");
                }
            }
        });
        retractButton.addActionListener(e -> display.removeStone());
        showRuleButton.addActionListener(e -> JOptionPane.showMessageDialog(this, swap2Rule, "Swap2 规则", JOptionPane.INFORMATION_MESSAGE));
        board.addGameStartedChangeListener(evt -> {
            if ((Boolean) evt.getNewValue()) {
                newGameButton.setText("认输");
                loadOrSaveGameButton.setText("保存游戏");
            }
            else {
                newGameButton.setText("新游戏");
                loadOrSaveGameButton.setText("载入游戏");
            }
            retractButton.setEnabled(board.canRetractStone());
        });
        board.addHistorySizeChangeListener(evt -> retractButton.setEnabled(board.canRetractStone()));
        
        display.setLayout(null);
        setSize(960, 700);
        newGameButton.setBounds(display.getBoundXR() + 2 * Display.sideLength, display.getBoundYU() + 3 * Display.sideLength, 220, 2 * Display.sideLength);
        loadOrSaveGameButton.setBounds(display.getBoundXR() + 2 * Display.sideLength, display.getBoundYU() + 6 * Display.sideLength, 220, 2 * Display.sideLength);
        retractButton.setBounds(display.getBoundXR() + 2 * Display.sideLength, display.getBoundYU() + 9 * Display.sideLength, 220, 2 * Display.sideLength);
        showRuleButton.setBounds(display.getBoundXR() + 2 * Display.sideLength, display.getBoundYU() + 12 * Display.sideLength, 220, 2 * Display.sideLength);
        display.add(newGameButton);
        display.add(loadOrSaveGameButton);
        display.add(retractButton);
        display.add(showRuleButton);
        
        display.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }
            
            
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    display.putStoneFromMouse(e.getX(), e.getY());
                }
                catch (GameNotStartedException | BadInputStoneException ignored) {
                }
            }
            
            
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            
            
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            
            
            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    
    public static void main(String[] args) {
        Gomoku gomoku = new Gomoku();
    }
}