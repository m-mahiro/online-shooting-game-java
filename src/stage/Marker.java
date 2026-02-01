package stage;

import java.awt.Graphics2D;

public class Marker implements UpperStageObject {
    private Tank target;

    public Marker(Tank target) {
        this.target = target;
    }

    @Override
    public void draw(Graphics2D g) {
        // TODO: Implement drawing logic for the marker
    }

    @Override
    public void update() {
        // TODO: Implement update logic if marker needs to be animated
    }

    @Override
    public boolean isExpired() {
        return target.isDead();
    }
}