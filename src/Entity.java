import java.awt.*;
import java.awt.image.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.*; 

class Entity extends Frame{
	public static final int NONE = 0;
	public static final int UP = 1;
	public static final int DOWN = 2;
	public static final int LEFT = 3;
	public static final int RIGHT = 4;
	
	public static final Color MAP_COL = new Color(33,33,222); 
	
	public BufferedImage map = null;
	
	private Image[][] sprites;
	private int x,initX,y,initY,direction,nextDirection,r,g,b,imgHeight,imgWidth,frame,picsPerSet,vulTimer;
	private boolean pac,moving,vulnerable,invisible;
	
    public Entity(int ex, int ey, boolean pac, String imageName, int numOfPicsPerSpriteSet, int imgDims) {
    	try{map = ImageIO.read(new File("map.png"));}
		catch(IOException e){}
    	
    	x = ex;
    	y = ey;
    	initX = ex;
    	initY = ey;
    	this.pac = pac;
    	direction = NONE; //default to no direction
    	nextDirection = NONE;
    	moving = false;
    	picsPerSet = numOfPicsPerSpriteSet;
    	vulnerable = false;
    	sprites = new Image[4][picsPerSet];
    	
    	invisible = false; //for ghosts
    	
    	int scaleX = imgDims;
    	int scaleY = imgDims;
    	
    	for (int i=1; i<5; i++){ //imports sprites
    		for (int j=1; j<picsPerSet+1; j++){
    			String tempStr = (imageName+i)+j;
    			sprites[i-1][j-1] = (new ImageIcon("sprites/"+tempStr+".png").getImage()).getScaledInstance(scaleX,scaleY,Image.SCALE_SMOOTH);
    		}
    	}
    	vulTimer = 0;
    	frame = 0;
    	imgHeight = scaleY;
    	imgWidth = scaleX;
    }
    
    //utility
    public int getPosX(){return x;}
    public int getPosY(){return y;}
    public int getInitX(){return initX;}
    public int getInitY(){return initY;}
    public int getDir(){return direction;}
    public int getNextDir(){return nextDirection;}
    public int getImgHeight(){return imgHeight;}
    public int getImgWidth(){return imgWidth;}
    public boolean isPacman(){return pac;}
    public boolean getMoving(){return moving;}
    public Image[][] getSprites(){return sprites;}
    public int getFrame(){return frame/100;}
    public boolean getVulnerable(){return vulnerable;}
    public boolean getInvisible(){return invisible;}
    
    public void revert(){
    	x=initX;
    	y=initY;
    }
    
    public void setX(int x){this.x=x;}
    public void setY(int y){this.y=y;}
    public void setVulnerable(boolean b){vulnerable = b;}
    public void setDirection(int direction){this.direction=direction;}
    public void setNextDirection(int direction){nextDirection=direction;}
    public void setInvisible(boolean b){invisible = b;}
	public void resetVulTimer(){vulTimer = 0;}
	
    //gets color that is one pixel in front of the direction that the entity is heading in
    public Color getColorAhead(){
    	int tempCol = 0;
    	if (direction==UP){tempCol = map.getRGB(x,y-1);}
    	else if (direction==DOWN){tempCol = map.getRGB(x,y+1);}
    	else if (direction==LEFT){tempCol = map.getRGB(x-1,y);}
    	else if (direction==RIGHT){tempCol = map.getRGB(x+1,y);}
    	
    	r = (tempCol >> 16) & 0xFF;
		g = (tempCol >> 8) & 0xFF;
		b = tempCol & 0xFF;
		
		return new Color(r,g,b);
    }
    
    //does the same as above, but with nextDirection
    public Color getNextColorAhead(){
    	int tempCol = 0;
    	if (nextDirection==UP){tempCol = map.getRGB(x,y-1);}
    	else if (nextDirection==DOWN){tempCol = map.getRGB(x,y+1);}
    	else if (nextDirection==LEFT){tempCol = map.getRGB(x-1,y);}
    	else if (nextDirection==RIGHT){tempCol = map.getRGB(x+1,y);}
    	
    	r = (tempCol >> 16) & 0xFF;
		g = (tempCol >> 8) & 0xFF;
		b = tempCol & 0xFF;
		
		return new Color(r,g,b);
    }
    
    //advances pacman
    public void advance(Color colorAhead, Color nextColorAhead){
    	if (areSameColors(colorAhead,MAP_COL)){ //checks if pacman can move forward in the direction from the direction variable
    		if (direction==UP){y-=1;}
			if (direction==DOWN){y+=1;}
			if (direction==LEFT){x-=1;}
			if (direction==RIGHT){x+=1;}
			moving = true;
    	}
    	else{
    		moving = false;
    	}
    	
    	if (moving==false && areSameColors(nextColorAhead,MAP_COL)){ //once pacman can move in the direction specified next, it will
    		direction = nextDirection;	
    	}
    	
    	if (x==24 && y==232 && direction==LEFT){x=423;} // tunnel points on left & right of map
    	if (x==423 && y==232 && direction==RIGHT){x=23;}
    	
    	int speed = 20; //frame speed
    	
    	if (moving){ //frames will stop when moving stops
			frame+=speed;
	    	if (frame>picsPerSet*100-speed){frame = 0;}
    	}
    }
    
    //main function to move ghost
    public void moveGhost(int pacX, int pacY, Color colorAhead, Color nextColorAhead){
    	if (areSameColors(colorAhead,MAP_COL)){ // if ghost can move in direction set, it will
    		if (direction==UP){y-=1;}
			if (direction==DOWN){y+=1;}
			if (direction==LEFT){x-=1;}
			if (direction==RIGHT){x+=1;}
			moving = true;
    	}
    	else{
    		moving = false;
    	}
    	
    	if (moving==false && areSameColors(nextColorAhead,MAP_COL)){
    		direction = nextDirection;	
    	}
    	
    	if (x==24 && y==232 && direction==LEFT){x=423;}
    	if (x==423 && y==232 && direction==RIGHT){x=23;}
    	
    	int speed = 20;
    	
    	if (moving){
			frame+=speed;
	    	if (frame>picsPerSet*100-speed){frame = 0;}
    	}
    	
    	if (moving==false){/*
    		if (x>pacX && areSameColors(getCol(LEFT,x,y),MAP_COL)){direction=LEFT;}
    		else if (x<pacX && areSameColors(getCol(RIGHT,x,y),MAP_COL)){direction=RIGHT;}
    		else if (y>pacY && areSameColors(getCol(UP,x,y),MAP_COL)){direction=UP;}
    		else if (y<pacY && areSameColors(getCol(DOWN,x,y),MAP_COL)){direction=DOWN;}
    		*/

    		direction = randNum(1,4);
    	}
    }
    
    //gets color of specified direction and point on map 
    public Color getCol(int d, int x1, int y1){
    	int tempCol = 0;
    	if (d==UP){tempCol = map.getRGB(x1,y1-1);}
    	else if (d==DOWN){tempCol = map.getRGB(x1,y1+1);}
    	else if (d==LEFT){tempCol = map.getRGB(x1-1,y1);}
    	else if (d==RIGHT){tempCol = map.getRGB(x1+1,y1);}
    	
    	r = (tempCol >> 16) & 0xFF;
		g = (tempCol >> 8) & 0xFF;
		b = tempCol & 0xFF;
		
		return new Color(r,g,b);
    }
    
    //updates vulnerable timer, makes vulnerable and invisibility false when time is up
    public void updateVulTimer(){
    	vulTimer++;
    	if (vulTimer==1000){
    		vulTimer = 0;
    		vulnerable = false;
    		invisible = false;
    	}
    }
    
    //checks if two colors are the same
    public static boolean areSameColors(Color col1, Color col2){
    	if (col1.getRed()==col2.getRed() && col1.getGreen()==col2.getGreen() && col1.getBlue()==col2.getBlue()){return true;}
    	return false;
    }
    
    //generates random number. min being the lowest included number, and max being the highest
    public int randNum(int min, int max){
	    int range = max-min+1;
	    return (int)(Math.random()*range)+min;
	}
}