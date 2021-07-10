package codegenerator;


import java.util.HashMap;
import java.util.Map;

public class SymbolTable{
    Map<String,Descriptor> symboltable;

    SymbolTable(String key,Descriptor descriptor){
        symboltable=new HashMap<>();
        symboltable.put(key,descriptor);
    }



}
