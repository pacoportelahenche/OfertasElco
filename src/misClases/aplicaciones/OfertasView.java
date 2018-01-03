package misClases.aplicaciones;

/*
 * Importaciones necesarias para el programa.
 */
import com.pacoportela.utilidades.IO.IOFichero;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import java.util.Collections;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.List;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JCheckBox;
import org.jdesktop.application.Action;
import misClases.aplicaciones.resources.ArticuloOferta;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Esta aplicación nos permite gestionar ofertas.
 *
 * @author Francisco Portela Henche
 */
public class OfertasView extends FrameView implements Printable {

    /**
     * Constructor de la clase.
     *
     * @param app un objeto SingleFrameApplication sobre el que se construye
     * esta interfaz gráfica.
     */
    public OfertasView(SingleFrameApplication app) {
        super(app);

        initComponents();
        initMisComponentes();
        comprobarFicherosDatos();
        cargarDatos();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger
            ("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener
            (new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    /**
     * Método utilizado para inicializar ciertas propiedades de la ventana
     * principal.
     */
    private void initMisComponentes() {
        this.getFrame().addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent ev) {
                textoOferta.requestFocusInWindow();
            }

            @Override
            public void windowClosing(WindowEvent ev) {
                accionSalir();
            }
        });
        this.listadoFechas = new ArrayList<String>();
        this.listadoOfertas = new ArrayList<ArticuloOferta>();
        URL url = this.getClass().getResource("resources/carrito.png");
        if (url != null) {
            ImageIcon icono = new ImageIcon(url);
            this.getFrame().setIconImage(icono.getImage());
        }
        OfertasView.anhoActual
                = String.valueOf(new GregorianCalendar().get(Calendar.YEAR));
        OfertasView.anhoEnUso = OfertasView.anhoActual;
        this.cambiarEtiquetaAnho();
    }

    /**
     * Método que gestiona la carga de los datos desde el disco duro. Llama a
     * los distinto métodos para cargar los distintos datos que se necesitan
     * (el índice del combo de fechas, las fechas de las ofertas existentes y
     * la oferta que coincida con el índice del combo de fechas.
     */
    private void cargarDatos() {
        int indice = cargarIndice(new File("datos/indice.xml"));
        cargarFechas(new File("datos/fechasOferta.xml"), indice);
        cargarOfertaSeleccionada();
    }

    /**
     * Carga el índice del combo de fechas que se estaba usando la última vez
     * que se cerró el programa. Con esto aseguramos que cuando vuelva a 
     * iniciarse la aplicación, cargaremos los datos de la oferta que estabamos
     * visualizando cuando cerramos el programa por última vez.
     * @param fichero el fichero donde se encuentran los datos del índice.
     * @return un entero indicando el índice.
     */
    private int cargarIndice(File fichero) {
        Document doc = cargarDocumentoXML(fichero);
        if(doc == null){
            return 0;
        }
        Element raiz = doc.getRootElement();
        Element indice = raiz.getChild("Indice");
        int ind = Integer.parseInt(indice.getText());
        return ind;
    }

    /**
     * Carga desde el disco las fechas de las ofertas existentes para incluirlas
     * en el comboBox de fechas.
     * @param fichero el fichero donde se encuentran las fechas.
     * @param indice el indice que señalará el combo de fechas.
     */
    private void cargarFechas(File fichero, int indice) {
        this.listadoFechas.clear();
        Document doc = cargarDocumentoXML(fichero);
        /* Si el documento no existe significa que no hay ficheros de
        datos, por lo que probablemente estemos empezando un año nuevo.
        Lo que hacemos es poner el índice a cero y poner una fecha 
        genérica que luego podremos cambiar. Al finalizar grabamos el
        índice y la fecha que acabamos de crear.
        */
        if(doc == null){
            this.listadoFechas.add("01Enero-31Enero");
            this.grabarIndice();
            this.grabarFechas();
            this.fechaOfertaActual = this.listadoFechas.get(indice);
            this.botonGuardar.doClick();
        }
        else{
            Element raiz = doc.getRootElement();
            List<Element> listaFechas = raiz.getChildren();
            for (Element fecha : listaFechas) {
                this.listadoFechas.add(fecha.getText());
            }
        }
        this.actualizarComboFechas();
        this.comboFechas.setSelectedIndex(indice);
        this.fechaOfertaActual = this.listadoFechas.get(indice);
    }

    /**
     * Carga desde el disco los datos de la oferta indicada en el combo de 
     * fechas.
     */
    private void cargarOfertaSeleccionada() {
        this.listadoOfertas.clear();
        String ruta;
        if (OfertasView.anhoActual.equalsIgnoreCase(OfertasView.anhoEnUso)) {
            ruta = "datos/ofertas";
        } else {
            ruta = "datos/" + OfertasView.anhoEnUso + "/ofertas";
        }
        File fichero = new File(ruta
                + (String) this.comboFechas.getSelectedItem()
                + OfertasView.anhoEnUso + ".xml");
        if (fichero.exists()) {
            Document doc = cargarDocumentoXML(fichero);
            Element raiz = doc.getRootElement();
            List<Element> listaArticulos = raiz.getChildren();
            for (Element articulo : listaArticulos) {
                ArticuloOferta art = new ArticuloOferta();
                art.setNombreArticulo(articulo.getChildText("Nombre"));
                art.setEstadoArticulo(Integer.parseInt
                    (articulo.getChildText("Estado")));
                this.listadoOfertas.add(art);
            }
            this.actualizarListaOfertas();
            this.comprobarOfertas();
        }
        else{
            this.borrarListaOfertas();
        }
    }

    /**
     * Este método crea el documento XML a partir del fichero de datos. Utiliza
     * la funcionalidad de la clase <code>SAXBuilder</code> de JDOM.
     *
     * @param fich un objeto <code>File</code> que representa el fichero donde
     * están guardados los datos XML.
     */
    private Document cargarDocumentoXML(File fich) {
        /* Si la longitud del fichero es cero significa que no contiene datos
         * y por lo tanto no podemos crear el documento XML. Salimos del
         * método.
         */
        if (fich.length() == 0) {
            return null;
        }
        Document doc = null;
        try {
            SAXBuilder constructor = new SAXBuilder();
            doc = constructor.build(fich);
        } catch (JDOMException ex) {
            JOptionPane.showMessageDialog(this.getFrame(),
                    ex.toString(),
                    "Error creando el documento XML",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this.getFrame(),
                    ex.toString(),
                    "Error accediendo el fichero " + fich.getAbsolutePath(),
                    JOptionPane.ERROR_MESSAGE);
        }
        return doc;
    }

    /**
     * Este método se usa para imprimir la lista de ofertas.
     *
     * @param g el contexto gráfico dentro del cual se imprime la página.
     * @param pf el tamaño y la orientación de la página a imprimir.
     * @param pageIndex el índice (la primera página es la cero) de la página a
     * imprimir.
     * @return PAGE_EXISTS si la página es reproducida con éxito ó NO_SUCH_PAGE
     * si pageIndex especifica una página que no existe.
     * @throws java.awt.print.PrinterException cuando el trabajo de impresión
     * termina.
     */
    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex)
            throws PrinterException {
        // obtenemos los nombres de las ofertas del vector de ofertas y los
        // pasamos a un array de Strings.
        String[] lineasTexto = new String[listadoOfertas.size()];
        for (int i = 0; i < listadoOfertas.size(); i++) {
            lineasTexto[i] = listadoOfertas.get(i).getNombreArticulo();
        }
        // creamos el tipo de letra que usaremos para imprimir.
        Font font = new Font("Arial", Font.PLAIN, 11);
        // obtenemos la altura de la linea de la fuente especificada.
        FontMetrics metrics = g.getFontMetrics(font);
        int lineHeight = metrics.getHeight();
        // calculamos el número de páginas que vamos a necesitar para imprimir
        // todas las ofertas.
        if (roturasPagina == null) {
            int lineasPorPagina = (int) (pf.getImageableHeight() / lineHeight);
            int numRoturas = (lineasTexto.length - 1) / lineasPorPagina;
            roturasPagina = new int[numRoturas];
            for (int b = 0; b < numRoturas; b++) {
                roturasPagina[b] = (b + 1) * lineasPorPagina;
            }
        }
        // cuando el índice de página a imprimir supere al número de páginas
        // necesitadas saldremos del método.
        if (pageIndex > roturasPagina.length) {
            return NO_SUCH_PAGE;
        }
        /* User (0,0) is typically outside the imageable area, so we must
         * translate by the X and Y values in the PageFormat to avoid clipping
         * Since we are drawing text we
         */
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        /* 
         * Dibuja cada linea de esta página. Incrementa la posición 'y'
         * con lineHeight en cada linea.
         */
        int y = 0;
        int start = (pageIndex == 0) ? 0 : roturasPagina[pageIndex - 1];
        int end = (pageIndex == roturasPagina.length)
                ? lineasTexto.length : roturasPagina[pageIndex];
        for (int line = start; line < end; line++) {
            y += lineHeight;
            g.drawString(lineasTexto[line], 0, y);
        }
        // le dice al método que llama que esta págine es parte del documento.
        return PAGE_EXISTS;
    }

    /**
     * Método que muestra el diálogo Acerca de... Se utiliza como una acción en
     * el programa.
     */
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = Ofertas.getApplication().getMainFrame();
            aboutBox = new OfertasAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        Ofertas.getApplication().show(aboutBox);
    }

    /**
     * Este método es llamado desde dentro del constructor para inicializar el
     * formulario.
     * Aviso: NO modificar este código. El contenido de este método es siempre
     * regenerado por el editor del formulario.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listaOfertas = new javax.swing.JList();
        textoOferta = new javax.swing.JTextField();
        botonAnadir = new javax.swing.JButton();
        botonBorrar = new javax.swing.JButton();
        botonSalir = new javax.swing.JButton();
        botonModificar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        textoTotales = new javax.swing.JTextArea();
        comboFechas = new javax.swing.JComboBox();
        etiquetaPeriodoOfertas = new javax.swing.JLabel();
        botonGuardar = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        panelVisual = new javax.swing.JPanel();
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
        jLabel7 = new javax.swing.JLabel();
        jCheckBox17 = new javax.swing.JCheckBox();
        jCheckBox18 = new javax.swing.JCheckBox();
        jCheckBox19 = new javax.swing.JCheckBox();
        jCheckBox20 = new javax.swing.JCheckBox();
        jCheckBox21 = new javax.swing.JCheckBox();
        jCheckBox22 = new javax.swing.JCheckBox();
        jCheckBox23 = new javax.swing.JCheckBox();
        jCheckBox24 = new javax.swing.JCheckBox();
        jCheckBox25 = new javax.swing.JCheckBox();
        jCheckBox26 = new javax.swing.JCheckBox();
        jCheckBox27 = new javax.swing.JCheckBox();
        etiquetaAnho = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        utilMenu = new javax.swing.JMenu();
        cambiarMenuItem = new javax.swing.JMenuItem();
        cargarMenuItem = new javax.swing.JMenuItem();
        imprimirMenuItem = new javax.swing.JMenuItem();
        borrarMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jRadioButton1 = new javax.swing.JRadioButton();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(400, 300));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(misClases.aplicaciones.Ofertas.class).getContext().getResourceMap(OfertasView.class);
        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jScrollPane1.border.title"))); // NOI18N
        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        listaOfertas.setBackground(resourceMap.getColor("listaOfertas.background")); // NOI18N
        listaOfertas.setFont(resourceMap.getFont("listaOfertas.font")); // NOI18N
        listaOfertas.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "oferta1", "oferta2", "oferta3" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listaOfertas.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        listaOfertas.setToolTipText(resourceMap.getString("listaOfertas.toolTipText")); // NOI18N
        listaOfertas.setName("listaOfertas"); // NOI18N
        listaOfertas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                listaOfertasMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(listaOfertas);

        textoOferta.setText(resourceMap.getString("textoOferta.text")); // NOI18N
        textoOferta.setToolTipText(resourceMap.getString("textoOferta.toolTipText")); // NOI18N
        textoOferta.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        textoOferta.setName("textoOferta"); // NOI18N
        textoOferta.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                caretUpdateListener(evt);
            }
        });
        textoOferta.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                textoOfertaFocusGained(evt);
            }
        });
        textoOferta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textoOfertaKeyPressed(evt);
            }
        });

        botonAnadir.setFont(resourceMap.getFont("botonAnadir.font")); // NOI18N
        botonAnadir.setForeground(resourceMap.getColor("botonAnadir.foreground")); // NOI18N
        botonAnadir.setIcon(resourceMap.getIcon("botonAnadir.icon")); // NOI18N
        botonAnadir.setText(resourceMap.getString("botonAnadir.text")); // NOI18N
        botonAnadir.setToolTipText(resourceMap.getString("botonAnadir.toolTipText")); // NOI18N
        botonAnadir.setActionCommand(resourceMap.getString("botonAnadir.actionCommand")); // NOI18N
        botonAnadir.setName("botonAnadir"); // NOI18N
        botonAnadir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAnadirActionPerformed(evt);
            }
        });

        botonBorrar.setFont(resourceMap.getFont("botonBorrar.font")); // NOI18N
        botonBorrar.setForeground(resourceMap.getColor("botonBorrar.foreground")); // NOI18N
        botonBorrar.setIcon(resourceMap.getIcon("botonBorrar.icon")); // NOI18N
        botonBorrar.setText(resourceMap.getString("botonBorrar.text")); // NOI18N
        botonBorrar.setToolTipText(resourceMap.getString("botonBorrar.toolTipText")); // NOI18N
        botonBorrar.setDefaultCapable(false);
        botonBorrar.setName("botonBorrar"); // NOI18N
        botonBorrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonBorrarActionPerformed(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(misClases.aplicaciones.Ofertas.class).getContext().getActionMap(OfertasView.class, this);
        botonSalir.setAction(actionMap.get("accionSalir")); // NOI18N
        botonSalir.setFont(resourceMap.getFont("botonSalir.font")); // NOI18N
        botonSalir.setForeground(resourceMap.getColor("botonSalir.foreground")); // NOI18N
        botonSalir.setIcon(resourceMap.getIcon("botonSalir.icon")); // NOI18N
        botonSalir.setText(resourceMap.getString("botonSalir.text")); // NOI18N
        botonSalir.setToolTipText(resourceMap.getString("botonSalir.toolTipText")); // NOI18N
        botonSalir.setName("botonSalir"); // NOI18N

        botonModificar.setFont(resourceMap.getFont("botonModificar.font")); // NOI18N
        botonModificar.setForeground(resourceMap.getColor("botonModificar.foreground")); // NOI18N
        botonModificar.setIcon(resourceMap.getIcon("botonModificar.icon")); // NOI18N
        botonModificar.setText(resourceMap.getString("botonModificar.text")); // NOI18N
        botonModificar.setToolTipText(resourceMap.getString("botonModificar.toolTipText")); // NOI18N
        botonModificar.setName("botonModificar"); // NOI18N
        botonModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonModificarActionPerformed(evt);
            }
        });

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jScrollPane2.border.title"))); // NOI18N
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        textoTotales.setColumns(20);
        textoTotales.setEditable(false);
        textoTotales.setRows(5);
        textoTotales.setToolTipText(resourceMap.getString("textoTotales.toolTipText")); // NOI18N
        textoTotales.setBorder(null);
        textoTotales.setName("textoTotales"); // NOI18N
        jScrollPane2.setViewportView(textoTotales);

        comboFechas.setEditable(true);
        comboFechas.setFont(resourceMap.getFont("comboFechas.font")); // NOI18N
        comboFechas.setToolTipText(resourceMap.getString("comboFechas.toolTipText")); // NOI18N
        comboFechas.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        comboFechas.setName("comboFechas"); // NOI18N
        comboFechas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboFechasActionPerformed(evt);
            }
        });

        etiquetaPeriodoOfertas.setFont(resourceMap.getFont("etiquetaPeriodoOfertas.font")); // NOI18N
        etiquetaPeriodoOfertas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        etiquetaPeriodoOfertas.setText(resourceMap.getString("etiquetaPeriodoOfertas.text")); // NOI18N
        etiquetaPeriodoOfertas.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        etiquetaPeriodoOfertas.setFocusable(false);
        etiquetaPeriodoOfertas.setName("etiquetaPeriodoOfertas"); // NOI18N

        botonGuardar.setFont(resourceMap.getFont("botonGuardar.font")); // NOI18N
        botonGuardar.setForeground(resourceMap.getColor("botonGuardar.foreground")); // NOI18N
        botonGuardar.setIcon(resourceMap.getIcon("botonGuardar.icon")); // NOI18N
        botonGuardar.setText(resourceMap.getString("botonGuardar.text")); // NOI18N
        botonGuardar.setToolTipText(resourceMap.getString("botonGuardar.toolTipText")); // NOI18N
        botonGuardar.setName("botonGuardar"); // NOI18N
        botonGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonGuardarActionPerformed(evt);
            }
        });

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jScrollPane3.border.title"))); // NOI18N
        jScrollPane3.setName("jScrollPane3"); // NOI18N

        panelVisual.setName("panelVisual"); // NOI18N

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

        jLabel7.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

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

        javax.swing.GroupLayout panelVisualLayout = new javax.swing.GroupLayout(panelVisual);
        panelVisual.setLayout(panelVisualLayout);
        panelVisualLayout.setHorizontalGroup(
            panelVisualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVisualLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelVisualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox10)
                    .addComponent(jCheckBox3)
                    .addComponent(jCheckBox4)
                    .addComponent(jCheckBox5)
                    .addComponent(jCheckBox6)
                    .addComponent(jCheckBox7)
                    .addComponent(jCheckBox8)
                    .addComponent(jCheckBox11)
                    .addComponent(jCheckBox12)
                    .addComponent(jCheckBox14)
                    .addComponent(jCheckBox15)
                    .addComponent(jCheckBox16)
                    .addComponent(jCheckBox17)
                    .addComponent(jCheckBox18)
                    .addComponent(jCheckBox20)
                    .addComponent(jCheckBox21)
                    .addComponent(jCheckBox22)
                    .addComponent(jCheckBox23)
                    .addComponent(jCheckBox24)
                    .addComponent(jCheckBox25)
                    .addComponent(jCheckBox26)
                    .addComponent(jCheckBox27)
                    .addComponent(jCheckBox9)
                    .addComponent(jCheckBox19)
                    .addComponent(jCheckBox1)
                    .addGroup(panelVisualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelVisualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        panelVisualLayout.setVerticalGroup(
            panelVisualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVisualLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox27)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jScrollPane3.setViewportView(panelVisual);

        etiquetaAnho.setFont(resourceMap.getFont("etiquetaAnho.font")); // NOI18N
        etiquetaAnho.setText(resourceMap.getString("etiquetaAnho.text")); // NOI18N
        etiquetaAnho.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        etiquetaAnho.setName("etiquetaAnho"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(comboFechas, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17)
                        .addComponent(etiquetaPeriodoOfertas)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(etiquetaAnho))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(botonAnadir)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botonBorrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botonModificar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botonGuardar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(botonSalir))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 567, Short.MAX_VALUE)
                    .addComponent(textoOferta, javax.swing.GroupLayout.DEFAULT_SIZE, 567, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(etiquetaPeriodoOfertas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(etiquetaAnho))
                            .addComponent(comboFechas))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textoOferta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(botonAnadir)
                            .addComponent(botonBorrar)
                            .addComponent(botonModificar)
                            .addComponent(botonGuardar)
                            .addComponent(botonSalir)))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE))
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        exitMenuItem.setAction(actionMap.get("accionSalir")); // NOI18N
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
        exitMenuItem.setToolTipText(resourceMap.getString("exitMenuItem.toolTipText")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        utilMenu.setText(resourceMap.getString("utilMenu.text")); // NOI18N
        utilMenu.setName("utilMenu"); // NOI18N

        cambiarMenuItem.setAction(actionMap.get("accionCambiarIntervalo")); // NOI18N
        cambiarMenuItem.setText(resourceMap.getString("cambiarMenuItem.text")); // NOI18N
        cambiarMenuItem.setToolTipText(resourceMap.getString("cambiarMenuItem.toolTipText")); // NOI18N
        cambiarMenuItem.setName("cambiarMenuItem"); // NOI18N
        utilMenu.add(cambiarMenuItem);

        cargarMenuItem.setAction(actionMap.get("accionCargarAnho")); // NOI18N
        cargarMenuItem.setText(resourceMap.getString("cargarMenuItem.text")); // NOI18N
        cargarMenuItem.setToolTipText(resourceMap.getString("cargarMenuItem.toolTipText")); // NOI18N
        cargarMenuItem.setName("cargarMenuItem"); // NOI18N
        utilMenu.add(cargarMenuItem);

        imprimirMenuItem.setText(resourceMap.getString("imprimirMenuItem.text")); // NOI18N
        imprimirMenuItem.setToolTipText(resourceMap.getString("imprimirMenuItem.toolTipText")); // NOI18N
        imprimirMenuItem.setName("imprimirMenuItem"); // NOI18N
        imprimirMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imprimirMenuItemActionPerformed(evt);
            }
        });
        utilMenu.add(imprimirMenuItem);

        borrarMenuItem.setText(resourceMap.getString("borrarMenuItem.text")); // NOI18N
        borrarMenuItem.setToolTipText(resourceMap.getString("borrarMenuItem.toolTipText")); // NOI18N
        borrarMenuItem.setName("borrarMenuItem"); // NOI18N
        borrarMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borrarMenuItemActionPerformed(evt);
            }
        });
        utilMenu.add(borrarMenuItem);

        menuBar.add(utilMenu);

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verProductosMenuItem(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        menuBar.add(jMenu1);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text")); // NOI18N
        aboutMenuItem.setToolTipText(resourceMap.getString("aboutMenuItem.toolTipText")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setFont(resourceMap.getFont("statusMessageLabel.font")); // NOI18N
        statusMessageLabel.setText(resourceMap.getString("statusMessageLabel.text")); // NOI18N
        statusMessageLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        jRadioButton1.setAction(actionMap.get("accionCambiarFlagGrabar")); // NOI18N
        jRadioButton1.setFont(resourceMap.getFont("radioBotonGrabar.font")); // NOI18N
        jRadioButton1.setForeground(resourceMap.getColor("radioBotonGrabar.foreground")); // NOI18N
        jRadioButton1.setText(resourceMap.getString("radioBotonGrabar.text")); // NOI18N
        jRadioButton1.setToolTipText(resourceMap.getString("radioBotonGrabar.toolTipText")); // NOI18N
        jRadioButton1.setName("radioBotonGrabar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 922, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 511, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButton1))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Método ejecutado cuando se pulsa el boton Añadir.
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void botonAnadirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAnadirActionPerformed
        anadirOferta(this.indiceModificar);
        this.textoOferta.requestFocus();
        this.textoOferta.selectAll();
    }//GEN-LAST:event_botonAnadirActionPerformed

    /**
     * Método ejecutado cuando cambia el punto de inserción (se añade ó se borra
     * un carácter) en el campo de texto 'textoOferta'. Desplaza la lista de
     * ofertas para que se vea la oferta alfabéticamente más cercana a la que
     * estamos escribiendo.
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void caretUpdateListener(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_caretUpdateListener
        String texto = this.textoOferta.getText();
        for (int i = 0; i < this.listadoOfertas.size(); i++) {
            String textoActual
                    = this.listadoOfertas.get(i).getNombreArticulo();
            if (texto.compareToIgnoreCase(textoActual) <= 0) {
                this.listaOfertas.ensureIndexIsVisible(i);
                break;
            }
        }
    }//GEN-LAST:event_caretUpdateListener

    /**
     * Método ejecutado cuando se pulsa el boton Borrar. Pide confirmación al
     * usuario y si la respuesta es sí elimina la linea seleccionada de la lista
     * de ofertas.
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void botonBorrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonBorrarActionPerformed
        int indice = this.listaOfertas.getSelectedIndex();
        if (indice != -1) {
            int op = JOptionPane.showConfirmDialog(this.getFrame(),
                    "Desea borrar la linea de oferta seleccionada?",
                    "Borrar linea de la oferta",
                    JOptionPane.YES_NO_OPTION);

            if (op == JOptionPane.YES_OPTION) {
                this.listadoOfertas.remove(indice);
                this.actualizarListaOfertas();
                this.actualizarEtiquetaMensajes();
                this.actualizarTotales();
                this.comprobarOfertas();
            }
        } else {
            JOptionPane.showMessageDialog(this.getFrame(),
                    "Debe seleccionar una linea de la lista de ofertas",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_botonBorrarActionPerformed

    /**
     * Método ejecutado cuando se pulsa el botón Modificar. Obtiene el texto del
     * campo de texto 'textoOferta' y activa el flag 'modificando' poniéndolo a
     * true.
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void botonModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonModificarActionPerformed
        if (modificando) {
            return;
        }
        this.indiceModificar = listaOfertas.getSelectedIndex();
        if (listaOfertas.getSelectedIndex() != -1) {
            String texto
                    = this.listadoOfertas.get(indiceModificar).getNombreArticulo();
            this.textoOferta.setText(texto);
            this.textoOferta.selectAll();
            this.textoOferta.requestFocus();
            this.modificando = true;
        } else {
            JOptionPane.showMessageDialog(this.getFrame(),
                    "Debe seleccionar una linea de la lista de ofertas",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_botonModificarActionPerformed

    /**
     * Con este método controlamos lo que ocurre en el comboFechas. Pueden
     * ocurrir dos cosas: que incluyamos un nuevo item en el combo ó que
     * seleccionemos uno eligiendo entre los ya existentes. Si añadimos un nuevo
     * item, lo almacenamos en el vectorFechas y borramos los datos que pudieran
     * existir en el vectorOfertas (estamos creando un nuevo período de oferta).
     * En el caso de que seleccionemos un item ya existente, cargamos los datos
     * de la oferta que corresponda con esas fechas y las presentamos en la
     * lista de ofertas.
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void comboFechasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboFechasActionPerformed
        if (this.inicioSesion) {
            this.inicioSesion = false;
            return;
        }
        Object obj = this.comboFechas.getSelectedItem();
        if (obj == null) {
            return;
        }
        String textoObj = obj.toString();
        String accion = evt.getActionCommand();

        // las acciones que devuelve el método getActionCommand son:
        // comboBoxChanged cuando cambiamos ó editamos el combo box
        // comboBoxEdited cuando editamos el combo box (incluimos un nuevo item)
        if (accion.equalsIgnoreCase("comboBoxEdited")) {
            if (textoObj.length() == 0) {
                return;
            }
            // comprobamos si el periodo de ofertas ya existe en el vector
            // fechas y si es así salimos del método.
            for (int i = 0; i < this.listadoFechas.size(); i++) {
                if (textoObj.equalsIgnoreCase(this.listadoFechas.get(i))) {
                    return;
                }
            }
            this.listadoFechas.add(textoObj);
            this.actualizarComboFechas();
            this.fechaOfertaActual = textoObj;
            this.borrarListaOfertas();
            this.comboFechas.setSelectedItem(obj);
            this.actualizarTotales();
        } else { // comboBoxChanged
            this.fechaOfertaActual
                    = this.comboFechas.getSelectedItem().toString();
            this.cargarOfertaSeleccionada();
        }
    }//GEN-LAST:event_comboFechasActionPerformed

    /**
     * Este método comprueba que haya alguna fecha en el combo box de fechas.
     *
     * @return un boolean que es false en el caso de que no haya ninguna fechas
     * en el combo box.
     */
    private boolean comprobarDatosComboFechas() {
        boolean b = true;
        String s = this.comboFechas.getSelectedItem().toString();
        if (s.length() == 0) {
            b = false;
        }
        return b;
    }

    /**
     * Método ejecutado cuando pulsamos una tecla sobre el campo de texto
     * 'textoOferta'. Si la tecla pulsada es ENTER hacemos una pulsación por
     * software del botón Añadir.
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void textoOfertaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textoOfertaKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            this.botonAnadir.doClick();
        }
    }//GEN-LAST:event_textoOfertaKeyPressed

    /**
     * Método ejecutado cuando pulsamos el botón Guardar. Le pide confirmación
     * al usuario y si es afirmativa graba las ofertas en el archivo del disco.
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void botonGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonGuardarActionPerformed
        if (!OfertasView.anhoActual.equalsIgnoreCase(OfertasView.anhoEnUso)) {
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this.getFrame(),
                "¿Desea guardar las ofertas de la lista?",
                "Guardar ofertas", JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            this.grabarOferta();
            JOptionPane.showMessageDialog(this.getFrame(),
                    "Datos grabados correctamente", "DATOS GRABADOS",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_botonGuardarActionPerformed

    /**
     * Método ejecutado al pulsar con el botón derecho del ratón sobre el
     * elemento seleccionado de la lista de ofertas. Lo que hace es cambiar el
     * estado del articúlo de la oferta. Al cambiar el estado cambia la forma en
     * como aparece representado (subrayado ó sin subrayar). Este comportamiento
     * está descrito en el objeto <code>ArticuloOferta</code>
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void listaOfertasMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listaOfertasMousePressed
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
            ArticuloOferta articulo
                    = (ArticuloOferta) this.listaOfertas.getSelectedValue();
            if (articulo != null) {
                articulo.cambiarEstadoArticuloOferta();
                this.listaOfertas.repaint();
            }
        }
    }//GEN-LAST:event_listaOfertasMousePressed

    /**
     * Este método se ejecuta cuando el campo texto oferta gana el foco. Lo que
     * hace es seleccionar todo el texto.
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void textoOfertaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textoOfertaFocusGained
        this.textoOferta.selectAll();
    }//GEN-LAST:event_textoOfertaFocusGained

    /**
     * Método ejecutado cuando pulsamos el menú item Imprimir ofertas del menú
     * Utilidades. Imprime la lista ofertas.
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void imprimirMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imprimirMenuItemActionPerformed
        if (listadoOfertas.isEmpty()) {
            return;
        }
        PrintRequestAttributeSet attributes = 
                new HashPrintRequestAttributeSet();
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPrintable(this);
        pj.printDialog(attributes);
        if (pj.printDialog()) {
            try {
                pj.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this.getFrame(),
                        ex.toString(),
                        "Error al imprimir",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_imprimirMenuItemActionPerformed

    /**
     * Método que se ejecuta al pulsar en el menú item borrar. Le pide
     * confirmación al usuario y si la respuesta es afirmativa borra todas las
     * ofertas de la lista.
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void borrarMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borrarMenuItemActionPerformed
        int opcion = JOptionPane.showConfirmDialog(this.getFrame(),
                "¿Desea borrar toda la lista de ofertas?",
                "Borrar lista de ofertas", JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            this.listadoOfertas.clear();
            this.actualizarListaOfertas();
            this.actualizarEtiquetaMensajes();
            this.actualizarTotales();
            this.comprobarOfertas();
        }
    }//GEN-LAST:event_borrarMenuItemActionPerformed

    /**
     * Método que se ejecuta cuando pulsamos el menú item Ver. Muestra una
     * ventana nueva con los productos de la oferta que ya hemos apuntado en
     * color verde y los que faltan por apuntar en color rojo.
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido
     */
    private void verProductosMenuItem(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verProductosMenuItem
        JFrame f = new JFrame();
        URL url = this.getClass().getResource("resources/carrito.png");
        if (url != null) {
            ImageIcon icono = new ImageIcon(url);
            f.setIconImage(icono.getImage());
        }
        f.setTitle("  PRODUCTOS YA APUNTADOS PARA LA OFERTA *EN VERDE*");
        f.add(new PanelOfertasVisual(this.listadoOfertas));
        f.pack();
        f.setResizable(false);
        f.setVisible(true);
    }//GEN-LAST:event_verProductosMenuItem
    /**
     * Este método se ejecuta cuando pulsamos uno de los check boxes del panel.
     * Lo que hace es ponerlo activo, cambiar el color de la fuente a verde y
     * poner el fondo de color gris oscuro.
     *
     * @param evt el objeto que contiene la información sobre el evento que se
     * ha producido.
     */
    private void checkBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxActionPerformed
        JCheckBox j = (JCheckBox) evt.getSource();
        if (j.isSelected()) {
            j.setForeground(Color.GREEN);
            j.setBackground(new Color(153, 153, 153));
        } else {
            j.setForeground(Color.RED);
            j.setBackground(new Color(236, 233, 216));
        }
    }//GEN-LAST:event_checkBoxActionPerformed

    /**
     * Método que borra los datos del vector que contiene las ofertas y
     * actualiza la vista de la 'listaOfertas'.
     */
    private void borrarListaOfertas() {
        this.listadoOfertas.clear();
        this.actualizarListaOfertas();
    }

    /**
     * Este método se ejecuta cuando añadimos una nueva oferta a la lista de
     * ofertas. Controla si la linea es nueva ó es una modificación de una
     * existente.
     *
     * @param indice el índice de la linea de la oferta que estamos modificando
     * ó -1 si es una linea nueva.
     */
    private void anadirOferta(int indice) {
        if (this.comprobarDatosComboFechas() == false) {
            JOptionPane.showMessageDialog(this.getFrame(),
                    "NO HAY NINGUNA FECHA DE OFERTA",
                    "Mensaje",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String texto = this.textoOferta.getText();
        // Si el texto está vacio mostramos un mensaje al usuario.
        if (texto == null || texto.isEmpty()) {
            String mensaje = "LA OFERTA NO CONTIENE TEXTO";
            JOptionPane.showMessageDialog(this.getFrame(), mensaje,
                    "Mensaje",
                    JOptionPane.INFORMATION_MESSAGE);
        } /*
         * Si el texto contiene algo comprobamos si se trata de una
         * modificación ó de una nueva linea, lo añadimos al vector de
         * ofertas, lo ordenamos y se lo pasamos a la lista de ofertas
         * para que lo muestre al usuario.
         */ else {
            if (!modificando) {
                ArticuloOferta articulo
                        = new ArticuloOferta(texto.toUpperCase(),
                                ArticuloOferta.ESTADO_SIN_MARCAR);
                this.listadoOfertas.add(articulo);
            } else {
                if (this.indiceModificar != -1) {
                    ArticuloOferta articulo = this.listadoOfertas.get(indice);
                    articulo.setNombreArticulo(texto.toUpperCase());
                    this.modificando = false;
                    this.indiceModificar = -1;
                }
            }
            Collections.sort(this.listadoOfertas);
            this.actualizarListaOfertas();
            this.comprobarOfertas();
            this.actualizarEtiquetaMensajes();
            this.actualizarTotales();
        }
    }

    /**
     * Método que actualiza la vista del 'comboFechas'.
     */
    private void actualizarComboFechas() {
        DefaultComboBoxModel modelo
          = new DefaultComboBoxModel(this.listadoFechas.toArray(new String[0]));
        this.comboFechas.setModel(modelo);
    }

    /**
     * Método que actualiza los datos de la lista de ofertas. Lo hace añadiendo
     * el vectorOfertas a su modelo.
     */
    private void actualizarListaOfertas() {
        this.listaOfertas.setListData
            (this.listadoOfertas.toArray(new ArticuloOferta[0]));
        this.actualizarEtiquetaMensajes();
        this.actualizarTotales();
    }

    /**
     * Método que actualiza la etiqueta de mensajes. Muestra el número de
     * ofertas apuntadas en un instante dado.
     */
    private void actualizarEtiquetaMensajes() {
        this.statusMessageLabel.setText("Número de ofertas apuntadas: "
                + this.listadoOfertas.size());
    }

    /**
     * Comprueba la existencia de los ficheros que contienen los datos de la
     * aplicación (datos de los ofertas y de los períodos de las ofertas).
     */
    private void comprobarFicherosDatos() {
        File ficheroIndice = new File("datos/indice.xml");
        comprobarFichero(ficheroIndice);
        File ficheroFechas = new File("datos/fechasOferta.xml");
        comprobarFichero(ficheroFechas);
    }

    /**
     * Comprueba la existencia del fichero pasado como argumento. En el caso de
     * que no exista lo crea, incluyendo toda la estructura de carpetas
     * necesaria para llegar hasta él.
     *
     * @param fichero el fichero del cual se va a comprobar su existencia.
     */
    private synchronized void comprobarFichero(File fichero) {
        if (!fichero.exists()) {
            try {
                File ruta = new File("datos");
                if (!ruta.exists()) {
                    ruta.mkdirs();
                }
                fichero.createNewFile();
                JOptionPane.showMessageDialog(this.getFrame(),
                        "Fichero " + fichero.getPath() + " creado con éxito",
                        "Fichero creado", JOptionPane.INFORMATION_MESSAGE);
            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(this.getFrame(),
                        ex.toString(),
                        "Error creando el fichero " + fichero.getPath(),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Este método carga desde el disco las ofertas de años anteriores (en el
     * caso de que existan dichos datos).
     *
     * @return true si la carga de datos se ha realizado con éxito, false en
     * caso contrario.
     */
    private boolean cargarDatosOtroAnho() {
        //FicheroIO ficheroIO = new FicheroIO();
        boolean resultado = false;
        String ruta;
        if (OfertasView.anhoActual.equalsIgnoreCase(OfertasView.anhoEnUso)) {
            ruta = "datos/";
        } else {
            ruta = "datos/" + OfertasView.anhoEnUso + "/";
        }
        // cargamos los datos del fichero de fechas para insertarlos en el
        // comboBox de fechas.
        File ficheroFechas = new File(ruta + "fechasOferta.xml");
        
        if (ficheroFechas.exists()) {
            cargarFechas(ficheroFechas, 0);
            this.fechaOfertaActual = this.listadoFechas.get(0);
            this.cambiarEtiquetaAnho();
            resultado = true;
        } else {
            JOptionPane.showMessageDialog(this.getFrame(),
                    "Fichero fechasOferta.xml no encontrado",
                    "Error en la carga del fichero",
                    JOptionPane.ERROR_MESSAGE);
        }
        return resultado;
    }

    /**
     * Método que actualiza el area de texto 'textoTotales'. Obtiene la primera
     * palabra de todos los datos almacenados en el 'vectorOfertas' y la va
     * comparando para obtener el total de cada palabra distinta encontrada.
     */
    private void actualizarTotales() {
        if (this.comprobarDatosComboFechas() == false) {
            return;
        }
        Object obj = this.comboFechas.getSelectedItem();
        String fecha;
        String[] tokens;
        if (obj != null) {
            String texto = "";
            fecha = obj.toString();
            tokens = fecha.split("-");
            for (String token : tokens) {
                texto += "   " + token.substring(0, 2) + 
                        " de " + token.substring(2) + "\n";
            }
            this.textoTotales.setText(texto);
            ArrayList<String> listadoVinos = new ArrayList<String>();
            String palabraActual;
            String palabraAnterior = "";
            String[] palabrasOferta;
            int contador = 0;
            for (int i = 0; i < this.listadoOfertas.size(); i++) {
                palabrasOferta
                        = this.listadoOfertas.get(i)
                                .getNombreArticulo().split(" ");
                palabraActual = palabrasOferta[0];
                if (palabraActual.equalsIgnoreCase("vino")) {
                    listadoVinos.add(this.listadoOfertas.get(i)
                            .getNombreArticulo());
                }
                if (i == 0) {
                    palabraAnterior = palabraActual;
                }
                if (palabraActual.equalsIgnoreCase(palabraAnterior)) {
                    contador++;
                } else {
                    this.textoTotales.setText
                        (this.textoTotales.getText() + "\n" + palabraAnterior
                            + ": " + contador);
                    if (palabraAnterior.equalsIgnoreCase("vino")) {
                        this.textoTotales.setText(this.textoTotales.getText()
                                + this.getTiposVino(listadoVinos));
                    }
                    palabraAnterior = palabraActual;
                    contador = 1;
                }
                if (i == this.listadoOfertas.size() - 1) {
                    this.textoTotales.setText(this.textoTotales.getText()
                            + "\n" + palabraAnterior
                            + ": " + contador);
                    if (palabraAnterior.equalsIgnoreCase("vino")) {
                        this.textoTotales.setText(this.textoTotales.getText()
                                + this.getTiposVino(listadoVinos));
                    }
                }
            }
        }
    }

    /**
     * Método que hace un conteo de los tipos de vino que hemos introducido en
     * la lista de ofertas para presentar la cantidad de cada tipo en el campo
     * 'textoTotales'.
     *
     * @param v el vector que contiene los nombres de los vinos que hemos
     * introducido en la oferta.
     * @return un string con los nombres de los vinos y su cantidad.
     */
    private String getTiposVino(ArrayList<String> v) {
        if (v == null || v.isEmpty()) {
            return null;
        }
        String respuesta = "";
        int[] contVinos = new int[this.DOVinos.length];
        for (int i = 0; i < v.size(); i++) {
            String vino = v.get(i);
            if (vino.contains("ALBARIÑO")) {
                contVinos[this.ALBARIÑO]++;
            } else if (vino.contains("GODELLO")) {
                contVinos[this.GODELLO]++;
            } else if (vino.contains("MENCIA")) {
                contVinos[this.MENCIA]++;
            } else if (vino.contains("PENEDES")) {
                contVinos[this.PENEDES]++;
            } else if (vino.contains("RIBEIRO")) {
                contVinos[this.RIBEIRO]++;
            } else if (vino.contains("DUERO")) {
                contVinos[this.DUERO]++;
            } else if (vino.contains("RIOJA")) {
                contVinos[this.RIOJA]++;
            } else if (vino.contains("SOMONTANO")) {
                contVinos[this.SOMONTANO]++;
            } else if (vino.contains("TORO")) {
                contVinos[this.TORO]++;
            } else if (vino.contains("VALDEPEÑAS")) {
                contVinos[this.VALDEPEÑAS]++;
            } else {
                contVinos[this.OTROS]++;
            }
        }
        for (int i = 0; i < this.DOVinos.length; i++) {
            respuesta += "\n  " + this.DOVinos[i] + ": "
                    + Integer.toString(contVinos[i]);
        }
        return respuesta;
    }

    /**
     * Método que se ejecuta cuando se sale de la aplicación. Graba todos los
     * datos y sale del sistema.
     */
    @Action
    public void accionSalir() {
        grabarDatos();
        System.exit(0);
    }

    /**
     * Método utilizado para grabar los datos en el disco duro. Llama a los 
     * distintos métodos que iran grabando, el indice seleccionado en el combo
     * de fechas, las fechas de las ofertas y los datos de la oferta actual.
     */
    private void grabarDatos() {
        // si el año en uso no coincide con el año actual ó el flag
        // 'hayQueGrabar' es false salimos del método sin grabar nada.
        if (OfertasView.anhoActual.equalsIgnoreCase(OfertasView.anhoEnUso)
                && this.hayQueGrabar) {
            grabarIndice();
            grabarFechas();
            grabarOferta();
            grabarNombreFicheroOfertaActual();
            JOptionPane.showMessageDialog(this.getFrame(),
                "Datos grabados correctamente", "DATOS GRABADOS",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Graba el índice seleccionado en el combo de fechas.
     */
    private void grabarIndice() {
        File ficheroIndice = new File("datos/indice.xml");
        // creamos el formato en el que será creada la salida.
        Format formato = Format.getPrettyFormat();
        // le asignamos el encoding para guardar datos en español.
        formato.setEncoding("UTF-16");
        // creamos el objeto para serializar el documento.
        XMLOutputter xmlOutputter = new XMLOutputter(formato);
        // obtenemos el documento XML de los objetos Aviso.
        Document doc = new Document();
        Element raiz = new Element("Indices");
        doc.addContent(raiz);
        Element indice = new Element("Indice");
        int index = this.comboFechas.getSelectedIndex();
        if(index == -1) index = 0;
        indice.setText(Integer.toString(index));
        raiz.addContent(indice);
        String docString;
        docString = xmlOutputter.outputString(doc);
        /* Utilizamos la clase )IOFichero para guardar los datos del String en
         * el disco.
         */
        IOFichero.stringAFichero
            (docString, ficheroIndice, Charset.forName("UTF-16"));
    }

    /**
     * Método utilizado para grabar las fechas de las ofertas en el disco duro.
     */
    private void grabarFechas() {
        File ficheroFechas = new File("datos/fechasOferta.xml");
        // creamos el formato en el que será creada la salida.
        Format formato = Format.getPrettyFormat();
        // le asignamos el encoding para guardar datos en español.
        formato.setEncoding("UTF-16");
        // creamos el objeto para serializar el documento.
        XMLOutputter xmlOutputter = new XMLOutputter(formato);
        // obtenemos el documento XML de los objetos Aviso.
        Document doc = new Document();
        Element raiz = new Element("Fechas");
        doc.addContent(raiz);
        for (int i = 0; i < this.listadoFechas.size(); i++) {
            Element periodo = new Element("Periodo");
            periodo.setText(this.listadoFechas.get(i));
            raiz.addContent(periodo);
        }
        String docString;
        docString = xmlOutputter.outputString(doc);
        /* Utilizamos la clase FicheroIO para guardar los datos del String en
         * el disco.
         */
        IOFichero.stringAFichero
            (docString, ficheroFechas, Charset.forName("UTF-16"));
    }

    /**
     * Método utilizado para grabar la oferta actual en el disco duro. Utiliza
     * la funcionalidad de las clase del API JDom.
     */
    private void grabarOferta() {
        // creamos el formato en el que será creada la salida.
        Format formato = Format.getPrettyFormat();
        // le asignamos el encoding para guardar datos en español.
        formato.setEncoding("UTF-16");
        // creamos el objeto para serializar el documento.
        XMLOutputter xmlOutputter = new XMLOutputter(formato);
        // obtenemos el documento XML de los objetos Aviso.
        Document doc = new Document();
        Element raiz = new Element("Ofertas");
        doc.addContent(raiz);
        for (int i = 0; i < this.listadoOfertas.size(); i++) {
            Element articulo = new Element("Articulo");
            Element nombre = new Element("Nombre");
            Element estado = new Element("Estado");
            nombre.setText(this.listadoOfertas.get(i).getNombreArticulo());
            estado.setText(Integer.toString
                (this.listadoOfertas.get(i).getEstadoArticulo()));
            articulo.addContent(nombre);
            articulo.addContent(estado);
            raiz.addContent(articulo);
        }
        String docString;
        docString = xmlOutputter.outputString(doc);

        File ficheroOferta = new File("datos/ofertas"
                + this.fechaOfertaActual + OfertasView.anhoActual + ".xml");
        IOFichero.stringAFichero
            (docString, ficheroOferta, Charset.forName("UTF-16"));
        
        //grabamos una copia de la oferta en otro fichero por posibles perdidas
        grabarCopiaOferta(docString);
    }
    
    private void grabarCopiaOferta(String docString){
        File ficheroCopia = new File("C:\\Users\\usuario\\Documents\\"
                + "copiaOfertaActual.xml");
        IOFichero.stringAFichero
            (docString, ficheroCopia, Charset.forName("UTF-16"));   
    }

    /**
     * Método que se ejecuta cuando el usuario elije en el menú de utilidades la
     * opcion 'Cambiar intervalo oferta'. Lo que hace es cambiar lo escrito en
     * el combo de fechas y renombrar el archivo de datos de la oferta
     * seleccionada.
     */
    @Action
    public void accionCambiarIntervalo() {
        if (!OfertasView.anhoActual.equalsIgnoreCase(OfertasView.anhoEnUso)) {
            JOptionPane.showMessageDialog(null, "No se pueden cambiar "
                    + "intervalos de años anteriores",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nuevoIntervalo = JOptionPane.showInputDialog
            (null, "Escriba el nuevo intervalo", "Cambiar intervalo",
                JOptionPane.INFORMATION_MESSAGE);
        if (nuevoIntervalo == null) {
            return;
        }
        if (!nuevoIntervalo.isEmpty()) {
            String antiguoIntervalo = this.comboFechas.getSelectedItem()
                    .toString();
            java.util.GregorianCalendar calendario
                    = new java.util.GregorianCalendar();
            String year = String.valueOf(calendario.get
                (java.util.Calendar.YEAR));
            File fichero = new File("datos/ofertas"
                    + antiguoIntervalo + year + ".xml");
            if (fichero.exists()) {
                fichero.renameTo(new File
                    ("datos/ofertas" + nuevoIntervalo + year + ".xml"));
                int indice = this.comboFechas.getSelectedIndex();
                if(indice == -1) indice = 0;
                this.listadoFechas.set(indice, nuevoIntervalo);
                this.comboFechas.setSelectedItem(nuevoIntervalo);
                this.grabarFechas();
            }
        }
    }

    /**
     * Método ejecutado cuando el usuario elije en el menú de utilidades la
     * opción 'Cargar otro año'.
     */
    @Action
    public void accionCargarAnho() {
        ArrayList<String> listadoAnhos
                = comprobarDirectoriosAnhos(new File("datos"));
        if (listadoAnhos.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No existen años anteriores",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        } else {
            listadoAnhos.add(OfertasView.anhoActual);
            DialogoAnhos dialogoAnhos = new DialogoAnhos(listadoAnhos);
            dialogoAnhos.pack();
            dialogoAnhos.setVisible(true);
        }
    }

    /**
     * Método ejecutado cuando se pulsa el radio botón que está en la barra de
     * mensajes. Lo que hace es cambiar el flag booleano 'hayQueGrabar' y
     * cambiar el texto y el color del texto del radio botón y del botón salir.
     */
    @Action
    public void accionCambiarFlagGrabar() {
        this.hayQueGrabar = !this.hayQueGrabar;
        this.cambiarEtiquetasBotonesSalir();
    }

    /**
     * Este método cambia el texto contenido en la etiqueta año.
     */
    private void cambiarEtiquetaAnho() {
        this.etiquetaAnho.setText(OfertasView.anhoEnUso);
    }

    /**
     * Este método cambia la apariencia del botón Salir y del radio botón
     * Grabar dependiendo de si hemos seleccionado grabar los datos ó no 
     * grabarlos.
     */
    private void cambiarEtiquetasBotonesSalir() {
        /* el Color(0,204,51) es un verde más oscuro que el GREEN de la
           clase Color. Se ve mejor.*/
        Color verdeOscuro = new Color(0, 204, 51);
        URL url_verde = 
                this.getClass().getResource("resources/salir_verde.jpeg");
        URL url_roja = 
                this.getClass().getResource("resources/salir_roja.jpeg");
        ImageIcon salir_verde = new ImageIcon(url_verde);
        ImageIcon salir_roja = new ImageIcon(url_roja);
        if (this.hayQueGrabar) {
            this.jRadioButton1.setForeground(verdeOscuro);
            this.jRadioButton1.setText("GRABAR");
            this.botonSalir.setForeground(verdeOscuro);
            this.botonSalir.setIcon(salir_verde);
            this.botonSalir.setToolTipText
                ("Pulse para grabar las ofertas y salir del programa");
        } else {
            this.jRadioButton1.setForeground(Color.RED);
            this.jRadioButton1.setText("NO GRABAR");
            this.botonSalir.setForeground(Color.RED);
            this.botonSalir.setIcon(salir_roja);
            this.botonSalir.setToolTipText
                ("Pulse para salir del programa sin grabar las ofertas");
        }
    }

    /**
     * Este método comprueba la existencia de directorios que contengan datos de
     * años anteriores. Primero obtiene una matriz con todos los ficheros del
     * directorio. Después recorre la matriz y comprueba si el fichero
     * comprobado es un directorio y si su nombre tiene cuatro letras (los
     * directorios de los años son de 4 letras p.e. 2011, 2012, etc.). Si es así
     * los carga en un vector que es el que devolverá el método.
     *
     * @param ruta el directorio donde buscaremos si exiten carpetas de otros
     * años.
     * @return un vector que contiene los nombres de las carpetas de los años
     * anteriores.
     */
    private ArrayList comprobarDirectoriosAnhos(File ruta) {
        ArrayList<String> v = new ArrayList<String>();
        File[] ficheros = ruta.listFiles();
        for (File f : ficheros) {
            if (f.isDirectory() && f.getName().length() == 4) {
                v.add(f.getName());
            }
        }
        return v;
    }

    /**
     * Este método obtiene todos los check boxes que contiene el panel, para
     * almacenarlos en un vector que luego usaremos para recorrerlos uno a uno.
     */
    private ArrayList<JCheckBox> obtenerCheckBoxes() {
        JCheckBox jcb;
        ArrayList<JCheckBox> listadoChecks = new ArrayList<JCheckBox>();
        for (int i = 0; i < this.panelVisual.getComponentCount(); i++) {
            Object o = this.panelVisual.getComponent(i);
            if (o instanceof JCheckBox) {
                jcb = (JCheckBox) o;
                listadoChecks.add(jcb);
            }
        }
        return listadoChecks;
    }

    /**
     * Este método obtiene el nombre de cada uno de los artículos contenidos en
     * el vector de artículos y luego le pasa dicho nombre al método
     * 'comprobarNombreArticulo'
     */
    private void comprobarOfertas() {
        String nombre;
        ArrayList<JCheckBox> v = this.obtenerCheckBoxes();
        this.inicializarCheckBoxes(v);
        for (int i = 0; i < this.listadoOfertas.size(); i++) {
            nombre = this.listadoOfertas.get(i).getNombreArticulo();
            comprobarNombreArticulo(nombre, v);
        }
    }

    /**
     * Método utilizado para inicializar los check boxes que presentan los 
     * productos de la oferta.
     * @param v el vector que contiene los check boxes.
     */
    private void inicializarCheckBoxes(ArrayList<JCheckBox> v) {
        for (int i = 0; i < v.size(); i++) {
            JCheckBox j = v.get(i);
            j.setForeground(Color.RED);
            j.setBackground(new Color(236, 233, 216));
            j.setSelected(false);
        }
    }

    /**
     * Este método comprueba si en el nombre del artículo pasado como parámetro
     * está contenido el texto de alguno de los check boxes (los cuales
     * contienen los nombres de los posibles artículos que se pueden incluir en
     * la oferta). En el caso de que dicho texto esté en el nombre del artículo,
     * se selecciona dicho check box haciendo una pulsación por sofware y se
     * elimina del vector de check boxes para no volverlo a comprobar a
     * posteriori.
     *
     * @param n el nombre del artículo que vamos a comprobar.
     */
    private void comprobarNombreArticulo
        (String n, ArrayList<JCheckBox> listadoChecks) {
        JCheckBox j;
        String texto;
        for (int i = 0; i < listadoChecks.size(); i++) {
            j = listadoChecks.get(i);
            /*j.setSelected(false);
            j.setForeground(Color.RED);
            j.setBackground(new Color(236,233,216));*/
            texto = j.getText();
            if (n.contains(texto)) {
                if (!j.isSelected()) {
                    j.setSelected(true);
                    j.setForeground(Color.GREEN);
                    j.setBackground(new Color(153, 153, 153));
                    listadoChecks.remove(j);
                }
            }
        }
    }

    /**
     * Este método graba en un fichero de texto el nombre del archivo que 
     * contiene los datos de la oferta que estamos preparando en este momento.
     */
    private void grabarNombreFicheroOfertaActual() {
        File fic = new File("datos/ofertaActual.txt");
        StringBuilder nombre = new StringBuilder();
        nombre.append("ofertas")
                .append(this.comboFechas.getSelectedItem().toString())
                .append(anhoEnUso).append(".xml");
        IOFichero.stringAFichero
            (nombre.toString(), fic, Charset.forName("UTF-16"));
    }

    /**
     * Clase que crea un diálogo que contiene un ComboBox que contiene los
     * nombres de los años anteriores guardados. A este ComboBox se le añade un
     * escuchador de eventos para que al seleccionar un item (un año) cargue las
     * ofertas que había en ese año desde los ficheros del disco.
     */
    private class DialogoAnhos extends JDialog {

        private DialogoAnhos dialogoActual;

        public DialogoAnhos(ArrayList v) {
            dialogoActual = this;
            this.setTitle("Años anteriores");
            this.setPreferredSize(new Dimension(200, 60));
            this.setLocationRelativeTo(null);
            JComboBox boxAnhos = new JComboBox(v.toArray(new String[0]));
            boxAnhos.setEditable(false);
            boxAnhos.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    String antiguoAnhoEnUso = OfertasView.anhoEnUso;
                    JComboBox combo = (JComboBox) evt.getSource();
                    OfertasView.anhoEnUso = (String) combo.getSelectedItem();
                    if (!OfertasView.anhoEnUso.equalsIgnoreCase(anhoActual)) {
                        comboFechas.setEditable(false);
                    } else {
                        comboFechas.setEditable(true);
                    }
                    // si existen datos de otros años cargamos los datos de la 
                    //oferta que se corresponda con el índice nº 0.
                    if (cargarDatosOtroAnho()) {
                        cargarOfertaSeleccionada();
                    } else {
                        OfertasView.anhoEnUso = antiguoAnhoEnUso;
                    }
                    dialogoActual.setVisible(false);
                    dialogoActual = null;
                }
            });
            this.add(boxAnhos);
        }
    }

    /*
     * Declaración de las variables utilizadas en el programa.
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem borrarMenuItem;
    private javax.swing.JButton botonAnadir;
    private javax.swing.JButton botonBorrar;
    private javax.swing.JButton botonGuardar;
    private javax.swing.JButton botonModificar;
    private javax.swing.JButton botonSalir;
    private javax.swing.JMenuItem cambiarMenuItem;
    private javax.swing.JMenuItem cargarMenuItem;
    private javax.swing.JComboBox comboFechas;
    private javax.swing.JLabel etiquetaAnho;
    private javax.swing.JLabel etiquetaPeriodoOfertas;
    private javax.swing.JMenuItem imprimirMenuItem;
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
    private javax.swing.JCheckBox jCheckBox3;
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
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList listaOfertas;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel panelVisual;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTextField textoOferta;
    private javax.swing.JTextArea textoTotales;
    private javax.swing.JMenu utilMenu;
    // End of variables declaration//GEN-END:variables
    // Variables usadas en la clase.
    private int[] roturasPagina = null;
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private ArrayList<ArticuloOferta> listadoOfertas;
    private ArrayList<String> listadoFechas;
    private boolean modificando = false;
    private int indiceModificar = -1;
    private boolean inicioSesion = true;
    private boolean hayQueGrabar = false;
    private String fechaOfertaActual = null;
    private static String anhoActual = null;
    private static String anhoEnUso = null;
    private final String[] DOVinos = {"Albariño", "Godello", "Mencia",
        "Penedes", "Ribeiro", "R.Duero", "Rioja", "Somontano", "Toro",
        "Valdepeñas", "Otros"};
    private final int ALBARIÑO = 0;
    private final int GODELLO = 1;
    private final int MENCIA = 2;
    private final int PENEDES = 3;
    private final int RIBEIRO = 4;
    private final int DUERO = 5;
    private final int RIOJA = 6;
    private final int SOMONTANO = 7;
    private final int TORO = 8;
    private final int VALDEPEÑAS = 9;
    private final int OTROS = 10;
    private JDialog aboutBox;
}
