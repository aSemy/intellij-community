// "Make 'bar' return 'java.lang.String'" "true-preview"
import java.util.List;

public class Test {
  void foo() {
    List<String> l = null;
    l.add(bar());
  }
  String bar() {
      return null;
  }
}
