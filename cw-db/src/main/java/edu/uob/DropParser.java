package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DropParser extends Parser{

    private String storageFolderPath;

    public DropParser(String currentDatabase, String currentTable, ArrayList<ArrayList<String>> table, ArrayList<ArrayList<String>> table2) {
        super(currentDatabase, currentTable, table, table2);
    }


    public void judgeDrop(ArrayList<String> tokens, Parser parser){
        super.getRestToken(tokens);
        if (tokens.size() != 0) {
            switch (tokens.get(0).toUpperCase()){
                case ("DATABASE"):
                    super.getRestToken(tokens);
                    dropDatabase(tokens,parser);
                    break;
                case ("TABLE"):
                    super.getRestToken(tokens);
                    dropTable(tokens, parser);
                    break;
                default:
                    clientMessage = "[ERROR] wrong drop type!";
                    break;
            }
        }else {
            //FIXME show the information on the client not in the server! how?
            clientMessage = "[ERROR] wrong syntax";
        }
    }

    public void dropDatabase(ArrayList<String> tokens,Parser parser){
        if (tokens.size() != 0) {
            if (isPlainText(tokens.get(0))) {
                String database = tokens.get(0);
                getRestToken(tokens);
                if(checkLastToken(tokens)){
                    //FIXME drop database
                    databaseToDrop(database,parser);
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

    public void dropTable(ArrayList<String> tokens, Parser parser){
        //"DROP TABLE " [TableName]
        //name is true?
        if (tokens.size() != 0) {
            System.out.println("table name is:" + tokens.get(0));
            String table = tokens.get(0);
            currentTable = tokens.get(0);
            if (isPlainText(tokens.get(0))) {
                getRestToken(tokens);
                if(checkLastToken(tokens)){
                    //FIXME drop table
                    tableToDrop(currentDatabase,table, parser);
                }else {
                    clientMessage = "[ERROR] last token is not ';' or not only ';' ";
                }
            }else {
                clientMessage = "[ERROR] name is null";
            }
        }else{
            clientMessage = "[ERROR] after table is null";
        }
    }

    public void databaseToDrop(String databaseName, Parser parser){
        storageFolderPath = Paths.get("databases", databaseName).toAbsolutePath().toString();
        // Create the database storage folder if it doesn't already exist !
        File databaseDrop = new File(storageFolderPath);
        if (databaseDrop.isDirectory()) {
            File[] files = databaseDrop.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            databaseDrop.delete();
            if(databaseName.equals(parser.currentDatabase)){
                parser.currentDatabase = null;
            }
            clientMessage = "[OK] database drop successfully";
        }else {
            clientMessage = "[ERROR] "  + "Failed to delete " + databaseName;
        }
    }

    public void tableToDrop(String databaseName,String tableName, Parser parser){
        String suffixPath = ".tab";
        if(tableName == null || databaseName == null){
            clientMessage = "[ERROR] cannot find this table";
        }else {
            storageFolderPath = Paths.get("databases", databaseName,tableName+suffixPath).toAbsolutePath().toString();
            // Create the database storage folder if it doesn't already exist !
            File tableDrop = new File(storageFolderPath);
            if (tableDrop.exists() && checkTableExist(tableName,tableName)) {
                tableDrop.delete();
                //FIXME fix drop when already in select table ????????
                clientMessage = "[OK] table drop successfully";
            }else {
                clientMessage = "[ERROR] "  + "Failed to delete " + tableName;
            }
        }

    }
}
