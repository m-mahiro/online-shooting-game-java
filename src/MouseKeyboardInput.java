import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class MouseKeyboardInput
		implements
			InputHandler,
			KeyListener,
			MouseMotionListener,
			MouseListener,
			MouseWheelListener {


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
	private final int CHARGE_START_COUNT = (int) (GamePanel.FPS * 0.5);

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
		STAND_BY, IS_CHARGING
	}

	// ============================= InputHandlerの実装 =============================

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

	@Override
	public boolean shootBullet() {
		boolean pressed = this.leftClicked;
		boolean result = canShootBullet && pressed;
		canShootBullet = false;
		return result;
	}

	@Override
	public boolean chargeButtonPressed() {
		switch (chargeState) {
			case STAND_BY:
				if (continuousLeftPressedCount > CHARGE_START_COUNT) {
					this.chargeState = ChargeState.IS_CHARGING;
					return true;
				} else {
					return false;
				}
			case IS_CHARGING:
				boolean result = !leftClicked;
				chargeState = ChargeState.STAND_BY;
				return result;
			default:
				throw new IllegalStateException("Unexpected value: " + chargeState);
		}
	}

	@Override
	public boolean createBlock() {
		boolean pressed = this.rightClicked;
		this.rightClicked = false;
		return pressed;
	}

	@Override
	public int getZoomAmount() {
		int zoomAmount = this.scrollAmount;
		this.scrollAmount = 0;
		return zoomAmount;
	}

	//　hack: フレームが更新されてから、他のどのInputHandlerメソッドよりも先に呼ばれる必要がある。
	@Override
	public void onFrameUpdate() {
		if (leftClicked) {
			continuousLeftPressedCount++;
		} else {
			continuousLeftPressedCount = 0;
		}
	}


	// ============================= KeyListenerの実装 =============================

	@Override
	public void keyTyped(KeyEvent e) {
	} // 使わない

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();

		// 押されたキーに対応するフラグをONにする
		if (code == KeyEvent.VK_W) up = true;
		if (code == KeyEvent.VK_A) left = true;
		if (code == KeyEvent.VK_S) down = true;
		if (code == KeyEvent.VK_D) right = true;
	}

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

	@Override
	public void mouseDragged(MouseEvent e) {
		// ドラッグ中も位置を更新する
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// マウスが動いたら位置を更新する
		mouseX = e.getX();
		mouseY = e.getY();
	}


	// ============================= MouseListenerの実装 =============================

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

	@Override
	public void mouseReleased(MouseEvent e) {
		this.leftClicked = false;
		canShootBullet = true;
		continuousLeftPressedCount = 0;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	// ============================= MouseWheelListenerの実装 =============================

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		this.scrollAmount += e.getWheelRotation();
	}
}