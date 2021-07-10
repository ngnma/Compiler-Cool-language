package codegenerator;

import codegenerator.COOLException.DeclarationException;
import codegenerator.COOLException.TypeMismatchException;
import scanner.CompilerScanner;
import java.io.*;
import java.util.*;

class ArrayClass{
    String name;
    String index;

    public ArrayClass(String name,String index){
        this.name = name;
        this.index = index;
    }
}

public class CodeGenerator implements parser.CodeGenerator {
    private CompilerScanner lexical;
    String text = new String();
    String data = new String();
    boolean inDCL = false;
    Descriptor stp = null;

    int LabelCounter = 0;
    int continueCounter = 0;
    int ifcounter = 0;
    int elsecounter = 0;
    int endcounter = 0;
    int lCounter = 0;
    int continueEqualcounter = 0;
    int end_loopcounter = 0;
    int samecounter = 0;
    int notsamecounter = 0;
    int loopcunter = 0;
    int newStringCounter = 0;
    int newArrayCounter = 0;

    Stack<Object> semanticStack = new Stack<>();
    Map<String, Descriptor> symbolTable = new HashMap<>();
    LinkedList<String> address = new LinkedList<>();
    LinkedList<String> floating_address = new LinkedList<>();

    public CodeGenerator(CompilerScanner lexical) throws FileNotFoundException, UnsupportedEncodingException {
        this.lexical = lexical;
        setAddress();
        setKeywords();
        setFloating_address();
    }
    
    private void setFloating_address(){
        for (int i = 0; i < 29; i++) {
            floating_address.add("$f" + i);
        }
        floating_address.remove("$f12");
        floating_address.remove("$f0");
    }

    private void setAddress() {
        for (int i = 0; i < 7; i++) {
            address.add("$t" + i);
        }
        for (int i = 4; i < 8; i++) {
            address.add("$s" + i);
        }
    }

    private void setKeywords() {
        symbolTable.put("bool", null);
        symbolTable.put("break", null);
        symbolTable.put("int", null);
        symbolTable.put("void", null);
        symbolTable.put("real", null);
        symbolTable.put("string", null);
        symbolTable.put("class", null);
        symbolTable.put("for", null);
        symbolTable.put("while", null);
        symbolTable.put("if", null);
        symbolTable.put("else", null);
        symbolTable.put("return", null);
        symbolTable.put("rof", null);
        symbolTable.put("let", null);
        symbolTable.put("fi", null);
        symbolTable.put("Array", null);
        symbolTable.put("in_string", null);
        symbolTable.put("out_string", null);
        symbolTable.put("continue", null);
        symbolTable.put("new", null);
        symbolTable.put("loop", null);
        symbolTable.put("pool", null);
        symbolTable.put("then", null);
        symbolTable.put("len", null);
        symbolTable.put("in_int", null);
        symbolTable.put("out_int", null);
    }

    public String getData() {
        return data;
    }

    public String getText() {
        return text;
    }

    private String FtoHex(String input) {
        String binary = java.lang.Long.toBinaryString(Float.floatToIntBits(Float.parseFloat(input)));
        String s;
        if (Float.parseFloat(input) > 0)
            s= "0"+binary;
        else
            s= "1"+binary;
        return "0x"+Integer.toHexString(Integer.parseInt(s, 2));
    }

    String maketype(Object object) {
        if (object instanceof Descriptor)
            return ((Descriptor) object).type;
        else if (symbolTable.containsKey(object))
            return symbolTable.get(object).type;
        else
            return null;
    }

    public String checkType(Object oprand1, Object oprand2, String operator,String sem) throws TypeMismatchException {
        String type1, type2;
        type1=maketype(oprand1);
        type2=maketype(oprand2);

        String number[] = new String[]{"-", "+", "/", "%", "*", "+=", "-=", "*=", "/="};
        String bool[] = new String[]{"||", "&&"};
        String numberToBool[]=new String[]{">=","<=","<",">","==","!="};
        if (type1.equals(type2)) {
            switch (type1) {
                case "int":
                    if (operator.equals("&") || operator.equals("|") || operator.equals("^"))
                        return "int";
                    for (int i = 0; i < number.length; i++) {
                        if (operator.equals(number[i]))
                            return "int";
                    }

                    for (int i = 0; i < numberToBool.length; i++) {
                        if (operator.equals(numberToBool[i]))
                            return "int";
                    }
                    text = text.concat("li $v0,4\n" +
                            "la $a0,err\n"+
                            "syscall\n");
                    data = data.concat("err: .asciiz \""+"Operation " + operator + " is not valid for Int operators"+ "\"\n");
                    throw new TypeMismatchException("Operation " + operator + " is not valid for Int operators");
                case "real":
                    for (int i = 0; i < number.length; i++) {
                        if (operator.equals(number[i]))
                            return "real";
                    }
                    for (int i = 0; i < numberToBool.length; i++) {
                        if (operator.equals(numberToBool[i]))
                            return "real";
                    }
                    text = text.concat("li $v0,4\n" +
                            "la $a0,err\n"+
                            "syscall\n");
                    data = data.concat("err: .asciiz \""+"Operation " + operator + " is not valid for Real operators"+ "\"\n");
                    throw new TypeMismatchException("Operation " + operator + " is not valid for Real operators");
                case "string":
                    if (operator.equals("+"))
                        return "string";
                    else if (operator.equals("==") )
                        return "string";
                    else if (operator.equals("!=") )
                        return "string";
                    text = text.concat("li $v0,4\n" +
                            "la $a0,err\n"+
                            "syscall\n");
                    data = data.concat("err: .asciiz \""+"Operation " + operator + " is not valid for String operators"+ "\"\n");
                    throw new TypeMismatchException("Operation " + operator + " is not valid for String operators");
                case "bool":
                    for (int i = 0; i < bool.length; i++) {
                        if (operator.equals(bool[i]))
                            return "bool";
                    }
                    text = text.concat("li $v0,4\n" +
                            "la $a0,err\n"+
                            "syscall\n");
                    data = data.concat("err: .asciiz \""+"Operation " + operator + " is not valid for Bool operators"+ "\"\n");
                    throw new TypeMismatchException("Operation " + operator + " is not valid for Bool operators");
            }
        }
        text = text.concat("li $v0,4\n" +
                "la $a0,err\n"+
                "syscall\n");
        data = data.concat("err: .asciiz \""+"can not "+sem+" : "+type1+" , "+type2+ "\"\n");
        throw new TypeMismatchException("can not "+sem+" : "+type1+" , "+type2);
    }

    @Override
    public void doSemantic(String sem) throws DeclarationException, TypeMismatchException {

        System.out.println("semantic = " + sem);
        switch (sem) {
            case "push": {
                System.out.println("push id running...");
                System.out.println("push : " + lexical.currentSymbol.getValue().toString());
                if (lexical.currentSymbol.getTokenName().equals("Identifier")) {
                    if (inDCL) {
                        if (!symbolTable.containsKey(lexical.currentSymbol.getValue().toString()))
                            semanticStack.push(lexical.currentSymbol.getValue().toString());
                        else
                            throw new DeclarationException("The variable already exist!");
                    } else {
                        if (symbolTable.containsKey(lexical.currentSymbol.getValue().toString()))
                            semanticStack.push(lexical.currentSymbol.getValue().toString());
                        else
                            throw new DeclarationException("The variable have not exist yet!");
                    }
                } else if (lexical.currentSymbol.getTokenName().equals("DecIntegerLiteral")) {
                    Descriptor descriptor = new Descriptor("int");
                    descriptor.value = lexical.currentSymbol.getValue().toString();
                    semanticStack.push(descriptor);
                }
                else if (lexical.currentSymbol.getTokenName().equals("RealNumber")) {
                    Descriptor descriptor = new Descriptor("real");
                    descriptor.value = lexical.currentSymbol.getValue().toString();
                    semanticStack.push(descriptor);
                }
                else if (lexical.currentSymbol.getTokenName().equals("String")) {
                    Descriptor descriptor = new Descriptor("string");
                    descriptor.value = lexical.currentSymbol.getValue().toString();
                    semanticStack.push(descriptor);
                }
                else if (lexical.currentSymbol.getTokenName().equals("true")) {
                    Descriptor descriptor = new Descriptor("bool");
                    descriptor.value = lexical.currentSymbol.getValue().toString();
                    semanticStack.push(descriptor);
                }
                else if (lexical.currentSymbol.getTokenName().equals("false")) {
                    Descriptor descriptor = new Descriptor("bool");
                    descriptor.value = lexical.currentSymbol.getValue().toString();
                    semanticStack.push(descriptor);
                }
                else
                    semanticStack.push(lexical.currentSymbol.getValue().toString());
                break;
            }
            case "switch": {
                System.out.println("switch id running...");
                inDCL = !inDCL;
                break;
            }
            case "msdscp": {
                System.out.println("msdscp id running...");
                String type = (String) semanticStack.pop();
                String name = (String) semanticStack.pop();
                symbolTable.put(name, new Descriptor(type));

                //address
                if (type.equals("int")){
                    symbolTable.get(name).setAddress(address.pop());
                    text = text.concat("la " + symbolTable.get(name).address + "," + name + "\n");
                    data = data.concat(name + ": .word 0\n");
                }
                else if (type.equals("real")){
                    symbolTable.get(name).setAddress(address.pop());
                    data = data.concat(name + ": .float 0.0\n");
                    text = text.concat("la " + symbolTable.get(name).address + "," + name + "\n");
                }
                else if (type.equals("string")){
                    symbolTable.get(name).setAddress(address.pop());
                    data = data.concat(name+": .asciiz \"\\n\""+"\n");
                    text = text.concat("la " + symbolTable.get(name).address + "," + name + "\n");
                }
                else if (type.equals("bool")){
                    symbolTable.get(name).setAddress(address.pop());
                    data = data.concat(name+": .word 0"+"\n");
                    text = text.concat("la " + symbolTable.get(name).address + "," + name + "\n");
                }

                break;
            }
            case "len" :{
                /* len(string) --> Emtiazi! */
                System.out.println("len is running");

                String name = semanticStack.pop().toString();

                if (symbolTable.get(name) instanceof ArrayDescriptor){
                    ArrayDescriptor arrayDescriptor = (ArrayDescriptor) symbolTable.get(name);
                    int size = arrayDescriptor.size;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(size);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");

                    text = text.concat("li $t9,"+size+"\n");

                }
                else if (symbolTable.get(name).type.equals("string")){
                    int size = symbolTable.get(name).value.toString().length();

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(size);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");

                    text = text.concat("move $t8,"+symbolTable.get(name).address+"\n" +
                            "li $s0,0\n" +
                            "loop"+loopcunter+":\n" +
                            "lb $k1,($t8)\n" +
                            "lb $t9, endline\n" +
                            "beq $k1,$t9, end"+endcounter+"\n" +
                            "addi $s0,$s0,1\n" +
                            "j loop"+loopcunter+"\n" +
                            "end"+endcounter+":\n" +
                            "move $t9,$s0\n");

                    endcounter++;
                    loopcunter++;
                }

                //mips:)
                break;
            }
            case "push_element":{
                String index=( (Descriptor) semanticStack.pop() ).value.toString();
                String name= (String) semanticStack.pop();
                ArrayDescriptor ad= (ArrayDescriptor) symbolTable.get(name);

                //mips:)
                semanticStack.push(ad.value[Integer.parseInt(index)]);
                semanticStack.push(new ArrayClass(name,index));
                break;
            }
            case "madscp":{
                System.out.println("madscp id running...");
                String type = (String) semanticStack.pop();
                String name = (String) semanticStack.pop();
                symbolTable.put(name,new ArrayDescriptor(type));

                //mips:)

                symbolTable.get(name).setAddress(address.pop());
                text = text.concat("la " + symbolTable.get(name).address + "," + name + "\n");
                data = data.concat(name + ": .word 0:20\n");
                break;

            }
            case "new_array":{
                String size= ( (Descriptor) semanticStack.pop() ).value.toString();
                String type = (String) semanticStack.pop();

                ArrayDescriptor ad=new ArrayDescriptor(type);
                ad.value=new Descriptor[Integer.parseInt(size)];
                ad.size=Integer.parseInt(size);

                for (int i = 0; i < Integer.parseInt(size); i++) {
                    if(ad.type.equals("int")){
                        Descriptor descriptor = new Descriptor("int");
                        descriptor.value = "0";
                        ad.value[i] = descriptor;
                    }
                    else {
                        Descriptor descriptor = new Descriptor("real");
                        descriptor.value = "0.0";
                        ad.value[i] = descriptor;
                    }
                }


                semanticStack.push(ad);

                //mips:)

                String value = "0:"+size;

                String name = "new_array"+newArrayCounter;

                if(type.equals("int"))
                    data = data.concat(name+": .word "+value+"\n");
                else data = data.concat(name+": .float "+value+"\n");

                text = text.concat("la $t8,new_array"+newArrayCounter+ "\n");
                symbolTable.put(name,new ArrayDescriptor("type"));
                symbolTable.get(name).setAddress("$t8");

                newArrayCounter++;


                break;
            }

            case "assign": {
                System.out.println("assign id running...");

                String value;
                //my code
                if (semanticStack.peek() instanceof ArrayDescriptor){
                    ArrayDescriptor new_dscp= (ArrayDescriptor) semanticStack.pop();
                    String name= (String) semanticStack.pop();
                    ArrayDescriptor old_dscp= (ArrayDescriptor) symbolTable.get(name);
                    if(!old_dscp.type.equals(new_dscp.type))
                        throw new TypeMismatchException("Array with "+new_dscp.type+" elements can not assign to "+name+" : "+old_dscp.type);
                    new_dscp.address = old_dscp.address;
                    symbolTable.put(name,new_dscp);
                    //mips
                    value = "0:"+new_dscp.size;

                    if (new_dscp.type.equals("int")){
                        String target = name+": .word "+"0:20";
                        String replace = name+": .word "+value;
                        data = data.replace(target,replace);
                    }
                    else {
                        String target = name+": .word "+"0:20";
                        String replace = name+": .float "+value;
                        data = data.replace(target,replace);
                    }

                    text = text.concat("move "+symbolTable.get(name).address+",$t8\n");
                    break;

                }
                //your code

                else if (semanticStack.peek() instanceof Descriptor) {
                    Descriptor d = (Descriptor) semanticStack.pop();
                    value = d.value.toString();

                     Object o = semanticStack.peek();

                     if (o instanceof ArrayClass){
                         if (symbolTable.get( ((ArrayClass) o).name ).type.equals("int")){
                             text = text.concat("li $t9," + value + "\n");
                         }
                         else if (symbolTable.get( ((ArrayClass) o).name ).type.equals("real")){
                             value = FtoHex(value);
                             text = text.concat("li $t9," + value + "\n");
                         }
                     }
                     else {
                         if (symbolTable.get(o).type.equals("int")){
                             text = text.concat("li $t9," + value + "\n");
                         }
                         else if (symbolTable.get(o).type.equals("real")){
                             String val = FtoHex(value);
                             text = text.concat("li $t9," + val + "\n");
                             text = text.concat("mtc1 $t9,$f29\n");
                         }
                         else if (symbolTable.get(o).type.equals("bool")){

                             if (value.equals("true")){
                                 text = text.concat("li $t9," + 0 + "\n");
                             }
                             else {
                                 text = text.concat("li $t9," + 1 + "\n");
                             }
                         }

                     }
                } else if (semanticStack.peek() instanceof ArrayClass){
                    ArrayClass d = (ArrayClass) semanticStack.pop();
                    value = ( (ArrayDescriptor) symbolTable.get(d.name) ).value[Integer.parseInt(d.index)].value.toString();


                    text = text.concat("li $s3,"+d.index+"\n" +
                            "addi $s3,$s3,1\n" +
                            "li $s2,4\n" +
                            "mul $s3,$s3,$s2\n" +
                            "subi $s3,$s3,4\n");

                    if (symbolTable.get(d.name).type.equals("int"))
                    text = text.concat("move $t8,"+( (ArrayDescriptor) symbolTable.get(d.name) ).address+"\n" +
                            "add $t8,$t8,$s3\n" +
                            "lw $t9,($t8)\n" +
                            "sub $t8,$t8,$s3\n" +
                            "move "+( (ArrayDescriptor) symbolTable.get(d.name) ).address+",$t8\n");

                    else if (symbolTable.get(d.name).type.equals("real"))
                        text = text.concat("move $t8,"+( (ArrayDescriptor) symbolTable.get(d.name) ).address+"\n" +
                                "add $t8,$t8,$s3\n" +
                                "l.s $f29,($t8)\n" +
                                "sub $t8,$t8,$s3\n" +
                                "move "+( (ArrayDescriptor) symbolTable.get(d.name) ).address+",$t8\n");

                }
                else
                    value = (String) semanticStack.pop();


                if (semanticStack.peek() instanceof ArrayClass){
                    ArrayClass d = (ArrayClass) semanticStack.pop();
                    Descriptor descriptor = new Descriptor(( (ArrayDescriptor) symbolTable.get(d.name) ).type);
                    descriptor.value = value;
                    ( (ArrayDescriptor) symbolTable.get(d.name) ).value[Integer.parseInt(d.index)] = descriptor;

                    text = text.concat("li $s1,"+d.index+"\n" +
                            "addi $s1,$s1,1\n" +
                            "li $s0,4\n" +
                            "mul $s1,$s1,$s0\n" +
                            "subi $s1,$s1,4\n");

                    if(symbolTable.get(d.name).type.equals("int"))
                    text = text.concat("move $t7,"+( (ArrayDescriptor) symbolTable.get(d.name) ).address+"\n" +
                            "add $t7,$t7,$s1\n" +
                            "sw $t9,($t7)\n" +
                            "sub $t7,$t7,$s1\n" +
                            "move "+( (ArrayDescriptor) symbolTable.get(d.name) ).address+",$t7\n");

                    else if(symbolTable.get(d.name).type.equals("real"))
                        text = text.concat("move $t7,"+( (ArrayDescriptor) symbolTable.get(d.name) ).address+"\n" +
                                "add $t7,$t7,$s1\n" +
                                "s.s $f29,($t7)\n" +
                                "sub $t7,$t7,$s1\n" +
                                "move "+( (ArrayDescriptor) symbolTable.get(d.name) ).address+",$t7\n");

                }
                else {
                    String name = (String) semanticStack.pop();
                    if (symbolTable.get(name).type.equals("int") ){
                        if (symbolTable.containsKey(value)) {
                            symbolTable.get(name).setValue(symbolTable.get(value).value);
                            if (!value.equals("$t10")){
                                text = text.concat("lw " + "$t9" + ",(" + symbolTable.get(value).address + ")\n");
                                text = text.concat("sw " + "$t9" + ",(" + symbolTable.get(name).address + ")\n");
                            }
                            else {
                                text = text.concat("sw " + "$t9" + ",(" + symbolTable.get(name).address + ")\n");
                            }
                        } else {
                            if (!value.equals("$t10")){
                                symbolTable.get(name).setValue(value);

                                text = text.concat("sw $t9,(" + symbolTable.get(name).address + ")\n");
                            }
                            else {
                                text = text.concat("sw " + "$t9" + ",(" + symbolTable.get(name).address + ")\n");
                            }
                        }
                    }
                    else if (symbolTable.get(name).type.equals("real")){
                        if (symbolTable.containsKey(value)) {
                            symbolTable.get(name).setValue(symbolTable.get(value).value);
                            if (!value.equals("$f32")){
                                text = text.concat("l.s " + "$f29" + ",(" + symbolTable.get(value).address + ")\n");
                                text = text.concat("s.s " + "$f29" + ",(" + symbolTable.get(name).address + ")\n");
                            }
                            else {
                                text = text.concat("s.s " + "$f29" + ",(" + symbolTable.get(name).address + ")\n");
                            }
                        } else {
                            if (!value.equals("$f32")){
                                symbolTable.get(name).setValue(value);
                                text = text.concat("s.s $f29,(" + symbolTable.get(name).address + ")\n");
                            }
                            else {
                                text = text.concat("s.s " + "$f29" + ",(" + symbolTable.get(name).address + ")\n");
                            }
                        }
                    }
                    else if (symbolTable.get(name).type.equals("string")){
                        if (symbolTable.containsKey(value)) {
                            symbolTable.get(name).setValue(symbolTable.get(value).value);
                            text = text.concat("move " + symbolTable.get(name).address + "," + symbolTable.get(value).address + "\n");
                        } else {
                            symbolTable.get(name).setValue(value);
                            //String target = name+": .asciiz \"\\n\"";
                            //String replace = name+": .asciiz \""+value+"\\n\"";
                            //data = data.replace(target,replace);
                            text = text.concat("la $t8,new_string"+newStringCounter+"\n");
                            text = text.concat("move "+symbolTable.get(name).address+",$t8\n");
                            data = data.concat("new_string"+newStringCounter+": .asciiz \""+value+"\\n\"\n");
                            newStringCounter++;
                        }
                    }
                    else if (symbolTable.get(name).type.equals("bool")){
                        if (symbolTable.containsKey(value)) {
                            symbolTable.get(name).setValue(symbolTable.get(value).value);
                            text = text.concat("lw $t9,(" + symbolTable.get(value).address + ")\n");
                            text = text.concat("sw $t9,(" + symbolTable.get(name).address + ")\n");
                        } else {
                            symbolTable.get(name).setValue(value);
                            if (value.equals("true")){
                                text = text.concat("li $t9,0\n");
                                text = text.concat("sw $t9,(" + symbolTable.get(name).address + ")\n");
                            }
                            else {
                                text = text.concat("li $t9,1\n");
                                text = text.concat("sw $t9,(" + symbolTable.get(name).address + ")\n");
                            }
                        }
                    }
                }

                break;
            }
            case "add": {
                System.out.println("add is running...");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();
                String t=checkType(o1,o2,"+","Add");

                if (t.equals("int")){
                    String var_name1;
                    String var_name2;
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("add $t9,$k0,$k1" + "\n");
                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("add $t9,$k0,$k1" + "\n");
                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("add $t9,$k0,$k1" + "\n");
                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("add $t9,$k0,$k1" + "\n");

                    }

                    int var3 = var1 + var2;

                    //symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.put("$t10", new Descriptor(t));

                    symbolTable.get("$t10").value = Integer.toString(var3);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (t.equals("real")){
                    String var_name1;
                    String var_name2;
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);

                        String var11 = FtoHex(var_name1);
                        text = text.concat("li $k0," + var11 + "\n");

                        String var12 = FtoHex(var_name1);
                        text = text.concat("li $k1," + var12 + "\n");

                        text = text.concat("mtc1 $k0,$f30\n");
                        text = text.concat("mtc1 $k1,$f31\n");
                        text = text.concat("add.s $f29,$f30,$f31" + "\n");
                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        String var = FtoHex(var_name1);
                        text = text.concat("li $k0," + var + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");
                        if (var_name2.equals("$f32")) {
                            text = text.concat("mov.s $f31,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f31,("+symbolTable.get(var_name2).address + ")\n");
                        }
                        text = text.concat("add.s $f29,$f31,$f30\n");
                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());

                        String var = FtoHex(var_name2);
                        text = text.concat("li $fk1," + var + "\n");
                        text = text.concat("mtc1 $k1,$f31\n");
                        if (var_name1.equals("$f32")) {
                            text = text.concat("mov.s $f30,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f31,("+symbolTable.get(var_name1).address + ")\n");
                        }
                        text = text.concat("add.s $f29,$f30,$f31\n");
                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32")) {
                            text = text.concat("mov.s $f30,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f30,("+symbolTable.get(var_name1).address + ")\n");
                        }
                        if (var_name2.equals("$f32")) {
                            text = text.concat("mov.s $f31,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f31,("+symbolTable.get(var_name2).address + ")\n");
                        }
                        text = text.concat("add.s $f29,$f30,$f31\n");
                    }

                    double var3 = var1 + var2;

                    //symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.put("$f32", new Descriptor(t));
                    symbolTable.get("$f32").value = Double.toString(var3);
                    symbolTable.get("$f32").address = "$f29";
                    semanticStack.push("$f32");
                }
                else if(t.equals("string")){
                    String var_name1;
                    String var_name2;

                    String var1;
                    String var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = var_name1;
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = var_name2;

                        data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o1).value +"\\n\""+"\n");
                        text = text.concat("la $t7,new_string"+newStringCounter+ "\n");
                        newStringCounter++;
                        data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o2).value +"\\n\""+"\n");
                        text = text.concat("la $t8,new_string"+newStringCounter+ "\n");
                        newStringCounter++;

                        text = text.concat(
                                        "li $s0,0\n" +
                                        "loop"+loopcunter+":\n" +
                                        "lb $k1,($t8)\n" +
                                        "lb $t9, endline\n" +
                                        "beq $k1, $t9, loop"+ ++loopcunter +"\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "addi $s0,$s0,1\n" +
                                        "j loop"+ --loopcunter +"\n" +
                                        "loop"+ ++loopcunter +":\n" +
                                        "lb $k1,($t7)\n" +
                                        "sb $k1,($t8)\n" +
                                        "addi $t7,$t7,1\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "addi $s0,$s0,1\n" +
                                        "lb $t9,endline\n" +
                                        "beq $k1,$t9,end"+endcounter+"\n" +
                                        "j loop"+loopcunter+"\n" +
                                        "end"+endcounter+":\n" +
                                        "addi $t7,$t7,1\n" +
                                        "lb $k1,($t7)\n" +
                                        "sb $k1,($t8)\n" +
                                        "sub $t8,$t8,$s0\n");

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = var_name1;
                        var_name2 = o2.toString();
                        var2 = symbolTable.get(var_name2).value.toString();

                        data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o1).value +"\\n\""+"\n");
                        text = text.concat("la $t7,new_string"+newStringCounter+ "\n");
                        text = text.concat("move $t8,"+symbolTable.get(var_name2).address+"\n");
                        newStringCounter++;

                        text = text.concat(
                                "li $s0,0\n" +
                                        "loop"+ loopcunter +":\n" +
                                        "lb $k1,($t8)\n" +
                                        "lb $t9, endline\n" +
                                        "beq $k1, $t9, loop"+ ++loopcunter +"\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "addi $s0,$s0,1\n" +
                                        "j loop"+ --loopcunter +"\n" +
                                        "loop"+ ++loopcunter +":\n" +
                                        "lb $k1,($t7)\n" +
                                        "sb $k1,($t8)\n" +
                                        "addi $t7,$t7,1\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "addi $s0,$s0,1\n" +
                                        "lb $t9,endline\n" +
                                        "beq $k1,$t9,end"+endcounter+"\n" +
                                        "j loop"+ loopcunter +"\n" +
                                        "end"+endcounter+":\n" +
                                        "addi $t7,$t7,1\n" +
                                        "lb $k1,($t7)\n" +
                                        "sb $k1,($t8)\n" +
                                        "sub $t8,$t8,$s0\n");

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = var_name2;
                        var_name1 = o1.toString();
                        var1 = symbolTable.get(var_name1).value.toString();

                        text = text.concat("move $t7,"+symbolTable.get(var_name1).address+"\n");
                        data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o2).value +"\\n\""+"\n");
                        text = text.concat("la $t8,new_string"+newStringCounter+ "\n");
                        newStringCounter++;

                        text = text.concat(
                                        "li $s0,0\n" +
                                        "loop"+ loopcunter +":\n" +
                                        "lb $k1,($t8)\n" +
                                        "lb $t9, endline\n" +
                                        "beq $k1, $t9, loop"+ ++loopcunter +"\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "addi $s0,$s0,1\n" +
                                        "j loop"+ --loopcunter +"\n" +
                                        "loop"+ ++loopcunter +":\n" +
                                        "lb $k1,($t7)\n" +
                                        "sb $k1,($t8)\n" +
                                        "addi $t7,$t7,1\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "addi $s0,$s0,1\n" +
                                        "lb $t9,endline\n" +
                                        "beq $k1,$t9,end"+endcounter+"\n" +
                                        "j loop"+ loopcunter +"\n" +
                                        "end"+endcounter+":\n" +
                                        "addi $t7,$t7,1\n" +
                                        "lb $k1,($t7)\n" +
                                        "sb $k1,($t8)\n" +
                                        "sub $t8,$t8,$s0\n");

                    } else {
                        var_name1 = o1.toString();
                        var1 = symbolTable.get(var_name1).value.toString();
                        var_name2 = o2.toString();
                        var2 = symbolTable.get(var_name2).value.toString();

                        text = text.concat(
                                "move $t7,"+symbolTable.get(var_name1).address+"\n" +
                                        "move $t8,"+symbolTable.get(var_name2).address+"\n" +
                                        "li $s0,0\n" +
                                        "loop"+ loopcunter +":\n" +
                                        "lb $k1,($t8)\n" +
                                        "lb $t9, endline\n" +
                                        "beq $k1, $t9, loop"+ ++loopcunter +"\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "addi $s0,$s0,1\n" +
                                        "j loop"+ --loopcunter +"\n" +
                                        "loop"+ ++loopcunter +":\n" +
                                        "lb $k1,($t7)\n" +
                                        "sb $k1,($t8)\n" +
                                        "addi $t7,$t7,1\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "addi $s0,$s0,1\n" +
                                        "lb $t9,endline\n" +
                                        "beq $k1,$t9,end"+endcounter+"\n" +
                                        "j loop"+ loopcunter +"\n" +
                                        "end"+endcounter+":\n" +
                                        "addi $t7,$t7,1\n" +
                                        "lb $k1,($t7)\n" +
                                        "sb $k1,($t8)\n" +
                                        "sub $t8,$t8,$s0\n");

                    }

                    endcounter++;
                    loopcunter++;


                    String s = var2.concat(var1);
                    Descriptor d = new Descriptor("string");
                    d.setValue(s);
                    d.setAddress("$t8");
                    semanticStack.push(d);
                }

                break;
            }
            case "sub": {
                System.out.println("sub is running...");
                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();
                String var_name1;
                String var_name2;
                String type = checkType(o1,o2,"-","Subtract");

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("sub $t9,$k1,$k0" + "\n");
                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("sub $t9,$k1,$k0" + "\n");
                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("sub $t9,$k1,$k0" + "\n");
                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("sub $t9,$k1,$k0" + "\n");

                    }

                    int var3 = var2 - var1;
                    symbolTable.put("$t10", new Descriptor(type));
                    symbolTable.get("$t10").value = Integer.toString(var3);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (type.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);

                        var_name1 = FtoHex(var_name1);
                        var_name2 = FtoHex(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");
                        text = text.concat("mtc1 $k1,$f31\n");
                        text = text.concat("sub.s $f29,$f31,$f30" + "\n");
                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        String var = FtoHex(var_name1);
                        text = text.concat("li $k0," + var + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");
                        if (var_name2.equals("$f32")) {
                            text = text.concat("mov.s $f31,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f31,("+symbolTable.get(var_name2).address + ")\n");
                        }
                        text = text.concat("sub.s $f29,$f31,$f30\n");
                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());

                        String var = FtoHex(var_name2);
                        text = text.concat("li $k1," + var + "\n");
                        text = text.concat("mtc1 $k1,$f31\n");
                        if (var_name1.equals("$f32")) {
                            text = text.concat("mov.s $f30,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f30,("+symbolTable.get(var_name1).address + ")\n");
                        }
                        text = text.concat("sub.s $f29,$f31,$f30\n");
                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32")) {
                            text = text.concat("mov.s $f30,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f30,("+symbolTable.get(var_name1).address + ")\n");
                        }
                        if (var_name2.equals("$f32")) {
                            text = text.concat("mov.s $f31,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f31,("+symbolTable.get(var_name2).address + ")\n");
                        }
                        text = text.concat("sub.s $f29,$f31,$f30\n");
                    }

                    double var3 = var2 - var1;

                    //symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.put("$f32", new Descriptor(type));
                    symbolTable.get("$f32").value = Double.toString(var3);
                    symbolTable.get("$f32").address = "$f29";
                    semanticStack.push("$f32");
                }

                break;
            }
            case "mult": {
                System.out.println("mult is running...");
                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();
                String var_name1;
                String var_name2;

                String type = checkType(o1,o2,"*","Multipile");

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mul $t9,$k0,$k1" + "\n");
                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("mul $t9,$k0,$k1" + "\n");
                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mul $t9,$k0,$k1" + "\n");
                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("mul $t9,$k0,$k1" + "\n");

                    }

                    int var3 = var1 * var2;
                    symbolTable.put("$t10", new Descriptor(type));
                    symbolTable.get("$t10").value = Integer.toString(var3);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (type.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);

                        var_name1 = FtoHex(var_name1);
                        var_name2 = FtoHex(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");
                        text = text.concat("mtc1 $k1,$f31\n");
                        text = text.concat("mul.s $f29,$f30,$f31" + "\n");
                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        String var = FtoHex(var_name1);
                        text = text.concat("li $k0," + var + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");
                        if (var_name2.equals("$f32")) {
                            text = text.concat("mov.s $f31,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f31,("+symbolTable.get(var_name2).address + ")\n");
                        }
                        text = text.concat("mul.s $f29,$f31,$f30\n");
                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());

                        String var = FtoHex(var_name2);
                        text = text.concat("li $k1," + var + "\n");
                        text = text.concat("mtc1 $k1,$f31\n");
                        if (var_name1.equals("$f32")) {
                            text = text.concat("mov.s $f30,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f30,("+symbolTable.get(var_name1).address + ")\n");
                        }
                        text = text.concat("mul.s $f29,$f30,$f31\n");
                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32")) {
                            text = text.concat("mov.s $f30,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f30,("+symbolTable.get(var_name1).address + ")\n");
                        }
                        if (var_name2.equals("$f32")) {
                            text = text.concat("mov.s $f31,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f31,("+symbolTable.get(var_name2).address + ")\n");
                        }
                        text = text.concat("mul.s $f29,$f30,$f31\n");
                    }

                    double var3 = var2 * var1;

                    //symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.put("$f32", new Descriptor(type));
                    symbolTable.get("$f32").value = Double.toString(var3);
                    symbolTable.get("$f32").address = "$f29";
                    semanticStack.push("$f32");
                }

                break;
            }
            case "div": {
                System.out.println("div is running...");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();
                String var_name1;
                String var_name2;

                String type = checkType(o1,o2,"/","Division");

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("div $t9,$k1,$k0" + "\n");
                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());


                        text = text.concat("li $k0," + var_name1 + "\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("div $t9,$k1,$k0" + "\n");
                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("div $t9,$k1,$k0" + "\n");
                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("div $t9,$k1,$k0" + "\n");

                    }

                    int var3 = var2 / var1;
                    symbolTable.put("$t10", new Descriptor(type));
                    symbolTable.get("$t10").value = Integer.toString(var3);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (type.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);

                        var_name1 = FtoHex(var_name1);
                        var_name2 = FtoHex(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");
                        text = text.concat("mtc1 $k1,$f31\n");
                        text = text.concat("div.s $f29,$f31,$f30" + "\n");
                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        String var = FtoHex(var_name1);
                        text = text.concat("li $k0," + var + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");
                        if (var_name2.equals("$f32")) {
                            text = text.concat("mov.s $f31,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f31,("+symbolTable.get(var_name2).address + ")\n");
                        }
                        text = text.concat("div.s $f29,$f31,$f30\n");
                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());

                        String var = FtoHex(var_name2);
                        text = text.concat("li $k1," + var + "\n");
                        text = text.concat("mtc1 $k1,$f31\n");
                        if (var_name1.equals("$f32")) {
                            text = text.concat("mov.s $f30,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f30,("+symbolTable.get(var_name1).address + ")\n");
                        }
                        text = text.concat("div.s $f29,$f31,$f30\n");
                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32")) {
                            text = text.concat("mov.s $f30,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f30,("+symbolTable.get(var_name1).address + ")\n");
                        }
                        if (var_name2.equals("$f32")) {
                            text = text.concat("mov.s $f31,$f29\n");
                        }
                        else
                        {
                            text = text.concat("l.s $f31,("+symbolTable.get(var_name2).address + ")\n");
                        }
                        text = text.concat("div.s $f29,$f31,$f30\n");
                    }

                    double var3 = var2 / var1;

                    //symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.put("$f32", new Descriptor(type));
                    symbolTable.get("$f32").value = Double.toString(var3);
                    symbolTable.get("$f32").address = "$f29";
                    semanticStack.push("$f32");
                }

                break;
            }
            case "mod": {
                System.out.println("mod is running...");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();
                String var_name1;
                String var_name2;

                String type = checkType(o1,o2,"%","Modulus");

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("div $t9,$k1,$k0" + "\n");
                        text = text.concat("mfhi $t9\n");
                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("div $t9,$k1,$k0" + "\n");
                        text = text.concat("mfhi $t9\n");
                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("div $t9,$k1,$k0" + "\n");
                        text = text.concat("mfhi $t9\n");
                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        text = text.concat("div $t9,$k1,$k0" + "\n");
                        text = text.concat("mfhi $t9\n");

                    }

                    int var3 = var2 % var1;
                    symbolTable.put("$t10", new Descriptor(type));
                    symbolTable.get("$t10").value = Integer.toString(var3);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");

                }
                else if (type.equals("real")){

                }

                break;
            }
            case "pop": {
                if (!semanticStack.empty()) {
                    semanticStack.pop();
                    System.out.println("Warning::unreachable code");
                }
                break;
            }
            case "plusassign":{
                System.out.println("plusassign is running...");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();
                String var_name1;
                String var_name2;

                String t=checkType(o1,o2,"+=","plusassign");

                if (t.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("add $t9,$k1,$k0" + "\n");
                        text = text.concat("sw $t9,(" + symbolTable.get(var_name2).address + ")\n");

                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("add $t9,$k1,$k0" + "\n");
                        text = text.concat("sw $t9,(" + symbolTable.get(var_name2).address + ")\n");
                    }

                    var2 = var2 + var1;
                    symbolTable.get(var_name2).value = Integer.toString(var2);

                    symbolTable.put("$t10", new Descriptor(t));
                    symbolTable.get("$t10").value = Integer.toString(var2);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (t.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        String var = FtoHex(var_name1);
                        text = text.concat("li $k0," + var + "\n");
                        text = text.concat("mtc1 $k0,$f30" + "\n");
                        text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("add.s $f29,$f31,$f30" + "\n");
                        text = text.concat("s.s $f29,(" + symbolTable.get(var_name2).address + ")\n");

                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("add.s $f29,$f31,$f30" + "\n");
                        text = text.concat("s.s $f29,(" + symbolTable.get(var_name2).address + ")\n");
                    }

                    var2 = var2 + var1;
                    symbolTable.get(var_name2).value = Double.toString(var2);
                    symbolTable.put("$f32", new Descriptor(t));
                    symbolTable.get("$f32").value = Double.toString(var2);
                    symbolTable.get("$f32").address = "$f29";
                    semanticStack.push("$f32");

                }

                break;
            }
            case "minusassign":{
                System.out.println("minusassign is running...");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();
                String var_name1;
                String var_name2;

                String type = checkType(o1,o2,"-=","minusassign");

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("sub $t9,$k1,$k0" + "\n");
                        text = text.concat("sw $t9,(" + symbolTable.get(var_name2).address + ")\n");

                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("sub $t9,$k1,$k0" + "\n");
                        text = text.concat("sw $t9,(" + symbolTable.get(var_name2).address + ")\n");
                    }

                    var2 = var2 - var1;
                    symbolTable.get(var_name2).value = Integer.toString(var2);

                    symbolTable.put("$t10", new Descriptor(type));
                    symbolTable.get("$t10").value = Integer.toString(var2);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (type.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        String var = FtoHex(var_name1);
                        text = text.concat("li $k0," + var + "\n");
                        text = text.concat("mtc1 $k0,$f30" + "\n");
                        text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("sub.s $f29,$f31,$f30" + "\n");
                        text = text.concat("s.s $f29,(" + symbolTable.get(var_name2).address + ")\n");

                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("sub.s $f29,$f31,$f30" + "\n");
                        text = text.concat("s.s $f29,(" + symbolTable.get(var_name2).address + ")\n");
                    }

                    var2 = var2 - var1;
                    symbolTable.get(var_name2).value = Double.toString(var2);
                    symbolTable.get(var_name2).value = Double.toString(var2);
                    symbolTable.put("$f32", new Descriptor(type));
                    symbolTable.get("$f32").value = Double.toString(var2);
                    symbolTable.get("$f32").address = "$f29";
                    semanticStack.push("$f32");

                }

                break;
            }
            case "multassign":{
                System.out.println("multassign is running...");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();
                String var_name1;
                String var_name2;

                String type = checkType(o1,o2,"*=","multassign");

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("mul $t9,$k1,$k0" + "\n");
                        text = text.concat("sw $t9,(" + symbolTable.get(var_name2).address + ")\n");

                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("mul $t9,$k1,$k0" + "\n");
                        text = text.concat("sw $t9,(" + symbolTable.get(var_name2).address + ")\n");
                    }

                    var2 = var2 * var1;
                    symbolTable.get(var_name2).value = Integer.toString(var2);

                    symbolTable.put("$t10", new Descriptor(type));
                    symbolTable.get("$t10").value = Integer.toString(var2);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (type.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        String var = FtoHex(var_name1);
                        text = text.concat("li $k0," + var + "\n");
                        text = text.concat("mtc1 $k0,$f30" + "\n");
                        text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("mul.s $f29,$f31,$f30" + "\n");
                        text = text.concat("s.s $f29,(" + symbolTable.get(var_name2).address + ")\n");

                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("mul.s $f29,$f31,$f30" + "\n");
                        text = text.concat("s.s $f29,(" + symbolTable.get(var_name2).address + ")\n");
                    }

                    var2 = var2 * var1;
                    symbolTable.get(var_name2).value = Double.toString(var2);
                    symbolTable.get(var_name2).value = Double.toString(var2);
                    symbolTable.put("$f32", new Descriptor(type));
                    symbolTable.get("$f32").value = Double.toString(var2);
                    symbolTable.get("$f32").address = "$f29";
                    semanticStack.push("$f32");
                }

                break;
            }
            case "divassign":{
                System.out.println("divassign is running...");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();
                String var_name1;
                String var_name2;

                String type = checkType(o1,o2,"/=","divassign");

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("div $t9,$k1,$k0" + "\n");
                        text = text.concat("sw $t9,(" + symbolTable.get(var_name2).address + ")\n");

                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("div $t9,$k1,$k0" + "\n");
                        text = text.concat("sw $t9,(" + symbolTable.get(var_name2).address + ")\n");
                    }

                    var2 = var2 / var1;
                    symbolTable.get(var_name2).setValue(Integer.toString(var2));

                    symbolTable.put("$t10", new Descriptor(type));
                    symbolTable.get("$t10").value = Integer.toString(var2);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (type.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        String var = FtoHex(var_name1);
                        text = text.concat("li $k0," + var + "\n");
                        text = text.concat("mtc1 $k0,$f30" + "\n");
                        text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("div.s $f29,$f31,$f30" + "\n");
                        text = text.concat("s.s $f29,(" + symbolTable.get(var_name2).address + ")\n");

                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");
                        text = text.concat("div.s $f29,$f31,$f30" + "\n");
                        text = text.concat("s.s $f29,(" + symbolTable.get(var_name2).address + ")\n");
                    }

                    var2 = var2 / var1;
                    symbolTable.get(var_name2).value = Double.toString(var2);
                    symbolTable.get(var_name2).value = Double.toString(var2);
                    symbolTable.put("$f32", new Descriptor(type));
                    symbolTable.get("$f32").value = Double.toString(var2);
                    symbolTable.get("$f32").address = "$f29";
                    semanticStack.push("$f32");
                }

                break;
            }
            case "equal":{
                System.out.println("equal is running");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();

                String type = checkType(o1,o2,"==","equal");

                String var_name1;
                String var_name2;

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("beq $k0,$k1,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bne $k0,$k1,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("beq $k0,$k1,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bne $k0,$k1,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        text = text.concat("li $k1," + var_name2 + "\n");

                        text = text.concat("beq $k0,$k1,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bne $k0,$k1,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("beq $k0,$k1,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bne $k0,$k1,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    }

                    Descriptor d = new Descriptor("bool");
                    if (var1 == var2)
                        d.setValue(0);
                    else d.setValue(1);

                    semanticStack.push(d);
                }
                else if (type.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);

                        var_name1 = FtoHex(var_name1);
                        var_name2 = FtoHex(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n" +
                                "mtc1 $k1,$f31\n" +
                                "c.eq.s $f30,$f31 \n");
                        text = text.concat("bc1t label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1f label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        var_name1 = FtoHex(var_name1);
                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");

                        if (var_name2.equals("$f32"))
                            text = text.concat("mov.s $f31,$f29\n");
                        else
                            text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("c.eq.s $f30,$f31\n");

                        text = text.concat("bc1t ,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1f ,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov. $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        var_name2 = FtoHex(var_name2);
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k1,$f31\n");

                        text = text.concat("c.eq.s $f30,$f31\n");

                        text = text.concat("bc1t ,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1f ,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f31,$f29\n");
                        else
                            text = text.concat("l.s $f31,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("c.eq.s $f30,$f31\n");

                        text = text.concat("bc1t ,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1f ,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    }

                    Descriptor d = new Descriptor("bool");
                    if (Double.compare(var1,var2) == 0)
                        d.setValue(0);
                    else d.setValue(1);

                    semanticStack.push(d);
                }
                else if (type.equals("string")){

                    String var1;
                    String var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = var_name1;
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = var_name2;

                        data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o1).value +"\\n\""+"\n");
                        text = text.concat("la $t7,new_string"+newStringCounter+ "\n");
                        newStringCounter++;
                        data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o2).value +"\\n\""+"\n");
                        text = text.concat("la $t8,new_string"+newStringCounter+ "\n");
                        newStringCounter++;

                        text = text.concat(
                                "loop"+loopcunter+":\n" +
                                        "lb $k0, 0($t7)\n" +
                                        "lb $k1, 0($t8)\n" +
                                        "sub $t9, $k0, $k1\n" +
                                        "beqz $t9, continueEqual"+continueEqualcounter+"\n" +
                                        "j end_loop"+end_loopcounter+"\n" +
                                        "continueEqual"+continueEqualcounter+":\n" +
                                        "lb $t6, endline\n" +
                                        "beq $k0, $t6, end_loop"+end_loopcounter+"\n" +
                                        "addi $t7, $t7, 1\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "j loop"+loopcunter+"\n" +
                                        "end_loop"+end_loopcounter+":\n" +
                                        "beqz $t9, same"+samecounter+"\n" +
                                        "notSame"+notsamecounter+":\n" +
                                        "li $t9,1\n" +
                                        "j end"+endcounter+"\n" +
                                        "same"+samecounter+":\n" +
                                        "li $t9,0\n" +
                                        "end"+endcounter+":\n");

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = var_name1;
                        var_name2 = o2.toString();
                        var2 = symbolTable.get(var_name2).value.toString();

                        data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o1).value +"\\n\""+"\n");
                        text = text.concat("la $t7,new_string"+newStringCounter+ "\n");
                        text = text.concat("move $t8,"+symbolTable.get(var_name2).address+"\n");
                        newStringCounter++;

                        text = text.concat(
                                "loop"+loopcunter+":\n" +
                                "lb $k0, 0($t7)\n" +
                                "lb $k1, 0($t8)\n" +
                                "sub $t9, $k0, $k1\n" +
                                "beqz $t9, continueEqual"+continueEqualcounter+"\n" +
                                "j end_loop"+end_loopcounter+"\n" +
                                "continueEqual"+continueEqualcounter+":\n" +
                                "lb $t6, endline\n" +
                                "beq $k0, $t6, end_loop"+end_loopcounter+"\n" +
                                "addi $t7, $t7, 1\n" +
                                "addi $t8, $t8, 1\n" +
                                "j loop"+loopcunter+"\n" +
                                "end_loop"+end_loopcounter+":\n" +
                                "beqz $t9, same"+samecounter+"\n" +
                                "notSame"+notsamecounter+":\n" +
                                "li $t9,1\n" +
                                "j end"+endcounter+"\n" +
                                "same"+samecounter+":\n" +
                                "li $t9,0\n" +
                                "end"+endcounter+":\n");

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = var_name2;
                        var_name1 = o1.toString();
                        var1 = symbolTable.get(var_name1).value.toString();

                        text = text.concat("move $t7,"+symbolTable.get(var_name1).address+"\n");
                        data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o2).value +"\\n\""+"\n");
                        text = text.concat("la $t8,new_string"+newStringCounter+ "\n");
                        newStringCounter++;

                        text = text.concat(
                                "loop"+loopcunter+":\n" +
                                "lb $k0, 0($t7)\n" +
                                "lb $k1, 0($t8)\n" +
                                "sub $t9, $k0, $k1\n" +
                                "beqz $t9, continueEqual"+continueEqualcounter+"\n" +
                                "j end_loop"+end_loopcounter+"\n" +
                                "continueEqual"+continueEqualcounter+":\n" +
                                "lb $t6, endline\n" +
                                "beq $k0, $t6, end_loop"+end_loopcounter+"\n" +
                                "addi $t7, $t7, 1\n" +
                                "addi $t8, $t8, 1\n" +
                                "j loop"+loopcunter+"\n" +
                                "end_loop"+end_loopcounter+":\n" +
                                "beqz $t9, same"+samecounter+"\n" +
                                "notSame"+notsamecounter+":\n" +
                                "li $t9,1\n" +
                                "j end"+endcounter+"\n" +
                                "same"+samecounter+":\n" +
                                "li $t9,0\n" +
                                "end"+endcounter+":\n");

                    } else {
                        var_name1 = o1.toString();
                        var1 = symbolTable.get(var_name1).value.toString();
                        var_name2 = o2.toString();
                        var2 = symbolTable.get(var_name2).value.toString();

                        text = text.concat(
                                "move $t7,"+symbolTable.get(var_name1).address+"\n" +
                                "move $t8,"+symbolTable.get(var_name2).address+"\n" +
                                "loop"+loopcunter+":\n" +
                                "lb $k0, 0($t7)\n" +
                                "lb $k1, 0($t8)\n" +
                                "sub $t9, $k0, $k1\n" +
                                "beqz $t9, continueEqual"+continueEqualcounter+"\n" +
                                "j end_loop"+end_loopcounter+"\n" +
                                "continueEqual"+continueEqualcounter+":\n" +
                                "lb $t6, endline\n" +
                                "beq $k0, $t6, end_loop"+end_loopcounter+"\n" +
                                "addi $t7, $t7, 1\n" +
                                "addi $t8, $t8, 1\n" +
                                "j loop"+loopcunter+"\n" +
                                "end_loop"+end_loopcounter+":\n" +
                                "beqz $t9, same"+samecounter+"\n" +
                                "notSame"+notsamecounter+":\n" +
                                "li $t9,1\n" +
                                "j end"+endcounter+"\n" +
                                "same"+samecounter+":\n" +
                                "li $t9,0\n" +
                                "end"+endcounter+":\n");

                    }

                    endcounter++;
                    loopcunter++;
                    samecounter++;
                    notsamecounter++;
                    end_loopcounter++;
                    continueEqualcounter++;

                    Descriptor d = new Descriptor("bool");
                    if (var1.equals(var2))
                        d.setValue(0);
                    else d.setValue(1);

                }

                break;
            }
            case "notequal":{
                System.out.println("notequal is running");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();

                String type = checkType(o1,o2,"!=","notequal");

                String var_name1;
                String var_name2;

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("beq $k0,$k1,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bne $k0,$k1,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("beq $k0,$k1,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bne $k0,$k1,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        text = text.concat("li $k1," + var_name2 + "\n");

                        text = text.concat("beq $k0,$k1,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bne $k0,$k1,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("beq $k0,$k1,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bne $k0,$k1,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    }

                    Descriptor d = new Descriptor("bool");
                    if (var1 != var2)
                        d.setValue(0);
                    else d.setValue(1);

                    semanticStack.push(d);
                }
                else if (type.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);

                        var_name1 = FtoHex(var_name1);
                        var_name2 = FtoHex(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n" +
                                "mtc1 $k1,$f31\n" +
                                "c.eq.s $f30,$f31 \n");
                        text = text.concat("bc1f label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1t label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        var_name1 = FtoHex(var_name1);
                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");

                        if (var_name2.equals("$f32"))
                            text = text.concat("mov.s $f31,$f29\n");
                        else
                            text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("c.eq.s $f30,$f31\n");

                        text = text.concat("bc1f label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1t label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov. $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        var_name2 = FtoHex(var_name2);
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k1,$f31\n");

                        text = text.concat("c.eq.s $f30,$f31\n");

                        text = text.concat("bc1f label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1t label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f31,$f29\n");
                        else
                            text = text.concat("l.s $f31,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("c.eq.s $f30,$f31\n");

                        text = text.concat("bc1f label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1t label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    }

                    Descriptor d = new Descriptor("bool");
                    if (Double.compare(var1,var2) != 0)
                        d.setValue(0);
                    else d.setValue(1);

                    semanticStack.push(d);
                }
                else if (type.equals("string")){

                    String var1;
                    String var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = var_name1;
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = var_name2;

                        data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o1).value +"\\n\""+"\n");
                        text = text.concat("la $t7,new_string"+newStringCounter+ "\n");
                        newStringCounter++;
                        data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o2).value +"\\n\""+"\n");
                        text = text.concat("la $t8,new_string"+newStringCounter+ "\n");
                        newStringCounter++;

                        text = text.concat(
                                "loop"+loopcunter+":\n" +
                                        "lb $k0, 0($t7)\n" +
                                        "lb $k1, 0($t8)\n" +
                                        "sub $t9, $k0, $k1\n" +
                                        "beqz $t9, continueEqual"+continueEqualcounter+"\n" +
                                        "j end_loop"+end_loopcounter+"\n" +
                                        "continueEqual"+continueEqualcounter+":\n" +
                                        "lb $t6, endline\n" +
                                        "beq $k0, $t6, end_loop"+end_loopcounter+"\n" +
                                        "addi $t7, $t7, 1\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "j loop"+loopcunter+"\n" +
                                        "end_loop"+end_loopcounter+":\n" +
                                        "beqz $t9, same"+samecounter+"\n" +
                                        "notSame"+notsamecounter+":\n" +
                                        "li $t9,0\n" +
                                        "j end"+endcounter+"\n" +
                                        "same"+samecounter+":\n" +
                                        "li $t9,1\n" +
                                        "end"+endcounter+":\n");

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = var_name1;
                        var_name2 = o2.toString();
                        var2 = symbolTable.get(var_name2).value.toString();

                        data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o1).value +"\\n\""+"\n");
                        text = text.concat("la $t7,new_string"+newStringCounter+ "\n");
                        text = text.concat("move $t8,"+symbolTable.get(var_name2).address+"\n");
                        newStringCounter++;

                        text = text.concat(
                                "loop"+loopcunter+":\n" +
                                        "lb $k0, 0($t7)\n" +
                                        "lb $k1, 0($t8)\n" +
                                        "sub $t9, $k0, $k1\n" +
                                        "beqz $t9, continueEqual"+continueEqualcounter+"\n" +
                                        "j end_loop"+end_loopcounter+"\n" +
                                        "continueEqual"+continueEqualcounter+":\n" +
                                        "lb $t6, endline\n" +
                                        "beq $k0, $t6, end_loop"+end_loopcounter+"\n" +
                                        "addi $t7, $t7, 1\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "j loop"+loopcunter+"\n" +
                                        "end_loop"+end_loopcounter+":\n" +
                                        "beqz $t9, same"+samecounter+"\n" +
                                        "notSame"+notsamecounter+":\n" +
                                        "li $t9,0\n" +
                                        "j end"+endcounter+"\n" +
                                        "same"+samecounter+":\n" +
                                        "li $t9,1\n" +
                                        "end"+endcounter+":\n");

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = var_name2;
                        var_name1 = o1.toString();
                        var1 = symbolTable.get(var_name1).value.toString();

                        text = text.concat("move $t7,"+symbolTable.get(var_name1).address+"\n");
                        data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o2).value +"\\n\""+"\n");
                        text = text.concat("la $t8,new_string"+newStringCounter+ "\n");
                        newStringCounter++;

                        text = text.concat(
                                "loop"+loopcunter+":\n" +
                                        "lb $k0, 0($t7)\n" +
                                        "lb $k1, 0($t8)\n" +
                                        "sub $t9, $k0, $k1\n" +
                                        "beqz $t9, continueEqual"+continueEqualcounter+"\n" +
                                        "j end_loop"+end_loopcounter+"\n" +
                                        "continueEqual"+continueEqualcounter+":\n" +
                                        "lb $t6, endline\n" +
                                        "beq $k0, $t6, end_loop"+end_loopcounter+"\n" +
                                        "addi $t7, $t7, 1\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "j loop"+loopcunter+"\n" +
                                        "end_loop"+end_loopcounter+":\n" +
                                        "beqz $t9, same"+samecounter+"\n" +
                                        "notSame"+notsamecounter+":\n" +
                                        "li $t9,0\n" +
                                        "j end"+endcounter+"\n" +
                                        "same"+samecounter+":\n" +
                                        "li $t9,1\n" +
                                        "end"+endcounter+":\n");

                    } else {
                        var_name1 = o1.toString();
                        var1 = symbolTable.get(var_name1).value.toString();
                        var_name2 = o2.toString();
                        var2 = symbolTable.get(var_name2).value.toString();

                        text = text.concat(
                                "move $t7,"+symbolTable.get(var_name1).address+"\n" +
                                        "move $t8,"+symbolTable.get(var_name2).address+"\n" +
                                        "loop"+loopcunter+":\n" +
                                        "lb $k0, 0($t7)\n" +
                                        "lb $k1, 0($t8)\n" +
                                        "sub $t9, $k0, $k1\n" +
                                        "beqz $t9, continueEqual"+continueEqualcounter+"\n" +
                                        "j end_loop"+end_loopcounter+"\n" +
                                        "continueEqual"+continueEqualcounter+":\n" +
                                        "lb $t6, endline\n" +
                                        "beq $k0, $t6, end_loop"+end_loopcounter+"\n" +
                                        "addi $t7, $t7, 1\n" +
                                        "addi $t8, $t8, 1\n" +
                                        "j loop"+loopcunter+"\n" +
                                        "end_loop"+end_loopcounter+":\n" +
                                        "beqz $t9, same"+samecounter+"\n" +
                                        "notSame"+notsamecounter+":\n" +
                                        "li $t9,0\n" +
                                        "j end"+endcounter+"\n" +
                                        "same"+samecounter+":\n" +
                                        "li $t9,1\n" +
                                        "end"+endcounter+":\n");

                    }

                    endcounter++;
                    loopcunter++;
                    samecounter++;
                    notsamecounter++;
                    end_loopcounter++;
                    continueEqualcounter++;

                    Descriptor d = new Descriptor("bool");
                    if (!var1.equals(var2))
                        d.setValue(0);
                    else d.setValue(1);

                }



                break;
            }
            case "little":{
                System.out.println("little is running");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();

                String type = checkType(o1,o2,"<","little");

                String var_name1;
                String var_name2;

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("blt $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bge $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("blt $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bge $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        text = text.concat("li $k1," + var_name2 + "\n");

                        text = text.concat("blt $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bge $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("blt $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bge $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    }

                    Descriptor d = new Descriptor("bool");
                    if (var2 < var1)
                        d.setValue(0);
                    else d.setValue(1);

                    semanticStack.push(d);
                }
                else if (type.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);

                        var_name1 = FtoHex(var_name1);
                        var_name2 = FtoHex(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n" +
                                "mtc1 $k1,$f31\n" +
                                "c.lt.s $f31,$f30\n");
                        text = text.concat("bc1t label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1f label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        var_name1 = FtoHex(var_name1);
                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");

                        if (var_name2.equals("$f32"))
                            text = text.concat("mov.s $f31,$f29\n");
                        else
                            text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("c.lt.s $f31,$f30\n");

                        text = text.concat("bc1t label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1f label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov. $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        var_name2 = FtoHex(var_name2);
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k1,$f31\n");

                        text = text.concat("c.lt.s $f31,$f30\n");

                        text = text.concat("bc1t label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1f label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f31,$f29\n");
                        else
                            text = text.concat("l.s $f31,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("c.lt.s $f31,$f30\n");

                        text = text.concat("bc1t label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1f label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    }

                    Descriptor d = new Descriptor("bool");
                    if (Double.compare(var2,var1) < 0)
                        d.setValue(0);
                    else d.setValue(1);

                    semanticStack.push(d);
                }


                break;
            }
            case "bigger":{
                System.out.println("bigger is running");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();

                String type = checkType(o1,o2,">","bigger");

                String var_name1;
                String var_name2;

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("bgt $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("ble $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("bgt $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("ble $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        text = text.concat("li $k1," + var_name2 + "\n");

                        text = text.concat("bgt $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("ble $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("bgt $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("ble $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    }

                    Descriptor d = new Descriptor("bool");
                    if (var2 > var1)
                        d.setValue(0);
                    else d.setValue(1);

                    semanticStack.push(d);
                }
                else if (type.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);

                        var_name1 = FtoHex(var_name1);
                        var_name2 = FtoHex(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n" +
                                "mtc1 $k1,$f31\n" +
                                "c.le.s $f31,$f30\n");
                        text = text.concat("bc1f label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1t label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        var_name1 = FtoHex(var_name1);
                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");

                        if (var_name2.equals("$f32"))
                            text = text.concat("mov.s $f31,$f29\n");
                        else
                            text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("c.le.s $f31,$f30\n");

                        text = text.concat("bc1f label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1t label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov. $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        var_name2 = FtoHex(var_name2);
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k1,$f31\n");

                        text = text.concat("c.le.s $f31,$f30\n");

                        text = text.concat("bc1f label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1t label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f31,$f29\n");
                        else
                            text = text.concat("l.s $f31,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("c.le.s $f31,$f30\n");

                        text = text.concat("bc1f label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1t label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    }

                    Descriptor d = new Descriptor("bool");
                    if (Double.compare(var2,var1) > 0)
                        d.setValue(0);
                    else d.setValue(1);

                    semanticStack.push(d);
                }

                break;
            }
            case "littleequal":{
                System.out.println("littleequal is running");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();

                String type = checkType(o1,o2,"<=","littleequal");

                String var_name1;
                String var_name2;

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("ble $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bgt $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("ble $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bgt $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        text = text.concat("li $k1," + var_name2 + "\n");

                        text = text.concat("ble $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bgt $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("ble $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bgt $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    }

                    Descriptor d = new Descriptor("bool");
                    if (var2 <= var1)
                        d.setValue(0);
                    else d.setValue(1);

                    semanticStack.push(d);
                }
                else if (type.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);

                        var_name1 = FtoHex(var_name1);
                        var_name2 = FtoHex(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n" +
                                "mtc1 $k1,$f31\n" +
                                "c.le.s $f31,$f30\n");
                        text = text.concat("bc1t label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1f label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        var_name1 = FtoHex(var_name1);
                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");

                        if (var_name2.equals("$f32"))
                            text = text.concat("mov.s $f31,$f29\n");
                        else
                            text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("c.le.s $f31,$f30\n");

                        text = text.concat("bc1t label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1f label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov. $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        var_name2 = FtoHex(var_name2);
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k1,$f31\n");

                        text = text.concat("c.le.s $f31,$f30\n");

                        text = text.concat("bc1t label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1f label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f31,$f29\n");
                        else
                            text = text.concat("l.s $f31,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("c.le.s $f31,$f30\n");

                        text = text.concat("bc1t label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1f label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    }

                    Descriptor d = new Descriptor("bool");
                    if (Double.compare(var2,var1) <= 0)
                        d.setValue(0);
                    else d.setValue(1);

                    semanticStack.push(d);
                }


                break;
            }
            case "biggerequal":{
                System.out.println("biggerequal is running");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();

                String type = checkType(o1,o2,">=","biggerequal");

                String var_name1;
                String var_name2;

                if (type.equals("int")){
                    int var1;
                    int var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("bge $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("blt $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Integer.parseInt(var_name1);
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        text = text.concat("li $k0," + var_name1 + "\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("bge $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("blt $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Integer.parseInt(var_name2);
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        text = text.concat("li $k1," + var_name2 + "\n");

                        text = text.concat("bge $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("blt $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else {
                        var_name1 = o1.toString();
                        var1 = Integer.parseInt(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Integer.parseInt(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$t10"))
                            text = text.concat("move $k0,$t9\n");
                        else
                            text = text.concat("lw $k0,(" + symbolTable.get(var_name1).address + ")\n");
                        if (var_name2.equals("$t10"))
                            text = text.concat("move $k1,$t9\n");
                        else
                            text = text.concat("lw $k1,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("bge $k1,$k0,label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("blt $k1,$k0,label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    }

                    Descriptor d = new Descriptor("bool");
                    if (var2 >= var1)
                        d.setValue(0);
                    else d.setValue(1);

                    semanticStack.push(d);
                }
                else if (type.equals("real")){
                    double var1;
                    double var2;

                    if (o1 instanceof Descriptor && o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);

                        var_name1 = FtoHex(var_name1);
                        var_name2 = FtoHex(var_name2);

                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n" +
                                "mtc1 $k1,$f31\n" +
                                "c.lt.s $f31,$f30\n");
                        text = text.concat("bc1f label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1t label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o1 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o1;
                        var_name1 = d.value.toString();
                        var1 = Double.parseDouble(var_name1);
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        var_name1 = FtoHex(var_name1);
                        text = text.concat("li $k0," + var_name1 + "\n");
                        text = text.concat("mtc1 $k0,$f30\n");

                        if (var_name2.equals("$f32"))
                            text = text.concat("mov.s $f31,$f29\n");
                        else
                            text = text.concat("l.s $f31,(" + symbolTable.get(var_name2).address + ")\n");

                        text = text.concat("c.lt.s $f31,$f30\n");

                        text = text.concat("bc1f label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1t label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else if (o2 instanceof Descriptor) {
                        Descriptor d = (Descriptor) o2;
                        var_name2 = d.value.toString();
                        var2 = Double.parseDouble(var_name2);
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov. $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        var_name2 = FtoHex(var_name2);
                        text = text.concat("li $k1," + var_name2 + "\n");
                        text = text.concat("mtc1 $k1,$f31\n");

                        text = text.concat("c.lt.s $f31,$f30\n");

                        text = text.concat("bc1f label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1t label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    } else {
                        var_name1 = o1.toString();
                        var1 = Double.parseDouble(symbolTable.get(var_name1).value.toString());
                        var_name2 = o2.toString();
                        var2 = Double.parseDouble(symbolTable.get(var_name2).value.toString());

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f30,$f29\n");
                        else
                            text = text.concat("l.s $f30,(" + symbolTable.get(var_name1).address + ")\n");

                        if (var_name1.equals("$f32"))
                            text = text.concat("mov.s $f31,$f29\n");
                        else
                            text = text.concat("l.s $f31,(" + symbolTable.get(var_name1).address + ")\n");

                        text = text.concat("c.lt.s $f31,$f30\n");

                        text = text.concat("bc1f label" + LabelCounter + "\n");
                        LabelCounter++;
                        text = text.concat("bc1t label" + LabelCounter+ "\n");
                        LabelCounter--;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,0\n");
                        text = text.concat("b continue"+continueCounter+"\n");
                        LabelCounter++;
                        text = text.concat("label" + LabelCounter + ":\n");
                        text = text.concat("li $t9,1\n");
                        text = text.concat("continue" + continueCounter + ":\n");
                        LabelCounter++;
                        continueCounter++;

                    }

                    Descriptor d = new Descriptor("bool");
                    if (Double.compare(var2,var1) >= 0)
                        d.setValue(0);
                    else d.setValue(1);

                    semanticStack.push(d);
                }


                break;
            }
            case "minusminus":{
                System.out.println("minusminus is running");

                Object o = semanticStack.pop();

                if (symbolTable.get(o).type.equals("int")){
                    int val = Integer.parseInt(symbolTable.get(o).value.toString());
                    --val;

                    text = text.concat("li $k0,1\n");
                    text = text.concat("lw $k1,("+symbolTable.get(o).address+")\n");
                    text = text.concat("sub $t9,$k1,$k0\n");
                    text = text.concat("sw $t9,("+symbolTable.get(o).address+")\n");

                    symbolTable.get(o).setValue(val);

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(val);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");

                }
                else if (symbolTable.get(o).type.equals("real")){
                    double val = Double.parseDouble(symbolTable.get(o).value.toString());
                    --val;

                    text = text.concat("li $k0,0x3f800000\n");
                    text = text.concat("mtc1 $k0,$f30\n");
                    text = text.concat("l.s $f31,("+symbolTable.get(o).address+")\n");
                    text = text.concat("sub.s $f29,$f31,$f30\n");
                    text = text.concat("s.s $f29,("+symbolTable.get(o).address+")\n");

                    symbolTable.get(o).setValue(val);

                    symbolTable.put("$f32", new Descriptor("real"));
                    symbolTable.get("$f32").value = Double.toString(val);
                    symbolTable.get("$f32").address = "$f29";
                    semanticStack.push("$f32");
                }

                break;
            }
            case "plusplus":{
                System.out.println("plusplus is running");

                Object o = semanticStack.pop();

                if (symbolTable.get(o).type.equals("int")){
                    int val = Integer.parseInt(symbolTable.get(o).value.toString());
                    ++val;

                    text = text.concat("li $k0,1\n");
                    text = text.concat("lw $k1,("+symbolTable.get(o).address+")\n");
                    text = text.concat("add $t9,$k1,$k0\n");
                    text = text.concat("sw $t9,("+symbolTable.get(o).address+")\n");

                    symbolTable.get(o).setValue(val);

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(val);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");

                }
                else if (symbolTable.get(o).type.equals("real")){
                    double val = Double.parseDouble(symbolTable.get(o).value.toString());
                    --val;

                    text = text.concat("li $k0,0x3f800000\n");
                    text = text.concat("mtc1 $k0,$f30\n");
                    text = text.concat("l.s $f31,("+symbolTable.get(o).address+")\n");
                    text = text.concat("add.s $f29,$f31,$f30\n");
                    text = text.concat("s.s $f29,("+symbolTable.get(o).address+")\n");

                    symbolTable.get(o).setValue(val);

                    symbolTable.put("$f32", new Descriptor("real"));
                    symbolTable.get("$f32").value = Double.toString(val);
                    symbolTable.get("$f32").address = "$f29";
                    semanticStack.push("$f32");
                }

                break;
            }
            case "minusminuspost":{
                System.out.println("minusminus is running");

                Object o = semanticStack.pop();

                if (symbolTable.get(o).type.equals("int")){
                    int val = Integer.parseInt(symbolTable.get(o).value.toString());


                    text = text.concat("li $k0,1\n");
                    text = text.concat("lw $k1,("+symbolTable.get(o).address+")\n");
                    text = text.concat("sub $t9,$k1,$k0\n");
                    text = text.concat("sw $t9,("+symbolTable.get(o).address+")\n");

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(val);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");

                    --val;
                    symbolTable.get(o).setValue(val);

                }
                else if (symbolTable.get(o).type.equals("real")){
                    double val = Double.parseDouble(symbolTable.get(o).value.toString());

                    text = text.concat("li $k0,0x3f800000\n");
                    text = text.concat("mtc1 $k0,$f30\n");
                    text = text.concat("l.s $f31,("+symbolTable.get(o).address+")\n");
                    text = text.concat("sub.s $f29,$f31,$f30\n");
                    text = text.concat("s.s $f29,("+symbolTable.get(o).address+")\n");


                    symbolTable.put("$f32", new Descriptor("real"));
                    symbolTable.get("$f32").value = Double.toString(val);
                    symbolTable.get("$f32").address = "$f29";
                    semanticStack.push("$f32");

                    --val;
                    symbolTable.get(o).setValue(val);
                }

                break;
            }
            case "pluspluspost":{
                System.out.println("plusplus is running");

                Object o = semanticStack.pop();

                if (symbolTable.get(o).type.equals("int")){
                    int val = Integer.parseInt(symbolTable.get(o).value.toString());

                    text = text.concat("li $k0,1\n");
                    text = text.concat("lw $k1,("+symbolTable.get(o).address+")\n");
                    text = text.concat("add $t9,$k1,$k0\n");
                    text = text.concat("sw $t9,("+symbolTable.get(o).address+")\n");

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(val);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");

                    ++val;
                    symbolTable.get(o).setValue(val);

                }
                else if (symbolTable.get(o).type.equals("real")){
                    double val = Double.parseDouble(symbolTable.get(o).value.toString());

                    text = text.concat("li $k0,0x3f800000\n");
                    text = text.concat("mtc1 $k0,$f30\n");
                    text = text.concat("l.s $f31,("+symbolTable.get(o).address+")\n");
                    text = text.concat("add.s $f29,$f31,$f30\n");
                    text = text.concat("s.s $f29,("+symbolTable.get(o).address+")\n");

                    symbolTable.put("$f32", new Descriptor("real"));
                    symbolTable.get("$f32").value = Double.toString(val);
                    symbolTable.get("$f32").address = "$f29";
                    semanticStack.push("$f32");

                    ++val;
                    symbolTable.get(o).setValue(val);
                }

                break;
            }
            case "not":{
                System.out.println("not is running");

                Object o = semanticStack.pop();
                semanticStack.push( !Boolean.parseBoolean(symbolTable.get(o).value.toString()) );

                if (o instanceof Descriptor){
                    text = text.concat("li $k0,"+symbolTable.get(o).value.toString()+"\n");
                }
                else if (o.toString().equals("$t10")){
                    text = text.concat("move $k0,$t9\n");
                }
                else{
                    text = text.concat("lw $k0,("+symbolTable.get(o).address+")\n");
                }

                break;
            }
            case "and":{
                System.out.println("and is running");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();
                String var1 = symbolTable.get(o1).toString();
                String var2 = symbolTable.get(o2).toString();

                String type = checkType(o1,o2,"&&","and");

                String b1 = symbolTable.get(o1).value.toString();
                String b2 = symbolTable.get(o2).value.toString();

                if (o1 instanceof Descriptor && o2 instanceof Descriptor){
                    if (b1.equals("true"))
                        text = text.concat("li $k0,"+0+"\n");
                    else
                        text = text.concat("li $k0,"+1+"\n");
                    if (b2.equals("true"))
                        text = text.concat("li $k1,"+0+"\n");
                    else
                        text = text.concat("li $k1,"+1+"\n");

                    text = text.concat("add $t9,$k0,$k1\n" +
                            "bgt $t9,1,label"+LabelCounter+"\n" +
                            "ble $t9,1,label"+ ++LabelCounter +"\n" +
                            "label"+ --LabelCounter +":\n" +
                            "addi $t9,$t9,-1\n" +
                            "label"+ ++LabelCounter +":\n");
                }
                else if (o1 instanceof Descriptor){
                    if (b1.equals("true"))
                        text = text.concat("li $k0,"+0+"\n");
                    else
                        text = text.concat("li $k0,"+1+"\n");
                    if (var2.equals("$t10")){
                        text = text.concat("move $k1,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k1,("+symbolTable.get(o2).address+")\n");
                    }
                    text = text.concat("add $t9,$k0,$k1\n" +
                            "bgt $t9,1,label"+LabelCounter+"\n" +
                            "ble $t9,1,label"+ ++LabelCounter+"\n" +
                            "label"+ --LabelCounter+":\n" +
                            "addi $t9,$t9,-1\n" +
                            "label"+ ++LabelCounter+":\n");
                }
                else if (o2 instanceof Descriptor){
                    if (var1.equals("$t10")){
                        text = text.concat("move $k0,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k0,("+symbolTable.get(o1).address+")\n");
                    }
                    if (b2.equals("true"))
                        text = text.concat("li $k1,"+0+"\n");
                    else
                        text = text.concat("li $k1,"+1+"\n");
                    text = text.concat("add $t9,$k0,$k1\n" +
                            "bgt $t9,1,label"+LabelCounter+"\n" +
                            "ble $t9,1,label"+ ++LabelCounter+"\n" +
                            "label"+ --LabelCounter+":\n" +
                            "addi $t9,$t9,-1\n" +
                            "label"+ ++LabelCounter+":\n");
                }
                else {
                    if (var1.equals("$t10")){
                        text = text.concat("move $k0,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k0,("+symbolTable.get(o1).address+")\n");
                    }
                    if (var2.equals("$t10")){
                        text = text.concat("move $k1,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k1,("+symbolTable.get(o2).address+")\n");
                    }
                    text = text.concat("add $t9,$k0,$k1\n" +
                            "bgt $t9,1,label"+LabelCounter+"\n" +
                            "ble $t9,1,label"+ ++LabelCounter+"\n" +
                            "label"+ --LabelCounter+":\n" +
                            "addi $t9,$t9,-1\n" +
                            "label"+ ++LabelCounter+":\n");
                }

                int result ;
                if (b1.equals("true") && b2.equals("true"))
                    result = 0;
                else result = 1;
                LabelCounter++;

                symbolTable.put("$t10", new Descriptor("int"));
                symbolTable.get("$t10").value = Integer.toString(result);
                symbolTable.get("$t10").address = "$t9";
                semanticStack.push("$t10");

                semanticStack.push(result);
                break;
            }
            case "or":{
                System.out.println("or is running");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();
                String var1 = symbolTable.get(o1).toString();
                String var2 = symbolTable.get(o2).toString();

                String type = checkType(o1,o2,"||","or");

                String b1 = symbolTable.get(o1).value.toString();
                String b2 = symbolTable.get(o2).value.toString();

                if (o1 instanceof Descriptor && o2 instanceof Descriptor){
                    if (b1.equals("true"))
                        text = text.concat("li $k0,"+0+"\n");
                    else
                        text = text.concat("li $k0,"+1+"\n");
                    if (b2.equals("true"))
                        text = text.concat("li $k1,"+0+"\n");
                    else
                        text = text.concat("li $k1,"+1+"\n");
                    text = text.concat("add $t9,$k0,$k1\n" +
                            "bgt $t9,1,label"+LabelCounter+"\n" +
                            "ble $t9,1,label"+ ++LabelCounter+"\n" +
                            "label"+ --LabelCounter+":\n" +
                            "li $t9,1\n" +
                            "label"+ ++LabelCounter+":\n" +
                            "li $t9,0\n");
                }
                else if (o1 instanceof Descriptor){
                    if (b1.equals("true"))
                        text = text.concat("li $k0,"+0+"\n");
                    else
                        text = text.concat("li $k0,"+1+"\n");
                    if (var2.equals("$t10")){
                        text = text.concat("move $k1,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k1,("+symbolTable.get(o2).address+")\n");
                    }
                    text = text.concat("add $t9,$k0,$k1\n" +
                            "bgt $t9,1,label"+LabelCounter+"\n" +
                            "ble $t9,1,label"+ ++LabelCounter+"\n" +
                            "label"+ --LabelCounter+":\n" +
                            "li $t9,1\n" +
                            "label"+ ++LabelCounter+":\n" +
                            "li $t9,0\n");
                }
                else if (o2 instanceof Descriptor){
                    if (var1.equals("$t10")){
                        text = text.concat("move $k0,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k0,("+symbolTable.get(o1).address+")\n");
                    }
                    if (b2.equals("true"))
                        text = text.concat("li $k1,"+0+"\n");
                    else
                        text = text.concat("li $k1,"+1+"\n");
                    text = text.concat("add $t9,$k0,$k1\n" +
                            "bgt $t9,1,label"+LabelCounter+"\n" +
                            "ble $t9,1,label"+ ++LabelCounter+"\n" +
                            "label"+ --LabelCounter+":\n" +
                            "li $t9,1\n" +
                            "label"+ ++LabelCounter+":\n" +
                            "li $t9,0\n");
                }
                else {
                    if (var1.equals("$t10")){
                        text = text.concat("move $k0,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k0,("+symbolTable.get(o1).address+")\n");
                    }
                    if (var2.equals("$t10")){
                        text = text.concat("move $k1,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k1,("+symbolTable.get(o2).address+")\n");
                    }
                    text = text.concat("add $t9,$k0,$k1\n" +
                            "bgt $t9,1,label"+LabelCounter+"\n" +
                            "ble $t9,1,label"+ ++LabelCounter+"\n" +
                            "label"+ --LabelCounter+":\n" +
                            "li $t9,1\n" +
                            "label"+ ++LabelCounter+":\n" +
                            "li $t9,0\n");
                }

                int result ;
                if (!b1.equals("false") && !b2.equals("false"))
                    result = 0;
                else result = 1;

                loopcunter++;

                symbolTable.put("$t10", new Descriptor("int"));
                symbolTable.get("$t10").value = Integer.toString(result);
                symbolTable.get("$t10").address = "$t9";
                semanticStack.push("$t10");

                break;
            }
            case "xor":{
                System.out.println("xor is running");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();

                String type = checkType(o1,o2,"^","xor");


                if (o1 instanceof Descriptor && o2 instanceof Descriptor){

                    String var1 =( (Descriptor)o1 ).value.toString();
                    String var2 =( (Descriptor)o2 ).value.toString();

                    int b1 = Integer.parseInt(var1);
                    int b2 = Integer.parseInt(var2);

                    text = text.concat("li $k0,"+var1+"\n");
                    text = text.concat("li $k1,"+var2+"\n");
                    text = text.concat("xor $t9,$k0,$k1"+"\n");

                    int result = b1^b2;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(result);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (o1 instanceof Descriptor){

                    String var1 =( (Descriptor)o1 ).value.toString();
                    String var2 = symbolTable.get(o2).value.toString();

                    int b1 = Integer.parseInt(var1);
                    int b2 = Integer.parseInt(var2);

                    text = text.concat("li $k0,"+var1+"\n");
                    if (var2.equals("$t10")){
                        text = text.concat("move $k1,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k1,("+symbolTable.get(o2).address+")\n");
                    }
                    text = text.concat("xor $t9,$k0,$k1"+"\n");

                    int result = b1^b2;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(result);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (o2 instanceof Descriptor){

                    String var1 =symbolTable.get(o1).value.toString();
                    String var2 =( (Descriptor)o2 ).value.toString();

                    int b1 = Integer.parseInt(var1);
                    int b2 = Integer.parseInt(var2);

                    if (var1.equals("$t10")){
                        text = text.concat("move $k0,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k0,("+symbolTable.get(o1).address+")\n");
                    }
                    text = text.concat("li $k1,"+var2+"\n");
                    text = text.concat("xor $t9,$k0,$k1"+"\n");

                    int result = b1^b2;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(result);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else {
                    String var1 =symbolTable.get(o1).value.toString();
                    String var2 =symbolTable.get(o2).value.toString();

                    int b1 = Integer.parseInt(var1);
                    int b2 = Integer.parseInt(var2);

                    if (var1.equals("$t10")){
                        text = text.concat("move $k0,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k0,("+symbolTable.get(o1).address+")\n");
                    }
                    if (var2.equals("$t10")){
                        text = text.concat("move $k1,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k1,("+symbolTable.get(o2).address+")\n");
                    }
                    text = text.concat("xor $t9,$k0,$k1"+"\n");

                    int result = b1^b2;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(result);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }

                break;
            }
            case "bitwiseand":{
                System.out.println("bitwiseand is running");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();

                String type = checkType(o1,o2,"&","bitwiseand");

                if (o1 instanceof Descriptor && o2 instanceof Descriptor){

                    String var1 = ((Descriptor) o1).value.toString();
                    String var2 = ((Descriptor) o2).value.toString();
                    int b1 = Integer.parseInt(var1);
                    int b2 = Integer.parseInt(var2);

                    text = text.concat("li $k0,"+var1+"\n");
                    text = text.concat("li $k1,"+var2+"\n");
                    text = text.concat("and $t9,$k0,$k1"+"\n");

                    int result = b1&b2;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(result);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (o1 instanceof Descriptor){

                    String var1 = ((Descriptor) o1).toString();
                    String var2 = symbolTable.get(o2).value.toString();
                    int b1 = Integer.parseInt(var1);
                    int b2 = Integer.parseInt(symbolTable.get(o2).value.toString());

                    text = text.concat("li $k0,"+var1+"\n");
                    if (var2.equals("$t10")){
                        text = text.concat("move $k1,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k1,("+symbolTable.get(o2).address+")\n");
                    }
                    text = text.concat("and $t9,$k0,$k1"+"\n");

                    int result = b1&b2;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(result);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (o2 instanceof Descriptor){

                    String var1 = symbolTable.get(o1).value.toString();
                    String var2 = ((Descriptor) o2).value.toString();
                    int b1 = Integer.parseInt(symbolTable.get(o1).value.toString());
                    int b2 = Integer.parseInt(var2);

                    if (var1.equals("$t10")){
                        text = text.concat("move $k0,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k0,("+symbolTable.get(o1).address+")\n");
                    }
                    text = text.concat("li $k1,"+var2+"\n");
                    text = text.concat("and $t9,$k0,$k1"+"\n");

                    int result = b1&b2;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(result);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else {

                    String var1 = symbolTable.get(o1).value.toString();
                    String var2 = symbolTable.get(o2).value.toString();
                    int b1 = Integer.parseInt(symbolTable.get(o1).value.toString());
                    int b2 = Integer.parseInt(symbolTable.get(o2).value.toString());

                    if (var1.equals("$t10")){
                        text = text.concat("move $k0,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k0,("+symbolTable.get(o1).address+")\n");
                    }
                    if (var2.equals("$t10")){
                        text = text.concat("move $k1,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k1,("+symbolTable.get(o2).address+")\n");
                    }
                    text = text.concat("and $t9,$k0,$k1"+"\n");

                    int result = b1&b2;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(result);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");

                }



                break;
            }
            case "bitwiseor":{
                System.out.println("bitwiseor is running");

                Object o1 = semanticStack.pop();
                Object o2 = semanticStack.pop();


                String type = checkType(o1,o2,"|","bitwiseor");


                if (o1 instanceof Descriptor && o2 instanceof Descriptor){

                    String var1 = ((Descriptor) o1).value.toString();
                    String var2 = ((Descriptor) o2).value.toString();
                    int b1 = Integer.parseInt(var1);
                    int b2 = Integer.parseInt(var2);

                    text = text.concat("li $k0,"+var1+"\n");
                    text = text.concat("li $k1,"+var2+"\n");
                    text = text.concat("or $t9,$k0,$k1"+"\n");

                    int result = b1|b2;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(result);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (o1 instanceof Descriptor){

                    String var1 = ((Descriptor) o1).value.toString();
                    String var2 = symbolTable.get(o2).toString();
                    int b1 = Integer.parseInt(var1);
                    int b2 = Integer.parseInt(symbolTable.get(o2).value.toString());

                    text = text.concat("li $k0,"+var1+"\n");
                    if (var2.equals("$t10")){
                        text = text.concat("move $k1,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k1,("+symbolTable.get(o2).address+")\n");
                    }
                    text = text.concat("or $t9,$k0,$k1"+"\n");

                    int result = b1|b2;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(result);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else if (o2 instanceof Descriptor){

                    String var1 = symbolTable.get(o1).toString();
                    String var2 = ((Descriptor) o2).value.toString();
                    int b1 = Integer.parseInt(symbolTable.get(o1).value.toString());
                    int b2 = Integer.parseInt(var2);

                    if (var1.equals("$t10")){
                        text = text.concat("move $k0,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k0,("+symbolTable.get(o1).address+")\n");
                    }
                    text = text.concat("li $k1,"+var2+"\n");
                    text = text.concat("or $t9,$k0,$k1"+"\n");

                    int result = b1|b2;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(result);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }
                else {

                    String var1 = symbolTable.get(o1).value.toString();
                    String var2 = symbolTable.get(o2).value.toString();
                    int b1 = Integer.parseInt(symbolTable.get(o1).value.toString());
                    int b2 = Integer.parseInt(symbolTable.get(o2).value.toString());

                    if (var1.equals("$t10")){
                        text = text.concat("move $k0,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k0,("+symbolTable.get(o1).address+")\n");
                    }
                    if (var2.equals("$t10")){
                        text = text.concat("move $k1,$t9"+"\n");
                    }
                    else {
                        text = text.concat("lw $k1,("+symbolTable.get(o2).address+")\n");
                    }
                    text = text.concat("or $t9,$k0,$k1"+"\n");

                    int result = b1|b2;

                    symbolTable.put("$t10", new Descriptor("int"));
                    symbolTable.get("$t10").value = Integer.toString(result);
                    symbolTable.get("$t10").address = "$t9";
                    semanticStack.push("$t10");
                }


                break;
            }
            case "readint":{
                System.out.println("readint is running");

                text = text.concat("li $v0,5\nsyscall\nmove $t9,$v0\n");
                symbolTable.put("$t10", new Descriptor("int"));

                symbolTable.get("$t10").value = "Integer.toString(input)";
                symbolTable.get("$t10").address = "$t9";
                semanticStack.push("$t10");

                break;
            }
            case "readstring":{
                System.out.println("readstring is running");

                text = text.concat("li $v0,8\n" +
                        "la $a0,buffer\n" +
                        "li $a1,32\n" +
                        "move $t9, $a0\n" +
                        "syscall\n");

                symbolTable.put("$t10", new Descriptor("string"));
                symbolTable.get("$t10").value = "input";
                symbolTable.get("$t10").address = "$t9";
                semanticStack.push("$t10");

                break;
            }
            case "readreal":{
                System.out.println("readreal is running");

                text = text.concat("li $v0,6\nsyscall\nmov.s $f29,$f0\n");
                symbolTable.put("$f32", new Descriptor("real"));

                symbolTable.get("$f32").value = "val";
                symbolTable.get("$f32").address = "$f29";
                semanticStack.push("$f32");

                break;
            }
            case "printint":{
                System.out.println("printint is running");

                Object o = semanticStack.pop();

                if (o instanceof Descriptor){
                    text = text.concat("li $v0,1\n" +
                            "li $a0,"+((Descriptor) o).value +"\n" +
                            "syscall\n");
                }
                else {
                    if (o.toString().equals("$t10")){
                        text = text.concat("li $v0,1\n" +
                                "move $a0,$t9\n" +
                                "syscall\n");
                    }
                    else
                        text = text.concat("li $v0,1\n" +
                                "lw $a0,("+symbolTable.get(o).address+")\n" +
                                "syscall\n");
                }

                break;
            }
            case "printstring":{
                System.out.println("printstring is running");

                Object o = semanticStack.pop();

                if (symbolTable.containsKey(o))
                text = text.concat("li $v0,4\n" +
                        "move $a0,"+symbolTable.get(o).address+"\n" +
                        "syscall\n");
                else if (o instanceof Descriptor){
                    data = data.concat("new_string"+newStringCounter+": .asciiz \""+ ((Descriptor) o).value +"\\n\""+"\n");
                    text = text.concat("la $t7,new_string"+newStringCounter+ "\n");
                    newStringCounter++;
                    text = text.concat("li $v0,4\n" +
                            "move $a0,$t7\n" +
                            "syscall\n");
                }

                break;
            }
            case "printreal":{
                System.out.println("printreal is running");

                Object o = semanticStack.pop();

                if (o instanceof Descriptor){
                    String val = FtoHex(o.toString());
                    text = text.concat("li $k0,"+val+"\n" +
                            "mtc1 $k0,$f29\n" +
                            "li $v0, 2\n" +
                            "mov.s $f12, $f29\n" +
                            "syscall\n");
                }
                else {
                    if (o.toString().equals("$f32")){
                        text = text.concat("li $v0, 2\n" +
                                "mov.s $f12, $f29\n" +
                                "syscall\n");
                    }
                    else
                        text = text.concat("li $v0, 2\n" +
                                "l.s $f12,("+symbolTable.get(o).address+")\n" +
                                "syscall\n");
                }

                break;
            }
            case "BR":{
                System.out.println("BR is running");

                text = text.concat(
                        "beqz $t9,st"+ifcounter+"\n" +
                        "bnez $t9,else"+elsecounter+"\n" +
                        "st"+ifcounter+":\n");
                ifcounter++;


                break;
            }
            case "CompBR":{
                System.out.println("compBR is running");

                //text = text.concat("b end"+endcounter+"\n");

                break;
            }
            case "JP":{
                System.out.println("JP is running");

                text = text.concat("b end"+endcounter+"\n");
                text = text.concat("else"+elsecounter+":\n");
                elsecounter++;

                break;
            }
            case "CompJP":{
                System.out.println("compJP is running");

                text = text.concat("b end"+endcounter+"\n");
                text = text.concat("end"+endcounter+":\n");
                endcounter++;

                break;
            }
            case "fiLabel":{
                System.out.println("filabel is running");

                text = text.concat("else"+elsecounter+":\n");
                elsecounter++;

                text = text.concat("b end"+endcounter+"\n");
                text = text.concat("end"+endcounter+":\n");
                endcounter++;

                break;
            }
            case "decide":{
                System.out.println("decide is running");

                Object o = semanticStack.pop();

                if (symbolTable.containsKey(o))
                    text = text.concat(
                            "beqz "+ symbolTable.get(o).address+",l"+ lCounter+1 +"\n" +
                            "bnez "+ symbolTable.get(o).address+",l"+ lCounter+3 +"\n");
                else text = text.concat(
                        "beqz $t9,l"+ lCounter+1 +"\n" +
                        "bnez $t9,l"+ lCounter+3 +"\n");

                text = text.concat("l"+ lCounter+1 +":\n");

                break;
            }
            case "cjnzjbe":{
                System.out.println("cjnzjbe is running");

                text = text.concat("b l"+ lCounter+2 +"\n" +
                        "l"+ lCounter+2 +":\n");
                break;
            }
            case "cjzjstep":{
                System.out.println("cjzjstep is running");

                text = text.concat("b l"+lCounter+"\n" +
                        "l"+lCounter+3+":\n");

                lCounter+=4;

                break;
            }
            case "continue":{
                System.out.println("continue is running");

                text = text.concat("b l"+lCounter+"\n");

                break;
            }
            case "break":{
                System.out.println("break is running");

                text = text.concat("b l"+ lCounter+3 +"\n");

                break;
            }
            case "save":{
                System.out.println("save is running");

                break;
            }
            case "myLabel":{
                System.out.println("myLabel is running");

                text = text.concat("l"+lCounter+":\n");

                break;
            }
            case "whilelable":{
                System.out.println("whilelabel is running");

                text = text.concat("l"+lCounter+":\n");

                break;
            }
            case "jz":{
                System.out.println("jz is running");

                Object o = semanticStack.pop();

                if (symbolTable.containsKey(o))
                    text = text.concat(
                            "beqz "+ symbolTable.get(o).address+",l"+ lCounter+1 +"\n" +
                                    "bnez "+ symbolTable.get(o).address+",l"+ lCounter+3 +"\n");
                else text = text.concat(
                        "beqz $t9,l"+ lCounter+1 +"\n" +
                                "bnez $t9,l"+ lCounter+3 +"\n");

                text = text.concat("l"+ lCounter+1 +":\n");

                break;
            }
            case "cjz":{
                System.out.println("cjz is running");

                text = text.concat("l"+lCounter+3+":\n");

                lCounter+=4;

                break;
            }
            case "jump":{
                System.out.println("jump is running");

                text = text.concat("b l"+lCounter+"\n");

                break;
            }
            case "realtoint_id":{
                System.out.println("realtoint_id is running");

                if(!symbolTable.get(lexical.currentSymbol.getValue().toString()).type.equals("real"))
                    throw new TypeMismatchException("this id is not real");

                //mips:set int value for d
                text = text.concat("l.s $f29,("+symbolTable.get(lexical.currentSymbol.getValue().toString()).address+")\n" +
                        "cvt.w.s $f29,$f29\n" +
                        "mfc1 $t9,$f29\n");

                Descriptor descriptor = new Descriptor("int");
                double tmp=Double.parseDouble(symbolTable.get(lexical.currentSymbol.getValue().toString()).value.toString());
                descriptor.setValue(Integer.toString((int)tmp));
                semanticStack.push(descriptor);

                break;
            }
            case "inttoreal_id":{
                System.out.println("inttoreal_id is running");

                if(!symbolTable.get(lexical.currentSymbol.getValue().toString()).type.equals("int"))
                    throw new TypeMismatchException("this id is not int");

                //mips:set real value for d
                text = text.concat("l.s $f29,("+symbolTable.get(lexical.currentSymbol.getValue().toString()).address+")\n" +
                        "cvt.s.w $f29,$f29\n");

                Descriptor descriptor = new Descriptor("real");
                int tmp=Integer.parseInt(symbolTable.get(lexical.currentSymbol.getValue().toString()).value.toString());
                descriptor.setValue(Double.valueOf(tmp).toString());
                semanticStack.push(descriptor);

                break;
            }
            case "inttoreal_ic":{
                System.out.println("inttoreal_ic is running");

                Descriptor d= new Descriptor("real");
                int tmp=Integer.parseInt(lexical.currentSymbol.getValue().toString());
                d.setValue(Double.toString((double)tmp));
                semanticStack.push(d);

                text = text.concat("li $t9,"+lexical.currentSymbol.getValue().toString()+"\n" +
                        "mtc1 $t9,$f29\n" +
                        "cvt.s.w $f29,$f29\n");

                break;
            }
            case "realtoint_rc":{
                System.out.println("realtoint_rc is running");

                Descriptor d= new Descriptor("int");
                double tmp=Double.parseDouble(lexical.currentSymbol.getValue().toString());
                d.setValue(Integer.toString((int)tmp));
                semanticStack.push(d);

                String val = lexical.currentSymbol.getValue().toString();
                val = FtoHex(val);

                text = text.concat("li $t9,"+val+"\n" +
                        "mtc1 $t9,$f29\n" +
                        "cvt.w.s $f29,$f29\n" +
                        "mfc1 $t9,$f29\n");

                break;
            }
            default:
                System.out.println("Not Supported : "+sem);
        }
    }
}