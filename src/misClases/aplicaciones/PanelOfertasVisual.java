/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PanelOfertasVisual.java
 *
 * Created on 11-mar-2015, 17:50:01
 */

package misClases.aplicaciones;

import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import misClases.aplicaciones.resources.ArticuloOferta;

/**
 *
 * @author Francisco Portela Henche
 * 
 * Esta clase crea un panel que contiene una serie de Check Boxes los cuales
 * indican, cuando están activados, cuales de los diferentes productos de la
 * oferta han sido ya apuntados. De esta forma se aprecia, de manera muy visual,
 * los que aún faltan por anotar ó pedir.
 */
public class PanelOfertasVisual extends javax.swing.JPanel {
    private final ArrayList<ArticuloOferta> listado;
    private final ArrayList<JCheckBox> listadoChecks = new ArrayList<JCheckBox>();
    //private Vector<ArticuloOferta> v;
    //private Vector<JCheckBox> vectorChecks = new Vector<JCheckBox>();

    /**
     * Constructor de la clase
     * @param listado un ArrayList que contiene los artículos apuntados 
     * actualmente en la lista de las ofertas.
     */
    public PanelOfertasVisual(ArrayList<ArticuloOferta> listado) {
        this.listado = listado;
        initComponents();
        obtenerCheckBoxes();
        comprobarOfertas();
    }

    /**
     * Este método obtiene el nombre de cada uno de los artículos contenidos
     * en el vector de artículos y luego le pasa dicho nombre al método
     * 'comprobarNombreArticulo'
     */
    private void comprobarOfertas(){
        String nombre;
        for(int i = 0; i < listado.size(); i++){
            nombre = this.listado.get(i).getNombreArticulo();
            comprobarNombreArticulo(nombre);
        }
    }

    /**
     * Este método comprueba si en el nombre del artículo pasado como
     * parámetro está contenido el texto de alguno de los check boxes (los
     * cuales contienen los nombres de los posibles artículos que se pueden
     * incluir en la oferta).
     * En el caso de que dicho texto esté en el nombre del artículo, se
     * selecciona dicho check box haciendo una pulsación por sofware y se
     * elimina del vector de check boxes para no volverlo a comprobar a
     * posteriori.
     * @param n el nombre del artículo que vamos a comprobar.
     */
    private void comprobarNombreArticulo(String n){
        JCheckBox j;
        String texto;
        for(int i = 0; i < this.listadoChecks.size(); i++){
            j = this.listadoChecks.get(i);
            texto = j.getText();
            if(n.contains(texto)){
                j.doClick();
                this.listadoChecks.remove(i);
                break;
            }
        }
    }

    /**
     * Este método obtiene todos los check boxes que contiene el panel, para
     * almacenarlos en un vector que luego usaremos para recorrerlos uno a uno.
     */
    private void obtenerCheckBoxes(){
        JCheckBox jcb;
        for(int i = 0; i < this.getComponentCount(); i++){
            Object o = this.getComponent(i);
            if(o instanceof JCheckBox){
                jcb = (JCheckBox)o;
                jcb.setFocusable(false);
                this.listadoChecks.add(jcb);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jCheckBox8 = new javax.swing.JCheckBox();
        jCheckBox9 = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jCheckBox10 = new javax.swing.JCheckBox();
        jCheckBox11 = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jCheckBox12 = new javax.swing.JCheckBox();
        jCheckBox13 = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jCheckBox14 = new javax.swing.JCheckBox();
        jCheckBox15 = new javax.swing.JCheckBox();
        jCheckBox16 = new javax.swing.JCheckBox();
        jCheckBox17 = new javax.swing.JCheckBox();
        jCheckBox18 = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jCheckBox19 = new javax.swing.JCheckBox();
        jCheckBox20 = new javax.swing.JCheckBox();
        jCheckBox21 = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        jCheckBox22 = new javax.swing.JCheckBox();
        jCheckBox23 = new javax.swing.JCheckBox();
        jCheckBox24 = new javax.swing.JCheckBox();
        jCheckBox25 = new javax.swing.JCheckBox();
        jCheckBox26 = new javax.swing.JCheckBox();
        jCheckBox27 = new javax.swing.JCheckBox();
        jCheckBox28 = new javax.swing.JCheckBox();
        jCheckBox29 = new javax.swing.JCheckBox();
        jCheckBox30 = new javax.swing.JCheckBox();
        jCheckBox31 = new javax.swing.JCheckBox();
        jCheckBox32 = new javax.swing.JCheckBox();
        jCheckBox33 = new javax.swing.JCheckBox();
        jCheckBox34 = new javax.swing.JCheckBox();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(misClases.aplicaciones.Ofertas.class).getContext().getResourceMap(PanelOfertasVisual.class);
        setBackground(resourceMap.getColor("Form.background")); // NOI18N
        setName("Form"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jCheckBox1.setForeground(resourceMap.getColor("jCheckBox1.foreground")); // NOI18N
        jCheckBox1.setText(resourceMap.getString("jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox2.setForeground(resourceMap.getColor("jCheckBox2.foreground")); // NOI18N
        jCheckBox2.setText(resourceMap.getString("jCheckBox2.text")); // NOI18N
        jCheckBox2.setName("jCheckBox2"); // NOI18N
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jCheckBox3.setForeground(resourceMap.getColor("jCheckBox3.foreground")); // NOI18N
        jCheckBox3.setText(resourceMap.getString("jCheckBox3.text")); // NOI18N
        jCheckBox3.setName("jCheckBox3"); // NOI18N
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox4.setForeground(resourceMap.getColor("jCheckBox4.foreground")); // NOI18N
        jCheckBox4.setText(resourceMap.getString("jCheckBox4.text")); // NOI18N
        jCheckBox4.setName("jCheckBox4"); // NOI18N
        jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox5.setForeground(resourceMap.getColor("jCheckBox5.foreground")); // NOI18N
        jCheckBox5.setText(resourceMap.getString("jCheckBox5.text")); // NOI18N
        jCheckBox5.setName("jCheckBox5"); // NOI18N
        jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jCheckBox6.setForeground(resourceMap.getColor("jCheckBox6.foreground")); // NOI18N
        jCheckBox6.setText(resourceMap.getString("jCheckBox6.text")); // NOI18N
        jCheckBox6.setName("jCheckBox6"); // NOI18N
        jCheckBox6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox7.setForeground(resourceMap.getColor("jCheckBox7.foreground")); // NOI18N
        jCheckBox7.setText(resourceMap.getString("jCheckBox7.text")); // NOI18N
        jCheckBox7.setName("jCheckBox7"); // NOI18N
        jCheckBox7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox8.setForeground(resourceMap.getColor("jCheckBox8.foreground")); // NOI18N
        jCheckBox8.setText(resourceMap.getString("jCheckBox8.text")); // NOI18N
        jCheckBox8.setName("jCheckBox8"); // NOI18N
        jCheckBox8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox9.setForeground(resourceMap.getColor("jCheckBox9.foreground")); // NOI18N
        jCheckBox9.setText(resourceMap.getString("jCheckBox9.text")); // NOI18N
        jCheckBox9.setName("jCheckBox9"); // NOI18N
        jCheckBox9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jLabel4.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jCheckBox10.setForeground(resourceMap.getColor("jCheckBox10.foreground")); // NOI18N
        jCheckBox10.setText(resourceMap.getString("jCheckBox10.text")); // NOI18N
        jCheckBox10.setName("jCheckBox10"); // NOI18N
        jCheckBox10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox11.setForeground(resourceMap.getColor("jCheckBox11.foreground")); // NOI18N
        jCheckBox11.setText(resourceMap.getString("jCheckBox11.text")); // NOI18N
        jCheckBox11.setName("jCheckBox11"); // NOI18N
        jCheckBox11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jCheckBox12.setForeground(resourceMap.getColor("jCheckBox12.foreground")); // NOI18N
        jCheckBox12.setText(resourceMap.getString("jCheckBox12.text")); // NOI18N
        jCheckBox12.setName("jCheckBox12"); // NOI18N
        jCheckBox12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox13.setForeground(resourceMap.getColor("jCheckBox13.foreground")); // NOI18N
        jCheckBox13.setText(resourceMap.getString("jCheckBox13.text")); // NOI18N
        jCheckBox13.setName("jCheckBox13"); // NOI18N
        jCheckBox13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jLabel6.setFont(resourceMap.getFont("jLabel6.font")); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jCheckBox14.setForeground(resourceMap.getColor("jCheckBox14.foreground")); // NOI18N
        jCheckBox14.setText(resourceMap.getString("jCheckBox14.text")); // NOI18N
        jCheckBox14.setName("jCheckBox14"); // NOI18N
        jCheckBox14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox15.setForeground(resourceMap.getColor("jCheckBox15.foreground")); // NOI18N
        jCheckBox15.setText(resourceMap.getString("jCheckBox15.text")); // NOI18N
        jCheckBox15.setName("jCheckBox15"); // NOI18N
        jCheckBox15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox16.setForeground(resourceMap.getColor("jCheckBox16.foreground")); // NOI18N
        jCheckBox16.setText(resourceMap.getString("jCheckBox16.text")); // NOI18N
        jCheckBox16.setName("jCheckBox16"); // NOI18N
        jCheckBox16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox17.setForeground(resourceMap.getColor("jCheckBox17.foreground")); // NOI18N
        jCheckBox17.setText(resourceMap.getString("jCheckBox17.text")); // NOI18N
        jCheckBox17.setName("jCheckBox17"); // NOI18N
        jCheckBox17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox18.setForeground(resourceMap.getColor("jCheckBox18.foreground")); // NOI18N
        jCheckBox18.setText(resourceMap.getString("jCheckBox18.text")); // NOI18N
        jCheckBox18.setName("jCheckBox18"); // NOI18N
        jCheckBox18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jLabel7.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jCheckBox19.setForeground(resourceMap.getColor("jCheckBox19.foreground")); // NOI18N
        jCheckBox19.setText(resourceMap.getString("jCheckBox19.text")); // NOI18N
        jCheckBox19.setName("jCheckBox19"); // NOI18N
        jCheckBox19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox20.setForeground(resourceMap.getColor("jCheckBox20.foreground")); // NOI18N
        jCheckBox20.setText(resourceMap.getString("jCheckBox20.text")); // NOI18N
        jCheckBox20.setName("jCheckBox20"); // NOI18N
        jCheckBox20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox21.setForeground(resourceMap.getColor("jCheckBox21.foreground")); // NOI18N
        jCheckBox21.setText(resourceMap.getString("jCheckBox21.text")); // NOI18N
        jCheckBox21.setName("jCheckBox21"); // NOI18N
        jCheckBox21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jLabel8.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jCheckBox22.setForeground(resourceMap.getColor("jCheckBox22.foreground")); // NOI18N
        jCheckBox22.setText(resourceMap.getString("jCheckBox22.text")); // NOI18N
        jCheckBox22.setName("jCheckBox22"); // NOI18N
        jCheckBox22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox23.setForeground(resourceMap.getColor("jCheckBox23.foreground")); // NOI18N
        jCheckBox23.setText(resourceMap.getString("jCheckBox23.text")); // NOI18N
        jCheckBox23.setName("jCheckBox23"); // NOI18N
        jCheckBox23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox24.setForeground(resourceMap.getColor("jCheckBox24.foreground")); // NOI18N
        jCheckBox24.setText(resourceMap.getString("jCheckBox24.text")); // NOI18N
        jCheckBox24.setName("jCheckBox24"); // NOI18N
        jCheckBox24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox25.setForeground(resourceMap.getColor("jCheckBox25.foreground")); // NOI18N
        jCheckBox25.setText(resourceMap.getString("jCheckBox25.text")); // NOI18N
        jCheckBox25.setName("jCheckBox25"); // NOI18N
        jCheckBox25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox26.setForeground(resourceMap.getColor("jCheckBox26.foreground")); // NOI18N
        jCheckBox26.setText(resourceMap.getString("jCheckBox26.text")); // NOI18N
        jCheckBox26.setName("jCheckBox26"); // NOI18N
        jCheckBox26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox27.setForeground(resourceMap.getColor("jCheckBox27.foreground")); // NOI18N
        jCheckBox27.setText(resourceMap.getString("jCheckBox27.text")); // NOI18N
        jCheckBox27.setName("jCheckBox27"); // NOI18N
        jCheckBox27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox28.setForeground(resourceMap.getColor("jCheckBox28.foreground")); // NOI18N
        jCheckBox28.setText(resourceMap.getString("jCheckBox28.text")); // NOI18N
        jCheckBox28.setName("jCheckBox28"); // NOI18N
        jCheckBox28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox29.setForeground(resourceMap.getColor("jCheckBox29.foreground")); // NOI18N
        jCheckBox29.setText(resourceMap.getString("jCheckBox29.text")); // NOI18N
        jCheckBox29.setName("jCheckBox29"); // NOI18N
        jCheckBox29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox30.setForeground(resourceMap.getColor("jCheckBox30.foreground")); // NOI18N
        jCheckBox30.setText(resourceMap.getString("jCheckBox30.text")); // NOI18N
        jCheckBox30.setName("jCheckBox30"); // NOI18N
        jCheckBox30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox31.setForeground(resourceMap.getColor("jCheckBox31.foreground")); // NOI18N
        jCheckBox31.setText(resourceMap.getString("jCheckBox31.text")); // NOI18N
        jCheckBox31.setName("jCheckBox31"); // NOI18N
        jCheckBox31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox32.setForeground(resourceMap.getColor("jCheckBox32.foreground")); // NOI18N
        jCheckBox32.setText(resourceMap.getString("jCheckBox32.text")); // NOI18N
        jCheckBox32.setName("jCheckBox32"); // NOI18N
        jCheckBox32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox33.setForeground(resourceMap.getColor("jCheckBox33.foreground")); // NOI18N
        jCheckBox33.setText(resourceMap.getString("jCheckBox33.text")); // NOI18N
        jCheckBox33.setName("jCheckBox33"); // NOI18N
        jCheckBox33.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        jCheckBox34.setForeground(resourceMap.getColor("jCheckBox34.foreground")); // NOI18N
        jCheckBox34.setText(resourceMap.getString("jCheckBox34.text")); // NOI18N
        jCheckBox34.setName("jCheckBox34"); // NOI18N
        jCheckBox34.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jCheckBox4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox1)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox3)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jCheckBox6)
                    .addComponent(jCheckBox7)
                    .addComponent(jCheckBox8)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jCheckBox11)
                    .addComponent(jCheckBox10))
                .addGap(65, 65, 65)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBox20)
                        .addContainerGap())
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jCheckBox13)
                                    .addComponent(jCheckBox12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(jCheckBox16)
                                .addComponent(jCheckBox17)
                                .addComponent(jCheckBox15)
                                .addComponent(jCheckBox14)
                                .addComponent(jCheckBox21)
                                .addComponent(jCheckBox34)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jCheckBox19, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jCheckBox18, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 87, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jCheckBox22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(jCheckBox23)
                                .addComponent(jCheckBox24)
                                .addComponent(jCheckBox25)
                                .addComponent(jCheckBox26)
                                .addComponent(jCheckBox27)
                                .addComponent(jCheckBox28)
                                .addComponent(jCheckBox29)
                                .addComponent(jCheckBox30)
                                .addComponent(jCheckBox31)
                                .addComponent(jCheckBox32))
                            .addGap(80, 80, 80))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jCheckBox33)
                            .addContainerGap()))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jCheckBox12)
                    .addComponent(jCheckBox22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox2)
                    .addComponent(jCheckBox13)
                    .addComponent(jCheckBox23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel6)
                    .addComponent(jCheckBox24))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox3)
                    .addComponent(jCheckBox14)
                    .addComponent(jCheckBox25))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox4)
                    .addComponent(jCheckBox15)
                    .addComponent(jCheckBox26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox5)
                    .addComponent(jCheckBox16)
                    .addComponent(jCheckBox27))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jCheckBox17)
                    .addComponent(jCheckBox28))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox6)
                    .addComponent(jCheckBox18)
                    .addComponent(jCheckBox29))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox7)
                    .addComponent(jCheckBox30)
                    .addComponent(jCheckBox34))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox8)
                    .addComponent(jCheckBox31)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox9)
                    .addComponent(jCheckBox32)
                    .addComponent(jCheckBox19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jCheckBox21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox10)
                    .addComponent(jCheckBox20, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox11)
                    .addComponent(jCheckBox33))
                .addContainerGap(32, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Este método se ejecuta cuando pulsamos uno de los check boxes del panel.
     * Lo que hace es ponerlo activo, cambiar el color de la fuente a verde y
     * poner el fondo de color gris oscuro.
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void checkBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxActionPerformed
        JCheckBox jcb = (JCheckBox)evt.getSource();
        jcb.setSelected(true);
        jcb.setForeground(Color.GREEN);
        jcb.setBackground(new Color(153,153,153));
    }//GEN-LAST:event_checkBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox12;
    private javax.swing.JCheckBox jCheckBox13;
    private javax.swing.JCheckBox jCheckBox14;
    private javax.swing.JCheckBox jCheckBox15;
    private javax.swing.JCheckBox jCheckBox16;
    private javax.swing.JCheckBox jCheckBox17;
    private javax.swing.JCheckBox jCheckBox18;
    private javax.swing.JCheckBox jCheckBox19;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox20;
    private javax.swing.JCheckBox jCheckBox21;
    private javax.swing.JCheckBox jCheckBox22;
    private javax.swing.JCheckBox jCheckBox23;
    private javax.swing.JCheckBox jCheckBox24;
    private javax.swing.JCheckBox jCheckBox25;
    private javax.swing.JCheckBox jCheckBox26;
    private javax.swing.JCheckBox jCheckBox27;
    private javax.swing.JCheckBox jCheckBox28;
    private javax.swing.JCheckBox jCheckBox29;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox30;
    private javax.swing.JCheckBox jCheckBox31;
    private javax.swing.JCheckBox jCheckBox32;
    private javax.swing.JCheckBox jCheckBox33;
    private javax.swing.JCheckBox jCheckBox34;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    // End of variables declaration//GEN-END:variables

}
