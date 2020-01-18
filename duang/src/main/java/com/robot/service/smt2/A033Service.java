package com.robot.service.smt2;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.ProtocolParam;
import com.robot.agv.vehicle.telegrams.StateRequest;
import com.robot.mvc.annotations.Service;
import com.robot.mvc.exceptions.RobotException;
import com.robot.numes.RobotEnum;
import com.robot.service.common.BaseService;
import com.robot.utils.ProtocolUtils;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

@Service
public class A033Service extends BaseService {

    private static final Logger LOG =  LoggerFactory.getLogger(A033Service.class);

    /**
     * 下发车辆移动指令
     * @param request
     * @param response
     * @return
     */
    public String setRout(Request request, Response response) {
        if (!(request instanceof StateRequest)) {
            throw new RobotException("该请求不是移动命令请求");
        }
        StateRequest stateRequest =(StateRequest)request;
        Queue<MovementCommand> queue = stateRequest.getCommandQueue();
        MovementCommand firstCommand = queue.peek();
        String firstPont = firstCommand.getFinalDestination().getName();
        String travelParamString = "";
        if (firstPont.endsWith("50")) {
            travelParamString = "mb50::lf49::sf51";

        }
        if (firstPont.endsWith("51")) {
            travelParamString = "mb51::mf49::sf50";
        }
        // 根据规则组建下发路径指令的协议指令
        Protocol protocol = new Protocol.Builder()
                .deviceId(stateRequest.getModel().getName())
                .direction(RobotEnum.UP_LINK.getValue())
                .commandKey("setrout")
                .params(travelParamString).build();
        return ProtocolUtils.converterString(protocol);

        /*
        List<ProtocolParam> protocolParamList =  getProtocolParamList(stateRequest, response);

        if (protocolParamList.isEmpty()) {
            throw new RobotException("协议参数列表对象不能为空");
        }

        ProtocolParam lastProtocolParam = protocolParamList.get(protocolParamList.size()-1);
        String after = lastProtocolParam.getAfter();
        if (after.endsWith("50")) {
            protocolParamList.get(0).setBefore("mf49");
        }
        if (after.endsWith("51")) {
            protocolParamList.get(0).setBefore("lf49");
        }
        if (after.endsWith("49")) {
            lastProtocolParam.setAfter("ef49");
        }
        return getProtocolString(stateRequest, protocolParamList);
         */
    }

    /**
     * 上报卡号
     * @param request
     * @param response
     * @return
     */
    public String rptAc(Request request, Response response) {
        LOG.info("车辆[{}]行驶到达[{}]卡号", request.getProtocol().getDeviceId(), RobotUtil.getReportPoint(request.getProtocol()));
        return request.getRawContent();
    }

    /**
     * 预停车到位
     * @param request
     * @param response
     * @return
     */
    public String rptRtp(Request request, Response response) {
        LOG.info("车辆[{}]接收到预停车到位协议：[{}]", request.getProtocol().getDeviceId(), request.getRawContent());
        return request.getRawContent();
    }

}
