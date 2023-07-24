package edu.uob;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CreateParser extends Parser{

//    CreateParser createParser = new CreateParser();
   // Parser ceateParser = new Parser();
//    Tokeniser tokeniser = new Tokeniser();


    public String tableName = "";
    private String storageFolderPath;

    public CreateParser(String currentDatabase, String currentTable, ArrayList<ArrayList<String>> table, ArrayList<ArrayList<String>> table2) {
        super(currentDatabase, currentTable, table, table2);
    }


    public void judgeCreate(ArrayList<String> tokens) throws IOException {
        super.getRestToken(tokens);
        if (tokens.size() != 0) {
            switch (tokens.get(0).toUpperCase()){
                case ("DATABASE"):
                    System.out.println("create in database");
                    super.getRestToken(tokens);
                    createDatabase(tokens);
                    break;
                case ("TABLE"):
                    System.out.println("create in table");
                    super.getRestToken(tokens);
                    createTable(tokens,this);
                    break;
                default:
                    clientMessage = "[ERROR] wrong create type!";
                    System.out.println("wrong create type!");
                    break;
            }
        }else {
            //FIXME show the information on the client not in the server! how?
            clientMessage = "[ERROR] wrong syntax";
            System.out.println("wrong syntax");
        }

    }

    public void createDatabase(ArrayList<String> tokens){
        //"CREATE DATABASE " [DatabaseName]
        if (tokens.size() != 0) {
            super.currentDatabase = tokens.get(0);
            if (isPlainText(tokens.get(0))) {
                getRestToken(tokens);
                if(checkLastToken(tokens)){
                    clientMessage = "[OK] create successfully";
                    setDatabase(super.currentDatabase);
                    //FIXME add database
                }else {
                    clientMessage = "[ERROR] last token is not ';' or not only ';' ";
                }
            }else {
                clientMessage = "[ERROR] name is null";
            }
        }else{
            clientMessage = "[ERROR] after database is null";
        }
    }

    public void createTable(ArrayList<String> tokens,Parser parser) throws IOException {
        //"CREATE TABLE " [TableName]
        if (tokens.size() != 0) {
            tableName = tokens.get(0).toLowerCase();
            if (isPlainText(tokens.get(0))) {//name is correct?
                getRestToken(tokens);
                int size = tokens.size();
                if(checkLastToken(tokens)){
                    if(tokens.get(0).charAt(0) =='('&& tokens.get(size - 2).charAt(0) == ')'){
                        //TODO add table with () "CREATE TABLE " [TableName] "(" <AttributeList> ")"
                        ArrayList<String > tmp = new ArrayList<String>();
                        ArrayList<String > splitTokens = new ArrayList<String>();
                        int j = 0;
                        for (int i = 1; i < tokens.size() - 2; i++) {
                            tmp.add(j++,tokens.get(i++));
                        }
                        if (isAttributeList(tmp, splitTokens)) {
                            setTableWithBracket(parser.currentDatabase ,tableName,splitTokens);
                        }
                    }else if (tokens.get(0).charAt(0) != '(' && tokens.size() == 1){
                        //TODO add table without () "CREATE TABLE " [TableName]
                        setTable(parser.currentDatabase,tableName);
                    }else {
                        clientMessage = "[ERROR] wrong syntax";
                    }
                }else {
                    clientMessage = "[ERROR] last token is not ';' or not only ';' ";
                }
            }else {
                clientMessage = "[ERROR] name is null or false";
            }
        }else{
            clientMessage = "[ERROR] after table is null";
        }
    }

    public void setDatabase(String databaseName){
        storageFolderPath = Paths.get("databases", databaseName).toAbsolutePath().toString();
            // Create the database storage folder if it doesn't already exist !
        File database = new File(storageFolderPath);
        if (!database.exists()){
            database.mkdirs();
            clientMessage = "[OK] database: " + databaseName + " created successfully";
        }else {
            clientMessage = "[ERROR] " + databaseName + " already exist";
        }
    }

    public void setTable(String currentDatabase,String tableName) throws IOException {
        String suffixPath = ".tab";
        System.out.println(currentDatabase);
        if (currentDatabase == null) {
            clientMessage = "[ERROR] need to choose a database";
        } else {
            storageFolderPath = Paths.get("databases",currentDatabase,tableName+suffixPath).toAbsolutePath().toString();
            File table = new File(storageFolderPath);
            if (!table.exists()){
//                try {
                    table.createNewFile();
                    clientMessage = "[OK] " + tableName + " create successfully";
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
            }else {
                clientMessage = "[ERROR] " + tableName + " already exist";
            }
        }

    }

    public void setTableWithBracket(String currentDatabase, String tableName, ArrayList<String > splitTokens) throws IOException {
        String suffixPath = ".tab";
        if (currentDatabase == null) {
            clientMessage = "[ERROR] need to choose a database";
        } else {
            storageFolderPath = Paths.get("databases",currentDatabase,tableName+suffixPath).toAbsolutePath().toString();
            File table = new File(storageFolderPath);
            if (!table.exists()){
                    table.createNewFile();
                    StringBuilder sb = new StringBuilder(0);
                    sb.append("ID").append("\t");
                    for (String l : splitTokens){
                        sb.append(l) .append("\t");
                    }
//                    FileWriter writer = new FileWriter(storageFolderPath);
                    BufferedWriter writer = Files.newBufferedWriter(Paths.get(storageFolderPath));
                    writer.write(sb.toString());
                    writer.close();
                    clientMessage = "[OK] " + tableName + " create successfully";
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
            }else {
                clientMessage = "[ERROR] " + tableName + " already exist";
            }
        }
    }

}
