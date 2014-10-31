package org.srobo.ide.api;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamHandler<IS extends InputStream> {
    void handleData(IS is) throws IOException;
}
