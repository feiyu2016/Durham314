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
 * 		Line1(0,1,2): Class Name, Field Count, Method Count
 * 		Line2(0,1,2,3): "Field", Field Name, Field Type, Field Modifiers Value
 * 		...
 * 		...
 * 		LineX(0,1,2,3,4,5): "Method", Method Name, Return Type, Method Modifiers Value, Method File Name, Method SubSignature
 * 			(note: sometimes method signature is too long to be used as a file name, that's why some methods' file name != sub signature)
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
 * 		LineY(0,1,2,3,4):	"FieldRef", tgtField Name, Ftgtield Type, tgtField Declaring Class, stmt line number in the method
 * 		LineZ(0,1,2,3,4):	"MethodCall", tgtMethod Name, tgtMethod Declaring Class, tgtMethod Subsignature, stmt line number in the method
 * 		...
 * 		...
 * 
 * 
 *  --------Call Graph Format (~/CallGraph.csv)
 *  
 *  Note: FieldRefs and MethodCalls are not separately stored, all mixed together
 *  
 * 		LineA(0,1,2,3,4,5):	"FieldRef", srcClass name, srcMethod Subsignature, tgtClass name, tgtField Subsignature, stmt line number
 *  	LineB(0,1,2,3,4,5):	"MethodCall", srcClass name, srcMethod Subsignature, tgtClass name, tgtMethod Subsignature, stmt line number
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
 * 
 * ** jdb command list **
connectors                -- list available connectors and transports in this VM

run [class [args]]        -- start execution of application's main class

threads [threadgroup]     -- list threads
thread <thread id>        -- set default thread
suspend [thread id(s)]    -- suspend threads (default: all)
resume [thread id(s)]     -- resume threads (default: all)
where [<thread id> | all] -- dump a thread's stack
wherei [<thread id> | all]-- dump a thread's stack, with pc info
up [n frames]             -- move up a thread's stack
down [n frames]           -- move down a thread's stack
kill <thread id> <expr>   -- kill a thread with the given exception object
interrupt <thread id>     -- interrupt a thread

print <expr>              -- print value of expression
dump <expr>               -- print all object information
eval <expr>               -- evaluate expression (same as print)
set <lvalue> = <expr>     -- assign new value to field/variable/array element
locals                    -- print all local variables in current stack frame

classes                   -- list currently known classes
class <class id>          -- show details of named class
methods <class id>        -- list a class's methods
fields <class id>         -- list a class's fields

threadgroups              -- list threadgroups
threadgroup <name>        -- set current threadgroup

stop in <class id>.<method>[(argument_type,...)]
                          -- set a breakpoint in a method
stop at <class id>:<line> -- set a breakpoint at a line
clear <class id>.<method>[(argument_type,...)]
                          -- clear a breakpoint in a method
clear <class id>:<line>   -- clear a breakpoint at a line
clear                     -- list breakpoints
catch [uncaught|caught|all] <class id>|<class pattern>
                          -- break when specified exception occurs
ignore [uncaught|caught|all] <class id>|<class pattern>
                          -- cancel 'catch' for the specified exception
watch [access|all] <class id>.<field name>
                          -- watch access/modifications to a field
unwatch [access|all] <class id>.<field name>
                          -- discontinue watching access/modifications to a field
trace [go] methods [thread]
                          -- trace method entries and exits.
                          -- All threads are suspended unless 'go' is specified
trace [go] method exit | exits [thread]
                          -- trace the current method's exit, or all methods' exits
                          -- All threads are suspended unless 'go' is specified
untrace [methods]         -- stop tracing method entrys and/or exits
step                      -- execute current line
step up                   -- execute until the current method returns to its caller
stepi                     -- execute current instruction
next                      -- step one line (step OVER calls)
cont                      -- continue execution from breakpoint

list [line number|method] -- print source code
use (or sourcepath) [source file path]
                          -- display or change the source path
exclude [<class pattern>, ... | "none"]
                          -- do not report step or method events for specified classes
classpath                 -- print classpath info from target VM

monitor <command>         -- execute command each time the program stops
monitor                   -- list monitors
unmonitor <monitor#>      -- delete a monitor
read <filename>           -- read and execute a command file

lock <expr>               -- print lock info for an object
threadlocks [thread id]   -- print lock info for a thread

pop                       -- pop the stack through and including the current frame
reenter                   -- same as pop, but current frame is reentered
redefine <class id> <class file name>
                          -- redefine the code for a class

disablegc <expr>          -- prevent garbage collection of an object
enablegc <expr>           -- permit garbage collection of an object

!!                        -- repeat last command
<n> <command>             -- repeat command n times
# <command>               -- discard (no-op)
help (or ?)               -- list commands
version                   -- print version information
exit (or quit)            -- exit debugger

<class id>: a full class name with package qualifiers
<class pattern>: a class name with a leading or trailing wildcard ('*')
<thread id>: thread number as reported in the 'threads' command
<expr>: a Java(tm) Programming Language expression.
Most common syntax is supported.

Startup commands can be placed in either "jdb.ini" or ".jdbrc"
in user.home or user.dir

 * 
 * 
 * */
}
