package stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Objects;

import static stage.Team.*;

public class Wall implements GameObject {

	// 特徴
	public static final int WIDTH = 100;
	public static final int HEIGHT = 100;

	// 状態（クライアント間の同期に必要)
	private final Point2D.Double position;

	// 画像リソース
	private static final Image wallImage;

	static {
		try {
			wallImage = ImageIO.read(Objects.requireNonNull(Wall.class.getResource("/client/assets/wall.png"))).getScaledInstance(WIDTH + 1, HEIGHT + 1, Image.SCALE_SMOOTH);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	    /**
	     * 新しい壁オブジェクトを生成します。
	     * @param coordinate 壁の中心座標
	     */
	    public Wall(Point2D.Double coordinate) {		this.position = (Point2D.Double) coordinate.clone();
	}

	// ============================= GameObjectインタフェースのメソッド =============================

	    /**
	     * 壁の状態を更新します。
	     * 壁は静的なオブジェクトであるため、このメソッドは何も処理しません。
	     */
	    @Override
	    public void update() {
	}

	    /**
	     * 壁自身を描画します。
	     * @param graphics 描画に使用するGraphics2Dオブジェクト
	     */
	    @Override
	    public void draw(Graphics2D graphics) {		AffineTransform trans = new AffineTransform();
		trans.translate(this.position.x, this.position.y);
		trans.translate(-WIDTH / 2.0, -HEIGHT / 2.0);
		graphics.drawImage(wallImage, trans, null);
	}

	    /**
	     * 他のGameObjectとの衝突時に呼び出されます。
	     * 壁は衝突によって特別な状態変化を起こさないため、このメソッドは何も処理しません。
	     * @param other 衝突したGameObject
	     */
	    @Override
	    public void onCollision(GameObject other) {
	}

	    /**
	     * プロジェクタイルから被弾した際に呼び出されます。
	     * 壁はダメージを受けないため、このメソッドは何も処理しません。
	     * @param other 衝突したプロジェクタイル
	     */
	    @Override
	    public void onHitBy(Projectile other) {
	}

	    /**
	     * オブジェクトがステージから削除されるべきかを判定します。
	     * 壁はゲーム終了までステージに残り続けるため、常にfalseを返します。
	     * @return 常にfalse
	     */
	    @Override
	    public boolean isExpired() {		return false;
	}

	    /**
	     * オブジェクトが剛体として扱われるべきか、つまり衝突判定の対象となるべきかを判定します。
	     * 壁は常に剛体として扱われます。
	     * @return 常にtrue
	     */
	    @Override
	    public boolean hasRigidBody() {		return true;
	}

	    /**
	     * オブジェクトの描画レイヤーを取得します。
	     * 壁は常にTANGIBLE_OBJECTレイヤーとして描画されます。
	     * @return 描画レイヤー
	     */
	    @Override
	    public RenderLayer getRenderLayer() {		return RenderLayer.TANGIBLE_OBJECT;
	}

	    /**
	     * オブジェクトの衝突判定に使用される形状を取得します。
	     * 壁は長方形の形状を持ちます。
	     * @return 壁の形状を表すShapeオブジェクト
	     */
	    @Override
	    public Shape getShape() {		return new Rectangle(this.position, WIDTH, HEIGHT);
	}

	    /**
	     * 壁の中心座標を取得します。
	     * @return 壁の中心座標のPoint2D.Doubleオブジェクト
	     */
	    @Override
	    public Point2D.Double getPosition() {		return (Point2D.Double) this.position.clone();
	}

	    /**
	     * 壁の中心座標を設定します。
	     * @param position 設定する新しい中心座標のPoint2D.Doubleオブジェクト
	     */
	    @Override
	    public void setPosition(Point2D.Double position) {		this.position.setLocation(position);
	}

	    /**
	     * 壁の現在のHPを取得します。
	     * 壁は破壊されないため、常にInteger.MAX_VALUEを返します。
	     * @return 壁の現在のHP (Integer.MAX_VALUE)
	     */
	    @Override
	    public int getHP() {		return Integer.MAX_VALUE;
	}

	    /**
	     * 壁が所属するチームを取得します。
	     * 壁は常にOBSTACLEチームに所属します。
	     * @return 常にOBSTACLE
	     */
	    @Override
	    public Team getTeam() {		return OBSTACLE;
	}
}
