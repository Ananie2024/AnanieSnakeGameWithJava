package com.ananie.snake;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Button;

import java.util.LinkedList;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SnakeGame extends Application{
	private static final int TILE_SIZE = 15;
	private static final int WIDTH=30;
	private static final int HEIGHT=30;
	private static final int WINDOW_WIDTH=WIDTH*TILE_SIZE;
	private static final int WINDOW_HEIGHT=HEIGHT*TILE_SIZE;
    private LinkedList<Point> snake= new LinkedList<Point>();
    private Direction direction =Direction.RIGHT;
    private AnimationTimer timer;
    private Point food;
    private int score=0;
    private Label scoreLabel = new Label("YOUR SCORE: " + score);
    private Button pauseButton = new Button("PAUSE");
    private Button restartButton = new Button("START");
    private Button resetButton = new Button("RESET");
    private boolean isPaused = true;
    private VBox welcomePane;
    
	@Override
	public void start(Stage primaryStage) throws Exception {
		Canvas canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
		GraphicsContext drw =canvas.getGraphicsContext2D();
		BorderPane pane = new BorderPane();
		HBox buttons = new HBox(10,restartButton,pauseButton,resetButton);
		buttons.setAlignment(Pos.CENTER);
		
		scoreLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: darkblue;");
		// the welcome screen
		
		
		Label welcomeLabel = new Label("WELCOME TO ANANIE'SNAKE GAME");
		welcomeLabel.setStyle("-fx-font-size: 25px; -fx-text-fill:darkviolet;");
		Label instructions = new Label("1. use Arrow Keys.\n\n"
				+ "2.avoid hitting the walls.\n\n"
				+ "3.avoid self-eating.\n\n"
				+"click START THE GAME button to begin");
		instructions.setStyle("-fx-font-size: 16px; fx-text-alignement: center;");
		Button startButton = new Button("START THE GAME");
		welcomePane = new VBox(30,welcomeLabel,instructions,startButton);
		welcomePane.setAlignment(Pos.CENTER);
		pane.setCenter(welcomePane);
		// the start button  removes the welcome pane and sets the gameboard and the canvas
		startButton.setOnAction(e->{
			pane.setCenter(canvas);
			pane.setTop(scoreLabel); 
			pane.setBottom(buttons);
		 
		});
		

		// let's manage invalid keys using pause transition timer
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setContentText("Invalid Key pressed");
		PauseTransition delay = new PauseTransition(Duration.seconds(2));
	
		Scene scene = new Scene(pane,600,600);
		scene.setFill(Color.LIGHTCYAN);
		// let's make the initial snake with these points
		snake.add(new Point(6,7)); // head
		snake.add(new Point(5,7));
		snake.add(new Point(4,7));
		snake.add(new Point(3,7)); // tail

	   spawnFood();
	   //let's handle user keyboards actions controlling movements of the snake
	   scene.setOnKeyPressed((KeyEvent event)->{
		 			    switch (event.getCode()) {
			        case UP:
			            if (direction != Direction.DOWN) {
			                  direction = Direction.UP;
			            }
			            break;
			        case DOWN:
			            if (direction != Direction.UP) {
			                direction = Direction.DOWN;
			            }
			            break;
			        case RIGHT:
			            if (direction != Direction.LEFT) {
			                direction = Direction.RIGHT;
			            }
			            break;
			        case LEFT:
			            if (direction != Direction.RIGHT) {
			                direction = Direction.LEFT;
			            }
			            break;
			        default:// when the user clicks an invalid key
			            alert.show();
			            delay.setOnFinished(event1 -> alert.close());
			            delay.play();
			            break;
			    }
			});
	   pauseButton.setOnAction(e-> { 
		   if(isPaused) {
			   timer.start();
			   isPaused=false;
		       pauseButton.setText("PAUSE");
		       } else { 
			   timer.stop();
		       pauseButton.setText("RESUME");  
		       isPaused=true;
		   }
		   pane.requestFocus();
});
	   restartButton.setOnAction(e-> { if( isPaused) {
	   timer.start();
	   pauseButton.setText("PAUSE");
	   isPaused=false; 
	   pauseButton.setDisable(false);}
	   pane.requestFocus();
});
	   
	   resetButton.setOnAction(e-> {
		   timer.stop();
		   isPaused=true;
		   pauseButton.setText("PAUSE");
		   resetGame();
	   pane.requestFocus();
 });

		
	 // let's handle the constant redrawing of the snake due to user directions using Animation timer
	 timer =  new AnimationTimer() {
		   long lastUpdate =0;
		   @Override
		 public void handle(long now){
			   if(now-lastUpdate>200_000_000) {
				   update();
	                           draw(drw);
				   lastUpdate=now; 
	   }  
	 }
  };	
	   primaryStage.setScene(scene);
	   primaryStage.setTitle("ANANIE SNAKE GAME");
	   primaryStage.show();
	   pane.setFocusTraversable(true);
	   pane.requestFocus();
	}	
private void resetGame() {
		snake.clear();
		snake.add(new Point(6,7)); 
		snake.add(new Point(5,7));
		snake.add(new Point(4,7));
		snake.add(new Point(3,7)); 
        score=0;
        scoreLabel.setText("YOUR SCORE :" + score);
        spawnFood();
        direction=Direction.RIGHT;
        pauseButton.setText("PAUSE");
        restartButton.setDisable(false);
       
	}
// this method redraws the snake whenever the direction is changed by the user
public void update() {
	Point head = snake.get(0);
	Point newHead = switch(direction) {
	case UP-> new Point(head.x,head.y-1);
	case DOWN ->  new Point(head.x,head.y+1);
	case RIGHT -> new Point(head.x+1,head.y);
	case LEFT->new Point(head.x-1, head.y);
	};
	//when the snake collide with the edges of the canvas
	for(Point p: snake) {if(p.x<=0||p.y<=0||p.x>=(int)WIDTH||p.y>=(int)HEIGHT) {		
		     gameOver("you've hit the wall");
		     return;
		     }
		  }
	
	// check self-collision
	if(snake.subList(1,snake.size()).contains(snake.get(0))) { gameOver("self-collision ");
	return;}
	//eat and grow
	snake.addFirst(newHead);
	if(newHead.equals(food)) {
		score++;
		scoreLabel.setText("YOUR SCORE:" + score);
		spawnFood();
	} else {
	snake.removeLast(); }
}
// this method draws the snake by taking the Points in the LinkedList and giving the rectangular form and the Points spawned an oval form
public void draw(GraphicsContext dr) {
	// clean the canvas
	dr.setFill(Color.BLUEVIOLET);
    dr.fillRect(0,0,WINDOW_WIDTH,WINDOW_HEIGHT);
    // draw the snake 
    dr.setFill(Color.YELLOWGREEN);
   // iterate through all points in the snake linkedlist
    for(Point h: snake) {
    	dr.fillRect(h.x*TILE_SIZE, h.y*TILE_SIZE,TILE_SIZE, TILE_SIZE);
    	    }
   // let's draw the food
    dr.setFill(Color.CADETBLUE);
    dr.fillOval(food.x*TILE_SIZE, food.y*TILE_SIZE,TILE_SIZE,TILE_SIZE);
   }
public void gameOver(String reason) {
	timer.stop();
	resetGame();
	isPaused= true;
	restartButton.setDisable(true);
	pauseButton.setDisable(true);
	
	Alert alert =new Alert(Alert.AlertType.INFORMATION);
	alert.setTitle("GAME OVER");
	alert.setContentText("Sorry,you failed" + reason);
	PauseTransition pause = new PauseTransition(Duration.seconds(2));
	pause.setOnFinished(e->alert.close());
	alert.show();
	pause.play();
    }
	// this method  displays a oval food at any area on the canvas using java random integers
	// when the generated Point is already on the snake, the method keeps generating other Points
public void spawnFood() {
 Point newFood;// 
 Random rand = new Random(); 
 do { 
	 int x=rand.nextInt(WIDTH);
	 int y=rand.nextInt(HEIGHT);
	 newFood= new Point(x,y);
 } while(snake.contains(newFood));
 food=newFood;
}
public static void main(String[]args) {
	launch(args);
}
}
