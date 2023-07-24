package edu.uob;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;

public class AlterParser extends Parser{


    public AlterParser(String currentDatabase, String currentTable, ArrayList<ArrayList<String>> table, ArrayList<ArrayList<String>> table2) {
        super(currentDatabase, currentTable, table, table2);
    }

    //"ALTER TABLE " [TableName] " " [AlterationType] " " [AttributeName]
    public void alterDatabase(ArrayList<String> tokens) throws IOException {
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] need something after alter";
            return;
        }
        if (!tokens.get(0).toUpperCase().equals("TABLE")) {
            clientMessage = "[ERROR] wrong syntax after ALTER";
            return;
        }
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after TABLE need something";
            return;
        }
        //[TableName]
        //FIXME only have to check table exist? because when create table it should already check the name first
        if(!checkTableExist(currentDatabase, tokens.get(0))){
            clientMessage = "[ERROR] table is not exist";
            return;
        }
        currentTable = tokens.get(0);
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after tablename need an exact type";
            return;
        }
        //[AlterationType]
        //"ADD" | "DROP"
        if(tokens.get(0).toUpperCase().equals("ADD")){
            //[AttributeName]
            super.getRestToken(tokens);
            if(tokens.size() == 0){
                clientMessage = "[ERROR] after add need a name";
                return;
            }
            if(isAttributeName(tokens.get(0))){
                if(checkLastToken(tokens)){
                    //TODO add alter function
                    alterAddQuery(tokens.get(0));
                    clientMessage = "[OK] alter add successfully";
                }else {
                    clientMessage = "[ERROR] last is not ;";
                }
            }else {
                clientMessage = "[ERROR] illegal attribute name";
            }

        } else if (tokens.get(0).toUpperCase().equals("DROP")) {
            //[AttributeName]
            super.getRestToken(tokens);
            if(tokens.size() == 0){
                clientMessage = "[ERROR] after drop need a name";
                return;
            }
            if(isAttributeName(tokens.get(0))){
                if(checkLastToken(tokens)){
                    alterDropQuery(tokens.get(0));
                }else {
                    clientMessage = "[ERROR] last is not ;";
                }
            }else {
                clientMessage = "[ERROR] illegal attribute name";
            }
        }
    }

    public void alterAddQuery(String tokens) throws IOException {
        QueryTable queryTable = readFile1(currentDatabase,currentTable,table);
        queryTable.addColSingle(tokens);
        if (table.size() != 1){
            for (int i = 1; i < table.size(); i++) {
                table.get(i).add("null");
            }
        }
//        FileWriter myWriter = new FileWriter(storageFolderPath);
        BufferedWriter myWriter = Files.newBufferedWriter(Paths.get(storageFolderPath));
        for (int i = 0; i < table.size(); i++) {
            for (int j = 0; j < table.get(0).size(); j++) {
                if (table.get(i).get(j).equals("\t")){
                    myWriter.write("\t");
                }else {
                    myWriter.write(table.get(i).get(j));
                    myWriter.write("\t");
                }
            }
            if (i < table.size() - 1){
                myWriter.write("\n");
            }
        }
        myWriter.close();
        queryTable.printTable();
        table.clear();
    }

    public void alterDropQuery(String tokens) throws IOException {
        QueryTable queryTable = readFile1(currentDatabase, currentTable,table);
        if (tokens.equals("ID")) {
            clientMessage = "[ERROR] cannot remove ID column";
        } else {
            if (queryTable.removeCol(tokens)) {
                BufferedWriter myWriter = Files.newBufferedWriter(Paths.get(storageFolderPath));
                for (int i = 0; i < table.size(); i++) {
                    for (int j = 0; j < table.get(0).size(); j++) {
                        if (table.get(i).get(j).equals("\t")) {
                            myWriter.write("");
                            myWriter.write("\t");
                        } else {
                            myWriter.write(table.get(i).get(j));
                            myWriter.write("\t");
                        }
                    }
                    if (i < table.size() - 1) {
                        myWriter.write("\n");
                    }
                }
                myWriter.close();
                queryTable.printTable();
                table.clear();
                clientMessage = "[OK] alter drop successfully";
            } else {
                clientMessage = "[ERROR] cannnot find this coloum";
            }
        }
    }
}
