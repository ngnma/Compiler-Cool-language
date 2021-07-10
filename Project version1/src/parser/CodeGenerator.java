package parser;

import codegenerator.COOLException.DeclarationException;
import codegenerator.COOLException.TypeMismatchException;

public interface CodeGenerator {
    void doSemantic(String sem) throws DeclarationException, TypeMismatchException;
}