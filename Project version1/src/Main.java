import codegenerator.COOLException.DeclarationException;
import codegenerator.COOLException.TypeMismatchException;
import codegenerator.CodeGenerator;
import parser.Parser;
import scanner.CompilerScanner;
import java.io.*;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, DeclarationException, TypeMismatchException {
        String inputAddress = "src/in.txt";
        String tableAddress = "src/parser/n.npt";
        CompilerScanner scanner = new CompilerScanner(new FileReader(inputAddress));
        CodeGenerator codeGenerator = new CodeGenerator(scanner);
        Parser parser = new Parser(scanner, codeGenerator, tableAddress);
        // For debugging parser, use bellow
        // Parser parser = new Parser(scanner, codeGenerator, tableAddress, true);

        parser.parse();

        try {
            FileWriter fileWriter = new FileWriter("src/mips1.s");

            fileWriter.write(".text\n.globl main\nmain:\n");
            fileWriter.write(codeGenerator.getText());
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
