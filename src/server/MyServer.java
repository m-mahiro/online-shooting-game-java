package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 各クライアント接続を処理するスレッドクラス。
 * クライアントからのメッセージを受信し、全クライアントにブロードキャストする。
 */
class ClientProcThread extends Thread {
	private int number;//自分の番号
	private Socket incoming;
	private InputStreamReader myIsr;
	private BufferedReader myIn;
	private PrintWriter myOut;
	private String myName;//接続者の名前
	private String myColor;

	/**
	 * ClientProcThreadのコンストラクタ。
	 *
	 * @param n クライアント番号
	 * @param i クライアントとの接続ソケット
	 * @param isr 入力ストリームリーダー
	 * @param in バッファ付きリーダー
	 * @param out 出力ライター
	 */
	public ClientProcThread(int n, Socket i, InputStreamReader isr, BufferedReader in, PrintWriter out) {
		number = n;
		incoming = i;
		myIsr = isr;
		myIn = in;
		myOut = out;
	}

	/**
	 * クライアントとの通信を処理するメインループ。
	 * クライアント番号を送信し、名前を受信し、色を割り当てる。
	 * その後、クライアントからのメッセージを継続的に受信し、全クライアントにブロードキャストする。
	 */
	public void run() {
		try {
			myOut.println(number + " is client number");//初回だけ呼ばれる

			myName = myIn.readLine();//初めて接続したときの一行目は名前
			if (MyServer.member % 2 == 0) {
				myOut.println("BLACK");
			} else {
				myOut.println("WHITE");
			}



			while (true) {//無限ループで，ソケットへの入力を監視する
				String str = myIn.readLine();

				System.out.println("Received from client No."+number+"("+myName+"), Messages: "+str);
				if (str != null) {//このソケット（バッファ）に入力があるかをチェック
					if (str.toUpperCase().equals("BYE")) {
						myOut.println("Good bye!");
						break;
					}
					MyServer.SendAll(str, myName);//サーバに来たメッセージは接続しているクライアント全員に配る
				} else {
					break;
				}
			}
		} catch (Exception e) {
			//ここにプログラムが到達するときは，接続が切れたとき
			System.out.println("Disconnect from client No."+number+"("+myName+")");
			MyServer.SetFlag(number, false);//接続が切れたのでフラグを下げる
		}
	}
}

/**
 * マルチクライアント対応のTCPサーバークラス。
 * 複数のクライアントからの接続を受け付け、メッセージをブロードキャストする。
 * ポート10000でクライアント接続を待ち受け、各接続に対して個別のスレッドを作成する。
 */
class MyServer {

	private static int maxConnection=100;//最大接続数
	private static Socket[] incoming;//受付用のソケット
	private static boolean[] flag;//接続中かどうかのフラグ
	private static InputStreamReader[] isr;//入力ストリーム用の配列
	private static BufferedReader[] in;//バッファリングをによりテキスト読み込み用の配列
	private static PrintWriter[] out;//出力ストリーム用の配列
	private static ClientProcThread[] myClientProcThread;//スレッド用の配列
	public static int member;//接続しているメンバーの数

	/**
	 * 接続中の全クライアントにメッセージを送信する。
	 * 接続フラグが立っているクライアントのみに送信する。
	 *
	 * @param str 送信するメッセージ
	 * @param myName メッセージ送信者の名前
	 */
	public static void SendAll(String str, String myName){
		//送られた来たメッセージを接続している全員に配る
		for(int i=0;i<=member;i++){
			if(flag[i] == true){
				out[i].println(str);
				out[i].flush();//バッファをはき出す＝＞バッファにある全てのデータをすぐに送信する
				System.out.println("Send messages to client No."+i);
			}
		}
	}

	/**
	 * 指定されたクライアント番号の接続フラグを設定する。
	 * クライアントの接続状態を管理するために使用される。
	 *
	 * @param n クライアント番号
	 * @param value 設定するフラグの値（true: 接続中, false: 切断）
	 */
	public static void SetFlag(int n, boolean value){
		flag[n] = value;
	}

	/**
	 * サーバーのメインエントリーポイント。
	 * ポート10000でサーバーソケットを作成し、クライアント接続を待ち受ける。
	 * 各クライアント接続に対して個別のスレッドを作成して処理を行う。
	 *
	 * @param args コマンドライン引数（未使用）
	 */
	public static void main(String[] args) {
		//必要な配列を確保する
		incoming = new Socket[maxConnection];
		flag = new boolean[maxConnection];
		isr = new InputStreamReader[maxConnection];
		in = new BufferedReader[maxConnection];
		out = new PrintWriter[maxConnection];
		myClientProcThread = new ClientProcThread[maxConnection];

		int n = 0;
		member = 0;//誰も接続していないのでメンバー数は０

		try {
			System.out.println("The server has launched!");
			ServerSocket server = new ServerSocket(10000);//10000番ポートを利用する
			while (true) {
				incoming[n] = server.accept();
				flag[n] = true;
				System.out.println("Accept client No." + n);
				//必要な入出力ストリームを作成する
				isr[n] = new InputStreamReader(incoming[n].getInputStream());
				in[n] = new BufferedReader(isr[n]);
				out[n] = new PrintWriter(incoming[n].getOutputStream(), true);

				myClientProcThread[n] = new ClientProcThread(n, incoming[n], isr[n], in[n], out[n]);//必要なパラメータを渡しスレッドを作成
				myClientProcThread[n] .start();//スレッドを開始する
				member = n;//メンバーの数を更新する
				n++;
			}
		} catch (Exception e) {
			System.err.println("ソケット作成時にエラーが発生しました: " + e);
		}
	}
}