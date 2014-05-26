package com.augustl.pathtravelagent;

class TestHandler implements IRouteHandler<TestReq, TestRes> {
    private final String ret;

    public TestHandler() {
        this.ret = null;
    }

    public TestHandler(String ret) {
        this.ret = ret;
    }

    @Override
    public TestRes call(RouteMatch<TestReq> match) {
        return new TestRes(this.ret);
    }
}
