package mx.ipn;

import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JTable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class ImpresoraTabla {

    // Imprimir los datos de la tabla en un archivo de Excel
    // Parametros: table - la tabla que se va a imprimir
    //             filePath - la ruta del archivo a crear
    public void imprimir(JTable table, String filePath) {
        try {
            try ( // Crear un libro de trabajo de Excel 
            Workbook workbook = new XSSFWorkbook()) {
                // Crear una hoja de trabajo de Excel
                Sheet hoja = workbook.createSheet("Datos");

                // Crear un estilo para los encabezados
                CellStyle estiloHeader = workbook.createCellStyle(); // Crear un template para el estilo
                estiloHeader.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex()); // Establecer el color de fondo
                estiloHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND); // Establecer el patron de relleno
                estiloHeader.setAlignment(HorizontalAlignment.CENTER); // Establecer la alineacion
                estiloHeader.setBorderBottom(BorderStyle.THIN); // Establecer el estilo de la linea inferior
                estiloHeader.setBorderLeft(BorderStyle.THIN); // Establecer el estilo de la linea izquierda
                estiloHeader.setBorderRight(BorderStyle.THIN); // Establecer el estilo de la linea derecha
                estiloHeader.setBorderTop(BorderStyle.THIN); // Establecer el estilo de la linea superior
                
                Font font = workbook.createFont(); // Crear un template para la fuente
                font.setColor(IndexedColors.WHITE.getIndex()); // Establecer el color de la fuente
                estiloHeader.setFont(font); // Establecer la fuente

                // Crear la fila de encabezados
                Row filaEncabezados = hoja.createRow(0);
                for (int columna = 0; columna < table.getColumnCount(); columna++) {
                    Object nombreColumna = table.getColumnName(columna); // Obtener el nombre de la columna actual
                    Cell celda = filaEncabezados.createCell(columna); // Crear la celda en la posicion de la columna actual
                    celda.setCellValue(String.valueOf(nombreColumna)); // Escribir el nombre de la columna en la celda

                    celda.setCellStyle(estiloHeader); // Aplicar el estilo a la celda
                    hoja.autoSizeColumn(columna); // Ajustar el ancho de la columna para que quepa el contenido
                }

                // Iterar sobre los datos de la tabla y escribirlos en la hoja de trabajo de Excel
                for (int fila = 0; fila < table.getRowCount(); fila++) {
                    Row columnaHoja = hoja.createRow(fila + 1); // Crear una fila en la posicion de la fila actual (empezando en 1 porque la fila 0 son los encabezados)
                    for (int columna = 0; columna < table.getColumnCount(); columna++) { // Iterar sobre las columnas
                        Object valorCelda = table.getValueAt(fila, columna); // Obtener el valor de la celda en la posicion actual
                        Cell celda = columnaHoja.createCell(columna); // Crear la celda en la posicion de la columna actual
                        celda.setCellValue(String.valueOf(valorCelda)); // Escribir el valor de la celda en la hoja de trabajo de Excel

                        hoja.autoSizeColumn(columna); // Ajustar el ancho de la columna para que quepa el contenido
                    }
                }

                // Escribir el libro de trabajo de Excel en un archivo
                // Si el archivo no existe, se crea
                File archivo = new File(filePath); // Crear un objeto File con la ruta del archivo
                if (!archivo.exists()) {
                    archivo.createNewFile(); // Crear el archivo si no existe
                }

                // Si el archivo existe, se sobreescribe
                try (FileOutputStream outputStream = new FileOutputStream(archivo)) { // Crear un objeto FileOutputStream (para escribir los bytes del archivo)  
                    workbook.write(outputStream); // Escribir el libro de trabajo de Excel en el archivo
                }
            }
            System.out.println("Archivo creado: " + filePath);

        } catch (Exception e) {
            e.printStackTrace(); // Imprimir errores si los hay
        }
    }
}
