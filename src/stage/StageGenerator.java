package stage;

import java.awt.*;

public interface StageGenerator {

    /**
     * ゲームステージに配置するGameObjectの配列を取得します。
     * これには<code>getRedBase()</code>や<code>getBlueBase()</code>が返すオブジェクトも含まれます。
     * @return 配置するGameObjectの配列
     */
    GameObject[] getGameObjects();

    /**
     * ゲームステージに配置するScreenObjectの配列を取得します。
     * @return 配置するScreenObjectの配列
     */
    ScreenObject[] getScreenObjects();

    /**
     * 赤チームの基地オブジェクトを取得します。
     * @return 赤チームの基地
     */
    Base getRedBase();

    /**
     * 青チームの基地オブジェクトを取得します。
     * @return 青チームの基地
     */
    Base getBlueBase();

    /**
     * ステージの幅を取得します。
     *
     * @return ステージの幅
     */
    double getStageWidth();

    /**
     * ステージの高さを取得します。
     *
     * @return ステージの高さ
     */
    double getStageHeight();

    /**
     * 背景（ステージのテクスチャとステージ外の背景）を描画します。
     * @param graphics カメラ位置・ズームによる座標変換を適用済みのGraphics2D
     * @param visibleWidth カメラに映る（ウィンドウに映る）範囲の幅(ステージ上の座標系)
     * @param visibleHeight カメラに映る（ウィンドウに映る）範囲の高さ(ステージ上の座標系)
     * @param animationFrame ステージ外のテクスチャのアニメーション用フレーム数
     */
    void drawBackground(Graphics2D graphics, double visibleWidth, double visibleHeight, double animationFrame);
}
