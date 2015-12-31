package org.jboss.tools.examples.datagrid;

import java.lang.annotation.Annotation;

public class CachedQualifier implements Annotation{

	public CachedQualifier() {
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return Cached.class;
	}
}
