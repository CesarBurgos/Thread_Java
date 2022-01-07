import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Trabajador extends JLabel{
    private Trabajador Ref = this;
    private ImageIcon img;
    
    public Trabajador(){
        img = new ImageIcon("src\\Imgs\\Paquete.png");
        img = new ImageIcon(img.getImage().getScaledInstance(35,35,Image.SCALE_DEFAULT));
        Ref.setIcon(img);
    }
}
