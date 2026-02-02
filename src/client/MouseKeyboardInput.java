package client;

import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import client.GameEngine;

/**
 * マウスとキーボードによる入力を処理するクラス。
 * InputHandlerインターフェースを実装し、各種リスナーを通じて入力を検知する。
 */
public class MouseKeyboardInput
		implements
		InputHandler,
		KeyListener,
		MouseMotionListener,
		MouseListener,
		MouseWheelListener {


	/**
	 * マウスとキーボードの入力ハンドラを初期化する。
	 * 各種リスナーをゲームパネルに登録する。
	 *
	 * @param gamePanel 入力を受け取るゲームパネル
	 */
	public MouseKeyboardInput(GamePanel gamePanel) {
		gamePanel.addKeyListener(this);
		gamePanel.addMouseMotionListener(this);
		gamePanel.addMouseListener(this);
		gamePanel.addMouseWheelListener(this);
		gamePanel.setFocusable(true);
		gamePanel.requestFocusInWindow();
		gamePanel.setFocusable(true);
		gamePanel.requestFocusInWindow();
	}

	// 定数
	private final int CHARGE_START_COUNT = (int) (GameEngine.FPS * 0.5);

	// キーが押されているかどうかのフラグ
	private boolean up, down, left, right;
	private boolean leftClicked, rightClicked;
	// マウスの現在位置
	private int mouseX, mouseY;

	private int scrollAmount = 0;

	private boolean canShootBullet = true;

	private int continuousLeftPressedCount = 0;
	private ChargeState chargeState = ChargeState.STAND_BY;

	private enum ChargeState {
		STAND_BY, POST_CHARGE_START, POST_CHARGE_FINISH, CHARGING
	}

	// ============================= InputHandlerの実装 =============================

	/**
	 * {@inheritDoc}
	 * WASDキーの入力から移動ベクトルを計算する。
	 */
	@Override
	public Point2D.Double getMoveVector(AffineTransform canvasTransform) {

		// 1. キー入力から移動方向を決める
		double x = 0;
		double y = 0;

		if (this.up) y = -1;
		if (this.down) y = 1;
		if (this.left) x = -1;
		if (this.right) x = 1;

		return new Point2D.Double(x, y);
	}

	/**
	 * {@inheritDoc}
	 * マウスカーソルの位置をゲーム世界座標に変換する。
	 */
	@Override
	public Point2D.Double getAimedCoordinate(AffineTransform canvasTransform) {
		Point2D.Double sourcePoint = new Point2D.Double(this.mouseX, this.mouseY);
		Point2D.Double destinationPoint = new Point2D.Double();
		try {
			canvasTransform.createInverse().transform(sourcePoint, destinationPoint);
		} catch (NoninvertibleTransformException e) {
			// なにもしない
		}

		double x = destinationPoint.x;
		double y = destinationPoint.y;
		return new Point2D.Double(x, y);
	}

	/**
	 * {@inheritDoc}
	 * 左クリックの状態をチェックし、発射可能であれば弾丸を発射する。
	 */
	@Override
	public boolean shootBullet() {
		boolean pressed = this.leftClicked;
		boolean result = canShootBullet && pressed;
		canShootBullet = false;
		return result;
	}

	/**
	 * {@inheritDoc}
	 * チャージ状態がPOST_CHARGE_STARTの場合にtrueを返す。
	 */
	@Override
	public boolean startEnergyCharge() {
		boolean temp = chargeState == ChargeState.POST_CHARGE_START;
		if (temp) chargeState = ChargeState.CHARGING;
		return temp;
	}

	/**
	 * {@inheritDoc}
	 * チャージ状態がPOST_CHARGE_FINISHの場合にtrueを返す。
	 */
	@Override
	public boolean finishEnergyCharge() {
		boolean temp = chargeState == ChargeState.POST_CHARGE_FINISH;
		if (temp) chargeState = ChargeState.STAND_BY;
		return temp;
	}

	/**
	 * {@inheritDoc}
	 * 右クリックの状態をチェックし、ブロックを生成する。
	 */
	@Override
	public boolean createBlock() {
		boolean pressed = this.rightClicked;
		this.rightClicked = false;
		return pressed;
	}

	/**
	 * {@inheritDoc}
	 * マウスホイールのスクロール量を返す。
	 */
	@Override
	public int getZoomAmount() {
		int zoomAmount = this.scrollAmount;
		this.scrollAmount = 0;
		return zoomAmount;
	}

	/**
	 * {@inheritDoc}
	 * 左クリックの継続時間を計測し、チャージ状態を更新する。
	 */
	@Override
	public void onFrameUpdate() {
		if (leftClicked) {
			continuousLeftPressedCount++;
		} else {
			continuousLeftPressedCount = 0;
		}

		switch (this.chargeState) {
			case STAND_BY:
				if (continuousLeftPressedCount > CHARGE_START_COUNT) {
					chargeState = ChargeState.POST_CHARGE_START;
				}
				break;
			case CHARGING:
				if (continuousLeftPressedCount <= CHARGE_START_COUNT) {
					chargeState = ChargeState.POST_CHARGE_FINISH;
				}
				break;
		}
	}


	// ============================= KeyListenerの実装 =============================

	/**
	 * {@inheritDoc}
	 * 使用しない。
	 */
	@Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * {@inheritDoc}
	 * WASDキーが押された時、対応する方向フラグをONにする。
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();

		// 押されたキーに対応するフラグをONにする
		if (code == KeyEvent.VK_W) up = true;
		if (code == KeyEvent.VK_A) left = true;
		if (code == KeyEvent.VK_S) down = true;
		if (code == KeyEvent.VK_D) right = true;
	}

	/**
	 * {@inheritDoc}
	 * WASDキーが離された時、対応する方向フラグをOFFにする。
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();

		// キーが離されたらフラグをOFFにする
		if (code == KeyEvent.VK_W) up = false;
		if (code == KeyEvent.VK_A) left = false;
		if (code == KeyEvent.VK_S) down = false;
		if (code == KeyEvent.VK_D) right = false;
	}

	// ============================= MouseMotionListenerの実装 =============================

	/**
	 * {@inheritDoc}
	 * マウスがドラッグされた時、マウス座標を更新する。
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		// ドラッグ中も位置を更新する
		mouseX = e.getX();
		mouseY = e.getY();
	}

	/**
	 * {@inheritDoc}
	 * マウスが移動した時、マウス座標を更新する。
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		// マウスが動いたら位置を更新する
		mouseX = e.getX();
		mouseY = e.getY();
	}


	// ============================= MouseListenerの実装 =============================

	/**
	 * {@inheritDoc}
	 * マウスボタンが押された時、対応するフラグをONにする。
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		int button = e.getButton();
		switch (button) {
			case MouseEvent.BUTTON1:
				this.leftClicked = true;
				canShootBullet = true;
				break;
			case MouseEvent.BUTTON3:
				this.rightClicked = true;
				break;
		}
	}

	/**
	 * {@inheritDoc}
	 * マウスボタンが離された時、クリック状態とチャージ状態をリセットする。
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		this.leftClicked = false;
		canShootBullet = true;
		continuousLeftPressedCount = 0;
	}

	/**
	 * {@inheritDoc}
	 * 使用しない。
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * {@inheritDoc}
	 * 使用しない。
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * {@inheritDoc}
	 * 使用しない。
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	// ============================= MouseWheelListenerの実装 =============================

	/**
	 * {@inheritDoc}
	 * マウスホイールが動いた時、スクロール量を累積する。
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		this.scrollAmount += e.getWheelRotation();
	}
}