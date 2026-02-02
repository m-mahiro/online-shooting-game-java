package stage;

import java.awt.Graphics2D;

public class Marker implements ScreenObject {
    private Tank target;

    /**
     * 新しいマーカーオブジェクトを生成します。
     * @param target マーカーが追跡する対象の戦車
     */
    public Marker(Tank target) {
        this.target = target;
    }

    /**
     * マーカーを描画します。
     * @param g 描画に使用するGraphics2Dオブジェクト
     */
    @Override
    public void draw(Graphics2D g) {
        // TODO: Implement drawing logic for the marker
    }

    /**
     * マーカーの状態を更新します。
     * 現在は特別なアニメーションロジックがないため、空の実装です。
     */
    @Override
    public void update() {
        // TODO: Implement update logic if marker needs to be animated
    }

    /**
     * マーカーが関連付けられている戦車が死亡している場合、マーカーも期限切れと判断されます。
     * @return ターゲットの戦車が死亡している場合はtrue、それ以外はfalse
     */
    @Override
    public boolean isExpired() {
        return target.isDead();
    }
}