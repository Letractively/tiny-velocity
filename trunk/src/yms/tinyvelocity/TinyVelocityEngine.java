package yms.tinyvelocity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A template engine with syntax similar to Apache Velocity but simpler. it
 * supports only <b>if</b> and <b>foreach</b>. The engine is so simple and
 * lightweight that it can also work at GWT client side.
 * 
 * Example template:
 * 
 * <pre>
 * #if ($condition)
 *         Condition is true! 
 *         #foreach ($ii in $keys)
 *            Hi: ${ii}    
 *         #end
 *     #else
 *         #foreach ($zz in $keys)
 *            ${zz} is the best!   
 *         #end
 *     #end
 * 
 * </pre>
 * 
 * @author Yotam Madem
 * 
 */
public class TinyVelocityEngine {
	private static final String CRLF = "\\r?\\n";

	// All the "tokens" declarations
	private static final String END_STR = "#end";
	private static final String FOREACH_STR = "#foreach";
	private static final String ELSE_STR = "#else";
	private static final String IF_STR = "#if";

	private static final int TOK_IF = 1;
	private static final int TOK_ELSE = 2;
	private static final int TOK_FOREACH = 3;
	private static final int TOK_END = 10;

	/**
	 * Parse the given template and return the output as string. the
	 * valuesProvider is needed to return the values of the template variables
	 * and their inner properties.
	 * 
	 * @param template
	 * @param valuesProvider
	 * @return
	 */
	public String parse(String template, final ValuesProvider valuesProvider) {
		StringBuffer buff = new StringBuffer();

		String lines[] = template.split(CRLF);
		ArrayList<Scope> scopes = new ArrayList<Scope>();
		scopes.add(createFirstScope(valuesProvider));

		parse(lines, new int[] { 0 }, lines.length - 1, new int[] {}, scopes,
				valuesProvider, buff);

		return buff.toString();
	}

	public class Scope {
		TreeMap<String, Object> vars = null;
		ValuesProvider valuesProvider = null;

		public Scope(ValuesProvider provider, boolean isFirst) {
			this.valuesProvider = provider;
			if (!isFirst) {
				vars = new TreeMap<String, Object>();
			}
		}

		public void setVarValue(String varName, Object value) {
			if (vars != null) {
				vars.put(varName, value);
			}
		}

		public Object getVarValue(String varName) {
			String[] elements = varName.split("\\.");
			if (elements.length > 0) {
				Object obj = null;
				if (vars == null) {
					obj = valuesProvider.getProperty(null, elements[0]);
				} else {
					obj = vars.get(elements[0]);
				}
				if (obj == null) {
					return null;
				}
				for (int i = 1; i < elements.length; i++) {
					obj = valuesProvider.getProperty(obj, elements[i]);
					if (obj == null) {
						return null;
					}
				}
				return obj;
			}
			return null;
		}

		public boolean containsVar(String varId) {
			return getVarValue(varId) != null;
		}
	}

	private int parse(String[] content, int[] pos, int to, int[] terminators,
			ArrayList<Scope> scopes, ValuesProvider provider,
			StringBuffer buff) {

		scopes.add(0, createScope(provider));

		try {
			int lines = 0;
			while (pos[0] <= to) {
				if (pos[0] >= content.length) {
					throw new Error("Parse error: expected token(s): "
							+ getArrayAsString(terminators) + " but found EOF.");
				}

				lines++;
				String line = content[pos[0]];
				String tr = line.trim();
				if (!tr.startsWith("#")) {
					format(line, scopes, buff, provider);
					pos[0]++;
				} else {
					int tokenCode = getTokenCode(tr);
					for (int terminator : terminators) {
						if (tokenCode == terminator) {
							return lines;
						}
					}
					switch (tokenCode) {
					case TOK_IF:
						parseIf(content, pos, scopes, provider, buff);
						break;
					case TOK_END:
						break;
					case TOK_ELSE:
						break;
					case TOK_FOREACH:
						parseForeach(content, pos, scopes, provider, buff);
						break;
					default:
						pos[0]++;
						format(line, scopes, buff, provider);
						break;
					}
				}
			}
			return lines;

		} finally {
			scopes.remove(0);
		}
	}

	private void format(String line, ArrayList<Scope> scopes,
			StringBuffer buff, ValuesProvider provider) {
		TreeSet<String> vars = new TreeSet<String>();
		getVarsOfLine(line, vars);
		for (String v : vars) {
			Object value = getVarValue(scopes, v);
			if (value != null) {
				line = line.replaceAll("\\$\\{" + v + "\\}",
						provider.getObjectAsString(value));
			}
		}
		buff.append(line + "\r\n");
	}

	private void getVarsOfLine(String line, TreeSet<String> vars) {
		String vName = "";
		boolean readingVar = false;
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == '$') {
				vName = "";
				readingVar = true;
			} else {
				if (readingVar) {
					if (isLegalVarCharacter(line.charAt(i), vName)) {
						vName += line.charAt(i);
					} else {
						readingVar = false;
						if (vName.startsWith("{") && vName.endsWith("}")) {
							vars.add(vName.substring(1, vName.length() - 1));
						}
						i--;
					}
				}
			}
		}
		if (vName.length() > 0) {
			if (vName.startsWith("{") && vName.endsWith("}")) {
				vars.add(vName.substring(1, vName.length() - 1));
			}
		}
	}

	private boolean isLegalVarCharacter(char charAt, String varNameSoFar) {
		if (charAt >= '0' && charAt <= '9') {
			if (varNameSoFar.length() > 0) {
				return true; // digit can appear in var name but not at the
								// beginning.
			}
		}
		if (charAt >= 'a' && charAt <= 'z') {
			return true;
		}
		if (charAt >= 'A' && charAt <= 'Z') {
			return true;
		}
		if (charAt == '_') {
			return true;
		}
		if (charAt == '.') {
			return true;
		}
		if (charAt == '{') {
			return true;
		}
		if (charAt == '}') {
			return true;
		}
		return false;
	}

	private int parseForeach(String[] content, int[] pos,
			ArrayList<Scope> scopes, ValuesProvider provider,
			StringBuffer buff) {
		String line = content[pos[0]].trim();
		pos[0]++;
		int from = line.indexOf('(');
		int to = line.indexOf(')');
		line = line.substring(from + 1, to);

		ArrayList<String> tokens = new ArrayList<String>();
		for (String elem : line.split(" ")) {
			if (!elem.trim().isEmpty()) {
				tokens.add(elem);
			}
		}

		if (tokens.size() < 3) {
			throw new Error("foreach syntax is wrong: "
					+ content[pos[0]].trim());
		}

		String loopVarName = tokens.get(0);
		String containerName = tokens.get(2);

		Scope myScope = createScope(provider);
		myScope.setVarValue(removeDollarIfExist(loopVarName), "");
		scopes.add(0, myScope);
		int savePos = pos[0];

		Object container = getVarValue(scopes, containerName);
		Collection<?> c = getAsCollection(container);
		if (c != null) {
			if (c.isEmpty()) {
				skipTo(content, pos, new int[] { TOK_END });
			} else {
				for (Object obj : c) {
					Object o2 = obj;
					if (o2 == null){
						o2 = "null";
					}
					myScope.setVarValue(removeDollarIfExist(loopVarName), o2);
					pos[0] = savePos;
					parse(content, pos, Integer.MAX_VALUE,
							new int[] { TOK_END }, scopes, provider, buff);
				}
			}
		} else {
			throw new Error("The container: " + container
					+ " is not collection or an array");
		}
		scopes.remove(0);
		pos[0]++;
		return 0;
	}

	private Collection<?> getAsCollection(Object collectionOrArray) {
		ArrayList<Object> lst = new ArrayList<Object>();
		if (collectionOrArray instanceof Collection<?>){
			return (Collection<?>) collectionOrArray;
		}else if (collectionOrArray instanceof Object[]){
			Object[] arr = (Object[]) collectionOrArray;
			for (Object obj : arr){
				lst.add(obj);
			}
			return lst;
		}else if (collectionOrArray instanceof int[]){
			int[] arr = (int[]) collectionOrArray;
			for (int obj : arr){
				lst.add(obj);
			}
			return lst;
		}else if (collectionOrArray instanceof double[]){
			double[] arr = (double[]) collectionOrArray;
			for (double obj : arr){
				lst.add(obj);
			}
			return lst;
		}else if (collectionOrArray instanceof long[]){
			long[] arr = (long[]) collectionOrArray;
			for (long obj : arr){
				lst.add(obj);
			}
			return lst;
		}else if (collectionOrArray instanceof byte[]){
			byte[] arr = (byte[]) collectionOrArray;
			for (byte obj : arr){
				lst.add(obj);
			}
			return lst;
		}else if (collectionOrArray instanceof char[]){
			char[] arr = (char[]) collectionOrArray;
			for (char obj : arr){
				lst.add(obj);
			}
			return lst;
		}else if (collectionOrArray instanceof float[]){
			float[] arr = (float[]) collectionOrArray;
			for (float obj : arr){
				lst.add(obj);
			}
			return lst;
		}else if (collectionOrArray instanceof boolean[]){
			boolean[] arr = (boolean[]) collectionOrArray;
			for (boolean obj : arr){
				lst.add(obj);
			}
			return lst;
		}
		return null;
	}

	private String removeDollarIfExist(String varName) {
		if (varName.charAt(0) == '$') {
			return varName.substring(1);
		} else {
			return varName;
		}
	}

	private int getTokenCode(String tr) {
		if (tr.startsWith(IF_STR)) {
			return TOK_IF;
		} else if (tr.startsWith(ELSE_STR)) {
			return TOK_ELSE;
		} else if (tr.startsWith(FOREACH_STR)) {
			return TOK_FOREACH;
		} else if (tr.startsWith(END_STR)) {
			return TOK_END;
		}
		return 0;
	}

	private int parseIf(String[] content, int[] pos, ArrayList<Scope> scopes,
			ValuesProvider provider, StringBuffer buff) {
		if (checkCond(content[pos[0]], scopes)) {
			pos[0]++;
			parse(content, pos, Integer.MAX_VALUE, new int[] { TOK_ELSE,
					TOK_END }, scopes, provider, buff);
			int lastTok = getTokenCode(content[pos[0]].trim());
			if (lastTok == TOK_END) {
				pos[0]++;
				return 0;
			} else { // TOK_ELSE
				skipTo(content, pos, new int[] { TOK_END });
				pos[0]++;
				return 0;
			}
		} else {
			pos[0]++;
			skipTo(content, pos, new int[] { TOK_END, TOK_ELSE });
			int lastTok = getTokenCode(content[pos[0]].trim());
			if (lastTok == TOK_ELSE) {
				pos[0]++;
				parse(content, pos, Integer.MAX_VALUE, new int[] { TOK_END },
						scopes, provider, buff);
				pos[0]++;
				return 0;
			} else {
				pos[0]++;
				return 0;
			}
		}
	}

	private int skipTo(String[] content, int[] pos, int terminators[]) {
		ArrayList<Integer> stack = new ArrayList<Integer>();
		while (true) {
			if (pos[0] >= content.length) {
				throw new Error("Parse error: expected token(s): "
						+ getArrayAsString(terminators) + " but found EOF.");
			}
			int t = getTokenCode(content[pos[0]].trim());
			if (stack.isEmpty()) {
				for (int tk : terminators) {
					if (t == tk) {
						return 0;
					}
				}
			}
			if (isOpeningTok(t)) {
				stack.add(0, t);
			} else if (isColsingTok(t)) {
				stack.remove(0);
			}
			pos[0]++;
		}
	}

	private String getArrayAsString(int[] terminators) {
		String str = "";
		for (int term : terminators) {
			if (str.length() > 0) {
				str += ", ";
			}
			str += getTokenName(term);
		}
		return str;
	}

	private String getTokenName(int tok) {
		switch (tok) {
		case TOK_ELSE:
			return ELSE_STR;
		case TOK_END:
			return END_STR;
		case TOK_FOREACH:
			return FOREACH_STR;
		case TOK_IF:
			return IF_STR;
		default:
			return "N/A";
		}
	}

	private boolean isColsingTok(int t) {
		return t == TOK_END;
	}

	private boolean isOpeningTok(int t) {
		return t == TOK_IF || t == TOK_FOREACH;
	}

	private boolean checkCond(String expression, ArrayList<Scope> scopes) {
		expression = expression.substring(expression.indexOf("(") + 2,
				expression.lastIndexOf(')'));
		expression = expression.trim();
		Object value = getVarValue(scopes, expression);
		if (value != null && (value instanceof Boolean)) {
			return ((Boolean) value).booleanValue();
		}
		return false;
	}

	private Object getVarValue(ArrayList<Scope> scopes, String varId) {
		if (varId.startsWith("$")) {
			varId = varId.substring(1);
		}
		for (Scope scope : scopes) {
			if (scope.containsVar(varId)) {
				return scope.getVarValue(varId);
			}
		}
		return null;
	}

	private Scope createFirstScope(final ValuesProvider valuesProvider) {
		return new Scope(valuesProvider, true);
	}

	public Scope createScope(final ValuesProvider valuesProvider) {
		return new Scope(valuesProvider, false);
	}

	
	static class Model{
		public int idea=8;
		public String word="bla";
	}

}