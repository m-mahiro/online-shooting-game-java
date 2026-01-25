import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class Block implements GameObject {

	private double velocity = Tank.velocity; // Tankと同じ

	private final Point2D.Double translate = new Point2D.Double(0, 0);
	private double hp = 40.0;

	// 画像
	private BufferedImage blockImage;
	private static BufferedImage normalBlockImage, transparentBlockImage;
	static {
		try {
			normalBlockImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/block_normal.png")));
			transparentBlockImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/block_trans.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Block(double x, double y) {
		this.blockImage = Block.normalBlockImage;
		this.translate.setLocation(x, y);
	}

	// ============================= Blockクラス独自のメソッド =============================
	public void move(Point2D.Double vector) {
		Point2D.Double p = new Point2D.Double(vector.x, vector.y);
		p = Util.normalize(p);
		p = Util.multiple(p, this.velocity);
		p = Util.addition(this.translate, p);
		this.translate.setLocation(p);
	}

	// ============================= GameObjectインタフェースのメソッド =============================

	@Override
	public void update() {

	}

	@Override
	public void draw(Graphics2D graphics) {
		AffineTransform trans = new AffineTransform();
		trans.translate(this.translate.x, this.translate.y);
		trans.translate(-getWidth() / 2.0, -getHeight() / 2.0);
		graphics.drawImage(getBlockImage(), trans, null);
	}

	@Override
	public void onCollision(GameObject other) {
		if (hp <= 0) return;
		// ============================= オブジェクトがのめりこまないように、適切な方向に逃げる =============================

		// 相対的な位置関係を取得 (相手 - 自分)
		Point2D.Double vector = Util.subtract(other.getTranslate(), this.getTranslate());

		// 全く同じ位置だった場合、少しだけずらす
		if (vector.x == 0 && vector.y == 0) {
			Random random = new Random();
			vector.x = random.nextDouble() - 0.5; // 0.0~1.0だと正の方向に偏るので -0.5
			vector.y = random.nextDouble() - 0.5;
		}

		// 相手のサイズを取得（これが抜けていました）
		double otherWidth, otherHeight;
		if (other.getShape() instanceof Rectangle) {
			Rectangle rect = (Rectangle) other.getShape();
			otherWidth = rect.width;
			otherHeight = rect.height;
		} else {
			assert other.getShape() instanceof Circle;
			Circle circle = (Circle) other.getShape();
			otherWidth = otherHeight = circle.radius * 2; // 半径x2 = 直径(幅)
		}

		// 衝突判定に使う「合体した矩形」のサイズ
		double totalWidth = this.getWidth() + otherWidth;
		double totalHeight = this.getHeight() + otherHeight;

		// ベクトルの絶対値
		double absX = Math.abs(vector.x);
		double absY = Math.abs(vector.y);

		// 逃げる方向（自分から相手への逆 = 自分自身の移動方向）
		vector = Util.multiple(vector, -1);

		// 対角線判定: (absY / absX) < (totalHeight / totalWidth)
		// 式変形して割り算をなくすと: absY * totalWidth < absX * totalHeight
		// これが成り立つなら「横長」の領域にいるため、横（左右）からの衝突
		if (absY * totalWidth < absX * totalHeight) {
			// 横方向 (左右) からの衝突 -> Y成分を0にして、純粋にX方向に逃げる
			vector.y = 0;
		} else {
			// 縦方向 (上下) からの衝突 -> X成分を0にして、純粋にY方向に逃げる
			vector.x = 0;
		}

		// 移動実行
		move(vector);
	}

	@Override
	public void onHitBy(DangerGameObject other) {
		if (hp > 0) {
			hp -= other.getDamageAbility();
			if (hp <= 0) {
				// HPが0になったので、非物理オブジェクトに変化
				this.blockImage = transparentBlockImage;
			}
		}
	}

	@Override
	public boolean shouldRemove() {
		return hp <= 0;
	}

	@Override
	public boolean isTangible() {
		return hp > 0;
	}

	@Override
	public Shape getShape() {
		return new Rectangle(this.translate, this.getWidth(), this.getHeight());
	}

	@Override
	public Point2D.Double getTranslate() {
		return (Point2D.Double) this.translate.clone();
	}

	@Override
	public void setTranslate(double x, double y) {
		this.translate.setLocation(x, y);
	}

	// ============================= ゲッターセッター =============================
	public int getWidth() {
		return blockImage.getWidth();
	}

	public int getHeight() {
		return blockImage.getHeight();
	}

	private BufferedImage getBlockImage() {
		return isTangible() ? normalBlockImage : transparentBlockImage;
	}
}
