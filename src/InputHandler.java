import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class InputHandler implements KeyListener, MouseMotionListener {

	// キーが押されているかどうかのフラグ
	public boolean up, down, left, right;

	// マウスの現在位置
	public int mouseX, mouseY;

	@Override
	public void keyTyped(KeyEvent e) {} // 使わない

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
}