package tbc.trader;

public class Item
{
    public static final Item NOTHING = new Item(null);
    
    private Info.Type type;
    
    public Item(Info.Type t)
    {
        this.type = t;
    }
    
    public Info.Type getType()
    {
        return type;
    }
    
}
