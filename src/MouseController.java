import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.io.IOException;

class MouseController implements MouseListener
{
	Game game;

	MouseController(Game game) {
		this.game = game;
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1)
			this.game.OnClick(e.getX(), e.getY());
		if (e.getButton() == MouseEvent.BUTTON3)
			this.game.RightClick(e.getX(), e.getY());
	}
	
	public void mouseReleased(MouseEvent e) 
	{    
		if (e.getButton() == MouseEvent.BUTTON1)
			this.game.ClickRelease(e.getX(), e.getY());
		if (e.getButton() == MouseEvent.BUTTON3)
			this.game.RightRelease(e.getX(), e.getY());
	}
	public void mouseEntered(MouseEvent e) {    }
	public void mouseExited(MouseEvent e) {    }
	public void mouseClicked(MouseEvent e) {    }

}
