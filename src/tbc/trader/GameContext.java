package tbc.trader;

public class GameContext
{
    private Info info;
    
    public GameContext(String infoSetName)
    {
        info = Info.getSet(infoSetName);
    }
    
    public Info getInfoSet()
    {
        return info;
    }

}
