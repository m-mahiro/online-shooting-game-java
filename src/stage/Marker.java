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

    @Override
    public void update() {
        animationCounter++;
    }

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

    @Override
    public boolean isExpired() {
        return tank.isExpired();
    }

    @Override
    public Point2D.Double getPosition() {
        return (Point2D.Double) this.position.clone();
    }

    @Override
    public void setPosition(Point2D.Double position) {
        this.position.setLocation(position);
    }
}