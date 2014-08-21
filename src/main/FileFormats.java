package main;

public class FileFormats {
/*
 * 

 *  		'~' = /AppData/(file name)/
 *  
 *  
 *---------APK Info (~/ApkInfo.csv)----------
 * 		Line1(0,1,2): Apk Path, Class Number, Time of Analysis
 * 		Line2(0,1):	  "Class", Class Name
 * 		...
 * 		...
 * 
 * 
 * 
 * --------Class Info (~/ClassesInfo/(Class Name)/ClassInfo.csv)---------
 * 		Line1(0,1,2): Class Name, Field Number, Method Number
 * 		Line2(0,1,2,3): "Field", Field Name, Field Type, Field Modifiers Value
 * 		...
 * 		...
 * 		LineX(0,1,2,3,4,5): "Method", Method Name, Return Type, Method Modifiers Value, Method File Name, Method SubSignature
 * 											(note: sometimes method signature is too long for a file name, that's why some methods' file name != sub signature)
 * 		...
 * 		...
 * 
 * 
 * 
 * --------Method Info (~/ClassesInfo/ClassName/MethodFileName.csv)
 * 		Line1(0,1,2,3,4,5): Method Name, Return Type, Method Modifiers Value, Parameter Count, Local Count, MethodSubSig
 * 		Line2(0,1,2):	"Param", Variable Name, Variable Type
 * 		...
 * 		...
 * 		LineX(0,1,2): 	"Local", Variable Name, Variable Type
 * 		...
 * 		...
 * 		LineY(0,1,2,3,4):	"FieldRef", tgtField Name, Ftgtield Type, tgtField Declaring Class, the statement, stmt line number in the method
 * 		LineZ(0,1,2,3,4):	"MethodCall", tgtMethod Name, tgtMethod Declaring Class, tgtMethod Subsignature, the statement, stmt line number in the method
 * 		...
 * 		...
 * 
 * 
 *  --------Call Graph Format (~/CallGraph.csv)
 *  
 *  Note: FieldRefs and MethodCalls are not separately stored, all mixed together
 *  
 * 		LineA(0,1,2,3,4,5,6,7,8):	"FieldRef", srcClass name, srcMethod name, tgtField Declaring Class, tgtField Name, tgtField Type, stmt, line number, column number
 *  	LineB(0,1,2,3,4,5,6,7):	"MethodCall", srcClass name, srcMethod name, tgtClass name, tgtMethod Subsignature, stmt, line number, column number
 *  
 *  
 *  
 *  
 *  
 * soot.Modifier
public static final int	ABSTRACT	1024
public static final int	ANNOTATION	8192
public static final int	ENUM	16384
public static final int	FINAL	16
public static final int	INTERFACE	512
public static final int	NATIVE	256
public static final int	PRIVATE	2
public static final int	PROTECTED	4
public static final int	PUBLIC	1
public static final int	STATIC	8
public static final int	STRICTFP	2048
public static final int	SYNCHRONIZED	32
public static final int	TRANSIENT	128
public static final int	VOLATILE	64

 * 
 * */
}
