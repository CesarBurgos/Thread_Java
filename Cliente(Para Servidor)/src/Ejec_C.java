import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.JOptionPane;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Conexion.
 * Clase que establece la conexión con el servidor (tienda)...
 * @author Cesar Brgs
 */
class Conexion{
    //Para comunicación con servidor
    Socket Cliente = null;
    PrintWriter ATienda;
    //BufferedReader delTeclado;
    DataInputStream DeTienda;
    String SolicitudPed,NombClient,aux,aux2[];
    Ejec_C Ref;
    int i;
    
    public Conexion(Ejec_C RefVent,String NombClt){
        Ref = RefVent;
        
        NombClient="-";
        //Relizando conexión con el servidor
        String IP = "127.0.0."+(int)((Math.random()*240)+1);
        try{
            try{
                Cliente = new Socket(IP,5432);   
                NombClient = NombClt;
                JOptionPane.showMessageDialog(null, "- Bienvedid@ -\n"+NombClient+"\nLa tienda esta activa...", " Tienda dice:",  JOptionPane.INFORMATION_MESSAGE,null);
            }catch (UnknownHostException e){
                JOptionPane.showMessageDialog(null, "Ha ocurrido un error", " Tienda dice:",  JOptionPane.WARNING_MESSAGE);
            }
            
            ATienda = new PrintWriter(Cliente.getOutputStream(),true);
            DeTienda = new DataInputStream(Cliente.getInputStream());
            ATienda.println(NombClient);
            //Recibiendo el numero maximo de productos
            aux = DeTienda.readUTF();
            
            aux2 = aux.split("-");
            for(i=1; i<aux2.length; i++){
                Ref.Max[i-1] = Integer.parseInt(aux2[i]);
            }
            
        }catch (IOException e){
            JOptionPane.showMessageDialog(null,"Por el momento, la tienda no esta activa...", " Tienda dice: ",  JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void CerrarConex(int op) throws IOException{
        if(op==0){
            ATienda.println("X");
            JOptionPane.showMessageDialog(null,"Gracias por su vista...\n Esperemos vuelva prontó...", " Tienda dice: ", JOptionPane.INFORMATION_MESSAGE,null);
        }else if(op==1){
            JOptionPane.showMessageDialog(null,"La tienda no esta activa...", " Tienda dice: ",  JOptionPane.WARNING_MESSAGE);            
        }
        
        if(DeTienda!=null && ATienda!=null && Cliente!=null){
            if(op==-1){
                ATienda.println("X");
                JOptionPane.showMessageDialog(null,"Gracias por su vista...\n Esperemos vuelva prontó...", " Tienda dice: ", JOptionPane.INFORMATION_MESSAGE,null);
            }
            
            DeTienda.close();
            ATienda.close();
            Cliente.close();
        }
    }

    public void Pedido(String pedido){
        ATienda.println(pedido);
        try {
            //Recibiendo el numero maximo de productos
            aux = DeTienda.readUTF();
            aux2 = aux.split("-");
            for(i=1; i<aux2.length; i++){
                Ref.Max[i-1] = Integer.parseInt(aux2[i]);
            }
        } catch (IOException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

/**
 * Ejec_C.
 * Clase "Pricipal" - Controla lo relacionado a los elementos de la ventana...
 * @author Cesar Brgs
 */
public class Ejec_C extends javax.swing.JFrame {
    int i,Cants[],Max[],aux;
    String cad;
    Ejec_C Ref = this;
    Conexion c;
    
    public Ejec_C() {
        initComponents();
        Ref.setLocationRelativeTo(null);
        Ref.setTitle("Programación concurrente (Threads) - Cliente en línea");
        
        Image Logo=Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("Icono.png"));
        Ref.setIconImage(Logo);
        
        //Cantidades máximas a solicitar por producto
        Max = new int[6];
        //Cantidades del producto solicitado
        Cants = new int[6];
        
        //Reseteo de los valores
        Reseteo();
    }
    
    //Función que reinicia los valores
    public final void Reseteo(){
        c = null;
        NombClt.setText("");
        Pedido.setVisible(false);
        
        for(i=0; i<6; i++)
            Cants[i]=0;
        
        CantFrutVeg.setText(""+Cants[0]);
        mFrut.setVisible(false);
        
        CantLact.setText(""+Cants[1]);
        mLac.setVisible(false);
        
        CantCarnes.setText(""+Cants[2]);
        mCarn.setVisible(false);
        
        CantFarmacia.setText(""+Cants[3]);
        mFarm.setVisible(false);
        
        CantBlancHogr.setText(""+Cants[4]);
        mHogr.setVisible(false);
        
        CantPanCereal.setText(""+Cants[5]);
        mPan.setVisible(false);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        NombClt = new javax.swing.JTextField();
        Ingreso = new javax.swing.JButton();
        Pedido = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        Solicitud = new javax.swing.JButton();
        MPan = new javax.swing.JButton();
        mPan = new javax.swing.JButton();
        CantPanCereal = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        CantFarmacia = new javax.swing.JTextField();
        mFarm = new javax.swing.JButton();
        MFarm = new javax.swing.JButton();
        CantFrutVeg = new javax.swing.JTextField();
        mFrut = new javax.swing.JButton();
        MFrut = new javax.swing.JButton();
        CantLact = new javax.swing.JTextField();
        mLac = new javax.swing.JButton();
        MLac = new javax.swing.JButton();
        CantCarnes = new javax.swing.JTextField();
        mCarn = new javax.swing.JButton();
        MCarn = new javax.swing.JButton();
        CantBlancHogr = new javax.swing.JTextField();
        mHogr = new javax.swing.JButton();
        MHogr = new javax.swing.JButton();
        Salida = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setBackground(new java.awt.Color(255, 255, 204));
        jLabel1.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Cliente en línea de la tienda departamental");
        jLabel1.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 0, 2, new java.awt.Color(0, 0, 0)));
        jLabel1.setOpaque(true);
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 650, 40));

        jPanel1.setBackground(new java.awt.Color(255, 255, 204));
        jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel2.setText("Nombre del cliente:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, 40));

        NombClt.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        NombClt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(NombClt, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 20, 305, 40));

        Ingreso.setBackground(new java.awt.Color(204, 255, 255));
        Ingreso.setFont(new java.awt.Font("sansserif", 1, 15)); // NOI18N
        Ingreso.setText("Ingresar a la tienda");
        Ingreso.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Ingreso.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                IngresoMousePressed(evt);
            }
        });
        jPanel1.add(Ingreso, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 30, -1, 30));

        Pedido.setBackground(new java.awt.Color(255, 199, 172));
        Pedido.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(0, 0, 0)));
        Pedido.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Century Gothic", 1, 17)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Indique la cantidad de productos a extraer de cada departamento:");
        Pedido.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 610, 40));

        Solicitud.setBackground(new java.awt.Color(204, 255, 204));
        Solicitud.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        Solicitud.setText("Solicitar pedido");
        Solicitud.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Solicitud.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                SolicitudMousePressed(evt);
            }
        });
        Pedido.add(Solicitud, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, 310, 40));

        MPan.setBackground(new java.awt.Color(204, 255, 204));
        MPan.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        MPan.setText("+");
        MPan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MPan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                MPanMousePressed(evt);
            }
        });
        Pedido.add(MPan, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 120, -1, -1));

        mPan.setBackground(new java.awt.Color(204, 255, 255));
        mPan.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        mPan.setText("-");
        mPan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        mPan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mPanMousePressed(evt);
            }
        });
        Pedido.add(mPan, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 120, -1, 30));

        CantPanCereal.setEditable(false);
        CantPanCereal.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        CantPanCereal.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        CantPanCereal.setAutoscrolls(false);
        Pedido.add(CantPanCereal, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 120, 50, -1));

        jLabel5.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Pan y cereales:");
        Pedido.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 120, 140, 30));

        jLabel7.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Farmacia:");
        Pedido.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 40, 140, 30));

        jLabel6.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Frutas y vegetales:");
        Pedido.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 140, 30));

        jLabel8.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Blancos y hogar:");
        Pedido.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 80, 140, 30));

        jLabel9.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Lácteos:");
        Pedido.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 140, 30));

        jLabel10.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Carnes:");
        Pedido.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 140, 30));

        CantFarmacia.setEditable(false);
        CantFarmacia.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        CantFarmacia.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        CantFarmacia.setAutoscrolls(false);
        Pedido.add(CantFarmacia, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 40, 50, -1));

        mFarm.setBackground(new java.awt.Color(204, 255, 255));
        mFarm.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        mFarm.setText("-");
        mFarm.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        mFarm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mFarmMousePressed(evt);
            }
        });
        Pedido.add(mFarm, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 40, -1, -1));

        MFarm.setBackground(new java.awt.Color(204, 255, 204));
        MFarm.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        MFarm.setText("+");
        MFarm.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MFarm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                MFarmMousePressed(evt);
            }
        });
        Pedido.add(MFarm, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 40, -1, -1));

        CantFrutVeg.setEditable(false);
        CantFrutVeg.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        CantFrutVeg.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        CantFrutVeg.setAutoscrolls(false);
        Pedido.add(CantFrutVeg, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 40, 50, -1));

        mFrut.setBackground(new java.awt.Color(204, 255, 255));
        mFrut.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        mFrut.setText("-");
        mFrut.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        mFrut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mFrutMousePressed(evt);
            }
        });
        Pedido.add(mFrut, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 40, -1, -1));

        MFrut.setBackground(new java.awt.Color(204, 255, 204));
        MFrut.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        MFrut.setText("+");
        MFrut.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MFrut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                MFrutMousePressed(evt);
            }
        });
        Pedido.add(MFrut, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 40, -1, -1));

        CantLact.setEditable(false);
        CantLact.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        CantLact.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        CantLact.setAutoscrolls(false);
        Pedido.add(CantLact, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 80, 50, -1));

        mLac.setBackground(new java.awt.Color(204, 255, 255));
        mLac.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        mLac.setText("-");
        mLac.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        mLac.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mLacMousePressed(evt);
            }
        });
        Pedido.add(mLac, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 80, -1, -1));

        MLac.setBackground(new java.awt.Color(204, 255, 204));
        MLac.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        MLac.setText("+");
        MLac.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MLac.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                MLacMousePressed(evt);
            }
        });
        Pedido.add(MLac, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 80, -1, -1));

        CantCarnes.setEditable(false);
        CantCarnes.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        CantCarnes.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        CantCarnes.setAutoscrolls(false);
        Pedido.add(CantCarnes, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 120, 50, -1));

        mCarn.setBackground(new java.awt.Color(204, 255, 255));
        mCarn.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        mCarn.setText("-");
        mCarn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        mCarn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mCarnMousePressed(evt);
            }
        });
        Pedido.add(mCarn, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 120, -1, -1));

        MCarn.setBackground(new java.awt.Color(204, 255, 204));
        MCarn.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        MCarn.setText("+");
        MCarn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MCarn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                MCarnMousePressed(evt);
            }
        });
        Pedido.add(MCarn, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 120, -1, -1));

        CantBlancHogr.setEditable(false);
        CantBlancHogr.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        CantBlancHogr.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        CantBlancHogr.setAutoscrolls(false);
        Pedido.add(CantBlancHogr, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 80, 50, -1));

        mHogr.setBackground(new java.awt.Color(204, 255, 255));
        mHogr.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        mHogr.setText("-");
        mHogr.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        mHogr.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mHogrMousePressed(evt);
            }
        });
        Pedido.add(mHogr, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 80, -1, -1));

        MHogr.setBackground(new java.awt.Color(204, 255, 204));
        MHogr.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        MHogr.setText("+");
        MHogr.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MHogr.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                MHogrMousePressed(evt);
            }
        });
        Pedido.add(MHogr, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 80, -1, -1));

        Salida.setBackground(new java.awt.Color(255, 204, 204));
        Salida.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        Salida.setText("Salir");
        Salida.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Salida.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                SalidaMousePressed(evt);
            }
        });
        Pedido.add(Salida, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 160, 310, 40));

        jPanel1.add(Pedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 650, 210));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 670, 290));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void IngresoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_IngresoMousePressed
        if(NombClt.getText().length()==0)
            JOptionPane.showMessageDialog(null, "No ha ingresado un nombre que lo identifique...", " Tienda dice: ",  JOptionPane.INFORMATION_MESSAGE,null);
        else{
            //Creando una posible conexión con el servidor
            c = new Conexion(Ref, NombClt.getText());

            if(!c.NombClient.equals("-")){
                Ingreso.setVisible(false);
                Pedido.setVisible(true);
                NombClt.setEditable(false);
            }
        }
    }//GEN-LAST:event_IngresoMousePressed

    private void MPanMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MPanMousePressed
        //Incrementando solicitud Pan
        if(Cants[5]<Max[5]){
            Cants[5]++;
            CantPanCereal.setText(""+Cants[5]);
            if(Cants[5]==Max[5])
                MPan.setVisible(false);
        }mPan.setVisible(true);
    }//GEN-LAST:event_MPanMousePressed

    private void mPanMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mPanMousePressed
        //Decrementando solicitud Pan
        if(Cants[5]>0){
            Cants[5]--;
            CantPanCereal.setText(""+Cants[5]);
            if(Cants[5]==0)
                mPan.setVisible(false);
        }MPan.setVisible(true);
    }//GEN-LAST:event_mPanMousePressed

    private void MFarmMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MFarmMousePressed
        //Incrementando solicitud Farmacia
        if(Cants[3]<Max[3]){
            Cants[3]++;
            CantFarmacia.setText(""+Cants[3]);
            if(Cants[3]==Max[3])
                MFarm.setVisible(false);
        }mFarm.setVisible(true);
    }//GEN-LAST:event_MFarmMousePressed

    private void mFarmMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mFarmMousePressed
        //Decrementando solicitud Farmacia
        if(Cants[3]>0){
            Cants[3]--;
            CantFarmacia.setText(""+Cants[3]);
            if(Cants[3]==0)
                mFarm.setVisible(false);
        }MFarm.setVisible(true);
    }//GEN-LAST:event_mFarmMousePressed

    private void MFrutMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MFrutMousePressed
        //Incrementando solicitud Frutas y verduras
        if(Cants[0]<Max[0]){
            Cants[0]++;
            CantFrutVeg.setText(""+Cants[0]);
            if(Cants[0]==Max[0])
                MFrut.setVisible(false);
        }mFrut.setVisible(true);
    }//GEN-LAST:event_MFrutMousePressed

    private void mFrutMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mFrutMousePressed
        //Decrementando solicitud Frutas y verduras
        if(Cants[0]>0){
            Cants[0]--;
            CantFrutVeg.setText(""+Cants[0]);
            if(Cants[0]==0)
                mFrut.setVisible(false);
        }MFrut.setVisible(true);
    }//GEN-LAST:event_mFrutMousePressed

    private void MHogrMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MHogrMousePressed
        //Incrementando solicitud Hogar y blancos
        if(Cants[4]<Max[4]){
            Cants[4]++;
            CantBlancHogr.setText(""+Cants[4]);
            if(Cants[4]==Max[4])
                MHogr.setVisible(false);
        }mHogr.setVisible(true);
    }//GEN-LAST:event_MHogrMousePressed

    private void mHogrMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mHogrMousePressed
        //Decrementando solicitud Hogar y blancos
        if(Cants[4]>0){
            Cants[4]--;
            CantBlancHogr.setText(""+Cants[4]);
            if(Cants[4]==0)
                mHogr.setVisible(false);
        }MHogr.setVisible(true);
    }//GEN-LAST:event_mHogrMousePressed

    private void MLacMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MLacMousePressed
        //Incrementando solicitud Lacteos
        if(Cants[1]<Max[1]){
            Cants[1]++;
            CantLact.setText(""+Cants[1]);
            if(Cants[1]==Max[1])
                MLac.setVisible(false);
        }mLac.setVisible(true);
    }//GEN-LAST:event_MLacMousePressed

    private void mLacMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mLacMousePressed
        //Decrementando solicitud Hogar y blancos
        if(Cants[1]>0){
            Cants[1]--;
            CantLact.setText(""+Cants[1]);
            if(Cants[1]==0)
                mLac.setVisible(false);
        }MLac.setVisible(true);
    }//GEN-LAST:event_mLacMousePressed

    private void MCarnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MCarnMousePressed
        //Incrementando solicitud Carne
        if(Cants[2]<Max[2]){
            Cants[2]++;
            CantCarnes.setText(""+Cants[2]);
            if(Cants[2]==Max[2])
                MCarn.setVisible(false);
        }mCarn.setVisible(true);
    }//GEN-LAST:event_MCarnMousePressed

    private void mCarnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mCarnMousePressed
        //Decrementando solicitud Carne
        if(Cants[2]>0){
            Cants[2]--;
            CantCarnes.setText(""+Cants[2]);
            if(Cants[2]==0)
                mCarn.setVisible(false);
        }MCarn.setVisible(true);
    }//GEN-LAST:event_mCarnMousePressed

    private void SolicitudMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SolicitudMousePressed
        //Solicitando productos
        aux = 0;
        cad = "";
        for(i=0; i<6; i++){
            if(Cants[i] == 0)
                aux++;
            cad=cad+"-"+Cants[i];
        }
        
        if(aux == 6){
            JOptionPane.showMessageDialog(null,"Al parecer no ha solicitado ningún producto...", " Tienda dice: ", JOptionPane.INFORMATION_MESSAGE,null);
        }else{
            c.Pedido(cad);
        }
    }//GEN-LAST:event_SolicitudMousePressed

    private void SalidaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SalidaMousePressed
        NombClt.setEditable(true);
        Ingreso.setVisible(true);
        try {
            // Desconectar del servidor
            c.CerrarConex(0);
        } catch (IOException ex) {
            Logger.getLogger(Ejec_C.class.getName()).log(Level.SEVERE, null, ex);
        }
        Reseteo();
        Ref.dispose();
    }//GEN-LAST:event_SalidaMousePressed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if(c!=null){
            try {
                // Desconectar del servidor
                c.CerrarConex(-1);
            } catch (IOException ex) {
                Logger.getLogger(Ejec_C.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_formWindowClosing

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Ejec_C().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField CantBlancHogr;
    private javax.swing.JTextField CantCarnes;
    private javax.swing.JTextField CantFarmacia;
    private javax.swing.JTextField CantFrutVeg;
    private javax.swing.JTextField CantLact;
    private javax.swing.JTextField CantPanCereal;
    private javax.swing.JButton Ingreso;
    private javax.swing.JButton MCarn;
    private javax.swing.JButton MFarm;
    private javax.swing.JButton MFrut;
    private javax.swing.JButton MHogr;
    private javax.swing.JButton MLac;
    private javax.swing.JButton MPan;
    private javax.swing.JTextField NombClt;
    private javax.swing.JPanel Pedido;
    private javax.swing.JButton Salida;
    private javax.swing.JButton Solicitud;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton mCarn;
    private javax.swing.JButton mFarm;
    private javax.swing.JButton mFrut;
    private javax.swing.JButton mHogr;
    private javax.swing.JButton mLac;
    private javax.swing.JButton mPan;
    // End of variables declaration//GEN-END:variables
}
