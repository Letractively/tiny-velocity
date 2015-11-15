## Description ##
Tiny (650 lines of code, 4 source files) quick and dirty template engine based on the Apache Velocity syntax, but has less functionality: supports only if and foreach. it is extremely embeddable:  it can even run from a GWT client code.

### Things you can do with tiny-velocity: ###
  * Generate html files from a template
  * Generate source code of almost any kind
  * Generate e-mails and system automated messages.
### Things you cannot do with it ###
  * Resolve complicated expressions and do calculations inside the template
  * Invoke java static methods, load custom classes, run scripts, hack and blow up your system...
  * Include other templates inside a template (will be added in the near future)
  * Make coffe

## How to use ##
In this example we use a model class like the following:
```
public class MyModel{
    public int number = 5;
    public String name = "Joe";
    public String[] list = {"One", "Two", "Three" };
    public boolean cond = true;
}
```
But you may use any javabean you like.

Now, lets write the template code: (can be in a seperate file)
```
The number is ${number}
The name is ${name}
The list is:
#foreach ($item in $list)
     ${item}
#end
#if ($cond)
Cond is true
#else
Cond is false
#end
```

The rendering program:
```
MyModel model = new MyModel();
TinyVelocityEngine engine = new TinyVelocityEngine();
String template = // get the template code somehow (e.g. file, database, network, hard-coded)
String result = engine.parse(template, new POJOValuesProvider(model));
System.out.println(result);
```
Surprisingly, the output will be:
```
The number is 5
The name is Joe
The list is:
     One
     Two
     Three
Cond is true
```

For any complains, issues, consulting, feel free to contact me: yotammadem at gmail
