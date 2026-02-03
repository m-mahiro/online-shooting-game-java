package client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * スタート画面のパネルクラス。
 * ゲーム開始ボタンを配置し、ゲームの開始を制御する。
 */
public class StartScreenPanel extends JPanel {
    private JButton startButton;

    /**
     * StartScreenPanelのコンストラクタ。
     * スタートボタンを生成し、中央に配置する。
     *
     * @param startListener スタートボタンが押された時の処理
     */
    public StartScreenPanel(ActionListener startListener) {
        // レイアウトマネージャーを設定
        setLayout(new GridBagLayout());

        // モダンなスタートボタンを生成
        startButton = new JButton("Start Game");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 28));
        startButton.setPreferredSize(new Dimension(250, 80));

        // モダンな色設定
        startButton.setBackground(new Color(0, 122, 255)); // 鮮やかな青
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);

        // 角丸ボーダー風の効果（マウスホバー時のエフェクト）
        startButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(0, 100, 220));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(0, 122, 255));
            }
        });

        // ボタンにアクションリスナーを設定
        startButton.addActionListener(startListener);

        // ボタンをパネル中央に配置
        add(startButton);
    }


}
