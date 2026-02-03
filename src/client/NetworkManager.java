package client;

import stage.*;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ネットワーク通信を管理するクラス。
 * サーバーとの通信を行い、ゲーム状態を同期する。
 */
public class NetworkManager extends Thread {

	private GameEngine gameEngine;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;

	private int networkClientID;
	private int myTankID;
	private int playerCount;
	private Runnable onReady;

	/**
	 * ネットワークマネージャーを初期化し、サーバーに接続する。
	 * クライアントIDを受信し、プレイヤー名を送信する。
	 *
	 * @param onReady PLAYER_COUNTが揃ったときに呼ばれるコールバック関数
	 */
	public NetworkManager(Runnable onReady) {
		this.onReady = onReady;
		try {
			socket = new Socket("localhost", 10000);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			System.out.println("サーバーに接続しました。");

			// 最初の行は "ID is client number" 形式で来る想定
			String initMsg = in.readLine();
			if (initMsg != null) {
				String[] tokens = initMsg.split(" ");

				this.networkClientID = Integer.parseInt(tokens[0]);
				this.playerCount = 10;
				this.myTankID = networkClientID % this.playerCount;


				// 名前送信（サーバーが期待しているので送る）
				out.println("Player" + myTankID);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ゲームエンジンのインスタンスを設定する。
	 * ネットワークメッセージに基づいてゲーム状態を更新するために使用される。
	 */
	public void setGameEngine(GameEngine gameEngine) {
		this.gameEngine = gameEngine;
	}

	/**
	 * ゲームに参加しているプレイヤー数を取得する。
	 *
	 * @return プレイヤー数
	 */
	public int getPlayerCount() {
		return playerCount;
	}


	/**
	 * 自分のタンクIDを取得する。
	 *
	 * @return タンクID
	 */
	public int getMyTankID() {
		return this.myTankID;
	}

	/**
	 * ネットワーククライアントIDを取得する。
	 *
	 * @return ネットワーククライアントID
	 */
	public int getNetworkClientID() {
		return this.networkClientID;
	}

	/**
	 * {@inheritDoc}
	 * サーバーからのメッセージを継続的に受信し、解釈する。
	 * 接続が切断されるまでループを続ける。
	 */
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

	/**
	 * サーバーから受信したメッセージを解釈し、対応する処理を実行する。
	 *
	 * @param msg 受信したメッセージ文字列
	 */
	private void parseMessage(String msg) {

		String[] tokens = msg.split(" ");
		String cmd = tokens[0];

		if (cmd.equals("PLAYER_COUNT")) {
			playerCount = Integer.parseInt(tokens[1]);
			if (onReady != null) {
				onReady.run();
			}
			return;
		}

		if (gameEngine == null) return;
		GameStage stage = gameEngine.getStage();
		int tankObjectID = Integer.parseInt(tokens[1]);
		if (gameEngine.getMyTankID() == tankObjectID) return;
		Tank tank = (Tank) gameEngine.getStage().getGameObject(tankObjectID);

		try {
			switch (cmd) {
				case "LOCATE": {
					double x = Double.parseDouble(tokens[2]);
					double y = Double.parseDouble(tokens[3]);
					stage.getGameObject(tankObjectID).setPosition(new Point2D.Double(x, y));
					break;
				}
				case "BULLET": {
					Bullet bullet = tank.shootBullet();
					stage.addGameObject(bullet);
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
					stage.addGameObject(block);
					break;
				}
			}
		} catch (NumberFormatException e) {
			System.err.println("パースエラー: " + msg);
		}
	}

	/**
	 * タンクの位置をサーバーに送信する。
	 *
	 * @param id タンクのID
	 * @param position タンクの位置座標
	 */
	public void locateTank(int id, Point2D.Double position) {
		if (out == null) return;
		out.println("LOCATE " + id + " " + position.x + " " + position.y);
	}

	/**
	 * 弾丸発射をサーバーに通知する。
	 *
	 * @param tankID 発射するタンクのID
	 */
	public void shootGun(int tankID) {
		if (out == null) return;
		out.println("BULLET " + tankID);
	}

	/**
	 * エネルギーチャージ開始をサーバーに通知する。
	 *
	 * @param tankID チャージを開始するタンクのID
	 */
	public void startCharge(int tankID) {
		if (out == null) return;
		out.println("START_CHARGE " + tankID);
	}

	/**
	 * エネルギーチャージ完了をサーバーに通知する。
	 *
	 * @param tankID チャージを完了するタンクのID
	 */
	public void finishCharge(int tankID) {
		if (out == null) return;
		out.println("FINISH_CHARGE " + tankID);
	}

	/**
	 * タンクの照準位置をサーバーに送信する。
	 *
	 * @param id タンクのID
	 * @param aimPosition 照準の位置座標
	 */
	public void aimAt(int id, Point2D.Double aimPosition) {
		if (out == null) return;
		out.println("AIM " + id + " " + aimPosition.x + " " + aimPosition.y);
	}

	/**
	 * ブロック生成をサーバーに通知する。
	 *
	 * @param tankID ブロックを生成するタンクのID
	 */
	public void createBlock(int tankID) {
		if (out == null) return;
		out.println("BLOCK " + tankID);
	}
}