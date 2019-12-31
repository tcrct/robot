package com.robot.utils;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.core.AppContext;
import com.robot.service.common.ActionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 工站设备的动作队列
 * 存放动作请求对象
 *
 * Created by laotang on 2019/10/16.
 */
public class ActionsQueue {

    private static final Logger logger = LoggerFactory.getLogger(ActionsQueue.class);

    private final static Map<String, Queue<Request>> ACTION_MAP = new HashMap<>();
    private final static Map<String, List<String>> VERIFICATIONCODE_MAP = new HashMap<>();

    private static ActionsQueue actionsQueue;
    private static Lock lock = new ReentrantLock();

    public static ActionsQueue duang() {
        synchronized (lock) {
            if (null == actionsQueue) {
                actionsQueue = new ActionsQueue();
            }
            return actionsQueue;
        }
    }

    private ActionsQueue() {

    }

    public void put(String deviceId, Queue queue) {
        ACTION_MAP.put(deviceId, queue);
    }
    public void remove(String deviceId) {
        ACTION_MAP.remove(deviceId);
    }

    /**
     * 取所有工站动作请求
     * key为CRC验证码
     * @return
     */
    public Map<String ,Request> getAllRequest() {
        Map<String ,Request> allRequest = new HashMap<>();
        ACTION_MAP.entrySet().iterator().forEachRemaining(new Consumer<Map.Entry<String, Queue<Request>>>() {
            @Override
            public void accept(Map.Entry<String, Queue<Request>> entry) {
                Queue<Request> queue = entry.getValue();
                List<String> verificationCodeList = new ArrayList<>(queue.size());
                for (Request request : queue) {
                    Protocol protocol = (Protocol) request.getProtocol();
                    String verificationCode = protocol.getCode();
                    // ServiceRequest时，verificationCode为空，所以要退出本次循环
                    if (ToolsKit.isEmpty(verificationCode)) {
                        continue;
                    }
                    allRequest.put(verificationCode, request);
                    verificationCodeList.add(verificationCode);
                }
                VERIFICATIONCODE_MAP.put(entry.getKey(), verificationCodeList);
            }
        });
        AppContext.getCustomActionRequests().putAll(allRequest);
        return allRequest;
    }

    /**
     * 根据动作名称清除已经执行完成动作指令
     * @param actionKey 动作名称
     */
    public void clearVerificationCodeMap(String actionKey) {
        List<String> codeList = VERIFICATIONCODE_MAP.get(actionKey);
        if(ToolsKit.isNotEmpty(codeList)) {
            StringBuilder codeStr = new StringBuilder();
            logger.info("移除前的AppContext.getCustomActionRequests()总数为： " +AppContext.getCustomActionRequests().size());
            logger.info("移除前的AppContext.getAdvanceReportMap()总数为： " +AppContext.getAdvanceReportMap().size());
            for (String code : codeList) {
                AppContext.getCustomActionRequests().remove(code);
                AppContext.getAdvanceReportMap().remove(code);
                codeStr.append(code).append(",");
            }
            logger.info("移除VerificationCodeMap集合key为["+actionKey+"]的元素["+codeStr+"]成功!");
            logger.info("移除后的AppContext.getCustomActionRequests()总数为： " +AppContext.getCustomActionRequests().size());
            logger.info("移除后的AppContext.getAdvanceReportMap()总数为： " +AppContext.getAdvanceReportMap().size());
        }
        VERIFICATIONCODE_MAP.remove(actionKey);
    }


    public Queue<Request> getQueue(String actionKey) {
        actionKey = Objects.requireNonNull(actionKey, "指令组合名称不能为空");
        Queue<Request> queue = ACTION_MAP.get(actionKey);
        if(ToolsKit.isEmpty(queue)) {
            queue = new LinkedBlockingQueue<>();
//            queue = new PriorityBlockingQueue();
        }
        return queue;
    }



    /**
     * 添加到队列头部(第一位)
     */
    public void add2Top(String actionKey, Request request) {
        Queue<Request> queue = getQueue(actionKey);
        ActionRequest actionRequest = (ActionRequest)queue.peek();
        Double index = actionRequest.getIndex();
        if(index >= 1d) {
            index--;
        }
        if (index < 0d) {
            index = 0d;
        }
        add2Queue(queue, (ActionRequest)request,actionKey, index);
    }

    private void add2Queue(Queue<Request> queue, ActionRequest actionRequest, String actionKey, Double index) {
        actionRequest.setIndex(index);
        if(ToolsKit.isNotEmpty(queue)) {
            queue.add(actionRequest);
            sort(queue);
            put(actionKey, queue);
        }
    }

    /**
     * 添加到队列底部(最后一位)
     */
    public void add2Bottom(String actionKey, Request request) {
        Queue<Request> queue = getQueue(actionKey);
        ActionRequest actionRequest = (ActionRequest)request;
        double index = (Integer.valueOf(queue.size()).doubleValue());
        add2Queue(queue, actionRequest,actionKey, index);
    }

    /**
     * 添加到队列中任意部分
     */
    public void add2QueueByIndex(String actionKey, Request request) {
        Queue<Request> queue = getQueue(actionKey);
        ActionRequest actionRequest = (ActionRequest)request;
        double index = actionRequest.getIndex();
        add2Queue(queue, actionRequest,actionKey, index);
    }

    /**
     * 排序，index数据越少越靠前
     * @param queue
     */
    private void sort(Queue<Request> queue) {
        List<Request> requestList = new ArrayList<>(queue.size());
        requestList.addAll(queue);
        Collections.sort(requestList, new Comparator<Request>() {
            @Override
            public int compare(Request o1, Request o2) {
                if(((ActionRequest)o1).getIndex() < ((ActionRequest)o2).getIndex()) {
                    return -1;
                }
                return 1;
            }
        });
        queue.clear();
        queue.addAll(requestList);
    }



}
