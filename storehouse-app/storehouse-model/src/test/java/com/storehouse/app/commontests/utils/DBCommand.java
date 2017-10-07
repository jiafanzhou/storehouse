package com.storehouse.app.commontests.utils;

import org.junit.Ignore;

@Ignore
public interface DBCommand<T> {
    T execute();
}
