package de.inovex.liferay.test;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;

public enum PrimitiveType {

	Z("boolean", JExpr.lit(true)), B("byte", null), C("char", JExpr.lit('t')), D(
			"double", JExpr.lit(0.0)), F("float", JExpr.lit(0)), I("int", JExpr
			.lit(0)), J("long", JExpr.lit(0)), S("short", null), V("void", null);

	private String value;

	private JExpression defaultReturnValue;

	private PrimitiveType(String value, JExpression returnValue) {
		this.value = value;
		this.defaultReturnValue = returnValue;
	}

	public String getValue() {
		return value;
	}

	public JExpression getDefaultReturnValue() {
		return defaultReturnValue;
	}

	public static PrimitiveType valueOf(Class<?> primitiveClass, JCodeModel codeModel) {
		if (primitiveClass.isPrimitive()) {
			if (primitiveClass == java.lang.Boolean.TYPE) {
				return Z;
			} else if (primitiveClass == java.lang.Character.TYPE) {
				return C;
			} else if (primitiveClass == java.lang.Byte.TYPE) {
				return B;
			} else if (primitiveClass == java.lang.Short.TYPE) {
				if(S.defaultReturnValue == null){
					S.defaultReturnValue = JExpr.cast(JType.parse(codeModel, "short"), JExpr.lit(0));
				}
				return S;
			} else if (primitiveClass == java.lang.Integer.TYPE) {
				return I;
			} else if (primitiveClass == java.lang.Long.TYPE) {
				return J;
			} else if (primitiveClass == java.lang.Float.TYPE) {
				return F;
			} else if (primitiveClass == java.lang.Double.TYPE) {
				return D;
			}
		}
		throw new IllegalArgumentException(primitiveClass.getName()
				+ " is not a primitive type");

	}
}
