package org.hzw.winter.context.resource;

/**
 * @author hzw
 */
public class Resource {

    private final String relativePath;

    private final String name;

    public Resource(String relativePath, String name) {
        this.relativePath = relativePath;
        this.name = name;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getName() {
        return name;
    }
}
