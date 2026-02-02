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
        
        // スタートボタンを生成
        startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 24));
        
        // ボタンにアクションリスナーを設定
        startButton.addActionListener(startListener);
        
        // ボタンをパネル中央に配置
        add(startButton);
    }
}
