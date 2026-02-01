package stage;

public interface StageGenerator {
    GameObject[] getGameObjects();
    UpperStageObject[] getUpperStageObjects();
    Base getRedBase();
    Base getBlueBase();
    int getStageWidth();
    int getStageHeight();
    Tank[] getTanks();
    boolean isNetworked();
}
