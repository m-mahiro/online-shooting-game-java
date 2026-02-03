package stage;

public interface StageInfo {

	int getRedBaseHP();

	int getBlueBaseHP();

	Base.State getRedBaseState();

	Base.State getBlueBaseState();

	int getRedTank();

	int getBlueTank();

	boolean hasFinished();

	Team getWinner();

}
