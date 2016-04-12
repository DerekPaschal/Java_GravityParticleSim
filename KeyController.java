import java.awt.event.*;
import java.io.IOException;

class KeyController implements KeyListener
{
	Game game;

	KeyController(Game game) {
		this.game = game;
	}
	
	public void keyPressed(KeyEvent k)
	{
		int keyCode = k.getKeyCode();
		switch( keyCode ) 
		{ 
			case KeyEvent.VK_UP:
				this.game.cameraUp = true; 
				break;
				
			case KeyEvent.VK_DOWN:
				this.game.cameraDown = true; 
				break;
				
			case KeyEvent.VK_LEFT:
				this.game.cameraLeft = true;
				break;
				
			case KeyEvent.VK_RIGHT :
				this.game.cameraRight = true;
				break;
				
			case 88 : //'x'
				this.game.Clear();
				break;
				
			case KeyEvent.VK_1 : //'1'
				this.game.changeState(1);
				break;
				
			case KeyEvent.VK_2 : //'2'
				this.game.changeState(2);
				break;
				
			case KeyEvent.VK_3 : //'3'
				this.game.changeState(3);
				break;
				
			case 61 : //'+'
				this.game.changeSpeed(1);
				break;
				
			case 45 : //'-'
				this.game.changeSpeed(-1);
				break;
				
			case 86 : //'v'
				this.game.coloring = (game.coloring+1)%2;
				break;
				
			case 71 : //'g'
				this.game.field.grav_on = !this.game.field.grav_on;
				break;
				
			case 44 : //'<'
				this.game.field.core_count = Math.max(this.game.field.core_count-1,1);
				break;
				
			case 46 : //'>'
				this.game.field.core_count = Math.min(this.game.field.core_count+1,8);
				break;
		}
	}
	public void keyReleased(KeyEvent k)
	{
		int keyCode = k.getKeyCode();
		switch( keyCode ) 
		{ 
			case KeyEvent.VK_UP:
				this.game.cameraUp = false;
				break;
				
			case KeyEvent.VK_DOWN:
				this.game.cameraDown = false;
				break;
				
			case KeyEvent.VK_LEFT:
				this.game.cameraLeft = false;
				break;
				
			case KeyEvent.VK_RIGHT :
				this.game.cameraRight = false;
				break;
		}
	}
	public void keyTyped(KeyEvent k){}
}