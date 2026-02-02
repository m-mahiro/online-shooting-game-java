package client;

import stage.StageInfo;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * 練習モード用の入力戦略。
 * 移動とブロック生成を制限し、照準と射撃のみを許可します。
 * StageInfoを使用してゲーム状態に基づいた処理を実装できます。
 */
public class PracticeInputStrategy implements InputStrategy {

    private final StageInfo stageInfo;

    /**
     * コンストラクタ。
     *
     * @param stageInfo ステージ情報を取得するためのStageInfo
     */
    public PracticeInputStrategy(StageInfo stageInfo) {
        this.stageInfo = stageInfo;
    }

    @Override
    public Point2D.Double getMoveVector(AffineTransform canvasTransform) {
        // 練習モードでは移動不可
        return new Point2D.Double(0, 0);
    }

    @Override
    public Point2D.Double getAimedCoordinate(AffineTransform canvasTransform) {
        // 練習モードでは照準は中心固定（自動照準など実装可能）
        return new Point2D.Double(0, 0);
    }

    @Override
    public boolean shootBullet() {
        // 練習モードでは射撃不可
        return false;
    }

    @Override
    public boolean startEnergyCharge() {
        // 練習モードではチャージ不可
        return false;
    }

    @Override
    public boolean finishEnergyCharge() {
        // 練習モードではチャージ不可
        return false;
    }

    @Override
    public boolean createBlock() {
        // 練習モードではブロック生成不可
        return false;
    }

    @Override
    public int getZoomAmount() {
        // 練習モードではズーム不可
        return 0;
    }

    @Override
    public void onFrameUpdate() {
        // 練習モードでは特に処理なし
    }
}
