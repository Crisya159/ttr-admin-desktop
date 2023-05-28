package mx.ipn;

import java.util.ArrayList;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import org.bson.Document;

public class Notificacion {
    private String from = "ttr2320003@gmail.com"; // Dirección de correo electrónico del remitente
    private String password = "fpgmwxamlwshlkjp"; // Contraseña de la dirección de correo electrónico del remitente
    private Properties properties = new Properties(); // Propiedades para la conexión con el servidor SMTP

    {
        properties.setProperty("mail.smtp.host", "smtp.gmail.com"); // Servidor SMTP (Gmail)
        properties.setProperty("mail.smtp.port", "587"); // Puerto para TLS
        properties.setProperty("mail.smtp.auth", "true"); // Habilitar autenticación
        properties.setProperty("mail.smtp.starttls.enable", "true"); // Habilitar cifrado TLS
    }

    public void sendNotification(ArrayList<Document> to, Document report){
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() { // Crear una sesión de correo electrónico autenticada
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            for ( Document recipient : to ) { // Iterar sobre los destinatarios del mensaje
                String body = "";
                if (report.get("aprobado").toString() == "Aprobado"){ // Construir el cuerpo del mensaje dependiendo del estado de aprobación del reporte
                    body = "<html><body>" +
                    "<p>"+ recipient.get("nombres") +" "+ recipient.get("apellido_paterno") +" "+ recipient.get("apellido_materno") +",</p>" +
                    "<p>Le informamos que se ha revisado su reporte <strong>" + report.get("filename") + "</strong> version <strong>" + report.get("version") + "</strong>. El resultado de la evaluación para el reporte es: <strong>Aprobado</strong>. La revisión se realizó el " + report.get("updatedAt") + ".</p>" +
                    "<p>Puede revisar sus reportes subidos dando click en el siguiente botón:</p>" +
                    "<p><a href=\"" + "http://localhost:8080/alumno/reportes" + "\"><button style=\"background-color: #008CBA; color: white; padding: 12px 24px; border: none; border-radius: 4px; cursor: pointer;\">Ver reportes</button></a></p>" +
                    "<p>Saludos</p>" +
                    "<hr>" +
                    "<p><small>Este mensaje es informativo, para cualquier duda favor de presentarse en la oficina de .</small></p>" +
                    "</body></html>";
                }else{
                    body = "<html><body>" +
                    "<p>"+ recipient.get("nombres") +" "+ recipient.get("apellido_paterno") +" "+ recipient.get("apellido_materno") +",</p>" +
                    "<p>Le informamos que se ha revisado su reporte <strong>" + report.get("filename") + "</strong> version " + report.get("version") + ". El resultado de la evaluación para el reporte es: <strong>Rechazado</strong>. La revisión se realizó el " + report.get("updatedAt") + ".</p>" +
                    "<p>Su reporte cuenta con comentarios, para verlos debe acceder a la plataforma web dando click en el siguiente botón:</p>" +
                    "<p><a href=\"http://localhost:8080/alumno/reportes\"><button style=\"background-color: #008CBA; color: white; padding: 12px 24px; border: none; border-radius: 4px; cursor: pointer;\">Ver reportes</button></a></p>" +
                    "<p>Saludos</p>" +
                    "<hr>" +
                    "<p><small>Este mensaje es informativo, para cualquier duda favor de presentarse en la oficina de .</small></p>" +
                    "</body></html>";
                }

                Message message = new MimeMessage(session); // Crear el mensaje
                message.setFrom(new InternetAddress(from)); // Establecer el remitente del mensaje
                message.setRecipients( // Establecer los destinatarios del mensaje
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipient.get("correo_electronico").toString())
                );
                message.setSubject("Revisión de reporte de TT"); // Establecer el asunto del mensaje
                message.setContent(body, "text/html; charset=utf-8"); // Establecer el cuerpo del mensaje

                Transport.send(message); // Enviar el mensaje
            }

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
