package codegenerator;

import java.util.LinkedHashMap;
import java.util.Map;

public class MethodDescriptor extends Descriptor{
    public LinkedHashMap<String,Descriptor> symbolTB=new LinkedHashMap<>();
    public String returnType;
    public String scopeName;
    public String returnAddress;
    public int number_of_params=0;

    MethodDescriptor(String scopeName){
        super();
        this.scopeName=scopeName;
    }
}