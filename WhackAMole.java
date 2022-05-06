package project6;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class WhackAMole extends Application
{
	private int molesWhacked = 0;
	private int molesMissed = 0;
	private Button field[][] = new Button[10][10];
	private int moleX;
	private int moleY;
	private long previousTime = System.currentTimeMillis();
	private long missTime = 2000;
	private Random randomNumber = new Random();
	private Label hitScore;
	private Label missedScore;
	private Label highScore;
	private long highPlayTime = 0;
	private long lastHitTime= 0;
	private String highPlayTimeFormatted;	
	private long startTime = System.currentTimeMillis();
	private java.io.File file = new java.io.File("scores.txt");

	/**
	 * Setting the stage
	 */
	@Override
	public void start(Stage primaryStage) throws Exception 
	{
		//Creates grid playing field
		GridPane fieldPane = new GridPane();
		FieldHandlerClass fieldHandler = new FieldHandlerClass();
		for(int i = 0; i < 10; i++)
		{
			for(int j = 0; j < 10; j++)
			{
				field[i][j] = new Button("  ");
				fieldPane.add(field[i][j], i, j);
				field[i][j].setOnAction(fieldHandler);
			}
		}
		fieldPane.setAlignment(Pos.CENTER);
		newMole();
		
		//Creates the top pane with current track of hits, misses, and high score time
		openHighScores();
		HBox topPane = new HBox();
		hitScore = new Label("Whacked: " + molesWhacked);
		missedScore = new Label("Missed: " + molesMissed);
		highPlayTimeFormatted = formatHighScore(highPlayTime);
		highScore = new Label("High Score: " + highPlayTimeFormatted);
		topPane.getChildren().add(hitScore);
		topPane.getChildren().add(new Label(" "));
		topPane.getChildren().add(missedScore);
		topPane.getChildren().add(new Label(" "));
		topPane.getChildren().add(highScore);
		topPane.setAlignment(Pos.CENTER);
		
		//Sets up the bottom box with the reset button
		HBox bottomPane = new HBox();
		Button btReset = new Button("Reset");
		ResetHandlerClass resetHandler = new ResetHandlerClass();
		btReset.setOnAction(resetHandler);
		bottomPane.getChildren().add(btReset);
		bottomPane.setAlignment(Pos.CENTER);
		
		//Sets up the full window
		BorderPane pane = new BorderPane();
		pane.setTop(topPane);
		pane.setBottom(bottomPane);
		pane.setCenter(fieldPane);
		
		//Sets the scene
		Scene scene = new Scene(pane, 260, 300);
		primaryStage.setTitle("Whac-A-Mole");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	/**
	 * Bit needed for the IDE to run it
	 */
	public static void main(String[] args) 
	{
	    launch(args);
	}
	
	/**
	 * Opens the high score file and sets the high score time if the file exists
	 */
	private void openHighScores() 
	{
		if(file.exists())
		{
			try(Scanner input = new Scanner(file))
			{
				highPlayTime = input.nextLong();
			}
			catch(FileNotFoundException ex)
			{
				System.out.println("You're encountering the FNF exception, which shouldn't be possible.");
			}
		}
		return;
	}
	
	/**
	 * Writes the high score time to the high score file.
	 */
	private void saveHighScore()
	{
		boolean saving = true;
		while(saving)
		{
			try(PrintWriter outWriter = new PrintWriter(file))
			{
				outWriter.write("" + highPlayTime);
				saving = false;
			}
			catch (FileNotFoundException e)
		    {
				try  
			    {
					file.createNewFile();
			    }
			    catch (IOException IOe)
			    {
			    	System.out.println("Error creating high score file.");
			    }
		    }
		}
	}
	
	/**
	 * Chooses a new mole location at random
	 */
	private void newMole()
	{
		this.moleX = randomNumber.nextInt(10);
		this.moleY = randomNumber.nextInt(10);
		field[moleX][moleY].setStyle("-fx-background-color: #783c00; ");
	}
	
	/**
	 * Resets the game to the starting state.
	 */
	private void resetGame() 
	{
		this.molesWhacked = 0;
		this.molesMissed = 0;
		hitScore.setText("Whacked: " + molesWhacked);
		missedScore.setText("Missed: " + molesMissed);
		field[moleX][moleY].setStyle("");
		this.missTime = 2000;
		this.lastHitTime= 0;
		newMole();
		this.previousTime = System.currentTimeMillis();
	}
	
	/**
	 * Calculates whether clicking on the mole counts as a hit or a miss
	 */
	private void calculateHit() 
	{
		long currentTime = System.currentTimeMillis();
		
		if(currentTime - previousTime <= missTime)
		{
			//Increments moles whacked, keeps track of current hit time, and decrements time to hit
			this.molesWhacked += 1;
			lastHitTime = currentTime;
			hitScore.setText("Whacked: " + molesWhacked);
			this.missTime += missTime >= 500 ? -50 : 0;
		}
		else
		{
			//Soft resets score and time, calculates new high score
			this.molesMissed += 1;
			this.molesWhacked = 0;
			missedScore.setText("Missed: " + molesMissed);
			hitScore.setText("Whacked: " + molesWhacked);
			calculateHighScore();
			this.missTime = 2000;
			
			this.startTime = System.currentTimeMillis();
		}
		
		newMole();
		previousTime = currentTime;		
	}
	
	/**
	 * Determines if the time as of a miss is the high score, and formats it correctly for display if it is. 
	 * Saves the high score if it is new.
	 */
	private void calculateHighScore()
	{
		if(lastHitTime - startTime > highPlayTime)
		{
			this.highPlayTime = lastHitTime - startTime;
			saveHighScore();
			highPlayTimeFormatted = formatHighScore(highPlayTime);
			highScore.setText("High Score: " + highPlayTimeFormatted);
		}
	}
	
	/**
	 * Formats the high score
	 * @param playTime the high score time in ms
	 * @return the high score time in mm:ss:ms
	 */
	private String formatHighScore(long playTime) 
	{
		int minutes = 0;
		int seconds = 0;
		String result = "";
		
		//Calculates minutes. If this is ever hit I'm impressed
		while(playTime >= 60000)
		{
			minutes += 1;
			playTime += -60000;
		}
		//Calculates seconds
		while(playTime >= 2000)
		{
			seconds += 1;
			playTime += -2000;
		}
		
		//Adds a leading 0 if the minutes are under 2 digits
		if(minutes < 10)
		{
			result += "0" + minutes;
		}
		else if(minutes >= 100)
		{
			//This one is just so that the formatting of the window doesn't get screwed
			//up by a three digit minute
			result = "99:99:9999";
			return result;
		}
		else
		{
			result += minutes;
		}
		
		result += ":"; //formating
		
		//Adds a leading 0 if the minutes are under 2 digits
		if(seconds < 10)
		{
			result += "0" + seconds;
		}
		else
		{
			result += seconds;
		}
		
		result += ":"; //formatting
		
		//Makes sure milliseconds are in 0000 style
		if(playTime >= 1000)
		{
			result += playTime;
		}
		else
		{
			if(playTime >= 100)
			{
				result += "0" + playTime;
			}
			else
			{
				if(playTime >= 10)
				{
					result += "00" + playTime;
				}
				else
				{
					result += "000" + playTime;
				}
			}
		}
		return result;
	}

	/**
	 * Handler for if the mole button is clicked
	 *
	 */
	class FieldHandlerClass implements EventHandler<ActionEvent> 
	{   
		@Override   
		public void handle(ActionEvent e) 
		{      
			for (int i = 0; i < 10; i++) 
			{
				for(int j = 0; j < 10; j++)
				{
					if (e.getSource() == field[i][j]) 
					{
						if(i == moleX && j == moleY)
						{
							field[i][j].setStyle("");
							calculateHit();
						}
					}
				}       
			}    
		}
	}
	
	/**
	 * Handler for if the reset button is clicked
	 *
	 */
	class ResetHandlerClass implements EventHandler<ActionEvent> 
	{   
		@Override   
		public void handle(ActionEvent e) 
		{      
			resetGame();
		}
	}
}
