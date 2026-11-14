import javax.tools.*;
import com.sun.source.util.JavacTask;
import java.io.File;
import java.util.*;

/** Syntax-only Java parser; does not replace a Gradle/Android compilation. */
class JavaSyntaxCheck {
    public static void main(String[] args) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("Requires JDK, not JRE");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, null)) {
            List<File> files = new ArrayList<>();
            for (String argument : args) files.add(new File(argument));
            Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
            JavacTask task = (JavacTask) compiler.getTask(null, manager, diagnostics,
                Arrays.asList("-proc:none", "-encoding", "UTF-8"), null, units);
            task.parse();
            for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                if (d.getKind() == Diagnostic.Kind.ERROR) throw new AssertionError(d);
            }
            System.out.println("PASS: " + files.size() + " Java source files parsed (syntax only)");
        }
    }
}
