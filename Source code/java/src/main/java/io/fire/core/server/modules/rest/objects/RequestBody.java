package io.fire.core.server.modules.rest.objects;

import lombok.AllArgsConstructor;

import java.io.InputStream;
import java.util.Scanner;

@AllArgsConstructor
public class RequestBody {

    private InputStream inputStream;

    public InputStream asStream() {
        return inputStream;
    }

    public String asString() {
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
