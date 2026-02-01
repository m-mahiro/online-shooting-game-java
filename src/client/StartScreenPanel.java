package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class StartScreenPanel extends JPanel {
    private JButton startButton;

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
