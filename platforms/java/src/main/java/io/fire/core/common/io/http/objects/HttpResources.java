package io.fire.core.common.io.http.objects;

import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@NoArgsConstructor
public class HttpResources {

    private Map<String, String> cache = new HashMap<>();

    /**
     * Resource cache for default web pages
     * like the 404, 500 and index HTML
     *
     * @param file
     * @return
     */
    public String get(String file) {
        if (cache.containsKey(file)) return cache.get(file);

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("html/" + file);
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String out  = s.hasNext() ? s.next() : "";
        cache.put(file, out);
        return out;
    }

}
