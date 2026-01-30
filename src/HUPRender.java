import java.awt.*;
import java.util.HashSet;

public class HUPRender {

	private final HashSet<UIContent> contents = new HashSet<>();

	public void update() {

	}

	public void draw(Graphics2D graphics, int windowWidth, int windowHeight) {
		synchronized (this.contents) {
			for (UIContent content : this.contents) {
				content.draw(graphics, windowWidth, windowHeight);
			}
		}
	}
}
