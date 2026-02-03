package stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Marker implements ScreenObject {

    private final Tank tank;
    private final Point2D.Double position;

    private BufferedImage image;

    private int animationCounter = 0;

    /**
     * Markerオブジェクトを初期化します。
     * 戦車のチームに応じて適切なマーカー画像を読み込みます。
     */
    public Marker(Tank tank) {
        this.tank = tank;
        this.position = tank.getPosition();
        try {
            switch (tank.getTeam()) {
                case BLUE:
                    this.image = ImageIO.read(Objects.requireNonNull(Marker.class.getResource("../client/assets/marker_blue.png")));
                    break;
                case RED:
                    this.image = ImageIO.read(Objects.requireNonNull(Marker.class.getResource("../client/assets/marker_red.png")));
                    break;
                default:
                    throw new RuntimeException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     * アニメーションカウンターをインクリメントします。
     */
    @Override
    public void update() {
        animationCounter++;
    }

    /**
     * {@inheritDoc}
     * 戦車の上にマーカーを描画します。マーカーは水平方向にsin波でアニメーションします。
     * 戦車が死亡している場合は描画をスキップします。
     */
    @Override
    public void draw(Graphics2D graphics) {
        if (this.tank.isDead()) return;
        Point2D.Double tankPosition = this.tank.getPosition();
        AffineTransform trans = new AffineTransform();
        trans.translate(tankPosition.x, tankPosition.y);
        trans.scale(Math.sin(animationCounter / 10.0), 1);
        trans.translate(-this.image.getWidth() / 2.0, -this.image.getHeight() - tank.getHeight() / 2.0);
        graphics.drawImage(this.image, trans, null);
    }

    /**
     * {@inheritDoc}
     * 関連付けられた戦車が期限切れの場合にこのマーカーも期限切れになります。
     */
    @Override
    public boolean isExpired() {
        return tank.isExpired();
    }

    /**
     * {@inheritDoc}
     * 現在の位置のクローンを返します。
     */
    @Override
    public Point2D.Double getPosition() {
        return (Point2D.Double) this.position.clone();
    }

    /**
     * {@inheritDoc}
     * 指定された位置に内部の位置を更新します。
     */
    @Override
    public void setPosition(Point2D.Double position) {
        this.position.setLocation(position);
    }
}