import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;

//Para servidor
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

//Para clip
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * Clase monitor.
 * Cuenta con las funciones que serán llamadas por los hilos...
 */
class Monitor{
    int aux,NumCaj,Caja;    
    boolean Recepcion;//Recepcion de clientes = True
    boolean Caj1,Caj2,Caj3, esperar;
    Ejec_S RefVent;
    private ClienteTienda CltCaja1,CltCaja2,CltCaja3;
    boolean Surt0,Surt1,Surt2,Surt3,Surt4,Surt5;
    
    public Monitor(Ejec_S Vent){    
        RefVent = Vent;
        esperar = true;
        Caj1 = false;
        Caj2 = false;
        Caj3 = false;
        
        Surt0 = false;
        Surt1 = false;
        Surt2 = false;
        Surt3 = false;
        Surt4 = false;
        Surt5 = false;
    }
    
    //Información del Servidor
    public synchronized void MensjServer(String Mensaje){
        RefVent.InfoLinea.setText(RefVent.InfoLinea.getText()+Mensaje);
        RefVent.InfoLinea.setCaretPosition(RefVent.InfoLinea.getDocument().getLength());
    }
    
    //Movimiento inicial de cliente(Local)...
    public synchronized void MovCltIn(ClienteTienda Clt, int x, int y){
        Clt.setLocation(x,y);
    }
    
    //Método que realizará la impresión de la actividad de los clientes
    public synchronized void MensajeMovClt(String Mensaje){
        RefVent.InfoTienda.setText(RefVent.InfoTienda.getText()+Mensaje); 
        RefVent.InfoTienda.setCaretPosition(RefVent.InfoTienda.getDocument().getLength());
    }
    
    //Cambio de productos disponibles
    public synchronized void CambioProducts(int NumEstante){
        RefVent.ProductDisp[NumEstante] = RefVent.ProductDisp[NumEstante]-1;
        RefVent.Dats[NumEstante].setText(""+RefVent.ProductDisp[NumEstante]);
    }
    
    //Existencia de productos - ¿el cliente puede consumir?
    public synchronized boolean ExistProducts(int NumEstante){
        aux = RefVent.ProductDisp[NumEstante];
        if(aux == 1)
            return false;
        else
            return true;
    }
    
    public synchronized int Disponibilidad(int NumEstante){
        return RefVent.ProductAlm[NumEstante];
    }
    
    //Cambio de productos disponibles (Almacen)
    public synchronized void CambProductsAlm(int NumEstante,int cant) throws InterruptedException{
        RefVent.ProductAlm[NumEstante] = RefVent.ProductAlm[NumEstante]-cant;
        
        if(cant!=0){
            RefVent.ProductDisp[NumEstante] = cant;
            RefVent.Dats[NumEstante].setText(""+cant); //Actalizando disponibles
        }
        
        RefVent.Dats2[NumEstante].setText(""+RefVent.ProductAlm[NumEstante]); //Actualizando Almacen
    }
    
    //Función que validará el estado de las cajas
    public synchronized void ComprobCajs(ClienteTienda Clt) throws InterruptedException{
        while(true){
            while(true){
                //System.out.println("Cliente: "+Clt.NoCliente+" esperando...");
                wait();
                break;
            }
        
            if((!Caj1 && !Caj2 && !Caj3) || (!Caj1 && Caj2 && Caj3)){
                CltCaja1=Clt;
                Caj1 = true;
                Caja = 1;
                break;
            }else if((Caj1 && !Caj2 && Caj3) || (Caj1 && !Caj2 && !Caj3)){
                CltCaja2=Clt;
                Caj2 = true;
                Caja = 2;
                break;
            }else if(Caj1 && Caj2 && !Caj3){
                CltCaja3=Clt;
                Caj3 = true;
                Caja = 3;
                break;
            }
        }
        
        //System.out.println("Cliente: "+Clt.NoCliente+" debe pasar a: "+Caja);
    }
    
    public synchronized ClienteTienda Cobrar1() throws InterruptedException{
        if(!Caj1)
            notify();
        
        return CltCaja1;
    }
    
    public synchronized ClienteTienda Cobrar2() throws InterruptedException{
        if(!Caj2)
            notify();
        
        return CltCaja2;
    }
        
    public synchronized ClienteTienda Cobrar3() throws InterruptedException{
        if(!Caj3)
            notify();
        
        return CltCaja3;
    }
    
    public synchronized void ResetClt(int No){
        switch (No){
            case 1:
                CltCaja1=null;
            break;
            
            case 2:
                CltCaja2=null;
            break;
            
            case 3:
                CltCaja3=null;
            break;
        }
    }
    
    //Función que liberará o indicará cómo libre una caja
    public synchronized void CajaLibre(int NumCaja){
        switch (NumCaja) {
            case 1:
                Caj1 = false;
                RefVent.Cj1.setBackground(Color.GREEN);
            break;
            
            case 2:
                Caj2 = false;
                RefVent.Cj2.setBackground(Color.GREEN);
            break;
            
            case 3:
                Caj3 = false;
                RefVent.Cj3.setBackground(Color.GREEN);
            break;
        }
    }
    
    //Elimación del cliente de la tienda...
    public synchronized void ElimClient(ClienteTienda Clt){
        //Quitando cliente del panel
        RefVent.TiendaPanel.remove(Clt);
        RefVent.TiendaPanel.repaint();
        
        //Quitando cliente de la lista de clientes
        RefVent.Clts.remove(Clt);
        
        //Llamar al actualizar del numero de clientes
        ActNumClts(false);
    }
    
    //Numero de clientes
    public synchronized void ActNumClts(boolean band){
        if(!band){
            //Restando el cliente que se ha retirado...
            RefVent.TClients = RefVent.TClients-1;
        }else{
            RefVent.TClients = RefVent.TClients+1;
        }
        
        RefVent.TClient.setText(""+RefVent.TClients);
    }
    
    //Numero de clientes2
    public synchronized void ActNumClts2(String cad){
        RefVent.TClients = RefVent.TClients+1;
        RefVent.TClient.setText(""+RefVent.TClients);        
        RefVent.InfoTienda.setText(RefVent.InfoTienda.getText()+cad);
        RefVent.InfoTienda.setCaretPosition(RefVent.InfoTienda.getDocument().getLength());
    }
}

/**
 * ServidorActv.
 * Hilo que activará al servidor y atenderá a todo cliente que ingrese...
*/
class ServidorActv extends Thread{	
    final Monitor Cntrl;
    //Para servidor
    ServerSocket TiendaLinea = null;
    Socket Clientex = null;

    public ServidorActv(Monitor Ctrl){
        this.Cntrl = Ctrl;
    }
    
    //Es un hilo que llama a la conexión con el servidor
    @Override
    public void run(){
        try{        
            Cntrl.MensjServer(" ==== Tienda online: Activada ====");
            try{
                TiendaLinea = new ServerSocket(5432);
                while(true){    	
                    Clientex = TiendaLinea.accept();
                    Cntrl.RefVent.TClientesL =  Cntrl.RefVent.TClientesL+1;
                    (new AtencionCltsL(Cntrl.RefVent, Clientex,Cntrl.RefVent.TClientesL,Cntrl.RefVent.InfoLinea)).start();
                    Cntrl.RefVent.TClientOn.setText(""+Cntrl.RefVent.TClientesL);
                    Cntrl.MensjServer("\n Cliente No. "+Cntrl.RefVent.TClientesL+" ---- IP: "+Clientex.getInetAddress());
                }
            }catch (IOException e){
                //InfoLinea.setText(InfoLinea.getText()+"\n ********** C\n "+e.getMessage());
                System.out.println("Posible error en la conexión con cliente: "+e.getMessage());
                //Cntrl.RefVent.InfoLinea.setCaretPosition(Cntrl.RefVent.InfoLinea.getDocument().getLength());
            }
        }catch(Exception ex){
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}

/**
 * AtencionCltsL.
 * Hilo que se encuentra atendiendo al cliente que ingresó...
*/
class AtencionCltsL extends Thread {
    BufferedReader DelCliente;
    DataOutputStream AlCliente;
    String InfDelCliente,NombClt;
    Socket Comunicacion;
    int NoClt,i,auxNum; //No. de cliente
    JTextArea InfoLinea;
    Ejec_S Ref;
    boolean band=false; 
    
    String aux, Prodcts[],MaxProdctAlm;
    
    public AtencionCltsL(Ejec_S RefVent,Socket Comunicacion2, int No, JTextArea InfLinea){
        InfoLinea = InfLinea;
        Ref = RefVent; // Referencia a la ventana
        
        //Maximo de productos en almacen
        MaxProdctAlm = "";
        for (int Prd: Ref.ProductAlm)
            MaxProdctAlm = MaxProdctAlm+"-"+Prd;
        
        //Estableciendo conexión con el cliente
        try{
            NoClt=No;
            Comunicacion=Comunicacion2;
            DelCliente = new BufferedReader(new InputStreamReader(Comunicacion.getInputStream()));
   	    AlCliente = new DataOutputStream(Comunicacion.getOutputStream());
        }catch (IOException e){
            InfoLinea.setText(e.getMessage());
            InfoLinea.setCaretPosition(InfoLinea.getDocument().getLength());
        }
    }
       
    @Override
    public void run(){
        try{ 
            //Se rompe si envia un espacio vacio o null
            while(true){
   	        InfDelCliente = DelCliente.readLine();
                
                if(!band){
                    band=!band;
                    NombClt = InfDelCliente;
                    InfoLinea.setText(InfoLinea.getText()+"\n > Nombre: "+NombClt);
                    InfoLinea.setCaretPosition(InfoLinea.getDocument().getLength());
                    //Enviando el maximo de productos en almacen
                    AlCliente.writeUTF(MaxProdctAlm);
                }
                
                if(InfDelCliente.equals("X")){
                    break;
                }else{
                    if(InfDelCliente.length()==12 && (InfDelCliente.charAt(0)=='-') && (InfDelCliente.charAt(10)=='-')){
                        //System.out.println("Pedido"+InfDelCliente);
                        
                        //Para servidor
                        Prodcts = InfDelCliente.split("-");
                        aux="\n\n El cliente ("+NoClt+") solicito productos del almacén:";
                        
                        for(i=1; i<Prodcts.length; i++){
                            if(!Prodcts[i].equals("0")){
                                aux=aux+"\n  Del ["+(i)+"] se restan: "+Prodcts[i];
                            }
                            
                            Ref.ProductAlm[i-1] = Ref.ProductAlm[i-1]-(Integer.parseInt(Prodcts[i]));
                        }
                        ActAlm(); //Actualizar almacén
                                //Maximo de productos en almacen
                        MaxProdctAlm = "";
                        for (int Prd: Ref.ProductAlm)
                            MaxProdctAlm = MaxProdctAlm+"-"+Prd;
                        AlCliente.writeUTF(MaxProdctAlm);
                        InfoLinea.setText(InfoLinea.getText()+aux);
                        InfoLinea.setCaretPosition(InfoLinea.getDocument().getLength());
                    }
                }
            }
            
            InfoLinea.setText(InfoLinea.getText()+"\n\n ---- Se ha ido el "+NoClt+"° cliente ----");
            Ref.TClientesL = Ref.TClientesL-1;
            Ref.TClientOn.setText(""+Ref.TClientesL);
            
            DelCliente.close();
            AlCliente.close();
   	    Comunicacion.close();
        }catch (IOException e){
            InfoLinea.setText(InfoLinea.getText()+e.getMessage());
        }
        InfoLinea.setCaretPosition(InfoLinea.getDocument().getLength());
    }
    
    //Actualizando valores del almacén
    public void ActAlm(){
        Ref.A1.setText(""+Ref.ProductAlm[0]);
        Ref.A2.setText(""+Ref.ProductAlm[1]);
        Ref.A3.setText(""+Ref.ProductAlm[2]);
        Ref.A4.setText(""+Ref.ProductAlm[3]);
        Ref.A5.setText(""+Ref.ProductAlm[4]);
        Ref.A6.setText(""+Ref.ProductAlm[5]);
    }
}

/**
 * MovIni.
 * Hilo que controlará el movimiento inicial...
 */
class MovIni extends Thread {
    final ClienteTienda Client;
    final Monitor Cntrl;
    private MovClts MovsClt;
    final int NoClient;
    int x,y,aux,tiempo=200;
    
    public MovIni(Monitor Ctrl, ClienteTienda Clts, int No, int ValX){
        this.x = ValX;
        this.Client = Clts;
        this.Cntrl = Ctrl;
        this.NoClient = No;
    }
    
    @Override
    public void run(){
        Cntrl.MensajeMovClt("\n Esta entrado el "+NoClient+"° cliente a la tienda...");
        while(true){
            try{
                x-=25;
                Cntrl.MovCltIn(Client,x,165);
                Thread.sleep(tiempo);

                if(x == 750)
                    break;
            } catch (InterruptedException ex) {
                Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Client.NPosicion(x, 165);

        //Iniciando hilo de movimientos aleatorios....
        MovsClt = new MovClts(NoClient, Cntrl, Client);
        MovsClt.start();
    }
}

/**
 * MovClts.
 * Hilo que controlará el movimiento de los clientes en la tienda...
 */
class MovClts extends Thread {
    final ClienteTienda Clt; //Cliente
    final Monitor Cntrl;
    private int NoClt,x,y,NumProdcts,tiempo = 200,aux,existencia;
    final MovClts Ref;
    private Pago P;
    private Surtidor S0,S1,S2,S3,S4,S5;
    
    public MovClts(int No, Monitor Ctrl, ClienteTienda Cl){
        this.NoClt = No;
        this.Clt = Cl;
        this.Cntrl = Ctrl;
        Ref = this;
    }
    
    @Override
    public void run(){        
        x=Clt.PosX();
        y=Clt.PosY();
        
        //Aleatorio inicio...
        aux = ((int)(Math.random()*3));
        switch(aux){
            case 1:
                //Moviendo a Blancos y hogar
                while(y != 270){
                    try {
                        y+=15;
                        Cntrl.MovCltIn(Clt,x,y);
                        Thread.sleep(tiempo);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            break;

            case 2:
                //Moviendo a Pan y cereales
                while(y != 390){
                    try{
                        y+=25;
                        Cntrl.MovCltIn(Clt,x,y);
                        Thread.sleep(tiempo);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            break;
        }

        while(x != 550){
            try {
                x-=25;
                Cntrl.MovCltIn(Clt,x,y);
                Thread.sleep(tiempo);
            } catch (InterruptedException ex) {
                Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         
        //Extracción de productos que consumirá el cliente
        NumProdcts = Clt.NumProducts;
        
        while(NumProdcts != 0){
            //Validando en que estante o "departamento" se encuentra el cliente
            if(x == 550){
                switch (y) {
                    case 165:
                        //--- Esta en Farmacia [3]
                        NumProdcts--; //Tomando un producto
                        
                        //Comprobando existencia de productos
                        if(Cntrl.ExistProducts(3)){
                            Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a FARMACIA [4]...");
                            Cntrl.CambioProducts(3);
                        }else{
                            try {
                                //Comprobando si en almacen hay disponibilidad...
                                existencia = Cntrl.Disponibilidad(3);
                                if(existencia>=15){
                                    if(!Cntrl.Surt3){
                                        //Inicia hilo que surte 15 productos...
                                        Cntrl.MensajeMovClt("\n > Surtiedo a FARMACIA [4] (+15)...");
                                        Cntrl.Surt3=true;
                                        //Llamar a hilo...
                                        S3 = new Surtidor(Cntrl,670, 450, 3, 15,Ref);
                                        S3.start();
                                        //Cntrl.CambProductsAlm(3,15);
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a FARMACIA [4]...");
                                        //Cntrl.CambioProducts(3);
                                    }else{
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de FARMACIA [4]...");
                                        Cntrl.CambProductsAlm(3,0);
                                    }
                                }else if(existencia <= 0){
                                    //No se puede surtir... Si es el último termina producto...
                                    Cntrl.MensajeMovClt("\n == No puede ser surtida FARMACIA [4]...");
                                    Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de FARMACIA [4]...");
                                    Cntrl.CambProductsAlm(3,0);
                                }else{
                                    //Inicia hilo que surte 1o que hay...
                                    Cntrl.MensajeMovClt("\n > Surtiendo a FARMACIA [4] (+"+existencia+")...");
                                    if(!Cntrl.Surt3){
                                        Cntrl.Surt3=true;
                                        S3 = new Surtidor(Cntrl,670, 450, 3, existencia,Ref);
                                        S3.start();
                                        //Cntrl.CambProductsAlm(3,existencia);

                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a FARMACIA [4]...");
                                        //Cntrl.CambioProducts(3);
                                    }else{
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de FARMACIA [4]...");
                                        Cntrl.CambProductsAlm(3,0);
                                    }
                                }
                            } catch (InterruptedException ex) {
                                Logger.getLogger(MovClts.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        
                        if(NumProdcts==0){
                            break;
                        }
                        
                        //---- Determinando nueva ruta
                        aux = (int)(Math.random()*3);
                        switch(aux){
                            case 1:
                                //Moviendo a Frutas y vegetales
                                while(x != 200){
                                    try {
                                        x-=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            case 2:
                                //Moviendo a Blancos y hogar
                                while(y != 270){
                                    try {
                                        y+=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            default:
                                //Moviendo a Lácteos
                                while(x != 400){
                                    try{
                                        x-=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }        
                                        
                                while(y != 270){
                                    try {
                                        y+=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                            
                                while(x != 200){
                                    try {
                                        x-=20;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                        }
                    break;
                    
                    case 270:
                        //--- Esta en Blancos y hogar [4]
                        NumProdcts--; //Tomando un producto
                        
                        if(Cntrl.ExistProducts(4)){
                            Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a BLANCOS Y HOGAR [5]...");
                            Cntrl.CambioProducts(4);
                        }else{
                            try { 
                                //Comprobando si en almacen hay disponibilidad...
                                existencia = Cntrl.Disponibilidad(4);
                                if(existencia>=15){
                                    //Inicia hilo que surte 15 productos...
                                    Cntrl.MensajeMovClt("\n > Surtiedo a BLANCOS Y HOGAR [5] (+15)...");
                                    if(!Cntrl.Surt4){
                                        Cntrl.Surt4=true;
                                        //Cntrl.CambProductsAlm(4,15);
                                        S4 = new Surtidor(Cntrl,670, 450, 4, 15,Ref);
                                        S4.start();

                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a BLANCOS Y HOGAR [5]...");
                                        //Cntrl.CambioProducts(4);
                                    }else{
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de BLANCOS Y HOGAR [5]...");
                                        Cntrl.CambProductsAlm(4,0);
                                    }
                                }else if(existencia <= 0){
                                    //No se puede surtir... Si es el último termina producto...
                                    Cntrl.MensajeMovClt("\n == No puede ser surtida BLANCOS Y HOGAR [5]...");
                                    Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de BLANCOS Y HOGAR [5]...");
                                    Cntrl.CambProductsAlm(4,0);
                                }else{
                                    //Inicia hilo que surte 1o que hay...
                                    Cntrl.MensajeMovClt("\n > Surtiendo a BLANCOS Y HOGAR [5] (+"+existencia+")...");
                                    if(!Cntrl.Surt4){
                                        Cntrl.Surt4=true;
                                        //Cntrl.CambProductsAlm(4,existencia);
                                        S4 = new Surtidor(Cntrl,670, 450, 4, existencia,Ref);
                                        S4.start();

                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a BLANCOS Y HOGAR [5]...");
                                        //Cntrl.CambioProducts(4);
                                    }else{
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de BLANCOS Y HOGAR [5]...");
                                        Cntrl.CambProductsAlm(4,0);
                                    }
                                }
                            } catch (InterruptedException ex) {
                                Logger.getLogger(MovClts.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        
                        if(NumProdcts==0){
                            break;
                        }
                        
                        //---- Determinando nueva ruta
                        aux = (int)(Math.random()*5);
                        switch(aux){
                            case 1:
                                //Moviendo a Farmacia
                                while(y != 165){
                                    try {
                                        y-=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            case 2:
                                //Moviendo a Lácteos
                                while(x != 200){
                                    try {
                                        x-=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            case 3:
                                //Moviendo a Pan y cereales
                                while(y != 390){
                                    try{
                                        y+=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            case 4:
                                //Moviendo a frutas y vegetales
                                while(x != 400){
                                    try{
                                        x-=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } 
                                
                                while(y != 165){
                                    try {
                                        y-=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                while(x != 200){
                                    try {
                                        x-=20;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            default:
                                //Moviendo a Carnes
                                while(x != 400){
                                    try{
                                        x-=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                while(y != 390){
                                    try{
                                        y+=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                while(x != 200){
                                    try {
                                        x-=20;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                        }
                    break;
                    
                    default:
                        //--- Esta en Pan y cereales
                        NumProdcts--; //Tomando un producto
                        
                        if(Cntrl.ExistProducts(5)){
                            Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a PAN Y CEREALES [6]...");
                            Cntrl.CambioProducts(5);
                        }else{
                            try { 
                                //Comprobando si en almacen hay disponibilidad...
                                existencia = Cntrl.Disponibilidad(5);
                                if(existencia>=15){
                                    //Inicia hilo que surte 15 productos...
                                    Cntrl.MensajeMovClt("\n > Surtiedo a PAN Y CEREALES [6] (+15)...");
                                    if(!Cntrl.Surt5){
                                        Cntrl.Surt5=true;
                                        //Cntrl.CambProductsAlm(5,15);
                                        S5 = new Surtidor(Cntrl,670, 450, 5, 15,Ref);
                                        S5.start();

                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a PAN Y CEREALES [6]...");
                                        //Cntrl.CambioProducts(5);
                                    }else{
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de PAN Y CEREALES [6]...");
                                        Cntrl.CambProductsAlm(5,0);
                                    }
                                }else if(existencia <= 0){
                                    //No se puede surtir... Si es el último termina producto...
                                    Cntrl.MensajeMovClt("\n == No puede ser surtida PAN Y CEREALES [6]...");
                                    Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de PAN Y CEREALES [6]...");
                                    Cntrl.CambProductsAlm(5,0);
                                }else{
                                    //Inicia hilo que surte 1o que hay...
                                    Cntrl.MensajeMovClt("\n > Surtiendo a PAN Y CEREALES [6] (+"+existencia+")...");
                                    
                                    if(!Cntrl.Surt5){
                                        Cntrl.Surt5=true;
                                        //Cntrl.CambProductsAlm(5,existencia);
                                        S5 = new Surtidor(Cntrl,670, 450, 5, existencia,Ref);
                                        S5.start();

                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a PAN Y CEREALES [6]...");
                                        //Cntrl.CambioProducts(5);
                                    }else{
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de PAN Y CEREALES [6]...");
                                        Cntrl.CambProductsAlm(5,0);
                                    }
                                }
                            } catch (InterruptedException ex) {
                                Logger.getLogger(MovClts.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        
                        if(NumProdcts==0){
                            //Ya termino de comprar va a pagar
                            break;
                        }
                        
                        //---- Determinando nueva ruta
                        aux = (int)(Math.random()*3);
                        switch(aux){
                            case 1:
                                //Moviendo a Blancos y hogar
                                while(y != 270){
                                    try{
                                        y-=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            case 2:
                                //Moviendo a Carnes
                                while(x != 200){
                                    try {
                                        x-=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            default:
                                //Moviendo a Lacteos
                                while(x != 400){
                                    try{
                                        x-=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                while(y != 270){
                                    try{
                                        y-=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                while(x != 200){
                                    try {
                                        x-=20;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                        }
                    break;
                }
            }else{
                switch (y) {
                    case 165:
                        //--- Esta en Frutas y vegetales
                        NumProdcts--; //Tomando un producto
                        
                        if(Cntrl.ExistProducts(0)){
                            Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a FRUTAS Y VEGETALES [1]...");
                            Cntrl.CambioProducts(0);
                        }else{
                            try { 
                                //Comprobando si en almacen hay disponibilidad...
                                existencia = Cntrl.Disponibilidad(0);
                                if(existencia>=15){
                                    //Inicia hilo que surte 15 productos...
                                    Cntrl.MensajeMovClt("\n > Surtiedo a FRUTAS Y VEGETALES [1] (+15)...");
                                    
                                    if(!Cntrl.Surt0){
                                        Cntrl.Surt0=true;
                                        //Cntrl.CambProductsAlm(0,15);
                                        S0 = new Surtidor(Cntrl,670, 450, 0, 15,Ref);
                                        S0.start();

                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a FRUTAS Y VEGETALES [1]...");
                                        //Cntrl.CambioProducts(0);
                                    }else{
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de FRUTAS Y VEGETALES [1]...");
                                        Cntrl.CambProductsAlm(0,0);
                                    }
                                }else if(existencia <= 0){
                                    //No se puede surtir... Si es el último termina producto...
                                    Cntrl.MensajeMovClt("\n == No puede ser surtida FRUTAS Y VEGETALES [1]...");
                                    Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de FRUTAS Y VEGETALES [1]...");
                                    Cntrl.CambProductsAlm(0,0);
                                }else{
                                    //Inicia hilo que surte 1o que hay...
                                    Cntrl.MensajeMovClt("\n > Surtiendo a FRUTAS Y VEGETALES [1] (+"+existencia+")...");
                                    if(!Cntrl.Surt0){
                                        Cntrl.Surt0=true;
                                        //Cntrl.CambProductsAlm(0,existencia);

                                        S0 = new Surtidor(Cntrl,670, 450, 0, existencia,Ref);
                                        S0.start();

                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a FRUTAS Y VEGETALES [1]...");
                                        //Cntrl.CambioProducts(0);
                                    }else{
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de FRUTAS Y VEGETALES [1]...");
                                        Cntrl.CambProductsAlm(0,0);
                                    }
                                }
                            } catch (InterruptedException ex) {
                                Logger.getLogger(MovClts.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        
                        if(NumProdcts==0){
                            break;
                        }
                        
                        //---- Determinando nueva ruta
                        aux = (int)(Math.random()*3);
                        switch(aux){
                            case 1:
                                //Moviendo a Farmacia
                                while(x != 550){
                                    try {
                                        x+=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;

                            case 2:
                                //Moviendo a Lacteos
                                while(y != 270){
                                    try {
                                        y+=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;

                            default:
                                //Moviendo a Lácteos
                                while(x != 400){
                                    try{
                                        x+=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }       

                                while(y != 270){
                                    try {
                                        y+=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }

                                while(x != 550){
                                    try {
                                        x+=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                        }
                    break;
                    
                    case 270:
                        //---- Esta en Lacteos
                        NumProdcts--; //Tomando un producto
                        
                        if(Cntrl.ExistProducts(1)){
                            Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a LACTEOS [2]...");
                            Cntrl.CambioProducts(1);
                        }else{
                            try { 
                                //Comprobando si en almacen hay disponibilidad...
                                existencia = Cntrl.Disponibilidad(1);
                                if(existencia>=15){
                                    //Inicia hilo que surte 15 productos...
                                    Cntrl.MensajeMovClt("\n > Surtiedo a LACTEOS [2] (+15)...");
                                    if(!Cntrl.Surt1){
                                        Cntrl.Surt1=true;
                                        //Cntrl.CambProductsAlm(1,15);

                                        S1 = new Surtidor(Cntrl,670, 450, 1, 15,Ref);
                                        S1.start();

                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a LACTEOS [2]...");
                                        //Cntrl.CambioProducts(1);
                                    }else{
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de LACTEOS [2]...");
                                        Cntrl.CambProductsAlm(1,0);
                                    }
                                }else if(existencia <= 0){
                                    //No se puede surtir... Si es el último termina producto...
                                    Cntrl.MensajeMovClt("\n == No puede ser surtido LACTEOS [2]...");
                                    Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de LACTEOS [2]...");
                                    Cntrl.CambProductsAlm(1,0);
                                }else{
                                    //Inicia hilo que surte 1o que hay...
                                    Cntrl.MensajeMovClt("\n > Surtiendo a LACTEOS [2] (+"+existencia+")...");
                                    
                                    if(!Cntrl.Surt1){
                                        Cntrl.Surt1=true;
                                        //Cntrl.CambProductsAlm(1,existencia);

                                        S1 = new Surtidor(Cntrl,670, 450, 1, existencia,Ref);
                                        S1.start();

                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a LACTEOS [2]...");
                                        //Cntrl.CambioProducts(1);
                                    }else{
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de LACTEOS [2]...");
                                        Cntrl.CambProductsAlm(1,0);   
                                    }    
                                }
                            } catch (InterruptedException ex) {
                                Logger.getLogger(MovClts.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        
                        if(NumProdcts==0){
                            break;
                        }
                    
                        
                        //---- Determinando nueva ruta    
                        aux = (int)(Math.random()*5);
                        switch(aux){
                            case 1:
                                //Moviendo a Frutas y vegetales
                                while(y != 165){
                                    try {
                                        y-=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            case 2:
                                //Moviendo a Carnes
                                while(y != 390){
                                    try{
                                        y+=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            case 3:
                                //Moviendo a blancos y hogar
                                while(x != 550){
                                    try {
                                        x+=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            case 4:
                                //Moviendo a Pan y cereales
                                while(x != 400){
                                    try{
                                        x+=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                while(y != 390){
                                    try{
                                        y+=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                while(x != 550){
                                    try {
                                        x+=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            default:
                                //Moviendo a Farmacia
                                while(x != 400){
                                    try{
                                        x+=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                while(y != 165){
                                    try {
                                        y-=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                while(x != 550){
                                    try {
                                        x+=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                        }
                    break;
                    
                    default:
                        //--- Esta en Carnes
                        NumProdcts--; //Tomando un producto                        
                        
                        if(Cntrl.ExistProducts(2)){
                            Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a CARNES [3]...");
                            Cntrl.CambioProducts(2);
                        }else{
                            try { 
                                //Comprobando si en almacen hay disponibilidad...
                                existencia = Cntrl.Disponibilidad(2);
                                if(existencia>=15){
                                    //Inicia hilo que surte 15 productos...
                                    Cntrl.MensajeMovClt("\n > Surtiedo a CARNES [3] (+15)...");
                                    if(!Cntrl.Surt2){
                                        Cntrl.Surt2=true;
                                        //Cntrl.CambProductsAlm(2,15);

                                        S2 = new Surtidor(Cntrl,670, 450, 2, 15,Ref);
                                        S2.start();

                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a CARNES [3]...");
                                        //Cntrl.CambioProducts(2);
                                    }else{
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de CARNES [3]...");
                                        Cntrl.CambProductsAlm(2,0);
                                    }
                                }else if(existencia <= 0){
                                    //No se puede surtir... Si es el último termina producto...
                                    Cntrl.MensajeMovClt("\n == No puede ser surtido CARNES [3]...");
                                    Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de CARNES [3]...");
                                    Cntrl.CambProductsAlm(2,0);
                                }else{
                                    //Inicia hilo que surte 1o que hay...
                                    Cntrl.MensajeMovClt("\n > Surtiendo a CARNES [3] (+"+existencia+")...");
                                    if(!Cntrl.Surt2){
                                        Cntrl.Surt2=true;
                                        //Cntrl.CambProductsAlm(2,existencia);

                                        S2 = new Surtidor(Cntrl,670, 450, 2, existencia,Ref);
                                        S2.start();

                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: -1 a CARNES [3]...");
                                        //Cntrl.CambioProducts(2);
                                    }else{
                                        Cntrl.MensajeMovClt("\n * "+NoClt+"° Cliente: No tomó de CARNES [3]...");
                                        Cntrl.CambProductsAlm(2,0);
                                    }
                                }
                            } catch (InterruptedException ex) {
                                Logger.getLogger(MovClts.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        
                        if(NumProdcts==0){
                            //Ya termino de comprar va a pagar
                            break;
                        }
                        
                        //---- Determinando nueva ruta                       
                        aux = (int)(Math.random()*3);
                        switch(aux){
                            case 1:
                                //Moviendo a Lacteos
                                while(y != 270){
                                    try{
                                        y-=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            case 2:
                                //Moviendo a Panes y cereales
                                while(x != 550){
                                    try {
                                        x+=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                            
                            default:
                                //Moviendo a Blancos y hogar
                                while(x != 400){
                                    try{
                                        x+=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                while(y != 270){
                                    try{
                                        y-=15;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                while(x != 550){
                                    try {
                                        x+=25;
                                        Cntrl.MovCltIn(Clt,x,y);
                                        Thread.sleep(tiempo);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            break;
                        }
                    break;
                }
            }
        }
        
        Clt.NPosicion(x, y);
        
        P = new Pago(Cntrl,Clt);
        P.start();
    }
}

/**
 * Surtidor.
 * Este hilo realizará la animación de surtir a los estantes
 */
class Surtidor extends Thread{
    private int x,y;
    final int cant,estante,tiempo=200;
    private Trabajador T;
    final Monitor Cntrl;
    final MovClts Padre;
    
    //En este hilo llamar a la funcion que ponga el notifica all
    public Surtidor(Monitor Ctrl, int Px, int Py, int estant, int cantPr, MovClts ClasePadre){
        this.x=Px;
        this.y=Py;
        this.estante = estant;
        this.cant = cantPr;
        this.Cntrl=Ctrl;
        this.Padre = ClasePadre;
    }
    
    @Override
    public void run(){
        //x670 y 450
        T = new Trabajador();
        Cntrl.RefVent.TiendaPanel.add(T);
        T.setBounds(670, 450, 35, 35);
        Cntrl.RefVent.TiendaPanel.repaint();
        
        while(x != 400){
            try{
                x-=15;
                T.setLocation(x, y);
                Thread.sleep(tiempo);
            } catch (InterruptedException ex) {
                Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try{
            switch(estante){
                case 0:
                    //Surtir frutas y vegetales...
                    while(y != 165){
                        y-=15;
                        T.setLocation(x, y);
                        Thread.sleep(tiempo);
                    }

                    while(x != 200){
                        x-=20;
                        T.setLocation(x, y);
                        Thread.sleep(tiempo);
                    }
                    
                    Cntrl.CambProductsAlm(estante,cant);
                    Cntrl.CambioProducts(estante);
                    Cntrl.Surt0 = false;
                break;

                case 1:
                    //Surtir Lacteos
                    while(y != 270){
                        y-=15;
                        T.setLocation(x, y);
                        Thread.sleep(tiempo);
                    }

                    while(x != 200){
                        x-=20;
                        T.setLocation(x, y);
                        Thread.sleep(tiempo);
                    }
                    
                    Cntrl.CambProductsAlm(estante,cant);
                    Cntrl.CambioProducts(estante);
                    Cntrl.Surt1 = false;
                break;

                case 2:
                    //Surtir Carnes
                    while(y != 390){
                        y-=15;
                        T.setLocation(x, y);
                        Thread.sleep(tiempo);
                    }

                    while(x != 200){
                        x-=20;
                        T.setLocation(x, y);
                        Thread.sleep(tiempo);
                    }
                    
                    Cntrl.CambProductsAlm(estante,cant);
                    Cntrl.CambioProducts(estante);
                    Cntrl.Surt2 = false;
                break;

                case 3:
                    //Surtir farmacia...
                    while(y != 165){
                        y-=15;
                        T.setLocation(x, y);
                        Thread.sleep(tiempo);
                    }

                    while(x != 550){
                        x+=25;
                        T.setLocation(x, y);
                        Thread.sleep(tiempo);
                    }
                    
                    Cntrl.CambProductsAlm(estante,cant);
                    Cntrl.CambioProducts(estante);
                    Cntrl.Surt3 = false;
                break;

                case 4:
                    //Surtir blancos y hogar
                    while(y != 270){
                        y-=15;
                        T.setLocation(x, y);
                        Thread.sleep(tiempo);
                    }

                    while(x != 550){
                        x+=25;
                        T.setLocation(x, y);
                        Thread.sleep(tiempo);
                    }
                    
                    Cntrl.CambProductsAlm(estante,cant);
                    Cntrl.CambioProducts(estante);
                    Cntrl.Surt4 = false;
                break;

                case 5:
                    //Surtir pan y cereales
                    while(y != 390){
                        y-=15;
                        T.setLocation(x, y);
                        Thread.sleep(tiempo);
                    }

                    while(x != 550){
                        x+=25;
                        T.setLocation(x, y);
                        Thread.sleep(tiempo);
                    }

                    Cntrl.CambProductsAlm(estante,cant);
                    Cntrl.CambioProducts(estante);
                    Cntrl.Surt5 = false;
                break;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Surtidor.class.getName()).log(Level.SEVERE, null, ex);
        }
           
        Cntrl.RefVent.TiendaPanel.remove(T);
        Cntrl.RefVent.TiendaPanel.repaint();
    }
}

/**
 * Pago.
 * Este hilo realizará la animación de pago hacia la caja
 */
class Pago extends Thread{
    final ClienteTienda Clt; //Cliente
    final Monitor Cntrl;
    private int x,y;
    final int tiempo = 200;
    
    public Pago(Monitor Ctrl, ClienteTienda Cl){
        this.Clt = Cl;
        this.Cntrl = Ctrl;
    }
    
    @Override
    public void run(){
        x=Clt.PosX();
        y=Clt.PosY();
        
        if(x == 200){            
            switch(y){
                case 165:
                    //Se detuvo en frutas y vegetales
                    try{    
                        while(y != 205){
                            y+=20;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }

                        while(x != 750){
                            x+=25;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }

                        while(y != 245){
                            y+=20;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                    }
                break;
                
                case 270:
                    // se detuvo en lacteos
                    try{
                        while(y != 310){
                            y+=20;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }
                        
                        while(x != 750){
                            x+=25;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }
                        
                        while(y != 235){
                            y-=25;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }
                        y+=10;
                        Cntrl.MovCltIn(Clt,x,y);
                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                    }
                break;
                
                case 390:
                    //Se detuvo en carnes
                    try{
                        while(y != 310){
                            y-=20;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }
                        
                        while(x != 750){
                            x+=25;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }
                        
                        while(y != 235){
                            y-=25;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }
                        y+=10;
                        Cntrl.MovCltIn(Clt,x,y);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                    }
                break;
            }
        }else{
            switch(y){
                case 165:
                    //Se detuvo en farmacia
                    try{
                        while(x != 750){
                            x+=20;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }
                        
                        while(y != 245){
                            y+=20;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                    }
                break;

                case 270:
                    // se detuvo en hogar
                    try{
                        while(x != 750){
                            x+=20;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }
                        
                        y-=25;
                        Cntrl.MovCltIn(Clt,x,y);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                    }
                break;

                case 390:
                    //Se detuvo en pan
                    try{
                        while(x != 750){
                            x+=20;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }
                        while(y != 240){
                            y-=25;
                            Cntrl.MovCltIn(Clt,x,y);
                            Thread.sleep(tiempo);
                        }
                        y+=5;
                        Cntrl.MovCltIn(Clt,x,y);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Ejec_S.class.getName()).log(Level.SEVERE, null, ex);
                    }
                break;
            }
        }
        Clt.NPosicion(x, y);
        
        //Comprobando el estado de las cajas - función que decidirá el numero de cajero a pasar
        try {
            Cntrl.ComprobCajs(Clt);
        } catch (InterruptedException ex) {
            Logger.getLogger(Pago.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class Inicio extends Thread{
    final Monitor Cntrl;
    final int tiempo = 200;
    final JButton BCaja1,BCaja2,BCaja3;
    private Cj1 C1;
    private Cj2 C2;
    private Cj3 C3;
    private String Cad;
    private SonidoCliente SoundClt;
    private int clts,aux,i,TClts;
    
    public Inicio(Monitor Ctrl){
        this.Cntrl = Ctrl;
        //Referencia a cada caja...
        BCaja1 = Cntrl.RefVent.Cj1;
        BCaja2 = Cntrl.RefVent.Cj2;
        BCaja3 = Cntrl.RefVent.Cj3;
    }
    
    @Override
    public void run(){
        C1 = new Cj1(Cntrl,BCaja1);
        C1.start();
          
        C2 = new Cj2(Cntrl,BCaja2);
        C2.start();

        C3 = new Cj3(Cntrl,BCaja3);
        C3.start();
        
        boolean generar=true;
        while(generar){
            try {
                Thread.sleep(5000*3+(int)(Math.random()*6));
            } catch (InterruptedException ex) {
                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            TClts = Cntrl.RefVent.TClients;
            if(TClts<15){
                //Creación aleatoria de clientes
                clts = (int)((Math.random()*4)+2);
                aux = 1050+(25*clts);
                TClts = Cntrl.RefVent.TClients;

                for(i=0;i<clts;i++){
                    ClienteTienda C;

                    if(((int)((Math.random()*10))) %2==0){
                        C = new ClienteTienda((TClts+(i+1)),0);
                        Cad = "\n Nuevo cliente (No. "+(TClts+(i+1))+") Hombre: Esperando...";
                    }else{
                        C = new ClienteTienda((TClts+(i+1)),1);
                        Cad = "\n Nuevo cliente (No. "+(TClts+(i+1))+") Mujer: Esperando...";
                    }

                    //Añadiendo cliente al panel...
                    Cntrl.RefVent.TiendaPanel.add(C);
                    C.setBounds(1050,160,38,30);
                    //Control del cliente (añadiendo a lista)
                    Cntrl.RefVent.Clts.add(C);

                    // Hilo que encamina a los clientes...
                    MovIni Movs = new MovIni(Cntrl,C,TClts,aux);
                    Cntrl.ActNumClts2(Cad);
                    Movs.start();
                    aux-=50;
                }

                SoundClt = new SonidoCliente();
                SoundClt.start();
            }
        }
    }
}

class Cj1 extends Thread{
    private ClienteTienda Clt; //Cliente
    final Monitor Cntrl;
    private int x,y;
    final int tiempo = 200;
    final JButton BCaja1;
    SonidoCaja Tono;
    
    public Cj1(Monitor Ctrl,JButton B){
        this.Cntrl = Ctrl;
        this.BCaja1 =B;
    }
    
    @Override
    public void run(){
        while(true){
            Clt = null;
            BCaja1.setBackground(Color.GREEN);
            while(Clt==null){
                try {
                    Clt = Cntrl.Cobrar1();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            BCaja1.setBackground(Color.ORANGE);
            Cntrl.ResetClt(1);

            //System.out.println("Este hilo es la caja: 1 atendiendo a: "+Clt.NoCliente);
            x = Clt.PosX();
            y = Clt.PosY();
            try{
                while(y != 195){
                    y-=25;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }

                while(x != 870){
                    x+=20;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }

                Cntrl.MensajeMovClt("\n [Cliente: "+Clt.NoCliente+" en caja 1]");
                Thread.sleep(1000*3+(int)(Math.random()*5));
                //CL1.start();
                //Ton1.start();
                
                                
                Tono = new SonidoCaja();
                Tono.start();
                
                Cntrl.MensajeMovClt("\n >>> Cliente: "+Clt.NoCliente+" saldrá...");

                while(x != 950){
                    x+=20;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }

                y+=20;
                Clt.setLocation(x, y);

                while(x != 1050){
                    x+=20;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Función que saca al cliente
            Cntrl.ElimClient(Clt);
            //Función que reseteará las banderas de las cajas
            Cntrl.CajaLibre(1);
        }
    }
}

class Cj2 extends Thread{
    private ClienteTienda Clt; //Cliente
    final Monitor Cntrl;
    private int x,y;
    final int tiempo = 200;
    final JButton BCaja2;
    SonidoCaja Tono;
    
    public Cj2(Monitor Ctrl,JButton B){
        this.Cntrl = Ctrl;
        this.BCaja2 = B;
    }
    
    @Override
    public void run(){
        while(true){
            BCaja2.setBackground(Color.GREEN);
            Clt = null;
            while(Clt==null){
                try {
                    Clt = Cntrl.Cobrar2();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            BCaja2.setBackground(Color.YELLOW);
            Cntrl.ResetClt(2);

            //System.out.println("Este hilo es la caja: 2 atendiendo a: "+Clt.NoCliente);
            x = Clt.PosX();
            y = Clt.PosY();
            try{
                while(x != 870){
                    x+=20;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }

                Cntrl.MensajeMovClt("\n [Cliente: "+Clt.NoCliente+" en caja 2]");
                Thread.sleep(1000*3+(int)(Math.random()*6));
                
                Tono = new SonidoCaja();
                Tono.start();
                
                Cntrl.MensajeMovClt("\n >>> Cliente: "+Clt.NoCliente+" saldrá...");

                 while(x != 950){
                    x+=20;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }

                while(y != 215){
                    y-=15;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }

                while(x != 1050){
                    x+=20;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Función que saca al cliente
            Cntrl.ElimClient(Clt);
            //Función que reseteará las banderas de las cajas
            Cntrl.CajaLibre(2);
        }
    }
}

class Cj3 extends Thread{
    private ClienteTienda Clt; //Cliente
    final Monitor Cntrl;
    private int x,y;
    final int tiempo = 200;
    final JButton BCaja3;
    SonidoCaja Tono;
    
    public Cj3(Monitor Ctrl,JButton B){
        this.Cntrl = Ctrl;
        this.BCaja3=B;
    }
    
    @Override
    public void run(){
        while(true){
            Clt = null;
            BCaja3.setBackground(Color.GREEN);
            while(Clt==null){
                try {
                    Clt = Cntrl.Cobrar3();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            BCaja3.setBackground(Color.RED);
            Cntrl.ResetClt(3);

            //System.out.println("Este hilo es la caja: 3 atendiendo a: "+Clt.NoCliente);
            x = Clt.PosX();
            y = Clt.PosY();
            try{
                while(y != 295){
                    y+=25;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }

                while(x != 870){
                    x+=20;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }

                Cntrl.MensajeMovClt("\n [Cliente: "+Clt.NoCliente+" en caja 3]");
                Thread.sleep(1000*3+(int)(Math.random()*6));
//                CL3.start();
//                Ton3.start();

                
                Tono = new SonidoCaja();
                Tono.start();
                
                Cntrl.MensajeMovClt("\n >>> Cliente: "+Clt.NoCliente+" saldrá...");

                while(x != 950){
                    x+=20;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }

                while(y != 215){
                    y-=20;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }

                while(x != 1050){
                    x+=20;
                    Clt.setLocation(x, y);
                    Thread.sleep(tiempo);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Función que saca al cliente
            Cntrl.ElimClient(Clt);
            //Función que reseteará las banderas de las cajas
            Cntrl.CajaLibre(3);
        }
    }
}


class SonidoCaja extends Thread{
    final int tiempo = 300;
    private Clip CLP;
    private AudioInputStream Flujo;
    
    public SonidoCaja(){
        try {
            this.CLP = AudioSystem.getClip();
            this.Flujo = CargaTono();
            CLP.open(Flujo);
        } catch (LineUnavailableException | IOException ex) {
            Logger.getLogger(SonidoCaja.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run(){
        CLP.start();
        while(true){
            if(CLP.getMicrosecondPosition()==CLP.getMicrosecondLength()){
                CLP.drain();
                CLP.flush();
                CLP.close();
                CLP.stop();
                
                break;
            }
        }
    }
    
    public AudioInputStream CargaTono(){
        AudioInputStream F = null;
        
        try{
            F = AudioSystem.getAudioInputStream(new File("Cajero.wav"));
        }catch (UnsupportedAudioFileException e){
            System.out.println("\n Tipo de archivo o formato no soportado...");
            System.out.println(e);
        }catch (IOException e){
            System.out.println("Ocurrio un error al abrir o leer el archivo...");
        }
        
        return F;
    }
}


class SonidoFondo extends Thread{
    final int tiempo = 300;
    private Clip CLP;
    private AudioInputStream Flujo;
    
    public SonidoFondo(){
        try {
            this.CLP = AudioSystem.getClip();
            this.Flujo = CargaTono();
            CLP.open(Flujo);
        } catch (LineUnavailableException | IOException ex) {
            Logger.getLogger(SonidoCaja.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run(){
        while(true){
            CLP.start();
            while(true){
                if(CLP.getMicrosecondPosition()==CLP.getMicrosecondLength()){
                    CLP.drain();
                    CLP.flush();
                    CLP.close();
                    CLP.stop();
                    break;
                }
            }
            try {
                Flujo = CargaTono();
                CLP.open(Flujo);
            } catch (LineUnavailableException | IOException ex) {
                Logger.getLogger(SonidoCaja.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public AudioInputStream CargaTono(){
        AudioInputStream F = null;
        
        try{
            F = AudioSystem.getAudioInputStream(new File("Fondo.wav"));
        }catch (UnsupportedAudioFileException e){
            System.out.println("\n Tipo de archivo o formato no soportado...");
            System.out.println(e);
        }catch (IOException e){
            System.out.println("Ocurrio un error al abrir o leer el archivo...");
        }
        
        return F;
    }
}


class SonidoCliente extends Thread{
    final int tiempo = 300;
    private Clip CLP;
    private AudioInputStream Flujo;
    
    public SonidoCliente(){
        try {
            this.CLP = AudioSystem.getClip();
            this.Flujo = CargaTono();
            CLP.open(Flujo);
        } catch (LineUnavailableException | IOException ex) {
            Logger.getLogger(SonidoCaja.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run(){
        CLP.start();
        while(true){
            if(CLP.getMicrosecondPosition()==CLP.getMicrosecondLength()){
                CLP.drain();
                CLP.flush();
                CLP.close();
                CLP.stop();
                break;
            }
        }
    }
    
    public AudioInputStream CargaTono(){
        AudioInputStream F = null;
        
        try{
            F = AudioSystem.getAudioInputStream(new File("Cliente.wav"));
        }catch (UnsupportedAudioFileException e){
            System.out.println("\n Tipo de archivo o formato no soportado...");
            System.out.println(e);
        }catch (IOException e){
            System.out.println("Ocurrio un error al abrir o leer el archivo...");
        }
        
        return F;
    }
}


/**
 * ======================================================== Ejec_S.
 * Clase "Principal" que controla los eventos de la ventana
 * @author Cesar Brgs
 */
public class Ejec_S extends javax.swing.JFrame {
    Ejec_S Ref = this;
    ImageIcon Imgx,x,IconCarrs[];
    String Cad;
    int ProductAlm[], ProductDisp[],TClients,TClientesL,i,j,NoCarr, Car1, Car2;
    boolean ActServ,band;
    JTextField Dats[],Dats2[];
    
    //Temporizadores de animaciones
    Timer Luz,Arboles,Carritos;
    
    Monitor Control;
    Inicio Iniciado;  // Hilo de preparación
    ServidorActv HiloServ; // Hilo de servidor activo
     
    //Clientes
    ArrayList<ClienteTienda> Clts = new ArrayList<>();
    
    //Animación Luces de tienda
    ActionListener AnimLuces=new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent ev){
            band=!band;
            if(band){
                L1.setBackground(new Color(246,246,217));
                L2.setBackground(new Color(246,246,217));
                L3.setBackground(new Color(246,246,217));
                L4.setBackground(new Color(246,246,217));
                L5.setBackground(new Color(246,246,217));
                L6.setBackground(new Color(246,246,217));
                L7.setBackground(new Color(246,246,217));
                L8.setBackground(new Color(246,246,217));
            }else{
                L1.setBackground(new Color(255,255,181));
                L2.setBackground(new Color(255,255,181));
                L3.setBackground(new Color(255,255,181));
                L4.setBackground(new Color(255,255,181));
                L5.setBackground(new Color(255,255,181));
                L6.setBackground(new Color(255,255,181));
                L7.setBackground(new Color(255,255,181));
                L8.setBackground(new Color(255,255,181));
                
            }
        }
    };
    
    //Animación Carros
    ActionListener AnimCarros=new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent ev){
            Car1-=20;
            Car2+=20;
            if(Car2==1060 && Car1== -20){
                Car1 = 1060;
                Car2 = -20;
                Carr1.setIcon(IconCarrs[(int)(Math.random()*3)]);
                Carr2.setIcon(IconCarrs[(int)(Math.random()*3)]);
            }
            
            Carr1.setLocation(Car1,0);
            Carr2.setLocation(Car2,0);
        }
    };
    
    public Ejec_S(){
        initComponents();
        Ref.setLocationRelativeTo(null);
        Ref.setTitle("Programación concurrente (Threads) - Simulador de tienda departamental");
        
        Image Logo=Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("Imgs/Icono.png"));
        Ref.setIconImage(Logo);
        
        //Temporizadores
        Luz =  new Timer(1300, AnimLuces);
        Carritos = new Timer(100, AnimCarros);
        
        //Cantidad de productos disponibles
        ProductDisp = new int[6];
        
        //Cantidad de productos en almacén
        ProductAlm = new int[6];
        
        //Arreglo de etiquetas datos
        Dats = new JTextField[6];
        Dats2 = new JTextField[6];
        
        //Total de clientes (Local)
        TClients = 0;
        
        //Creando objeto Monitor, el cual tendrá el control de los hilos...
        Control = new Monitor(Ref);
        
        //Fondo
        x=new ImageIcon("src\\Imgs\\Fondo.png");
        Imgx=new ImageIcon(x.getImage().getScaledInstance(1110,590,Image.SCALE_DEFAULT));
        FondoTienda.setIcon(Imgx);
        
        x=new ImageIcon("src\\Imgs\\h.png");
        Imgx=new ImageIcon(x.getImage().getScaledInstance(IH.getWidth(),IH.getHeight(),Image.SCALE_DEFAULT));
        IH.setIcon(Imgx);
        
        x=new ImageIcon("src\\Imgs\\m.png");
        Imgx=new ImageIcon(x.getImage().getScaledInstance(IM.getWidth(),IM.getHeight(),Image.SCALE_DEFAULT));
        IM.setIcon(Imgx);
        
        x=new ImageIcon("src\\Imgs\\C5.png");
        Imgx=new ImageIcon(x.getImage().getScaledInstance(100,50,Image.SCALE_DEFAULT));
        Camion.setIcon(Imgx);
        Camion1.setIcon(Imgx);
        
        IconCarrs = new ImageIcon[4];
        //Iconos carros
        for(i=0;i<4;i++){
            x=new ImageIcon("src\\Imgs\\C"+(i+1)+".png");
            Imgx=new ImageIcon(x.getImage().getScaledInstance(60,30,Image.SCALE_DEFAULT));
            IconCarrs[i]=Imgx;
        }
        
        //Cargando la cantidad de produtos a vender
        int Almcn = ((int)((Math.random()*5)+4))*10;
        for(i=0;i<6;i++){
            ProductAlm[i] = Almcn;
        }
        
        A1.setText(""+ProductAlm[0]);
        A2.setText(""+ProductAlm[1]);
        A3.setText(""+ProductAlm[2]);
        A4.setText(""+ProductAlm[3]);
        A5.setText(""+ProductAlm[4]);
        A6.setText(""+ProductAlm[5]);
        
        Dats2[0] = A1;
        Dats2[1] = A2;
        Dats2[2] = A3;
        Dats2[3] = A4;
        Dats2[4] = A5;
        Dats2[5] = A6;
        
        for(i=0;i<6;i++){
            ProductDisp[i] = ((int)((Math.random()*4)+1))*1;//10
        }
        
        D1.setText(""+ProductDisp[0]);
        D2.setText(""+ProductDisp[1]);
        D3.setText(""+ProductDisp[2]);
        D4.setText(""+ProductDisp[3]);
        D5.setText(""+ProductDisp[4]);
        D6.setText(""+ProductDisp[5]);

        Dats[0] = D1;
        Dats[1] = D2;
        Dats[2] = D3;
        Dats[3] = D4;
        Dats[4] = D5;
        Dats[5] = D6;
        
        //Servidor Inactivo
        ActServ = false;
        AgrH.setVisible(false);
        AgrM.setVisible(false);
        Aleat.setVisible(false);
        
        Carr1.setIcon(IconCarrs[1]);
        Carr2.setIcon(IconCarrs[0]);
        
        Car1 = 1060;
        Car2 = -20;
        Carritos.start();
        Luz.start();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TituloControl = new javax.swing.JLabel();
        PanelControl = new javax.swing.JPanel();
        Inicio = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        TClient = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        InfoTienda = new javax.swing.JTextArea();
        TituloClientes = new javax.swing.JLabel();
        PanelClientes = new javax.swing.JPanel();
        Aleat = new javax.swing.JButton();
        AgrM = new javax.swing.JButton();
        IM = new javax.swing.JLabel();
        EtqM = new javax.swing.JLabel();
        AgrH = new javax.swing.JButton();
        IH = new javax.swing.JLabel();
        EtqH = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        TituloPanel = new javax.swing.JLabel();
        PanelTitulo = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        Info = new javax.swing.JButton();
        TituloEnLinea = new javax.swing.JLabel();
        PanelEnLinea = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        InfoLinea = new javax.swing.JTextArea();
        BServidor = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        TClientOn = new javax.swing.JLabel();
        TituloEnProductos = new javax.swing.JLabel();
        PanelProductos = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        A1 = new javax.swing.JTextField();
        A2 = new javax.swing.JTextField();
        A3 = new javax.swing.JTextField();
        A4 = new javax.swing.JTextField();
        A5 = new javax.swing.JTextField();
        A6 = new javax.swing.JTextField();
        D1 = new javax.swing.JTextField();
        D2 = new javax.swing.JTextField();
        D3 = new javax.swing.JTextField();
        D4 = new javax.swing.JTextField();
        D5 = new javax.swing.JTextField();
        D6 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        TiendaPanel = new javax.swing.JPanel();
        L5 = new javax.swing.JButton();
        L4 = new javax.swing.JButton();
        L1 = new javax.swing.JButton();
        L2 = new javax.swing.JButton();
        L7 = new javax.swing.JButton();
        L8 = new javax.swing.JButton();
        L6 = new javax.swing.JButton();
        L3 = new javax.swing.JButton();
        Carr2 = new javax.swing.JLabel();
        Carr1 = new javax.swing.JLabel();
        Cj2 = new javax.swing.JButton();
        Cj3 = new javax.swing.JButton();
        Cj1 = new javax.swing.JButton();
        Camion = new javax.swing.JLabel();
        Camion1 = new javax.swing.JLabel();
        FondoTienda = new javax.swing.JLabel();
        Fondo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TituloControl.setBackground(new java.awt.Color(204, 255, 255));
        TituloControl.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        TituloControl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TituloControl.setText("- Control de la tienda departamental -");
        TituloControl.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 0, 2, new java.awt.Color(0, 0, 0)));
        TituloControl.setOpaque(true);
        getContentPane().add(TituloControl, new org.netbeans.lib.awtextra.AbsoluteConstraints(1120, 0, 340, 30));

        PanelControl.setBackground(new java.awt.Color(204, 255, 255));
        PanelControl.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(0, 0, 0)));
        PanelControl.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Inicio.setBackground(new java.awt.Color(255, 255, 204));
        Inicio.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        Inicio.setText("Iniciar ejecución");
        Inicio.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Inicio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                InicioMousePressed(evt);
            }
        });
        PanelControl.add(Inicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jLabel3.setFont(new java.awt.Font("sansserif", 1, 16)); // NOI18N
        jLabel3.setText("Núm. de clientes:");
        PanelControl.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 10, 140, 30));

        TClient.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        TClient.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TClient.setText("0");
        PanelControl.add(TClient, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 10, 30, 30));

        InfoTienda.setEditable(false);
        InfoTienda.setColumns(20);
        InfoTienda.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        InfoTienda.setLineWrap(true);
        InfoTienda.setRows(5);
        jScrollPane2.setViewportView(InfoTienda);

        PanelControl.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 340, 180));

        getContentPane().add(PanelControl, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 20, 360, 230));

        TituloClientes.setBackground(new java.awt.Color(255, 255, 204));
        TituloClientes.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        TituloClientes.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TituloClientes.setText("- Control del ingreso de clientes a la tienda -");
        TituloClientes.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 0, 2, new java.awt.Color(0, 0, 0)));
        TituloClientes.setOpaque(true);
        getContentPane().add(TituloClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 0, 720, -1));

        PanelClientes.setBackground(new java.awt.Color(255, 255, 204));
        PanelClientes.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 1, 2, new java.awt.Color(0, 0, 0)));
        PanelClientes.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Aleat.setBackground(new java.awt.Color(204, 255, 255));
        Aleat.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        Aleat.setText("Agregar");
        Aleat.setToolTipText("");
        Aleat.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Aleat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AleatMousePressed(evt);
            }
        });
        PanelClientes.add(Aleat, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 33, 110, 30));

        AgrM.setBackground(new java.awt.Color(204, 255, 255));
        AgrM.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        AgrM.setText("Agregar");
        AgrM.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        AgrM.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AgrMMousePressed(evt);
            }
        });
        PanelClientes.add(AgrM, new org.netbeans.lib.awtextra.AbsoluteConstraints(554, 30, 160, -1));
        PanelClientes.add(IM, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 30, 60, 30));

        EtqM.setBackground(new java.awt.Color(255, 219, 219));
        EtqM.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        EtqM.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        EtqM.setText("- Cliente Mujer -");
        EtqM.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        EtqM.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(0, 0, 0)));
        EtqM.setOpaque(true);
        PanelClientes.add(EtqM, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 10, 250, 60));

        AgrH.setBackground(new java.awt.Color(204, 255, 255));
        AgrH.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        AgrH.setText("Agregar");
        AgrH.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        AgrH.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AgrHMousePressed(evt);
            }
        });
        PanelClientes.add(AgrH, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 30, 140, -1));
        PanelClientes.add(IH, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 60, 30));

        EtqH.setBackground(new java.awt.Color(206, 230, 255));
        EtqH.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        EtqH.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        EtqH.setText("- Cliente Hombre -");
        EtqH.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        EtqH.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(0, 0, 0)));
        EtqH.setOpaque(true);
        PanelClientes.add(EtqH, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 10, 250, 60));

        jLabel1.setBackground(new java.awt.Color(255, 233, 212));
        jLabel1.setFont(new java.awt.Font("sansserif", 1, 16)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Aleatorio");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(0, 0, 0)));
        jLabel1.setOpaque(true);
        PanelClientes.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, 170, 60));

        getContentPane().add(PanelClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 20, 760, 70));

        TituloPanel.setBackground(new java.awt.Color(204, 255, 204));
        TituloPanel.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        TituloPanel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TituloPanel.setText("Programación colaborativa: ");
        TituloPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 0, 2, new java.awt.Color(0, 0, 0)));
        TituloPanel.setOpaque(true);
        getContentPane().add(TituloPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 310, -1));

        PanelTitulo.setBackground(new java.awt.Color(204, 255, 204));
        PanelTitulo.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 1, 2, new java.awt.Color(0, 0, 0)));
        PanelTitulo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("- Simulación de tienda departamental -");
        PanelTitulo.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 327, 20));

        Info.setBackground(new java.awt.Color(255, 255, 204));
        Info.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        Info.setText("Información Personal");
        Info.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Info.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                InfoMousePressed(evt);
            }
        });
        PanelTitulo.add(Info, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 30, 220, 30));

        getContentPane().add(PanelTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 350, 70));

        TituloEnLinea.setBackground(new java.awt.Color(153, 204, 255));
        TituloEnLinea.setFont(new java.awt.Font("sansserif", 1, 16)); // NOI18N
        TituloEnLinea.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TituloEnLinea.setText(" Servicio en línea");
        TituloEnLinea.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 0, 2, new java.awt.Color(0, 0, 0)));
        TituloEnLinea.setOpaque(true);
        getContentPane().add(TituloEnLinea, new org.netbeans.lib.awtextra.AbsoluteConstraints(1120, 480, 340, 30));

        PanelEnLinea.setBackground(new java.awt.Color(153, 204, 255));
        PanelEnLinea.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(0, 0, 0)));
        PanelEnLinea.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        InfoLinea.setEditable(false);
        InfoLinea.setColumns(20);
        InfoLinea.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        InfoLinea.setLineWrap(true);
        InfoLinea.setRows(5);
        jScrollPane1.setViewportView(InfoLinea);

        PanelEnLinea.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 340, 130));

        BServidor.setBackground(new java.awt.Color(204, 255, 204));
        BServidor.setFont(new java.awt.Font("sansserif", 1, 16)); // NOI18N
        BServidor.setText("Activar servidor");
        BServidor.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        BServidor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                BServidorMousePressed(evt);
            }
        });
        PanelEnLinea.add(BServidor, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jLabel12.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Núm. de clientes:");
        PanelEnLinea.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 10, 140, 30));

        TClientOn.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        TClientOn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TClientOn.setText("0");
        PanelEnLinea.add(TClientOn, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 10, 30, 30));

        getContentPane().add(PanelEnLinea, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 500, 360, 180));

        TituloEnProductos.setBackground(new java.awt.Color(255, 204, 153));
        TituloEnProductos.setFont(new java.awt.Font("sansserif", 1, 16)); // NOI18N
        TituloEnProductos.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TituloEnProductos.setText("Cantidad de productos");
        TituloEnProductos.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 0, 2, new java.awt.Color(0, 0, 0)));
        TituloEnProductos.setOpaque(true);
        getContentPane().add(TituloEnProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 250, 320, 30));

        PanelProductos.setBackground(new java.awt.Color(255, 204, 153));
        PanelProductos.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(0, 0, 0)));
        PanelProductos.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Pan y cereales [6]:");
        PanelProductos.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 160, 30));

        jLabel6.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Frutas y vegetales [1]:");
        PanelProductos.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 160, 30));

        jLabel7.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Farmacia [4]:");
        PanelProductos.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 160, 30));

        jLabel8.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Blancos y hogar [5]:");
        PanelProductos.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 160, 30));

        jLabel9.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Lácteos [2]:");
        PanelProductos.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 160, 30));

        jLabel10.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Carnes [3]:");
        PanelProductos.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 160, 30));

        A1.setEditable(false);
        A1.setBackground(new java.awt.Color(255, 255, 204));
        A1.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        A1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        A1.setText("0");
        PanelProductos.add(A1, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 20, 55, -1));

        A2.setEditable(false);
        A2.setBackground(new java.awt.Color(255, 255, 204));
        A2.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        A2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        A2.setText("0");
        PanelProductos.add(A2, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 50, 55, -1));

        A3.setEditable(false);
        A3.setBackground(new java.awt.Color(255, 255, 204));
        A3.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        A3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        A3.setText("0");
        PanelProductos.add(A3, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 80, 55, -1));

        A4.setEditable(false);
        A4.setBackground(new java.awt.Color(255, 255, 204));
        A4.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        A4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        A4.setText("0");
        PanelProductos.add(A4, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 110, 55, -1));

        A5.setEditable(false);
        A5.setBackground(new java.awt.Color(255, 255, 204));
        A5.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        A5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        A5.setText("0");
        PanelProductos.add(A5, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 140, 55, -1));

        A6.setEditable(false);
        A6.setBackground(new java.awt.Color(255, 255, 204));
        A6.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        A6.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        A6.setText("0");
        PanelProductos.add(A6, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 170, 55, -1));

        D1.setEditable(false);
        D1.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        D1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        D1.setText("0");
        PanelProductos.add(D1, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 20, 55, -1));

        D2.setEditable(false);
        D2.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        D2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        D2.setText("0");
        PanelProductos.add(D2, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 50, 55, -1));

        D3.setEditable(false);
        D3.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        D3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        D3.setText("0");
        PanelProductos.add(D3, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 80, 55, -1));

        D4.setEditable(false);
        D4.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        D4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        D4.setText("0");
        PanelProductos.add(D4, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 110, 55, -1));

        D5.setEditable(false);
        D5.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        D5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        D5.setText("0");
        PanelProductos.add(D5, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 140, 55, -1));

        D6.setEditable(false);
        D6.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        D6.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        D6.setText("0");
        PanelProductos.add(D6, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 170, 55, -1));

        jLabel14.setBackground(new java.awt.Color(255, 255, 204));
        jLabel14.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("A");
        jLabel14.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel14.setOpaque(true);
        jLabel14.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        PanelProductos.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 30, 30, 30));

        jLabel15.setBackground(new java.awt.Color(255, 255, 204));
        jLabel15.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("l");
        jLabel15.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel15.setOpaque(true);
        jLabel15.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        PanelProductos.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 50, 30, 30));

        jLabel16.setBackground(new java.awt.Color(255, 255, 204));
        jLabel16.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("m");
        jLabel16.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel16.setOpaque(true);
        jLabel16.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        PanelProductos.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 70, 30, 30));

        jLabel17.setBackground(new java.awt.Color(255, 255, 204));
        jLabel17.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("a");
        jLabel17.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel17.setOpaque(true);
        jLabel17.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        PanelProductos.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 90, 30, 30));

        jLabel18.setBackground(new java.awt.Color(255, 255, 204));
        jLabel18.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("c");
        jLabel18.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel18.setOpaque(true);
        jLabel18.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        PanelProductos.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 110, 30, 30));

        jLabel19.setBackground(new java.awt.Color(255, 255, 204));
        jLabel19.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("é");
        jLabel19.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel19.setOpaque(true);
        jLabel19.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        PanelProductos.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 130, 30, 30));

        jLabel20.setBackground(new java.awt.Color(255, 255, 204));
        jLabel20.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("n");
        jLabel20.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel20.setOpaque(true);
        jLabel20.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        PanelProductos.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 150, 30, 30));

        getContentPane().add(PanelProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 270, 360, 210));

        TiendaPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(255, 255, 255)));
        TiendaPanel.setOpaque(false);
        TiendaPanel.setLayout(null);

        L5.setBackground(new java.awt.Color(255, 255, 181));
        L5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(255, 255, 0), new java.awt.Color(255, 255, 51), new java.awt.Color(255, 255, 51)));
        TiendaPanel.add(L5);
        L5.setBounds(380, 300, 60, 30);

        L4.setBackground(new java.awt.Color(255, 255, 181));
        L4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(255, 255, 0), new java.awt.Color(255, 255, 51), new java.awt.Color(255, 255, 51)));
        TiendaPanel.add(L4);
        L4.setBounds(380, 180, 60, 30);

        L1.setBackground(new java.awt.Color(255, 255, 181));
        L1.setFont(new java.awt.Font("Arial Black", 1, 24)); // NOI18N
        L1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(255, 255, 0), new java.awt.Color(255, 255, 51), new java.awt.Color(255, 255, 51)));
        TiendaPanel.add(L1);
        L1.setBounds(80, 180, 60, 30);

        L2.setBackground(new java.awt.Color(255, 255, 181));
        L2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(255, 255, 0), new java.awt.Color(255, 255, 51), new java.awt.Color(255, 255, 51)));
        TiendaPanel.add(L2);
        L2.setBounds(80, 300, 60, 30);

        L7.setBackground(new java.awt.Color(255, 255, 181));
        L7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(255, 255, 0), new java.awt.Color(255, 255, 51), new java.awt.Color(255, 255, 51)));
        TiendaPanel.add(L7);
        L7.setBounds(640, 180, 60, 30);

        L8.setBackground(new java.awt.Color(255, 255, 181));
        L8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(255, 255, 0), new java.awt.Color(255, 255, 51), new java.awt.Color(255, 255, 51)));
        TiendaPanel.add(L8);
        L8.setBounds(640, 300, 60, 30);

        L6.setBackground(new java.awt.Color(255, 255, 181));
        L6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(255, 255, 0), new java.awt.Color(255, 255, 51), new java.awt.Color(255, 255, 51)));
        TiendaPanel.add(L6);
        L6.setBounds(370, 420, 60, 30);

        L3.setBackground(new java.awt.Color(255, 255, 181));
        L3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(255, 255, 0), new java.awt.Color(255, 255, 51), new java.awt.Color(255, 255, 51)));
        TiendaPanel.add(L3);
        L3.setBounds(80, 410, 60, 30);
        TiendaPanel.add(Carr2);
        Carr2.setBounds(0, 0, 80, 50);
        TiendaPanel.add(Carr1);
        Carr1.setBounds(1040, 0, 80, 50);

        Cj2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.black, null, null));
        Cj2.setOpaque(true);
        TiendaPanel.add(Cj2);
        Cj2.setBounds(860, 240, 30, 20);

        Cj3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.black, null, null));
        Cj3.setOpaque(true);
        TiendaPanel.add(Cj3);
        Cj3.setBounds(860, 290, 30, 20);

        Cj1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.black, null, null));
        Cj1.setOpaque(true);
        TiendaPanel.add(Cj1);
        Cj1.setBounds(860, 190, 30, 20);
        TiendaPanel.add(Camion);
        Camion.setBounds(990, 380, 100, 50);
        TiendaPanel.add(Camion1);
        Camion1.setBounds(990, 360, 100, 50);

        getContentPane().add(TiendaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 1110, 590));

        FondoTienda.setOpaque(true);
        getContentPane().add(FondoTienda, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 1110, 590));

        Fondo.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(0, 0, 0)));
        getContentPane().add(Fondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1470, 680));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void InfoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_InfoMousePressed
        Inf inf=new Inf(this,true);
        inf.setVisible(true);
    }//GEN-LAST:event_InfoMousePressed

    private void InicioMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_InicioMousePressed
        Cad = "";
        int cls = (int)((Math.random()*6)+2);
        int Val = 1050+(25*cls);
        for(i=0;i<cls;i++){
            TClients++;
            ClienteTienda C;
            
            if(((int)((Math.random()*10))) %2==0){
                C = new ClienteTienda(TClients,0);
                Cad = Cad+" Nuevo cliente (No. "+TClients+") Hombre: Esperando...\n";
            }else{
                C = new ClienteTienda(TClients,1);
                Cad = Cad+" Nuevo cliente (No. "+TClients+") Mujer: Esperando...\n";
            }
            
            //Añadiendo cliente al panel...
            TiendaPanel.add(C);
            C.setBounds(1050,160,38,30);
            //Control del cliente (añadiendo a lista)
            Clts.add(C);
            
            // Hilo que encamina a los clientes...
            MovIni Movs = new MovIni(Control,C,TClients,Val);
            Movs.start();
            Val-=50;
        }
        
        SonidoCliente SoundCliente = new SonidoCliente();
        SoundCliente.start();
        
        //SonidoFondo MusicFondo = new SonidoFondo();
        //MusicFondo.start();
        
        Iniciado = new Inicio(Control);
        Iniciado.start();
                       
        InfoTienda.setText(Cad);
        InfoTienda.setCaretPosition(InfoTienda.getDocument().getLength());
        
        TClient.setText(""+TClients);
        Inicio.setVisible(false);
        
        AgrH.setVisible(true);
        AgrM.setVisible(true);
        Aleat.setVisible(true);
    }//GEN-LAST:event_InicioMousePressed

    private void BServidorMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BServidorMousePressed
        if(!ActServ){
            ActServ = !ActServ;
            
            //Activando servidor
            HiloServ = new ServidorActv(Control);
            HiloServ.start(); //Inicia Hilo....
            
            BServidor.setText("- Servidor Activo -");
            BServidor.setBackground(new java.awt.Color(255,204,204));
            //BServidor.setEnabled(false);
        }

    }//GEN-LAST:event_BServidorMousePressed

    private void AgrHMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AgrHMousePressed
        //TClients++;
        ClienteTienda C;
            
        C = new ClienteTienda(TClients,0);
        Cad = "\n Nuevo cliente (No. "+TClients+") Hombre: Esperando...\n";
        
        InfoTienda.setText(InfoTienda.getText()+Cad);
        InfoTienda.setCaretPosition(InfoTienda.getDocument().getLength());
        
        //TClient.setText(""+TClients);
        Inicio.setVisible(false);
        
        MovIni Movs = new MovIni(Control, C,TClients, 1050);
        Control.ActNumClts(true);
        Movs.start();
        
        SonidoCliente SoundCliente = new SonidoCliente();
        SoundCliente.start();
        
        //Añadiendo cliente al panel...
        TiendaPanel.add(C);
        C.setBounds(1050,160,38,30);
        //Control del cliente (añadiendo a lista)
        Clts.add(C);
    }//GEN-LAST:event_AgrHMousePressed

    private void AgrMMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AgrMMousePressed
        //TClients++;
        ClienteTienda C;
            
        C = new ClienteTienda(TClients,1);
        Cad = "\n Nuevo cliente (No. "+TClients+") Mujer: Esperando...\n";
        InfoTienda.setText(InfoTienda.getText()+Cad);
        InfoTienda.setCaretPosition(InfoTienda.getDocument().getLength());
        
        //TClient.setText(""+TClients);
        Inicio.setVisible(false);
        
        MovIni Movs = new MovIni(Control, C,TClients,1050);
        Control.ActNumClts(true);
        Movs.start();
        
        SonidoCliente SoundCliente = new SonidoCliente();
        SoundCliente.start();
        
        //Añadiendo cliente al panel...
        TiendaPanel.add(C);
        C.setBounds(1050,160,38,30);
        //Control del cliente (añadiendo a lista)
        Clts.add(C);
    }//GEN-LAST:event_AgrMMousePressed

    private void AleatMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AleatMousePressed
        Cad = "\n";
        int cls = (int)((Math.random()*6)+2);
        int Val = 1050+(25*cls);
        for(i=0;i<cls;i++){
            //TClients++;
            ClienteTienda C;
            
            if(((int)((Math.random()*10))) %2==0){
                C = new ClienteTienda((TClients+(i+1)),0);
                Cad = Cad+" Nuevo cliente (No. "+(TClients+(i+1))+") Hombre: Esperando...\n";
            }else{
                C = new ClienteTienda((TClients+(i+1)),1);
                Cad = Cad+" Nuevo cliente (No. "+(TClients+(i+1))+") Mujer: Esperando...\n";
            }
            
            //Añadiendo cliente al panel...
            TiendaPanel.add(C);
            C.setBounds(1050,160,38,30);
            //Control del cliente (añadiendo a lista)
            Clts.add(C);
            
            // Hilo que encamina a los clientes...
            MovIni Movs = new MovIni(Control,C,(TClients+(i+1)),Val);
            Control.ActNumClts(true);
            Movs.start();
            Val-=50;
        }
        SonidoCliente SoundCliente = new SonidoCliente();
        SoundCliente.start();
        
        InfoTienda.setText(InfoTienda.getText()+Cad);
        InfoTienda.setCaretPosition(InfoTienda.getDocument().getLength());
        //TClient.setText(""+TClients);
    }//GEN-LAST:event_AleatMousePressed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Ejec_S.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Ejec_S().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JTextField A1;
    public javax.swing.JTextField A2;
    public javax.swing.JTextField A3;
    public javax.swing.JTextField A4;
    public javax.swing.JTextField A5;
    public javax.swing.JTextField A6;
    private javax.swing.JButton AgrH;
    private javax.swing.JButton AgrM;
    private javax.swing.JButton Aleat;
    private javax.swing.JButton BServidor;
    private javax.swing.JLabel Camion;
    private javax.swing.JLabel Camion1;
    private javax.swing.JLabel Carr1;
    private javax.swing.JLabel Carr2;
    public javax.swing.JButton Cj1;
    public javax.swing.JButton Cj2;
    public javax.swing.JButton Cj3;
    private javax.swing.JTextField D1;
    private javax.swing.JTextField D2;
    private javax.swing.JTextField D3;
    private javax.swing.JTextField D4;
    private javax.swing.JTextField D5;
    private javax.swing.JTextField D6;
    private javax.swing.JLabel EtqH;
    private javax.swing.JLabel EtqM;
    private javax.swing.JLabel Fondo;
    private javax.swing.JLabel FondoTienda;
    private javax.swing.JLabel IH;
    private javax.swing.JLabel IM;
    private javax.swing.JButton Info;
    public javax.swing.JTextArea InfoLinea;
    public javax.swing.JTextArea InfoTienda;
    private javax.swing.JButton Inicio;
    private javax.swing.JButton L1;
    private javax.swing.JButton L2;
    private javax.swing.JButton L3;
    private javax.swing.JButton L4;
    private javax.swing.JButton L5;
    private javax.swing.JButton L6;
    private javax.swing.JButton L7;
    private javax.swing.JButton L8;
    private javax.swing.JPanel PanelClientes;
    private javax.swing.JPanel PanelControl;
    private javax.swing.JPanel PanelEnLinea;
    private javax.swing.JPanel PanelProductos;
    private javax.swing.JPanel PanelTitulo;
    public javax.swing.JLabel TClient;
    public javax.swing.JLabel TClientOn;
    public javax.swing.JPanel TiendaPanel;
    private javax.swing.JLabel TituloClientes;
    private javax.swing.JLabel TituloControl;
    private javax.swing.JLabel TituloEnLinea;
    private javax.swing.JLabel TituloEnProductos;
    private javax.swing.JLabel TituloPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
