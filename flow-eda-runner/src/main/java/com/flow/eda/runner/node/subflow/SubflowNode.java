package com.flow.eda.runner.node.subflow;

import com.flow.eda.runner.node.AbstractNode;
import com.flow.eda.runner.node.NodeFunction;
import com.flow.eda.runner.node.NodeVerify;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

/** 子流程节点，运行此节点相当于运行子流程 */
public class SubflowNode extends AbstractNode {
    /** 子流程的id */
    private String subflow;
    /** 本节点的输入参数 */
    private Document input;

    public SubflowNode(Document params) {
        super(params);
    }

    @Override
    public void run(NodeFunction callback) {
        // 运行子流程
        SubFlowRuntime.startRunSubFlow(subflow, input);
        // 阻塞等待子流程运行完成
        while (SubFlowRuntime.SUB_FLOW_MAP.get(subflow)) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
        // 子流程运行结束
        SubFlowRuntime.SUB_FLOW_MAP.remove(subflow);
        setStatus(Status.FINISHED);
        // 获取子流程的输出
        Document output = output();
        if (SubFlowRuntime.SUB_OUTPUT.containsKey(subflow)) {
            output = SubFlowRuntime.SUB_OUTPUT.get(subflow);
            SubFlowRuntime.SUB_OUTPUT.remove(subflow);
        }
        callback.callback(output);
    }

    @Override
    protected void verify(Document params) {
        this.subflow = params.getString("subflow");
        NodeVerify.notNull(subflow, "subflow");

        this.input = getInput();
    }
}
