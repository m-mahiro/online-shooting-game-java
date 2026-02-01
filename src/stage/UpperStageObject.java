package stage;

import java.awt.Graphics2D;

// このインターフェースを実装するオブジェクトは、
// GameStageの上層に（UIよりは下層に）描画される
public interface UpperStageObject {
	void draw(Graphics2D g);
	void update();
	boolean isExpired();
}