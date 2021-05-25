/**
 * Mathew Gabriel Lopez Garcia
 * A01635001
 */
package src;

import java.io.*;
import java.util.*;

public class Cyk{

    public static String word;
    public static String startingSymbol;
    public static ArrayList<String> terminals = new ArrayList<String>();
    public static ArrayList<String> nonTerminals = new ArrayList<String>();
    public static TreeMap<String,ArrayList<String>> grammar = new TreeMap<>();

    public static void main(String[] args){
        String gramatica = "C:\\Users\\52331\\IdeaProjects\\MatesCompus\\src\\src\\gramatica.txt";
        String palabra = "()()";
        parseGrammar(gramatica,palabra);
        String[][] cykTable = createCYKTable();
        String[][] resultado = doCyk(cykTable);
        printResult(resultado);
    }


    public static void parseGrammar(String gramatica, String palabra){
        Scanner input = openFile(gramatica);
        ArrayList<String> tmp = new ArrayList<>();
        int line = 2;
        word = palabra;
        startingSymbol = input.next(); //Simbolo inicial en la primera linea
        input.nextLine();

        while(input.hasNextLine() && line <= 3){ //En la linea 3 empiezan las producciones, aqui recojo terminales y no terminales
            tmp.addAll(Arrays.<String>asList(toArray(input.nextLine())));
            if(line == 2) { terminals.addAll(tmp); } //Linea 2 simbolos terminales
            if(line == 3) { nonTerminals.addAll(tmp); } //Linea 3 simbolos generadores
            tmp.clear();
            line++;
        }
        //A partir de la linea 3 tengo las producciones
        while(input.hasNextLine()){
            tmp.addAll(Arrays.<String>asList(toArray(input.nextLine())));
            String generador = tmp.get(0); //El generador es el primer elemento de la linea
            tmp.remove(0); //Remuevo el primer elemento
            grammar.put(generador, new ArrayList<String>()); //Creo una entrada en el treemap con el simbolo generador y un arrayList vacio de producciones
            grammar.get(generador).addAll(tmp); //Agrego todas las producciones correspondientes
            tmp.clear();
        }
        input.close();
    }

    public static void printResult (String[][] cykTable){
        System.out.println("Cadena: " + word);
        System.out.println("\nG = (" + terminals.toString().replace("[", "{").replace("]", "}")
                + ", " + nonTerminals.toString().replace("[", "{").replace("]", "}")
                + ", P, " + startingSymbol + ")\n\nCon las producciones P de la forma:");
        for(String s: grammar.keySet()){
            System.out.println(s + " -> " + grammar.get(s).toString().replaceAll("[\\[\\]\\,]", "").replaceAll("\\s", " | "));
        }
        System.out.println("\nAplicando el algoritmo CYK:\n");
        drawTable(cykTable);
    }

    public static void drawTable(String[][] cykTable){
        int l = findLongestString(cykTable) + 2;
        String formatString = "| %-" + l + "s ";
        String s = "";
        StringBuilder sb = new StringBuilder();
        sb.append("*");
        for(int x = 0; x <= l + 2; x++){
            if(x == l + 2){
                sb.append("*");
            }else{
                sb.append("-");
            }
        }
        String low = sb.toString();
        sb.delete(0, 1);
        String lowRight = sb.toString();
        //Imprimir tabla
        for(int i = 0; i < cykTable.length; i++){
            for(int j = 0; j <= cykTable[i].length; j++){
                System.out.print((j == 0) ? low : (i <= 1 && j == cykTable[i].length - 1) ? "" : lowRight);
            }
            System.out.println();
            for(int j = 0; j < cykTable[i].length; j++){
                s = (cykTable[i][j].isEmpty()) ? "-" : cykTable[i][j];
                System.out.format(formatString, s.replaceAll("\\s", ","));
                if(j == cykTable[i].length - 1) { System.out.print("|"); }
            }
            System.out.println();
        }
        System.out.println(low+"\n");
        //Paso 4: evaluar el éxito
        if(cykTable[cykTable.length-1][cykTable[cykTable.length-1].length-1].contains(startingSymbol)){
            System.out.println("La cadena \"" + word + "\" puede ser derivada de la gramatica G.");
            backTrack(cykTable);
        }else{
            System.out.println("La cadena \"" + word + "\" no puede ser derivada de la gramatica G.");
        }
    }

    public static int findLongestString(String[][] cykTable){
        int x = 0;
        for(String[] s : cykTable){
            for(String d : s){
                if(d.length() > x){ x = d.length(); }
            }
        }
        return x;
    }

    //Matriz 2x2 para el algoritmo CYK
    public static String[][] createCYKTable (){
        int length = word.length();
        String[][] cykTable = new String[length + 1][];
        cykTable[0] = new String[length];
        for(int i = 1; i < cykTable.length; i++){
            cykTable[i] = new String[length - (i - 1)];
        }
        for(int i = 1; i < cykTable.length; i++){
            Arrays.fill(cykTable[i], "");
        }
        return cykTable;
    }

    public static String[][] doCyk(String[][] cykTable){
        //Paso 1: Llenar la fila del encabezado (Contiene los simbolos terminales de la palabra)
        for(int i = 0; i < cykTable[0].length; i++){
            cykTable[0][i] = Character.toString(word.charAt(i));
        }
        //Paso 2: Obtener producciones para los simbolos terminales
        for(int i = 0; i < cykTable[1].length; i++){
            String[] validCombinations = checkIfProduces(new String[] {cykTable[0][i]});
            cykTable[1][i] = toString(validCombinations);
        }
        if(word.length() <= 1) { return cykTable; }
        //Paso 3: Obtener producciones de las subpalabras de tamaño 2
        for(int i = 0; i < cykTable[2].length; i++){
            String[] downwards = toArray(cykTable[1][i]);
            String[] diagonal = toArray(cykTable[1][i+1]);
            String[] validCombinations = checkIfProduces(getAllCombinations(downwards, diagonal));
            cykTable[2][i] = toString(validCombinations);
        }
        if(word.length() <= 2){ return cykTable; }
        //Paso 3: Obtener las producciones de subcadenas de tamaño n
        TreeSet<String> currentValues = new TreeSet<String>();
        for(int i = 3; i < cykTable.length; i++){
            for(int j = 0; j < cykTable[i].length; j++){
                for(int compareFrom = 1; compareFrom < i; compareFrom++){
                    String[] downwards = cykTable[compareFrom][j].split("\\s");
                    String[] diagonal = cykTable[i-compareFrom][j+compareFrom].split("\\s");
                    String[] combinations = getAllCombinations(downwards, diagonal);
                    String[] validCombinations = checkIfProduces(combinations);
                    if(cykTable[i][j].isEmpty()){
                        cykTable[i][j] = toString(validCombinations);
                    }else{
                        String[] oldValues = toArray(cykTable[i][j]);
                        ArrayList<String> newValues = new ArrayList<String>(Arrays.asList(oldValues));
                        newValues.addAll(Arrays.asList(validCombinations));
                        currentValues.addAll(newValues);
                        cykTable[i][j] = toString(currentValues.toArray(new String[currentValues.size()]));
                    }
                }
                currentValues.clear();
            }
        }
        return cykTable;
    }

    public static String[] checkIfProduces(String[] toCheck){
        ArrayList<String> storage = new ArrayList<>();
        for(String s : grammar.keySet()){
            for(String current : toCheck){
                if(grammar.get(s).contains(current)){
                    //AGREGAR PRODUCCION
                    //System.out.println("Agregando el simbolo generador " + s);
                    storage.add(s);
                }
            }
        }
        if(storage.size() == 0) {
            return new String[] {};
        }
        return storage.toArray(new String[storage.size()]);
    }

    public static String[] getAllCombinations(String[] from, String[] to){
        int length = from.length * to.length;
        int counter = 0;
        String[] combinations = new String[length];
        if(length == 0){
            return combinations;
        }
        for(int i = 0; i < from.length; i++){
            for(int j = 0; j < to.length; j++){
                combinations[counter] = from[i] + to[j];
                counter++;
            }
        }
        return combinations;
    }

    public static String buscaProducciones(int fila, int columna, String[][] cykTable){
        String resultado = "";
        ArrayList<String> listapares = new ArrayList<>();
        String s;
        ArrayList<String> produccionesValidas = new ArrayList();
        String simbolo = cykTable[fila][columna];
        //System.out.println("Las producciones para el simbolo " + simbolo + " son:");
        //System.out.println(grammar.get(simbolo));
        //System.out.println("Buscando todas las combinaciones que puede generar el simbolo generador en la fila y columna correspondientes");
        //Buscando todas las combinaciones de producciones en la fila y columna correspondientes
        for(int i = fila + 1; i < cykTable.length; i++){
            String charfila = cykTable[i][columna];
            if(charfila.compareTo("") == 0){
                cykTable[i][columna] = "-";
            }
            //System.out.print("Caracter de la fila " + cykTable[i][columna] + ",");
            for(int j = columna + 1; j < cykTable[2].length; j++ ){
                String charcolumna = cykTable[fila+1][j];
                if(charcolumna.compareTo("") == 0){
                    cykTable[i][j] = "-";
                }
                String par = cykTable[i][columna] + cykTable[fila+1][j];
                //System.out.println(par);
                if(par.contains("-")){
                    //System.out.println("El par contiene NULL, ignorando par");
                }else{
                    //System.out.println("Agregando par a la lista");
                    if(!produccionesValidas.contains(par)){
                        produccionesValidas.add(par);
                    }
                }

            }
            //System.out.println();
        }
        //System.out.println("PRODUCCIONES VALIDAS DEL SIMBOLO INICIAL ");
        System.out.println("ARBOL DE DERIVACION");
        System.out.println("    " + startingSymbol);
        for(String curr: produccionesValidas){
            System.out.println("   " + curr);
            String tmp1 = String.valueOf(curr.charAt(1));
            //System.out.println(curr.charAt(0) + " puede generar: \n " + grammar.get(tmp1));
            //System.out.println(curr.charAt(1) + " puede generar: \n " + grammar.get(tmp1));
            /**
            for(String ax: grammar.get(tmp1)){
                System.out.println(ax);
            }
            for(String bx: grammar.get(tmp1)){
                System.out.println(bx);
            }**/
            String first = grammar.get(tmp1).get(0);
            String second = grammar.get(tmp1).get(0);
            System.out.println("  " + first + " " + second);
            for(char cx: first.toCharArray()){
                //System.out.print(String.valueOf(cx));
                String tmp = String.valueOf(cx);
                resultado += grammar.get(tmp).get(0);
            }
            for(char cx: second.toCharArray()){
               // System.out.print(String.valueOf(cx));
                String tmp = String.valueOf(cx);
                resultado += grammar.get(tmp).get(0);
            }
            System.out.println(" " +resultado);
        }
        System.out.println("--------------------------------------------------------------");
        /**
        for(int i = 0; i < cykTable.length; i++){
            for(int j = 0; j < cykTable[i].length; j++){
                //s = (cykTable[i][j].isEmpty()) ? "-" : cykTable[i][j];
                //System.out.print(s);
            }
            System.out.println();
        }**/
        return resultado;
    }

    public static void backTrack(String[][] cykTable){
        System.out.println("La cadena puede ser formada a traves de las siguientes derivaciones:");
        //buscaProducciones(start[0], start[1], cykTable);
        int n = 1;
        int m = 0;
        String resultado = buscaProducciones(n, m, cykTable);
        //Imprime toda la tabla sin divisiones ni formato
        /**for(int i = 0; i < cykTable.length; i++){
         for(int j = 0; j < cykTable[i].length; j++){
         s = (cykTable[i][j].isEmpty()) ? "X" : cykTable[i][j];
         System.out.print(s);
         }
         System.out.println();
         } **/
        if(resultado.equals( word)){
            System.out.println("DERIVACION ENCONTRADA");
        }
        else{
            resultado = buscaProducciones(n+1,m+1,cykTable);
        }
    }

    public static String toString(String[] input){

        return Arrays.toString(input).replaceAll("[\\[\\]\\,]", "");
    }

    public static String[] toArray(String input){

        return input.split("\\s");
    }

    public static Scanner openFile(String file){
        try{
            return new Scanner(new File(file));
        }catch(FileNotFoundException e){
            System.out.println("Error: No se pudo abrir el archivo: " + file + ".");
            System.exit(1);
            return null;
        }
    }
}