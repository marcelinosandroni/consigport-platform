package br.com.consigport.central.security;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Request wrapper that keeps the body readable after the HMAC filter consumed it.
 */
class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

  private final byte[] body;

  CachedBodyHttpServletRequest(HttpServletRequest request, byte[] body) {
    super(request);
    this.body = body.clone();
  }

  @Override
  public ServletInputStream getInputStream() {
    ByteArrayInputStream input = new ByteArrayInputStream(body);
    return new ServletInputStream() {
      @Override
      public int read() {
        return input.read();
      }

      @Override
      public int read(byte[] bytes, int offset, int length) {
        return input.read(bytes, offset, length);
      }

      @Override
      public boolean isFinished() {
        return input.available() == 0;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener listener) {
        throw new UnsupportedOperationException("Leitura assíncrona não suportada no mock.");
      }
    };
  }

  @Override
  public BufferedReader getReader() {
    return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
  }
}
