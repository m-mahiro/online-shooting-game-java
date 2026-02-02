package stage;

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
     * @return ステージの幅
     */
    int getStageWidth();

    /**
     * ステージの高さを取得します。
     * @return ステージの高さ
     */
    int getStageHeight();
}
