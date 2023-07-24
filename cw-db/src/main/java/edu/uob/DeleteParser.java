package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeleteParser extends Parser{


    public DeleteParser(String currentDatabase, String currentTable, ArrayList<ArrayList<String>> table, ArrayList<ArrayList<String>> table2) {
        super(currentDatabase, currentTable, table, table2);
    }

    public void deleteTable(ArrayList<String> tokens, Parser parser) throws IOException {
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] need something after DELETE";
            return;
        }
        if (!tokens.get(0).toUpperCase().equals("FROM")) {
            clientMessage = "[ERROR] wrong syntax after DELETE";
            return;
        }
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] need table name after FROM";
            return;
        }
        if (!isPlainText(tokens.get(0))){
            clientMessage = "[ERROR] table name is illegal";
            return;
        }
        if(!checkTableExist(currentDatabase,tokens.get(0))){
            clientMessage = "[ERROR] table is not exist";
            return;
        }
        currentTable = tokens.get(0);
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after table name need WHERE";
            return;
        }
        if (!tokens.get(0).toUpperCase().equals("WHERE")) {
            clientMessage = "[ERROR] wrong syntax after tablename";
            return;
        }
        if (checkLastToken(tokens)){
            getRestToken(tokens);
            tokens.remove(tokens.size()-1);
            deleteQuery(tokens);
        }else {
            clientMessage = "[ERROR] last is not ;";
        }
    }

    public boolean deleteQuery(ArrayList<String> tokens) throws IOException {
        QueryTable queryTable1 = readFile1(currentDatabase,currentTable,table);
        queryTable1.doNothing();
        if ((judgeIsCondition(tokens))){
            deleteQuotation(tokens);
            List<Integer> rowNumbers = new ArrayList<>();
            int col = 0;
            switch (tokens.get(1)){
                case ("=="):
                    for (int i = 0; i < table.get(0).size(); i++) {
                        if (table.get(0).get(i).equals(tokens.get(0))){
                            col = i;
                            break;
                        }
                    }
                    for (int i = 0; i < table.size(); i++) {
                        ArrayList<String> row = table.get(i);
                        if (row.get(col).equals(tokens.get(2))) {
                            rowNumbers.add(i);
                        }
                    }
                    break;
                case (">"):
                    for (int i = 0; i < table.get(0).size(); i++) {
                        if (table.get(0).get(i).equals(tokens.get(0))){
                            col = i;
                            break;
                        }
                    }
                    for (int i = 0; i < table.size(); i++) {
                        ArrayList<String> row = table.get(i);
                        if (row.get(col).hashCode() > tokens.get(2).hashCode()) {
                            rowNumbers.add(i);
                        }
                    }
                    break;
                case ("<"):
                    for (int i = 0; i < table.get(0).size(); i++) {
                        if (table.get(0).get(i).equals(tokens.get(0))){
                            col = i;
                            break;
                        }
                    }
                    for (int i = 0; i < table.size(); i++) {
                        ArrayList<String> row = table.get(i);
                        if (row.get(col).hashCode() < tokens.get(2).hashCode()) {
                            rowNumbers.add(i);
                        }
                    }
                    break;
                case (">="):
                    for (int i = 0; i < table.get(0).size(); i++) {
                        if (table.get(0).get(i).equals(tokens.get(0))){
                            col = i;
                            break;
                        }
                    }
                    for (int i = 0; i < table.size(); i++) {
                        ArrayList<String> row = table.get(i);
                        if (row.get(col).hashCode() >= tokens.get(2).hashCode()) {
                            rowNumbers.add(i);
                        }
                    }
                    break;
                case ("<="):
                    for (int i = 0; i < table.get(0).size(); i++) {
                        if (table.get(0).get(i).equals(tokens.get(0))){
                            col = i;
                            break;
                        }
                    }
                    for (int i = 0; i < table.size(); i++) {
                        ArrayList<String> row = table.get(i);
                        if (row.get(col).hashCode() <= tokens.get(2).hashCode()) {
                            rowNumbers.add(i);
                        }
                    }
                    break; case ("!="):
                    for (int i = 0; i < table.get(0).size(); i++) {
                        if (table.get(0).get(i).equals(tokens.get(0))){
                            col = i;
                            break;
                        }
                    }
                    for (int i = 0; i < table.size(); i++) {
                        ArrayList<String> row = table.get(i);
                        if (row.get(col).hashCode() != tokens.get(2).hashCode()) {
                            rowNumbers.add(i);
                        }
                    }
                    break;
                case ("LIKE"):
                    for (int i = 0; i < table.get(0).size(); i++) {
                        if (table.get(0).get(i).equals(tokens.get(0))){
                            col = i;
                            break;
                        }
                    }
                    for (int i = 0; i < table.size(); i++) {
                        ArrayList<String> row = table.get(i);
                        if (row.get(col).contains(tokens.get(2))) {
                            rowNumbers.add(i);
                        }
                    }
                    break;
                default:
                    clientMessage = "[ERROR] Comparator is wrong";
                    break;
            }
            Collections.sort(rowNumbers, Collections.reverseOrder());
            for (int rowIndex : rowNumbers) {
                if (rowIndex != 0){
                    table.remove(rowIndex);
                }
            }
            writeFile(super.storageFolderPath);
            clientMessage = "[OK] delete successfully";
        }else {
            clientMessage = "[ERROR] wrong condition";
            return false;
        }
        return true;
    }
}
