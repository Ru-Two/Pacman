import java.awt.*;
import javax.swing.*;

public class Item {
	
	int x,y,points;
	Image pic;
	boolean onScreen;
	//creates item with image, and coordinates
    public Item(int x, int y, String picName) {
    	this.x=x;
    	this.y=y;
    	if (picName.equals("pacdot")){points = 10;}
    	if (picName.equals("bigdot")){points = 50;}
    	onScreen = true;
    	pic = new ImageIcon(picName+".png").getImage();
    }    
	//utility
	public int getX(){return x;};
	public int getY(){return y;};
	public Image getPic(){return pic;}
	public boolean getOnScreen(){return onScreen;}
	public int getPoints(){return points;}
	
	public void setOnScreen(boolean b){onScreen = b;}
}