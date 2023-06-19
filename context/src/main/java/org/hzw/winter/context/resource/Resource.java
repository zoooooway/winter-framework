package org.hzw.winter.context.resource;

/**
 * @author hzw
 */
public class Resource {

    private final String relativePath;

    public Resource(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getRelativePath() {
        return relativePath;
    }
}
