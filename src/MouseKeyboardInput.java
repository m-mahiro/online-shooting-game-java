import java.awt.event.*;

public class MouseKeyboardInput implements InputHandler, KeyListener, MouseMotionListener, MouseListener {

	// キーが押されているかどうかのフラグ
	public boolean up, down, left, right;
	public boolean mousePressed;
	// マウスの現在位置
	public int mouseX, mouseY;


	// ============================= InputHandlerの実装 =============================

	@Override
	public double[] getMoveVector() {

		// 1. キー入力から移動方向を決める
		double x = 0;
		double y = 0;

		if (this.up) y = -1;
		if (this.down) y = 1;
		if (this.left) x = -1;
		if (this.right) x = 1;

		return new double[]{x, y};
	}

	@Override
	public double[] getGunVector() {
		return new double[]{this.mouseX, this.mouseY};
	}

	@Override
	public boolean gunButtonPressed() {
		boolean pressed = this.mousePressed;
		this.mousePressed = false;
		return pressed;
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
		this.mousePressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.mousePressed = false;
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

}