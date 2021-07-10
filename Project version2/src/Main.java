import codegenerator.COOLException.DeclarationException;
import codegenerator.COOLException.TypeMismatchException;
import codegenerator.CodeGenerator;
import codegenerator.MethodDescriptor;
import parser.Parser;
import scanner.CompilerScanner;
import java.io.*;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, DeclarationException, TypeMismatchException {
        String inputAddress = "src/input.d";
        String tableAddress = "src/parser/graph.npt";
        CompilerScanner scanner = new CompilerScanner(new FileReader(inputAddress));
        CodeGenerator codeGenerator = new CodeGenerator(scanner);
        Parser parser = new Parser(scanner, codeGenerator, tableAddress);
        // For debugging parser, use bellow
        // Parser parser = new Parser(scanner, codeGenerator, tableAddress, true);

        parser.parse();

        try {
            FileWriter fileWriter = new FileWriter("src/output.s");

            fileWriter.write(".text\n.globl Main\nMain:\n");
            fileWriter.write(codeGenerator.getText());
            fileWriter.write("li $v0, 10\n" +
                    "syscall\n");
            int i = codeGenerator.getTextOfMethods().size()-1;
            while (i>=0){
                fileWriter.write(codeGenerator.getTextOfMethods().get(i));
                i--;
            }
            fileWriter.write(".data\n");
            fileWriter.write("buffer: .space 32\n");
            fileWriter.write("endline: .asciiz \"\\n\"\n");
            fileWriter.write("space: .asciiz \" \"\n");
            fileWriter.write(codeGenerator.getData());

            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
