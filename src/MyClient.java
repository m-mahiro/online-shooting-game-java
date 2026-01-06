import java.net.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;

public class MyClient extends JFrame implements MouseMotionListener, KeyListener {
	private Container c;
	ImageIcon blackIcon = new ImageIcon("assets/Black.jpg");
	ImageIcon whiteIcon = new ImageIcon("assets/White.jpg");
	ImageIcon lightBlackIcon = new ImageIcon("assets/LightBlack.jpg");
	ImageIcon lightWhiteIcon = new ImageIcon("assets/LightWhite.jpg");
	ImageIcon bullet = new ImageIcon("assets/GreenFrame.jpg");
	PrintWriter out;//出力用のライター
	private int myClientNumber;
	public volatile ImageIcon tern;
	private JLabel label;
	private JLabel[] tanks;
	int myTankId;
	int mouseX, mouseY;

	public MyClient() {
		//名前の入力ダイアログを開く
//		String myName = JOptionPane.showInputDialog(null, "名前を入力してください", "名前の入力", JOptionPane.QUESTION_MESSAGE);
		String myName = "test";
		if (myName.equals("")) {
			myName = "No name";//名前がないときは，"No name"とする
		}

		//ウィンドウを作成する
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じるときに，正しく閉じるように設定する
		setTitle("MyClient");//ウィンドウのタイトルを設定する
		setSize(400, 400);//ウィンドウのサイズを設定する
		c = getContentPane();//フレームのペインを取得する

		tanks = new JLabel[4];

		//アイコンの設定
		c.setLayout(null);//自動レイアウトの設定を行わない
		tanks[0] = new JLabel(blackIcon);
		tanks[1] = new JLabel(whiteIcon);
		tanks[2] = new JLabel(blackIcon);
		tanks[3] = new JLabel(whiteIcon);
		tanks[0].setBounds(10, 100 + 0, 50, 50);
		tanks[1].setBounds(10, 100 + 50, 50, 50);
		tanks[2].setBounds(60, 100 + 0, 50, 50);
		tanks[3].setBounds(60, 100 + 50, 50, 50);
		this.addMouseMotionListener(this);
		tanks[0].setText("0");
		tanks[1].setText("1");
		tanks[2].setText("2");
		tanks[3].setText("3");
		this.addKeyListener(this);
		setFocusable(true);
		setVisible(true);

		for (JLabel tank : tanks) {
			c.add(tank);
		}

		JLabel myIconInfoText = new JLabel("あなた: ");
		label = new JLabel();

		myIconInfoText.setBounds(10, 5, 90, 50);
		label.setBounds(10, 70, 380, 10);

		c.add(myIconInfoText);
		c.add(label);

		//サーバに接続する
		Socket socket = null;
		try {
			//"localhost"は，自分内部への接続．localhostを接続先のIP Address（"133.42.155.201"形式）に設定すると他のPCのサーバと通信できる
			//10000はポート番号．IP Addressで接続するPCを決めて，ポート番号でそのPC上動作するプログラムを特定する
			socket = new Socket("localhost", 10000);
		} catch (UnknownHostException e) {
			System.err.println("ホストの IP アドレスが判定できません: " + e);
		} catch (IOException e) {
			System.err.println("エラーが発生しました: " + e);
		}

		MesgRecvThread mrt = new MesgRecvThread(socket, myName);//受信用のスレッドを作成する
		mrt.start();//スレッドを動かす（Runが動く）
	}

	@Override
	public void keyTyped(KeyEvent e) {
		JLabel myTank = tanks[myTankId];

		int x = myTank.getX();
		int y = myTank.getY();

		switch (e.getKeyChar()) {
			case 'p': {
				BulletThread bulletFlying = new BulletThread(x + 50, y + 50, mouseX - x, mouseY - y, true);
				System.out.println("発射！");
				break;
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		char key = e.getKeyChar();
		System.out.println(key);
		JLabel myTank = tanks[myTankId];
		int x = myTank.getX();
		int y = myTank.getY();
		switch (key) {
			case 'w': y = y - 3; break;
			case 's': y = y + 3; break;
			case 'a': x = x - 3; break;
			case 'd': x = x + 3; break;
			default: {
				return;
			}
		}
		myTank.setLocation(x, y);
		out.println("MOVE " + MyClient.this.myTankId + " " + x + " " + y);

	}

	private class BulletThread extends Thread{
		private JLabel bullet;
		private int x, y, dx, dy;
		BulletThread(int x, int y, int dx, int dy, boolean isShooter) {
			this.x = x;
			this.y = y;
			this.dx = dx;
			this.dy = dy;
			bullet = new JLabel(MyClient.this.bullet);
			bullet.setLocation(x, y);
			bullet.setSize(10, 10);
			c.add(bullet);
			if (isShooter) {
				MyClient.this.out.println("BULLET " + MyClient.this.myTankId + " " +  x + " " + y + " " + dx + " " + dy);
			}
			this.start();
		}

		@Override
		public void run() {
			for (int i = 0; i < 100; i++) {
				x += dx;
				y += dy;
				bullet.setLocation(x, y);
				try {
					sleep(10);
				} catch (Exception e) {
					continue;
				}
			}
			SwingUtilities.invokeLater(() -> {
				c.remove(bullet);
				c.repaint();
			});
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		this.mouseX = e.getX();
		this.mouseY = e.getY();
		System.out.println(this.mouseX + " " + this.mouseY);
	}

	//メッセージ受信のためのスレッド
	public class MesgRecvThread extends Thread {

		Socket socket;
		String myName;

		public MesgRecvThread(Socket s, String n) {
			socket = s;
			myName = n;
		}

		//通信状況を監視し，受信データによって動作する
		public void run() {
			try {
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);

				MyClient.this.myClientNumber = Integer.parseInt(br.readLine().split(" ")[0]);
				System.out.println("MyClientNumber: " + myClientNumber); // Hello, Clientを受け取る

				myTankId = MyClient.this.myClientNumber % 4;
				tanks[myTankId].setIcon(myTankId % 2 == 0 ? lightBlackIcon : lightWhiteIcon);

				out = new PrintWriter(socket.getOutputStream(), true);

				JLabel myIconInfo = new JLabel(MyClient.this.myClientNumber % 2 == 0 ? "黒" : "白");
				myIconInfo.setBounds(110, 5, 50, 50);
				c.add(myIconInfo);


				while (true) {
					String inputLine = br.readLine();//データを一行分だけ読み込んでみる
					if (inputLine != null) {//読み込んだときにデータが読み込まれたかどうかをチェックする
//						System.out.println("Receive: " + inputLine);//デバッグ（動作確認用）にコンソールに出力する
						String[] inputTokens = inputLine.split(" ");    //入力データを解析するために、スペースで切り分ける
						String cmd = inputTokens[0];//コマンドの取り出し．１つ目の要素を取り出す

						if (cmd.equals("MOVE")) {//cmdの文字と"MOVE"が同じか調べる．同じ時にtrueとなる
							//MOVEの時の処理(コマの移動の処理)
							int tankId = Integer.parseInt(inputTokens[1]);//ボタンの名前を数値に変換する
							int x = Integer.parseInt(inputTokens[2]);//数値に変換する
							int y = Integer.parseInt(inputTokens[3]);//数値に変換する
							tanks[tankId].setLocation(x, y);//指定のボタンを位置をx,yに設定する
						} else if (cmd.equals("BULLET")) {
							int tankId = Integer.parseInt(inputTokens[1]);//ボタンの名前を数値に変換する
							if (tankId == MyClient.this.myTankId) {
								continue;
							}
							int x = Integer.parseInt(inputTokens[2]);//数値に変換する
							int y = Integer.parseInt(inputTokens[3]);//数値に変換する
							int dx = Integer.parseInt(inputTokens[4]);
							int dy = Integer.parseInt(inputTokens[5]);
							BulletThread bulletFlying = new BulletThread(x, y, dx, dy, false);
						}

					} else {
						break;
					}
				}
				socket.close();
			} catch (
					IOException e) {
				System.err.println("エラーが発生しました: " + e);
			}
		}


	}

	public static void main(String[] args) {
		MyClient net = new MyClient();
		net.setVisible(true);
	}

}