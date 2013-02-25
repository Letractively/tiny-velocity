package yms.tinyvelocity.test;

import java.util.ArrayList;
import java.util.TreeMap;

import org.junit.Test;

import yms.tinyvelocity.TinyVelocityEngine;
import yms.tinyvelocity.providers.POJOValuesProvider;
import yms.tinyvelocity.providers.TreeMapValuesProvider;

import junit.framework.TestCase;

public class Tests extends TestCase {

	TinyVelocityEngine engine = new TinyVelocityEngine();
	
	public static class Model{
		public int var = 10;
		public String var2="yyy";
	}

	public static class ModelGetters{
		private int var = 10;
		private String var2="yyy";
		public int getVar() {
			return var;
		}
		public void setVar(int var) {
			this.var = var;
		}
		public String getVar2() {
			return var2;
		}
		public void setVar2(String var2) {
			this.var2 = var2;
		}
	}

	@Test
	public void testVarsTreeMap(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		model.put("var", 10);
		model.put("var2", "joe");
		String result = engine.parse("Hi there ${var},${var2}", new TreeMapValuesProvider(model)).trim();
		assertEquals("Hi there 10,joe",result);
	}
	
	@Test
	public void testVarsPOJO(){
		String result = engine.parse("Hi there ${var},${var2}", new POJOValuesProvider(new Model())).trim();
		assertEquals("Hi there 10,yyy",result);
	}
	
	@Test
	public void testVarsPOJOGetters(){
		String result = engine.parse("Hi there ${var},${var2}", new POJOValuesProvider(new ModelGetters())).trim();
		assertEquals("Hi there 10,yyy",result);
	}

	@Test
	public void testIF(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		model.put("truefact", true);
		model.put("falsefact", false);
		String result = engine.parse("#if ($truefact)\n" +
									  "TRUE\n" +
									  "#end", new TreeMapValuesProvider(model)).trim();
		assertEquals("TRUE",result);
	}

	@Test
	public void testIF2(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		model.put("truefact", true);
		model.put("falsefact", false);
		String result = engine.parse("#if ($falsefact)\n" +
									  "TRUE\n" +
									  "#end", new TreeMapValuesProvider(model)).trim();
		assertEquals("",result);
	}
	
	@Test
	public void testIfELSE(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		model.put("truefact", true);
		model.put("falsefact", false);
		String result = engine.parse("#if ($falsefact)\n" +
									  "FALSE\n" +
									  "#else\n" +
									  "TRUE\n" +
									  "#end", new TreeMapValuesProvider(model)).trim();
		assertEquals("TRUE",result);
	}
	
	@Test
	public void testIfELSE2(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		model.put("truefact", true);
		model.put("falsefact", false);
		String result = engine.parse("#if ($truefact)\n" +
									  "FALSE\n" +
									  "#else\n" +
									  "TRUE\n" +
									  "#end", new TreeMapValuesProvider(model)).trim();
		assertEquals("FALSE",result);
	}
	
	@Test
	public void testIfELSE3(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		String result = engine.parse("#if ($nonexistvar)\n" +
									  "FALSE\n" +
									  "#else\n" +
									  "TRUE\n" +
									  "#end", new TreeMapValuesProvider(model)).trim();
		assertEquals("TRUE",result);
	}

	@Test
	public void testForeachEmpty(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		ArrayList<Integer> lst = new ArrayList<Integer>();
		model.put("items", lst);
		String result = engine.parse("#foreach($item in $items)\n" +
									  "BBB\n" +
									  "#end", new TreeMapValuesProvider(model)).trim();
		assertEquals("",result);
	}

	@Test
	public void testForeach(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		ArrayList<Integer> lst = new ArrayList<Integer>();
		lst.add(1);
		lst.add(2);
		lst.add(3);
		model.put("items", lst);
		String result = engine.parse("#foreach($item in $items)\n" +
									  "BBB\n" +
									  "#end", new TreeMapValuesProvider(model)).trim();
		assertEquals("BBB\r\n" +
					 "BBB\r\n" +
					 "BBB",result);
	}

	@Test
	public void testForeach2(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		ArrayList<Integer> lst = new ArrayList<Integer>();
		lst.add(1);
		lst.add(2);
		lst.add(3);
		model.put("items", lst);
		String result = engine.parse("#foreach($item in $items)\n" +
									  "BBB:${item}\n" +
									  "#end", new TreeMapValuesProvider(model)).trim();
		assertEquals("BBB:1\r\n" +
					 "BBB:2\r\n" +
					 "BBB:3",result);
	}

	@Test
	public void testForeachArray(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		
		model.put("items", new int[]{1,2,3});
		String result = engine.parse("#foreach($item in $items)\n" +
									  "BBB:${item}\n" +
									  "#end", new TreeMapValuesProvider(model)).trim();
		assertEquals("BBB:1\r\n" +
					 "BBB:2\r\n" +
					 "BBB:3",result);
	}

	@Test
	public void testScopes(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		ArrayList<Integer> lst = new ArrayList<Integer>();
		lst.add(1);
		lst.add(2);
		lst.add(3);
		model.put("items", lst);
		model.put("item", "YY");
		String result = engine.parse("${item}\n" +
									  "#foreach($item in $items)\n" +
									  "BBB:${item}\n" +
									  "#end", new TreeMapValuesProvider(model)).trim();
		assertEquals("YY\r\n" +
					 "BBB:1\r\n" +
					 "BBB:2\r\n" +
					 "BBB:3",result);
	}

	@Test
	public void testNested(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		ArrayList<Object> lst = new ArrayList<Object>();
		lst.add(1);
		lst.add(true);
		lst.add(3);
		model.put("items", lst);
		model.put("item", "YY");
		String result = engine.parse("${item}\n" +
									  "#foreach($item in $items)\n" +
									  	"#if ($item)\n" +
									  		"BBB:${item}\n" +
									  	"#else\n" +
									  		"CCC:${item}\n" +
									  	"#end\n" +
									  "#end", new TreeMapValuesProvider(model)).trim();
		assertEquals("YY\r\n" +
					 "CCC:1\r\n" +
					 "BBB:true\r\n" +
					 "CCC:3",result);
	}

	@Test
	public void testNestedProperty(){
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		model.put("a", new Model());
		String result = engine.parse("${a.var}\n", new POJOValuesProvider(model)).trim();
		assertEquals("10",result);
	}


}
