import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.io.IOException;

class MouseController implements MouseListener
{
	Model model;

	MouseController(Model m) {
		this.model = m;
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1)
			this.model.OnClick(e.getX(), e.getY());
		if (e.getButton() == MouseEvent.BUTTON3)
			this.model.RightClick(e.getX(), e.getY());
	}
	
	public void mouseReleased(MouseEvent e) 
	{    
		if (e.getButton() == MouseEvent.BUTTON1)
			this.model.ClickRelease(e.getX(), e.getY());
		if (e.getButton() == MouseEvent.BUTTON3)
			this.model.RightRelease(e.getX(), e.getY());
	}
	public void mouseEntered(MouseEvent e) {    }
	public void mouseExited(MouseEvent e) {    }
	public void mouseClicked(MouseEvent e) {    }

}
