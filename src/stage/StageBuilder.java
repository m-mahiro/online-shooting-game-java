package stage;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

import static stage.Team.*;

public class StageBuilder {

    public GameStage createDefaultStage(int players) {
        // 1. ベースを生成
        Base redBase = new Base(2000, 2000, RED);
        Base blueBase = new Base(-2000, -2000, BLUE);

        // 2. ベースを渡してステージを生成
        GameStage stage = new GameStage(redBase, blueBase);

        // 3. その他のオブジェクトを生成
        Collection<GameObject> objects = new ArrayList<>();

        // 戦車
        objects.add(new Tank(redBase));
        for (int i = 1; i < players; i++) {
            Team team = (i % 2 == 0 ) ? RED : BLUE;
            switch (team) {
                case RED: {
                    Tank tank = new Tank(redBase);
                    objects.add(tank);
                    break;
                }
                case BLUE: {
                    Tank tank = new Tank(blueBase);
                    objects.add(tank);
                    break;

                }
            }
        }

        // 壁
        int stageWidth = stage.getStageWidth();
        int stageHeight = stage.getStageHeight();
        int verticalWall = stageHeight / Wall.HEIGHT;
		int horizontalWall = stageWidth / Wall.WIDTH;
		for (int i = 0; i <= verticalWall; i++) {
			for (int j = 0; j <= horizontalWall; j++) {
				if (i != 0 && i != verticalWall && j != 0 && j != horizontalWall) continue;
				double x = Wall.WIDTH * i - stageWidth / 2.0;
				double y = Wall.HEIGHT * j - stageHeight / 2.0;
				Point2D.Double point = new Point2D.Double(x, y);
				Wall wall = new Wall(point);
				objects.add(wall);
			}
		}

        // 4. ステージにオブジェクトを追加
        stage.addStageObjects(objects);

        return stage;
    }
}
