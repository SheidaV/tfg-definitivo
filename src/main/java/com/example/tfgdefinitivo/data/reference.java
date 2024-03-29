package com.example.tfgdefinitivo.data;

import com.example.tfgdefinitivo.config.DBConnection;
import com.example.tfgdefinitivo.domain.dto.*;
import org.jbibtex.ParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class reference {

    public static int insertRow(Statement s, String doi, String idDL, String estado, String apCriteria)
            throws SQLException {
        try {
            String query;
            if (estado!=null && apCriteria != null)
                query = "INSERT INTO referencias(doi,idDL,state,applCriteria) VALUES (\'" + doi
                        + "\', " + idDL + ",'" + estado+"', '"+apCriteria+"')";
            else if (estado!=null)
                query = "INSERT INTO referencias(doi,idDL,state,applCriteria) VALUES (\'" + doi
                    + "\', " + idDL + ",'" + estado+"', null)";
            else if (apCriteria!=null)
                query = "INSERT INTO referencias(doi,idDL,state,applCriteria) VALUES (\'" + doi
                        + "\', " + idDL + ",null,'" + apCriteria+"')";
            else query = "INSERT INTO referencias(doi,idDL,state,applCriteria) VALUES (\'" + doi
                    + "\', " + idDL + ", "+estado+ ", "+apCriteria+")";
            //System.out.println(query);
            s.execute(query);
            System.out.println("Inserted row with doi, idDL.. in referencias");
            s.getConnection().commit();
        } catch (SQLException e) {
            if(e.getSQLState().equals(23505)){
                return -1;
            }
            while (e != null) {
                System.err.println("\n----- SQLException -----");
                System.err.println("  SQL State:  " + e.getSQLState());
                System.err.println("  Error Code: " + e.getErrorCode());
                System.err.println("  Message:    " + e.getMessage());
                e = e.getNextException();
            }
        }
        ResultSet rs = s.executeQuery("SELECT idRef FROM referencias where doi = '" + doi + "' and idDL =" + idDL);
        rs.next();
        return rs.getInt(1);
    }

    public static void createTable(Statement s) {
        try {
            s.execute("create table referencias(idRef INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, " +
                    "INCREMENT BY 1), doi varchar(50), idDL int, state VARCHAR(10), applCriteria varchar(5), " +
                    "PRIMARY KEY(idRef), unique(doi,idDL), CONSTRAINT DL_FK_R FOREIGN KEY (idDL) " +
                    "REFERENCES digitalLibraries (idDL),CONSTRAINT AR_FK_R FOREIGN KEY (doi) REFERENCES articles (doi),"+
                    "CONSTRAINT state_chk CHECK (state IN ( 'pending', 'in', 'duplicated','out')), " +
                    "CONSTRAINT CRI_FK_R FOREIGN KEY (applCriteria) REFERENCES criteria(idICEC) on delete set null)");
            System.out.println("Created table referencias");
        } catch (SQLException e) {
            if (e.getSQLState().equals("X0Y32"))
                System.out.println("Table referencias exists");
            else if (e.getMessage().contains("primary key"))
                System.out.println("Referencia ya importada");
            else {
                while (e != null) {
                    System.err.println("\n----- SQLException -----");
                    System.err.println("  SQL State:  " + e.getSQLState());
                    System.err.println("  Error Code: " + e.getErrorCode());
                    System.err.println("  Message:    " + e.getMessage());
                    e = e.getNextException();
                }
            }
        }
    }

    public static void dropTable(Statement s) throws SQLException {
        try{
        s.execute("drop table referencias");
        System.out.println("Dropped table referencias");
        }
        catch (SQLException sqlException) {
            System.out.println("Tabla referencias not exist");
        }
    }

    public static ResultSet getAll(Statement s) throws SQLException {
        ResultSet rs;
        rs = s.executeQuery("SELECT * FROM referencias ");
        return rs;
    }

    public static List<referenceDTO> getAllReferences() {
        List<referenceDTO> refList = null;
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DBConnection.class);
        Connection conn = ctx.getBean(Connection.class);
        Statement s;
        try {
            conn.setAutoCommit(false);
            s = conn.createStatement();
            ResultSet rs = getAll(s);
            int number = 1;
            refList = new ArrayList<referenceDTO>();
            while(rs.next()) {
                System.out.println(number++);

                int idR = rs.getInt(1);
                String doiR = rs.getString(2);
                int dlR = rs.getInt(3);
                String estado = rs.getString(4);
                String applCriteria = rs.getString(5);
                //System.out.println(idR + " " + doiR + " " + dlR + " "+ estado + " " + applCriteria);

                referenceDTO NewRef = new referenceDTO(idR,doiR,dlR,estado,applCriteria);
                obtainReferenceDTO(conn, NewRef, doiR, dlR);

                refList.add(NewRef);
            }
            conn.commit();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return refList;
    }

    public static void create() {
        Connection conn;
        ArrayList<Statement> statements = new ArrayList<Statement>(); // list of Statements, PreparedStatements
        Statement s = null;
        try {
            ApplicationContext ctx = new AnnotationConfigApplicationContext(DBConnection.class);
            conn = ctx.getBean(Connection.class);
            conn.setAutoCommit(false);

            // Statement object for running various SQL statements commands against the database.
            s = conn.createStatement();
            crearTablas(s,conn,statements);
            //deleteTables(s,conn,statements);

            conn.commit();
            System.out.println("Committed the transaction");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally{
            if(s!=null) {
                try {
                    s.close();
                } catch (SQLException ex) {
                    System.out.println("Could not close query");
                }
            }
        }
    }

    public static void delete() {
        Connection conn;
        ArrayList<Statement> statements = new ArrayList<Statement>(); // list of Statements, PreparedStatements
        Statement s = null;
        try {
            ApplicationContext ctx = new AnnotationConfigApplicationContext(DBConnection.class);
            conn = ctx.getBean(Connection.class);
            conn.setAutoCommit(false);

            // Statement object for running various SQL statements commands against the database.
            s = conn.createStatement();
            deleteTables(s,conn);
            conn.commit();

            System.out.println("Committed the transaction");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally{
            if(s!=null) {
                try {
                    s.close();
                } catch (SQLException ex) {
                    System.out.println("Could not close query");
                }
            }
        }
    }

    private static void crearTablas(Statement s, Connection conn, ArrayList<Statement> statements) throws SQLException {
        // Create table digitalLibraries if not exist
        if (digitalLibrary.createTable(s))
            //insert rows in table
            digitalLibrary.insertRows(conn, statements);
        venue.createTable(s);
        article.createTable(s);
        researcher.createTable(s);
        if (criteria.createTable(s))
            //insert duplicate exclusion
            criteria.insertRowIni(conn,statements);
        reference.createTable(s);
        author.createTable(s);
        company.createTable(s);
        affiliation.createTable(s);
        importationLogError.createTable(s);
    }

    private static void deleteTables(Statement s, Connection conn) throws SQLException {
        importationLogError.dropTable(s);
        affiliation.dropTable(s);
        company.dropTable(s);
        author.dropTable(s);
        reference.dropTable(s);
        researcher.dropTable(s);
        article.dropTable(s);
        venue.dropTable(s);
        criteria.dropTable(s);
        digitalLibrary.dropTable(s);
    }

    public static List<importErrorDTO> importar( String idDL, MultipartFile file) throws SQLException, IOException, ParseException {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DBConnection.class);
        Connection conn = ctx.getBean(Connection.class);
        conn.setAutoCommit(false);
        Statement s = conn.createStatement();
        Timestamp t = article.importar( idDL, s, file);
        List<importErrorDTO> r =  importationLogError.getErrors(s,t);
        conn.commit();
        return r;
    }

    public static List<importErrorDTO> getAllErrors() throws SQLException, IOException, ParseException {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DBConnection.class);
        Connection conn = ctx.getBean(Connection.class);
        conn.setAutoCommit(false);
        Statement s = conn.createStatement();
        List<importErrorDTO> r = importationLogError.getAllErrors(s);
        conn.commit();
        return r;
    }

    public static referenceDTO getReference(int idR) {
        referenceDTO r = null;

        Connection conn;
        Statement s;
        try {
            ApplicationContext ctx = new AnnotationConfigApplicationContext(DBConnection.class);
            conn = ctx.getBean(Connection.class);
            s = conn.createStatement();
            referenceDTO NewRef = find(idR,s);

            String doiR = NewRef.getDoi();
            int dlR = NewRef.getidDL();
            String estado = NewRef.getEstado();
            //System.out.println(idR + " " + doiR + " " + dlR + " " + estado);

            obtainReferenceDTO(conn, NewRef, doiR,dlR);
            r=NewRef;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return r;
    }

    private static void obtainReferenceDTO(Connection conn, referenceDTO newRef, String doiR, int dlR) throws SQLException {
        Statement s2 = conn.createStatement();
        ResultSet rsDL = digitalLibrary.getdigitalLibrary(s2,dlR);
        digitalLibraryDTO dl = null;

        if(rsDL.next())
            dl = new digitalLibraryDTO(rsDL.getInt(1),rsDL.getString(2),
                    rsDL.getString(3),rsDL.getInt(4));
        newRef.setDl(dl);

        Statement s3 = conn.createStatement();
        ResultSet rsAr = article.getArticle(s3,doiR);
        articleDTO ar = null;
        if(rsAr.next()) {
            ar = new articleDTO(rsAr.getString(1), rsAr.getString(2),
                    rsAr.getString(3), rsAr.getInt(4), rsAr.getString(5),
                    rsAr.getString(6), rsAr.getString(7),
                    rsAr.getInt(8), rsAr.getString(9), rsAr.getString(10),
                    rsAr.getInt(11), rsAr.getString(12));
            rsAr = venue.getVenue(s3,rsAr.getInt(4));
            venueDTO v = null;
            if (rsAr.next())
                v = new venueDTO(rsAr.getInt(1), rsAr.getString(2), rsAr.getString(3));
            ar.setVen(v);

            rsAr = company.getCompanies(s3,doiR);
            List<companyDTO> c = new ArrayList<companyDTO>();
            companyDTO auxC = null;
            while (rsAr.next()) {
                auxC = new companyDTO(rsAr.getInt(1), rsAr.getString(2));
                c.add(auxC);
            }
            ar.setCompanies(c);

            rsAr = researcher.getResearchers(s3,doiR);
            List<researcherDTO> rss = new ArrayList<researcherDTO>();
            researcherDTO auxR = null;
            while (rsAr.next()) {
                auxR = new researcherDTO(rsAr.getInt(1), rsAr.getString(2));
                rss.add(auxR);
            }
            ar.setResearchers(rss);
        }
        newRef.setArt(ar);
    }

    private static referenceDTO find(int idR, Statement s) throws SQLException {
        ResultSet rs;
        rs = s.executeQuery("SELECT * FROM referencias where idRef=" + idR);
        rs.next();
        referenceDTO r = new referenceDTO(rs.getInt(1),rs.getString(2),rs.getInt(3),
                rs.getString(4), rs.getString(5));
        return r;
    }

    public static int getDL(String doi,Statement s) throws SQLException {
        ResultSet r = s.executeQuery("select idDL from referencias where doi = '" + doi + "'");
        r.next();
        return r.getInt(1);
    }
    static ResultSet isDuplicate(Statement s, int priorityImportado, String doi) throws SQLException {
        //exists (select * from references r, digitalLibraries dl
        //	where r.doi=varDoi and r.idDL = dl.idDL and dl.priority > varPriorityDLimportando)
        return s.executeQuery("select * from REFERENCIAS r, DIGITALLIBRARIES dl where r.doi='" + doi
                + "' and r.idDL = dl.idDL and dl.priority <= "+ priorityImportado);
    }
    static void updateEstateReferences(Statement s, String doi) throws SQLException {
        s.execute("update referencias set state = 'out', applCriteria='EC1' where doi = '" + doi + "' and state is null ");
    }

    public static void update(int idRef, String estado, String applCriteria) {
        try{
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DBConnection.class);
        Connection conn = ctx.getBean(Connection.class);
        Statement s = conn.createStatement();
        if (!estado.equals("") && !applCriteria.equals("")) {
            s.execute("update referencias set state = '" + estado + "' , applCriteria = '" + applCriteria +
                    "' where idRef = " + idRef );
        }
        else if (!estado.equals("")) s.execute("update referencias set state = '" + estado + "' where idRef = " + idRef );
        else if (!applCriteria.equals("")) s.execute("update referencias set applCriteria = '" + applCriteria + "' where idRef = " + idRef);
        } catch (SQLException e) {
            System.out.println("Error en update estado y criteria de una reference");
            while (e != null) {
                System.err.println("\n----- SQLException -----");
                System.err.println("  SQL State:  " + e.getSQLState());
                System.err.println("  Error Code: " + e.getErrorCode());
                System.err.println("  Message:    " + e.getMessage());
                e = e.getNextException();
            }
        }
    }
    public static List<Integer> getReferenceOfCriteria(String oldIdICEC) {
        try {
            ApplicationContext ctx = new AnnotationConfigApplicationContext(DBConnection.class);
            Connection conn = ctx.getBean(Connection.class);
            Statement s;
            s = conn.createStatement();
            ResultSet rs;
            List<Integer> r = new ArrayList<Integer>();
            rs = s.executeQuery("SELECT idRef FROM referencias where applcriteria='" + oldIdICEC + "'");
            while (rs.next()) r.add(rs.getInt(1));
            s.close();
            return r;
        }
        catch (SQLException e) {
            while (e != null) {
                System.err.println("\n----- SQLException -----");
                System.err.println("  SQL State:  " + e.getSQLState());
                System.err.println("  Error Code: " + e.getErrorCode());
                System.err.println("  Message:    " + e.getMessage());
                e = e.getNextException();
            }
        }
        return new ArrayList<Integer>();
    }

    public static void setNullCriteria(int idRef) {
        try{
            ApplicationContext ctx = new AnnotationConfigApplicationContext(DBConnection.class);
            Connection conn = ctx.getBean(Connection.class);
            Statement s;
            String query = "UPDATE referencias SET applcriteria=null WHERE idRef = " + idRef;
            //System.out.println(query);
            s = conn.createStatement();
            s.execute(query);
            System.out.println("Inserted row in criteria");
            s.close();
        }catch (SQLException e) {
            while (e != null) {
                System.err.println("\n----- SQLException -----");
                System.err.println("  SQL State:  " + e.getSQLState());
                System.err.println("  Error Code: " + e.getErrorCode());
                System.err.println("  Message:    " + e.getMessage());
                e = e.getNextException();
            }
        }
    }

    public static void setCriteria(int idRef, String idICEC) {
        try{
            ApplicationContext ctx = new AnnotationConfigApplicationContext(DBConnection.class);
            Connection conn = ctx.getBean(Connection.class);
            Statement s;
            String query = "UPDATE referencias SET applcriteria='"+ idICEC +"' WHERE idRef = " + idRef;
            //System.out.println(query);
            s = conn.createStatement();
            s.execute(query);
            System.out.println("Inserted row in criteria");
            s.close();
        }catch (SQLException e) {
            while (e != null) {
                System.err.println("\n----- SQLException -----");
                System.err.println("  SQL State:  " + e.getSQLState());
                System.err.println("  Error Code: " + e.getErrorCode());
                System.err.println("  Message:    " + e.getMessage());
                e = e.getNextException();
            }
        }
    }
}
