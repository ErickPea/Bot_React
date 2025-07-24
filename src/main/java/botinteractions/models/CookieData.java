package botinteractions.models;

import com.microsoft.playwright.options.Cookie;

public class CookieData {
    private String name;
    private String value;
    private String domain;
    private String path;
    private double expires;

    public CookieData(String name, String value, String domain, String path, double expires) {
        this.name = name;
        this.value = value;
        this.domain = domain;
        this.path = path;
        this.expires = expires;
    }

    public Cookie toPlaywrightCookie() {
        return new Cookie(name, value)
                .setDomain(domain)
                .setPath(path)
                .setExpires(expires);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }

    public double getExpires() {
        return expires;
    }
}
