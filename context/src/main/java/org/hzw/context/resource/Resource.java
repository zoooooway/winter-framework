package org.hzw.context.resource;

/**
 * @author hzw
 */
public class Resource {

    private String relativePath;

    public Resource(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getRelativePath() {
        return relativePath;
    }
}
