package client.ui;

import java.awt.*;

/**
 * UI要素の共通インターフェース。
 * ゲーム画面に表示されるすべてのUI要素が実装する。
 */
public interface UIContent {

	/**
	 * UI要素の状態を更新する。
	 */
	void update();

	/**
	 * UI要素を画面に描画する。
	 *
	 * @param graphics 描画に使用するGraphics2Dオブジェクト
	 * @param windowWidth ウィンドウの幅
	 * @param windowHeight ウィンドウの高さ
	 */
	void draw(Graphics2D graphics, int windowWidth, int windowHeight);

	/**
	 * UI要素が有効期限切れかどうかを判定する。
	 *
	 * @return 有効期限切れの場合true、そうでない場合false
	 */
	boolean isExpired();
}
