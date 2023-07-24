package edu.uob;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class InsertParser extends Parser {


    public InsertParser(String currentDatabase, String currentTable, ArrayList<ArrayList<String>> table, ArrayList<ArrayList<String>> table2) {
        super(currentDatabase, currentTable, table, table2);
    }

    public void insertTable(ArrayList<String> tokens, Parser parser) throws IOException {
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] need something after INSERT";
            return;
        }
        if (!tokens.get(0).toUpperCase().equals("INTO")) {
            clientMessage = "[ERROR] wrong syntax after INSERT";
            return;
        }
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after INTO need something";
            return;
        }
        //[TableName]
        if(!checkTableExist(currentDatabase,tokens.get(0))){
            clientMessage = "[ERROR] table is not exist";
            return;
        }
        currentTable = tokens.get(0);
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after tablename need VALUES";
            return;
        }
        //VALUES
        if (!tokens.get(0).toUpperCase().equals("VALUES")) {
            clientMessage = "[ERROR] wrong syntax after tablename";
            return;
        }
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after VALUES need something";
            return;
        }
        if(checkLastToken(tokens)){
            int size = tokens.size();
            if(tokens.get(0).charAt(0) =='('&& tokens.get(size - 2).charAt(0) == ')'){
                ArrayList<String > tmp = new ArrayList<String>();
                ArrayList<String > splitTokens = new ArrayList<String>();
                int j = 0;
                for (int i = 1; i < tokens.size() - 2; i++) {
                    tmp.add(j++,tokens.get(i++));
                }
                if (isValueList(tmp, splitTokens)) {
                    deleteQuotation(tmp);
                    insertQuery(tmp);
                }
            }else {
                clientMessage = "[ERROR] wrong syntax";
            }
        }else {
            clientMessage = "[ERROR] last token is not ';' or not only ';' ";
        }
    }

    public void insertQuery(ArrayList<String > splitTokens) throws IOException {
        QueryTable queryTable = readFile1(currentDatabase,currentTable,table);
        queryTable.addRow(splitTokens);
        if (table.get(0).size() - 1 != splitTokens.size()){
            clientMessage = "[ERROR] wrong value list length";
            table.clear();
            return;
        }
        writeFile(super.storageFolderPath);
        clientMessage = "[OK] " + currentTable + " insert successfully";
    }
}
