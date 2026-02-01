package client;

import client.ui.GameUI;
import stage.GameStage;
import stage.StageGenerator;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;

public class GamePanel extends JPanel {

    private final GameEngine gameEngine;
    private final InputHandler input;

    public GamePanel(StageGenerator generator) {
        // パネル設定
        this.setBackground(Color.WHITE);
        this.setDoubleBuffered(true);
        this.setPreferredSize(new Dimension(1000, 700));

        // 入力ハンドラ
        this.input = new MouseKeyboardInput(this);

        // ゲームエンジンを生成し、再描画用のコールバックと入力ハンドラを渡す
        this.gameEngine = new GameEngine(this::repaint, this.input, generator);

        // エンジンにリサイズを通知するためのリスナーを追加
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (gameEngine != null) {
                    gameEngine.setCanvasSize(getWidth(), getHeight());
                }
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (gameEngine != null) {
            gameEngine.startGameThread(getWidth(), getHeight());
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;

        if (gameEngine == null) {
            return;
        }

        GameStage stage = gameEngine.getStage();
        GameUI ui = gameEngine.getUi();

        // カメラのトランスフォームを適用
        g.transform(gameEngine.getCanvasTransform());

        // ゲームオブジェクトを描画
        if (stage != null) {
            stage.draw(g, this.getWidth(), this.getHeight(), gameEngine.getZoomDegrees());
        }

        // UI描画のためにトランスフォームをリセット
        g.setTransform(new AffineTransform());

        // UIを描画
        if (ui != null) {
            ui.draw(g, this.getWidth(), this.getHeight());
        }
    }
}