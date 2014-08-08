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
    public IRouteHandler<TestReq, TestRes> merge(IRouteHandler<TestReq, TestRes> other) {
        return other;
    }

    @Override
    public TestRes call(RouteMatch<TestReq> match) {
        return new TestRes(this.ret);
    }
}
