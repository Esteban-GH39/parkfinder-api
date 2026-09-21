/*package com.parkfinder.clientes;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Provider
@Priority(1)
public class DebugRequestFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        System.out.println("=== DEBUG REQUEST ===");
        System.out.println("Metodo: " + requestContext.getMethod());
        System.out.println("Path: " + requestContext.getUriInfo().getPath());
        requestContext.getHeaders().forEach((k, v) ->
            System.out.println("Header: " + k + " = " + v));

        InputStream is = requestContext.getEntityStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        String body = baos.toString("UTF-8");
        System.out.println("Body recibido: [" + body + "]");
        System.out.println("Body length (bytes): " + baos.size());
        System.out.println("=====================");

        requestContext.setEntityStream(new ByteArrayInputStream(baos.toByteArray()));
    }
}