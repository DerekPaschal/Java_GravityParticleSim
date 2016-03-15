import java.awt.event.*;
import java.io.IOException;

class KeyController implements KeyListener
{
	Model model;

	KeyController(Model m) {
		this.model = m;
	}
	
	public void keyPressed(KeyEvent k)
	{
		int keyCode = k.getKeyCode();
		switch( keyCode ) 
		{ 
			case KeyEvent.VK_UP:
				model.cameraUp = true; 
				break;
				
			case KeyEvent.VK_DOWN:
				model.cameraDown = true; 
				break;
				
			case KeyEvent.VK_LEFT:
				model.cameraLeft = true;
				break;
				
			case KeyEvent.VK_RIGHT :
				model.cameraRight = true;
				break;
				
			case 88 : //'x'
				this.model.Clear();
				this.model.ClearNotAdded();
				break;
				
			case KeyEvent.VK_1 : //'1'
				model.changeState(1);
				break;
				
			case KeyEvent.VK_2 : //'2'
				model.changeState(2);
				break;
				
			case 61 : //'+'
				model.changeSpeed(1);
				break;
				
			case 45 : //'-'
				model.changeSpeed(-1);
				break;
				
			case 86 : //'v'
				model.vel_color = !model.vel_color;
				break;
		}
	}
	public void keyReleased(KeyEvent k)
	{
		int keyCode = k.getKeyCode();
		switch( keyCode ) 
		{ 
			case KeyEvent.VK_UP:
				model.cameraUp = false;
				break;
				
			case KeyEvent.VK_DOWN:
				model.cameraDown = false;
				break;
				
			case KeyEvent.VK_LEFT:
				model.cameraLeft = false;
				break;
				
			case KeyEvent.VK_RIGHT :
				model.cameraRight = false;
				break;
		}
	}
	public void keyTyped(KeyEvent k){}
}