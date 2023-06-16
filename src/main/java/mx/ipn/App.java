package mx.ipn;

import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UnwindOptions;
import com.mongodb.client.MongoCollection;

import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.collections4.IterableUtils;
import org.bson.Document;
import org.bson.conversions.Bson;


public class App {

    MongoClient mongoClient;

    public App(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public List<Document> getReports() {
        List<Document> statusReports = new ArrayList<>();

        try {
            // Enviar un ping para confirmar la conexion exitosa
            MongoDatabase database = mongoClient.getDatabase("test");
            database.runCommand(new Document("ping", 1));
            System.out.println("Pinged your deployment. You successfully connected to MongoDB!");

            MongoCollection<Document> collection = database.getCollection("reports");

            List<Bson> pipeline = Arrays.asList(
                Aggregates.lookup("tts", "_id", "reportes", "tt"),
                Aggregates.unwind("$tt", new UnwindOptions().preserveNullAndEmptyArrays(true)),
                Aggregates.project(Projections.fields(
                    Projections.include("filename", "version", "aprobado", "comentarios", "createdAt", "tt.numero_tt"),
                    Projections.computed("numero_tt", "$tt.numero_tt")
                ))
            );

            AggregateIterable<Document> results = collection.aggregate(pipeline);

            for (Document report : results) {
                statusReports.add(report);
            }

            return statusReports;

        } catch (MongoException e) {
            e.printStackTrace();
        }

        return null;
    }

    public FindIterable<Document> getReportsByFileName(String fileName) {
        try {
            // Enviar un ping para confirmar la conexion exitosa
            MongoDatabase database = mongoClient.getDatabase("test");
            database.runCommand(new Document("ping", 1));
            System.out.println("Pinged your deployment. You successfully connected to MongoDB!");

            MongoCollection<Document> collection = database.getCollection("reports");

            // Query for the PDF file by filename
            Document query = new Document("filename", fileName);
            FindIterable<Document> reports = collection.find(query);

            return reports;

        } catch (MongoException e) {
                e.printStackTrace();
        }

        return null;
    }

    public List<Document> getReportsByTT(String numero_tt){
        try{
            MongoCollection<Document> collection = mongoClient.getDatabase("test").getCollection("tts");
            // query regex para buscar los TTs que contengan la busqueda
            Document query = new Document("numero_tt", new Document("$regex", numero_tt));
            FindIterable<Document> tts = collection.find(query);

            List<Document> modifiedReports = new ArrayList<>();

            // Iterar sobre los tts para obtener los reportes de cada TT que concuerde con la busqueda
            Iterable<Document> reports = new ArrayList<Document>();
            for (Document tt : tts) {
                ArrayList<String> reports_ids = (ArrayList<String>) tt.get("reportes");

                // Si no hay reportes, continuar con el siguiente TT
                if (reports_ids == null || reports_ids.isEmpty()) {
                    continue;
                }

                // Buscar los reportes que coincidan con los ids de los reportes del TT actual
                MongoCollection<Document> collection2 = mongoClient.getDatabase("test").getCollection("reports");
                Document query2 = new Document("_id", new Document("$in", reports_ids));
                FindIterable<Document> reportsCurrent = collection2.find(query2);
                reports = IterableUtils.chainedIterable(reports, reportsCurrent);

                for (Document report : reportsCurrent) {
                    report.put("numero_tt", tt.get("numero_tt"));
                    modifiedReports.add(report);
                }
            }
            return modifiedReports;
        } catch (MongoException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Metodo para obtener los reportes por status de aprobacion
    public List<Document> getReportsByStatus(String status){
        List<Document> statusReports = new ArrayList<>();
        try{
            MongoCollection<Document> reportsCollection = mongoClient.getDatabase("test").getCollection("reports");

            List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.regex("aprobado", status)),
                Aggregates.lookup("tts", "_id", "reportes", "tt"),
                Aggregates.unwind("$tt", new UnwindOptions().preserveNullAndEmptyArrays(true)),
                Aggregates.project(Projections.fields(
                    Projections.include("filename", "version", "aprobado", "comentarios", "createdAt", "tt.numero_tt"),
                    Projections.computed("numero_tt", "$tt.numero_tt")
                ))
            );

            AggregateIterable<Document> results = reportsCollection.aggregate(pipeline);

            for (Document report : results) {
                statusReports.add(report);
            }

            return statusReports;
        } catch (MongoException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Document getReportToOpen(String filename, String version){
        try{
            MongoCollection<Document> collection = mongoClient.getDatabase("test").getCollection("reports");
            Document query = new Document("filename", filename).append("version", Integer.parseInt(version));
            FindIterable<Document> reports = collection.find(query);
            return reports.first();
        } catch (MongoException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean evaluateReport(String filename, String version, String numero_tt, String status, String comentarios){
        try{
            MongoCollection<Document> collection = mongoClient.getDatabase("test").getCollection("tts");
            Document query = new Document("numero_tt", numero_tt);
            FindIterable<Document> tts = collection.find(query);
            Document tt = tts.first();
            // obtener el reporte seleccionado de la lista
            ArrayList<String> reports_ids = (ArrayList<String>) tt.get("reportes");
            MongoCollection<Document> collection2 = mongoClient.getDatabase("test").getCollection("reports");
            Document query2 = new Document("_id", new Document("$in", reports_ids));
            FindIterable<Document> reports = collection2.find(query2);

            Document reporte = null;
            for(Document report : reports){
                if(report.get("filename").equals(filename) && report.get("version").equals(Integer.parseInt(version))){
                    reporte = report;
                    break;
                }
            }

            // actualizar el status del reporte
            reporte.put("aprobado", status);
            reporte.put("comentarios", comentarios);
            collection2.replaceOne(Filters.eq("_id", reporte.get("_id")), reporte);
            System.out.println("Reporte evaluado");


            // obtener los usuarios que estan suscritos al TT
            MongoCollection<Document> collection3 = mongoClient.getDatabase("test").getCollection("users");
            Document query3 = new Document("tt", tt.get("_id"));
            FindIterable<Document> users = collection3.find(query3);

            ArrayList<Document> usersList = new ArrayList<>();
            for(Document user : users){
                usersList.add(user);
            }

            Notificacion notificacion = new Notificacion();
            notificacion.sendNotification(usersList, reporte);

            return true;
        }catch (MongoException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Document> getUsers(){
        try {
            MongoCollection<Document> usersCollection = mongoClient.getDatabase("test").getCollection("users");

            List<Bson> pipeline = Arrays.asList(
                Aggregates.lookup("tts", "tt", "_id", "tt"),
                Aggregates.unwind("$tt", new UnwindOptions().preserveNullAndEmptyArrays(true)),
                Aggregates.project(Projections.fields(
                    Projections.include("nombres", "apellido_paterno", "apellido_materno", "correo_electronico", "boleta", "tt"),
                    Projections.computed("tt", new Document("$ifNull", Arrays.asList("$tt.numero_tt", "No asignado")))
                ))
            );

            AggregateIterable<Document> results = usersCollection.aggregate(pipeline);

            List<Document> modifiedUsers = new ArrayList<>();
            for (Document user : results) {
                modifiedUsers.add(user);
            }
            return modifiedUsers;
        } catch (MongoException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteUser(String correo_electronico){
        try {
            MongoCollection<Document> collection = mongoClient.getDatabase("test").getCollection("users");
            Document query = new Document("correo_electronico", correo_electronico);
            collection.deleteOne(query);
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }
}
