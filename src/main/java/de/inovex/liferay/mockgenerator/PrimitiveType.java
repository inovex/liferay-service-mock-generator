package de.inovex.liferay.mockgenerator;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;

public enum PrimitiveType {

	Z("boolean", JExpr.lit(true), Boolean.class), B("byte", null, Byte.class), C("char", JExpr.lit('t'), Character.class), D(
			"double", JExpr.lit(0.0), Double.class), F("float", JExpr.lit(0), Float.class), I("int", JExpr
			.lit(0), Integer.class), J("long", JExpr.lit(0), Long.class), S("short", null, Short.class), V("void", null, Void.class);

	private String value;

	private JExpression defaultReturnValue;
	
	private Class<?> objectType;

	private PrimitiveType(String value, JExpression returnValue, Class<?> objectType) {
		this.value = value;
		this.defaultReturnValue = returnValue;
		this.objectType = objectType;
	}

	public String getValue() {
		return value;
	}

	public JExpression getDefaultReturnValue() {
		return defaultReturnValue;
	}

	public Class<?> getObjectType() {
		return objectType;
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
