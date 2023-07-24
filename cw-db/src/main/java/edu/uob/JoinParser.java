package edu.uob;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.SplittableRandom;

public class JoinParser extends Parser{



    public static String tableName1 = "";
    public static String tableName2 = "";
    public static String tableColumn1 = "";
    public static String tableColumn2 = "";
    public static int columnNum1 = 0;
    public static int columnNum2 = 0;
    ArrayList<ArrayList<String>> table1 = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> table2 = new ArrayList<ArrayList<String>>();

    public JoinParser(String currentDatabase, String currentTable, ArrayList<ArrayList<String>> table, ArrayList<ArrayList<String>> table2) {
        super(currentDatabase, currentTable, table, table2);
    }

    public void joinTable(ArrayList<String> tokens, Parser parser) throws IOException {
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] need something after JOIN";
            return;
        }
        //TODO 需要给别的判断么
        if (!isPlainText(tokens.get(0))){
            clientMessage = "[ERROR] table name is illegal";
            return;
        }
        if(!checkTableExist(currentDatabase,tokens.get(0))){
            clientMessage = "[ERROR] table is not exist";
            return;
        }
        tableName1 = tokens.get(0);
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after table name need AND ";
            return;
        }
        //AND
        if (!tokens.get(0).toUpperCase().equals("AND")) {
            clientMessage = "[ERROR] wrong syntax after table name";
            return;
        }
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after AND need something";
            return;
        }
        //TODO 需要给别的判断么
        if (!isPlainText(tokens.get(0))){
            clientMessage = "[ERROR] table name is illegal";
            return;
        }
        if(!checkTableExist(currentDatabase,tokens.get(0))){
            clientMessage = "[ERROR] table is not exist";
            return;
        }
        tableName2 = tokens.get(0);
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after table name need ON ";
            return;
        }
        //ON
        if (!tokens.get(0).toUpperCase().equals("ON")) {
            clientMessage = "[ERROR] wrong syntax after table name";
            return;
        }
        restJoin(tokens);
    }

    public void restJoin(ArrayList<String> tokens) throws IOException {
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after ON need something ";
            return;
        }
        if (!isAttributeName(tokens.get(0))){
            clientMessage = "[ERROR] illegal AttributeName";
            return;
        }
        tableColumn1 = tokens.get(0);
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after AttributeName need something ";
            return;
        }
        if (!tokens.get(0).toUpperCase().equals("AND")){
            clientMessage = "[ERROR] wrong syntax after attribute name";
            return;
        }
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after AND need something ";
            return;
        }
        if (!isAttributeName(tokens.get(0))){
            clientMessage = "[ERROR] illegal AttributeName";
            return;
        }
        tableColumn2 = tokens.get(0);
        if (checkLastToken(tokens)){
            if (!isAttributeName(tokens.get(0))){
                clientMessage = "[ERROR] illegal AttributeName";
            }else {
                //TODO add join
                tableColumn2 = tokens.get(0);
                joinQuery();
            }
        }else {
            clientMessage = "[ERROR] need ;";
        }
    }

    public void joinQuery() throws IOException {
        String suffixPath = ".tab";
        storageFolderPath = Paths.get("databases",currentDatabase,tableName1+suffixPath).toAbsolutePath().toString();
        BufferedReader br1 = new BufferedReader(Files.newBufferedReader(Paths.get(storageFolderPath)));
        String line1;
        while ((line1 = br1.readLine()) != null) {
            String[] columns = line1.split("\t");
            ArrayList<String> rowData = new ArrayList<>();
            for (String column : columns) {
                rowData.add(column);
            }
            table1.add(rowData);
        }
        br1.close();
        storageFolderPath = Paths.get("databases",currentDatabase,tableName2+suffixPath).toAbsolutePath().toString();
        BufferedReader br2 = new BufferedReader(Files.newBufferedReader(Paths.get(storageFolderPath)));
        String line2;
        while ((line2 = br2.readLine()) != null) {
            String[] columns = line2.split("\t");
            ArrayList<String> rowData = new ArrayList<>();
            for (String column : columns) {
                rowData.add(column);
            }
            table2.add(rowData);
        }
        br2.close();
        getJoin();
    }

    public void getJoin(){
        createJoinTable();
        System.out.println(table);
        System.out.println("148");
        for (int i = 0; i < table1.get(0).size(); i++) {
            if (table1.get(0).get(i).toLowerCase().equals(tableColumn1)) {
                columnNum1 = i;
                break;
            }
        }
        for (int i = 0; i < table2.get(0).size(); i++) {
            if (table2.get(0).get(i).toLowerCase().equals(tableColumn2)) {
                columnNum2 = i;
                break;
            }
        }

        int id = 0;
        for (int i = 0; i < table1.size(); i++) {
            ArrayList<String> tmp = new ArrayList<String>();
            int num = 0;
            tmp.add(num++,String.valueOf(id++));
            for (int j = 0; j < table2.size(); j++) {
                if (table1.get(i).get(columnNum1).equals(table2.get(j).get(columnNum2))){
                    for (int k = 0; k < table1.get(0).size(); k++) {
                        if (k == 0 || k == columnNum1) {
                            continue;
                        }
                        tmp.add(num++,table1.get(i).get(k));
                    }
                    for (int k = 0; k < table2.get(0).size(); k++) {
                        if (k == 0 || k == columnNum2) {
                            continue;
                        }
                        tmp.add(num++,table2.get(i).get(k));
                    }
                }
            }
            table.add(tmp);
        }
        table.remove(1);
        for (int i = 0; i < table.size(); i++) {
            System.out.println(table.get(i));
        }
        QueryTable queryTable = new QueryTable(currentDatabase,currentTable,table,table2);
        queryTable.printToClient();


    }

    public void createJoinTable(){
        table.clear();
        ArrayList<String> tmp = new ArrayList<String>();
        tmp.add(0,"id");
        int num = 1;
        for (int i = 0; i < table1.get(0).size(); i++) {
            if (table1.get(0).get(i).equals(tableColumn1) || table1.get(0).get(i).toLowerCase().equals("id")){
                continue;
            }
            tmp.add(num++,tableName1 + "." + table1.get(0).get(i));
        }

        for (int i = 0; i < table2.get(0).size(); i++) {
            if (table2.get(0).get(i).equals(tableColumn2) || table2.get(0).get(i).toLowerCase().equals("id")){
                continue;
            }
            tmp.add(num++,tableName2 + "." + table2.get(0).get(i));
        }
        table.add(tmp);
    }
}
