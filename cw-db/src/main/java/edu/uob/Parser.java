package edu.uob;

import java.io.*;
//import java.lang.runtime.SwitchBootstraps;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Parser {


    Tokeniser tokeniser = new Tokeniser();
    DBServer dbServer = new DBServer();

    public String currentDatabase = "";
    public String currentTable = "";
    String storageFolderPath = "";

    static String clientMessage = "";

    public ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
    public ArrayList<ArrayList<String>> table2 = new ArrayList<ArrayList<String>>();
    public Parser(String currentDatabase, String currentTable, ArrayList<ArrayList<String>> table, ArrayList<ArrayList<String>> table2){
        this.currentDatabase = currentDatabase;
        this.currentTable = currentTable;
        this.table = table;
        this.table2 = table2;
    }

    public void getRestToken(ArrayList<String> tokens){
        System.out.println("original tokens:" + tokens);
        tokens.remove(0);
        System.out.println("changed tokens:" + tokens);
    }

    public boolean checkLastToken(ArrayList<String> tokens){
        int size = tokens.size();
        System.out.println("size: " + size);
        if(size < 1){
            return false;
        }
        if (tokens.get(size - 1).charAt(0) ==  ';') {
            return true;
        }else {
            return false;
        }


    }

    public void switchToken(ArrayList<String> tokens) throws IOException {
        switch (tokens.get(0).toUpperCase(Locale.US)){
            case ("USE"):
                UseParser useParser = new UseParser(this.currentDatabase,this.currentTable,table,table2);
                useParser.useDatabase(tokens,this);
                break;
            case ("CREATE"):
                CreateParser createParser = new CreateParser(currentDatabase,currentTable,table,table2);
                createParser.judgeCreate(tokens);
                break;
            case ("DROP"):
                DropParser dropParser = new DropParser(this.currentDatabase,this.currentTable,table,table2);
                dropParser.judgeDrop(tokens,this);
                break;
            case ("ALTER"):
                AlterParser alterParser = new AlterParser(currentDatabase,currentTable,table,table2);
                alterParser.alterDatabase(tokens);
                break;
            case ("INSERT"):
                InsertParser insertParser = new InsertParser(currentDatabase,currentTable,table,table2);
                insertParser.insertTable(tokens,this);
                break;
            case ("SELECT"):
                SelectParser selectParser = new SelectParser(currentDatabase, currentTable,table,table2);
                selectParser.selectTable(tokens,this);
                break;
            case ("UPDATE"):
                UpdateParser updateParser = new UpdateParser(currentDatabase, currentTable,table,table2);
                updateParser.updateTable(tokens,this);
                break;
            case ("DELETE"):
                DeleteParser deleteParser = new DeleteParser(currentDatabase, currentTable,table,table2);
                deleteParser.deleteTable(tokens,this);
                break;
            case ("JOIN"):
                JoinParser joinParser = new JoinParser(currentDatabase, currentTable,table,table2);
                joinParser.joinTable(tokens,this);
                break;
            default:
                clientMessage = "[ERROR] wrong syntax";
                break;
        }
    }

    public boolean isPlainText(String plainText){
        for (int i = 0; i < plainText.length() ; i++) {
             if (!Character.isDigit(plainText.charAt(i)) && !Character.isLetter(plainText.charAt(i))) {
                     return false;
            }
        }
        return true;
    }




    public boolean isAttributeName(String tokens){
        //FIXME many problems see alterparser.java-58
        ArrayList<String> splitTokens = new ArrayList<String>();
        if (tokens.length() == 0) {
            clientMessage = "[ERROR] attribute name is null";
            return false;
        } else {
            splitAttributeName(splitTokens, tokens);
            if(splitTokens.size() == 1 ){
                if(isPlainText(splitTokens.get(0))){
                    return true;
                }else {
                    clientMessage = "[ERROR] wrong attribute name eg: [PlainText]";
                    return false;
                }
            }else if(splitTokens.size() == 2){
                if(isPlainText(splitTokens.get(0)) && isPlainText(splitTokens.get(1))){
                    return true;
                }else {
                    clientMessage = "[ERROR] wrong attribute name eg: [TableName] \".\" [PlainText]";
                    return false;
                }
            }
        }
        return false;
    }


    public boolean isAttributeList(ArrayList<String> tokens, ArrayList<String> splitTokens){
        //TODO add this function to create-table function!!!
//        ArrayList<String> splitTokens = new ArrayList<String>();
        if(tokens.size() > 1){
            splitListByComma(splitTokens,tokens);
            for (int i = 0; i < splitTokens.size(); i++) {
                if (!isAttributeName(splitTokens.get(i))) {
                    clientMessage = "[ERROR] wrong attribute list";
                    return false;
                }
            }
        } else if (tokens.size() == 1) {
            for (int i = 0; i < tokens.size(); i++) {
                if (!isAttributeName(tokens.get(i))) {
                    clientMessage = "[ERROR] wrong attribute list";
                    return false;
                }
            }
        }else {
            clientMessage = "[ERROR] wrong attribute list";
            return false;
        }
        return true;
    }


    public boolean isWildAttribList(ArrayList<String> tokens){
        //<AttributeList> | "*"
        ArrayList<String> splitTokens = new ArrayList<String>();
        if (tokens.size() == 0) {
            clientMessage = "[ERROR] wildAttribName is null";
            return false;
        } else {
            if(tokens.get(0).equals("*")){
                return true;
            } else if (isAttributeList(tokens, splitTokens)) {
//            FIXME loss another argument in isAttributeList
                return true;
            }
            else {
                clientMessage = "[ERROR] wrong wildAttribList";
                return false;
            }
        }

    }
    public void splitAttributeName(ArrayList<String> splitTokens, String tokens){
        int spotNum = 0;
        int size = tokens.length();
        for (int i = 0; i < size; i++) {
            if (tokens.charAt(i) == '.') {
                spotNum = i;
                break;
            }
        }
        if (spotNum == 0) {
            splitTokens.add(0,tokens);
        } else {
            splitTokens.add(0, tokens.substring(0,spotNum));
            splitTokens.add(1, tokens.substring(spotNum + 1, size));
        }
    }

    public void splitListByComma(ArrayList<String> splitTokens, ArrayList<String> tokens){
        int j = 0;
        int size = tokens.size();
        if(size == 1){
            splitTokens.add(0,tokens.get(0));
        }else {
            for (int i = 0; i < size; i++) {
                if (!tokens.get(i).equals(",")) {
                    splitTokens.add(j++, tokens.get(i));
                }
            }
        }


    }

    public String getCurrentDatabase() {
        return currentDatabase;
    }
    public void setCurrentDatabase(String currentDatabase){
        this.currentDatabase = currentDatabase;
    }

    public boolean checkTableExist(String currentTable, String tableName){
        //TODO check whether the tableName is exist;
//       tableName;
        if (currentTable == null){
            return false;
        } else {
            String storageFolderPath = "";
            String suffixPath = ".tab";
            storageFolderPath = Paths.get("databases",currentDatabase,tableName+suffixPath).toAbsolutePath().toString();
            File table = new File(storageFolderPath);
            if (!table.exists()) {
                return false;
            }
        }

        return true;
    }

    public boolean isDigit(Character character){
        if(character.hashCode() >= 48 && character.hashCode() <= 57){
            return true;
        }else {
            return false;
        }
    }

    public boolean isUppercase(Character character){
        if(character.hashCode() >= 65 && character.hashCode() <= 90){
            return true;
        }else {
            return false;
        }
    }

    public boolean isLowercase(Character character){
        if(character.hashCode() >= 97 && character.hashCode() <= 122){
            return true;
        }else {
            return false;
        }
    }

    public boolean isLetter(Character character){
        if(isUppercase(character) || isLowercase(character)){
            return true;
        }else {
            return false;
        }
    }

    public boolean isSymbol(Character character){
//        int num = character.hashCode();
//        if(num == 33 || (num >= 35 && num <= 47) || (num >= 58 && num <= 64) || (num >= 91 && num <= 96) || num == 123 || num == 125 || num == 126){
//            return true;
//        }else {
//            return false;
//        }
        String[] symbolList = {"!" , "#" , "$" , "%" , "&" , "(" , ")" , "*" , "+" , "," , "-" , "." , "/" , ":" , ";" , ">" , "=" ,
                "<" , "?" , "@" , "[" , "\\" , "]" , "^" , "_" , "`" , "{" , "}" , "~"};
        return Arrays.asList(symbolList).contains(character);
    }
    
    public boolean isSpace(Character character){
        if (character.hashCode() == 32) {
            return true;
        }else {
            return false;
        }
    }

    public boolean isDigitSequence(String digitList){
        for (int i = 0; i < digitList.length(); i++) {
            if (!isDigit(digitList.charAt(i))) {
                //FIXME return a clientmessage ?
                return false;
            }
        }
        return true;
    }

    public boolean isIntegerLiteral(String digitList){
        int len = digitList.length();
        if(digitList.charAt(0) == '+' && isDigitSequence(digitList.substring(1,len - 1))){
            return true;
        } else if (digitList.charAt(0) == '-' && isDigitSequence(digitList.substring(1,len - 1))) {
            return true;
        }else if (isDigitSequence(digitList)){
            return true;
        }
        return false;
    }
    public boolean isFloatLiteral(String digitList){
        String[] digits = splitFloatLiteral(digitList);
        String digit1 = digits[0];
        String digit2 = digits[1];
        int len1 = digit1.length();
//        int len2 = digit2.length();
        if(isDigitSequence(digit1) && isDigitSequence(digit2)){
            return true;
        } else if (digit1.charAt(0) == '+' && isDigitSequence(digit1.substring(1,len1 - 1)) &&  isDigitSequence(digit2)) {
            return true;
        } else if (digit1.charAt(0) == '-' && isDigitSequence(digit1.substring(1,len1 - 1)) &&  isDigitSequence(digit2)) {
            return true;
        }
        return false;
    }

    public String[] splitFloatLiteral(String digitList){
        String[] parts = digitList.split("\\.");
        String digit1 = parts[0];
        String digit2 = parts[1];
        String[] result = {digit1, digit2};
        return result;
    }
    public boolean isBooleanLiteral(String tokens){
        if(tokens.toUpperCase(Locale.US).equals("TRUE")){
            return true;
        } else if (tokens.toUpperCase(Locale.US).equals("FALSE")) {
            return true;
        }
        return false;
    }

    public boolean isComparator(String tokens){
        String[] comparatorList ={"==" , ">" , "<" , ">=" , "<=" , "!=" , "LIKE"};
        return Arrays.asList(comparatorList).contains(tokens);
    }


    public boolean isCharLiteral(Character character){
        //[Space] | [Letter] | [Symbol] | [Digit]
        if(isSpace(character)){
            return true;
        } else if (isLetter(character)) {
            return true;
        } else if (isSymbol(character)) {
            return true;
        } else if (isDigit(character)) {
            return true;
        }else {
            return false;
        }
    }

    public boolean isStringLiteral(String tokens){
        //TODO "" | [CharLiteral] | [StringLiteral] [CharLiteral] understand it!
        if(tokens == null){
            return true;
        }
        for (int i = 0; i < tokens.length(); i++) {
            if(!isCharLiteral(tokens.charAt(i))){
                return false;
            }else {
                return true;
            }
        }

        return false;
    }

    public boolean isValue(String tokens){
        int len = tokens.length();
        if(tokens.charAt(0) == '\'' && tokens.charAt(len-1) == '\'' && isStringLiteral(tokens.substring(1,len-1))){
            return true;
        } else if (isBooleanLiteral(tokens)) {
            return true;
        } else if (tokens.contains(".") && isFloatLiteral(tokens)) {
            return true;
        } else if (isIntegerLiteral(tokens)) {
            return true;
        } else if (tokens.toUpperCase(Locale.US).equals("NULL")) {
            return true;
        }else {
            return false;
        }
    }

    public boolean isValueList(ArrayList<String> tokens, ArrayList<String> splitTokens){
        if(tokens.size() > 1){
            splitListByComma(splitTokens,tokens);
            for (int i = 0; i < splitTokens.size(); i++) {
                System.out.println("471");
                System.out.println(splitTokens.get(i));
                if (!isValue(splitTokens.get(i))) {
                    clientMessage = "[ERROR] wrong value list";
                    return false;
                }
            }
        } else if (tokens.size() == 1) {
                if (!isValue(tokens.get(0))) {
                    System.out.println("437");
                    System.out.println(tokens.get(0));
                    clientMessage = "[ERROR] wrong value list";
                    return false;
                }
        }else {
            clientMessage = "[ERROR] wrong attribute list";
            return false;
        }
        return true;
    }


    public boolean isNameValuePair(ArrayList<String> tokens){
        ArrayList<String> splitTokens = new ArrayList<String>();
        if (tokens.size() == 0) {
            clientMessage = "[ERROR] nameValue is null";
            return false;
        } else {
            if(isAttributeName(tokens.get(0)) && tokens.get(1).equals("=") && isValue(tokens.get(2))){
                return true;
            }else {
                clientMessage = "[ERROR] wrong nameValue eg: [AttributeName] \"=\" [Value]";
                return false;
            }
        }
    }


    public boolean isNameValueList(ArrayList<String> tokens){
        //id = 1, name = 'bob'
        ArrayList<ArrayList<String>> tmp = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < tokens.size(); i += 4) {
            ArrayList<String> innerArrayList = new ArrayList<>();
            innerArrayList.add(tokens.get(i));
            innerArrayList.add(tokens.get(i + 1));
            innerArrayList.add(tokens.get(i + 2));
            tmp.add(innerArrayList);
        }
        ArrayList<String> splitTokens = new ArrayList<String>();
        if(tokens.size() != 0){
            splitListByComma(splitTokens,tokens);
            System.out.println("557");
            for (int i = 0; i < tmp.size(); i++) {
                if (!isNameValuePair(tmp.get(i))) {
                    clientMessage = "[ERROR] wrong nameValueList";
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isCondition(ArrayList<String> tokens){
        System.out.println("463");
        System.out.println(tokens);
        if (tokens.size() < 3){
            clientMessage = "[ERROR] wrong condition";
            return false;
        }else if(tokens.size() == 3){
            if (isAttributeName(tokens.get(0)) && isComparator(tokens.get(1)) && isValue(tokens.get(2))){
                //TODO [AttributeName] [Comparator] [Value]
                //FIXME 记得以后都要加！！！
                System.out.println("469");
                deleteQuotation(tokens);
                compareValue(tokens,tokens.get(1),table,table2);
                return true;
            }else {
                clientMessage = "[ERROR] wrong condition";
                return false;
            }
        } else if (tokens.size() == 5) {
            System.out.println("477");
            if (tokens.get(0).equals("(") && tokens.get(4).equals(")")){
                System.out.println("478");
                System.out.println(tokens);
                if (isAttributeName(tokens.get(1)) && isComparator(tokens.get(2)) && isValue(tokens.get(3))){
                    //TODO "(" [AttributeName] [Comparator] [Value] ")"
                    System.out.println("481");
                    removeBracket(tokens);
                    deleteQuotation(tokens);
                    compareValue(tokens,tokens.get(1),table,table2);
                    return true;
                }else {
                    clientMessage = "[ERROR] wrong condition";
                    return false;
                }
            }else {
                clientMessage = "[ERROR] need ( or )";
                return false;
            }
        }else {
            //TODO recursive
            if (tokens.get(0).equals("(")){
                ArrayList<ArrayList<String>> table1 = new ArrayList<>();
                ArrayList<ArrayList<String>> table2 = new ArrayList<>();
                removeBracket(tokens);
                System.out.println("495");
                System.out.println(tokens);
                deleteQuotation(tokens);
                compareValue(tokens,tokens.get(1),table,table2);
                tokens.remove(0);
                tokens.remove(0);
                tokens.remove(0);
//                isCondition(tokens);
                System.out.println("507");
                System.out.println(tokens);
                if (tokens.get(0).equals("AND")){
                    tokens.remove(0);
                    isCondition(tokens);
                } else if (tokens.get(0).equals("OR")) {
                    tokens.remove(0);

                    compareValue(tokens,tokens.get(1),table1,table2);
                    System.out.println("999");
                    System.out.println(table);
                    System.out.println(table1);
                    System.out.println(tokens);
                    System.out.println(tokens.get(1));
                    System.out.println(table1);
                    for (ArrayList<String> row : table1) {
                        if (!table.contains(row)) {
                            table.add(row);
                        }
                    }
                    System.out.println("535");
                    System.out.println(table);
                    ArrayList<ArrayList<String>> tmp = new ArrayList<>();
                    for (int i = 0; i < table.size(); i++) {
                        tmp.add(table.get(i));
                    }
                    updateTable(tmp,table);
                    System.out.println("555");
                    System.out.println(tokens);
                    if (tokens.size() == 3){
                        return true;
                    }
//                    isCondition(tokens);
                }
                return isCondition(tokens);
            }else {
                ArrayList<ArrayList<String>> table1 = new ArrayList<>();
                ArrayList<ArrayList<String>> table2 = new ArrayList<>();
//                removeBracket(tokens);
                System.out.println("525");
                System.out.println(tokens);
                deleteQuotation(tokens);
                compareValue(tokens,tokens.get(1),table,table2);
                tokens.remove(0);
                tokens.remove(0);
                tokens.remove(0);
//                isCondition(tokens);
                System.out.println("532");
                System.out.println(tokens);
                if (tokens.get(0).equals("AND")){
                    tokens.remove(0);
                    isCondition(tokens);
                } else if (tokens.get(0).equals("OR")) {
                    tokens.remove(0);
                    compareValue(tokens,tokens.get(1),table1,table2);
                    System.out.println("999");
                    System.out.println(table);
                    System.out.println(table1);
                    for (ArrayList<String> row : table1) {
                        if (!table.contains(row)) {
                            table.add(row);
                        }
                    }
                    System.out.println("569");
                    System.out.println(table);
                    ArrayList<ArrayList<String>> tmp = new ArrayList<>();
                    for (int i = 0; i < table.size(); i++) {
                        tmp.add(table.get(i));
                    }
                    updateTable(tmp,table);
                    System.out.println("555");
                    System.out.println(tokens);
                    if (tokens.size() == 3){
                        return true;
                    }
//                    isCondition(tokens);
                }
                return isCondition(tokens);
            }
        }
//        return false;
    }


    public void removeBracket(ArrayList<String> tokens){
        tokens.remove(tokens.size() - 1);
        tokens.remove(0);
    }

    public boolean judgeIsCondition(ArrayList<String> tokens){
        if(tokens.size() == 3){
            if (isAttributeName(tokens.get(0)) && isComparator(tokens.get(1)) && isValue(tokens.get(2))){
                //TODO [AttributeName] [Comparator] [Value]
                //FIXME 记得以后都要加！！！
                return true;
            }else {
                clientMessage = "[ERROR] wrong condition";
                return false;
            }
        } else if (tokens.size() == 5) {
            if (tokens.get(0) == "(" && tokens.get(4) == ")"){
                if (isAttributeName(tokens.get(1)) && isComparator(tokens.get(2)) && isValue(tokens.get(3))){
                    //TODO "(" [AttributeName] [Comparator] [Value] ")"
                    return true;
                }else {
                    clientMessage = "[ERROR] wrong condition";
                    return false;
                }
            }else {
                clientMessage = "[ERROR] need ( or )";
                return false;
            }
        }else {
            //TODO recursive
        }
        return false;
    }


    public QueryTable readFile1(String currentDatabase, String tableName, ArrayList<ArrayList<String>> thisTable) throws IOException{
        thisTable.clear();
        QueryTable queryTable = new QueryTable(currentDatabase,currentTable,table,table2);
        String suffixPath = ".tab";
        storageFolderPath = Paths.get("databases",currentDatabase,tableName+suffixPath).toAbsolutePath().toString();
//        File file = new File(storageFolderPath);
//        try (BufferedReader br = new BufferedReader(new FileReader(storageFolderPath))) {
        BufferedReader br = new BufferedReader(Files.newBufferedReader(Paths.get(storageFolderPath)));
            String line;
            while ((line = br.readLine()) != null) {
                // Split each line into separate columns using the tab delimiter (\t)
                String[] columns = line.split("\t");
                // Create a new ArrayList to store the columns for this row
                ArrayList<String> rowData = new ArrayList<>();
                // Add each column to the row ArrayList
                for (String column : columns) {
                    rowData.add(column);
                }
                // Add the row ArrayList to the 2D ArrayList
                thisTable.add(rowData);
            }
//        } catch (IOException e) {
//            System.err.println("Error reading file: " + e.getMessage());
//        }
        return queryTable;
    }

    public void compareValue(ArrayList<String> tokens, String symbol, ArrayList<ArrayList<String>> changedTable, ArrayList<ArrayList<String>> originTable){
        ArrayList<ArrayList<String>> selectedRows = new ArrayList<ArrayList<String>>();
        int columnIndex = 0;
        System.out.println("686");
        System.out.println(tokens);
        System.out.println("686+1");
        System.out.println(table2);
//        System.out.println(originTable);
        for (int i = 0; i < table2.get(0).size(); i++) {
            if (table2.get(0).get(i).equals(tokens.get(0))){
                columnIndex = i;
                break;
            }
        }
        switch (symbol){
            case ("=="):
                selectedRows.add(table2.get(0));
                for (ArrayList<String > row : table2) {
                    String columnValue = row.get(columnIndex);
                    if (columnValue.equals(tokens.get(2))) {
                        selectedRows.add(row);
                    }
                }
                break;
            case (">"):
                for (ArrayList<String > row : table2) {
                    String columnValue = row.get(columnIndex);
                    System.out.println("712");
                    if (columnValue.compareTo(tokens.get(2)) > 0 ) {
                        System.out.println("714");
                        selectedRows.add(row);
                    }
                }
                break;
            case ("<"):
                selectedRows.add(table2.get(0));
                for (ArrayList<String > row : table2) {
                    String columnValue = row.get(columnIndex);
                    if (columnValue.compareTo(tokens.get(2)) < 0 ) {
                        selectedRows.add(row);
                    }
                }
                break;
            case (">="):
                for (ArrayList<String > row : table2) {
                    String columnValue = row.get(columnIndex);
                    if (columnValue.compareTo(tokens.get(2)) >= 0 ) {
                        selectedRows.add(row);
                    }
                }
                break;
            case ("<="):
                selectedRows.add(table2.get(0));
                for (ArrayList<String > row : table2) {
                    String columnValue = row.get(columnIndex);
                    if (columnValue.compareTo(tokens.get(2)) <= 0 ) {
                        selectedRows.add(row);
                    }
                }
                break;
            case ("!="):
                for (ArrayList<String > row : table2) {
                    String columnValue = row.get(columnIndex);
                    if (columnValue.compareTo(tokens.get(2)) != 0 ) {
                        selectedRows.add(row);
                    }
                }
                break;
            case ("LIKE"):
                selectedRows.add(table2.get(0));
                for (ArrayList<String > row : table2) {
                    String columnValue = row.get(columnIndex);
                    if (columnValue.contains(tokens.get(2))) {
                        selectedRows.add(row);
                    }
                }
                break;
            default:
                clientMessage = "[ERROR] Comparator is wrong";
                break;
        }
        updateTable(selectedRows,changedTable);
    }



    public void updateTable(ArrayList<ArrayList<String>> selectedRows, ArrayList<ArrayList<String>> tempTable){
        tempTable.clear();
        for (int i = 0; i < selectedRows.size(); i++) {
            ArrayList<String> sourceRow = selectedRows.get(i);
            ArrayList<String> destRow = new ArrayList<>();
            for (int j = 0; j < sourceRow.size(); j++) {
                destRow.add(sourceRow.get(j));
            }
            tempTable.add(destRow);
        }
    }

    public void deleteQuotation(ArrayList<String > splitTokens) {
        for (int i = 0; i < splitTokens.size(); i++) {
            int len = splitTokens.get(i).length();
            if (splitTokens.get(i).charAt(0) == '\'' && splitTokens.get(i).charAt(len - 1) == '\'') {
                splitTokens.set(i, splitTokens.get(i).substring(1, len - 1));
            }
        }
    }

    public void writeFile(String storageFolderPath) throws IOException {
        BufferedWriter myWriter = Files.newBufferedWriter(Paths.get(storageFolderPath));
        for (int i = 0; i < table.size(); i++) {
            for (int j = 0; j < table.get(0).size(); j++) {
                if (table.get(i).get(j).equals("\t")){
//                    myWriter.write("");
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
        table.clear();
    }

}
