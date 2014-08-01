package com.augustl.pathtravelagent;

class TestRes {
    private final String body;

    public TestRes(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestRes) {
            return ((TestRes)obj).getBody().equals(this.getBody());
        }

        return super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString() + "<body:" + this.body + ">";
    }
}
