package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateParser extends Parser{


    public UpdateParser(String currentDatabase, String currentTable, ArrayList<ArrayList<String>> table, ArrayList<ArrayList<String>> table2) {
        super(currentDatabase, currentTable, table, table2);
    }

    public void updateTable(ArrayList<String> tokens, Parser parser) throws IOException {
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] need something after UPDATE";
            return;
        }
        //TODO 需要给别的判断么
        if (!isPlainText(tokens.get(0))){
            clientMessage = "[ERROR] table name is illegal";
            return;
        }
        if(!checkTableExist(currentDatabase,tokens.get(0))){
            clientMessage = "[ERROR] table is not exist";
            System.out.println("wrong syntax");
            return;
        }
        currentTable = tokens.get(0);
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after table name need SET ";
            System.out.println("wrong syntax");
            return;
        }
        //SET
        if (!tokens.get(0).toUpperCase().equals("SET")) {
            clientMessage = "[ERROR] wrong syntax after table name";
            return;
        }
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] after SET need something";
            return;
        }
        //TODO create a small function from this
        int whereNum = 0;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).toUpperCase().equals("WHERE")){
                System.out.println("00000000");
                whereNum = i;
                System.out.println("whereNum " + whereNum);
                break;
            }
        }
        if (whereNum == 0) {
            clientMessage = "[ERROR] need WHERE";
            return;
        }
        ArrayList<String> tmp = new ArrayList<>();
        System.out.println(tokens);
        int j = 0;
        for (int i = 0; i < whereNum; i++) {
            tmp.add(j++, tokens.get(i));
        }
        System.out.println("tmp: " + tmp);
        //TODO create a small function to this
        if(!isNameValueList(tmp)){
            clientMessage = "[ERROR] wrong NameValueList";
            return;
        }
        for (int i = 0; i < whereNum; i++) {
            super.getRestToken(tokens);
        }
        if(tokens.size() == 0){
            clientMessage = "[ERROR] need something after NameValueList";
            return;
        }
        if (checkLastToken(tokens)){
            getRestToken(tokens);
            tokens.remove(tokens.size()-1);
            if (updateQuery(tmp,tokens)){
                System.out.println("106");

            }else {

            }
        }else {
            clientMessage = "[ERROR] need ;";
            return;
        }

    }



    public boolean updateQuery(ArrayList<String> tmp, ArrayList<String> tokens) throws IOException {
        System.out.println("136");
//        QueryTable queryTable = new QueryTable(currentDatabase,currentTable,table);
//        queryTable.readFile1(currentDatabase,currentTable);
        QueryTable queryTable1 = readFile1(currentDatabase,currentTable,table);
        queryTable1.doNothing();
        System.out.println("101");
        System.out.println(storageFolderPath);
//        QueryTable queryTable = readFile1(currentDatabase,currentTable);
        if ((judgeIsCondition(tokens))){
            deleteQuotation(tokens);
            int col = 0;
            //get the exact rows
            List<Integer> rowNumbers = new ArrayList<>();
            System.out.println("111");
            System.out.println(table.get(0));
            System.out.println(table.get(0).size());
            for (int i = 0; i < table.get(0).size(); i++) {
                if (table.get(0).get(i).equals(tokens.get(0))){
                    col = i;
                    break;
                }
            }
            for (int i = 0; i < table.size(); i++) {
                ArrayList<String> row = table.get(i);
                if (row.get(col).equals(tokens.get(2))) {
                    System.out.println("125");
                    rowNumbers.add(i);
                }
            }
            int col1 = 0;
            for (int i = 0; i < tmp.size(); i += 4) {
                for (int j = 0; j < table.get(0).size(); j++) {
                    if (table.get(0).get(j).equals(tmp.get(i))){
                        col1 = j;
                        break;
                    }
                }
                System.out.println(col1);
                for (int k = 0; k < rowNumbers.size(); k++) {
                    System.out.println("1444");
                    table.get(rowNumbers.get(k)).set(col1,tmp.get(i+2));
                }
            }
            System.out.println("150");
            System.out.println(table);
            writeFile(super.storageFolderPath);
            System.out.println(clientMessage);
        }else {
            clientMessage = "[ERROR] wrong condition";
            return false;
        }

        return true;
    }


}
