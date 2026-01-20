import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkManager extends Thread {

	private GamePanel gamePanel;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;

	public NetworkManager(GamePanel gamePanel) {
		this.gamePanel = gamePanel;
	}

	@Override
	public void run() {
		try {
			// サーバーに接続 (IPは localhost 固定、適宜変更)
			socket = new Socket("localhost", 10000);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			System.out.println("サーバーに接続しました。");

			// 1. 最初の行は "ID is client number" 形式で来る想定
			// MyServer.java の仕様: "myOut.println(number + " is client number");"
			String initMsg = in.readLine();
			if (initMsg != null) {
				String[] tokens = initMsg.split(" ");
				int myTankID = Integer.parseInt(tokens[0]) % 4;

				// GamePanelに自分のIDを通知
				gamePanel.setMyTankID(myTankID);

				// 名前送信（サーバーが期待しているので送る）
				out.println("Player" + myTankID);

				// 色情報を受け取る（今回は読み捨てるか、後で利用）
				in.readLine();
			}

			// 2. メインループ: サーバーからのメッセージを監視
			while (true) {
				try {
					String msg = in.readLine();
					if (msg == null) break;

					parseMessage(msg);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null) socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// メッセージの解釈
	private void parseMessage(String msg) {
		// メッセージ例: "MOVE 1 100.5 200.5 1.57"
		String[] tokens = msg.split(" ");
		String cmd = tokens[0];

		try {
			switch (cmd) {
				case "MOVE": {
					int id = Integer.parseInt(tokens[1]);
					double x = Double.parseDouble(tokens[2]);
					double y = Double.parseDouble(tokens[3]);
					double angle = Double.parseDouble(tokens[4]);
					if (gamePanel.getMyTankID() == id) return;
					gamePanel.getTankByID(id).setPosition(x, y);
					break;
				}
				case "BULLET": {
					int id = Integer.parseInt(tokens[1]);
					double x = Double.parseDouble(tokens[2]);
					double y = Double.parseDouble(tokens[3]);
					double angle = Double.parseDouble(tokens[4]);
					if (gamePanel.getMyTankID() == id) return;
					Bullet bullet = gamePanel.getTankByID(id).shootBullet();
					gamePanel.addGameObject(bullet);
					break;
				}
				case "AIM": {
					int id = Integer.parseInt(tokens[1]);
					double targetX = Double.parseDouble(tokens[2]);
					double targetY = Double.parseDouble(tokens[3]);
					if (gamePanel.getMyTankID() == id) return;
					gamePanel.getTankByID(id).aimAt(targetX, targetY);
					break;
				}
			}
		} catch (NumberFormatException e) {
			System.err.println("パースエラー: " + msg);
		}
	}

	// --- 送信メソッド ---

	public void moveTank(int id, double x, double y, double angle) {
		if (out != null) {
			// プロトコル: MOVE id x y angle
			out.println("MOVE " + id + " " + x + " " + y + " " + angle);
		}
	}

	public void shootGun(int id, double x, double y,double angle) {
		if (out != null) {
			// プロトコル: BULLET id x y angle
			out.println("BULLET " + id + " " + x + " " + y + " " + angle);
		}
	}

	public void aimAt(int id, double targetX, double targetY) {
		if (out != null) {
			out.println("AIM " + id + " " + targetX + " " + targetY);
		}
	}
}