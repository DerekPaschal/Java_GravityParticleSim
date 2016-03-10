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
				this.model.need_to_clear = true;
				break;
				
			case KeyEvent.VK_1 : //'1'
				this.model.need_to_clear = true;
				this.model.state = 1;
				break;
			case KeyEvent.VK_2 : //'2'
				this.model.need_to_clear = true;
				this.model.state = 2;
				double new_size = 4;
				//double new_mass = 2 * 3.14 * new_size * new_size * model.density; //Mass dependent on Area of Circle
				double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) * model.density * 0.8);
				this.model.createPartDisk(100*100,200,false,true, new Vec3(model.window.x/2,model.window.y/2),
													new_size, 2* new_mass, true, 0.15, new Vec3(250,250,250));
				
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