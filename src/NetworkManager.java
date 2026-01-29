import java.awt.geom.Point2D;
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

	private int networkClientID;
	private int myTankID;

	public NetworkManager(GamePanel gamePanel) {
		this.gamePanel = gamePanel;
		try {
			// サーバーに接続 (IPは localhost 固定、適宜変更)
			socket = new Socket("10.75.200.52", 10000);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			System.out.println("サーバーに接続しました。");

			// 1. 最初の行は "ID is client number" 形式で来る想定
			String initMsg = in.readLine();
			if (initMsg != null) {
				String[] tokens = initMsg.split(" ");
				this.networkClientID = Integer.parseInt(tokens[0]);


				int myTankID = networkClientID % 4;
				this.setMyTankID(myTankID);

				// 名前送信（サーバーが期待しているので送る）
				out.println("Player" + myTankID);

				// 色情報を受け取る（今回は読み捨てるか、後で利用）
				in.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setMyTankID(int myTankID) {
		this.myTankID = myTankID;
	}

	public int getMyTankID() {
		return this.myTankID;
	}

	public int getNetworkClientID() {
		return this.networkClientID;
	}

	@Override
	public void run() {
		try {
			while (true) {
				try {
					String msg = in.readLine();
					if (msg == null) break;

					parseMessage(msg);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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
		GameStage stage = gamePanel.gameStage;
		int tankID = Integer.parseInt(tokens[1]);
		if (gamePanel.getMyTankID() == tankID) return;
		Tank tank = (Tank) gamePanel.gameStage.getObject(tankID);

		try {
			switch (cmd) {
				case "MOVE": {
					double x = Double.parseDouble(tokens[2]);
					double y = Double.parseDouble(tokens[3]);
					stage.getObject(tankID).setPosition(x, y);
					break;
				}
				case "BULLET": {
					Bullet bullet = tank.shootBullet();
					stage.addObject(bullet);
					break;
				}
				case "START_CHARGE": {
					tank.startEnergyCharge();
					break;
				}
				case "FINISH_CHARGE": {
					tank.finishEnergyCharge();
					break;
				}
				case "AIM": {
					double x = Double.parseDouble(tokens[2]);
					double y = Double.parseDouble(tokens[3]);
					Point2D.Double targetCoordinate = new Point2D.Double(x, y);
					tank.aimAt(targetCoordinate);
					break;
				}
				case "BLOCK": {
					Block block = tank.createBlock();
					stage.addObject(block);
					break;
				}
			}
		} catch (NumberFormatException e) {
			System.err.println("パースエラー: " + msg);
		}
	}

	// --- 送信メソッド ---

	public void locateTank(int id, Point2D.Double position) {
		if (out == null) return;
		out.println("MOVE " + id + " " + position.x + " " + position.y);
	}

	public void shootGun(int tankID) {
		if (out == null) return;
		out.println("BULLET " + tankID);
	}

	public void startCharge(int tankID) {
		if (out == null) return;
		out.println("START_CHARGE " + tankID);
	}

	public void finishCharge(int tankID) {
		if (out == null) return;
		out.println("FINISH_CHARGE " + tankID);
	}

	public void aimAt(int id, Point2D.Double aimPosition) {
		if (out == null) return;
		out.println("AIM " + id + " " + aimPosition.x + " " + aimPosition.y);
	}

	public void createBlock(int tankID) {
		if (out == null) return;
		out.println("BLOCK " + tankID);
	}
}