package server.TripToN.global.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    @Test
    void resolve_usesForwardedForAddress() {
        MockHttpServletRequest request = requestWithRemoteAddress("192.168.65.1");
        request.addHeader("X-Forwarded-For", "223.1.2.3");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("223.1.2.3");
    }

    @Test
    void resolve_usesFirstForwardedForAddressAndTrimsWhitespace() {
        MockHttpServletRequest request = requestWithRemoteAddress("192.168.65.1");
        request.addHeader("X-Forwarded-For", " 223.1.2.3, 172.18.0.1 ");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("223.1.2.3");
    }

    @Test
    void resolve_usesRealIpWhenForwardedForIsMissing() {
        MockHttpServletRequest request = requestWithRemoteAddress("192.168.65.1");
        request.addHeader("X-Real-IP", " 223.1.2.3 ");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("223.1.2.3");
    }

    @Test
    void resolve_usesRemoteAddressWhenProxyHeadersAreMissing() {
        MockHttpServletRequest request = requestWithRemoteAddress(" 2001:db8::1 ");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("2001:db8::1");
    }

    @Test
    void resolve_fallsBackWhenForwardedForContainsInvalidInput() {
        MockHttpServletRequest request = requestWithRemoteAddress("192.168.65.1");
        request.addHeader("X-Forwarded-For", "223.1.2.3\r\nInjected-Header: value");
        request.addHeader("X-Real-IP", "223.1.2.4");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("223.1.2.4");
    }

    @Test
    void resolve_returnsNullWhenNoCandidateIsAValidIpAddress() {
        MockHttpServletRequest request = requestWithRemoteAddress("not-an-ip");
        request.addHeader("X-Forwarded-For", "unknown");
        request.addHeader("X-Real-IP", "999.1.2.3");

        assertThat(ClientIpResolver.resolve(request)).isNull();
    }

    private MockHttpServletRequest requestWithRemoteAddress(String remoteAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddress);
        return request;
    }
}
