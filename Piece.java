
public class Piece 
{
	private String type;
	private int x;
	private int y;
	private boolean placed = false;
	private boolean selected = false;
	
	public Piece(String type)
	{
		this.type = type;
		this.x = -2;
		this.y = -2;
	}
	
	public Piece(String type, int x, int y)
	{
		this.type = type;
		this.x = x;
		this.y = y;
	}
	
	public String getType()
	{
		return type;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public boolean getPlaced()
	{
		return placed;
	}
	
	public boolean getSelected()
	{
		return selected;
	}
	
	public void SetX(int set)
	{
		x = set;
	}
	
	public void SetY(int set)
	{
		y = set;
	}
	
	public void SetPlaced(boolean set)
	{
		placed = set;
	}
	
	public void SetSelected(boolean set)
	{
		selected = set;
	}
}
