import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

// Clase que compredará al cliente moviendose en la simulación
public class ClienteTienda extends JLabel{
    ClienteTienda Ref = this;
    int NoCliente,NumProducts,PosX,PosY;
    final ImageIcon img,icono[] = new ImageIcon[4];
    
    //Constructor del objeto
    public ClienteTienda(int No, int NoIcon){
        //Monito - Posición
        PosX = 1050;
        PosY = 163;
        
        icono[0]=new ImageIcon("src\\Imgs\\hs.png");
        icono[1]=new ImageIcon("src\\Imgs\\ms.png");
        icono[2]=new ImageIcon("src\\Imgs\\hc.png");
        icono[3]=new ImageIcon("src\\Imgs\\mc.png");
                
        //Numero de Cliente
        NoCliente = No;
        
        //Total de productos que tomará el cliente
        NumProducts = (int)((Math.random()*9)+1);
        
        img = new ImageIcon(icono[NoIcon].getImage().getScaledInstance(38,30,Image.SCALE_DEFAULT));
        Ref.setIcon(img);
    }
    
    //Nueva Posición del cliente...
    public void NPosicion(int x, int y){
        PosX = x;
        PosY = y;
    }
    
    //Retorno de la posición X
    public int PosX(){
        return PosX;
    }
    
    //Retorno de la posición Y
    public int PosY(){
        return PosY;
    }
}