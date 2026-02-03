package client.ui;

import java.awt.*;

/**
 * ゲームUIの基本インターフェース。
 * すべてのゲームUI実装が提供すべきメソッドを定義する。
 */
public interface GameUI {

    /**
     * UIの状態を更新する。
     * フレームごとに呼び出される。
     */
    void update();

    /**
     * UIを画面に描画する。
     *
     * @param graphics2D 描画に使用するGraphics2Dオブジェクト
     * @param windowWidth ウィンドウの幅
     * @param windowHeight ウィンドウの高さ
     */
    void draw(Graphics2D graphics2D, int windowWidth, int windowHeight);

}
