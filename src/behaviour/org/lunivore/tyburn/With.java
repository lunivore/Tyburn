package org.lunivore.tyburn;

import org.mockito.Mockito;

public class With extends Mockito {
	public static <T> T a(Class<T> clazz) {
		return isA(clazz);
	}

	public static <T> T an(Class<T> clazz) {
		return isA(clazz);
	}
}
