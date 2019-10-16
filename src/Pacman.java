import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.image.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*; 

public class Pacman extends JFrame implements ActionListener,KeyListener{
	public static final int WINX = 500; //window size
	public static final int WINY = 600;
	
	Timer myTimer;
	GamePanel game;
	//game setup
    public Pacman() {
		super("Pacman");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(WINX,WINY);

		myTimer = new Timer(10, this);
		myTimer.start();

		Image icon = new ImageIcon("icon.png").getImage(); 
		setIconImage(icon);

		game = new GamePanel();
		add(game);
		addKeyListener(this);
		setResizable(false);
		setVisible(true);
    }

	public void actionPerformed(ActionEvent evt){
		if(game != null){
			game.moveAll();
			game.repaint();				
		}		
	}

	//Key listener functions
    public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
    	game.setKey(e.getKeyCode(),true);
    }

    public void keyReleased(KeyEvent e) {
    	game.setKey(e.getKeyCode(),false);
    }

    public static void main(String[] args) {    	
		Pacman frame = new Pacman();
    }
}

class GamePanel extends JPanel{
	//offset of map from 0,0 to center map
	public static final int OFFSETX = 22; 
	public static final int OFFSETY = 42;
	
	//directions
	public static final int NONE = 0; 
	public static final int UP = 1;
	public static final int DOWN = 2;
	public static final int LEFT = 3;
	public static final int RIGHT = 4;
	//item colors
	public static final Color PACDOT_COL = new Color(238,10,255);
	public static final Color BIGDOT_COL = new Color(11,221,0);
	
	private boolean []keys;
	private Image back,pacLogo;
	private int score;
	private int lives = 2;
	private int menuCount=0; //int timer for text on menu screen
	
	//creating all entities
	Entity myPacman = new Entity(223,376,true,"pacman",3,24);
	
	Entity redGhost = new Entity(223,184,false,"redghost",2,24);
	Entity blueGhost = new Entity(191,231,false,"blueghost",2,24);
	Entity orangeGhost = new Entity(223,231,false,"orangeghost",2,24);
	Entity pinkGhost = new Entity(255,231,false,"pinkghost",2,24);
	
	Image [] stunGhost = new Image[2]; //stunned ghost sprite
	Entity [] allGhosts = {redGhost,blueGhost,orangeGhost,pinkGhost};
	
	ArrayList<Item> allItems = getItems(); //gets items from item map
	
	Font fnt,pacFnt,smallFnt;
	
	//switches for which screen to show
	boolean mainMenu = true; 
	boolean mainGame = false;
	boolean gameOver = false;
	
	boolean win = false;
	boolean death = false;	//pacman's "death"
	int deathFrame = -1;
	int stunFrame = -1;
	Image[] deathAnimate = new Image[11];
	
	int multiplier = 0; //multiplier for ghost points
	
	public GamePanel(){		
		for (int i = 0; i<11; i++){ //death animation sprites for pacman
			deathAnimate[i] = (new ImageIcon("dead"+(i+1)+".png").getImage()).getScaledInstance(30,30,Image.SCALE_SMOOTH);
		}
		for (int i = 0; i<2; i++){ //death animation sprites for ghosts
			stunGhost[i] = (new ImageIcon("stun"+(i+1)+".png").getImage()).getScaledInstance(30,30,Image.SCALE_SMOOTH);
		}
		keys = new boolean[KeyEvent.KEY_LAST+1];
		back = new ImageIcon("pacman_layout.png").getImage();
		pacLogo = new ImageIcon("paclogo3d.png").getImage().getScaledInstance(408,218,Image.SCALE_SMOOTH);
		setSize(Pacman.WINX,Pacman.WINY);
		fnt = new Font("Emulogic",Font.PLAIN,25);
		smallFnt = new Font("Emulogic",Font.PLAIN,18);
	}

    public void setKey(int k, boolean v){
    	keys[k] = v;
    }
	
	//contains all of the movement and collide checking for ghosts, pacman and items
	public void moveAll(){
		if (lives==0){
			gameOver = true;
			mainMenu = false;
			mainGame = false;
		}
		if (mainMenu){
			if (keys[KeyEvent.VK_ENTER]){
				mainMenu=false;
				mainGame=true;
			}
		}	
		if (mainGame){
			int tempMove = myPacman.getNextDir();
			//key checking and move allocating for pacman
			if (keys[KeyEvent.VK_RIGHT] || keys[KeyEvent.VK_D]){tempMove = RIGHT;}
			if (keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_A]){tempMove = LEFT;}
			if (keys[KeyEvent.VK_DOWN] || keys[KeyEvent.VK_S]){tempMove = DOWN;}
			if (keys[KeyEvent.VK_UP] || keys[KeyEvent.VK_W]){tempMove = UP;}
			myPacman.setNextDirection(tempMove); 
			//if pacman can go int the next direction set, it will set it as current direction
			if (Entity.areSameColors(myPacman.getNextColorAhead(),Entity.MAP_COL)){
				myPacman.setDirection(myPacman.getNextDir());
			}
			//advances pacman
			myPacman.advance(myPacman.getColorAhead(),myPacman.getNextColorAhead());
			
			//checks collisions with items, and sets all ghosts as vulnerable when collecting the big dots
			for (Item i:allItems){
				if (i.getX()==myPacman.getPosX() && i.getY()==myPacman.getPosY() && i.getOnScreen()){
					if (i.getPoints()==50){
						for (Entity e:allGhosts){e.setVulnerable(true); e.resetVulTimer();}
					}
					i.setOnScreen(false);
					score+=i.getPoints(); 
				}
			}
			
			for (Entity e:allGhosts){//checks ghost collisions with pacman when stunned and when not stunned
				if (pointsCollide(myPacman.getPosX(),myPacman.getPosY(),e.getPosX(),e.getPosY()) && e.getVulnerable()==false && death==false  && e.getInvisible()==false){ 
					lives--;
					death = true;//allows for death animation to display, instead of regular pacman sprite
					myPacman.setDirection(NONE);
					myPacman.setNextDirection(NONE);
				}
				if (pointsCollide(myPacman.getPosX(),myPacman.getPosY(),e.getPosX(),e.getPosY()) && e.getVulnerable()==true && e.getInvisible()==false){
					multiplier++;
					score+=100*(Math.pow(2,multiplier));
					e.revert(); //reverts ghost back to original position
					e.setInvisible(true); //makes sure collisions don't get checked more than once when colliding
				}
			}

			for (Entity ghost:allGhosts){
				ghost.moveGhost(myPacman.getPosX(),myPacman.getPosY(),ghost.getColorAhead(),ghost.getNextColorAhead()); //moves ghost
				if (ghost.getVulnerable()){ghost.updateVulTimer();} //ghosts can only be vulnerable for a certain amount of time
			}

			if (allGhosts[0].getVulnerable()==false){
				multiplier = 0; //resets multiplier when ghosts aren't vulnerable anymore
			}
			
			stunFrame++; //frame for stunned ghosts
			if (stunFrame==40){stunFrame=0;}
		}
	}

	//paints screen depending on which screens are true/false
    public void paintComponent(Graphics g){
    	if (win){gameOverPaint(g);}
    	else if (mainMenu){mainMenuPaint(g);}
    	else if (mainGame){mainGamePaint(g);}
    	else if (gameOver){gameOverPaint(g);}
    }
    
    //main menu
    public void mainMenuPaint(Graphics g){
    	menuCount++; //acts as a counter for when text appears on screen
    	if (menuCount==100){menuCount=0;}
    	g.setColor(Color.BLACK);
    	g.fillRect(0,0,Pacman.WINX,Pacman.WINY);
    	g.drawImage(back,OFFSETX,OFFSETY,this);
    	g.drawImage(myPacman.getSprites()[2][1],myPacman.getPosX()+11,myPacman.getPosY()+31,this); 
    	g.drawImage(pacLogo,47,40,this);
    	g.setColor(Color.WHITE);
    	g.setFont(smallFnt);
    	if (menuCount>50){
    		g.drawString("PUSH ENTER TO START",65,300);
    	}    	
    }
    
    //main game
    public void mainGamePaint(Graphics g){
    	if (death){delay(1);}
    	g.setColor(Color.BLACK);
    	g.fillRect(0,0,Pacman.WINX,Pacman.WINY);
    	g.drawImage(back,OFFSETX,OFFSETY,this);
    	if (allItems.size()==0){win = true;} //once all items have been collected, game is finished
    	for (Item i:allItems){ // displays all items
    		if (i.getOnScreen()){drawItem(i,g);}
    	}
    	g.setColor(Color.WHITE);
    	g.setFont(fnt);
    	String scoreDisplay = "Score: "+Integer.toString(score); 
		g.drawString(scoreDisplay,5,28); //dispalys score
		if (death==false){drawEntity(myPacman,g);} //when death animation isn't playing, use default pacman sprite
		else{//play death animation | I've tried to get this work for 4 hours, and this animation does NOT want to work.
			deathFrame++;
			System.out.println(deathFrame);
			Image tempImage = deathAnimate[deathFrame];
			g.drawImage(tempImage ,  myPacman.getPosX()-16 + OFFSETX , myPacman.getPosY()-16 + OFFSETY , this);
			if (deathFrame==10){
				death=false; 
				deathFrame=-1; //reset frame
				myPacman.revert();
				for (Entity ghost:allGhosts){ghost.revert();} //once pacman is reset, all ghosts get reset too
			}
		}
		for (Entity ghost:allGhosts){
			if (death==false){ //doesn't display ghosts when death animation is playing
				if (ghost.getVulnerable()){ //draws different animation when ghosts are vulnerable
					int tempFrame = -1;
					if (stunFrame<20){tempFrame=0;} //sets frame using stunFrame timer since there are only 2 frames
					else{tempFrame=1;}
					g.drawImage(stunGhost[tempFrame],ghost.getPosX()-16 + OFFSETX , ghost.getPosY()-16 + OFFSETY , this);
				}
				else{drawEntity(ghost,g);} //regular ghost draw
			}
		}
		
		for (int i = 0; i<lives; i++){ //displays how many lives are left
			g.drawImage(myPacman.getSprites()[2][1],i*32+5,Pacman.WINY-58,this);
		}
    }
    
    //game over screen
    public void gameOverPaint(Graphics g){
    	g.setColor(Color.BLACK);
    	g.fillRect(0,0,Pacman.WINX,Pacman.WINY);
    	g.setColor(Color.WHITE);
    	g.setFont(fnt);
    	if (win){g.drawString("YOU WIN!",130,250);}
    	else {g.drawString("GAME OVER",130,250);}
    	g.setFont(smallFnt);
    	g.drawString(("SCORE: "+score),160,350);
    }
    
    //main function for drawing any entity
    public void drawEntity(Entity e, Graphics g){    	
    	int tempDir = e.getDir();
    	if (tempDir==0){tempDir=4;}
    	Image[][] sprites = e.getSprites();
    	int tempHeight = e.getImgHeight()/2;
    	int tempWidth = e.getImgWidth()/2;
    	int tempFrame = e.getFrame();
    	
    	g.drawImage(sprites[tempDir-1][tempFrame] , e.getPosX()-tempWidth + OFFSETX , e.getPosY()-tempHeight + OFFSETY , this);
    }
    
    //draws any item
    public void drawItem(Item dot, Graphics g){
    	int x = dot.getX() + OFFSETX;
    	int y = dot.getY() + OFFSETY;
    	if (dot.getPoints()==10){
    		g.drawImage(dot.getPic(),x,y,this);
    	}
    	else{
    		g.drawImage(dot.getPic(),x-3,y-3,this);
    	}
    	
    }
	
	//gets all items
	public ArrayList<Item> getItems(){
		BufferedImage itemsMap = null; 
		
		ArrayList<Item> tempItemList = new ArrayList<Item>();
		
		try{itemsMap = ImageIO.read(new File("pacdots.png"));} //imports map that is color coded with items
		catch(IOException e){}
		
		//checks every pixel on the map and adds colored pixels as an item to the array
		for (int x=0;x<itemsMap.getWidth(this);x++){ 
			for (int y=0;y<itemsMap.getHeight(this);y++){
				int r,g,b;
				int tempCol = itemsMap.getRGB(x,y);
				r = (tempCol >> 16) & 0xFF;
				g = (tempCol >> 8) & 0xFF;
				b = tempCol & 0xFF;
				Color checkCol = new Color(r,g,b);
				if (Entity.areSameColors(PACDOT_COL,checkCol)){ //adds regular pacdot 
					tempItemList.add(new Item(x,y,"pacdot"));
				}
				else if (Entity.areSameColors(BIGDOT_COL,checkCol)){ //adds big dot that makes ghost vulnerable
					tempItemList.add(new Item(x,y,"bigdot"));
				}
			}
		}
		return tempItemList;				
	}
	
	//delays game by specified unit (not in seconds)
	public static void delay(int s){
		try{
			Thread.sleep(s*200);
		}
		catch(InterruptedException ex){
			System.out.println("Something's wrong with this code");
		}
	}
	//mainly for checking if ghosts and pacman collide
	//takes 2 points and checks if they are in a 3 block radius from one another
	public boolean pointsCollide(int x1, int y1, int x2, int y2){
		if (((x2-x1)<3 && (x2-x1)>-3) && ((y2-y1)<3 && (y2-y1)>-3)){return true;}
		return false;
	}
	
}


