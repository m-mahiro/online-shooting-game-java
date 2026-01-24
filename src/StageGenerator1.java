import java.util.ArrayList;

public class StageGenerator1 implements StageGenerator {


	@Override
	public ArrayList<GameObject> generateStageObject(int bluePlayers, int redPlayers) {
		ArrayList<GameObject> objects = new ArrayList<>();
		Tank tank1 = new Tank(0, 0, Team.BLUE);
		Tank tank2 = new Tank(100, 500, Team.BLUE);
		Tank tank3 = new Tank(600, 200, Team.RED);
		Tank tank4 = new Tank(600, 500, Team.RED);
		objects.add(tank1);
		objects.add(tank2);
		objects.add(tank3);
		objects.add(tank4);

		// ブロック
		for (int j = 0; j < 4; j++) {
			Block block = new Block(350, 400 * j);
			objects.add(block);
		}

		return objects;
	}
}
