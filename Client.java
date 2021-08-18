import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Client extends JFrame implements ActionListener, MouseListener, Runnable
{
	private static final int SERVER_PORT = 9000;
	
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	
	private MyPanel pan;
	
	private JButton connectButton;
	private JLabel stratego;
	private JButton readyButton;
	private JButton advanceButton;
	private JTextField ipInput;
	private JLabel ipError;
	
	//track game states
	private boolean inGame = false;
	private boolean startPhase = false;
	private boolean oppReady = false;
	private boolean gameStarted = false;
	private boolean myTurn = false;
	private boolean battlePhase1 = false;
	private boolean battlePhase2 = false;
	private boolean connectionError = false;
	
	//track pieces involved in a battle
	private String myPiece;
	private String oppPiece;
	
	//track location of battle
	private int battleX;
	private int battleY;
	
	//track piece type selected in start phase
	private int selected = - 2;
	
	//track last move properties
	private int lastMovepx;
	private int lastMovepy;
	private int lastMovex;
	private int lastMovey;
	
	//track piece indices to be removed
	private int myToRemove = -1;
	private int oppToRemove = -1;
	
	//track result of a battle
	private int battle;
	
	private final Color LIGHT_BLUE = new Color(54, 88, 224);
	
	private ArrayList<Piece> myPieces = new ArrayList<Piece>();
	private ArrayList<Piece> oppPieces = new ArrayList<Piece>();
	
	public static void main(String[] args) throws UnknownHostException, IOException
	{
		Client client = new Client();
	}
	
	public Client() throws UnknownHostException, IOException
	{
		makePieces();
		createWindow();
		createMenu();
	}
	
	public void createConnection() throws IOException
	{
		socket = new Socket(ipInput.getText(), SERVER_PORT);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
	}
	
	public void exit() throws IOException
	{
		out.close();
		in.close();
		socket.close();
		System.exit(0);
	}
	
	//begin start phase
	public void startPhase()
	{
		inGame = true;
		startPhase = true;
		makeBoard();
	}
	
	public void createWindow()
	{
		setBounds(0, 0, 900, 650);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Stratego: Client");
		pan = new MyPanel();
		getContentPane().add(pan);
		addMouseListener(this);
		setResizable(false);
		setVisible(true);
	}
	
	public void createMenu()
	{
		
		stratego = new JLabel("Stratego");
		stratego.setFont(new Font("",Font.PLAIN, 100));
		pan.add(stratego);
		
		connectButton = new JButton("Connect");
		connectButton.setBounds(300, 300, 300, 80);
		connectButton.addActionListener(this);
		pan.add(connectButton);
		
		ipInput = new JTextField("Enter IP of Host");
		ipInput.setBounds(300, 400, 300, 30);
		ipInput.addActionListener(this);
		pan.add(ipInput);
	}
	
	public void makeBoard()
	{
		pan.remove(connectButton);
		pan.remove(stratego);
		
		stratego = new JLabel("Stratego");
		stratego.setBounds(600, 10, 800, 70);
		stratego.setFont(new Font("",Font.PLAIN, 60));
		pan.add(stratego);
		
		readyButton = new JButton("Ready");
		readyButton.setBounds(675, 540, 80, 40);
		readyButton.addActionListener(this);
		pan.add(readyButton);
	}
	
	public void makePieces()
	{
		for(int i = 0; i < 8; i++)
			myPieces.add(new Piece("2"));
		for(int i = 0; i < 5; i++)
			myPieces.add(new Piece("3"));
		for(int i = 0; i < 4; i++)
			myPieces.add(new Piece("4"));
		for(int i = 0; i < 4; i++)
			myPieces.add(new Piece("5"));
		for(int i = 0; i < 4; i++)
			myPieces.add(new Piece("6"));
		for(int i = 0; i < 3; i++)
			myPieces.add(new Piece("7"));
		for(int i = 0; i < 2; i++)
			myPieces.add(new Piece("8"));
		myPieces.add(new Piece("9"));
		myPieces.add(new Piece("10"));
		myPieces.add(new Piece("S"));
		for(int i = 0; i < 6; i++)
			myPieces.add(new Piece("B"));
		myPieces.add(new Piece("F"));
		
		for(int i = 0; i < 10; i++)
			for(int i2 = 0; i2 < 4; i2++)
				oppPieces.add(new Piece("0", i, i2));
	}
	
	public int getNumberUnplaced(String type)
	{
		int count = 0;
		for(Piece p : myPieces)
			if(p.getType().equals(type) && p.getPlaced() == false)
				count++;
		return count;
	}
	
	public void deselectAll()
	{
		for(Piece p : myPieces)
			p.SetSelected(false);
	}
	
	public void selectStartPhase(String type)
	{
		if(type.equals("11"))
			type = "S";
		if(type.equals("12"))
			type = "B";
		if(type.equals("13"))
			type = "F";
		
		for(Piece p : myPieces)
		{
			if(p.getPlaced() == false && p.getType().equals(type))
			{
				p.SetSelected(true);
				return;
			}
		}
	}
	
	public void placePiece(int x, int y)
	{
		if(y > 5 && y < 10 && x < 10)
		{
			for(Piece p : myPieces)
			{
				if(p.getX() == x && p.getY() == y)
					return;
			}
			for(Piece p : myPieces)
			{
				if(p.getSelected() == true)
				{
					p.SetPlaced(true);
					p.SetX(x);
					p.SetY(y);
					deselectAll();
				}
			}
		}
	}
	
	public void removePiece(int x, int y)
	{
		for(Piece p : myPieces)
		{
			if(p.getX() == x && p.getY() == y)
			{
				p.SetX(-2);
				p.SetY(-2);
				p.SetPlaced(false);
				deselectAll();
			}
				
		}
	}
	
	public void ready()
	{
		if(allPlaced())
		{
			startPhase = false;
			pan.remove(readyButton);
			out.println("Ready");
		}
	}
	
	public void selectPiece(int x, int y)
	{
		for(Piece p: myPieces)
		{
			if(p.getX() == x && p.getY() == y)
			{
				if(!p.getType().equals("B") && !p.getType().equals("F"))
				{
					p.SetSelected(true);
					break;
				}
			}
		}
	}
	
	public void movePiece(int x, int y)
	{
		for(Piece p: myPieces)
		{
			if(p.getSelected())
			{
				if(validMove(p, x, y))
				{
					out.println("Move " + p.getX() + p.getY() + x + y + p.getType());
					p.SetX(x);
					p.SetY(y);
					myTurn = false;
				}
			}
		}
	}
	
	public boolean validMove(Piece p, int x, int y)
	{
		if(x < 0 || x > 9 || y < 0 || y > 9)
		{
			return false;
		}
		
		if(x == 2 || x == 3)
			if(y == 4 || y == 5)
				return false;
		
		if(x == 6 || x == 7)
			if(y == 4 || y == 5)
				return false;
		
		for(Piece p2 : myPieces)
		{
			if(p2.getX() == x && p2.getY() == y)
				return false;
		}
		
		if(p.getX() == x && p.getY() == y)
			return false;
		
		if(p.getType().equals("2"))
		{
			if(Math.abs(p.getX() - x) + Math.abs(p.getY() - y) == 1)
				return true;
			
			if(p.getX() == x || p.getY() == y)
			{
				if(!pathClear(p.getX(), p.getY(), x, y))
					return false;
			}
			else
				return false;
		}
		else
		{
			if(Math.abs(p.getX() - x) + Math.abs(p.getY() - y) != 1)
				return false;
		}
		
		return true;
	}
	
	public boolean pathClear(int x1, int y1, int x2, int y2)
	{
		int xmod = 0;
		int ymod = 0;
		
		if(x1 < x2)
			xmod = 1;
		if(x1 > x2)
			xmod = -1;
		if(y1 < y2)
			ymod = 1;
		if(y1 > y2)
			ymod = -1;
		
		x1 += xmod;
		y1 += ymod;
		
		while(x1 != x2 || y1 != y2)
		{
			if(x1 == 2 || x1 == 3)
				if(y1 == 4 || y1 == 5)
					return false;
			
			if(x1 == 6 || x1 == 7)
				if(y1 == 4 || y1 == 5)
					return false;
			
			if(spaceOccupied(x1, y1))
				return false;
			if(spaceOccupied2(x1, y1))
				return false;
			x1 += xmod;
			y1 += ymod;
		}
		
		return true;
	}
	
	public boolean allPlaced()
	{
		for(Piece p : myPieces)
		{
			if(p.getPlaced() == false)
				return false;
		}
		return true;
	}
	
	public boolean pieceSelected()
	{
		for(Piece p : myPieces)
			if(p.getSelected())
				return true;
		return false;
	}
	
	public int translateNum(int i)
	{
		return Math.abs(i - 9);
	}
	
	public void moveOpp(int x1, int y1, int x2, int y2)
	{
		for(Piece p : oppPieces)
		{
			if(p.getX() == x1 && p.getY() == y1)
			{
				p.SetX(x2);
				p.SetY(y2);
				return;
			}
		}
	}
	
	public boolean spaceOccupied(int x, int y)
	{
		for(Piece p : myPieces)
			if(p.getX() == x && p.getY() == y)
				return true;
		return false;
	}
	
	public boolean spaceOccupied2(int x, int y)
	{
		for(Piece p : oppPieces)
			if(p.getX() == x && p.getY() == y)
				return true;
		return false;
	}
	
	public String getType(int x, int y)
	{
		for(Piece p : myPieces)
			if(p.getX() == x && p.getY() == y)
				return p.getType();
		return "Error Piece not Found";
	}
	
	//returns: 1. Win, 2. Loss, 3. Draw, 4. Opp Flag Captured, 5. Our Flag Captured
	public int simBattle(String mine, String opp, boolean attacking)
	{
		myPiece = mine;
		oppPiece = opp;
		advanceButton = new JButton("Advance");
		advanceButton.setBounds(610, 540, 200, 50);
		advanceButton.addActionListener(this);
		pan.add(advanceButton);
		
		if(opp.equals("F"))
			return 4;
		if(mine.equals("F"))
			return 5;
		if(mine.equals(opp))
			return 3;
		if(opp.equals("B"))
		{
			if(mine.equals("3"))
				return 1;
			else
				return 3;
		}
		if(mine.equals("B"))
		{
			if(opp.equals("3"))
				return 2;
			else
				return 3;
		}
		if(opp.equals("S"))
		{
			if(mine.equals("10") && !attacking)
				return 2;
			else
				return 1;
		}
		if(mine.equals("S"))
		{
			if(opp.equals("10"))
				return 1;
			else
				return 2;
		}
		if(Integer.parseInt(mine) > Integer.parseInt(opp))
			return 1;
		else
			return 2;
	}
	
	public void captureMyPiece(int x, int y)
	{
		for(int i = 0; i < myPieces.size(); i++)
		{
			if(myPieces.get(i).getX() == x && myPieces.get(i).getY() == y)
			{
				myToRemove = i;
				return;
			}
		}
	}
	
	public void captureOppPiece(int x, int y)
	{
		for(int i = 0; i < oppPieces.size(); i++)
		{
			if(oppPieces.get(i).getX() == x && oppPieces.get(i).getY() == y)
			{
				oppToRemove = i;
				return;
			}
		}
	}
	
	public void checkGameOver()
	{
		if(battle == 4)
		{
			gameOver(true);
			return;
		}
		
		if(battle == 5)
		{
			gameOver(false);
			return;
		}
		
		if(!movablePieceLeft())
		{
			gameOver(false);
			out.println("I Lost");
		}
	}
	
	public boolean movablePieceLeft()
	{
		for(Piece p : myPieces)
		{
			if(!p.getType().equals("F") && !p.getType().equals("B"))
				return true;
		}
		return false;
	}
	
	public void gameOver(boolean won)
	{
		inGame = false;
		gameStarted = false;
		pan.remove(stratego);
		
		JLabel gameOver = new JLabel("Game Over");
		gameOver.setBounds(225, 50, 500, 80);
		gameOver.setFont(new Font("",Font.PLAIN, 80));
		pan.add(gameOver);
		
		if(won)
		{
			JLabel winText = new JLabel("You Won!");
			winText.setFont(new Font("",Font.PLAIN, 50));
			winText.setBounds(325, 300, 300, 80);
			pan.add(winText);
		}
		else
		{
			JLabel lossText = new JLabel("You Lost!");
			lossText.setFont(new Font("",Font.PLAIN, 50));
			lossText.setBounds(325, 300, 300, 80);
			pan.add(lossText);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == connectButton)
		{
			try 
			{
				createConnection();
				pan.remove(connectButton);
				pan.remove(ipInput);
				if(connectionError)
					pan.remove(ipError);
				Thread t = new Thread(this);
				t.start();
			} 
			catch (IOException e1) 
			{
				// TODO Auto-generated catch block
				if(!connectionError)
				{
					ipError = new JLabel("Connection Failed");
					ipError.setBounds(400, 450, 300, 30);
					pan.add(ipError);
					connectionError = true;
				}
				e1.printStackTrace();
			}
			
			
		}
		
		if(e.getSource() == readyButton)
		{
			ready();
		}
		
		if(e.getSource() == advanceButton)
		{
			if(battlePhase2)
			{
				battlePhase2 = false;
				pan.remove(advanceButton);
				checkGameOver();
			}
			
			if(battlePhase1)
			{
				battlePhase1 = false;
				battlePhase2 = true;
			}
		}
	}

	@Override
	public void run() 
	{	
		startPhase();
		
		while(inGame)
		{
			String s = waitForMessage();
			
			waitForBattle();
			
			if(s.equals("Ready"))
				oppReady = true;
			if(s.contains("Move"))
				recievedMoveMessage(s);
			if(s.contains("Resp"))
				recievedResponseMessage(s);
			if(s.contains("I Lost"))
				gameOver(true);
			
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public String waitForMessage()
	{
		String s = "";
		try 
		{
			s = in.readLine();
		} 
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return s;
	}
	
	public void waitForBattle()
	{
		while(battlePhase1 || battlePhase2)
		{
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void recievedMoveMessage(String s)
	{
		int x1 = translateNum(Character.getNumericValue(s.charAt(5)));
		int y1 = translateNum(Character.getNumericValue(s.charAt(6)));
		int x2 = translateNum(Character.getNumericValue(s.charAt(7)));
		int y2 = translateNum(Character.getNumericValue(s.charAt(8)));
		String oppType = s.substring(9);
		
		moveOpp(x1, y1, x2, y2);
		lastMovepx = x1;
		lastMovepy = y1;
		lastMovex = x2;
		lastMovey = y2;
		
		if(spaceOccupied(x2, y2))
		{
			out.println("Resp " + x2 + y2 + getType(x2, y2));
			battle = simBattle(getType(x2, y2), oppType, false);
			if(battle == 1 || battle == 4)
				captureOppPiece(x2, y2);
			if(battle == 2 || battle == 5)
				captureMyPiece(x2, y2);
			if(battle == 3)
			{
				captureMyPiece(x2, y2);
				captureOppPiece(x2, y2);
			}
			
			battleX = x2;
			battleY = y2;
			
			battlePhase1 = true;
		}
		
		myTurn = true;
	}
	
	public void recievedResponseMessage(String s)
	{
		int x = translateNum(Character.getNumericValue(s.charAt(5)));
		int y = translateNum(Character.getNumericValue(s.charAt(6)));
		String oppType = s.substring(7);
		
		battle = simBattle(getType(x, y), oppType, true);
		
		if(battle == 1 || battle == 4)
			captureOppPiece(x, y);
		if(battle == 2 || battle == 5)
			captureMyPiece(x, y);
		if(battle == 3)
		{
			captureMyPiece(x, y);
			captureOppPiece(x, y);
		}
		
		battleX = x;
		battleY = y;
		
		battlePhase1 = true;
	}
	
	private class MyPanel extends JPanel
	{
		
		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			try 
			{
				drawStuff(g);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		
		
		public void drawStuff(Graphics g) throws InterruptedException
		{
			if(inGame)
			{	
				drawBoard(g);
				if(startPhase)
				{
					drawStartPhaseComponents(g);
				}
				
				//Remove pieces to be removed here in order to prevent modifying during a For loop from other thread
				if(myToRemove != -1)
				{
					myPieces.remove(myToRemove);
					myToRemove = -1;
				}
				
				if(oppToRemove != -1)
				{
					oppPieces.remove(oppToRemove);
					oppToRemove = -1;
				}
				
				drawMyPieces(g);
			}
			
			if(gameStarted)
			{
				drawTurnText(g);
				drawValidMoves(g);
				drawOpposingPieces(g);
				drawLastMove(g);
				drawBattle(g);
			}
			
			checkToUpdateTurn();
			repaint();
		}
		
		public void drawBoard(Graphics g)
		{
			stratego.setBounds(600, 10, 800, 70);
			
			for(int i = 0; i < 11; i++)
			{
				g.drawLine(20 + i * 50, 20, 20 + i * 50, 520);
			}
			
			for(int i = 0; i < 11; i++)
			{
				g.drawLine(20, 20 + i * 50, 520, 20 + i * 50);
			}
			
			g.fillRect(120, 220, 100, 100);
			g.fillRect(320, 220, 100, 100);
		}
		
		public void drawStartPhaseComponents(Graphics g)
		{
			g.setColor(LIGHT_BLUE);
			for(int i = 0; i < 13; i++)
			{
				g.fillRect(25 + i * 50, 540, 40, 40);
			}
			
			g.setColor(Color.yellow);
			g.fillRect(25 + selected * 50, 540, 40, 40);
			
			for(int i = 0; i < 8; i++)
			{
				g.setColor(Color.black);
				g.setFont(new Font("",Font.PLAIN, 30));
				g.drawString("" + (i + 2), 35 + i * 50, 570);
			}
			g.drawString("10", 427, 570);
			g.drawString("S", 485, 570);
			g.drawString("B", 535, 570);
			g.drawString("F", 585, 570);
			g.drawString("X", 635, 570);
			
			for(int i = 0; i < 8; i++)
			{
				g.setColor(Color.black);
				g.setFont(new Font("",Font.PLAIN, 15));
				g.drawString("(" + getNumberUnplaced("" + (i + 2)) + ")", 35 + i * 50, 595);
			}
			g.drawString("(" + getNumberUnplaced("10") + ")", 435, 595);
			g.drawString("(" + getNumberUnplaced("S") + ")", 485, 595);
			g.drawString("(" + getNumberUnplaced("B") + ")", 535, 595);
			g.drawString("(" + getNumberUnplaced("F") + ")", 585, 595);
		}
		
		public void drawMyPieces(Graphics g)
		{
			for(Piece p : myPieces)
			{
				g.setColor(LIGHT_BLUE);
				if(p.getSelected())
					g.setColor(Color.yellow);
				g.fillRect(25 + p.getX() * 50, 25 + p.getY() * 50, 40, 40);
				g.setColor(Color.black);
				g.setFont(new Font("",Font.PLAIN, 15));
				if(p.getType().equals("10"))
					g.drawString(p.getType(), 35 + p.getX() * 50, 50 + p.getY() * 50);
				else
					g.drawString(p.getType(), 40 + p.getX() * 50, 50 + p.getY() * 50);
			}
		}
		
		public void drawTurnText(Graphics g)
		{
			g.setColor(Color.black);
			g.setFont(new Font("",Font.PLAIN, 30));
			if(myTurn)
				g.drawString("It is your turn to move", 20, 595);
			else
				g.drawString("It is your opponent's turn to move", 20, 595);
		}
		
		public void drawValidMoves(Graphics g)
		{
			if(pieceSelected())
			{
				Piece temp = new Piece("0");
				for(Piece p : myPieces)
				{
					if(p.getSelected())
					{
						temp = p;
						break;
					}
				}
				
				for(int i = 0; i < 10; i++)
				{
					for(int i2 = 0; i2 < 10; i2++)
					{
						if(validMove(temp, i, i2))
						{
							g.setColor(Color.YELLOW);
							g.fillRect(21 + i * 50, 21 + i2 * 50, 49, 49);
						}
					}
				}
			}
		}
		
		public void drawOpposingPieces(Graphics g)
		{
			for(Piece p : oppPieces)
			{
				g.setColor(Color.red);
				g.fillRect(25 + p.getX() * 50, 25 + p.getY() * 50, 40, 40);
			}
		}
		
		public void drawLastMove(Graphics g)
		{
			g.setColor(Color.red);
			g.drawLine(45 + lastMovepx * 50, 45 + lastMovepy * 50, 45 + lastMovex * 50, 45 + lastMovey * 50);
		}
		
		public void drawBattle(Graphics g)
		{
			//Draw both pieces in battle (phase 1)
			if(battlePhase1)
			{
				g.setColor(LIGHT_BLUE);
				g.fillRect(610, 90, 200, 200);
				g.setColor(Color.black);
				g.setFont(new Font("",Font.PLAIN, 120));
				if(myPiece.equals("10"))
					g.drawString(myPiece, 640, 230);
				else
					g.drawString(myPiece, 675, 230);
				
				g.setColor(Color.red);
				g.fillRect(610, 310, 210, 200);
				g.setColor(Color.black);
				g.setFont(new Font("",Font.PLAIN, 120));
				if(oppPiece.equals("10"))
					g.drawString(oppPiece, 640, 450);
				else
					g.drawString(oppPiece, 675, 450);
				
				//Draw line from square battle took place on to make it clear
				g.setColor(Color.yellow);
				g.drawLine(45 + battleX * 50, 45 + battleY * 50, 710, 300);
			}
			
			//Draw only piece that survived to incidate this (phase 2)
			if(battlePhase2)
			{
				if(battle == 1 || battle == 4)
				{
					g.setColor(LIGHT_BLUE);
					g.fillRect(610, 90, 200, 200);
					g.setColor(Color.black);
					g.setFont(new Font("",Font.PLAIN, 120));
					if(myPiece.equals("10"))
						g.drawString(myPiece, 640, 230);
					else
						g.drawString(myPiece, 675, 230);
				}
				
				if(battle == 2 || battle == 5)
				{
					g.setColor(Color.red);
					g.fillRect(610, 310, 210, 200);
					g.setColor(Color.black);
					g.setFont(new Font("",Font.PLAIN, 120));
					if(oppPiece.equals("10"))
						g.drawString(oppPiece, 640, 450);
					else
						g.drawString(oppPiece, 675, 450);
				}
				
				//Draw line from square battle took place on to make it clear
				g.setColor(Color.yellow);
				g.drawLine(45 + battleX * 50, 45 + battleY * 50, 710, 300);
			}
		}
		
		public void checkToUpdateTurn()
		{
			if(startPhase == false && oppReady == true && gameStarted == false)
			{
				myTurn = true;
				gameStarted = true;
				oppReady = false;
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) 
	{
		
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		int x = e.getX() - 8;
		int y = e.getY() - 31;
		
		
		if(startPhase)
		{
			checkToRemove(x, y);
			checkToPlace(x, y);
			
			//If nothing's to be removed or placed, deselect all and see if click was to select
			deselectAll();
			selected = -2;
			
			checkToSelectPlacer(x, y);
			
			//Select piece of correct type if a piece is selected
			if(selected != -2)
			{
				selectStartPhase((selected + 2) + "");
			}
		}
		
		if(gameStarted && myTurn && !battlePhase1 && !battlePhase2)
		{
			if(pieceSelected())
			{
				for(int i = 0; i < 10; i++)
				{
					for(int i2 = 0; i2 < 10; i2++)
					{
						if(within(x, y, 20 + i * 50, 20 + i2 * 50, 50, 50))
							movePiece(i, i2);
					}
				}
				deselectAll();
			}
			else
			{
				for(int i = 0; i < 10; i++)
				{
					for(int i2 = 0; i2 < 10; i2++)
					{
						if(within(x, y, 20 + i * 50, 20 + i2 * 50, 50, 50))
							selectPiece(i, i2);
					}
				}
			}
		}
	}
	
	public void checkToRemove(int x, int y)
	{
		if(selected == 12)
		{
			for(int i = 0; i < 10; i++)
			{
				for(int i2 = 0; i2 < 10; i2++)
				{
					if(within(x, y, 20 + i * 50, 20 + i2 * 50, 50, 50))
						removePiece(i, i2);
				}
			}
		}
	}
	
	public void checkToPlace(int x, int y)
	{
		if(selected != -2 && selected != 12)
		{
			for(int i = 0; i < 10; i++)
			{
				for(int i2 = 0; i2 < 10; i2++)
				{
					if(within(x, y, 20 + i * 50, 20 + i2 * 50, 50, 50))
						placePiece(i, i2);
				}
			}
		}
	}
	
	public void checkToSelectPlacer(int x, int y)
	{
		for(int i = 0; i < 13; i++)
		{
			if(within(x, y, 25 + i * 50, 540, 40, 40))
				selected = i;
		}
	}
	
	//Check if an area is clicked
	public boolean within(int x, int y, int x2, int y2, int length, int height)
	{
		if(x > x2 && x < x2 + length && y > y2 && y < y2 + height)
			return true;
		return false;
	}
}
