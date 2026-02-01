package stage;

public interface StageGenerator {

    GameObject[] getGameObjects();

    ScreenObject[] getScreenObjects();

    Base getRedBase();

    Base getBlueBase();

    int getStageWidth();

    int getStageHeight();

    Tank[] getTanks();

    boolean isNetworked();
}
