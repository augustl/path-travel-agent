package com.augustl.pathtravelagent;

class TestReq implements IRequest {
    private final String path;
    private final Object extras;

    public TestReq(String path) {
        this.path = path;
        this.extras = null;
    }

    public TestReq(String path, Object extras) {
        this.path = path;
        this.extras = extras;
    }

    public Object getExtras() {
        return this.extras;
    }


    @Override
    public String getPath() {
        return this.path;
    }
}
