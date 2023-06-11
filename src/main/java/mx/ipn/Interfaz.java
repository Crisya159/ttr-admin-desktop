package mx.ipn;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.bson.Document;
import org.bson.types.Binary;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class Interfaz extends JFrame{
    private JTabbedPane tabbedPane;

    private static App app;

    // ------------------------------------------------------ informacion para conectarse a la base de datos ------------------------------------------------------
    // ------------------------------------------------------------------------(SENSIBLE)--------------------------------------------------------------------------
    private static String connectionString = "mongodb://localhost:27017/test";
    private static ServerApi serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build();
    private static MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .serverApi(serverApi)
            .build();
    private static MongoClient mongoClient = MongoClients.create(settings);
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static void main(String[] args) {
        // Se crea un cliente para conectarse a la base de datos
        try {
            app = new App(mongoClient);
            new Interfaz();
        } catch (MongoException e) {
                e.printStackTrace();
        }
    }

    public Interfaz() {
        // Set up the frame
        setTitle("Simple GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);

        // Create the tabbed pane
        tabbedPane = new JTabbedPane();
        getContentPane().add(tabbedPane);

        //Panel Principal
        generaTabPrincipal();

        // Panel de todos los reportes
        generaTabAllReportes();

        // Panel para buscar por numero de Trabajo Terminal
        generaTabBuscaPorTT();

        // Panel para monstrar reportes pendientes de revision
        generaTabReportesPendientes();

        // Panel de usuarios registrados
        generaTabUsuariosRegistrados();

        // Show the frame
        setVisible(true);
    }

    //Metodo para generar la pestaña principal
    private void generaTabPrincipal(){
        JPanel principalPanel = new JPanel(new BorderLayout());
        tabbedPane.add("Inicio", principalPanel );
        principalPanel.add(new JLabel("<html><body><h1> Bienvenido a la aplicación para reportes de Trabajo Terminal </h1></body></html>", SwingConstants.CENTER), BorderLayout.CENTER);

        JPanel avisoPanel = new JPanel(new BorderLayout());
        principalPanel.add(avisoPanel, BorderLayout.SOUTH);
        //avisoPanel.add(new JLabel("<html><body style='width: 80%'><p> Política de uso: El Departamento de Extensión y Apoyos Educativos de la Escuela Superior de Cómputo (ESCOM) del Instituto Politécnico Nacional (IPN), es el responsable del uso que se da a la información que se ingrese a este sistema, y su uso estará sujeto a su previa autorización y en apego al Artículo 16 de la Constitución Política de los Estados Unidos Mexicanos y la normatividad establecida por el Instituto Nacional de Acceso a la Información (INAI). </p></body></html>", SwingConstants.CENTER), BorderLayout.NORTH);
        //avisoPanel.add(new JLabel("<html><body style='width: 80%; text-align:center'>Política de uso: El Departamento de Extensión y Apoyos Educativos de la Escuela Superior de Cómputo (ESCOM) del Instituto Politécnico Nacional (IPN). <br>        Aviso de Privacidad: La información que ingrese será para el uso exclusivo del control de los reportes de trabajo terminal II, y no podrá ser utilizada con otros fines.</body></html>", SwingConstants.CENTER), BorderLayout.SOUTH);
        avisoPanel.add(
            new JLabel(
                "<html><body style='width: 80%'>"+
                "Política de uso: El Departamento de Extensión y Apoyos Educativos de la Escuela Superior de Cómputo (ESCOM) del Instituto Politécnico Nacional (IPN), es el responsable del uso que se da a la información que se ingrese a este sistema, y su uso estará sujeto a su previa autorización y en apego al Artículo 16 de la Constitución Política de los Estados Unidos Mexicanos y la normatividad establecida por el Instituto Nacional de Acceso a la Información (INAI)."+
                "<br>"+
                "Aviso de Privacidad: La información que ingrese será para el uso exclusivo del control de los reportes de trabajo terminal II, y no podrá ser utilizada con otros fines."+
                "</body></html>"
            , SwingConstants.CENTER), BorderLayout.SOUTH
        );
    }

    private DefaultTableModel generaTablaReportes(){
        // Se obtienen todos los reportes
        List<Document> reports = app.getReports();
        // Se crea un table model para la tabla
        DefaultTableModel reportesRegistradosTableModel = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Se agregan las columnas al table model
        reportesRegistradosTableModel.addColumn("Trabajo Terminal");
        reportesRegistradosTableModel.addColumn("Nombre del reporte técnico");
        reportesRegistradosTableModel.addColumn("Versión");
        reportesRegistradosTableModel.addColumn("Fecha de subida");
        reportesRegistradosTableModel.addColumn("Estatus");

        // Se agregan los usuarios al table model
        if (reports != null) {
            for (Document report : reports) {
                reportesRegistradosTableModel.addRow(new Object[]{report.get("numero_tt"), report.get("filename"), report.get("version"),
                                                                report.get("createdAt"), report.get("aprobado")});
            }
        } else {
            System.out.println("No se encontraron reportes.");
        }

        return reportesRegistradosTableModel;
    }

    // Metodo para generar la pestaña donde se muestran todos los reportes
    private void generaTabAllReportes(){
        JPanel allPDFsPanel = new JPanel(new BorderLayout());
        tabbedPane.addTab("Todos los reportes técnicos", allPDFsPanel);
        allPDFsPanel.add(new JLabel("Tabla de todos los reportes técnicos", SwingConstants.CENTER), BorderLayout.NORTH);
        JButton botonActualizar = new JButton("Actualizar");
        allPDFsPanel.add(botonActualizar, BorderLayout.EAST);


        // Se crea una tabla para mostrar los reportes
        JTable reportesRegistradosTable = new JTable();
        // Se crea un scroll para la tabla
        JScrollPane reportesRegistradosScrollPane = new JScrollPane(reportesRegistradosTable);
        // Se agrega la tabla al panel
        allPDFsPanel.add(reportesRegistradosScrollPane, BorderLayout.CENTER);

        DefaultTableModel newModel = generaTablaReportes();

        // Se muestra la tabla
        reportesRegistradosTable.setModel(newModel);
        reportesRegistradosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportesRegistradosTable.setRowSelectionAllowed(true);
        reportesRegistradosTable.setColumnSelectionAllowed(false);
        reportesRegistradosTable.setRowHeight(30);
        reportesRegistradosTable.getTableHeader().setReorderingAllowed(false);
        reportesRegistradosTable.getTableHeader().setResizingAllowed(false);
        reportesRegistradosTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        reportesRegistradosTable.setAutoCreateRowSorter(true);

        // Se actualiza la lista de todos los reportes
        botonActualizar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel newModel = generaTablaReportes();
                reportesRegistradosTable.setModel(newModel);
                reportesRegistradosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                reportesRegistradosTable.setRowSelectionAllowed(true);
                reportesRegistradosTable.setColumnSelectionAllowed(false);
                reportesRegistradosTable.setRowHeight(30);
                reportesRegistradosTable.getTableHeader().setReorderingAllowed(false);
                reportesRegistradosTable.getTableHeader().setResizingAllowed(false);
                reportesRegistradosTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                reportesRegistradosTable.setAutoCreateRowSorter(true);
            }
        });
        //Boton para convertir la tabla a excel
        JButton botonImprimirExcelTablaReportes = new JButton("Imprimir tabla");
        allPDFsPanel.add(botonImprimirExcelTablaReportes, BorderLayout.SOUTH);

        botonImprimirExcelTablaReportes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ImpresoraTabla impresora = new ImpresoraTabla();
                String ruta = "C:/Users/Luona/OneDrive/Escritorio/";
                String nombreArchivo= "reportesRegistradosTable.xlsx";
                impresora.imprimir(reportesRegistradosTable, ruta + nombreArchivo);
                JOptionPane.showMessageDialog(null, "El archivo se encuentra en el escritorio.");
            }
        });

    }


    // Metodo para generar la pestaña donde se busca por numero de Trabajo Terminal
    private void generaTabBuscaPorTT(){
        JPanel buscaPorTTPanel = new JPanel(new BorderLayout());
        tabbedPane.addTab("Buscar reporte técnico por Trabajo Terminal", buscaPorTTPanel);
        buscaPorTTPanel.add(new JLabel("Buscar por TT", SwingConstants.CENTER), BorderLayout.NORTH);


        JPanel busquedaYResultadosPanel = new JPanel(new BorderLayout());
        buscaPorTTPanel.add(busquedaYResultadosPanel, BorderLayout.CENTER);

        // Se crea un panel para el campo de texto y el boton
        JPanel buscaPorTTInputPanel = new JPanel(new FlowLayout());
        // Se crea el campo de texto y el boton
        JTextField buscaPorTTField = new JTextField(20);
        JButton buscaPorTTButton = new JButton("Buscar");
        // Se agrega el campo de texto y el boton al panel
        buscaPorTTInputPanel.add(buscaPorTTField);
        buscaPorTTInputPanel.add(buscaPorTTButton);
        // Se agrega el panel al panel principal
        busquedaYResultadosPanel.add(buscaPorTTInputPanel, BorderLayout.NORTH);
        // Se crea una lista para mostrar los PDFs
        JList<String> buscaPorTTList = new JList<String>();
        // Se crea un scroll para la lista
        JScrollPane buscaPorTTScrollPane = new JScrollPane(buscaPorTTList);
        // Se agrega la lista al panel
        busquedaYResultadosPanel.add(buscaPorTTScrollPane, BorderLayout.CENTER);
        // Se crea una lista para guardar los nombres de los PDFs
        ArrayList<String> buscaPorTT = new ArrayList<String>();
        // Se agrega el listener al boton
        buscaPorTTButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buscaPorTT.clear();
                // Se obtiene el numero de TT
                String searchTerm = buscaPorTTField.getText();
                // Se obtienen los PDFs con ese numero de TT
                List<Document> reports = app.getReportsByTT(searchTerm);

                // Se agregan los nombres de los PDFs a la lista
                if (reports != null && reports.iterator().hasNext()){
                    for (Document report : reports) {
                        buscaPorTT.add("(" + report.get("numero_tt") + ") " + report.get("filename").toString() + " version: " + report.get("version").toString());
                    }
                }else{
                    buscaPorTT.add("No se encontraron reportes para la busqueda " + searchTerm + ".");
                }

                // Se convierte la lista a un arreglo
                String[] buscaPorTTArray = new String[buscaPorTT.size()];
                buscaPorTTArray = buscaPorTT.toArray(buscaPorTTArray);
                // Se muestra la lista
                buscaPorTTList.setListData(buscaPorTTArray);
            }
        });

        // boton para abrir el reporte seleccionado
        JButton openFileButton = new JButton("Abrir reporte seleccionado");
        busquedaYResultadosPanel.add(openFileButton, BorderLayout.SOUTH);

        // Abrir reporte seleccionado de la lista en un navegador
        openFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Se obtiene el nombre del PDF seleccionado
                String selectedPDF = buscaPorTTList.getSelectedValue();
                // Se obtiene el nombre del PDF
                String filename = selectedPDF.substring(selectedPDF.indexOf(")") + 2, selectedPDF.indexOf("version:") - 1);
                // Se obtiene la version del PDF
                String version = selectedPDF.substring(selectedPDF.indexOf("version:") + 9);
                // Se obtiene el PDF
                Document report = app.getReportToOpen(filename, version);
                // Extract the binary data and create PDF document
                if (report != null) {
                    try {
                        Binary pdfData = report.get("data", Binary.class);
                        byte[] bytes = pdfData.getData();
                        PDDocument document = PDDocument.load(new ByteArrayInputStream(bytes));

                        // Save the PDF to a temporary file
                        File tempFile = File.createTempFile("my_report", ".pdf");
                        document.save(tempFile);

                        // Open the PDF in a browser
                        Desktop.getDesktop().browse(tempFile.toURI());

                        // Close the PDF document
                        document.close();
                    } catch (Exception e1) {
                        System.out.println("Error al abrir el PDF.");
                        System.out.println(e);
                    }

                } else {
                    System.out.println("Reportes no encontrados.");
                }
            }
        });

        // ------------------------------------ Espacio para mostrar detalles del reporte seleccionado ------------------------------------
        JPanel reportDetailsPanel = new JPanel(new BorderLayout());
        buscaPorTTPanel.add(reportDetailsPanel, BorderLayout.SOUTH);

        JLabel reportDetailsLabel = new JLabel("Detalles del reporte");
        reportDetailsPanel.add(reportDetailsLabel, BorderLayout.NORTH);


        JPanel reportDetailsLabelsPanel = new JPanel(new GridLayout(5, 1));
        String selectedPDF = buscaPorTTList.getSelectedValue();
        String filename = "", version = "", date = " ";

        if (selectedPDF == null){
            selectedPDF = "No se ha seleccionado un reporte.";
        }else{
            // Se obtiene el nombre del PDF
            filename = selectedPDF.substring(selectedPDF.indexOf(")") + 2, selectedPDF.indexOf("version:") - 1);
            // Se obtiene la version del PDF
            version = selectedPDF.substring(selectedPDF.indexOf("version:") + 9);
            // Se obtiene el PDF
            final Document report = app.getReportToOpen(filename, version);
            // Se obtienen los detalles del reporte
            date = report.get("updatedAt").toString();
        }

        // Se crean canvas para mostrar los detalles del reporte
        JLabel reportDetailsTT = new JLabel("TT: " + buscaPorTTField.getText());
        JLabel reportDetailsFilename = new JLabel("Nombre: " + filename);
        JLabel reportDetailsVersion = new JLabel("Version: " + version);
        JLabel reportDetailsDate = new JLabel("Fecha de carga: " + date);
        JLabel reportDetailStatus = new JLabel("Estado: ");

        reportDetailsLabelsPanel.add(reportDetailsTT);
        reportDetailsLabelsPanel.add(reportDetailsFilename);
        reportDetailsLabelsPanel.add(reportDetailsVersion);
        reportDetailsLabelsPanel.add(reportDetailsDate);
        reportDetailsLabelsPanel.add(reportDetailStatus);

        reportDetailsPanel.add(reportDetailsLabelsPanel, BorderLayout.CENTER);

        // Obtener detalles del reporte seleccionado en la lista y mostrarlos
        buscaPorTTList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedItem = buscaPorTTList.getSelectedValue();
                if (selectedItem == null){
                    reportDetailsTT.setText("TT: ");
                    reportDetailsFilename.setText("Nombre: ");
                    reportDetailsVersion.setText("Version: ");
                    reportDetailsDate.setText("Fecha de carga: ");
                    reportDetailStatus.setText("Estado: ");
                }else{
                    reportDetailsTT.setText("TT: " + selectedItem.substring(selectedItem.indexOf("(") + 1, selectedItem.indexOf(")")));
                    reportDetailsFilename.setText("Nombre: " + selectedItem.substring(selectedItem.indexOf(")") + 2, selectedItem.indexOf("version:") - 1));
                    reportDetailsVersion.setText("Version: " + selectedItem.substring(selectedItem.indexOf("version:") + 9));
                    Document report = app.getReportToOpen(selectedItem.substring(selectedItem.indexOf(")") + 2, selectedItem.indexOf("version:") - 1),
                                                            selectedItem.substring(selectedItem.indexOf("version:") + 9));
                    reportDetailsDate.setText("Fecha de carga: " + report.get("updatedAt").toString());
                    if (report.get("aprobado").toString().equals("Aprobado")){
                        reportDetailStatus.setText("Estado: " + "Aprobado");
                    }else if(report.get("aprobado").toString().equals("Rechazado")){
                        reportDetailStatus.setText("Estado: " + "Rechazado");
                    }else{
                        reportDetailStatus.setText("Estado: " + "Pendiente de revisión");
                    }

                }
            }
        });

        // ----------------------------------- Fin espacio para mostrar detalles del reporte seleccionado ----------------------------------

        //  ---------------------------------------- Espacio para opciones de evaluacion del reporte ---------------------------------------
        JPanel reportEvaluationPanel = new JPanel(new BorderLayout());
        reportDetailsPanel.add(reportEvaluationPanel, BorderLayout.SOUTH);

        JRadioButton opcionAprobar = new JRadioButton("Aprobar");
        JRadioButton opcionRechazar = new JRadioButton("Rechazar");
        JButton botonEnviar = new JButton("Evaluar");

        JLabel labelComentarios = new JLabel("Comentarios:");
        JTextField comentarios = new JTextField(20);

        opcionRechazar.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(opcionAprobar);
        group.add(opcionRechazar);

        JPanel reportEvaluationButtonsPanel = new JPanel();
        reportEvaluationButtonsPanel.add(opcionAprobar, BorderLayout.WEST);
        reportEvaluationButtonsPanel.add(opcionRechazar, BorderLayout.EAST);

        reportEvaluationPanel.add(reportEvaluationButtonsPanel, BorderLayout.NORTH);

        JPanel reportEvaluationCommentsPanel = new JPanel(new BorderLayout());
        reportEvaluationPanel.add(reportEvaluationCommentsPanel, BorderLayout.CENTER);

        reportEvaluationCommentsPanel.add(labelComentarios, BorderLayout.NORTH);
        reportEvaluationCommentsPanel.add(comentarios, BorderLayout.CENTER);

        reportEvaluationPanel.add(botonEnviar, BorderLayout.SOUTH);

        // Deshabilitar comentarios si se selecciona aprobar
        opcionAprobar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                comentarios.setEnabled(false);
            }
        });

        // Habilitar comentarios si se selecciona rechazar
        opcionRechazar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                comentarios.setEnabled(true);
            }
        });

        // Enviar evaluacion del reporte
        botonEnviar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedPDF = buscaPorTTList.getSelectedValue();
                String filename = "", version = "", tt="";

                if (selectedPDF == null){
                    selectedPDF = "No se ha seleccionado un reporte.";
                }else{
                    // Se obtiene el nombre del PDF
                    filename = selectedPDF.substring(selectedPDF.indexOf(")") + 2, selectedPDF.indexOf("version:") - 1);
                    // Se obtiene la version del PDF
                    version = selectedPDF.substring(selectedPDF.indexOf("version:") + 9);
                    // Se obtiene el TT del PDF
                    tt = selectedPDF.substring(selectedPDF.indexOf("(") + 1, selectedPDF.indexOf(")"));
                }

                if (opcionAprobar.isSelected()){
                    // Se aprueba el reporte
                    app.evaluateReport(filename, version, tt, "Aprobado", "");
                    // Se actualiza la lista de reportes
                    actualizaListaReportes(buscaPorTTField.getText(), buscaPorTTList);
                    // Se muestra mensaje de exito
                    JOptionPane.showMessageDialog(null, "El reporte ha sido aprobado.");
                }else{
                    // Se rechaza el reporte
                    app.evaluateReport(filename, version, tt, "Rechazado", comentarios.getText());
                    // Se actualiza la lista de reportes
                    actualizaListaReportes(buscaPorTTField.getText(), buscaPorTTList);
                    // Se muestra mensaje de exito
                    JOptionPane.showMessageDialog(null, "El reporte ha sido rechazado.");
                }
            }
        });

        //  -------------------------------------- Fin espacio para opciones de evaluacion del reporte -------------------------------------


    }

    private void actualizaListaReportes(String tt, JList<String> buscaPorTTList){
        // Se obtienen los reportes
        List<Document> reports = app.getReportsByTT(tt);
        // Se crea un modelo para la lista
        DefaultListModel<String> listModel = new DefaultListModel<>();
        // Se agregan los reportes al modelo
        for (Document report : reports){
            listModel.addElement("(" + report.get("numero_tt") + ") " + report.get("filename").toString() + " version: " + report.get("version").toString());
        }
        // Se agrega el modelo a la lista
        buscaPorTTList.setModel(listModel);
    }

    // Metodo para generar la pestaña donde se muestran los reportes pendientes de revision
    private void generaTabReportesPendientes(){
        JPanel reportesPendientesPanel = new JPanel(new BorderLayout());
        tabbedPane.addTab("Reportes técnicos pendientes", reportesPendientesPanel);
        reportesPendientesPanel.add(new JLabel("Reportes técnicos pendientes", SwingConstants.CENTER), BorderLayout.NORTH);

        JPanel listaReportesPendientesPanel = new JPanel(new BorderLayout());
        reportesPendientesPanel.add(listaReportesPendientesPanel, BorderLayout.CENTER);

        // Se obtienen todos los reportes pendientes
        List<Document> reports = app.getReportsByStatus("Esperando");
        // Se crea una lista para mostrar los reportes
        JList<String> reportesPendientesList = new JList<>();
        // Se crea un scroll para la lista
        JScrollPane reportesPendientesScrollPane = new JScrollPane(reportesPendientesList);
        // Se agrega la lista al panel
        listaReportesPendientesPanel.add(reportesPendientesScrollPane, BorderLayout.CENTER);
        // Se crea un list model para la lista
        DefaultListModel<String> reportesPendientesListModel = new DefaultListModel<>();
        // Se agregan los reportes al list model
        for (Document report : reports){
            reportesPendientesListModel.addElement("(" + report.get("numero_tt") + ") " + report.get("filename").toString() + " version: " + report.get("version").toString());
        }
        // Se agrega el list model a la lista
        reportesPendientesList.setModel(reportesPendientesListModel);

        // boton para abrir el reporte seleccionado
        JButton botonAbrirReporte = new JButton("Abrir reporte técnico");
        listaReportesPendientesPanel.add(botonAbrirReporte, BorderLayout.SOUTH);

        // Abrir reporte seleccionado de la lista en un navegador
        botonAbrirReporte.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Se obtiene el nombre del PDF seleccionado
                String selectedPDF = reportesPendientesList.getSelectedValue();
                // Se obtiene el nombre del PDF
                String filename = selectedPDF.substring(selectedPDF.indexOf(")") + 2, selectedPDF.indexOf("version:") - 1);
                // Se obtiene la version del PDF
                String version = selectedPDF.substring(selectedPDF.indexOf("version:") + 9);
                // Se obtiene el PDF
                Document report = app.getReportToOpen(filename, version);
                // Extract the binary data and create PDF document
                if (report != null) {
                    try {
                        Binary pdfData = report.get("data", Binary.class);
                        byte[] bytes = pdfData.getData();
                        PDDocument document = PDDocument.load(new ByteArrayInputStream(bytes));

                        // Save the PDF to a temporary file
                        File tempFile = File.createTempFile("my_report", ".pdf");
                        document.save(tempFile);

                        // Open the PDF in a browser
                        Desktop.getDesktop().browse(tempFile.toURI());

                        // Close the PDF document
                        document.close();
                    } catch (Exception e1) {
                        System.out.println("Error al abrir el PDF.");
                        System.out.println(e);
                    }

                } else {
                    System.out.println("Reportes no encontrados.");
                }
            }
        });

        JButton botonActualizar = new JButton("Actualizar");
        reportesPendientesPanel.add(botonActualizar, BorderLayout.LINE_END);

        // Se actualiza la lista de reportes pendientes
        botonActualizar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Se obtienen todos los reportes pendientes
                List<Document> reports = app.getReportsByStatus("Esperando");
                // Se crea un list model para la lista
                DefaultListModel<String> reportesPendientesListModel = new DefaultListModel<>();
                // Se agregan los reportes al list model
                for (Document report : reports){
                    reportesPendientesListModel.addElement("(" + report.get("numero_tt") + ") " + report.get("filename").toString() + " version: " + report.get("version").toString());
                }
                // Se agrega el list model a la lista
                reportesPendientesList.setModel(reportesPendientesListModel);
            }
        });

        // ------------------------------------ Espacio para mostrar detalles del reporte seleccionado ------------------------------------
        JPanel reportDetailsPanel = new JPanel(new BorderLayout());
        reportesPendientesPanel.add(reportDetailsPanel, BorderLayout.SOUTH);

        JLabel reportDetailsLabel = new JLabel("Detalles del reporte");
        reportDetailsPanel.add(reportDetailsLabel, BorderLayout.NORTH);


        JPanel reportDetailsLabelsPanel = new JPanel(new GridLayout(5, 1));
        String selectedPDF = reportesPendientesList.getSelectedValue();
        String filename = "", version = "", date = " ", tt= " ";

        if (selectedPDF == null){
            selectedPDF = "No se ha seleccionado un reporte.";
        }else{
            // Se obtiene el nombre del PDF
            filename = selectedPDF.substring(selectedPDF.indexOf(")") + 2, selectedPDF.indexOf("version:") - 1);
            // Se obtiene la version del PDF
            version = selectedPDF.substring(selectedPDF.indexOf("version:") + 9);
            // Se obtiene el tt del PDF
            tt = selectedPDF.substring(selectedPDF.indexOf("(") + 1, selectedPDF.indexOf(")"));
            // Se obtiene el PDF
            final Document report = app.getReportToOpen(filename, version);
            // Se obtienen los detalles del reporte
            date = report.get("updatedAt").toString();
        }

        // Se crean canvas para mostrar los detalles del reporte
        JLabel reportDetailsTT = new JLabel("TT: " + tt);
        JLabel reportDetailsFilename = new JLabel("Nombre: " + filename);
        JLabel reportDetailsVersion = new JLabel("Version: " + version);
        JLabel reportDetailsDate = new JLabel("Fecha de carga: " + date);
        JLabel reportDetailStatus = new JLabel("Estado: ");

        reportDetailsLabelsPanel.add(reportDetailsTT);
        reportDetailsLabelsPanel.add(reportDetailsFilename);
        reportDetailsLabelsPanel.add(reportDetailsVersion);
        reportDetailsLabelsPanel.add(reportDetailsDate);
        reportDetailsLabelsPanel.add(reportDetailStatus);

        reportDetailsPanel.add(reportDetailsLabelsPanel, BorderLayout.CENTER);

        // Obtener detalles del reporte seleccionado en la lista y mostrarlos
        reportesPendientesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedItem = reportesPendientesList.getSelectedValue();
                if (selectedItem == null){
                    reportDetailsTT.setText("TT: ");
                    reportDetailsFilename.setText("Nombre: ");
                    reportDetailsVersion.setText("Version: ");
                    reportDetailsDate.setText("Fecha de carga: ");
                    reportDetailStatus.setText("Estado: ");
                }else{
                    reportDetailsTT.setText("TT: " + selectedItem.substring(selectedItem.indexOf("(") + 1, selectedItem.indexOf(")")));
                    reportDetailsFilename.setText("Nombre: " + selectedItem.substring(selectedItem.indexOf(")") + 2, selectedItem.indexOf("version:") - 1));
                    reportDetailsVersion.setText("Version: " + selectedItem.substring(selectedItem.indexOf("version:") + 9));
                    Document report = app.getReportToOpen(selectedItem.substring(selectedItem.indexOf(")") + 2, selectedItem.indexOf("version:") - 1),
                                                            selectedItem.substring(selectedItem.indexOf("version:") + 9));
                    reportDetailsDate.setText("Fecha de carga: " + report.get("updatedAt").toString());
                    if (report.get("aprobado").toString().equals("Aprobado")){
                        reportDetailStatus.setText("Estado: " + "Aprobado");
                    }else if(report.get("aprobado").toString().equals("Rechazado")){
                        reportDetailStatus.setText("Estado: " + "Rechazado");
                    }else{
                        reportDetailStatus.setText("Estado: " + "Pendiente de revisión");
                    }

                }
            }
        });

        // ----------------------------------- Fin espacio para mostrar detalles del reporte seleccionado ----------------------------------

        //  ---------------------------------------- Espacio para opciones de evaluacion del reporte ---------------------------------------
        JPanel reportEvaluationPanel = new JPanel(new BorderLayout());
        reportDetailsPanel.add(reportEvaluationPanel, BorderLayout.SOUTH);

        JRadioButton opcionAprobar = new JRadioButton("Aprobar");
        JRadioButton opcionRechazar = new JRadioButton("Rechazar");
        JButton botonEnviar = new JButton("Evaluar");

        JLabel labelComentarios = new JLabel("Comentarios:");
        JTextField comentarios = new JTextField(20);

        opcionRechazar.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(opcionAprobar);
        group.add(opcionRechazar);

        JPanel reportEvaluationButtonsPanel = new JPanel();
        reportEvaluationButtonsPanel.add(opcionAprobar, BorderLayout.WEST);
        reportEvaluationButtonsPanel.add(opcionRechazar, BorderLayout.EAST);

        reportEvaluationPanel.add(reportEvaluationButtonsPanel, BorderLayout.NORTH);

        JPanel reportEvaluationCommentsPanel = new JPanel(new BorderLayout());
        reportEvaluationPanel.add(reportEvaluationCommentsPanel, BorderLayout.CENTER);

        reportEvaluationCommentsPanel.add(labelComentarios, BorderLayout.NORTH);
        reportEvaluationCommentsPanel.add(comentarios, BorderLayout.CENTER);

        reportEvaluationPanel.add(botonEnviar, BorderLayout.SOUTH);

        // Deshabilitar comentarios si se selecciona aprobar
        opcionAprobar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                comentarios.setEnabled(false);
            }
        });

        // Habilitar comentarios si se selecciona rechazar
        opcionRechazar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                comentarios.setEnabled(true);
            }
        });

        // Enviar evaluacion del reporte
        botonEnviar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedPDF = reportesPendientesList.getSelectedValue();
                String filename = "", version = "", tt="";

                if (selectedPDF == null){
                    selectedPDF = "No se ha seleccionado un reporte.";
                }else{
                    // Se obtiene el nombre del PDF
                    filename = selectedPDF.substring(selectedPDF.indexOf(")") + 2, selectedPDF.indexOf("version:") - 1);
                    // Se obtiene la version del PDF
                    version = selectedPDF.substring(selectedPDF.indexOf("version:") + 9);
                    // Se obtiene el TT del PDF
                    tt = selectedPDF.substring(selectedPDF.indexOf("(") + 1, selectedPDF.indexOf(")"));
                }

                if (opcionAprobar.isSelected()){
                    // Se aprueba el reporte
                    app.evaluateReport(filename, version, tt, "Aprobado", "");
                    // Se actualiza la lista de reportes
                    actualizaListaPendientes(reportesPendientesList);
                    // Se muestra mensaje de exito
                    JOptionPane.showMessageDialog(null, "El reporte ha sido aprobado.");
                }else{
                    // Se rechaza el reporte
                    app.evaluateReport(filename, version, tt, "Rechazado", comentarios.getText());
                    // Se actualiza la lista de reportes
                    actualizaListaPendientes(reportesPendientesList);
                    // Se muestra mensaje de exito
                    JOptionPane.showMessageDialog(null, "El reporte ha sido rechazado.");
                }
            }
        });

        //  -------------------------------------- Fin espacio para opciones de evaluacion del reporte -------------------------------------
    }

    private void actualizaListaPendientes(JList<String> reportesPendientesList){
        // Se obtienen los reportes
        List<Document> reports = app.getReportsByStatus("Esperando evaluación");
        // Se crea un modelo para la lista
        DefaultListModel<String> listModel = new DefaultListModel<>();
        // Se agregan los reportes al modelo
        for (Document report : reports){
            listModel.addElement("(" + report.get("numero_tt") + ") " + report.get("filename").toString() + " version: " + report.get("version").toString());
        }
        // Se agrega el modelo a la lista
        reportesPendientesList.setModel(listModel);
    }

    //Metodo para actualizar usuarios
    private DefaultTableModel actualizarTablaUsuarios(){
         // Se obtienen todos los usuarios
         List<Document> users = app.getUsers();
         // Se crea un table model para la tabla
         DefaultTableModel usuariosRegistradosTableModel = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // Se agregan las columnas al table model
        usuariosRegistradosTableModel.addColumn("Nombre");
        usuariosRegistradosTableModel.addColumn("Apellido Paterno");
        usuariosRegistradosTableModel.addColumn("Apellido Materno");
        usuariosRegistradosTableModel.addColumn("Correo electronico");
        usuariosRegistradosTableModel.addColumn("Boleta");
        usuariosRegistradosTableModel.addColumn("TT");
        // Se agregan los usuarios al table model
        if (users != null) {
            for (Document user : users) {
                usuariosRegistradosTableModel.addRow(new Object[]{user.get("nombres"), user.get("apellido_paterno"), user.get("apellido_materno"),
                                                                user.get("correo_electronico"), user.get("boleta"), user.get("tt")});
            }
        } else {
            System.out.println("No se encontraron usuarios.");
        }
        return usuariosRegistradosTableModel;
    }

    // Metodo para generar la pestaña donde se muestran los usuarios registrados en forma de tabla
    private void generaTabUsuariosRegistrados(){
        JPanel usuariosRegistradosPanel = new JPanel(new BorderLayout());
        tabbedPane.addTab("Usuarios Registrados", usuariosRegistradosPanel);
        usuariosRegistradosPanel.add(new JLabel("Usuarios Registrados", SwingConstants.CENTER), BorderLayout.NORTH);
        // Se crea una tabla para mostrar los usuarios
        JTable usuariosRegistradosTable = new JTable();
        // Se crea un scroll para la tabla
        JScrollPane usuariosRegistradosScrollPane = new JScrollPane(usuariosRegistradosTable);
        // Se agrega la tabla al panel
        usuariosRegistradosPanel.add(usuariosRegistradosScrollPane, BorderLayout.CENTER);

        DefaultTableModel newModel = actualizarTablaUsuarios();

        // Se muestra la tabla
        usuariosRegistradosTable.setModel(newModel);
        usuariosRegistradosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usuariosRegistradosTable.setRowSelectionAllowed(true);
        usuariosRegistradosTable.setColumnSelectionAllowed(false);
        usuariosRegistradosTable.setRowHeight(30);
        usuariosRegistradosTable.getTableHeader().setReorderingAllowed(false);
        usuariosRegistradosTable.getTableHeader().setResizingAllowed(false);
        usuariosRegistradosTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        usuariosRegistradosTable.setAutoCreateRowSorter(true);

        //Se crea un boton para actualizar usuarios
        JButton botonActualizar = new JButton("Actualizar");
        usuariosRegistradosPanel.add(botonActualizar, BorderLayout.LINE_END);
        botonActualizar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                DefaultTableModel newModel = actualizarTablaUsuarios();
                usuariosRegistradosTable.setModel(newModel);
                usuariosRegistradosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                usuariosRegistradosTable.setRowSelectionAllowed(true);
                usuariosRegistradosTable.setColumnSelectionAllowed(false);
                usuariosRegistradosTable.setRowHeight(30);
                usuariosRegistradosTable.getTableHeader().setReorderingAllowed(false);
                usuariosRegistradosTable.getTableHeader().setResizingAllowed(false);
                usuariosRegistradosTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                usuariosRegistradosTable.setAutoCreateRowSorter(true);
            }
        });


        // Se crea un boton para eliminar usuarios
        JButton eliminarUsuarioButton = new JButton("Eliminar");
        // Se agrega el boton al panel
        usuariosRegistradosPanel.add(eliminarUsuarioButton, BorderLayout.SOUTH);

        // Se agrega el listener al boton
        eliminarUsuarioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Se obtiene el usuario seleccionado
                int selectedRow = usuariosRegistradosTable.getSelectedRow();
                // Se obtiene el correo electronico del usuario seleccionado
                String correo = usuariosRegistradosTable.getValueAt(selectedRow, 3).toString();
                //Mensaje de confirmación
                int confirmacion = JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar este usuario?");
                if(confirmacion==0){
                    // Se elimina el usuario
                    app.deleteUser(correo);
                    // Se actualiza la tabla
                    newModel.removeRow(selectedRow);
                }

            }
        });
    }
}
