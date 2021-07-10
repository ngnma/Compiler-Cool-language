//import com.sun.java_cup.internal.runtime.Symbol;
import java.io.*;

/**
* This class is a Scanner
*/
%%

%class CompilerScanner
%unicode
%line
%column
%function nextSymbol
%type Symbol

%{
  StringBuffer string = new StringBuffer();  
  
  private String html = "<html>\n" +
            "<body>";

  public Symbol currentSymbol;
  private Symbol symbol(String tokenName, Object value) {
    return new Symbol(tokenName,value);
  }

  public void end() {
    html += "</body>\n" +
                            "</html>";
    try {
        File file = new File("src/tmp.html");
        FileWriter f = new FileWriter("C:/Users/HOME/IdeaProjects/Scanner/src/tmp.html");
          f.write(html);
          f.close();
        }catch (IOException e) {
          System.out.println("ERROR");
        }
  
  }

  private void IdentifierE() {
      html += "<l style=\"color:violet; \">" + yytext() +"</l>";
  }
            
  private void keyword() {
      html += "<l style=\"color:blue; font-weight : bold;\">" + yytext() +"</l>";
  }

  private void integerN() {
      html += "<l style=\"color:orange;\">" + yytext() +"</l>";
  }

  private void realNum() {
      html += "<l style=\"color:orange;\">" + "<i>" + yytext() + "</i>" +"</l>";
  }

  private void stringS() {
      html += "<l style=\"color:green;\">" + string +"</l>";
  }

  private void comment() {
      html += "<l style=\"color:yellow;\">" + yytext()+"</l>";
  }

  private void operator() {
      html += "<l style=\"color:black;\">" + yytext()+"</l>";
  }

  private void undefindToken() {
      html += "<l style=\"color:red;\">" + yytext()+"</l>";
  }

%}

LineTerminator = \r|\n|\r\n
SPACE = " "
InputCharacter = [^\r\n]

    /* comments */
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}

TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
// Comment can be the last line of the file, without line terminator.
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?
DocumentationComment = "/**" {CommentContent} "*"+ "/"
CommentContent       = ( [^*] | \* + [^/*] )*

Identifier = [:jletter:][:jletterdigit:]{0,30}

Sign = [-]?
DecIntegerLiteral = {Sign} 0 | [1-9][0-9]*
HexadecimalLiteral = {Sign} "0" [xX] ( [0-9] | [a-fA-F] )+
RealNumber =  {Sign} [1-9][0-9]* "." [0-9]*
ScientificNotation = {Sign} [1-9][0-9]* "." [0-9]* "E" {Sign} [0-9]*

%state STRING

%%

  /* keywords */
  <YYINITIAL> {
    
    "bool" | "break" | "int" | "void" | "real" | "string" | "class" | 
    "for" | "while" | "if" | "else" | "return" | "rof" | "let" | "fi" |
    "Array" | "in_string" | "out_string" | "new" | "continue" | "loop" | 
    "pool" | "then" | "len" | "in_int" | "out_int" | "out_real" | "Main"
                                   {
                                   keyword();
                                   yybegin(YYINITIAL);
                                   return symbol("keyword",yytext());}

  }

  <YYINITIAL> {
    /* identifiers */
    "true"  {yybegin(YYINITIAL); return symbol("true",yytext());}
   "false" {yybegin(YYINITIAL); return symbol("false",yytext());} 
    {Identifier}                   { IdentifierE(); yybegin(YYINITIAL); return symbol("Identifier",yytext()); }
     
    /* literals */
    {DecIntegerLiteral}            { integerN();yybegin(YYINITIAL); return symbol("DecIntegerLiteral",yytext());}
    {HexadecimalLiteral}           { integerN();yybegin(YYINITIAL); return symbol("HexadecimalLiteral",yytext());}
    {RealNumber}                  { realNum();yybegin(YYINITIAL); return symbol("RealNumber",yytext());}
    {ScientificNotation}         { integerN();yybegin(YYINITIAL); return symbol("ScientificNotation",yytext());}
    \"                             { string.setLength(0); yybegin(STRING);}

    /* operators */
    "=" | "==" | "+" | "-" | "*" | "/" |
    "+=" | "-=" | "/=" | "*=" | "++" |
    "--" | "!=" | "<" | ">" | "<=" | ">=" |
    "<-" | "%" | "&&" | "||" | "&" | "|" |
    "“" | "^" | "!" | "." | ";" |
    "(" | ")" | "{" | "}" | "[" | "]" | ":"   {operator(); yybegin(YYINITIAL); return symbol("operator",yytext());}

    "," {operator(); yybegin(YYINITIAL); return symbol("comma",yytext());}

    

    /* comments */
    {Comment}                      { comment(); html += "<l style=\"color:red;\"> <br> </l>"; return symbol("comment",yytext());}

    {LineTerminator}               { html += "<l style=\"color:red;\"> <br> </l>"; return symbol("LineTerminator",yytext());}
    {SPACE}                        { html += "<l style=\"color:red;\"> &nbsp </l>"; return symbol("SPACE",yytext());}
    
     
  }

  <STRING> {
    \"                             {yybegin(YYINITIAL);}
                    
    [^\n\r\"\\]+                   { string.append( yytext() ); html += "<l style=\"color:green;\">" + yytext() + "</l>";return symbol("String",yytext());}
    \\t                            { string.append('\t'); html += "<l style=\"color:green;\">" + "<i>" +"\\t"+ "</i>" +"</l>";}
    \\n                            { string.append('\n'); html += "<l style=\"color:green;\">" + "<i>" +"\\n"+ "</i>" +"</l>";}
    \\r                            { string.append('\r'); html += "<l style=\"color:green;\">" + "<i>" +"\\r"+ "</i>" +"</l>";}
    \\\"                           { string.append('\"'); html += "<l style=\"color:green;\">" + "<i>" + "\\\"" + "</i>" +"</l>";}
    \\                             { string.append('\\'); html += "<l style=\"color:green;\">" + "<i>" + "\\" + "</i>" +"</l>";}
   
  }

  

  /* error fallback */
  [^]                              { undefindToken(); return symbol(null,null);}