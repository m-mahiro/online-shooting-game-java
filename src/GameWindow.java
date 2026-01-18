import javax.swing.JFrame;

public class GameWindow extends JFrame {

	public GameWindow() {
		setTitle("Tank Game 2D"); // ウィンドウタイトル
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false); // 画面サイズを固定

		// GamePanelを作成してウィンドウに追加
		GamePanel gamePanel = new GamePanel();
		this.add(gamePanel);

		// コンポーネントのサイズに合わせてウィンドウを自動調整
		pack();

		// 画面中央に表示
		setLocationRelativeTo(null);
		setVisible(true);
		gamePanel.startGameThread();
	}
}