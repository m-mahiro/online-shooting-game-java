import javax.swing.*;

public class GameLauncher {
	public static void main(String[] args) {

		// Swingの推奨作法に従い、イベントディスパッチスレッド(EDT)でGUIを起動
		SwingUtilities.invokeLater(() -> {
			new GameWindow();
		});	}
}
